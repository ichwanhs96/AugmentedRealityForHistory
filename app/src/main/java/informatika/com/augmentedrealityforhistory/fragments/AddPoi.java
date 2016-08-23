package informatika.com.augmentedrealityforhistory.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import informatika.com.augmentedrealityforhistory.R;
import informatika.com.augmentedrealityforhistory.activities.MainMenuActivity;
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
    private EditText editTextPoiImageLink;
    private EditText editTextPoiRadius;
    private Button buttonPoiOpenMap;
    private Button buttonPoiSubmit;

    private ProgressDialog dialog;

    private RequestQueue mRequestQueue;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_poi, container, false);
        editTextPoiName = (EditText) v.findViewById(R.id.editTextPoiName);
        editTextPoiLatitude = (EditText) v.findViewById(R.id.editTextPoiLatitude);
        editTextPoiLongitude = (EditText) v.findViewById(R.id.editTextPoiLongitude);
        editTextPoiImageLink = (EditText) v.findViewById(R.id.editTextPoiImageLink);
        buttonPoiOpenMap = (Button) v.findViewById(R.id.buttonPoiOpenMap);
        buttonPoiSubmit = (Button) v.findViewById(R.id.buttonPoiSubmit);
        editTextPoiRadius = (EditText) v.findViewById(R.id.editTextPoiRadius);
        editTextPoiLatitude.setEnabled(false);
        editTextPoiLongitude.setEnabled(false);
        editTextPoiRadius.setEnabled(false);

        dialog = new ProgressDialog(getActivity());
        dialog.setCanceledOnTouchOutside(false);

        buttonPoiOpenMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPoiMapActivity();
            }
        });

        buttonPoiSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.setMessage("Adding Poi...");
                dialog.show();
                submitPoi();
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
            editTextPoiRadius.setText(""+(int)ResourceClass.radius);
            ResourceClass.poiLatLng = null;
            ResourceClass.radius = 100f;
        }
    }

    private void openPoiMapActivity(){
        Intent intent = new Intent(getActivity(), PoiMapActivity.class);
        startActivity(intent);
    }

    private void submitPoi(){
        String url = ResourceClass.url+"PointOfInterests";

        if(editTextPoiName.getText().toString().matches("")){
            Toast.makeText(getActivity(), "nama konten tidak boleh kosong", Toast.LENGTH_SHORT).show();
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            return;
        }
        if(editTextPoiLongitude.getText().toString().matches("")
                && editTextPoiLatitude.getText().toString().matches("")){
            Toast.makeText(getActivity(), "lokasi tidak boleh kosong", Toast.LENGTH_SHORT).show();
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            return;
        }

        mRequestQueue = Volley.newRequestQueue(getActivity());
        JSONObject jsonObject = new JSONObject();
        try {
            JSONObject jsonLocation = new JSONObject();
            jsonLocation.put("lat", editTextPoiLatitude.getText());
            jsonLocation.put("lng", editTextPoiLongitude.getText());
            jsonObject.put("location", jsonLocation);
            jsonObject.put("title", editTextPoiName.getText());
            jsonObject.put("radius", editTextPoiRadius.getText());
            if(!editTextPoiImageLink.getText().toString().matches("")) {
                jsonObject.put("imageLink", editTextPoiImageLink.getText());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest myReq = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        Toast.makeText(getActivity(),"berhasil membuat point of interest", Toast.LENGTH_SHORT).show();
                        ResourceClass.poiLatLng = null;
                        ((MainMenuActivity)getActivity()).goToMainFragment();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(),"error on adding poi", Toast.LENGTH_SHORT).show();
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", ResourceClass.auth_key);
                return headers;
            }
        };
        myReq.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mRequestQueue.add(myReq);
    }
}
