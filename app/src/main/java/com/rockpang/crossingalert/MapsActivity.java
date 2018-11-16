package com.rockpang.crossingalert;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //Test
    private static final String TAG = "MyActivity";

    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private Location currentLocation;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        startLocationUpdates();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Tigger new location updates at interval
     * Reference: https://github.com/codepath/android_guides/wiki/Retrieving-Location-with-LocationServices-API
     */
    protected void startLocationUpdates() {
        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if(checkPermission()) {
            getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            // do work here
                            currentLocation = locationResult.getLastLocation();
                            onLocationChanged(currentLocation);
                            new MyTask().execute();
                        }
                    },
                    Looper.myLooper());
        }
    }

    /**
     * Find the longitude and latitude of current location
     * @param location
     */
    public void onLocationChanged(Location location) {
        Toast.makeText(this, locationToString(location), Toast.LENGTH_SHORT).show();

        // You can now create a LatLng Object for use with maps
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in 1600 Grand Avenue and move the camera
        LatLng mac = new LatLng(44.938515, -93.167380);
        mMap.addMarker(new MarkerOptions().position(mac).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mac));

        if(checkPermission()) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            Toast.makeText(this, "Oh No", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This function checks if the device gives GPS access to Google map
     * @return boolean
     */
    private boolean checkPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            requestPermissions();
            return false;
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                1234);
    }

    private String locationToString(Location location) {
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        return msg;
    }


    private String urlToString(Location location){
        return "http://api.geonames.org/findNearestIntersectionJSON?lat="
                + location.getLatitude() + "&lng=" + location.getLongitude()
                + "&radius=0.01&username=RockMPang";
    }

    private class MyTask extends AsyncTask<Void, Void, Void> {
        String textResult;

        @Override
        protected Void doInBackground(Void... voids) {
            URL url;
            try {
                url = new URL(urlToString(currentLocation));

                BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

                String stringBuffer;
                String stringText = "";

                while ((stringBuffer = br.readLine()) != null) {
                    stringText = stringText + stringBuffer;
                }

                br.close();
                JSONObject json = new JSONObject(stringText);
                String longitude = (String) json.getJSONObject("intersection").get("lng");
                String latitude = (String) json.getJSONObject("intersection").get("lat");
                textResult = "Latitude: " + latitude + "\n" + "Longtitude: " + longitude;
                Toast.makeText(MapsActivity.this, textResult, Toast.LENGTH_SHORT).show();
            } catch(MalformedURLException e) {
                e.printStackTrace();
                textResult = e.toString();
            } catch(IOException e) {
                e.printStackTrace();
                textResult = e.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(MapsActivity.this, textResult, Toast.LENGTH_SHORT).show();
            super.onPostExecute(aVoid);
        }
    }
}
