package informatika.com.augmentedrealityforhistory.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import informatika.com.augmentedrealityforhistory.R;
import informatika.com.augmentedrealityforhistory.activities.PoiMapActivity;
import informatika.com.augmentedrealityforhistory.resources.ResourceClass;

/**
 * Created by USER on 7/23/2016.
 */
public class AddPoi extends Fragment {
    AppCompatActivity activity;
    private EditText editTextPoiName;
    private EditText editTextPoiLatitude;
    private EditText editTextPoiLongitude;
    private Button buttonPoiOpenMap;
    private Button buttonPoiSubmit;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_poi, container, false);
        editTextPoiName = (EditText) v.findViewById(R.id.editTextPoiName);
        editTextPoiLatitude = (EditText) v.findViewById(R.id.editTextPoiLatitude);
        editTextPoiLongitude = (EditText) v.findViewById(R.id.editTextPoiLongitude);
        buttonPoiOpenMap = (Button) v.findViewById(R.id.buttonPoiOpenMap);
        buttonPoiSubmit = (Button) v.findViewById(R.id.buttonPoiSubmit);

        buttonPoiOpenMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity, "open map clicked", Toast.LENGTH_SHORT).show();
                openPoiMapActivity();
            }
        });

        buttonPoiSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity, "submit button clicked", Toast.LENGTH_SHORT).show();
            }
        });

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (AppCompatActivity) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(ResourceClass.poiLatLng != null){
            editTextPoiLatitude.setText(""+ResourceClass.poiLatLng.latitude);
            editTextPoiLongitude.setText(""+ResourceClass.poiLatLng.longitude);
        }
    }

    private void openPoiMapActivity(){
        Intent intent = new Intent(getActivity(), PoiMapActivity.class);
        startActivity(intent);
    }
}
