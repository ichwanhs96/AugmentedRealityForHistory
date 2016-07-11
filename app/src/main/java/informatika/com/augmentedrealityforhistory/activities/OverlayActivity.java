package informatika.com.augmentedrealityforhistory.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import informatika.com.augmentedrealityforhistory.models.ElevationResponseContainer;
import informatika.com.augmentedrealityforhistory.models.ElevationResponseResult;
import informatika.com.augmentedrealityforhistory.models.Response;
import informatika.com.augmentedrealityforhistory.R;
import informatika.com.augmentedrealityforhistory.util.GsonRequest;

/**
 * Created by Ichwan Haryo Sembodo on 07/06/2016.
 */

public class OverlayActivity extends AppCompatActivity implements SensorEventListener, LocationListener, View.OnClickListener {
    public List<Response> responseList;
    public Map<String, ImageView> markers;
    private RelativeLayout overlayViewInsideRelativeLayout;
    private RelativeLayout.LayoutParams layoutParams;

    //sensor
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private LocationManager mLocationManager;

    //variable for sensor
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;

    private float[] mTemporaryRotationMatrix = new float[9];
    private float[] mRotationMatrix = new float[9];
    private float[] mOrientation = new float[3];

    //device location
    private Location deviceLocation;

    //target location
    private Location targetLocation;
    private int targetPositionInList = 0;

    //distance between device location to target location
    private double distance = 0f;

    //angle between target and device
    private float angleToTarget = 0f;

    //altitude target and device
    private double deviceAltitude = 0f;
    private double targetAltitude = 0f;

    //screen width and height
    private int screenWidth;
    private int screenHeight;

    //text view
    private TextView altitudeTextView;
    private TextView targetTextView;
    private TextView distanceTextView;

    //image view
    private ImageView navArrow;
    private ImageView compassImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //screen width and height
        if (Build.VERSION.SDK_INT >= 11) {
            Point size = new Point();
            try {
                this.getWindowManager().getDefaultDisplay().getRealSize(size);
                screenWidth = size.x;
                screenHeight = size.y;
            } catch (NoSuchMethodError e) {
                Log.i("error", "it can't work");
            }

        } else {
            DisplayMetrics metrics = new DisplayMetrics();
            this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            screenWidth = metrics.widthPixels;
            screenHeight = metrics.heightPixels;
        }

        markers = new HashMap<>();

        responseList = new ArrayList<Response>();
        responseList.add(new Response("1", -6.891814, 107.610263, "itb", "", ""));
        responseList.add(new Response("2", -6.8957288, 107.6206774, "gedung sate", "", ""));
        responseList.add(new Response("3", -6.8816689, 107.6156134, "sabuga", "", ""));

        setContentView(R.layout.activity_overlay);

        navArrow = (ImageView) findViewById(R.id.navArrow);
        altitudeTextView = (TextView) findViewById(R.id.altitudeTextView);
        targetTextView = (TextView) findViewById(R.id.targetTextView);
        distanceTextView = (TextView) findViewById(R.id.distanceTextView);
        //compassImage = (ImageView) findViewById(R.id.compassImage);

        overlayViewInsideRelativeLayout = (RelativeLayout) findViewById(R.id.overlayViewInsideRelativeLayout);
        layoutParams = new RelativeLayout.LayoutParams(50, 50);
        for(Response response :responseList){
            ImageView iv = new ImageView(this);
            iv.setImageResource(R.drawable.marker);
            iv.setVisibility(View.INVISIBLE);
            iv.setLayoutParams(layoutParams);
            iv.setRotation(90);
            iv.setOnClickListener(this);
            markers.put(response.getId(), iv);
            overlayViewInsideRelativeLayout.addView(iv);
        }

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        //position tracking
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //permission checking from android studio ._.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        if(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Log.d("gps enabled", "gps enabled");
        }

        if(mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            Log.d("network enabled", "network enabled");
        }


        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        if (mLocationManager != null) {
            deviceLocation = mLocationManager
                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (deviceLocation != null) {
                Log.d("device location", "device location set");
                new loadARContent(this).execute("");
                new loadTargetElevation(this).execute("");
                new loadDeviceElevation(this).execute("");
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location == null) return;
        //call get altitude for device and
        //calculateAngleBetweenDeviceAndTarget();
        this.deviceLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mTemporaryRotationMatrix, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.remapCoordinateSystem(mTemporaryRotationMatrix, SensorManager.AXIS_X,
                    SensorManager.AXIS_Z, mRotationMatrix);
            SensorManager.getOrientation(mRotationMatrix, mOrientation);

            float azimuth = (float)Math.toDegrees(mOrientation[0]); // orientation contains: azimut, pitch and roll
            float pitch = (float)Math.toDegrees(mOrientation[1]);
            float roll = (float)Math.toDegrees(mOrientation[2]);

            if(deviceLocation != null && targetLocation != null) {
                // Store the bearingTo in the bearTo variable
                float bearTo = deviceLocation.bearingTo(targetLocation);
                distance = deviceLocation.distanceTo(targetLocation);
                targetTextView.setText("target : " + responseList.get(targetPositionInList).getTitle());
                distanceTextView.setText("distance : " + distance);

                //This is where we choose to point it
                float direction = bearTo - azimuth;
                if(direction < -180){
                    direction += 360;
                }
                if(direction > 180){
                    direction -= 360;
                }
                if(direction >= -30 && direction <= 30 && pitch >= (-25+angleToTarget) && pitch <= (25+angleToTarget)){
                    //positioning rect here.
                    float x = 0f;
                    float y = 0f;
                    float ratio = 0f;
                    float absoluteValue = Math.abs(direction);
                    float absoluteValuePitch = Math.abs(pitch);
                    ratio = 1 - (absoluteValue / 30);
                    float ratioPitch = 1 - (absoluteValuePitch / 25);
                    if (direction >= -30 && direction <= 0) {
                        y = ratio * (screenHeight / 2);
                    } else if (direction <= 30 && direction >= 0) {
                        ratio = 1 - ratio;
                        y = ratio * (screenHeight / 2) + (screenHeight / 2);
                    }
                    if(pitch >= (-25+angleToTarget) && pitch <= angleToTarget){
                        x = ratioPitch * (screenWidth/2);
                    } else if(pitch <= (25+angleToTarget) && pitch >= angleToTarget){
                        ratioPitch = 1 - ratioPitch;
                        x = ratioPitch * (screenWidth/2) + (screenWidth/2);
                    }
                    markers.get(responseList.get(targetPositionInList).getId()).setVisibility(View.VISIBLE);
                    layoutParams.leftMargin = (int) x;
                    layoutParams.topMargin = (int) y;
                    markers.get(responseList.get(targetPositionInList).getId()).setLayoutParams(layoutParams);
                } else {
                    markers.get(responseList.get(targetPositionInList).getId()).setVisibility(View.INVISIBLE);
                }
                navArrow.setRotation(direction+90);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mLastAccelerometerSet = false;
        mLastMagnetometerSet = false;
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v == markers.get(responseList.get(targetPositionInList).getId())){
            Toast.makeText(this, responseList.get(targetPositionInList).getTitle(), Toast.LENGTH_SHORT).show();
        }
    }

    private void calculateAngleBetweenDeviceAndTarget(){
        if(deviceAltitude <= targetAltitude){
            double differenceHeight = targetAltitude - deviceAltitude;
            angleToTarget = (float)Math.toDegrees(Math.atan(differenceHeight/distance));
            angleToTarget = -angleToTarget;
        } else if(deviceAltitude > targetAltitude){
            double differenceHeight = deviceAltitude - targetAltitude;
            angleToTarget = (float)Math.toDegrees(Math.atan(differenceHeight/distance));
        }
    }

    private class loadARContent extends AsyncTask<String, Void, String>{
        private ProgressDialog dialog;
        private OverlayActivity overlayActivity;
        private Context context;

        public loadARContent(OverlayActivity activity) {
            this.overlayActivity = activity;
            context = activity;
            dialog = new ProgressDialog(context);
        }

        @Override
        protected void onPostExecute(String s) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if(s.equals("success")){
                Toast.makeText(context, "Content retrieved", Toast.LENGTH_SHORT);
            }
        }

        protected void onPreExecute() {
            this.dialog.setMessage("Retrieving content...");
            this.dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            overlayActivity.targetLocation = responseList.get(targetPositionInList).getLocation();
            return "success";
        }
    }

    private class loadTargetElevation extends AsyncTask<String, Void, String>{
        private ProgressDialog dialog;
        private OverlayActivity overlayActivity;
        private Context context;
        private RequestQueue mRequestQueue;
        private String url = "http://maps.googleapis.com/maps/api/elevation/json?locations=";
        private boolean success = false;

        public loadTargetElevation(OverlayActivity activity) {
            this.overlayActivity = activity;
            context = activity;
            dialog = new ProgressDialog(context);
        }

        protected void onPreExecute() {
            this.dialog.setMessage("Retrieving content...");
            this.dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            url += String.valueOf(responseList.get(targetPositionInList).getLatitude())
                    + ","
                    + String.valueOf(responseList.get(targetPositionInList).getLongitude())
                    + "&sensor=true";
            Log.d("url", url);
            mRequestQueue = Volley.newRequestQueue(overlayActivity);
            GsonRequest<ElevationResponseContainer> myReq = new GsonRequest<ElevationResponseContainer>(
                    Request.Method.GET,
                    url,
                    ElevationResponseContainer.class,
                    new com.android.volley.Response.Listener<ElevationResponseContainer>() {
                        @Override
                        public void onResponse(ElevationResponseContainer response) {
                            overlayActivity.targetAltitude = response.getResults().get(0).getElevation();
                            overlayActivity.altitudeTextView.setText("target altitude : "+overlayActivity.targetAltitude);
                            success = true;
                        }
                    },
                    new com.android.volley.Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            success = false;
                        }
                    }
            );
            mRequestQueue.add(myReq);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if(success){
                Toast.makeText(context, "Target altitude retrieved", Toast.LENGTH_SHORT);
            } else {
                Toast.makeText(context, "failed retrieving data", Toast.LENGTH_SHORT);
            }
        }
    }

    private class loadDeviceElevation extends AsyncTask<String, Void, String>{
        private ProgressDialog dialog;
        private OverlayActivity overlayActivity;
        private Context context;
        private RequestQueue mRequestQueue;
        private String url = "http://maps.googleapis.com/maps/api/elevation/json?locations=";
        private boolean success = false;

        public loadDeviceElevation(OverlayActivity activity) {
            this.overlayActivity = activity;
            context = activity;
            dialog = new ProgressDialog(context);
        }

        protected void onPreExecute() {
            this.dialog.setMessage("Retrieving content...");
            this.dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            url += String.valueOf(deviceLocation.getLatitude())
                    + ","
                    + String.valueOf(deviceLocation.getLongitude())
                    + "&sensor=true";
            Log.d("url", url);
            mRequestQueue = Volley.newRequestQueue(overlayActivity);
            GsonRequest<ElevationResponseContainer> myReq = new GsonRequest<ElevationResponseContainer>(
                    Request.Method.GET,
                    url,
                    ElevationResponseContainer.class,
                    new com.android.volley.Response.Listener<ElevationResponseContainer>() {
                        @Override
                        public void onResponse(ElevationResponseContainer response) {
                            overlayActivity.deviceAltitude = response.getResults().get(0).getElevation();
                            overlayActivity.calculateAngleBetweenDeviceAndTarget();
                            success = true;
                        }
                    },
                    new com.android.volley.Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            success = false;
                        }
                    }
            );
            mRequestQueue.add(myReq);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if(success){
                Toast.makeText(context, "Target altitude retrieved", Toast.LENGTH_SHORT);
            } else {
                Toast.makeText(context, "failed retrieving data", Toast.LENGTH_SHORT);
            }
        }
    }
}
