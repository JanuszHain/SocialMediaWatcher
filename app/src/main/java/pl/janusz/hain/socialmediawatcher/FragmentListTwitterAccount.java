package pl.janusz.hain.socialmediawatcher;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * A fragment representing a list of {@link TwitterAccount}s.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnTwitterAccountListFragmentInteractionListener}
 * interface.
 */
public class FragmentListTwitterAccount extends ListFragment implements ListenerListViewScrolledToBottom.ListViewScrolledToBottomCallback {

    private OnTwitterAccountListFragmentInteractionListener listListener;
    private ListenerListViewScrolledToBottom listenerListViewScrolledToBottom;
    private String sorting;
    private String fragmentTag;
    private static final String ARG_FRAGMENT_TAG = "fragmentTag";
    private final int limitOfResults = 5;
    private int startDatabasePosition = 0;
    private boolean isCABDestroyed = true;
    private boolean twittersBeingLoaded;
    private ArrayList<TwitterAccount> twitterAccounts;
    private ArrayList<TwitterAccount> multiChoiceTwitterAccounts;
    private ArrayAdapter adapter;
    private EventBus eventBus;
    private DatabaseTwitterManager databaseTwitterManager;
    private WeakReference<MyAsyncTask> asyncTaskWeakRef;
    private ListView listView;

    private Subscription subscriptionGetAllTwitterAccounts;
    private boolean loadingAllTwitterAccounts = false;

    public FragmentListTwitterAccount() {
    }

    public static FragmentListTwitterAccount newInstance(String fragmentTag) {
        FragmentListTwitterAccount fragment = new FragmentListTwitterAccount();
        Bundle args = new Bundle();
        args.putString(ARG_FRAGMENT_TAG, fragmentTag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.twitterlist_menu, menu);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
        if (fragmentTag.equals(FragmentTags.MANAGE_TWITTERS_FRAGMENT_LIST_TAG)) {
            restartListOfTwitters();
        } else {
            loadMoreAccountsIfAllViewsAreVisible();
        }
    }

    @Subscribe
    public void getEventTwitterEdited(EventTwitterEdited eventTwitterEdited) {
        findAndChangeTwitter(eventTwitterEdited.getTwitterAccount());
        adapter.notifyDataSetChanged();
    }

    public void findAndChangeTwitter(TwitterAccount twitterAccount) {
        int positionOfTwitter = twitterAccounts.indexOf(twitterAccount);
            twitterAccounts.get(positionOfTwitter).setTwitterValues(twitterAccount);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (eventBus.isRegistered(this)) {
            eventBus.unregister(this);
        }
        if (subscriptionGetAllTwitterAccounts != null && !subscriptionGetAllTwitterAccounts.isUnsubscribed()) {
            subscriptionGetAllTwitterAccounts.unsubscribe();
            loadingAllTwitterAccounts = false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menuSortDate:
                sorting = item.getTitle().toString();
                restartListOfTwitters();
                return true;

            case R.id.menuSortValue:
                sorting = item.getTitle().toString();
                restartListOfTwitters();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void restartListOfTwitters() {
        startDatabasePosition = 0;
        initiateArrayList();
        if (getActivity() != null) {
            databaseTwitterManager = new DatabaseTwitterManager(getActivity());
            startNewAsyncTask();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView = getListView();
        listView.getEmptyView().setVisibility(ListView.GONE);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        isCABDestroyed = true;
        setListeners();
    }

    private void setListeners() {
        setOnItemClickListener();
        setOnItemLongClickListener();
        setScrollToBottomListener();
        setMultiChoiceModeListener();
    }

    private void setOnItemClickListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isCABDestroyed) {
                    listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                    if (fragmentTag.equals(FragmentTags.TWITTERS_FRAGMENT_LIST_TAG)) {
                        if (position == 0) {
                            getAllTwitters();
                        } else {
                            listListener.onTwitterAccountChosen(fragmentTag, getTwitterAccount(position));
                        }
                    } else {
                        listListener.onTwitterAccountChosen(fragmentTag, getTwitterAccount(position));
                    }

                }
            }
        });
    }

    private void setOnItemLongClickListener() {
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if (isCABDestroyed) {
                    listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
                    listView.setItemChecked(position, true);
                    isCABDestroyed = false;
                    return true;
                }

                return false;
            }
        });
    }

    private void setScrollToBottomListener() {
        listenerListViewScrolledToBottom = new ListenerListViewScrolledToBottom(this, listView);
    }

    private void setMultiChoiceModeListener() {
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position,
                                                  long id, boolean checked) {
                // Here you can do something when items are selected/de-selected,
                // such as update the title in the CAB
                if (position == 0 && fragmentTag.equals(FragmentTags.TWITTERS_FRAGMENT_LIST_TAG)) {
                    if (checked) {
                        listView.setItemChecked(position, false);
                    }
                } else if (checked) {
                    multiChoiceTwitterAccounts.add(getTwitterAccount(position));
                } else {
                    int positionToBeRemoved = getIndexOfChosenTwitterFromOriginalArrayList(position);
                    if (positionToBeRemoved != -1) {
                        multiChoiceTwitterAccounts.remove(getIndexOfChosenTwitterFromOriginalArrayList(position));
                    }
                }
            }

            private int getIndexOfChosenTwitterFromOriginalArrayList(int position) {
                return multiChoiceTwitterAccounts.indexOf(twitterAccounts.get(position));
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // Respond to clicks on the actions in the CAB
                switch (item.getItemId()) {
                    case R.id.choseTwitters:
                        listListener.onTwitterAccountsChosen(fragmentTag, multiChoiceTwitterAccounts);

                        mode.finish(); // Action picked, so close the CAB
                        return true;

                    case R.id.deleteTwitters:
                        alertDialogIfDeleteTwitter();
                        mode.finish();
                        return true;

                    default:
                        return false;
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate the menu for the CAB
                MenuInflater inflater = mode.getMenuInflater();
                if (fragmentTag.equals(FragmentTags.TWITTERS_FRAGMENT_LIST_TAG)) {
                    inflater.inflate(R.menu.twitterlist_menu_cab, menu);
                } else {
                    inflater.inflate(R.menu.twitterlist_menu_cab_manage_tweets, menu);
                }
                multiChoiceTwitterAccounts = new ArrayList<>();
                isCABDestroyed = false;
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Here you can make any necessary updates to the activity when
                // the CAB is removed. By default, selected items are deselected/unchecked.
                isCABDestroyed = true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // Here you can perform updates to the CAB due to
                // an invalidate() request
                return false;
            }
        });
    }

    private void alertDialogIfDeleteTwitter() {
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.deleteTwitter))
                .setMessage(getString(R.string.doYouWantDeleteTwitters))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteChosenTwitters();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteChosenTwitters() {
        for (TwitterAccount twitterAccount : multiChoiceTwitterAccounts) {
            if (deleteTwitterFromDatabase(twitterAccount)) {
                deleteTwitterFromAdapter(twitterAccount);
            } else {
                showToast(getString(R.string.couldntDeleteTwitter) + "" + twitterAccount);
            }
        }
        notifyDataChanged();
        showToast(getString(R.string.twittersDeleted));
    }


    private boolean deleteTwitterFromDatabase(TwitterAccount twitterAccount) {
        DatabaseTwitterManager databaseTwitterManager = new DatabaseTwitterManager(getActivity());
        return databaseTwitterManager.deleteTwitter(twitterAccount);
    }

    private void deleteTwitterFromAdapter(TwitterAccount twitterAccount) {
        twitterAccounts.remove(twitterAccount);
    }

    private void showToast(String text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
    }

    private void notifyDataChanged() {
        adapter.notifyDataSetChanged();
    }

    private void getAllTwitters() {
        DatabaseTwitterManager databaseTwitterManager = new DatabaseTwitterManager(getActivity());

        Observer<ArrayList<TwitterAccount>> myObserver = new Observer<ArrayList<TwitterAccount>>() {
            @Override
            public void onCompleted() {
                loadingAllTwitterAccounts = false;
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(ArrayList<TwitterAccount> twitterAccounts) {
                listListener.onTwitterAccountsChosen(fragmentTag, twitterAccounts);
            }
        };


        if (!loadingAllTwitterAccounts) {
            loadingAllTwitterAccounts = true;
            subscriptionGetAllTwitterAccounts = databaseTwitterManager.getObservableLoadAllTweets()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(myObserver);
        }


    }

    private TwitterAccount getTwitterAccount(int position) {
        return twitterAccounts.get(position);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.twitter_listfragment, container, false);
        twittersBeingLoaded = false;
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listListener = null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnTwitterAccountListFragmentInteractionListener) {
            listListener = (OnTwitterAccountListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fragmentTag = getArguments().getString(ARG_FRAGMENT_TAG);
        }
        eventBus = EventBus.getDefault();
        if (!fragmentTag.equals(FragmentTags.MANAGE_TWITTERS_FRAGMENT_LIST_TAG)) {
            sorting = getString(R.string.menuSortDate);
            startDatabasePosition = 0;
            initiateArrayList();
            if (getActivity() != null) {
                databaseTwitterManager = new DatabaseTwitterManager(getActivity());
                startNewAsyncTask();
            }
        }
        else{
            sorting = getString(R.string.menuSortDate);
        }
    }


    private void initiateArrayList() {
        twitterAccounts = new ArrayList<>();
        if (FragmentTags.TWITTERS_FRAGMENT_LIST_TAG.equals(fragmentTag)) {
            TwitterAccount allTwitterAccountsItem = new TwitterAccount();
            allTwitterAccountsItem.setName(getString(R.string.allTwitters));
            twitterAccounts.add(allTwitterAccountsItem);
        }
    }

    private void startNewAsyncTask() {
        MyAsyncTask asyncTask = new MyAsyncTask(this);
        this.asyncTaskWeakRef = new WeakReference<MyAsyncTask>(asyncTask);
        asyncTask.execute();
    }

    @Override
    public void onScrolledToBottom() {
        if (!twittersBeingLoaded) {
            startNewAsyncTask();
        }
    }

    private class MyAsyncTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<FragmentListTwitterAccount> fragmentWeakRef;

        private MyAsyncTask(FragmentListTwitterAccount fragment) {
            this.fragmentWeakRef = new WeakReference<FragmentListTwitterAccount>(fragment);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            twittersBeingLoaded = true;
        }

        @Override
        protected Void doInBackground(Void... params) {
            getTwitterAccountsAndCreateAdapter();
            return null;
        }

        public void getTwitterAccountsAndCreateAdapter() {
            Context context = getActivity();
            if (context != null) {
                ArrayList<TwitterAccount> twitterAccountsFromDB;
                if (sorting.equals(getString(R.string.menuSortDate))) {
                    twitterAccountsFromDB = databaseTwitterManager.getTwittersOrderedByDataCreated(startDatabasePosition, limitOfResults);

                } else {
                    twitterAccountsFromDB = databaseTwitterManager.getTwittersOrderedByValue(startDatabasePosition, limitOfResults);
                }

                twitterAccounts.addAll(twitterAccountsFromDB);

                if (startDatabasePosition == 0) {
                    if (twitterAccountsFromDB.size() == 0) {
                        twitterAccounts.clear();
                    }
                    adapter = new ArrayAdapter<>(context, R.layout.item_twitteraccount, R.id.twitter_account, twitterAccounts);
                }
            } else {
                twittersBeingLoaded = false;
                this.fragmentWeakRef.clear();
            }
        }

        @Override
        protected void onPostExecute(Void response) {
            super.onPostExecute(response);
            if (this.fragmentWeakRef.get() != null) {
                if (startDatabasePosition == 0) {
                    setListAdapter(adapter);
                    loadMoreAccountsIfAllViewsAreVisible();
                } else {
                    adapter.notifyDataSetChanged();
                }
                startDatabasePosition += limitOfResults;
            }
            twittersBeingLoaded = false;
        }
    }

    private void loadMoreAccountsIfAllViewsAreVisible() {

        listView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (listenerListViewScrolledToBottom.allViewsVisibleForInitialLoading()) {
                    startNewAsyncTask();
                }
                listView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });


    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnTwitterAccountListFragmentInteractionListener {
        void onTwitterAccountsChosen(String fragmentTag, ArrayList<TwitterAccount> twitterAccounts);

        void onTwitterAccountChosen(String fragmentTag, TwitterAccount twitterAccount);
    }
}
