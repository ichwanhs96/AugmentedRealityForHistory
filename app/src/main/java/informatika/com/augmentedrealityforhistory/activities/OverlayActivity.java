package informatika.com.augmentedrealityforhistory.activities;

import android.Manifest;
import android.app.Activity;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import informatika.com.augmentedrealityforhistory.models.CustomRect;
import informatika.com.augmentedrealityforhistory.models.Response;
import informatika.com.augmentedrealityforhistory.views.DrawThread;
import informatika.com.augmentedrealityforhistory.views.DrawView;
import informatika.com.augmentedrealityforhistory.R;

import static java.lang.Math.abs;

/**
 * Created by Ichwan Haryo Sembodo on 07/06/2016.
 */

public class OverlayActivity extends AppCompatActivity implements View.OnTouchListener, SensorEventListener, LocationListener {
    //map thread for drawing
    public Map<String, DrawThread> drawThreads;
    //map for positioning rect
    public Map<String, CustomRect> customRects;
    public List<Response> responseList;
    private DrawView drawView;

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

    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];

    //device location
    private Location deviceLocation;

    //target location
    private Location targetLocation;
    private int targetPositionInList;

    //screen width and height
    private int screenWidth;
    private int screenHeight;

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

        drawThreads = new HashMap<>();
        customRects = new HashMap<>();

        responseList = new ArrayList<Response>();
        responseList.add(new Response("1", -6.891814, 107.610263));
        responseList.add(new Response("2", -7.562740, 110.865483));
        responseList.add(new Response("3", -7.758048, 110.377622));
        for(Response response :responseList){
            customRects.put(response.getId(),new CustomRect(-1,-1));
        }

        setContentView(R.layout.activity_overlay);


        drawView = (DrawView) findViewById(R.id.drawView);
        drawView.setOnTouchListener(this);

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
            }
        }

        navArrow = (ImageView) findViewById(R.id.navArrow);
        compassImage = (ImageView) findViewById(R.id.compassImage);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Toast.makeText(this, "view clicked", Toast.LENGTH_SHORT).show();
        float x = event.getX();
        float y = event.getY();
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                for(Map.Entry<String, CustomRect> entry: customRects.entrySet()){
                    if(entry.getValue().getRect().contains((int)x, (int)y)){
                        Toast.makeText(this, "image clicked", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }
        }
        return false;
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location == null) return;
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
        float azimuth;

        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);

            //mOrientation[0] -> azimuth
            //mOrientation[1] -> pitch
            //mOrientation[2] -> roll

            azimuth = mOrientation[0]; // orientation contains: azimut, pitch and roll
            float azimuthInDegress = (float)Math.toDegrees(azimuth);
            if (azimuthInDegress < 0.0f) {
                azimuthInDegress += 360.0f;
            }

            compassImage.setRotation(-azimuthInDegress);

            if(deviceLocation != null && targetLocation != null) {
                // Store the bearingTo in the bearTo variable
                float bearTo = deviceLocation.bearingTo(targetLocation);
                if (bearTo < 0.0f) {
                    bearTo += 360.0f;
                }
                //This is where we choose to point it
                float direction = bearTo - azimuthInDegress;
                float directionForMarker = direction - 90;
                //marker still inside screen 45 degree till 135 degree
                if(directionForMarker >= -45 && directionForMarker <= 45){
                    //positioning rect here.
                    float x = 0f;
                    float y = 0f;
                    float ratio = 0f;
                    float absoluteValue = Math.abs((int)directionForMarker-0);
                    ratio = 1-(absoluteValue/45);
                    if(directionForMarker >= -45 && directionForMarker <= 0){
                        x = ratio*(screenWidth/2);
                        y = (screenHeight/2);
                    } else if(directionForMarker <= 45 && directionForMarker >= 0){
                        ratio = 1 - ratio;
                        x = ratio*(screenWidth/2) + (screenWidth/2);
                        y = (screenHeight/2);
                    }
                    customRects.get(responseList.get(targetPositionInList).getId()).setX((int)x);
                    customRects.get(responseList.get(targetPositionInList).getId()).setY((int)y);
                    customRects.get(responseList.get(targetPositionInList).getId()).setRect();
                }
                navArrow.setRotation(direction);
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
            float distanceInMeters = 0f;
            float tmp = 0f;
            int target = 0;
            int iterator = 0;
            for(Response response : overlayActivity.responseList){
                Log.d("device location", "lat :" + overlayActivity.deviceLocation.getLatitude() + ", long :"+ overlayActivity.deviceLocation.getLongitude());
                tmp = overlayActivity.deviceLocation.distanceTo(response.getLocation());
                if(distanceInMeters < tmp){
                    distanceInMeters = tmp;
                    target = iterator;
                }
                iterator++;
            }
            overlayActivity.targetLocation = overlayActivity.responseList.get(target).getLocation();
            overlayActivity.targetPositionInList = target;
            return "success";
        }
    }
}
