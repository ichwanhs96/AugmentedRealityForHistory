package informatika.com.augmentedrealityforhistory.fragments;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import informatika.com.augmentedrealityforhistory.R;
import informatika.com.augmentedrealityforhistory.activities.ImageLocationMapActivity;
import informatika.com.augmentedrealityforhistory.activities.OverlayActivity;
import informatika.com.augmentedrealityforhistory.resources.ResourceClass;

/**
 * Created by USER on 8/6/2016.
 */
public class ARConfigurationDialog extends DialogFragment {
    private TextView textViewRadiusPOI;
    private TextView textViewDistancePOI;
    private SeekBar seekBarRadiusPOI;
    private EditText editTextMaximumRadiusPOI;
    private TextView textViewRadiusImage;
    private TextView textViewDistanceImage;
    private SeekBar seekBarRadiusImage;
    private EditText editTextMaximumRadiusImage;
    private TextView textViewMaximumRadiusImage;
    private Button buttonARConfigurationConfirm;
    private Button buttonShowImageLocation;
    private int maximumRadiusPOI = 0;
    private int radiusPOI = 0;
    private int maximumRadiusImage = 0;
    private int radiusImage = 0;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_arconfiguration_dialog, container ,false);

        maximumRadiusPOI = ((OverlayActivity)getActivity()).show_poi_distance_min;

        textViewRadiusPOI = (TextView) v.findViewById(R.id.textViewRadiusPOI);
        textViewDistancePOI = (TextView) v.findViewById(R.id.textViewDistancePOI);
        seekBarRadiusPOI = (SeekBar) v.findViewById(R.id.seekBarRadiusPOI);
        editTextMaximumRadiusPOI = (EditText) v.findViewById(R.id.editTextMaximumRadiusPOI);
        buttonARConfigurationConfirm = (Button) v.findViewById(R.id.buttonARConfigurationConfirm);

        maximumRadiusPOI = Integer.valueOf(editTextMaximumRadiusPOI.getText().toString());

        textViewRadiusPOI.setText("Radius POI : "+maximumRadiusPOI);
        textViewDistancePOI.setText("Jarak POI : "+((OverlayActivity)getActivity()).distance);
        seekBarRadiusPOI.setProgress(maximumRadiusPOI/100);

        if(((OverlayActivity)getActivity()).mode.matches("SHOW_IMAGE_PLACE_TAKEN")){
            textViewRadiusImage = (TextView) v.findViewById(R.id.textViewDistancePOI);
            textViewDistanceImage = (TextView) v.findViewById(R.id.textViewDistanceImage);
            seekBarRadiusImage = (SeekBar) v.findViewById(R.id.seekBarRadiusImage);
            editTextMaximumRadiusImage = (EditText) v.findViewById(R.id.editTextMaximumRadiusImage);
            textViewMaximumRadiusImage = (TextView) v.findViewById(R.id.textViewMaximumRadiusImage);
            buttonShowImageLocation = (Button) v.findViewById(R.id.buttonShowImageLocation);
            maximumRadiusImage = Integer.valueOf(editTextMaximumRadiusImage.getText().toString());

            textViewRadiusImage.setVisibility(View.VISIBLE);
            maximumRadiusImage = ((OverlayActivity)getActivity()).show_image_place_taken_thresold;
            textViewRadiusImage.setText("Radius Lokasi Gambar : "+maximumRadiusImage);

            if(((OverlayActivity)getActivity()).imagePlaceTakenLocation != null){
                float distanceToImageLocation = ResourceClass.deviceLocation.distanceTo(((OverlayActivity)getActivity()).imagePlaceTakenLocation);

                textViewDistanceImage.setText("Jarak Lokasi Gambar : "+distanceToImageLocation);
                textViewDistanceImage.setVisibility(View.VISIBLE);
            }

            seekBarRadiusImage.setVisibility(View.VISIBLE);
            seekBarRadiusImage.setProgress(maximumRadiusImage/100);
            textViewMaximumRadiusImage.setVisibility(View.VISIBLE);
            editTextMaximumRadiusImage.setVisibility(View.VISIBLE);
            buttonShowImageLocation.setVisibility(View.VISIBLE);

            editTextMaximumRadiusImage.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(!s.toString().matches("")) {
                        maximumRadiusImage = Integer.valueOf(s.toString());
                    }
                }
            });

            seekBarRadiusImage.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    radiusImage = (maximumRadiusImage/100) * progress;
                    textViewRadiusImage.setText("Radius Lokasi Gambar : "+radiusImage);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            buttonShowImageLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), ImageLocationMapActivity.class);
                    startActivity(intent);
                    //open map image location here
                }
            });
        }

        editTextMaximumRadiusPOI.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().matches("")) {
                    maximumRadiusPOI = Integer.valueOf(s.toString());
                }
            }
        });

        seekBarRadiusPOI.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radiusPOI = (maximumRadiusPOI/100) * progress;
                textViewRadiusPOI.setText("Radius POI : "+radiusPOI);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        buttonARConfigurationConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((OverlayActivity)getActivity()).show_image_place_taken_thresold = maximumRadiusImage;
                ((OverlayActivity)getActivity()).show_poi_distance_min = maximumRadiusPOI;
                dismiss();
            }
        });

        return v;
    }
}
