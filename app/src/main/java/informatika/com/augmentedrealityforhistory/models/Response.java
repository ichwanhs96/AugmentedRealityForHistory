package informatika.com.augmentedrealityforhistory.models;

import android.location.Location;

/**
 * Created by Ichwan Haryo Sembodo on 29/06/2016.
 */
public class Response {
    private String id;
    private double latitude;
    private double longitude;
    private String title;
    private String description;
    private String imageLink;
    private String videoLink;

    public Response(String id){
        this.id = id;
    }

    public Response(String id, double latitude, double longitude){
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Response(String id, double latitude, double longitude, String title, String description, String imageLink){
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        this.description = description;
        this.imageLink = imageLink;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(long latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(long longitude) {
        this.longitude = longitude;
    }

    public Location getLocation() {
        Location loc = new Location("");
        loc.setLatitude(latitude);
        loc.setLongitude(longitude);
        return loc;
    }
}
