package pl.janusz.hain.socialmediawatcher;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import java.util.ArrayList;

import butterknife.BindBool;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity implements
        FragmentListTwitterAccount.OnTwitterAccountListFragmentInteractionListener,
        InterfaceOnTwitterTimelineFragmentInteraction {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "";
    private static final String TWITTER_SECRET = "";
    @BindBool(R.bool.big_screen)
    protected boolean bigScreen;
    private DatabaseConnector databaseConnector;

    private Unbinder unbinder;

    //here I don't use ButterKnife, because I may have different layouts for different screen sizes
    private FrameLayout mainFrameLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        unbinder = ButterKnife.bind(this);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            twitterAutoConfig();
            addStartFragment();
        }
        if (bigScreen) {
            mainFrameLayout = (FrameLayout) findViewById(R.id.mainFrameLayout);
        }
    }

    private void twitterAutoConfig() {
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        twitterGuestAuth();
    }

    private void twitterGuestAuth() {
        Thread thread = new Thread() {
            public void run() {
                try {
                    TwitterGuestSession.GuestSessionLogIn();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    private void addStartFragment() {
        Fragment mainMenuFragment = FragmentTwitterMainMenu.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainFrameLayout, mainMenuFragment, "main_menu")
                .commit();
    }

    public void clickGoToFragmentAddTwitter(View v) {
        Fragment addTwitterAccountFragment = FragmentTwitterAddAccount.newInstance(null);
        if (!bigScreen) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mainFrameLayout, addTwitterAccountFragment, "add_twitter")
                    .addToBackStack("main")
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.secondFrameLayout, addTwitterAccountFragment, "add_twitter")
                    .addToBackStack("main")
                    .commit();
        }
    }

    public void clickGoToFragmentTweets(View v) {
        removeFragmentFromSecondFrameLayout();
        Fragment tweetsListFragment = FragmentListTwitterAccount.newInstance(FragmentTags.TWITTERS_FRAGMENT_LIST_TAG);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainFrameLayout, tweetsListFragment, "tweets")
                .addToBackStack("main")
                .commit();
    }

    public void clickGoToFragmentManageTwitters(View v) {
        removeFragmentFromSecondFrameLayout();
        Fragment tweetsListFragment = FragmentListTwitterAccount.newInstance(FragmentTags.MANAGE_TWITTERS_FRAGMENT_LIST_TAG);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainFrameLayout, tweetsListFragment, "manage_twitters")
                .addToBackStack("main")
                .commit();
    }

    private void removeFragmentFromSecondFrameLayout() {
        Fragment fragment = (getSupportFragmentManager().findFragmentById(R.id.secondFrameLayout));
        if (fragment != null) {
            getSupportFragmentManager().popBackStack();
            getSupportFragmentManager().beginTransaction()
                    .remove(fragment)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (bigScreen) {
            removeFragmentFromSecondFrameLayout();
            mainFrameLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        openDatabase();
    }

    private void openDatabase() {
        databaseConnector = new DatabaseConnector(this);
    }

    @Override
    public void onTwitterAccountsChosen(String fragmentTag, ArrayList<TwitterAccount> twitterAccounts) {
        addFixedTwitterTimelineFragment(twitterAccounts);

    }

    private void addFixedTwitterTimelineFragment(ArrayList<TwitterAccount> twitterAccounts) {
        Fragment twitterFixedTweetTimelineFragmentList = FragmentListTwitterFixedTweetTimeline.newInstance(twitterAccounts);
        if (!bigScreen) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mainFrameLayout, twitterFixedTweetTimelineFragmentList, "fixed_twitter_timeline")
                    .addToBackStack(null)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.secondFrameLayout, twitterFixedTweetTimelineFragmentList, "fixed_twitter_timeline")
                    .commit();
        }
    }

    @Override
    public void onTwitterAccountChosen(String fragmentTag, TwitterAccount twitterAccount) {
        switch (fragmentTag) {
            case FragmentTags.TWITTERS_FRAGMENT_LIST_TAG:
                addTwitterFragment(twitterAccount);
                break;
            case FragmentTags.MANAGE_TWITTERS_FRAGMENT_LIST_TAG:
                editTwitterFragment(twitterAccount);
                break;
        }
    }

    private void addTwitterFragment(TwitterAccount twitterAccount) {
        Fragment twitterSingleUserFragmentList = FragmentListTwitterSingleUser.newInstance(twitterAccount);
        if (!bigScreen) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mainFrameLayout, twitterSingleUserFragmentList, "add_twitter")
                    .addToBackStack("twitterList")
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.secondFrameLayout, twitterSingleUserFragmentList, "add_twitter")
                    .commit();
        }
    }

    private void editTwitterFragment(TwitterAccount twitterAccount) {
        Fragment twitterEditFragment = FragmentTwitterAddAccount.newInstance(twitterAccount);
        if (!bigScreen) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mainFrameLayout, twitterEditFragment, "edit_twitter")
                    .addToBackStack("twitterList")
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.secondFrameLayout, twitterEditFragment, "edit_twitter")
                    .commit();
        }
    }

    @Override
    public void switchScreenToSingle() {
        if (mainFrameLayout.getVisibility() == View.VISIBLE) {
            mainFrameLayout.setVisibility(View.GONE);
        } else {
            mainFrameLayout.setVisibility(View.VISIBLE);
        }
    }
}