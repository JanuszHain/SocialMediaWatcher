package pl.janusz.hain.socialmediawatcher;

/**
 * <br>
 * Event for {@link org.greenrobot.eventbus.EventBus}. <br>
 * <br>
 * Event is sent when Twitter authorization is successful.
 */
public class EventLogin {
    private boolean loginSuccessful = false;

    public EventLogin(boolean loginSuccessful) {
        this.loginSuccessful = loginSuccessful;
    }

    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }
}
