package informatika.com.augmentedrealityforhistory.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.List;
import java.util.Map;

import informatika.com.augmentedrealityforhistory.R;
import informatika.com.augmentedrealityforhistory.models.Content;
import informatika.com.augmentedrealityforhistory.models.DirectionsResult;
import informatika.com.augmentedrealityforhistory.resources.ResourceClass;
import informatika.com.augmentedrealityforhistory.util.GsonRequest;

/**
 * Created by USER on 7/13/2016.
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private RequestQueue mRequestQueue;
    private List<LatLng> latLngs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        loadDirection();
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
        String currentContentId = ResourceClass.currentContentId;
        LatLng targetLocation = new LatLng(ResourceClass.arcontents.get(currentContentId).pointOfInterest.location.getLat(), ResourceClass.arcontents.get(currentContentId).pointOfInterest.location.getLng());
        mMap.addMarker(new MarkerOptions().position(targetLocation).title(ResourceClass.arcontents.get(currentContentId).title));
        for(Map.Entry<String, Content> entry : ResourceClass.arcontents.entrySet()){
            if(currentContentId != entry.getKey()) {
                LatLng loc = new LatLng(entry.getValue().pointOfInterest.location.getLat(), entry.getValue().pointOfInterest.location.getLng());
                mMap.addMarker(new MarkerOptions().position(loc).title(entry.getValue().title));
            }
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(targetLocation));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(targetLocation, 14.0f));
    }

    private void loadDirection(){
        String url = "http://maps.google.com/maps/api/directions/json?origin=";
        url += ResourceClass.deviceLocation.getLatitude() + "," + ResourceClass.deviceLocation.getLongitude();
        url += "&destination=";
        url += ResourceClass.targetLocation.getLatitude() + "," + ResourceClass.targetLocation.getLongitude();
        System.out.println("direction maps : "+url);
        mRequestQueue = Volley.newRequestQueue(this);
        GsonRequest<DirectionsResult> myReq = new GsonRequest<DirectionsResult>(
                Request.Method.GET,
                url,
                DirectionsResult.class,
                new com.android.volley.Response.Listener<DirectionsResult>() {
                    @Override
                    public void onResponse(DirectionsResult response) {
                        Log.d("direction response", "direction response retrieved");
                        String encodedPoints = response.routes.get(0).overviewPolyLine.points;
                        latLngs = PolyUtil.decode(encodedPoints);
                        PolylineOptions polylineOptions = new PolylineOptions();
                        polylineOptions.addAll(latLngs);
                        polylineOptions.color(Color.RED);
                        polylineOptions.width(3);
                        mMap.addPolyline(polylineOptions);
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("direction response", "direction response failed");
                        Toast.makeText(MapsActivity.this, "direction cant be retrieved", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        myReq.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mRequestQueue.add(myReq);
    }
}
