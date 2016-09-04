package pl.janusz.hain.socialmediawatcher;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

/**
 * A fragment representing main menu for Twitter actions.
 */

public class FragmentTwitterMainMenu extends Fragment {

    public FragmentTwitterMainMenu() {
    }

    public static FragmentTwitterMainMenu newInstance() {
        FragmentTwitterMainMenu fragment = new FragmentTwitterMainMenu();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.licenses:
                displayLicensesAlertDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_twitter_main_menu, container, false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void displayLicensesAlertDialog() {
        WebView view = (WebView) getLayoutInflater(Bundle.EMPTY).inflate(R.layout.dialog_licenses, null);
        view.loadUrl("file:///android_asset/licenses.html");
        new AlertDialog.Builder(getActivity())
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
