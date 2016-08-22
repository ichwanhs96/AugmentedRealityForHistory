package informatika.com.augmentedrealityforhistory.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import informatika.com.augmentedrealityforhistory.R;
import informatika.com.augmentedrealityforhistory.activities.MainMenuActivity;
import informatika.com.augmentedrealityforhistory.adapters.ExpandableListHistoryAdapter;
import informatika.com.augmentedrealityforhistory.models.ArrayWithId;
import informatika.com.augmentedrealityforhistory.models.Content;
import informatika.com.augmentedrealityforhistory.models.Group;
import informatika.com.augmentedrealityforhistory.models.History;
import informatika.com.augmentedrealityforhistory.models.ListHistoryResponseContainer;
import informatika.com.augmentedrealityforhistory.resources.ResourceClass;
import informatika.com.augmentedrealityforhistory.util.GsonRequest;

/**
 * Created by USER on 7/22/2016.
 */
public class ListHistory extends Fragment {
    private RequestQueue mRequestQueue;
    private List<History> histories;
    private ExpandableListView listView;
    private SparseArray<Group> groups = new SparseArray<Group>();
    private AppCompatActivity activity;
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_listhistory, container, false);
        histories = new ArrayList<>();
        listView = (ExpandableListView) v.findViewById(R.id.listView);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Retrieving histories...");
        progressDialog.show();
        loadHistories();
        createData();
        return v;
    }

    private void createData() {
        for(int i = 0; i < histories.size(); i++){
            ArrayWithId arrayWithId = new ArrayWithId();
            arrayWithId.setmId(histories.get(i).id);
            arrayWithId.setmText(histories.get(i).title);
            arrayWithId.setmDescription(histories.get(i).description);
            arrayWithId.setmIsTeacher(histories.get(i).isTeacher);
            if(histories.get(i).imageLink != null && !histories.get(i).imageLink.matches("")){
                arrayWithId.setmImageLink(histories.get(i).imageLink);
            }
            Group group = new Group(arrayWithId);
            for(Content content : histories.get(i).contents){
                ArrayWithId arrayWithId1 = new ArrayWithId();
                arrayWithId1.setmText(content.title);
                arrayWithId1.setmId(content.id);
                group.children.add(arrayWithId1);
            }
            groups.append(i, group);
        }
        ExpandableListHistoryAdapter adapter = new ExpandableListHistoryAdapter(getActivity(),
                groups);
        listView.setAdapter(adapter);
    }

    private void loadHistories(){
        String url = ResourceClass.url+"Histories/getHistories";
        mRequestQueue = Volley.newRequestQueue(getActivity());
        GsonRequest<ListHistoryResponseContainer> myReq = new GsonRequest<ListHistoryResponseContainer>(
                Request.Method.GET,
                url,
                ListHistoryResponseContainer.class,
                new com.android.volley.Response.Listener<ListHistoryResponseContainer>() {
                    @Override
                    public void onResponse(ListHistoryResponseContainer response) {
                        if(progressDialog.isShowing()){
                            progressDialog.dismiss();
                        }
                        histories = response.histories;
                        if(!ResourceClass.mapHistoryWithContent.isEmpty()){
                            ResourceClass.mapHistoryWithContent.clear();
                        }
                        for(History history : histories) {
                            ResourceClass.mapHistoryWithContent.put(history.id, history.contents);
                        }
                        createData();
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(progressDialog.isShowing()){
                            progressDialog.dismiss();
                        }
                        ((MainMenuActivity)getActivity()).clearSharedPref();
                        ((MainMenuActivity)getActivity()).nextLoginActivity();
                        Toast.makeText(activity, "histories cant be retrieved", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.activity = (AppCompatActivity) context;
    }
}
