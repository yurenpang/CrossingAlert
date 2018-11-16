//package com.rockpang.crossingalert;
//import android.content.pm.PackageManager;
//import android.content.Context;
//import android.os.AsyncTask;
//import android.os.Environment;
//import android.os.Handler;
//import android.media.MediaPlayer;
//import android.media.AudioManager;
//import android.net.Uri;
//import android.os.Bundle;
//import android.support.annotation.MainThread;
//import android.support.v4.app.ActivityCompat;
//import android.support.v7.app.AppCompatActivity;
//import android.view.View;
//import android.widget.*;
//import android.location.Location;
//
//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.tasks.OnSuccessListener;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.UnsupportedEncodingException;
//import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.text.DecimalFormat;
//
//import static android.Manifest.permission.ACCESS_FINE_LOCATION;
//import static android.Manifest.permission.INTERNET;
//
//public class MainActivity extends AppCompatActivity {
//    private FusedLocationProviderClient client;
//    private double lat;
//    private double lon;
//    private static double MAC_LAT = 44.937893;
//    private static double MAC_LON = -93.169043;
//    private double nextIntersection_latitude;
//    private double getNextIntersection_longitude;
//    private static String URLSTRING = "http://api.geonames.org/findNearestIntersectionJSON?lat=44.936963&lng=-93.166903&radius=0.01&username=RockMPang";
//
//    AudioManager am;
//    TextView textJsonResponse;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        textJsonResponse = findViewById(R.id.textJsonResponse);
//
//        TextView traffic = findViewById(R.id.trafficLight);
//        traffic.setText("Traffic Light:\n"+"Latitude: " + MAC_LAT + "\n" + "Longitude: " + MAC_LON + "\n");
//
//        ProgressBar pb = findViewById(R.id.volume);
//        pb.setProgress(80);
//        lat = 40.0;
//        lon = -90.0;
//        TextView textView = findViewById(R.id.location);
//        textView.setText("Position:\n" + "Latitude: " + lat + "\n" + "Longitude: " + lon + "\n");
//
//        am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
//        ActivityCompat.requestPermissions(this, new String[]{INTERNET}, 1);
//        Button button = findViewById(R.id.getLocation);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                double dist1 = distance();
//                lat += 0.15;
//                lon -= 0.15;
//                TextView textView = findViewById(R.id.location);
//                DecimalFormat df = new DecimalFormat("#.#####");
//                textView.setText("Position:\n" + "Latitude: " + df.format(lat) + "\n" + "Longitude: " + df.format(lon)+ "\n");
//
//                new MyTask().execute();
//
//                double dist = distance();
//
//                TextView distText = findViewById(R.id.dist);
//                distText.setText("Distance: " + dist);
//
//                if(dist < 2){
//                    ProgressBar pb = findViewById(R.id.volume);
//                    if(dist < dist1){
//                        pb.setProgress(pb.getProgress() - 5);
//                        decreaseVolume();
//                    } else {
//                        pb.setProgress(pb.getProgress() + 5);
//                        increaseVolume();
//                    }
//                }
//            }
//        });
//    }
//
//    private class MyTask extends AsyncTask<Void, Void, Void> {
//        String textResult;
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            URL url;
//            try {
//                HttpURLConnection urlConnection = null;
//                url = new URL(URLSTRING);
//
//                BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
//
//                String stringBuffer;
//                String stringText = "";
//
//                while ((stringBuffer = br.readLine()) != null) {
//                    stringText = stringText + stringBuffer;
//                }
//                br.close();
//                JSONObject json = new JSONObject(stringText);
//                String longitude = (String) json.getJSONObject("intersection").get("lng");
//                String latitude = (String) json.getJSONObject("intersection").get("lat");
//                textResult = "Latitude: " + latitude + "\n" + "Longtitude: " + longitude;
//            } catch(MalformedURLException e) {
//                e.printStackTrace();
//                textResult = e.toString();
//            } catch(IOException e) {
//                e.printStackTrace();
//                textResult = e.toString();
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            textJsonResponse.setText(textResult);
//            super.onPostExecute(aVoid);
//        }
//    }
//
//    private double distance(){
//        return Math.sqrt((lat - MAC_LAT)*(lat - MAC_LAT) + (lon - MAC_LON)*(lon - MAC_LON));
//    }
//
//    private void getLocation(){
//        requestPermission();
//        client = LocationServices.getFusedLocationProviderClient(this);
//        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
//            @Override
//            public void onSuccess(Location location) {
//                if(location != null){
//                    TextView textView = findViewById(R.id.location);
//                    lat = location.getLatitude();
//                    lon =  location.getLongitude();
//                }
//            }
//        });
//    }
//
//
//    private void requestPermission(){
//        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
//    }
//
//    private void increaseVolume(){
//        am.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
//    }
//
//    private void decreaseVolume(){
//        am.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
//    }
//}