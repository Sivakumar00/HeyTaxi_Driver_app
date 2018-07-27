package com.siva;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.siva.Common.Common;
import com.siva.Helper.DirectionJSONParser;
import com.siva.Remote.IGoogleAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Welcome extends AppCompatActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener,LocationListener
{

    private GoogleMap mMap;
    //payservices
    TextView accuracy,meters;
    private static final int MY_PERMISSION_REQUEST_CODE=7000;
    private static final int PLAY_SERVICES_RES_REQUEST=7001;
    ActionBar actionBar;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Menu menu;
    private static int UPDATE_INTERVAL=5000;
    private static int FASTEST_INTERVAL=3000;
    private static int DISPLACEMENT=10;
    private Toolbar mToolbar;
    DatabaseReference drivers;
    GeoFire geoFire;
    Marker mCurrent;
    MaterialAnimatedSwitch location_switch;
    SupportMapFragment mapFragment;
    FirebaseAuth mAuth;
    private Polyline direction;
    //car animation variables
    private List<LatLng> polyLineList;
    private Marker carMarker;
    private float v;
    private double lat,lng;
    private Handler handler=new Handler();
    private LatLng startPosition,endPosition,currentPosition;
    private int index,next;
    //private ImageButton btnGo;
    private PlaceAutocompleteFragment places;
    private String destination;
    private PolylineOptions polylineOptions,blackPolylineOptions;
    private Polyline blackPolyLine,greyPolyline;
    private IGoogleAPI mService;
    Runnable drawPathRunnable=new Runnable() {
        @Override
        public void run() {
            if(index<polyLineList.size()-1){
                index++;
                next=index+1;
            }
            if(index<polyLineList.size()-1){
                startPosition=polyLineList.get(index);
                endPosition=polyLineList.get(next);

            }
            final ValueAnimator valueAnimator=ValueAnimator.ofFloat(0,1);
            valueAnimator.setDuration(3000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    v=valueAnimator.getAnimatedFraction();
                    lng=v*endPosition.longitude+(1-v)*startPosition.longitude;
                    lat=v*endPosition.latitude+(1-v)*startPosition.latitude;
                    LatLng newPos=new LatLng(lat,lng);
                    carMarker.setPosition(newPos);
                    carMarker.setAnchor(0.5f,0.5f);
                    carMarker.setRotation(getBearing(startPosition,newPos));
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                    .target(newPos)
                            .zoom(15.5f)
                            .build()
                    ));
                }
            });
            valueAnimator.start();
            handler.postDelayed(this, 3000);
        }
    };

    private float getBearing(LatLng startPosition, LatLng newPos) {
        double lat=Math.abs(startPosition.latitude-endPosition.latitude);
        double lng=Math.abs(startPosition.longitude-endPosition.longitude);

        if(startPosition.latitude<endPosition.latitude&&startPosition.longitude<endPosition.longitude){
            return (float)(Math.toDegrees(Math.atan(lng/lat)));
        }
        else if(startPosition.latitude>=endPosition.latitude&&startPosition.longitude<endPosition.longitude){
            return (float)((90-Math.toDegrees(Math.atan(lng/lat)))+90);
        }
        else if(startPosition.latitude>=endPosition.latitude&&startPosition.longitude>=endPosition.longitude){
            return (float)(Math.toDegrees(Math.atan(lng/lat))+180);
        }
        else if(startPosition.latitude<endPosition.latitude&&startPosition.longitude>=endPosition.longitude){
            return (float)((90-Math.toDegrees(Math.atan(lng/lat)))+270);
        }
      return -1;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_welcome);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        accuracy=(TextView)findViewById(R.id.accuracy);
        meters=(TextView)findViewById(R.id.meters);

         mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            Snackbar.make(mapFragment.getView(),"Turn ON GPS Location..",Snackbar.LENGTH_LONG).show();
        }

        mToolbar=(Toolbar)findViewById(R.id.welcome_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Where is my Bus?");
        //init
        drivers= FirebaseDatabase.getInstance().getReference("Drivers");
        geoFire=new GeoFire(drivers);

        mAuth=FirebaseAuth.getInstance();
        Paper.init(this);

        places=(PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.places_autocomplete_fragment);
        places.setBoundsBias(new LatLngBounds(
                new LatLng(10.62267107, 78.5621312),
                new LatLng(10.9111352,78.90625977)));
        places.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if(location_switch.isChecked()){
                    destination=place.getAddress().toString();
                    destination=destination.replace(" ","+");
                    getDirection();
                }else {
                    Toast.makeText(Welcome.this,"Please Change your status to ONLINE",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(Welcome.this,""+status.toString(),Toast.LENGTH_LONG).show();
            }
        });
       ImageButton logout=(ImageButton)findViewById(R.id.btn_logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                sendToStart();
            }
        });
        location_switch=(MaterialAnimatedSwitch)findViewById(R.id.location_switch);
        location_switch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean isOnline) {
                if(isOnline){
                   // turnOnGPS();
                    startLocationUpdates();
                    accuracy.setVisibility(View.VISIBLE);
                    meters.setVisibility(View.VISIBLE);
                    getAccuracyLocation();
                    displayLocation();

                    Snackbar.make(mapFragment.getView(),"Driver Mode ON..!",Snackbar.LENGTH_SHORT).show();
                }else {
                    if(carMarker!=null) {
                        stopLocationUpdates();
                        geoFire.removeLocation(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        carMarker.remove();
                        mMap.clear();
                        accuracy.setVisibility(View.INVISIBLE);
                        meters.setVisibility(View.INVISIBLE);
                        handler.removeCallbacks(drawPathRunnable);
                        Snackbar.make(mapFragment.getView(), "Driver Mode OFF..!", Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });

        polyLineList =new ArrayList<>();

        //geofire

        setUpLocation();
        mService= Common.getGoogleAPI();


    }

   //google docs
    private void getDirection() {
        currentPosition=new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
        String requestApi=null;
        try{
            requestApi="https://maps.googleapis.com/maps/api/directions/json?"+"mode=driving&"
                    +"transit_routing_preference=less_driving&"
                    +"origin="+currentPosition.latitude+","+currentPosition.longitude+"&"
                    +"destination="+destination+"&"+"key=AIzaSyBZKTJisYnRgbUdxWFj5we_4zS6fD0g9G8";
            Log.d("SIVA",requestApi);
            mService.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                 public void onResponse(Call<String> call, Response<String> response) {

                    try {
                        new Welcome.ParserTask().execute(response.body().toString());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }




                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(Welcome.this,""+t.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }
//github decode poly-json parser
    private List decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser mCurrentUser=mAuth.getCurrentUser();
        if(mCurrentUser==null){
            sendToStart();
        }
    }

    private void sendToStart() {
        Paper.book().destroy();
        Intent intent=new Intent(Welcome.this,MainActivity.class);
        startActivity(intent);

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        geoFire.removeLocation(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    @Override
    protected void onResume() {
        super.onResume();

        displayLocation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        geoFire.removeLocation(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        geoFire.removeLocation(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    private void setUpLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED&&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED ){
           //runtime permission
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            },MY_PERMISSION_REQUEST_CODE);
        }
        else{
            if(checkPlayServices()){
                buildGoogleApiClient();
                createLocationRequest();
                if(location_switch.isChecked())
                    displayLocation();
            }
        }


    }

    private void createLocationRequest() {
        mLocationRequest =new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);


    }

    private void buildGoogleApiClient() {
        mGoogleApiClient=new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(LocationServices.API)
        .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode= GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode!=ConnectionResult.SUCCESS){
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICES_RES_REQUEST).show();
            else {
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }return false;
        }
    return true;
    }

    private void stopLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED&&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED ){
            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION_REQUEST_CODE:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    if(checkPlayServices()){
                        buildGoogleApiClient();
                        createLocationRequest();
                        if(location_switch.isChecked())
                            displayLocation();
                    }
                }
        }
    }

    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED&&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED ){
            return;
        }
       mLastLocation=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation!=null){
            if(location_switch.isChecked()){
                final double latitude=mLastLocation.getLatitude();
                final double longitude=mLastLocation.getLongitude();

                //update to Firebase

                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        //Add marker
                        if(carMarker!=null){
                            carMarker.remove();
                        }


                                carMarker = mMap.addMarker(new MarkerOptions()
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_bus))
                                        .position(new LatLng(latitude, longitude))
                                        .title("Your Location"));
                                //move view to position
                        getAccuracyLocation();
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f));



                    }
                });

            }
        }else {
            Log.d("Error"," cannot get your location");
        }


    }
    private void getAccuracyLocation(){
        int i=(int)mLastLocation.getAccuracy();
        if(i<10){
            accuracy.setText("Location Accuracy : Very Good");
            accuracy.setTextColor(Color.rgb(127,255,0));
            meters.setText("Deflected Rate :"+i);
            meters.setTextColor(Color.rgb(127,255,0));
        }
        if(i>10 && i <150){
            accuracy.setText("Location Accuracy : Good");
            accuracy.setTextColor(Color.rgb(0,128,0));
            meters.setText("Deflected Rate :"+i);
            meters.setTextColor(Color.rgb(0,128,0));
        }
        if( i >=150&&i<600){
            accuracy.setText("Location Accuracy : Medium");
            accuracy.setTextColor(Color.rgb(255,140,0));
            meters.setText("Deflected Rate :"+i);
            meters.setTextColor(Color.rgb(255,140,0));
        }
        if( i >300){
            accuracy.setText("Location Accuracy : Poor");
            accuracy.setTextColor(Color.RED);
            meters.setText("Deflected Rate :"+i);
            meters.setTextColor(Color.RED);
        }

    }
    private void rotateMarker(final Marker mCurrent, final float i, GoogleMap mMap) {
        final Handler handler=new Handler();
        final long start= SystemClock.uptimeMillis();
        final float startRotation=mCurrent.getRotation();
        final long duration=1500;

        final Interpolator interpolator = new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
               long elapsed=SystemClock.uptimeMillis() - start;
                float t=interpolator.getInterpolation((float)elapsed/duration);
                float rot=t*i+(1-t)*startRotation;
                mCurrent.setRotation(-rot>180?rot/2:rot);
                if(t<1.0){
                    handler.postDelayed(this,16);
                }
            }
        });


    }

    private void startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED&&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED ){
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(true);
        mMap.setBuildingsEnabled(true);

        mMap.getUiSettings().setZoomControlsEnabled(true);


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        displayLocation();
        startLocationUpdates();

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        geoFire.removeLocation(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation=location;
        displayLocation();
    }
    private class ParserTask extends AsyncTask<String,Integer,List<List<HashMap<String,String>>>> {

        ProgressDialog dialog=new ProgressDialog(Welcome.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Please wait..");
            dialog.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes=null;
            try {
                jObject=new JSONObject(strings[0]);
                DirectionJSONParser parser=new DirectionJSONParser();// google la iruku...
                routes=parser.parse(jObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            dialog.dismiss();
            ArrayList points=null;
            PolylineOptions polylineOptions=null;

            for (int i=0;i<lists.size();i++){
                points=new ArrayList();
                polylineOptions=new PolylineOptions();

                List<HashMap<String,String>> path=lists.get(i);
                for (int j=0;j<path.size();j++){
                    HashMap<String,String> point =path.get(j);
                    double lat=Double.parseDouble(point.get("lat"));
                    double lng=Double.parseDouble(point.get("lng"));

                    LatLng position=new LatLng(lat,lng);
                    points.add(position);

                }
                polylineOptions.addAll(points);

                polylineOptions.width(10);
                polylineOptions.color(Color.BLUE);
                polylineOptions.geodesic(true);

            }
            if(points.size()!=0)
               if(direction==null) {
                   direction = mMap.addPolyline(polylineOptions);
               }
            if(direction!=null){
                direction.remove();
                direction=mMap.addPolyline(polylineOptions);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.menu=menu;
        getMenuInflater().inflate(R.menu.welcome_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);


        if (item.getItemId() == R.id.welcome_about){
            /*Intent intent1 = new Intent(getApplicationContext(), AboutActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent1);
            */
        }
        if(item.getItemId()==R.id.welcome_bus_list) {
            Intent intent = new Intent(getApplicationContext(), BusListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

        }


        return true;
    }

}
