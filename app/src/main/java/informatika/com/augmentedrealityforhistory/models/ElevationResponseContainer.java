package informatika.com.augmentedrealityforhistory.models;

import java.util.ArrayList;

/**
 * Created by USER on 7/10/2016.
 */
public class ElevationResponseContainer {
    ArrayList<ElevationResponseResult> results;
    String status;

    public ArrayList<ElevationResponseResult> getResults() {
        return results;
    }

    public void setResults(ArrayList<ElevationResponseResult> results) {
        this.results = results;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
