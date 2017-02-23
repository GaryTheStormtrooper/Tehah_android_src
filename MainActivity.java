package com.github.nkzawa.socketio.tehahChat;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBarActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity  {

    private static GoogleApiClient mGoogleApiClient;
    private static Location userLastLocation;
    private static String userLatitudeText;
    private static String userLongitudeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    //.addConnectionCallbacks(this)
                    //.addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    /*
    @Override
    protected void onStart() {

        Toast.makeText(this, "main.onStart start", Toast.LENGTH_SHORT).show();

        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    */

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }
/*
    @Override
    public void onConnected(Bundle connectionHint) {

        userLatitudeText = "";
        userLongitudeText = "";

        //flag 1
        Toast.makeText(this, "onConnected start", Toast.LENGTH_LONG).show();

        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            //flag 1b
            Toast.makeText(this, "access permission not granted", Toast.LENGTH_LONG).show();
            userLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
        }
        else
        {

            //flag 2
            Toast.makeText(this, "access permission granted", Toast.LENGTH_LONG).show();
            userLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            //flag 3
            Toast.makeText(this, "my last location object: " + userLastLocation, Toast.LENGTH_LONG).show();

        }
        if (userLastLocation != null) {
            userLatitudeText = String.valueOf(userLastLocation.getLatitude());
            userLongitudeText = String.valueOf(userLastLocation.getLongitude());
            Toast.makeText(this, "coordinates: " + userLatitudeText + "," + userLongitudeText, Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "onConnectionSuspended", Toast.LENGTH_LONG).show();
    }

    @Override
    //Connection Debugger
    //Displays error codes
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "onConnectionFailed, error code: " + connectionResult.getErrorCode(), Toast.LENGTH_SHORT).show();
        // Toast.makeText(this, "onConnectionFailed " + connectionResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }

    */
}
