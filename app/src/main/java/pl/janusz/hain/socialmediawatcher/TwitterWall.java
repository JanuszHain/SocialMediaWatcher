package pl.janusz.hain.socialmediawatcher;

import android.util.Log;

import com.twitter.sdk.android.core.models.Tweet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Represetnts TwitterWall which shows content of several Twitter users. Loads data for each {@link TwitterAccount} and sorts it.<br>
 * Note that it uses parallel loading with merged observable.
 */

public class TwitterWall {
    private ArrayList<TwitterTimeline> twitterTimelines;
    private ArrayList<Observable<String>> observables;
    private ArrayList<TwitterTimeline> twitterTimelinesInObservables;
    private int countOfTweetsPerNetworkRequest = 5;
    private final int numberOfTweetsToBeLoaded;
    private boolean classNewlyCreated = true;


    public TwitterWall(ArrayList<TwitterAccount> twitterAccounts, int numberOfTweetsToBeLoaded) {
        twitterTimelines = new ArrayList<>(twitterAccounts.size());
        observables = new ArrayList<>(twitterAccounts.size());
        twitterTimelinesInObservables = new ArrayList<>(twitterAccounts.size());
        fromTwitterAccountsToTwitterTimelines(twitterAccounts);
        this.numberOfTweetsToBeLoaded = numberOfTweetsToBeLoaded;
    }

    private void fromTwitterAccountsToTwitterTimelines(final ArrayList<TwitterAccount> twitterAccounts) {

        for (TwitterAccount twitterAccount : twitterAccounts) {
            TwitterTimeline twitterTimeline = new TwitterTimeline(twitterAccount);
            twitterTimeline.setCountOfTweetsPerRequest(3); //lowering amount of tweets to be loaded to lower the initial size of loaded data
            twitterTimelines.add(
                    twitterTimeline
            );
            addObservableToArrayList(twitterTimeline);
        }
    }

    public void setCountOfTweetsPerNetworkRequest(int countOfTweetsPerNetworkRequest) {
        if (countOfTweetsPerNetworkRequest > 1) {
            for (TwitterTimeline twitterTimeline : twitterTimelines) {
                twitterTimeline.setCountOfTweetsPerRequest(countOfTweetsPerNetworkRequest);
            }
            this.countOfTweetsPerNetworkRequest = countOfTweetsPerNetworkRequest;
        }
    }

    public Observable<ArrayList<Tweet>> getObservableGetLastestTweets() {
        Observable<ArrayList<Tweet>> observableGetTweets = Observable.create(new Observable.OnSubscribe<ArrayList<Tweet>>() {
            @Override
            public void call(final Subscriber<? super ArrayList<Tweet>> subscriber) {
                if (classNewlyCreated) {
                    initialLoadOfTweets();
                    classNewlyCreated = false;
                }

                ArrayList<Tweet> tweets = new ArrayList<Tweet>();
                try {
                    tweets = getLastestTweets();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
                subscriber.onNext(tweets);
                subscriber.onCompleted();
            }
        });

        return observableGetTweets;
    }

    private void initialLoadOfTweets() {
        try {
            loadTweetsWithObservables();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Tweet> getLastestTweets() throws Exception {
        ArrayList<Tweet> tweetArrayList = new ArrayList<Tweet>();

        for (int i = 0; i < numberOfTweetsToBeLoaded; i++) {
            Tweet tweet = getTheNewestTweet();
            if (tweet != null) {
                tweetArrayList.add(tweet);
            }
        }

        return tweetArrayList;
    }

    private Tweet getTheNewestTweet() throws Exception {
        if (!isTwitterTimelinesArrayEmpty()) {

            sortTwitterTimelines();

            try {

                TwitterTimeline twitterTimeline = twitterTimelines.get(0);

                Tweet tweet = null;

                if (!isNeededToLoadMore(twitterTimeline)) {
                    tweet = twitterTimeline.removeFirst();
                }

                if (isTwitterTimelineSizeLowEnoughtToLoadMore(twitterTimeline)) {
                    Log.d(getClass().getName(), "ENough to load more: " + twitterTimeline.getLenght());
                    addObservableToArrayList(twitterTimeline);


                    if (isNeededToLoadMore(twitterTimeline)) {
                        Log.d(getClass().getName(), "isNeededToLoadMore " + twitterTimeline.getLenght());
                        loadTweetsWithObservables();
                    }
                }

                if (tweet == null) {
                    tweet = twitterTimeline.removeFirst();
                }

                if (isTwitterTimelineEmpty(twitterTimeline)) {
                    twitterTimelines.remove(twitterTimeline);
                }
                return tweet;
            } catch (Exception e) {
                throw e;
            }


        } else {
            throw new Exception("Timeline empty");
        }
    }

    private void addObservableToArrayList(TwitterTimeline twitterTimeline) {
        if (!isTwitterTimelineInObservables(twitterTimeline)) {
            observables.add(twitterTimeline.observableGetNewTweets());
            twitterTimelinesInObservables.add(twitterTimeline);
        }
    }

    private boolean isTwitterTimelineInObservables(TwitterTimeline twitterTimeline) {
        return twitterTimelinesInObservables.contains(twitterTimeline);
    }

    private boolean isTwitterTimelinesArrayEmpty() {
        return twitterTimelines.size() < 1;
    }

    private void sortTwitterTimelines() {
        Collections.sort(twitterTimelines, Collections.reverseOrder(new ComparatorTwitterTimeline()));
    }

    private boolean isTwitterTimelineSizeLowEnoughtToLoadMore(TwitterTimeline twitterTimeline) {
        return twitterTimeline.getLenght() < countOfTweetsPerNetworkRequest;
    }

    private boolean isNeededToLoadMore(TwitterTimeline twitterTimeline) {
        return twitterTimeline.getLenght() < 2;
    }

    private boolean isTwitterTimelineEmpty(TwitterTimeline twitterTimeline) {
        return twitterTimeline.getLenght() == 0;
    }

    private void loadTweetsWithObservables() throws Exception {
        if (observables.size() > 0) {
            Action1<String> actionOnNext = new Action1<String>() {
                @Override
                public void call(String s) {
                }
            };

            Observable<String> mergedObservable = Observable.merge(observables);
            mergedObservable
                    .subscribeOn(Schedulers.from(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())))
                    .toBlocking()
                    .forEach(actionOnNext);

            clearObservables();
        }
    }

    private void clearObservables() {
        observables.clear();
        twitterTimelinesInObservables.clear();
    }
}
