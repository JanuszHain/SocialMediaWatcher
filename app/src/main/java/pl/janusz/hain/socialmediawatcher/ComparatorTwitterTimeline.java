package pl.janusz.hain.socialmediawatcher;

import com.twitter.sdk.android.core.models.Tweet;

import java.util.Comparator;

public class ComparatorTwitterTimeline implements Comparator<TwitterTimeline> {
    @Override
    public int compare(TwitterTimeline lhs, TwitterTimeline rhs) {
        Tweet lhsTweet = lhs.getFirst();
        Tweet rhsTweet = rhs.getFirst();
        return new ComparatorTweet().compare(lhsTweet, rhsTweet);
    }
}
