package informatika.com.augmentedrealityforhistory.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.util.Arrays;
import java.util.List;

import informatika.com.augmentedrealityforhistory.R;
import informatika.com.augmentedrealityforhistory.adapters.ExpandableListHistoryAdapter;
import informatika.com.augmentedrealityforhistory.models.Group;
import informatika.com.augmentedrealityforhistory.models.History;
import informatika.com.augmentedrealityforhistory.util.GsonRequest;

/**
 * Created by USER on 7/22/2016.
 */
public class ListHistory extends Fragment {
    private RequestQueue mRequestQueue;
    SparseArray<Group> groups = new SparseArray<Group>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_listhistory, container, false);
        loadHistories();
        createData();
        ExpandableListView listView = (ExpandableListView) v.findViewById(R.id.listView);
        ExpandableListHistoryAdapter adapter = new ExpandableListHistoryAdapter(getActivity(),
                groups);
        listView.setAdapter(adapter);
        return v;
    }

    private void createData() {
        for (int j = 0; j < 5; j++) {
            Group group = new Group("Test " + j);
            for (int i = 0; i < 5; i++) {
                group.children.add("Sub Item" + i);
            }
            groups.append(j, group);
        }
    }

    private void loadHistories(){
        String url = "http://192.168.1.107:3000/api/Histories";
        System.out.println("backend : "+url);
        mRequestQueue = Volley.newRequestQueue(getActivity());
        GsonRequest<History[]> myReq = new GsonRequest<History[]>(
                Request.Method.GET,
                url,
                History[].class,
                new com.android.volley.Response.Listener<History[]>() {
                    @Override
                    public void onResponse(History[] response) {
                        Log.d("direction response", "direction response retrieved");
                        Toast.makeText(getActivity(), "histories retrieved", Toast.LENGTH_SHORT).show();
                        List<History> histories = Arrays.asList(response);
                        for(History result : histories){
                            System.out.println("title : "+result.getTitle());
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("direction response", "direction response failed");
                        Toast.makeText(getActivity(), "histories cant be retrieved", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        myReq.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mRequestQueue.add(myReq);
    }
}
