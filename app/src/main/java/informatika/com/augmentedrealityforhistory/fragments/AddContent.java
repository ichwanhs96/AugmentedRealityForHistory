package informatika.com.augmentedrealityforhistory.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import informatika.com.augmentedrealityforhistory.R;

/**
 * Created by USER on 7/23/2016.
 */
public class AddContent extends Fragment implements AdapterView.OnItemSelectedListener {
    private EditText editTextContentName;
    private EditText editTextContentDescription;
    private EditText editTextContentImage;
    private EditText editTextContentVideo;
    private Spinner spinnerChoosePoi;
    private Button buttonContentSubmit;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_content, container, false);
        editTextContentName = (EditText) v.findViewById(R.id.editTextContentName);
        editTextContentDescription = (EditText) v.findViewById(R.id.editTextContentDescription);
        editTextContentImage = (EditText) v.findViewById(R.id.editTextContentImage);
        editTextContentVideo = (EditText) v.findViewById(R.id.editTextContentVideo);
        spinnerChoosePoi = (Spinner) v.findViewById(R.id.spinnerChoosePoi);
        buttonContentSubmit = (Button) v.findViewById(R.id.buttonContentSubmit);
        addPoisOnSpinner();
        buttonContentSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "submit clicked", Toast.LENGTH_SHORT).show();
            }
        });
        return v;
    }

    private void addPoisOnSpinner(){
        List<String> poiArray = new ArrayList<String>();
        poiArray.add("itb");
        poiArray.add("ui");
        poiArray.add("ugm");
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, poiArray);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerChoosePoi.setAdapter(adapter);
        spinnerChoosePoi.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getActivity(), parent.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
