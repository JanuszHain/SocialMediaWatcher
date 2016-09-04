package pl.janusz.hain.socialmediawatcher;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;
import com.twitter.sdk.android.tweetui.UserTimeline;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;

import butterknife.BindBool;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FragmentListTwitterSingleUser extends ListFragment {

    private static String ARG_TWITTER_ACCOUNT = "TWITTER_ACCOUNT";
    private boolean fragmentNewlyCreated = true;
    private TweetTimelineListAdapter adapter;
    private TwitterAccount twitterAccount;
    private EventBus eventBus;
    private ListenerConnectivity listenerConnectivity;
    private MyAsyncTask asyncTask;
    private WeakReference<MyAsyncTask> asyncTaskWeakRef;
    private InterfaceOnTwitterTimelineFragmentInteraction interfaceOnTwitterTimelineFragmentInteraction;

    @BindView(R.id.noConnection)
    protected TextView textViewNoConnection;

    @BindBool(R.bool.big_screen)
    protected boolean bigScreen;

    private Unbinder unbinder;

    public static FragmentListTwitterSingleUser newInstance(TwitterAccount twitterAccount) {
        FragmentListTwitterSingleUser fragment = new FragmentListTwitterSingleUser();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TWITTER_ACCOUNT, twitterAccount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            twitterAccount = (TwitterAccount) getArguments().getSerializable(ARG_TWITTER_ACCOUNT);
        }
        eventBus = EventBus.getDefault();
        listenerConnectivity = new ListenerConnectivity(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.twitter_timeline_listfragment, container, false);
        unbinder = ButterKnife.bind(this, view);
        if (bigScreen) {
            setHasOptionsMenu(true);
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof InterfaceOnTwitterTimelineFragmentInteraction) {
            interfaceOnTwitterTimelineFragmentInteraction = (InterfaceOnTwitterTimelineFragmentInteraction) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement InterfaceOnTwitterTimelineFragmentInteraction");
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (bigScreen) {
            inflater.inflate(R.menu.twitter_timeline_menu, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.fullscreen:
                interfaceOnTwitterTimelineFragmentInteraction.switchScreenToSingle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setRetainInstance(true);

        if (!listenerConnectivity.isNetworkAvailable()) {
            textViewNoConnection.setVisibility(View.VISIBLE);
            eventBus.register(this);
            listenerConnectivity.registerListening();
        } else {
            if (fragmentNewlyCreated) {
                startNewAsyncTask();
            }
        }
        fragmentNewlyCreated = false;
    }

    public void setAdapterListener() {
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (eventBus.isRegistered(this)) {
                    eventBus.unregister(this);
                }
                listenerConnectivity.unregisterListening();
                if (textViewNoConnection != null) {
                    textViewNoConnection.setVisibility(View.GONE);
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeviceConnectedEvent(EventDeviceConnected eventDeviceConnected) {
        if (adapter == null) {
            startNewAsyncTask();
        } else {
            if (adapter.isEmpty()) {
                startNewAsyncTask();
            }
        }

        if (eventBus.isRegistered(this)) {
            eventBus.unregister(this);
        }
        listenerConnectivity.unregisterListening();
        textViewNoConnection.setVisibility(View.GONE);
    }

    private void startNewAsyncTask() {
        asyncTask = new MyAsyncTask(this);
        this.asyncTaskWeakRef = new WeakReference<MyAsyncTask>(asyncTask);
        asyncTask.execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (eventBus.isRegistered(this)) {
            eventBus.unregister(this);
        }
        listenerConnectivity.unregisterListening();
        textViewNoConnection.setVisibility(View.GONE);
        if (asyncTask != null && asyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            asyncTask.cancel(true);
    }

    private class MyAsyncTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<FragmentListTwitterSingleUser> fragmentWeakRef;

        private MyAsyncTask(FragmentListTwitterSingleUser fragment) {
            this.fragmentWeakRef = new WeakReference<FragmentListTwitterSingleUser>(fragment);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... params) {
            if (twitterAccount != null) {
                buildUserTimelineAndSetAdapter(twitterAccount.getScreenName());
            }
            return null;
        }

        public void buildUserTimelineAndSetAdapter(String twitterUserName) {
            final UserTimeline userTimeline = new UserTimeline.Builder()
                    .screenName(twitterUserName)
                    .build();
            adapter = new TweetTimelineListAdapter.Builder(getActivity())
                    .setTimeline(userTimeline)
                    .build();
            setAdapterListener();
        }

        @Override
        protected void onPostExecute(Void response) {
            super.onPostExecute(response);
            if (this.fragmentWeakRef.get() != null) {
                setListAdapter(adapter);
            }
        }
    }
}