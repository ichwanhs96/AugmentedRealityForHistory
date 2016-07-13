package informatika.com.augmentedrealityforhistory.activities;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import informatika.com.augmentedrealityforhistory.R;
import informatika.com.augmentedrealityforhistory.models.Response;
import informatika.com.augmentedrealityforhistory.resources.ResourceClass;

/**
 * Created by USER on 7/13/2016.
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

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
        mMap.addMarker(new MarkerOptions().position(targetLocation).title("Marker in "+ResourceClass.responseList.get(targetPositionInList).getTitle()));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(targetLocation));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(targetLocation, 14.0f));
    }
}
