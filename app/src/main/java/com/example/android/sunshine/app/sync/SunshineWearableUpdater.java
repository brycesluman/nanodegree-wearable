package com.example.android.sunshine.app.sync;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.sunshine.app.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by bryce on 12/2/16.
 */

public class SunshineWearableUpdater implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public final String LOG_TAG = SunshineWearableUpdater.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private DataMap mMap;
    private Context mContext;
    public SunshineWearableUpdater(Context context, DataMap map) {
        Log.d(LOG_TAG, "initialized");
        mMap = map;
        mContext = context;
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(LOG_TAG, "connected");
        updateWearable(mMap);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(LOG_TAG, "connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(LOG_TAG, "connection failed");
    }

    public void updateWearable(DataMap map) {
        byte[] rawData = map.toByteArray();
        sendMessage( "/watchface", map );
    }
    private void sendMessage( final String path, final DataMap data ) {
        new Thread( new Runnable() {
            @Override
            public void run() {
                if (mGoogleApiClient.isConnected()) {
                    PutDataMapRequest dataMapRequest = PutDataMapRequest.create(path);
                    // Make sure the data item is unique. Usually, this will not be required, as the payload
                    // (in this case the title and the content of the notification) will be different for almost all
                    // situations. However, in this example, the text and the content are always the same, so we need
                    // to disambiguate the data item by adding a field that contains teh current time in milliseconds.
                    dataMapRequest.getDataMap().putAll(data);
//                    dataMapRequest.getDataMap().putDouble("timestamp", System.currentTimeMillis());
//                    dataMapRequest.getDataMap().putString("title", "This is the title");
//                    dataMapRequest.getDataMap().putString("content", "This is a notification with some text.");
                    PutDataRequest putDataRequest = dataMapRequest.asPutDataRequest();
                    Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest);
                }
                else {
                    Log.e("SunshineWearableUpdater", "No connection to wearable available!");
                }
            }
        }).start();
    }
}
