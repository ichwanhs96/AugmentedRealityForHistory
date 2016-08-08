package informatika.com.augmentedrealityforhistory.resources;

import android.location.Location;
import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import informatika.com.augmentedrealityforhistory.models.Content;
import informatika.com.augmentedrealityforhistory.models.PointOfInterest;
import informatika.com.augmentedrealityforhistory.models.Response;

/**
 * Created by USER on 7/13/2016.
 */
public class ResourceClass {
    public static String url = "http://192.168.1.133:3000/api/";

    //user token
    public static String auth_key;
    public static String user_id;
    public static boolean isTeacher;

    //variable for AR
    public static HashMap<String, List<Content>> mapHistoryWithContent = new HashMap<>();
    public static HashMap<String, Content> arcontents = new HashMap<>();

    public static HashMap<String, ImageView> markers = new HashMap<>();

    //device location
    public static Location deviceLocation;

    //target location
    public static Location targetLocation;
    public static int targetPositionInList = 0;
    public static int currentContentPosition = 0;
    public static String currentContentId;
    public static LatLng poiLatLng;

    //for add content image matching
    public static String imageMatchingUrl;

    public static Location imageLocation;
}
