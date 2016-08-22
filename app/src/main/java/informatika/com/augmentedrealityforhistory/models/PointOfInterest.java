package informatika.com.augmentedrealityforhistory.models;

/**
 * Created by USER on 7/19/2016.
 */
public class PointOfInterest {
    public Location location;
    public String title;
    public String imageLink;
    public String id;
    public int radius;

    @Override
    public String toString() {
        return title;
    }
}
