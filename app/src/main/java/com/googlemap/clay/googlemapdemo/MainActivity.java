package com.googlemap.clay.googlemapdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID;


public class MainActivity extends AppCompatActivity implements  OnMapReadyCallback,
                                  ActivityCompat.OnRequestPermissionsResultCallback{

    private TextView messageShow;
    private TextView lengthShow;

    private GoogleMap mMap;

    private UiSettings mUiSettings;

    private GoogleApiClient mGoogleApiClient;

    protected Location mLastKnownLocation;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private boolean mPermissionDenied = false;

    //for line
    private PolylineOptions lineOptions;

    private LatLng mLatLng;

    private int TIME = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messageShow = (TextView)findViewById(R.id.tv1);
        lengthShow = (TextView)findViewById(R.id.tv2);
        Button bt1 = (Button)findViewById(R.id.bt1);
        Button bt2 = (Button)findViewById(R.id.bt2);
        Log.d("debug", "Clay:: this is a DEBUG of MyAndroid. ");
        bt1.setOnClickListener(new bt1Action());
        bt2.setOnClickListener(new bt2Action());

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                //.enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                //.addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();

        lineOptions = new PolylineOptions();
        lineOptions.color(Color.RED);

    }

    class bt1Action implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            messageShow.setText(R.string.showM);
            Log.d("debug", "Clay:: bt1Action ");
            enableMyLocation();
            getDeviceLocation();

        }
    }

    class bt2Action implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.d("debug", "Clay:: bt2Action ");
            if(mMap != null){
                mMap.setMapType(MAP_TYPE_HYBRID);
            }
            //drawLine();

        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        //map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        mMap = map;
        mUiSettings = mMap.getUiSettings();
        Log.d("debug", "Clay:: onMapReady");
        getDeviceLocation();
        timer.schedule(task, 2000, 3000); // 1s后执行task,经过3s再次执行
        enableMyLocation();
        mUiSettings.setMyLocationButtonEnabled(false); //disable the location button UI

    }



    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
             //Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        }
             else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            //showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    private void getDeviceLocation() {
        Log.d("debug", "Clay:: getDeviceLocation");
    /*
     * Before getting the device location, you must check location
     * permission, as described earlier in the tutorial. Then:
     * Get the best and most recent location of the device, which may be
     * null in rare cases when a location is not available.
     */
//        if (mPermissionDenied) {
//            mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            Log.d("debug", "Clay:: get the devices's location");
            mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            Log.d("debug", "Clay:: location --> " + mLastKnownLocation);
        }

        // Set the map's camera position to the current location of the device.
//        if (mCameraPosition != null) {
//            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
//        } else
        if (mLastKnownLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude()), 14));
        } else {
            Log.d("Clay:: ", "Current location is null. Using defaults.");
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
//            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }


    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
//                tvShow.setText(Integer.toString(i++));
                //getDeviceLocation();
                drawLine();
            }
            super.handleMessage(msg);
        };
    };
    Timer timer = new Timer();
    TimerTask task = new TimerTask() {

        @Override
        public void run() {
            // 需要做的事:发送消息
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
        }
    };

    private void drawLine(){
//        LatLng ll1 = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
//        LatLng ll2 = new LatLng(48.578503, -121.167366);
//        LatLng ll3 = new LatLng(49.578503, -120.167366);
//        lineOptions.add(ll1);
//        lineOptions.add(ll2);
        getDeviceLocation();
        mLatLng = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
        lineOptions.add(mLatLng);
        mMap.addPolyline(lineOptions);
        lengthShow.setText("Length: " + getLength());
    }

    private float getLength(){

        List<LatLng> latlngs = lineOptions.getPoints();
        int size = latlngs.size() - 1;
        float[] results = new float[1];
        float sum = 0;

        for(int i = 0; i < size; i++){
            Location.distanceBetween(
                    latlngs.get(i).latitude,
                    latlngs.get(i).longitude,
                    latlngs.get(i+1).latitude,
                    latlngs.get(i+1).longitude,
                    results);
            sum += results[0];
        }
        return sum;
    }




}
