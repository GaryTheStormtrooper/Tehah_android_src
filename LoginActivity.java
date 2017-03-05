package com.github.nkzawa.socketio.tehahChat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.SharedPreferences;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.view.Gravity;
import java.util.Random;

/**
 * A login screen that offers login via username.
 */
public class LoginActivity extends Activity implements
        ConnectionCallbacks, OnConnectionFailedListener {

    private EditText mUsernameView;

    private String mUsername;

    private Socket mSocket;

    public static GoogleApiClient mGoogleApiClient;
    public static Location mLastLocation;
    public String userLatitudeText;
    public String userLongitudeText;

    String uniqueID = MainActivity.uniqueID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        ChatApplication app = (ChatApplication) getApplication();
        mSocket = app.getSocket();


        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username_input);
        mUsernameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button signInButton = (Button) findViewById(R.id.make_room_button);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                createRoom();
            }
        });

        Button refresher = (Button) findViewById(R.id.roomRefresh_button);
        refresher.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                refresher();
            }
        });

        /////// Insert suggested nickname ///////////
        // uses NicknameSuggester
        NicknameSuggester ns = new NicknameSuggester();
        TextView roomNameText = (TextView) findViewById(R.id.roomname_input);
        TextView nicknameText = (TextView) findViewById(R.id.username_input);
        String suggestedName = ns.suggestName();

        roomNameText.setText("Room " + suggestedName);
        nicknameText.setText(suggestedName);

        roomNameText.setSelectAllOnFocus(true);
        nicknameText.setSelectAllOnFocus(true);
        nicknameText.requestFocus();

        Random randomGenerator0 = new Random();
        int randomInt0 = randomGenerator0.nextInt(20);
        randomInt0 = 1;
        for (int i = 0; i < randomInt0; i++) {
            Random randomGenerator = new Random();
            int randomInt = randomGenerator.nextInt(10);
            Button myButton = new Button(this);
            myButton.setText("Pomona-" + randomInt);

            LinearLayout ll = (LinearLayout) findViewById(R.id.room_box);
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            myButton.setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        attemptLogin();
                                    }});
            ll.addView(myButton, lp);
        }

        //handle if there are no rooms found
        if (randomInt0 == 0)
        {
            LinearLayout ll = (LinearLayout) findViewById(R.id.room_box);
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            TextView padding = new TextView(this);
            TextView tv = new TextView(this);
            tv.setText("( No rooms are near you...Search again! )");
            tv.setGravity(Gravity.CENTER);
            ll.addView(padding);
            ll.addView(tv);

        }

        mSocket.on("login", onLogin);
        mSocket.on("join", onJoin);

        //mSocket.on("display rooms", updateRoomList);
        mSocket.on("get local", updateLocalRoomList);


        // device id maker
        SharedPreferences sharedPref = this.getSharedPreferences("com.Tehah.sharedPref", Context.MODE_PRIVATE);
        if (sharedPref.getString("userID", "").length() == 0) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("userID", uniqueID);
            editor.commit();
        }
        Toast.makeText(getApplicationContext(), "sharedPref " + sharedPref.getString("userID", "sharedPref not found"), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mSocket.off("login", onLogin);
    }

    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mUsernameView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString().trim();

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {

            // There was an error; don't attempt login and focus the first
            // form field with an error.
            mUsernameView.setError(getString(R.string.error_field_required));
            mUsernameView.requestFocus();
            return;
        }

        mUsername = username;
        //Toast.makeText(this, "attemptLogin() " + mUsername, Toast.LENGTH_SHORT).show();
        // perform the user login attempt.
        mSocket.emit("add user", username);
    }

    private void sendID() {
        Toast.makeText(this, "sendID " + uniqueID, Toast.LENGTH_SHORT).show();
        mSocket.emit("send id", uniqueID);
    }

    private void initializeRoomList(){
        //Toast.makeText(this, "initializeRoomList()", Toast.LENGTH_SHORT).show();
        refresher();
    }

    private void createRoom() {

        if (userLatitudeText == null)
        {
            Toast.makeText(this, "Location not found", Toast.LENGTH_LONG).show();
            return;
        }
        // Reset errors.
        mUsernameView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString().trim();

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {

            // There was an error; don't attempt login and focus the first
            // form field with an error.
            mUsernameView.setError(getString(R.string.error_field_required));
            mUsernameView.requestFocus();
            return;
        }

        mUsername = username;

        TextView roomNameText = (TextView) findViewById(R.id.roomname_input);
        JSONObject chatRoomInfo = new JSONObject();
        try {
            chatRoomInfo.put("roomName", roomNameText.getText().toString());
            chatRoomInfo.put("latitude", userLatitudeText);
            chatRoomInfo.put("longitude", userLongitudeText);
            chatRoomInfo.put("nameID", uniqueID);

        } catch (JSONException e ){}

        Toast.makeText(this, "createRoom() " + roomNameText.getText().toString() + " " + uniqueID, Toast.LENGTH_SHORT).show();

        // perform the user login attempt.
        mSocket.emit("create room", chatRoomInfo);
    }


    private void refresher(){

        if (userLatitudeText == null)
        {
            Toast.makeText(this, "Location not found", Toast.LENGTH_LONG).show();
            return;
        }

        JSONObject refreshInfo = new JSONObject();
        try {
            //refreshInfo.put("nameID", uniqueID);
            //refreshInfo.put("latitude", userLatitudeText);
            //refreshInfo.put("longitude", userLongitudeText);
            refreshInfo.put("nameID", uniqueID);
            refreshInfo.put("latitude", 35);
            refreshInfo.put("longitude", 117);
        } catch (JSONException e ){}
        Toast.makeText(this, "refresher() " + refreshInfo, Toast.LENGTH_SHORT).show();
        mSocket.emit("display local", refreshInfo);
    }

    private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];

            int numUsers;
            try {
                numUsers = data.getInt("numUsers");
            } catch (JSONException e) {
                return;
            }

            Intent intent = new Intent();
            intent.putExtra("username", mUsername);
            intent.putExtra("numUsers", numUsers);
            setResult(RESULT_OK, intent);
            finish();
        }
    };

    private Emitter.Listener onJoin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];

            int numUsers;
            //String
            try {
                numUsers = data.getInt("numUsers");
            } catch (JSONException e) {
                return;
            }

            Intent intent = new Intent();
            intent.putExtra("username", mUsername);
            intent.putExtra("numUsers", numUsers);
            setResult(RESULT_OK, intent);
            finish();
        }
    };

    private Emitter.Listener updateLocalRoomList = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Toast.makeText(getApplicationContext(), "get local received", Toast.LENGTH_SHORT).show();

/*
            JSONArray roomArrays;
            try {
                roomArrays = data.getJSONArray("roomList");
            } catch (JSONException e) {
                return;
            }

            Intent intent = new Intent();
            intent.putExtra("username", mUsername);
            intent.putExtra("numUsers", numUsers);
            setResult(RESULT_OK, intent);
            finish();*/
        }
    };

    @Override
    protected void onStart() {

        //flag 0
        //Toast.makeText(this, "login.onStart start", Toast.LENGTH_SHORT).show();

        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    //@Override
    public void onConnected(Bundle connectionHint) {


        userLatitudeText = "";
        userLongitudeText = "";
        Location userLastLocation;

        //flag 1
        //Toast.makeText(this, "onConnected start", Toast.LENGTH_LONG).show();

        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            //flag 2a
            Toast.makeText(this, "Google Play permission not granted", Toast.LENGTH_LONG).show();
            userLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
        }
        else
        {
            //flag 2
            //Toast.makeText(this, "access permission granted", Toast.LENGTH_LONG).show();
            userLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            //flag 3
            //Toast.makeText(this, "my last location object: " + userLastLocation, Toast.LENGTH_LONG).show();

        }
        if (userLastLocation != null) {
            userLatitudeText = String.valueOf(userLastLocation.getLatitude());
            userLongitudeText = String.valueOf(userLastLocation.getLongitude());
            //Toast.makeText(this, "Your coordinates: " + userLatitudeText + ", " + userLongitudeText, Toast.LENGTH_LONG).show();

            TextView tv = (TextView) findViewById(R.id.location_teller);
            tv.setText("Your coordinates: " + userLatitudeText + ", " + userLongitudeText);
        }
    }



    /////// Debuggers
    //
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

 }
