package informatika.com.augmentedrealityforhistory.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.LinearLayout;
import android.widget.Spinner;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import informatika.com.augmentedrealityforhistory.R;
import informatika.com.augmentedrealityforhistory.activities.MainMenuActivity;
import informatika.com.augmentedrealityforhistory.models.ArrayWithId;
import informatika.com.augmentedrealityforhistory.models.Content;
import informatika.com.augmentedrealityforhistory.resources.ResourceClass;
import informatika.com.augmentedrealityforhistory.util.GsonRequest;

/**
 * Created by USER on 7/23/2016.
 */
public class AddHistory extends Fragment {
    private FloatingActionButton fabAddHistory;
    private FloatingActionButton fabRemoveHistory;
    private Button buttonHistorySubmit;
    private EditText editTextHistoryName;
    private EditText editTextHistoryDescription;
    private EditText editTextHistoryImageLink;
    private EditText editTextHistoryVideoLink;

    private int contentCounter = 0;

    private RequestQueue mRequestQueue;
    private List<Content> contentArray;
    private List<ArrayWithId> contents;
    private HashMap<Integer, ArrayWithId> selectedContent;

    private ProgressDialog dialog;

    private String historyCreatedId;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_history, container, false);

        //init progress dialog
        dialog = new ProgressDialog(getActivity());
        dialog.setMessage("Retrieving contents...");
        dialog.show();

        //init pois
        contentArray = new ArrayList<>();
        contents = new ArrayList<>();
        selectedContent = new HashMap<>();
        loadContents();

        buttonHistorySubmit = (Button) v.findViewById(R.id.buttonHistorySubmit);
        fabAddHistory = (FloatingActionButton) v.findViewById(R.id.fabAddHistory);
        fabRemoveHistory = (FloatingActionButton) v.findViewById(R.id.fabRemoveHistory);
        editTextHistoryName = (EditText) v.findViewById(R.id.editTextHistoryName);
        editTextHistoryDescription = (EditText) v.findViewById(R.id.editTextHistoryDescription);
        editTextHistoryImageLink = (EditText) v.findViewById(R.id.editTextHistoryImageLink);
        editTextHistoryVideoLink = (EditText) v.findViewById(R.id.editTextHistoryVideoLink);

        buttonHistorySubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //call add history
                submitHistory();
            }
        });

        fabAddHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View childView = inflater.inflate(R.layout.view_content_add_history, null);

                AutoCompleteTextView autoCompleteTextViewSelectContent = (AutoCompleteTextView) childView.findViewById(R.id.autoCompleteTextViewSelectContent);
                ArrayAdapter<ArrayWithId> adapter = new ArrayAdapter<ArrayWithId>(getActivity(), android.R.layout.simple_dropdown_item_1line, contents);
                autoCompleteTextViewSelectContent.setAdapter(adapter);
                autoCompleteTextViewSelectContent.setThreshold(0);
                autoCompleteTextViewSelectContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public int pos = contentCounter;
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        selectedContent.put(pos, (ArrayWithId) parent.getItemAtPosition(position));
                    }
                });

                ViewGroup viewGroup = (ViewGroup) getView().findViewById(R.id.linearLayoutAddHistory);
                childView.setId(contentCounter);
                viewGroup.addView(childView, contentCounter+9);
                contentCounter++;
                buttonHistorySubmit.setVisibility(View.VISIBLE);
            }
        });

        fabRemoveHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(contentCounter > 0){
                    ViewGroup viewGroup = (ViewGroup) getView().findViewById(R.id.linearLayoutAddHistory);
                    contentCounter--;
                    LinearLayout linearLayout = (LinearLayout) viewGroup.findViewById(contentCounter);
                    viewGroup.removeView(linearLayout);
                    if(selectedContent.containsKey(contentCounter)){
                        selectedContent.remove(contentCounter);
                    }
                }
                if(contentCounter == 0){
                    buttonHistorySubmit.setVisibility(View.INVISIBLE);
                }
            }
        });
        return v;
    }

    private void loadContents(){
        String url = ResourceClass.url+"Contents";
        mRequestQueue = Volley.newRequestQueue(getActivity());
        GsonRequest<Content[]> myReq = new GsonRequest<Content[]>(
                Request.Method.GET,
                url,
                Content[].class,
                new com.android.volley.Response.Listener<Content[]>() {
                    @Override
                    public void onResponse(Content[] response) {
                        contentArray = Arrays.asList(response);
                        for(Content result : contentArray){
                            ArrayWithId arrayWithId = new ArrayWithId();
                            arrayWithId.setmId(result.id);
                            arrayWithId.setmText(result.title);
                            contents.add(arrayWithId);
                        }
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), "content cant be retrieved", Toast.LENGTH_SHORT).show();
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

    private void submitHistory() {
        String url = ResourceClass.url+"Histories";
        if (editTextHistoryName.getText().toString().matches("")) {
            Toast.makeText(getActivity(), "nama tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        } else if (editTextHistoryDescription.getText().toString().matches("")) {
            Toast.makeText(getActivity(), "deskripsi tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        } else if (selectedContent == null) {
            Toast.makeText(getActivity(), "konten tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        mRequestQueue = Volley.newRequestQueue(getActivity());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("title", editTextHistoryName.getText());
            jsonObject.put("description", editTextHistoryDescription.getText());
            jsonObject.put("userForHistoryId", ResourceClass.user_id);
            if (!editTextHistoryImageLink.getText().toString().matches("")) {
                jsonObject.put("imageLink", editTextHistoryImageLink.getText());
            }
            if (!editTextHistoryVideoLink.getText().toString().matches("")) {
                jsonObject.put("videoLink", editTextHistoryVideoLink.getText());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest myReq = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            historyCreatedId = response.getString("id");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        createHistoryContent();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), "error on adding history", Toast.LENGTH_SHORT).show();
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

    private void createHistoryContent() {
        String url = ResourceClass.url+"HistoryContents";

        mRequestQueue = Volley.newRequestQueue(getActivity());
        JSONArray jsonArray = new JSONArray();
        try {
            for(Map.Entry<Integer, ArrayWithId> entry : selectedContent.entrySet()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("historyId", historyCreatedId);
                jsonObject.put("contentId", entry.getValue().getmId());
                jsonObject.put("position", entry.getKey());
                jsonArray.put(jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonArrayRequest myReq = new JsonArrayRequest(Request.Method.PUT, url, jsonArray,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        Toast.makeText(getActivity(), "berhasil membuat konten sejarah", Toast.LENGTH_SHORT).show();
                        ((MainMenuActivity)getActivity()).goToMainFragment();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), "error on adding history content", Toast.LENGTH_SHORT).show();
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
