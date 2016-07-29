package informatika.com.augmentedrealityforhistory.fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import informatika.com.augmentedrealityforhistory.R;
import informatika.com.augmentedrealityforhistory.resources.ResourceClass;

/**
 * Created by USER on 7/29/2016.
 */
public class ChangeAddressServer extends DialogFragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dialog_change_server_address, container, false);
        getDialog().setTitle("Change IP Address server");
        final EditText editTextChangeServerAddress = (EditText) v.findViewById(R.id.editTextChangeServerAddress);
        Button buttonChangeServerAddress = (Button) v.findViewById(R.id.buttonChangeServerAddress);

        buttonChangeServerAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editTextChangeServerAddress.getText().toString().matches("")){
                    Toast.makeText(getActivity(), "ip address tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    ResourceClass.url = editTextChangeServerAddress.getText().toString() + "/api/";
                    dismiss();
                }
            }
        });
        return v;
    }
}
