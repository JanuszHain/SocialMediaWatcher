package pl.janusz.hain.socialmediawatcher;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.twitter.sdk.android.core.models.Tweet;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.BindBool;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * A fragment for showing Twitter timelines with multiple {@link TwitterAccount}s.
 */

public class FragmentListTwitterFixedTweetTimeline extends ListFragment implements ListenerListViewScrolledToBottom.ListViewScrolledToBottomCallback {

    private static String ARG_TWITTER_ACCOUNTS = "TWITTER_ACCOUNTS";
    private ArrayList<TwitterAccount> twitterAccounts;
    private boolean loadingTweets = false;

    private EventBus eventBus;
    private ListenerConnectivity listenerConnectivity;
    private ListenerListViewScrolledToBottom listenerListViewScrolledToBottom;
    private AdapterTweet adapterTweet;
    private TwitterWall twitterWall;
    private Subscription subscription;
    private ListView listView;
    private boolean newFragment = true;
    private InterfaceOnTwitterTimelineFragmentInteraction interfaceOnTwitterTimelineFragmentInteraction;

    @BindView(R.id.noConnection)
    protected TextView textViewNoConnection;

    @BindBool(R.bool.big_screen)
    protected boolean bigScreen;

    private Unbinder unbinder;


    public static FragmentListTwitterFixedTweetTimeline newInstance(ArrayList<TwitterAccount> twitterAccounts) {
        FragmentListTwitterFixedTweetTimeline fragment = new FragmentListTwitterFixedTweetTimeline();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TWITTER_ACCOUNTS, twitterAccounts);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            twitterAccounts = (ArrayList<TwitterAccount>) getArguments().getSerializable(ARG_TWITTER_ACCOUNTS);
        }
        eventBus = EventBus.getDefault();
        listenerConnectivity = new ListenerConnectivity(getContext());
        adapterTweet = new AdapterTweet(getContext());

        twitterWall = new TwitterWall(twitterAccounts, 10);
        twitterWall.setCountOfTweetsPerNetworkRequest(5);
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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setScrollToBottomListener();
    }

    private void setScrollToBottomListener() {
        listenerListViewScrolledToBottom = new ListenerListViewScrolledToBottom(this, getListView());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
        listView = getListView();
        listView.setAdapter(adapterTweet);

        if (!listenerConnectivity.isNetworkAvailable()) {
            textViewNoConnection.setVisibility(View.VISIBLE);
            eventBus.register(this);
            listenerConnectivity.registerListening();
        } else {
            loadTweets();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeviceConnectedEvent(EventDeviceConnected eventDeviceConnected) {
        loadTweets();

        if (eventBus.isRegistered(this)) {
            eventBus.unregister(this);
        }
        listenerConnectivity.unregisterListening();
        textViewNoConnection.setVisibility(View.GONE);
    }

    @Override
    public void onScrolledToBottom() {
        loadTweets();
    }

    private void loadTweets() {

        Observer<ArrayList<Tweet>> myObserver = new Observer<ArrayList<Tweet>>() {
            @Override
            public void onCompleted() {
                loadingTweets = false;
                if (newFragment) {
                    newFragment = false;
                    loadMoreAccountsIfAllViewsAreVisible();
                }
            }

            @Override
            public void onError(Throwable e) {
                loadingTweets = false;
                showToast(getString(R.string.couldntLoadTweets));
            }

            @Override
            public void onNext(ArrayList<Tweet> loadedTweets) {
                if (loadedTweets.size() > 0) {
                    adapterTweet.addTweets(loadedTweets);
                }
            }
        };

        if (!loadingTweets) {
            loadingTweets = true;
            subscription = twitterWall.getObservableGetLastestTweets()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(myObserver);
        }
    }

    private void loadMoreAccountsIfAllViewsAreVisible() {
        listView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (listenerListViewScrolledToBottom.allViewsVisibleForInitialLoading()) {
                    loadTweets();
                }
                listView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void showToast(String text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (eventBus.isRegistered(this)) {
            eventBus.unregister(this);
        }
        listenerConnectivity.unregisterListening();
        textViewNoConnection.setVisibility(View.GONE);

        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }


}