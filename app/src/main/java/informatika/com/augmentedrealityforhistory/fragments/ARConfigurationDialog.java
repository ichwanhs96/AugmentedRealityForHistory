package informatika.com.augmentedrealityforhistory.fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import informatika.com.augmentedrealityforhistory.R;

/**
 * Created by USER on 8/6/2016.
 */
public class ARConfigurationDialog extends DialogFragment {
    private TextView textViewRadius;
    private TextView textViewDistance;
    private SeekBar seekBarRadius;
    private EditText editTextMaximumRadius;
    private int maximumRadius = 0;
    private int radius = 0;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_arconfiguration_dialog, container ,false);
        textViewRadius = (TextView) v.findViewById(R.id.textViewRadius);
        textViewDistance = (TextView) v.findViewById(R.id.textViewDistance);
        seekBarRadius = (SeekBar) v.findViewById(R.id.seekBarRadius);
        editTextMaximumRadius = (EditText) v.findViewById(R.id.editTextMaximumRadius);
        maximumRadius = Integer.valueOf(editTextMaximumRadius.getText().toString());

        editTextMaximumRadius.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                maximumRadius = Integer.valueOf(s.toString());
            }
        });

        seekBarRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radius = (maximumRadius/100) * progress;
                textViewRadius.setText("Radius : "+radius);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        return v;
    }
}
