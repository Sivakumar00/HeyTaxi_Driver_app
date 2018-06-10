package com.siva.Common;

import android.location.Location;

import com.siva.Model.User;
import com.siva.Remote.FCMClient;
import com.siva.Remote.IFCMService;
import com.siva.Remote.IGoogleAPI;
import com.siva.Remote.RetrofitClient;

/**
 * Created by MANIKANDAN on 01-12-2017.
 */

public class Common {

    public static Location mLastLocation=null;

    public static final String driver_tbl="Drivers";
    public static final String user_driver_tbl="DriversInformation";
    public static final String user_rder_tbl="RidersInformation";
    public static final String pickup_request_tbl="PickupRequest";
    public static final String token_tbl="Tokens";
    public static User currentUser;
    public static final String user_field="usr";
    public static final String pwd_field="pwd";


    public static final String baseURL="https://maps.googleapis.com";
    public static final String fcmURL="https://fcm.googleapis.com/";

    public static IGoogleAPI getGoogleAPI(){
        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);

    }
    public static IFCMService getFCMService(){
        return FCMClient.getClient(fcmURL).create(IFCMService.class);

    }
}
