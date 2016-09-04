package pl.janusz.hain.socialmediawatcher;

import com.twitter.sdk.android.core.AppSession;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;

import org.greenrobot.eventbus.EventBus;

/**
 * Provides signing in guest to Twitter API.
 */

public final class TwitterGuestSession {

    private static AppSession session;
    private static EventBus eventBus = EventBus.getDefault();
    private static boolean loggingInProgress = false;

    private TwitterGuestSession() {
    }

    public static boolean isLoggingInProgress() {
        return loggingInProgress;
    }

    public static boolean isSessionExpiredOrNull() {
        return (session == null || session.getAuthToken().isExpired());
    }

    public static void GuestSessionLogIn() {
        if (!isLoggingInProgress()) {
            loggingInProgress = true;

            TwitterCore.getInstance().logInGuest(new Callback<AppSession>() {

                @Override
                public void success(Result<AppSession> result) {
                    registerEventInEventBus(true);
                    loggingInProgress = false;
                    session = TwitterCore.getInstance().getAppSessionManager().getActiveSession();
                }

                @Override
                public void failure(TwitterException exception) {
                    registerEventInEventBus(false);
                    loggingInProgress = false;
                }
            });
        }
    }

    private static void registerEventInEventBus(boolean logged) {
        EventLogin eventLogin = new EventLogin(logged);
        eventBus.post(eventLogin);
    }

}
