package informatika.com.augmentedrealityforhistory.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import informatika.com.augmentedrealityforhistory.R;

public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationListener {
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private LocationManager mLocationManager;

    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;

    private float[] mTemporaryRotationMatrix = new float[9];
    private float[] mRotationMatrix = new float[9];
    private float[] mOrientation = new float[3];

    //view
    private TextView textView1;
    private TextView textView2;
    private TextView textView3;
    private ImageView imageView;
    private TextView azimuthTextView;
    private TextView rollTextView;
    private TextView pitchTextView;
    private TextView bearToTextView;

    //variable
    private float directionBefore = 0.0f;
    //button
    private Button button;
    private Button buttonListView;
    private Button buttonLogin;

    private Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView1 = (TextView) findViewById(R.id.orientationTextView);
        textView2 = (TextView) findViewById(R.id.textView2);
        textView3 = (TextView) findViewById(R.id.textView3);
        azimuthTextView = (TextView) findViewById(R.id.azimuthTextView);
        rollTextView = (TextView) findViewById(R.id.rollTextView);
        pitchTextView = (TextView) findViewById(R.id.pitchTextView);
        bearToTextView = (TextView) findViewById(R.id.bearToTextView);
        imageView = (ImageView) findViewById(R.id.imageView);
        button = (Button) findViewById(R.id.button);
        buttonListView = (Button) findViewById(R.id.buttonListView);
        buttonLogin = (Button) findViewById(R.id.buttonLogin);

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
            location = mLocationManager
                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                textView2.setText("lat : "+location.getLatitude()+", long : "+location.getLongitude());
            }
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextOverlayActivity();
            }
        });

        buttonListView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextMainMenuActivity();
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextLoginActivity();
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelerometer) {
            mLastAccelerometer = event.values.clone();
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            mLastMagnetometer = event.values.clone();
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mTemporaryRotationMatrix, null, mLastAccelerometer, mLastMagnetometer);
//            SensorManager.remapCoordinateSystem(mTemporaryRotationMatrix, SensorManager.AXIS_X,
//                    SensorManager.AXIS_Z, mRotationMatrix);
            //configureDeviceAngle();
            SensorManager.getOrientation(mTemporaryRotationMatrix, mOrientation);
            textView1.setText("a: "+mOrientation[0]+", p: "+mOrientation[1]+", r: "+mOrientation[2]);
            float azimuth = (float)((Math.toDegrees(mOrientation[0])+360)%360);
            float pitch = (float)((Math.toDegrees(mOrientation[1])+360)%360);
            float roll = (float)((Math.toDegrees(mOrientation[2])+360)%360);
            azimuthTextView.setText("azimuth : "+azimuth);
            pitchTextView.setText("pitch : "+pitch);
            rollTextView.setText("roll : "+roll);
            //Set the field
            String bearingText = "N";

            if ( (360 >= azimuth && azimuth >= 337.5) || (0 <= azimuth && azimuth <= 22.5) ) bearingText = "N";
            else if (azimuth > 22.5 && azimuth < 67.5) bearingText = "NE";
            else if (azimuth >= 67.5 && azimuth <= 112.5) bearingText = "E";
            else if (azimuth > 112.5 && azimuth < 157.5) bearingText = "SE";
            else if (azimuth >= 157.5 && azimuth <= 202.5) bearingText = "S";
            else if (azimuth > 202.5 && azimuth < 247.5) bearingText = "SW";
            else if (azimuth >= 247.5 && azimuth <= 292.5) bearingText = "W";
            else if (azimuth > 292.5 && azimuth < 337.5) bearingText = "NW";
            else bearingText = "?";
            imageView.setRotation(azimuth);

            if(location != null) {
                Location destLoc = new Location("");
                destLoc.setLatitude(-6.891814);
                destLoc.setLongitude(107.610263);
                // Store the bearingTo in the bearTo variable
                float bearTo = (location.bearingTo(destLoc)+360)%360;
                bearToTextView.setText("bear to : " + bearTo);
                //This is where we choose to point it
                float direction = bearTo - azimuth;
                //imageView.setRotation(direction);
            }
            textView3.setText(String.valueOf(bearingText));
        }
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
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if(location == null) return;
        textView2.setText("lat : "+location.getLatitude()+", long : "+location.getLongitude());
        this.location = location;
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

    public void nextOverlayActivity() {
        Intent intent = new Intent(this, OverlayActivity.class);
        startActivity(intent);
    }

    public void nextListViewActivity() {
        Intent intent = new Intent(this, ListHistoryActivity.class);
        startActivity(intent);
    }

    public void nextMainMenuActivity(){
        Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
    }

    public void nextLoginActivity(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void configureDeviceAngle() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0: // Portrait
                SensorManager.remapCoordinateSystem(mTemporaryRotationMatrix, SensorManager.AXIS_Z,
                        SensorManager.AXIS_Y, mRotationMatrix);
                break;
            case Surface.ROTATION_90: // Landscape
                SensorManager.remapCoordinateSystem(mTemporaryRotationMatrix, SensorManager.AXIS_Y,
                        SensorManager.AXIS_MINUS_Z, mRotationMatrix);
                break;
            case Surface.ROTATION_180: // Portrait
                SensorManager.remapCoordinateSystem(mTemporaryRotationMatrix, SensorManager.AXIS_MINUS_Z,
                        SensorManager.AXIS_MINUS_Y, mRotationMatrix);
                break;
            case Surface.ROTATION_270: // Landscape
                SensorManager.remapCoordinateSystem(mTemporaryRotationMatrix, SensorManager.AXIS_MINUS_Y,
                        SensorManager.AXIS_Z, mRotationMatrix);
                break;
        }
    }
}
