package sg.edu.nus.comp.orbital.synchro;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.JsonArray;

import java.util.ArrayList;

import sg.edu.nus.comp.orbital.synchro.AsyncTasks.AsyncTaskSendPost;
import sg.edu.nus.comp.orbital.synchro.CardViewAdapters.CardViewPostAdapter;
import sg.edu.nus.comp.orbital.synchro.DataHolders.Post;

/**
 * Created by angja_000 on 18/7/2016.
 */
public class ViewPostsFragment extends Fragment {

    private static final String GET_GROUP_ID = "Group Id";
    private static String groupId;
    private ArrayList<Post> posts;

    public ViewPostsFragment() {}

    public static ViewPostsFragment newInstance() {
        return new ViewPostsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        JsonArray postsJsonArray = null;

        if (getArguments() != null) {
            groupId = getArguments().getString(GET_GROUP_ID);
            postsJsonArray = SynchroAPI.getInstance().getPostsByGroupId(groupId);
        }

        if (postsJsonArray == null) {
            return inflater.inflate(R.layout.error_layout, container, false);
        }
        else {
            posts = Post.parsePosts(postsJsonArray);
            return inflater.inflate(R.layout.fragment_posts, container, false);
        }
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (posts == null) {
            return;
        }

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_posts);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setAdapter(new CardViewPostAdapter(posts));

        recyclerView.scrollToPosition(posts.size()-1);

        final EditText editTextPost = (EditText) view.findViewById(R.id.inputPost);
        final Button buttonPost = (Button) view.findViewById(R.id.buttonPost);

        buttonPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputStr = editTextPost.getText().toString().trim();

                if (inputStr.isEmpty()) {
                    Toast.makeText(getContext(), "Empty message", Toast.LENGTH_SHORT).show();
                }
                else {
                    Post newPost = new Post(groupId, null, inputStr, null, null);
                    AsyncTaskSendPost.load(new ProgressDialog(getContext()), newPost, getFragmentManager());
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.menu_posts, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            refreshPage(getFragmentManager());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //to be called after Async Task for posting message loaded
    public static void redirectAfterPost(boolean result, FragmentManager manager) {

        if (result) {
            Toast.makeText(App.getContext(), "Posted", Toast.LENGTH_SHORT).show();
            refreshPage(manager);
        }
        else {
            Toast.makeText(App.getContext(), "Error posting. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private static void refreshPage(FragmentManager manager) {
        ViewPostsFragment viewPostsFragment = ViewPostsFragment.newInstance();

        Bundle bundle = new Bundle();
        bundle.putString(GET_GROUP_ID, groupId);
        viewPostsFragment.setArguments(bundle);

        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.content_fragment, viewPostsFragment);
        transaction.commit();
    }
}
