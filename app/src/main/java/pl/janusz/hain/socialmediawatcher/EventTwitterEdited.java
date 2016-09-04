package pl.janusz.hain.socialmediawatcher;

/**
 * <br>
 * Event for {@link org.greenrobot.eventbus.EventBus}. <br>
 * <br>
 * Event is sent when {@link TwitterAccount} is edited.<br>
 * It is needed for updating list of TwitterAccounts in tablet (dual fragment) mode.
 */
public class EventTwitterEdited {
    TwitterAccount twitterAccount;

    public EventTwitterEdited(TwitterAccount twitterAccount) {
        this.twitterAccount = twitterAccount;
    }

    public TwitterAccount getTwitterAccount() {
        return twitterAccount;
    }
}
