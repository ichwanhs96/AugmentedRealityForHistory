package informatika.com.augmentedrealityforhistory.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.List;

import informatika.com.augmentedrealityforhistory.R;
import informatika.com.augmentedrealityforhistory.models.DirectionsResult;
import informatika.com.augmentedrealityforhistory.models.Response;
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
        new loadDirectionGoogleAPI(this).execute("");
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
        int targetPositionInList = ResourceClass.targetPositionInList;
        BitmapDescriptor targetIcon = BitmapDescriptorFactory.fromResource(R.drawable.icon);
        LatLng markerLocation;
        for(Response response : ResourceClass.responseList){
            if(response.getId() != ResourceClass.responseList.get(targetPositionInList).getId()){
                markerLocation = new LatLng(response.getLatitude(), response.getLongitude());
                mMap.addMarker(new MarkerOptions().position(markerLocation).title("Marker in "+response.getTitle()));
            }
        }
        LatLng targetLocation = new LatLng(ResourceClass.responseList.get(targetPositionInList).getLatitude(), ResourceClass.responseList.get(targetPositionInList).getLongitude());
        LatLng deviceLocation = new LatLng(ResourceClass.deviceLocation.getLatitude(), ResourceClass.deviceLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(targetLocation).title("Marker in "+ResourceClass.responseList.get(targetPositionInList).getTitle()));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(targetLocation));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(targetLocation, 14.0f));
    }

    private class loadDirectionGoogleAPI extends AsyncTask<String, Void, String> {
        private ProgressDialog dialog;
        private MapsActivity mapsActivity;
        private Context context;
        private RequestQueue mRequestQueue;
        private boolean success = false;

        public loadDirectionGoogleAPI(MapsActivity activity) {
            this.mapsActivity = activity;
            context = activity;
            dialog = new ProgressDialog(context);
        }

        protected void onPreExecute() {
            this.dialog.setMessage("Retrieving directions...");
            this.dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String url = "http://maps.google.com/maps/api/directions/json?origin=";
            url += ResourceClass.deviceLocation.getLatitude() + "," + ResourceClass.deviceLocation.getLongitude();
            url += "&destination=";
            url += ResourceClass.targetLocation.getLatitude() + "," + ResourceClass.deviceLocation.getLongitude();
            mRequestQueue = Volley.newRequestQueue(mapsActivity);
            GsonRequest<DirectionsResult> myReq = new GsonRequest<DirectionsResult>(
                    Request.Method.GET,
                    url,
                    DirectionsResult.class,
                    new com.android.volley.Response.Listener<DirectionsResult>() {
                        @Override
                        public void onResponse(DirectionsResult response) {
                            success = true;
                            Log.d("direction response", "direction response retrieved");
                            String encodedPoints = response.routes.get(0).overviewPolyLine.points;
                            latLngs = PolyUtil.decode(encodedPoints);
                            PolylineOptions polylineOptions = new PolylineOptions();
                            polylineOptions.addAll(latLngs);
                            polylineOptions.color(Color.RED);
                            polylineOptions.width(2);
                            mMap.addPolyline(polylineOptions);
                        }
                    },
                    new com.android.volley.Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            success = false;
                            Log.d("direction response", "direction response failed");
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
                Toast.makeText(context, "direction retrieved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "direction cant be retrieved", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
