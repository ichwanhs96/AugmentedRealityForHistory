package informatika.com.augmentedrealityforhistory.models;

/**
 * Created by USER on 7/24/2016.
 */
public class ArrayWithId {
    private String mText;
    private String mId;
    private String mDescription;

    public String getmDescription() {
        return mDescription;
    }

    public void setmDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public String getmText() {
        return mText;
    }

    public void setmText(String mText) {
        this.mText = mText;
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    @Override
    public String toString() {
        return mText;
    }
}
