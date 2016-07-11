package informatika.com.augmentedrealityforhistory.models;

/**
 * Created by USER on 7/10/2016.
 */
public class ElevationResponseResult {
    Double elevation;
    ElevationResponseLocation location;
    Double resolution;

    public Double getElevation() {
        return elevation;
    }

    public void setElevation(Double elevation) {
        this.elevation = elevation;
    }

    public ElevationResponseLocation getLocation() {
        return location;
    }

    public void setLocation(ElevationResponseLocation location) {
        this.location = location;
    }

    public Double getResolution() {
        return resolution;
    }

    public void setResolution(Double resolution) {
        this.resolution = resolution;
    }
}
