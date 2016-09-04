package pl.janusz.hain.socialmediawatcher;

import java.io.Serializable;

public class TwitterAccount implements Serializable {
    private long id;
    private String screenName;
    private String name;
    private int value = 0;

    public TwitterAccount() {
    }

    public TwitterAccount(long id, String screenName, String name, int value) {
        this.id = id;
        this.screenName = screenName;
        this.name = name;
        this.value = value;
    }

    public void setTwitterValues(TwitterAccount twitterAccount) {
        if (twitterAccount != null) {
            id = twitterAccount.getId();
            screenName = twitterAccount.getScreenName();
            name = twitterAccount.getName();
            value = twitterAccount.getValue();
        }
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {

        if (screenName != null) {
            return name + "\n(" + screenName + ")";
        } else {
            return name;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TwitterAccount that = (TwitterAccount) o;

        return getScreenName().equals(that.getScreenName());
    }

    @Override
    public int hashCode() {
        return getScreenName().hashCode();
    }
}
