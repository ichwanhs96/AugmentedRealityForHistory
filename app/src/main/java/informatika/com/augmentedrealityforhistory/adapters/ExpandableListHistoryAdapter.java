package informatika.com.augmentedrealityforhistory.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;

import informatika.com.augmentedrealityforhistory.R;
import informatika.com.augmentedrealityforhistory.activities.OverlayActivity;
import informatika.com.augmentedrealityforhistory.models.ArrayWithId;
import informatika.com.augmentedrealityforhistory.models.Content;
import informatika.com.augmentedrealityforhistory.models.Group;
import informatika.com.augmentedrealityforhistory.resources.ResourceClass;

/**
 * Created by USER on 7/18/2016.
 */
public class ExpandableListHistoryAdapter extends BaseExpandableListAdapter {
    private final SparseArray<Group> groups;
    public LayoutInflater inflater;
    public Activity activity;
    private RequestQueue mRequestQueue;
    private HashMap<Integer, ImageView> imageViewThumbImages = new HashMap<>();
    private TextView textViewHistoryShortDescription;

    public ExpandableListHistoryAdapter(Activity activity, SparseArray<Group> groups){
        this.activity = activity;
        this.groups = groups;
        inflater = activity.getLayoutInflater();
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return groups.get(groupPosition).children.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return groups.get(groupPosition).children.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listrow_group, null);
        }
        Group group = (Group) getGroup(groupPosition);
        CheckedTextView checkedTextView = (CheckedTextView) convertView.findViewById(R.id.textViewListrowGroup);
        checkedTextView.setText(group.arrayWithId.getmText());
        checkedTextView.setChecked(isExpanded);

        textViewHistoryShortDescription = (TextView) convertView.findViewById(R.id.textViewHistoryShortDescription);
        textViewHistoryShortDescription.setText(group.arrayWithId.getmDescription());

        if(group.arrayWithId.getmIsTeacher()){
            TextView textViewIsHistoryValid = (TextView) convertView.findViewById(R.id.textViewIsHistoryValid);
            textViewIsHistoryValid.setVisibility(View.VISIBLE);
        }else{
            TextView textViewIsHistoryValid = (TextView) convertView.findViewById(R.id.textViewIsHistoryValid);
            textViewIsHistoryValid.setVisibility(View.GONE);
        }

        if(!imageViewThumbImages.containsKey(groupPosition)){
            ImageView imageViewThumbImage = (ImageView) convertView.findViewById(R.id.thumbImage);
            imageViewThumbImages.put(groupPosition, imageViewThumbImage);
        }

        if(group.arrayWithId.getmImageLink() != null){
            getImageForGroup(group.arrayWithId.getmImageLink(), groupPosition);
        } else {
            imageViewThumbImages.get(android.R.color.transparent);
        }

        LinearLayout linearLayout = (LinearLayout) convertView.findViewById(R.id.linearLayoutListrowGroup);
        linearLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String historyId = ((Group) getGroup(groupPosition)).arrayWithId.getmId();
                if(ResourceClass.mapHistoryWithContent.containsKey(historyId)){
                    if(!ResourceClass.arcontents.isEmpty()){
                        ResourceClass.arcontents.clear();
                    }
                    for(Content content : ResourceClass.mapHistoryWithContent.get(historyId)){
                        ResourceClass.arcontents.put(content.id, content);
                    }
                    nextOverlayActivity();
                }
            }
        });
        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final ArrayWithId children = (ArrayWithId) getChild(groupPosition, childPosition);
        TextView text = null;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listrow_details, null);
        }
        text = (TextView) convertView.findViewById(R.id.textViewListrowDetail);
        text.setText(children.getmText());

        convertView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity, children.getmText() + " + " + groupPosition + " + " + childPosition,
                        Toast.LENGTH_SHORT).show();
            }
        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    public void nextOverlayActivity() {
        Intent intent = new Intent(activity, OverlayActivity.class);
        activity.startActivity(intent);
    }

    private void getImageForGroup(String url, final int groupPosition) {
        mRequestQueue = Volley.newRequestQueue(activity);
        ImageRequest request = new ImageRequest(url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                imageViewThumbImages.get(groupPosition).setImageBitmap(response);
            }
        }, 0, 0, null,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("failed to retrieve icon");
                    }
                });
        request.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mRequestQueue.add(request);
    }
}
