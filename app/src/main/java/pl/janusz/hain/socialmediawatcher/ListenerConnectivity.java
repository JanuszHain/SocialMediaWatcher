package pl.janusz.hain.socialmediawatcher;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * <br>
 * Provides testing and listening methods for connectivity changes.<br>
 * <br>
 * For API > 20 class uses {@link ConnectivityManager}, else {@link BroadcastReceiver}.
 */

public class ListenerConnectivity {

    private Context context;
    private ConnectivityManager connectivityManager;
    private ConnectivityReceiver connectivityReceiver;
    private NetworkCallback networkCallback;
    private EventBus eventBus;

    public ListenerConnectivity(Context context) {
        this.context = context;
        eventBus = EventBus.getDefault();
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public void registerListening() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            registerBroadcastReceiver();
        } else {
            registerListener();
        }
    }

    @SuppressLint("NewApi")
    private void registerListener() {
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        networkCallback = new NetworkCallback();
        connectivityManager.registerNetworkCallback(
                builder.build(), networkCallback);
    }

    private void registerBroadcastReceiver() {
        connectivityReceiver = new ConnectivityReceiver();
        context.registerReceiver(connectivityReceiver, new IntentFilter(
                "android.net.conn.CONNECTIVITY_CHANGE"));
    }


    public void unregisterListening() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            unregisterBroadcastReceiver();
        } else {
            unregisterListener();
        }
    }

    @SuppressLint("NewApi")
    private void unregisterListener() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        } catch (Exception e) {

        }
    }

    private void unregisterBroadcastReceiver() {
        try {
            context.unregisterReceiver(connectivityReceiver);
        } catch (Exception e) {

        }
    }

    private void notifyDeviceConnectedToInternet() {
        EventDeviceConnected eventDeviceConnected = new EventDeviceConnected();
        eventBus.post(eventDeviceConnected);
    }

    public void waitForTwitterReachable() {
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        URL url = new URL("https://twitter.com/");
                        HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                        urlc.setConnectTimeout(10 * 1000);
                        urlc.connect();
                        if (urlc.getResponseCode() == 200) {
                            urlc.disconnect();
                            notifyDeviceConnectedToInternet();
                        }
                    } catch (MalformedURLException e1) {
                    } catch (IOException e) {
                    }
                }
            }).start();
        }
    }

    public boolean isNetworkAvailable() {
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    public class ConnectivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isNetworkAvailable()) {
                waitForTwitterReachable();
            }
        }
    }

    @SuppressLint("NewApi")
    public class NetworkCallback extends ConnectivityManager.NetworkCallback {

        boolean connected;

        public NetworkCallback() {
            super();
            connected = false;
        }

        @Override
        public void onAvailable(Network network) {
            super.onAvailable(network);
            if (!connected) {
                connected = true;
                waitForTwitterReachable();
            }
        }
    }
}
