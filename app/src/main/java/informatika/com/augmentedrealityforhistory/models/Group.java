package informatika.com.augmentedrealityforhistory.models;

/**
 * Created by USER on 7/18/2016.
 */
import java.util.ArrayList;
import java.util.List;

public class Group {

    public ArrayWithId arrayWithId;
    public final List<ArrayWithId> children = new ArrayList<ArrayWithId>();

    public Group(ArrayWithId arrayWithId) {
        this.arrayWithId = arrayWithId;
    }

}
