package pl.janusz.hain.socialmediawatcher;

import com.twitter.sdk.android.core.AppSession;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import pl.janusz.hain.socialmediawatcher.util.MyScheduler;
import rx.Observable;
import rx.Subscriber;

/**
 * Provides methods for loading timelines for single {@link TwitterAccount}.<br>
 * Note that Callback from Twitter API occurs on UI Thread.
 */

public class TwitterTimelineGetter {
    private AppSession session;
    private EventBus eventBus;

    private CountDownLatch loginWaiter;
    private String screen_name;
    private int count = 2;

    public TwitterTimelineGetter(String screen_name) {
        eventBus = EventBus.getDefault();
        this.screen_name = screen_name;
        loginWaiter = new CountDownLatch(1);
    }

    public void setCountOfTweetsPerRequest(int count) {
        this.count = count;
    }

    public Observable<ArrayList<Tweet>> getObservableReadTweets(long lastId) {
        Observable<ArrayList<Tweet>> observableGetTweets;
        if (lastId < 0) {
            observableGetTweets = createObservableReadTweets(screen_name, count);
        } else {
            observableGetTweets = createObservableReadTweets(screen_name, count, lastId);
        }
        return observableGetTweets;
    }


    private synchronized void logIn() throws InterruptedException {


        if (TwitterGuestSession.isSessionExpiredOrNull()) {

            TwitterGuestSession.GuestSessionLogIn();

            eventBus.register(this);

            loginWaiter.await(60, TimeUnit.SECONDS);
        }

        session = TwitterCore.getInstance().getAppSessionManager().getActiveSession();
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void getLoginEvent(EventLogin eventLogin) {
        loginWaiter.countDown();
        eventBus.unregister(this);
    }


    private Observable<ArrayList<Tweet>> createObservableReadTweets(final String screen_name, final int count) {

        Observable<ArrayList<Tweet>> observableGetTweets = Observable.create(new Observable.OnSubscribe<ArrayList<Tweet>>() {
            @Override
            public void call(final Subscriber<? super ArrayList<Tweet>> subscriber) {
                try {
                    logIn();
                    TwitterApiCustomClient apiclients = new TwitterApiCustomClient(session);
                    final ArrayList<Tweet> tweetArrayList = new ArrayList<Tweet>();
                    apiclients.getCustomService().getTweetsFromUserTimeline(screen_name, count, new Callback<List<Tweet>>() {

                        @Override
                        public void success(Result<List<Tweet>> result) {

                            for (Tweet tweet : result.data) {
                                tweetArrayList.add(tweet);
                            }
                            subscriber.onNext(tweetArrayList);
                            subscriber.onCompleted();
                        }

                        @Override
                        public void failure(TwitterException exception) {
                            subscriber.onError(exception);
                        }
                    });
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });

        return observableGetTweets.subscribeOn(MyScheduler.getScheduler());
    }

    private Observable<ArrayList<Tweet>> createObservableReadTweets(final String screen_name, final int count, final long lastId) {
        Observable<ArrayList<Tweet>> observableGetTweets = Observable.create(new Observable.OnSubscribe<ArrayList<Tweet>>() {
            @Override
            public void call(final Subscriber<? super ArrayList<Tweet>> subscriber) {
                try {
                    logIn();
                    TwitterApiCustomClient apiclients = new TwitterApiCustomClient(session);
                    final ArrayList<Tweet> tweetArrayList = new ArrayList<Tweet>();

                    apiclients.getCustomService().getTweetsFromUserTimeline(screen_name, count, lastId, new Callback<List<Tweet>>() {

                        @Override
                        public void success(Result<List<Tweet>> result) {
                            for (Tweet tweet : result.data) {
                                tweetArrayList.add(tweet);
                            }
                            subscriber.onNext(tweetArrayList);
                            subscriber.onCompleted();
                        }

                        @Override
                        public void failure(TwitterException exception) {
                            subscriber.onError(exception);
                        }
                    });
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });

        return observableGetTweets.subscribeOn(MyScheduler.getScheduler());
    }
}
