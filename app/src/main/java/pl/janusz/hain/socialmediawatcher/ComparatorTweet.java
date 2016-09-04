package pl.janusz.hain.socialmediawatcher;

import android.util.Log;

import com.twitter.sdk.android.core.models.Tweet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class ComparatorTweet implements Comparator<Tweet> {
    @Override
    public int compare(Tweet lhs, Tweet rhs) {
        String lhsDateString = lhs.createdAt;
        String rhsDateString = rhs.createdAt;
        try {
            Date lhsDate = getTwitterDate(lhsDateString);
            Date rhsDate = getTwitterDate(rhsDateString);
            return lhsDate.compareTo(rhsDate);
        } catch (Exception e) {
            Log.wtf("ComparatorTweet", "" + e);
            return 0;
        }
    }


    public Date getTwitterDate(String date) throws ParseException {
        final String TWITTER = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(TWITTER, Locale.ENGLISH);
        sf.setLenient(true);

        return sf.parse(date);
    }
}
