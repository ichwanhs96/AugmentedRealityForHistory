package informatika.com.augmentedrealityforhistory.fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import informatika.com.augmentedrealityforhistory.R;

/**
 * Created by USER on 7/11/2016.
 */
public class MarkerDialog extends DialogFragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_marker_dialog, container);
        getDialog().setTitle("DialogFragment Tutorial");
        return view;
    }
}
