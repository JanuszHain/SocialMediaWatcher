package pl.janusz.hain.socialmediawatcher;

import com.twitter.sdk.android.core.models.Tweet;

import java.util.ArrayList;
import java.util.Collections;

import pl.janusz.hain.socialmediawatcher.util.MyScheduler;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;

/**
 * Represents TwitterTimeline for single Twitter user. Provides methods for loading and sorting timelines for single {@link TwitterAccount}.
 */

public class TwitterTimeline {

    private ArrayList<Tweet> tweetsArrayList;
    private TwitterAccount twitterAccount;
    private TwitterTimelineGetter twitterTimelineGetter;
    private long lastId = -1;

    private TwitterTimeline() {
    }

    public TwitterTimeline(TwitterAccount twitterAccount) {
        this.twitterAccount = twitterAccount;
        tweetsArrayList = new ArrayList<>();
        twitterTimelineGetter = new TwitterTimelineGetter(twitterAccount.getScreenName());
    }

    public void setCountOfTweetsPerRequest(int countOfTweetsPerRequest) {
        twitterTimelineGetter.setCountOfTweetsPerRequest(countOfTweetsPerRequest);
    }

    private void addNewTweets(ArrayList<Tweet> tweetsArrayList) {
        this.tweetsArrayList.addAll(tweetsArrayList);
        sortTweets();
    }

    private void sortTweets() {
        Collections.sort(tweetsArrayList, Collections.reverseOrder(new ComparatorTweet()));
    }

    public Tweet removeFirst() {
        Tweet tweet = null;
        if (tweetsArrayList.size() > 0) {
            tweet = tweetsArrayList.remove(0);
        }
        return tweet;
    }

    public Tweet getFirst() {
        Tweet tweet = tweetsArrayList.get(0);
        return tweet;
    }

    public int getLenght() {
        return tweetsArrayList.size();
    }

    public Observable<String> observableGetNewTweets() {

        Observable<String> observableGetTweets = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                Observable<ArrayList<Tweet>> observableGetTweets = twitterTimelineGetter.getObservableReadTweets(lastId);
                Observer<ArrayList<Tweet>> myObserver = new Observer<ArrayList<Tweet>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        subscriber.onError(e);
                    }

                    @Override
                    public void onNext(ArrayList<Tweet> tweets) {

                        if (tweets.size() > 0) {
                            addNewTweets(tweets);
                            subscriber.onNext(tweets.get(0).user.name);
                            setLastTweetLoadedId(tweets);
                        }
                        subscriber.onCompleted();
                    }
                };

                observableGetTweets
                        .subscribe(myObserver);
            }
        });

        return observableGetTweets;
    }

    private void setLastTweetLoadedId(ArrayList<Tweet> tweets) {
        if (tweets.size() > 0) {
            Tweet lastTweet = tweets.get(tweets.size() - 1);
            lastId = lastTweet.getId();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TwitterTimeline that = (TwitterTimeline) o;

        return twitterAccount.equals(that.twitterAccount);
    }

    @Override
    public int hashCode() {
        return twitterAccount.hashCode();
    }
}
