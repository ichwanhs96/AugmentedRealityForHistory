package informatika.com.augmentedrealityforhistory.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import informatika.com.augmentedrealityforhistory.R;
import informatika.com.augmentedrealityforhistory.resources.ResourceClass;

/**
 * Created by USER on 7/23/2016.
 */
public class PoiMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private GoogleMap mMap;
    private Button poiSetPositionButton;
    private Button buttonPoiMapSetRadius;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private boolean isSetRadius = false;
    private float radius = 100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi_maps);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        poiSetPositionButton = (Button) findViewById(R.id.buttonPoiMapSetPosition);
        poiSetPositionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ResourceClass.poiLatLng != null) {
                    ResourceClass.radius = radius;
                    PoiMapActivity.this.finish();
                } else {
                    Toast.makeText(PoiMapActivity.this, "Silahkan mentukan posisi poi terlebih dahulu", Toast.LENGTH_SHORT).show();
                }
            }
        });
        buttonPoiMapSetRadius = (Button) findViewById(R.id.buttonPoiMapSetRadius);
        buttonPoiMapSetRadius.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isSetRadius) {
                    isSetRadius = true;
                    buttonPoiMapSetRadius.setText("selesai set radius");
                } else {
                    isSetRadius = false;
                    buttonPoiMapSetRadius.setText("set radius");
                }
            }
        });
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.poiMap);
        mapFragment.getMapAsync(this);
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

    @Override
    public void onBackPressed() {
        if(ResourceClass.poiLatLng != null){
            super.onBackPressed();
        } else {
            Toast.makeText(PoiMapActivity.this, "Silahkan mentukan posisi poi terlebih dahulu", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
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
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if(!isSetRadius){
            ResourceClass.poiLatLng = latLng;
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng)
                    .title("lat : " + latLng.latitude + ", lng : " + latLng.longitude));
            mMap.addCircle(new CircleOptions().center(latLng)
                    .radius(radius)
                    .fillColor(0x5576C8F5)
                    .strokeColor(0x55008FDB)
                    .strokeWidth(7));
        } else {
            if(ResourceClass.poiLatLng == null){
                Toast.makeText(PoiMapActivity.this, "Silahkan menentukan posisi poi terlebih dahulu", Toast.LENGTH_SHORT).show();
                isSetRadius = false;
                buttonPoiMapSetRadius.setText("set radius");
            } else {
                Location location = new Location("");
                location.setLatitude(ResourceClass.poiLatLng.latitude);
                location.setLongitude(ResourceClass.poiLatLng.longitude);
                Location location2 = new Location("");
                location2.setLatitude(latLng.latitude);
                location2.setLongitude(latLng.longitude);
                radius = location.distanceTo(location2);
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(ResourceClass.poiLatLng)
                        .title("lat : " + latLng.latitude + ", lng : " + latLng.longitude));
                mMap.addCircle(new CircleOptions().center(ResourceClass.poiLatLng)
                        .radius(radius)
                        .fillColor(0x5576C8F5)
                        .strokeColor(0x55008FDB)
                        .strokeWidth(7));
            }
        }
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
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            ResourceClass.deviceLocation = mLastLocation;
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 13));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
