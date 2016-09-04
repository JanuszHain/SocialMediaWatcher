package pl.janusz.hain.socialmediawatcher;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.CompactTweetView;

import java.util.ArrayList;

/**
 * <br>
 * Creates adapter for timeline of tweets needed by {@link com.twitter.sdk.android.tweetui.FixedTweetTimeline}.
 */

public class AdapterTweet extends BaseAdapter implements ListAdapter {

    private Context context;
    private ArrayList<Tweet> tweets;

    public AdapterTweet(Context context) {
        this.context = context;
        tweets = new ArrayList<>();
    }

    public void addTweets(ArrayList<Tweet> tweets) {
        this.tweets.addAll(tweets);
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return tweets.size();
    }

    @Override
    public Object getItem(int position) {
        return tweets.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        CompactTweetView compactTweetView = new CompactTweetView(context, tweets.get(position));

        return compactTweetView;
    }
}