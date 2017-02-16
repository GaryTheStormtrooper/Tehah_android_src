package com.github.nkzawa.socketio.androidchat;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBarActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;


public class MainActivity extends ActionBarActivity  implements
        ConnectionCallbacks, OnConnectionFailedListener {


    public static GoogleApiClient mGoogleApiClient;
    public static Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

    }

    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public void onConnected(Bundle connectionHint) {


        //flag 1
        Toast.makeText(this, "onConnected start", Toast.LENGTH_LONG).show();


        String mLatitudeText = "";
        String mLongitudeText = "";

        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            //flag 1b
            Toast.makeText(this, "access permission not granted", Toast.LENGTH_LONG).show();
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
        }
        else
        {

            //flag 2
            Toast.makeText(this, "access permission granted", Toast.LENGTH_LONG).show();
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            //flag 3
            Toast.makeText(this, "my last location object: " + mLastLocation, Toast.LENGTH_LONG).show();

        }
        if (mLastLocation != null) {
            mLatitudeText = String.valueOf(mLastLocation.getLatitude());
            mLongitudeText = String.valueOf(mLastLocation.getLongitude());
            Toast.makeText(this, "coordinates: " +mLatitudeText + "," + mLongitudeText, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "onConnectionSuspended", Toast.LENGTH_LONG).show();
    }

    @Override
    //Connection Debugger
    //Makes text pop up of error codes
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "onConnectionFailed, error code: " + connectionResult.getErrorCode(), Toast.LENGTH_SHORT).show();
        // Toast.makeText(this, "onConnectionFailed " + connectionResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }
}
