package informatika.com.augmentedrealityforhistory.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import informatika.com.augmentedrealityforhistory.R;
import informatika.com.augmentedrealityforhistory.activities.AddARContentActivity;
import informatika.com.augmentedrealityforhistory.activities.MainMenuActivity;
import informatika.com.augmentedrealityforhistory.activities.PoiMapActivity;
import informatika.com.augmentedrealityforhistory.models.ArrayWithId;
import informatika.com.augmentedrealityforhistory.models.PointOfInterest;
import informatika.com.augmentedrealityforhistory.resources.ResourceClass;
import informatika.com.augmentedrealityforhistory.util.FileUtils;
import informatika.com.augmentedrealityforhistory.util.GsonRequest;

/**
 * Created by USER on 7/23/2016.
 */
public class AddContent extends Fragment {
    private EditText editTextContentName;
    private EditText editTextContentDescription;
    private EditText editTextContentVideo;
    private EditText editTextContentReference;
    private AutoCompleteTextView autoCompleteTextViewSelectPoi;
    private Button buttonContentSubmit;
    private Button buttonChooseFileText;
    private FloatingActionButton fabAddImageLinkUrl;
    private FloatingActionButton fabRemoveImageLinkUrl;
    private HashMap<Integer, View> addContentImageUrlViews;

    private int imageUrlCounter = 0;
    private int clickedImageUrlButtonPosition = 0;
    private int clickedUploadImageButtonPosition = 0;

    private RequestQueue mRequestQueue;
    private List<PointOfInterest> poiArray;
    private List<PointOfInterest> pois;

    private PointOfInterest selectedPoi;

    private ProgressDialog dialog;

    private String contentCreatedId;

    private int PICK_TEXT_RESULT_CODE = 1;
    private Uri filePath;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_content, container, false);

        //init progress dialog
        dialog = new ProgressDialog(getActivity());
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("Retrieving Poi...");
        dialog.show();

        //init pois
        poiArray = new ArrayList<>();
        pois = new ArrayList<>();
        addContentImageUrlViews = new HashMap<>();
        loadPointOfInterests();

        editTextContentName = (EditText) v.findViewById(R.id.editTextContentName);
        editTextContentDescription = (EditText) v.findViewById(R.id.editTextContentDescription);
        editTextContentVideo = (EditText) v.findViewById(R.id.editTextContentVideo);
        editTextContentReference = (EditText) v.findViewById(R.id.editTextContentReference);
        autoCompleteTextViewSelectPoi = (AutoCompleteTextView) v.findViewById(R.id.autoCompleteTextViewSelectPoi);
        buttonContentSubmit = (Button) v.findViewById(R.id.buttonContentSubmit);
        buttonChooseFileText = (Button) v.findViewById(R.id.buttonChooseFileText);
        fabAddImageLinkUrl = (FloatingActionButton) v.findViewById(R.id.fabAddImageLinkUrl);
        fabRemoveImageLinkUrl = (FloatingActionButton) v.findViewById(R.id.fabRemoveImageLinkUrl);

        autoCompleteTextViewSelectPoi.setThreshold(0);

        addPoisOnAutoCompleteTextView();

        buttonContentSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.setMessage("Adding Content...");
                dialog.show();
                submitPoi();
            }
        });

        buttonChooseFileText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileTextChooser();
            }
        });

        fabAddImageLinkUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View childView = inflater.inflate(R.layout.view_add_content_image_link, null);
                Button buttonAddContentImageLink = (Button) childView.findViewById(R.id.buttonContentImageLinkOpenMap);
                Button buttonContentUploadImage = (Button) childView.findViewById(R.id.buttonContentUploadImage);
                final EditText editTextImageUrl = (EditText) childView.findViewById(R.id.editTextAddContentImageLink);
                buttonAddContentImageLink.setOnClickListener(new View.OnClickListener() {
                    private int position = imageUrlCounter;
                    @Override
                    public void onClick(View v) {
                        if(!editTextImageUrl.getText().toString().matches("")) {
                            clickedImageUrlButtonPosition = position;
                            ResourceClass.imageMatchingUrl = editTextImageUrl.getText().toString();
                            ResourceClass.selectedPoi = selectedPoi;
                            openAddARContentActivity();
                        } else {
                            Toast.makeText(getActivity(), "Url gambar tidak boleh kosong", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                buttonContentUploadImage.setOnClickListener(new View.OnClickListener() {
                    private int position = imageUrlCounter;
                    @Override
                    public void onClick(View v) {
                        clickedUploadImageButtonPosition = position;
                        UploadImage uploadImage = new UploadImage();
                        uploadImage.setTargetFragment(AddContent.this, 0);
                        uploadImage.show(getFragmentManager(), "fragment_upload_image");
                    }
                });

                addContentImageUrlViews.put(imageUrlCounter, childView);
                ViewGroup viewGroup = (ViewGroup) getView().findViewById(R.id.linearLayoutAddContent);
                childView.setId(imageUrlCounter);
                viewGroup.addView(childView, imageUrlCounter+11);
                imageUrlCounter++;
            }
        });
        fabRemoveImageLinkUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageUrlCounter > 0){
                    ViewGroup viewGroup = (ViewGroup) getView().findViewById(R.id.linearLayoutAddContent);
                    imageUrlCounter--;
                    LinearLayout linearLayout = (LinearLayout) viewGroup.findViewById(imageUrlCounter);
                    viewGroup.removeView(linearLayout);
                    if(addContentImageUrlViews.containsKey(imageUrlCounter)){
                        addContentImageUrlViews.remove(imageUrlCounter);
                    }
                }
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!addContentImageUrlViews.isEmpty() && ResourceClass.deviceLocation != null) {
            View v = addContentImageUrlViews.get(clickedImageUrlButtonPosition);
            TextView textViewImageLat = (TextView) v.findViewById(R.id.textViewContentImageLinkLatitude);
            TextView textViewImageLng = (TextView) v.findViewById(R.id.textViewContentImageLinkLongitude);
            textViewImageLat.setText("" + ResourceClass.deviceLocation.getLatitude());
            textViewImageLng.setText("" + ResourceClass.deviceLocation.getLongitude());
            ResourceClass.deviceLocation = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_TEXT_RESULT_CODE && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
                readFile();
        }
    }

    public void changeEditTextImageUrl(String url){
        if(!addContentImageUrlViews.isEmpty() && url != null){
            View v = addContentImageUrlViews.get(clickedUploadImageButtonPosition);
            EditText editTextImageUrl = (EditText) v.findViewById(R.id.editTextAddContentImageLink);
            editTextImageUrl.setText(url);
            editTextImageUrl.setEnabled(false);
        }
    }

    private void addPoisOnAutoCompleteTextView(){
        ArrayAdapter<PointOfInterest> adapter = new ArrayAdapter<PointOfInterest>(getActivity(), android.R.layout.simple_dropdown_item_1line, pois);
        autoCompleteTextViewSelectPoi.setAdapter(adapter);
        autoCompleteTextViewSelectPoi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPoi = (PointOfInterest) parent.getItemAtPosition(position);
            }
        });
    }

    private void openAddARContentActivity(){
        Intent intent = new Intent(getActivity(), AddARContentActivity.class);
        startActivity(intent);
    }

    private void loadPointOfInterests(){
        String url = ResourceClass.url+"PointOfInterests";
        mRequestQueue = Volley.newRequestQueue(getActivity());
        GsonRequest<PointOfInterest[]> myReq = new GsonRequest<PointOfInterest[]>(
                Request.Method.GET,
                url,
                PointOfInterest[].class,
                new com.android.volley.Response.Listener<PointOfInterest[]>() {
                    @Override
                    public void onResponse(PointOfInterest[] response) {
                        poiArray = Arrays.asList(response);
                        for(PointOfInterest result : poiArray){
                            pois.add(result);
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
        String url = ResourceClass.url+"Contents";
        if (editTextContentName.getText().toString().matches("")) {
            Toast.makeText(getActivity(), "nama konten tidak boleh kosong", Toast.LENGTH_SHORT).show();
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            return;
        } else if (editTextContentDescription.getText().toString().matches("")) {
            Toast.makeText(getActivity(), "deskripsi konten tidak boleh kosong", Toast.LENGTH_SHORT).show();
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            return;
        } else if (selectedPoi == null) {
            Toast.makeText(getActivity(), "point of interest konten tidak boleh kosong", Toast.LENGTH_SHORT).show();
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            return;
        }

        if(!addContentImageUrlViews.isEmpty()) {
            for(Map.Entry<Integer, View> entry : addContentImageUrlViews.entrySet()){
                View v = entry.getValue();
                EditText editTextImageUrl = (EditText) v.findViewById(R.id.editTextAddContentImageLink);
                TextView textViewImageLat = (TextView) v.findViewById(R.id.textViewContentImageLinkLatitude);
                TextView textViewImageLng = (TextView) v.findViewById(R.id.textViewContentImageLinkLongitude);
                if(editTextImageUrl.getText().toString().matches("")){
                    Toast.makeText(getActivity(), "url gambar tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    return;
                }
                if(textViewImageLat.getText().toString().matches("") && textViewImageLng.getText().toString().matches("")){
                    Toast.makeText(getActivity(), "lokasi gambar tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    return;
                }
            }
        }

        mRequestQueue = Volley.newRequestQueue(getActivity());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("title", editTextContentName.getText());
            jsonObject.put("description", editTextContentDescription.getText());
            if (!editTextContentVideo.getText().toString().matches("")) {
                jsonObject.put("videoLink", editTextContentVideo.getText());
            }
            jsonObject.put("pointOfInterestId", selectedPoi.id);
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
                            addImage();
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

    private void addImage(){
        if(!dialog.isShowing()){
            dialog.setMessage("menambahkan gambar...");
            dialog.show();
        }
        String url = ResourceClass.url+"Images/addImages";
        mRequestQueue = Volley.newRequestQueue(getActivity());
        JSONArray jsonArray = new JSONArray();
        if(!addContentImageUrlViews.isEmpty()) {
            for(Map.Entry<Integer, View> entry : addContentImageUrlViews.entrySet()){
                View v = entry.getValue();
                EditText editTextImageUrl = (EditText) v.findViewById(R.id.editTextAddContentImageLink);
                TextView textViewImageLat = (TextView) v.findViewById(R.id.textViewContentImageLinkLatitude);
                TextView textViewImageLng = (TextView) v.findViewById(R.id.textViewContentImageLinkLongitude);
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("url", editTextImageUrl.getText());
                    JSONObject jsonObjectLoc = new JSONObject();
                    jsonObjectLoc.put("lat", textViewImageLat.getText());
                    jsonObjectLoc.put("lng", textViewImageLng.getText());
                    jsonObject.put("location", jsonObjectLoc);
                    jsonObject.put("contentId", contentCreatedId);
                    jsonArray.put(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if(dialog.isShowing()) dialog.dismiss();
            ((MainMenuActivity)getActivity()).goToMainFragment();
            Toast.makeText(getActivity(), "berhasil membuat konten", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonArrayRequest myReq = new JsonArrayRequest(Request.Method.POST, url, jsonArray,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        Toast.makeText(getActivity(), "berhasil membuat konten", Toast.LENGTH_SHORT).show();
                        ((MainMenuActivity)getActivity()).goToMainFragment();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), "error on image content", Toast.LENGTH_SHORT).show();
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
        String url = ResourceClass.url+"Contents/"+contentCreatedId+"/References";
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
                        addImage();
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

    private void showFileTextChooser() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Text"), PICK_TEXT_RESULT_CODE);
    }

    private void readFile(){
        if(filePath != null){
            try {
                File file = new File(FileUtils.getPath(getActivity(),filePath));
                BufferedReader br = new BufferedReader(new FileReader(file));
                String text = "";
                String line;
                while((line = br.readLine()) != null){
                    text += line;
                }
                editTextContentDescription.setEnabled(false);
                editTextContentDescription.setText(text);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
