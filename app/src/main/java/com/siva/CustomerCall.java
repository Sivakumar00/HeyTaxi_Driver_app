package com.siva;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.siva.Common.Common;
import com.siva.Model.FCMResponse;
import com.siva.Model.Notification;
import com.siva.Model.Sender;
import com.siva.Model.Token;
import com.siva.Remote.IFCMService;
import com.siva.Remote.IGoogleAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.siva.Common.Common.mLastLocation;

public class CustomerCall extends AppCompatActivity {

    TextView txtTime,txtAddress,txtDistance;

    MediaPlayer mediaPlayer;
    IGoogleAPI mService;
    Button btnAccept,btnCancel;
    IFCMService mFCMService;
    double lat,lng;
    String customerId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_call);
        txtAddress=(TextView)findViewById(R.id.txtAddress);
        txtDistance=(TextView)findViewById(R.id.txtDistance);
        txtTime=(TextView)findViewById(R.id.txtTime);


        btnAccept=(Button)findViewById(R.id.btnAccept);
        btnCancel=(Button)findViewById(R.id.btnDecline);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(customerId)){
                    cancelBooking(customerId);
                }
            }
        });

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(CustomerCall.this,DriverTracking.class);
                intent.putExtra("lat",lat);
                intent.putExtra("lng",lng);
                intent.putExtra("customerId",customerId);
                startActivity(intent);
                finish();
            }
        });
        mService=Common.getGoogleAPI();
        mFCMService=Common.getFCMService();
        mediaPlayer=MediaPlayer.create(this,R.raw.ringtone);
        mediaPlayer.setLooping(true);
        Log.d("SIVA ERROR","media reached");
        mediaPlayer.start();

        if(getIntent()!=null){
             lat=getIntent().getDoubleExtra("lat",-1.0);
             lng=getIntent().getDoubleExtra("lng",-1.0);
            customerId =getIntent().getStringExtra("customer");
            getDirection(lat,lng);
        }
    }

    private void cancelBooking(String customerId) {

        Token token=new Token(customerId);
        Notification notification=new Notification("NOTICE","SOCIAL WORKER HAS CANCELLED YOUR REQUEST..!");
        Sender sender=new Sender(token.getToken(),notification);
        mFCMService.sendMessaage(sender).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if(response.body().success==1){
                    Toast.makeText(getApplicationContext(),"CANCELLED",Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });
    }

    private void getDirection(double lat,double lng) {
        String requestApi=null;
        try{
            requestApi="https://maps.googleapis.com/maps/api/directions/json?"+"mode=driving&"
                    +"transit_routing_preference=less_driving&"
                    +"origin="+ mLastLocation.getLatitude()+","+mLastLocation.getLongitude()+"&"
                    +"destination="+lat+","+lng+"&"+"key=AIzaSyBje0AqrNRCFrZq2WrodgxM9-4BxeoKO-4";
            Log.d("SIVA",requestApi);
            mService.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {

                    try {
                        JSONObject jsonObject=new JSONObject(response.body().toString());
                        JSONArray routes=jsonObject.getJSONArray("routes");
                        //first element of routes

                        JSONObject object=routes.getJSONObject(0);
                        //after ..get array with name "legs"
                        JSONArray legs=object.getJSONArray("legs");
                        //first element of legs
                        JSONObject legsObject=legs.getJSONObject(0);
                        //get Distance
                        JSONObject distance=legsObject.getJSONObject("distance");
                        txtDistance.setText(distance.getString("text"));

                        //get time
                        JSONObject time=legsObject.getJSONObject("duration");
                        txtTime.setText(time.getString("text"));

                        //get address
                        String address=legsObject.getString("end_address");
                        txtAddress.setText(address);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(CustomerCall.this,""+t.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        mediaPlayer.release();
        super.onStop();
    }

    @Override
    protected void onPause() {
        mediaPlayer.release();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaPlayer.start();
    }
}
