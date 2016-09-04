package pl.janusz.hain.socialmediawatcher;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * <br>
 * Listener for getting call when {@link ListView} is scrolled to bottom.<br>
 * Also there is method to check if all views are visible, so ListView can load more items if needed with larger screens.
 */
public class ListenerListViewScrolledToBottom implements AbsListView.OnScrollListener {

    private ListViewScrolledToBottomCallback scrolledToBottomCallback;
    private int currentFirstVisibleItem;
    private int currentVisibleItemCount;
    private int totalItemCount;
    private int currentScrollState;
    private int oldCount = 0;
    private ListView listView;

    public interface ListViewScrolledToBottomCallback {
        void onScrolledToBottom();
    }

    public ListenerListViewScrolledToBottom(Fragment fragment, ListView listView) {
        try {
            scrolledToBottomCallback = (ListViewScrolledToBottomCallback) fragment;
            this.listView = listView;
            this.listView.setOnScrollListener(this);
        } catch (ClassCastException e) {
            throw new ClassCastException(fragment.toString()
                    + " must implement ListViewScrolledToBottomCallback");
        }
    }

    public ListenerListViewScrolledToBottom(Activity activity, ListView listView) {
        try {
            scrolledToBottomCallback = (ListViewScrolledToBottomCallback) activity;
            this.listView = listView;
            this.listView.setOnScrollListener(this);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ListViewScrolledToBottomCallback");
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        this.currentFirstVisibleItem = firstVisibleItem;
        this.currentVisibleItemCount = visibleItemCount;
        this.totalItemCount = totalItemCount;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        this.currentScrollState = scrollState;
        if (isScrollCompleted()) {
            if (isScrolledToBottom()) {
                scrolledToBottomCallback.onScrolledToBottom();
            }
        }
    }

    private boolean isScrollCompleted() {
        if (this.currentVisibleItemCount > 0 && this.currentScrollState == SCROLL_STATE_IDLE) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isScrolledToBottom() {
        int lastItem = currentFirstVisibleItem + currentVisibleItemCount;
        if (lastItem == totalItemCount) {
            return true;
        } else {
            return false;
        }
    }

    public boolean allViewsVisibleForInitialLoading() {
        int count = listView.getCount();
        if (count != oldCount) {
            oldCount = count;
            int visibleChildCount = (listView.getLastVisiblePosition() - listView.getFirstVisiblePosition()) + 1;

            if (visibleChildCount == count) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

}
