package pl.janusz.hain.socialmediawatcher;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Session;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.models.Tweet;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Query;

/**
 * <br>
 * Provides REST endpoints for getting Tweets from users' timelines.
 */
public class TwitterApiCustomClient extends com.twitter.sdk.android.core.TwitterApiClient {

    public TwitterApiCustomClient(Session session) {
        super(session);
    }

    /**
     * Provide CustomService with defined endpoints
     */
    public CustomService getCustomService() {
        return getService(CustomService.class);
    }

    interface CustomService {

        @GET("/1.1/statuses/user_timeline.json")
        void getTweetsFromUserTimeline(@Query("screen_name") String screen_name,
                                       @Query("count") int count,
                                       @Query("max_id") long max_id,
                                       Callback<List<Tweet>> cb);

        @GET("/1.1/statuses/user_timeline.json")
        void getTweetsFromUserTimeline(@Query("screen_name") String screen_name,
                                       @Query("count") int count,
                                       Callback<List<Tweet>> cb);


    }


}
