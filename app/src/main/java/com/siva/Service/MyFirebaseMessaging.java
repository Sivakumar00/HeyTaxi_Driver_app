package com.siva.Service;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.siva.CustomerCall;

/**
 * Created by MANIKANDAN on 24-12-2017.
 */

public class MyFirebaseMessaging extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e("eree",remoteMessage.getData().toString());
        LatLng customer_location=new Gson().fromJson(remoteMessage.getNotification().getBody(),LatLng.class);
        Intent intent=new Intent(getBaseContext(), CustomerCall.class);
        intent.putExtra("lat",customer_location.latitude);
        intent.putExtra("lng",customer_location.longitude);
        intent.putExtra("customer",remoteMessage.getNotification().getTitle());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }
}
