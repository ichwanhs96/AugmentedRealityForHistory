package informatika.com.augmentedrealityforhistory.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
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
import android.widget.RelativeLayout;
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
    public Map<String, ImageView> markers;
    private DrawView drawView;
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
    private int targetPositionInList = -1;

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
        markers = new HashMap<>();

        responseList = new ArrayList<Response>();
        responseList.add(new Response("1", -6.891814, 107.610263));
        responseList.add(new Response("2", -6.891814, 107.610263));
        responseList.add(new Response("3", -6.891814, 107.610263));

        setContentView(R.layout.activity_overlay);

        //drawView = (DrawView) findViewById(R.id.drawView);
        //drawView.setOnTouchListener(this);

        overlayViewInsideRelativeLayout = (RelativeLayout) findViewById(R.id.overlayViewInsideRelativeLayout);
        layoutParams = new RelativeLayout.LayoutParams(50, 50);
        for(Response response :responseList){
            customRects.put(response.getId(),new CustomRect(-1,-1));
            ImageView iv = new ImageView(this);
            iv.setImageResource(R.drawable.marker);
            iv.setVisibility(View.INVISIBLE);
            iv.setLayoutParams(layoutParams);
            iv.setRotation(90);
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
            }
        }
        RelativeLayout.LayoutParams layoutParamsNavArrow = new RelativeLayout.LayoutParams(70, 93);
        navArrow = new ImageView(this);
        navArrow.setImageResource(R.drawable.arrow);
        navArrow.setVisibility(View.VISIBLE);
        layoutParamsNavArrow.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParamsNavArrow.addRule(RelativeLayout.ALIGN_PARENT_END);
        navArrow.setLayoutParams(layoutParamsNavArrow);
        overlayViewInsideRelativeLayout.addView(navArrow);
        //compassImage = (ImageView) findViewById(R.id.compassImage);
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
            SensorManager.getRotationMatrix(mTemporaryRotationMatrix, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.remapCoordinateSystem(mTemporaryRotationMatrix, SensorManager.AXIS_X,
                    SensorManager.AXIS_Z, mRotationMatrix);
            SensorManager.getOrientation(mRotationMatrix, mOrientation);

            azimuth = (float)Math.toDegrees(mOrientation[0]); // orientation contains: azimut, pitch and roll

            if(deviceLocation != null && targetLocation != null) {
                // Store the bearingTo in the bearTo variable
                float bearTo = deviceLocation.bearingTo(targetLocation);

                //This is where we choose to point it
                float direction = bearTo - azimuth;
                if(direction < -180){
                    direction += 360;
                }
                if(direction > 180){
                    direction -= 360;
                }
                if(direction >= -30 && direction <= 30){
                    //positioning rect here.
                    float x = 0f;
                    float y = 0f;
                    float ratio = 0f;
                    float absoluteValue = Math.abs(direction);
                    ratio = 1-(absoluteValue/30);
                    if(direction >= -45 && direction <= 0){
                        x = (screenWidth/2);
                        y = ratio*(screenHeight/2);
                    } else if(direction <= 45 && direction >= 0){
                        ratio = 1 - ratio;
                        x = (screenWidth/2);
                        y = ratio*(screenHeight/2) + (screenHeight/2);
                    }

                    markers.get(responseList.get(targetPositionInList).getId()).setVisibility(View.VISIBLE);
                    layoutParams.leftMargin = (int)x;
                    layoutParams.topMargin = (int)y;
                    markers.get(responseList.get(targetPositionInList).getId()).setLayoutParams(layoutParams);
                    Log.d("marker position", "x:"+markers.get(responseList.get(targetPositionInList).getId()).getX()+", y:"+markers.get(responseList.get(targetPositionInList).getId()).getY());
                    customRects.get(responseList.get(targetPositionInList).getId()).setX((int)x);
                    customRects.get(responseList.get(targetPositionInList).getId()).setY((int)y);
                    customRects.get(responseList.get(targetPositionInList).getId()).setRect();
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
            Log.d("device location", "lat :" + overlayActivity.deviceLocation.getLatitude() + ", long :"+ overlayActivity.deviceLocation.getLongitude());
            for(Response response : overlayActivity.responseList){
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
