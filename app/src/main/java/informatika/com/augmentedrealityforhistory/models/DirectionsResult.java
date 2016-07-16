package informatika.com.augmentedrealityforhistory.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by USER on 7/14/2016.
 */
public class DirectionsResult {
    @SerializedName("routes")
    public List<Route> routes;
}
