package informatika.com.augmentedrealityforhistory.activities;

import android.Manifest;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;

import java.util.Map;

import informatika.com.augmentedrealityforhistory.fragments.ARConfigurationDialog;
import informatika.com.augmentedrealityforhistory.fragments.ChooseContentDialog;
import informatika.com.augmentedrealityforhistory.fragments.MarkerDialog;
import informatika.com.augmentedrealityforhistory.models.Content;
import informatika.com.augmentedrealityforhistory.models.ElevationResponseContainer;
import informatika.com.augmentedrealityforhistory.R;
import informatika.com.augmentedrealityforhistory.resources.ResourceClass;
import informatika.com.augmentedrealityforhistory.util.GsonRequest;

/**
 * Created by Ichwan Haryo Sembodo on 07/06/2016.
 */

public class OverlayActivity extends AppCompatActivity implements SensorEventListener, LocationListener, View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final String SHOW_IMAGE_PLACE_TAKEN = "SHOW_IMAGE_PLACE_TAKEN";
    private final String SHOW_POI = "SHOW_POI";
    public int show_poi_distance_min = 100;
    private final int update_device_distance_threshold = 10;
    public int show_image_place_taken_threshold = 10;
    private float imageWidthtThreshold = 300f;
    private float imageHeightThreshold = 300f;
    private int markerWidth = 75;
    private int markerHeight = 75;

    private RelativeLayout overlayViewInsideRelativeLayout;
    private RelativeLayout.LayoutParams layoutParams;

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;

    //sensor
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

    //variable for sensor
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;

    private float[] mTemporaryRotationMatrix = new float[9];
    private float[] mRotationMatrix = new float[9];
    private float[] mOrientation = new float[3];

    //distance between device location to target location
    public double distance = 0f;

    public Bitmap bitmapForMarker;
    public Location imagePlaceTakenLocation;
    public String mode = SHOW_POI;
    private boolean isShowImagePlaceTaken = false;
    public boolean updateBitmapForMarker = false;
    public boolean updateRadius = false;

    //angle between target and device
    private float angleToTarget = 0f;

    //altitude target and device
    private double deviceAltitude = 0f;
    private double targetAltitude = 0f;

    //screen width and height
    private int screenWidth;
    private int screenHeight;

    //fragment manager
    private FragmentManager fragmentManager;

    //button
    private ImageButton nextContentButton;
    private Button buttonOverlayChooseContent;
    private ImageButton buttonARConfiguration;
    private ImageButton buttonMap;

    //image view
    private ImageView navArrow;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Mencari lokasi perangkat...");
        progressDialog.show();

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

        fragmentManager = getFragmentManager();

        setContentView(R.layout.activity_overlay);

        navArrow = (ImageView) findViewById(R.id.navArrow);
        nextContentButton = (ImageButton) findViewById(R.id.nextContentButton);
        buttonOverlayChooseContent = (Button) findViewById(R.id.buttonOverlayChooseContent);
        buttonARConfiguration = (ImageButton) findViewById(R.id.buttonARConfiguration);
        buttonMap = (ImageButton) findViewById(R.id.imageButtonMap);

        initTargetPosition();

        overlayViewInsideRelativeLayout = (RelativeLayout) findViewById(R.id.overlayViewInsideRelativeLayout);
        layoutParams = new RelativeLayout.LayoutParams(markerWidth, markerHeight);
        for (Map.Entry<String, Content> entry : ResourceClass.arcontents.entrySet()) {
            ImageView iv = new ImageView(this);
            iv.setImageResource(R.drawable.marker);
            iv.setVisibility(View.INVISIBLE);
            iv.setLayoutParams(layoutParams);
            iv.setOnClickListener(this);
            ResourceClass.markers.put(entry.getKey(), iv);
            overlayViewInsideRelativeLayout.addView(iv);
        }

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        initSensorListener();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        new loadTargetElevation(this).execute("");

        nextContentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResourceClass.markers.get(ResourceClass.currentContentId).setVisibility(View.INVISIBLE);
                if (ResourceClass.currentContentPosition < ResourceClass.arcontents.size()) {
                    ResourceClass.currentContentPosition += 1;
                    findNextContentId();
                }
            }
        });

        buttonOverlayChooseContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChooseContentDialog chooseContentDialog = new ChooseContentDialog();
                chooseContentDialog.show(fragmentManager, "fragment_dialog_choose_content");
            }
        });

        buttonARConfiguration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ARConfigurationDialog arConfigurationDialog = new ARConfigurationDialog();
                arConfigurationDialog.show(fragmentManager, "fragment_dialog_arconfiguration");
            }
        });

        buttonMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OverlayActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) return;
        //call get altitude for device and
        if(ResourceClass.deviceLocation != null && ResourceClass.deviceLocation.distanceTo(location) > update_device_distance_threshold){
            new loadDeviceElevation(this).execute("");
        }
        if (deviceAltitude != 0) {
            calculateAngleBetweenDeviceAndTarget();
        }
        if(location.distanceTo(ResourceClass.targetLocation) > show_poi_distance_min){
            mode = SHOW_POI;
        } else {
            mode = SHOW_IMAGE_PLACE_TAKEN;
        }

        ResourceClass.deviceLocation = location;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelerometer) {
            mLastAccelerometer = event.values;
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            mLastMagnetometer = event.values;
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mTemporaryRotationMatrix, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.remapCoordinateSystem(mTemporaryRotationMatrix, SensorManager.AXIS_X,
                    SensorManager.AXIS_Z, mRotationMatrix);
            SensorManager.getOrientation(mRotationMatrix, mOrientation);

            float azimuth = (float) Math.toDegrees(mOrientation[0]); // orientation contains: azimut, pitch and roll
            float pitch = (float) Math.toDegrees(mOrientation[1]);
            float roll = (float) Math.toDegrees(mOrientation[2]);

            if (ResourceClass.deviceLocation != null && ResourceClass.targetLocation != null) {
                if (mode.matches(SHOW_POI)) {
                    updateMarker(mode);
                    showAndMoveMarker(azimuth, pitch, roll);
                } else if (mode.matches(SHOW_IMAGE_PLACE_TAKEN)){
                    if(imagePlaceTakenLocation == null){
                        showAndMoveMarker(azimuth, pitch, roll);
                    }
                    else {
                        updateMarker(mode);
                        showAndMoveMarker(azimuth, pitch, roll);
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private void nextLoginActivity(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(ResourceClass.auth_key == null ||
                ResourceClass.user_id == null ||
                ResourceClass.user_email == null ||
                ResourceClass.user_name == null) {
            nextLoginActivity();
        }
        initSensorListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == ResourceClass.markers.get(ResourceClass.currentContentId)) {
            float distance = ResourceClass.deviceLocation.distanceTo(ResourceClass.targetLocation);
            if(distance < show_poi_distance_min){
                MarkerDialog markerDialog = new MarkerDialog();
                markerDialog.show(fragmentManager, "fragment_marker_dialog");
                mode = SHOW_IMAGE_PLACE_TAKEN;
            } else {
                Toast.makeText(this, "too far away from AR content", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        clearOverlayData();
        super.onDestroy();
    }

    private void showAndMoveMarker(float azimuth, float pitch, float roll){
        // Store the bearingTo in the bearTo variable
//        System.out.println("device location");
//        System.out.println("lat : "+ResourceClass.deviceLocation.getLatitude()+", lng : "+ResourceClass.deviceLocation.getLongitude());
        float bearTo = ResourceClass.deviceLocation.bearingTo(ResourceClass.targetLocation);
        distance = ResourceClass.deviceLocation.distanceTo(ResourceClass.targetLocation);

        //This is where we choose to point it
        float direction = bearTo - azimuth;
        if (direction < -180) {
            direction += 360;
        }
        if (direction > 180) {
            direction -= 360;
        }
        if (direction >= -30 && direction <= 30 && pitch >= (-25 + angleToTarget) && pitch <= (25 + angleToTarget)) {
            //positioning rect here.
            float x = 0f;
            float y = 0f;
            float ratio = 0f;
            float absoluteValue = Math.abs(direction);
            float absoluteValuePitch = Math.abs(pitch);
            ratio = 1 - (absoluteValue / 30);
            float ratioPitch = 1 - (absoluteValuePitch / 25);
            if (direction >= -30 && direction <= 0) {
                x = ratio * (screenWidth / 2);
                //y = ratio * (screenHeight / 2);
            } else if (direction <= 30 && direction >= 0) {
                ratio = 1 - ratio;
                x = ratio * (screenWidth / 2) + (screenWidth / 2);
                //y = ratio * (screenHeight / 2) + (screenHeight / 2);
            }
            if (pitch >= (-25 + angleToTarget) && pitch <= angleToTarget) {
                ratioPitch = 1 - ratioPitch;
                //x = ratioPitch * (screenWidth/2) + (screenWidth/2);
                y = ratioPitch * (screenHeight / 2) + (screenHeight / 2);
            } else if (pitch <= (25 + angleToTarget) && pitch >= angleToTarget) {
                //x = ratioPitch * (screenWidth/2);
                y = ratioPitch * (screenHeight / 2);
            }
            ResourceClass.markers.get(ResourceClass.currentContentId).setVisibility(View.VISIBLE);
            float absX = Math.abs(ResourceClass.markers.get(ResourceClass.currentContentId).getX() - x);
            float absY = Math.abs(ResourceClass.markers.get(ResourceClass.currentContentId).getY() - y);
            if(absX > 25 && absY > 25){
                layoutParams.leftMargin = (int) x;
                layoutParams.topMargin = (int) y;
                ResourceClass.markers.get(ResourceClass.currentContentId).setLayoutParams(layoutParams);
            }
        } else {
            ResourceClass.markers.get(ResourceClass.currentContentId).setVisibility(View.INVISIBLE);
        }
        navArrow.setRotation(direction);
    }

    private void updateMarker(String mode){
        switch (mode){
            case SHOW_IMAGE_PLACE_TAKEN : {
                //buttonARConfiguration.setVisibility(View.VISIBLE);
                if(!isShowImagePlaceTaken) {
                    if(ResourceClass.deviceLocation.distanceTo(imagePlaceTakenLocation) < show_image_place_taken_threshold) {
                        if (bitmapForMarker != null) {
                            float ratio = 0f;
                            if (bitmapForMarker.getHeight() >= bitmapForMarker.getWidth()) {
                                ratio = imageHeightThreshold / bitmapForMarker.getHeight();
                            } else {
                                ratio = imageWidthtThreshold / bitmapForMarker.getWidth();
                            }
                            int height = (int) (bitmapForMarker.getHeight() * ratio);
                            int width = (int) (bitmapForMarker.getWidth() * ratio);
                            System.out.println("ratio : " + ratio + ", heigth : " + height + ", width :" + width);
                            layoutParams = new RelativeLayout.LayoutParams(height, width);
                            ResourceClass.markers.get(ResourceClass.currentContentId).setImageBitmap(bitmapForMarker);
                            ResourceClass.markers.get(ResourceClass.currentContentId).setLayoutParams(layoutParams);
                        }
                    } else {
                        Toast.makeText(OverlayActivity.this, "Terlalu jauh dari tempat pengambilan gambar", Toast.LENGTH_SHORT).show();
                        layoutParams = new RelativeLayout.LayoutParams(50, 50);
                        ResourceClass.markers.get(ResourceClass.currentContentId).setImageResource(R.drawable.marker);
                        ResourceClass.markers.get(ResourceClass.currentContentId).setLayoutParams(layoutParams);
                    }
                    isShowImagePlaceTaken = true;
                } else if(updateBitmapForMarker){
                    updateBitmapForMarker = false;
                    if(ResourceClass.deviceLocation.distanceTo(imagePlaceTakenLocation) < show_image_place_taken_threshold) {
                        if (bitmapForMarker != null) {
                            float ratio = 0f;
                            if (bitmapForMarker.getHeight() >= bitmapForMarker.getWidth()) {
                                ratio = imageHeightThreshold / bitmapForMarker.getHeight();
                            } else {
                                ratio = imageHeightThreshold / bitmapForMarker.getWidth();
                            }
                            int height = (int) (bitmapForMarker.getHeight() * ratio);
                            int width = (int) (bitmapForMarker.getWidth() * ratio);
                            System.out.println("ratio : " + ratio + ", heigth : " + height + ", width :" + width);
                            layoutParams = new RelativeLayout.LayoutParams(height, width);
                            ResourceClass.markers.get(ResourceClass.currentContentId).setImageBitmap(bitmapForMarker);
                            ResourceClass.markers.get(ResourceClass.currentContentId).setLayoutParams(layoutParams);
                        }
                    } else {
                        Toast.makeText(OverlayActivity.this, "Terlalu jauh dari tempat pengambilan gambar", Toast.LENGTH_SHORT).show();
                    }
                } else if (updateRadius) {
                    updateRadius = false;
                    if(ResourceClass.deviceLocation.distanceTo(imagePlaceTakenLocation) < show_image_place_taken_threshold) {
                        if (bitmapForMarker != null) {
                            float ratio = 0f;
                            if (bitmapForMarker.getHeight() >= bitmapForMarker.getWidth()) {
                                ratio = imageHeightThreshold / bitmapForMarker.getHeight();
                            } else {
                                ratio = imageWidthtThreshold / bitmapForMarker.getWidth();
                            }
                            int height = (int) (bitmapForMarker.getHeight() * ratio);
                            int width = (int) (bitmapForMarker.getWidth() * ratio);
                            layoutParams = new RelativeLayout.LayoutParams(height, width);
                            ResourceClass.markers.get(ResourceClass.currentContentId).setImageBitmap(bitmapForMarker);
                            ResourceClass.markers.get(ResourceClass.currentContentId).setLayoutParams(layoutParams);
                        }
                    } else {
                        Toast.makeText(OverlayActivity.this, "Terlalu jauh dari tempat pengambilan gambar", Toast.LENGTH_SHORT).show();
                        layoutParams = new RelativeLayout.LayoutParams(markerWidth, markerHeight);
                        ResourceClass.markers.get(ResourceClass.currentContentId).setImageResource(R.drawable.marker);
                        ResourceClass.markers.get(ResourceClass.currentContentId).setLayoutParams(layoutParams);
                    }
                }
                break;
            }
            case SHOW_POI : {
                //buttonARConfiguration.setVisibility(View.GONE);
                if(isShowImagePlaceTaken) {
                    layoutParams = new RelativeLayout.LayoutParams(50, 50);
                    ResourceClass.markers.get(ResourceClass.currentContentId).setImageResource(R.drawable.marker);
                    ResourceClass.markers.get(ResourceClass.currentContentId).setLayoutParams(layoutParams);
                    isShowImagePlaceTaken = false;
                }
                break;
            }
            default: {
                Toast.makeText(this, "mode wrong", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void clearOverlayData(){
        ResourceClass.targetLocation = null;
        ResourceClass.targetPositionInList = 0;
        ResourceClass.currentContentId = null;
        ResourceClass.currentContentPosition = 0;
        ResourceClass.arcontents.clear();
        ResourceClass.markers.clear();
        ResourceClass.deviceLocation = null;
    }

    private void calculateAngleBetweenDeviceAndTarget() {
        if (deviceAltitude <= targetAltitude) {
            double differenceHeight = targetAltitude - deviceAltitude;
            angleToTarget = (float) Math.toDegrees(Math.atan(differenceHeight / distance));
            angleToTarget = -angleToTarget;
        } else if (deviceAltitude > targetAltitude) {
            double differenceHeight = deviceAltitude - targetAltitude;
            angleToTarget = (float) Math.toDegrees(Math.atan(differenceHeight / distance));
        }
    }

    private void initSensorListener() {
        mLastAccelerometerSet = false;
        mLastMagnetometerSet = false;
        mSensorManager.registerListener(this, mAccelerometer, 500000);
        mSensorManager.registerListener(this, mMagnetometer, 500000);
    }

    private void initTargetPosition(){
        for(Map.Entry<String, Content> entry : ResourceClass.arcontents.entrySet()){
            if(entry.getValue().position == ResourceClass.currentContentPosition){
                ResourceClass.currentContentId = entry.getKey();
                Location location = new Location("");
                location.setLatitude(entry.getValue().pointOfInterest.location.getLat());
                location.setLongitude(entry.getValue().pointOfInterest.location.getLng());
                ResourceClass.targetLocation = location;
                break;
            }
        }
    }

    private void findNextContentId(){
        String currentContentId = ResourceClass.currentContentId;
        for(Map.Entry<String, Content> entry : ResourceClass.arcontents.entrySet()){
            if(entry.getValue().position == ResourceClass.currentContentPosition){
                ResourceClass.currentContentId = entry.getKey();
                break;
            }
        }
        setNextTarget(currentContentId, ResourceClass.currentContentId);
    }

    public void setNextTarget(String prevId, String nextId){
        layoutParams = new RelativeLayout.LayoutParams(markerWidth, markerHeight);
        ResourceClass.markers.get(prevId).setVisibility(View.INVISIBLE);
        Location location = new Location("");
        location.setLatitude(ResourceClass.arcontents.get(nextId).pointOfInterest.location.getLat());
        location.setLongitude(ResourceClass.arcontents.get(nextId).pointOfInterest.location.getLng());
        ResourceClass.targetLocation = location;
        new loadTargetElevation(OverlayActivity.this).execute("");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
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
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation != null){
            ResourceClass.deviceLocation = mLastLocation;
            if(progressDialog.isShowing()) progressDialog.dismiss();
            new loadDeviceElevation(this).execute("");
        }
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class loadTargetElevation extends AsyncTask<String, Void, String>{
        private ProgressDialog dialog;
        private OverlayActivity overlayActivity;
        private Context context;
        private RequestQueue mRequestQueue;
        private String url = "https://maps.googleapis.com/maps/api/elevation/json?locations=";
        private boolean success = false;

        public loadTargetElevation(OverlayActivity activity) {
            this.overlayActivity = activity;
            context = activity;
            dialog = new ProgressDialog(context);
        }

        protected void onPreExecute() {
            this.dialog.setMessage("Retrieving target location altitude...");
            this.dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            url += String.valueOf(ResourceClass.arcontents.get(ResourceClass.currentContentId).pointOfInterest.location.getLat())
                    + ","
                    + String.valueOf(ResourceClass.arcontents.get(ResourceClass.currentContentId).pointOfInterest.location.getLng())
                    + "&sensor=true&key=" + ResourceClass.elevationApiKey;
            System.out.println("url load target elevation : "+url);
            mRequestQueue = Volley.newRequestQueue(overlayActivity);
            GsonRequest<ElevationResponseContainer> myReq = new GsonRequest<ElevationResponseContainer>(
                    Request.Method.GET,
                    url,
                    ElevationResponseContainer.class,
                    new com.android.volley.Response.Listener<ElevationResponseContainer>() {
                        @Override
                        public void onResponse(ElevationResponseContainer response) {
                            overlayActivity.targetAltitude = response.getResults().get(0).getElevation();
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
            myReq.setRetryPolicy(new DefaultRetryPolicy(5000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mRequestQueue.add(myReq);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if(success){
                //do nothing
            } else {
                //Toast.makeText(context, "failed retrieving target altitude", Toast.LENGTH_SHORT).show();
                //overlayActivity.finish();
            }
        }
    }

    private class loadDeviceElevation extends AsyncTask<String, Void, String>{
        private ProgressDialog dialog;
        private OverlayActivity overlayActivity;
        private Context context;
        private RequestQueue mRequestQueue;
        private String url = "https://maps.googleapis.com/maps/api/elevation/json?locations=";
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
            url += String.valueOf(ResourceClass.deviceLocation.getLatitude())
                    + ","
                    + String.valueOf(ResourceClass.deviceLocation.getLongitude())
                    + "&sensor=true&key=" + ResourceClass.elevationApiKey;
            Log.d("url", url);
            mRequestQueue = Volley.newRequestQueue(overlayActivity);
            GsonRequest<ElevationResponseContainer> myReq = new GsonRequest<ElevationResponseContainer>(
                    Request.Method.GET,
                    url,
                    ElevationResponseContainer.class,
                    new com.android.volley.Response.Listener<ElevationResponseContainer>() {
                        @Override
                        public void onResponse(ElevationResponseContainer response) {
                            System.out.println("device response retrieved");
                            overlayActivity.deviceAltitude = response.getResults().get(0).getElevation();
                            overlayActivity.calculateAngleBetweenDeviceAndTarget();
                            success = true;
                        }
                    },
                    new com.android.volley.Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("error", "error response");
                            success = false;
                        }
                    }
            );
            myReq.setRetryPolicy(new DefaultRetryPolicy(30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mRequestQueue.add(myReq);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if(success){
                //do nothing
            } else {
                //Toast.makeText(context, "retrieving device altitude failed", Toast.LENGTH_SHORT).show();
                //overlayActivity.finish();
            }
        }
    }
}
