package informatika.com.augmentedrealityforhistory.resources;

import android.location.Location;
import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Map;

import informatika.com.augmentedrealityforhistory.models.Response;

/**
 * Created by USER on 7/13/2016.
 */
public class ResourceClass {
    public static List<Response> responseList;
    public static Map<String, ImageView> markers;
    //device location
    public static Location deviceLocation;

    //target location
    public static Location targetLocation;
    public static int targetPositionInList = 0;
    public static LatLng poiLatLng;
}
