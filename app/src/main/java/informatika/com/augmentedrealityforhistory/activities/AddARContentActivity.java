package informatika.com.augmentedrealityforhistory.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import informatika.com.augmentedrealityforhistory.R;
import informatika.com.augmentedrealityforhistory.resources.ResourceClass;

/**
 * Created by USER on 7/29/2016.
 */
public class AddARContentActivity extends AppCompatActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
        ,OnMapReadyCallback{

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private ImageView imageViewFromContent;
    private Button buttonAddARContentSetPosition;
    private RequestQueue mRequestQueue;
    private GoogleMap mMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ar_content);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        imageViewFromContent = (ImageView) findViewById(R.id.imageViewFromContent);
        buttonAddARContentSetPosition = (Button) findViewById(R.id.buttonAddARContentSetPosition);

        getImage(ResourceClass.imageMatchingUrl);

        buttonAddARContentSetPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location loc = new Location("");
                loc.setLatitude(ResourceClass.selectedPoi.location.getLat());
                loc.setLongitude(ResourceClass.selectedPoi.location.getLng());
                if(ResourceClass.deviceLocation.distanceTo(loc) <= ResourceClass.selectedPoi.radius){
                    ResourceClass.selectedPoi = null;
                    AddARContentActivity.this.finish();
                } else {
                    Toast.makeText(AddARContentActivity.this, "Posisi anda tidak masuk dalam radius POI", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.myLocationWithRadiusPoiMap);
        mapFragment.getMapAsync(this);
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
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 13));
        }
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(ResourceClass.auth_key == null) {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            ResourceClass.auth_key = sharedPref.getString(getString(R.string.AugmentedRealityForHistory_token), null);
            if (ResourceClass.auth_key == null) {
                nextLoginActivity();
            }
        }
    }

    private void nextLoginActivity(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
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
    protected void onDestroy() {
        ResourceClass.imageMatchingUrl = null;
        super.onDestroy();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        ResourceClass.deviceLocation = location;
    }

    private void getImage(String url) {
        mRequestQueue = Volley.newRequestQueue(this);
        ImageRequest request = new ImageRequest(url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                imageViewFromContent.setImageBitmap(response);
            }
        }, 0, 0, null,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("failed to retrieve image in add ar content activity");
                        Toast.makeText(AddARContentActivity.this, "Can't retrieve image", Toast.LENGTH_SHORT).show();
                        AddARContentActivity.this.finish();
                    }
                });
        request.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mRequestQueue.add(request);
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
        if(ResourceClass.selectedPoi != null) {
            mMap.addCircle(new CircleOptions().center(new LatLng(ResourceClass.selectedPoi.location.getLat(), ResourceClass.selectedPoi.location.getLng()))
                    .radius(ResourceClass.selectedPoi.radius)
                    .fillColor(0x5576C8F5)
                    .strokeColor(0x55008FDB)
                    .strokeWidth(7));
        } else {
            Toast.makeText(AddARContentActivity.this, "Mohon pilih POI", Toast.LENGTH_SHORT).show();
            this.finish();
        }
    }


}
