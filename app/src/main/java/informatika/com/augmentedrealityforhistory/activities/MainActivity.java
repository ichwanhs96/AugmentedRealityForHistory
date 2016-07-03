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

    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];

    //view
    private TextView textView1;
    private TextView textView2;
    private TextView textView3;
    private ImageView imageView;

    //variable
    private float directionBefore = 0.0f;
    //button
    private Button button;

    private Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView1 = (TextView) findViewById(R.id.textView1);
        textView2 = (TextView) findViewById(R.id.textView2);
        textView3 = (TextView) findViewById(R.id.textView3);
        imageView = (ImageView) findViewById(R.id.imageView);
        button = (Button) findViewById(R.id.button);

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
                nextActivity();
            }
        });
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
            textView1.setText(String.format("Orientation: %f, %f, %f",
                    mOrientation[0], mOrientation[1], mOrientation[2]));

            azimuth = mOrientation[0]; // orientation contains: azimut, pitch and roll
            float azimuthInDegress = (float)Math.toDegrees(azimuth);
            if (azimuthInDegress < 0.0f) {
                azimuthInDegress += 360.0f;
            }

            //Set the field
            String bearingText = "N";

            if ( (360 >= azimuthInDegress && azimuthInDegress >= 337.5) || (0 <= azimuthInDegress && azimuthInDegress <= 22.5) ) bearingText = "N";
            else if (azimuthInDegress > 22.5 && azimuthInDegress < 67.5) bearingText = "NE";
            else if (azimuthInDegress >= 67.5 && azimuthInDegress <= 112.5) bearingText = "E";
            else if (azimuthInDegress > 112.5 && azimuthInDegress < 157.5) bearingText = "SE";
            else if (azimuthInDegress >= 157.5 && azimuthInDegress <= 202.5) bearingText = "S";
            else if (azimuthInDegress > 202.5 && azimuthInDegress < 247.5) bearingText = "SW";
            else if (azimuthInDegress >= 247.5 && azimuthInDegress <= 292.5) bearingText = "W";
            else if (azimuthInDegress > 292.5 && azimuthInDegress < 337.5) bearingText = "NW";
            else bearingText = "?";

            if(location != null) {
                Location destLoc = new Location("");
                destLoc.setLatitude(-6.891814);
                destLoc.setLongitude(107.610263);
                // Store the bearingTo in the bearTo variable
                float bearTo = location.bearingTo(destLoc);
                if (bearTo < 0.0f) {
                    bearTo += 360.0f;
                }
                //This is where we choose to point it
                float direction = bearTo - azimuthInDegress;
                imageView.setRotation(direction);
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

    public void nextActivity() {
        Intent intent = new Intent(this, OverlayActivity.class);
        startActivity(intent);
    }
}
