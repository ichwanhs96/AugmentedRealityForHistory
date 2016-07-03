package informatika.com.augmentedrealityforhistory.models;

/**
 * Created by Ichwan Haryo Sembodo on 29/06/2016.
 */
public class Response {
    private String id;
    private long latitude;
    private long longitude;
    private String title;
    private String description;
    private String imageLink;
    private String videoLink;

    public Response(String id){
        this.id = id;
    }

    public Response(String id, long latitude, long longitude, String title, String description, String imageLink){
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

    public long getLatitude() {
        return latitude;
    }

    public void setLatitude(long latitude) {
        this.latitude = latitude;
    }

    public long getLongitude() {
        return longitude;
    }

    public void setLongitude(long longitude) {
        this.longitude = longitude;
    }
}
