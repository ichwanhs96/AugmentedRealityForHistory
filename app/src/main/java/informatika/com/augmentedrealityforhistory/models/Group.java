package informatika.com.augmentedrealityforhistory.models;

/**
 * Created by USER on 7/18/2016.
 */
import java.util.ArrayList;
import java.util.List;

public class Group {

    public String string;
    public final List<String> children = new ArrayList<String>();

    public Group(String string) {
        this.string = string;
    }

}
