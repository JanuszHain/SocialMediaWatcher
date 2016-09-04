package pl.janusz.hain.socialmediawatcher;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * A fragment for adding {@link TwitterAccount}s to database.<br>
 * <br>
 * Use the {@link FragmentTwitterAddAccount#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentTwitterAddAccount extends Fragment implements View.OnClickListener {
    private static final String ARG_PARAM1 = "twitterAccount";
    private TwitterAccount twitterAccount;

    @BindView(R.id.buttonAddTwitter)
    protected Button buttonAddTwitter;
    @BindView(R.id.editTextName)
    protected EditText editTextName;
    @BindView(R.id.editTextNameOfTwitter)
    protected EditText editTextTwitterScreenName;
    @BindView(R.id.editTextValue)
    protected EditText editTextTwitterOrderValue;

    private Unbinder unbinder;

    public FragmentTwitterAddAccount() {
    }

    public static FragmentTwitterAddAccount newInstance(TwitterAccount twitterAccount) {
        FragmentTwitterAddAccount fragment = new FragmentTwitterAddAccount();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, twitterAccount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            twitterAccount = (TwitterAccount) getArguments().getSerializable(ARG_PARAM1);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_twitter_account, container, false);
        unbinder = ButterKnife.bind(this, view);
        buttonAddTwitter.setOnClickListener(this);
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        putDataIntoEditTexts();
    }

    private void putDataIntoEditTexts() {
        if (twitterAccount != null) {
            editTextTwitterOrderValue.setText(String.valueOf(twitterAccount.getValue()));
            editTextTwitterScreenName.setText(twitterAccount.getScreenName());
            editTextName.setText(twitterAccount.getName());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonAddTwitter:
                if (areUserParamsCorrect()) {
                    twitterAccount = newTwitterAccount();
                    if (!twitterExists()) {
                        addTwitterToDatabase();
                        clearDataAfterChange();
                    } else {
                        alertDialogIfUpdateTwitter();
                    }
                }
                break;
        }
    }

    public boolean areUserParamsCorrect() {
        if (isNameNullOrEmpty()) {
            showToast(getString(R.string.nameMustBeGiven));
            return false;
        }
        if (isTwitterNameNullOrEmpty()) {
            showToast(getString(R.string.screenNameMustBeGiven));
            return false;
        }
        if (isOrderValueNullOrEmpty()) {
            showToast(getString(R.string.valueMustBeGiven));
            return false;
        }

        return true;
    }

    private boolean isNameNullOrEmpty() {
        if (editTextName.getText().toString() == null || editTextName.getText().toString().isEmpty()) {
            return true;
        }
        return false;
    }

    private boolean isTwitterNameNullOrEmpty() {
        if (editTextTwitterScreenName.getText().toString() == null || editTextTwitterScreenName.getText().toString().isEmpty()) {
            return true;
        }
        return false;
    }

    private boolean isOrderValueNullOrEmpty() {
        if (editTextTwitterOrderValue.getText().toString() == null || editTextTwitterOrderValue.getText().toString().isEmpty()) {
            return true;
        }
        return false;
    }

    private boolean twitterExists() {
        DatabaseTwitterManager databaseTwitterManager = new DatabaseTwitterManager(getActivity());
        return databaseTwitterManager.twitterAccountExists(twitterAccount);
    }

    private void addTwitterToDatabase() {
        DatabaseTwitterManager databaseTwitterManager = new DatabaseTwitterManager(getActivity());
        databaseTwitterManager.addTwitter(twitterAccount);
        showToast(getString(R.string.twitterAdded));
    }

    private void alertDialogIfUpdateTwitter() {
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.updateTwitter))
                .setMessage(getString(R.string.doYouWantToOverrideTwitter))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        updateTwitterInDatabse();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void updateTwitterInDatabse() {
        DatabaseTwitterManager databaseTwitterManager = new DatabaseTwitterManager(getActivity());
        databaseTwitterManager.updateTwitter(twitterAccount);
        showToast(getString(R.string.twitterUpdated));
        notifyTwitterUpdated();
    }

    private void notifyTwitterUpdated() {
        EventBus eventbus = EventBus.getDefault();
        eventbus.post(new EventTwitterEdited(twitterAccount));
    }

    private TwitterAccount newTwitterAccount() {
        TwitterAccount twitterAccount = new TwitterAccount();
        twitterAccount.setName(editTextName.getText().toString());
        twitterAccount.setScreenName(editTextTwitterScreenName.getText().toString().replaceAll("@", ""));
        twitterAccount.setValue(Integer.valueOf(editTextTwitterOrderValue.getText().toString()));
        return twitterAccount;
    }

    private void clearDataAfterChange() {
        editTextTwitterScreenName.setText("");
        editTextTwitterOrderValue.setText("");
        editTextName.setText("");
        twitterAccount = null;
    }

    private void showToast(String text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
    }
}
