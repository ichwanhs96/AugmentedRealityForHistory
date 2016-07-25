package informatika.com.augmentedrealityforhistory.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import informatika.com.augmentedrealityforhistory.R;
import informatika.com.augmentedrealityforhistory.activities.MainMenuActivity;
import informatika.com.augmentedrealityforhistory.models.ArrayWithId;
import informatika.com.augmentedrealityforhistory.models.PointOfInterest;
import informatika.com.augmentedrealityforhistory.resources.ResourceClass;
import informatika.com.augmentedrealityforhistory.util.GsonRequest;

/**
 * Created by USER on 7/23/2016.
 */
public class AddContent extends Fragment {
    private EditText editTextContentName;
    private EditText editTextContentDescription;
    private EditText editTextContentImage;
    private EditText editTextContentVideo;
    private EditText editTextContentReference;
    private AutoCompleteTextView autoCompleteTextViewSelectPoi;
    private Button buttonContentSubmit;
    private RequestQueue mRequestQueue;
    private List<PointOfInterest> poiArray;
    private List<ArrayWithId> pois;

    private ArrayWithId selectedPoi;

    private ProgressDialog dialog;

    private String contentCreatedId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_content, container, false);

        //init progress dialog
        dialog = new ProgressDialog(getActivity());
        dialog.setMessage("Retrieving Poi...");
        dialog.show();

        //init pois
        poiArray = new ArrayList<>();
        pois = new ArrayList<>();
        loadPointOfInterests();

        editTextContentName = (EditText) v.findViewById(R.id.editTextContentName);
        editTextContentDescription = (EditText) v.findViewById(R.id.editTextContentDescription);
        editTextContentImage = (EditText) v.findViewById(R.id.editTextContentImage);
        editTextContentVideo = (EditText) v.findViewById(R.id.editTextContentVideo);
        editTextContentReference = (EditText) v.findViewById(R.id.editTextContentReference);
        autoCompleteTextViewSelectPoi = (AutoCompleteTextView) v.findViewById(R.id.autoCompleteTextViewSelectPoi);
        buttonContentSubmit = (Button) v.findViewById(R.id.buttonContentSubmit);
        addPoisOnAutoCompleteTextView();
        buttonContentSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPoi();
            }
        });
        return v;
    }

    private void addPoisOnAutoCompleteTextView(){
        ArrayAdapter<ArrayWithId> adapter = new ArrayAdapter<ArrayWithId>(getActivity(), android.R.layout.simple_dropdown_item_1line, pois);
        autoCompleteTextViewSelectPoi.setAdapter(adapter);
        autoCompleteTextViewSelectPoi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPoi = (ArrayWithId) parent.getItemAtPosition(position);
            }
        });
    }

    private void loadPointOfInterests(){
        String url = "http://192.168.1.107:3000/api/PointOfInterests";
        mRequestQueue = Volley.newRequestQueue(getActivity());
        GsonRequest<PointOfInterest[]> myReq = new GsonRequest<PointOfInterest[]>(
                Request.Method.GET,
                url,
                PointOfInterest[].class,
                new com.android.volley.Response.Listener<PointOfInterest[]>() {
                    @Override
                    public void onResponse(PointOfInterest[] response) {
                        Toast.makeText(getActivity(), "pois retrieved", Toast.LENGTH_SHORT).show();
                        poiArray = Arrays.asList(response);
                        for(PointOfInterest result : poiArray){
                            ArrayWithId arrayWithId = new ArrayWithId();
                            arrayWithId.setmId(result.id);
                            arrayWithId.setmText(result.title);
                            pois.add(arrayWithId);
                        }
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), "point of interest cant be retrieved", Toast.LENGTH_SHORT).show();
                        System.out.println(error.getMessage());
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

    private void submitPoi() {
        String url = "http://192.168.1.107:3000/api/Contents";
        if (editTextContentName.getText().toString().matches("")) {
            Toast.makeText(getActivity(), "nama konten tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        } else if (editTextContentDescription.getText().toString().matches("")) {
            Toast.makeText(getActivity(), "deskripsi konten tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        } else if (selectedPoi == null) {
            Toast.makeText(getActivity(), "point of interest konten tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        mRequestQueue = Volley.newRequestQueue(getActivity());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("title", editTextContentName.getText());
            jsonObject.put("description", editTextContentDescription.getText());
            if (!editTextContentImage.getText().toString().matches("")) {
                jsonObject.put("imageLink", editTextContentImage.getText());
            }
            if (!editTextContentVideo.getText().toString().matches("")) {
                jsonObject.put("videoLink", editTextContentVideo.getText());
            }
            jsonObject.put("pointOfInterestId", selectedPoi.getmId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest myReq = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            contentCreatedId = response.getString("id");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        if(editTextContentReference.getText().toString().matches("")){
                            Toast.makeText(getActivity(), "adding content complete", Toast.LENGTH_SHORT).show();
                            ((MainMenuActivity)getActivity()).goToMainFragment();
                        } else {
                            addReference();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), "error on adding content", Toast.LENGTH_SHORT).show();
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }
                }
        ) {
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

    private void addReference(){
        String url = "http://192.168.1.107:3000/api/Contents/"+contentCreatedId+"/References";
        mRequestQueue = Volley.newRequestQueue(getActivity());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("url", editTextContentReference.getText());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest myReq = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        Toast.makeText(getActivity(), "adding content complete", Toast.LENGTH_SHORT).show();
                        ((MainMenuActivity)getActivity()).goToMainFragment();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), "error on adding content", Toast.LENGTH_SHORT).show();
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }
                }
        ) {
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
