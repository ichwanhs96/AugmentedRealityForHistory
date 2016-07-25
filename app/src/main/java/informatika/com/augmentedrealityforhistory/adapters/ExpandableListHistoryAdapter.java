package informatika.com.augmentedrealityforhistory.adapters;

import android.app.Activity;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import informatika.com.augmentedrealityforhistory.R;
import informatika.com.augmentedrealityforhistory.models.ArrayWithId;
import informatika.com.augmentedrealityforhistory.models.Group;

/**
 * Created by USER on 7/18/2016.
 */
public class ExpandableListHistoryAdapter extends BaseExpandableListAdapter {
    private final SparseArray<Group> groups;
    public LayoutInflater inflater;
    public Activity activity;

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
        TextView textViewHistoryShortDescription = (TextView) convertView.findViewById(R.id.textViewHistoryShortDescription);
        textViewHistoryShortDescription.setText(group.arrayWithId.getmDescription());
        checkedTextView.setChecked(isExpanded);
        checkedTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //((Group) getGroup(groupPosition)).arrayWithId.getmText()
            }
        });
        ImageView imageView = (ImageView) convertView.findViewById(R.id.thumbImage);
        imageView.setImageResource(R.drawable.marker);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final ArrayWithId children = (ArrayWithId) getChild(groupPosition, childPosition);
        TextView text = null;
        ImageView thumbChildImage;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listrow_details, null);
        }
        text = (TextView) convertView.findViewById(R.id.textViewListrowDetail);
        thumbChildImage = (ImageView) convertView.findViewById(R.id.thumbChildImage);
        thumbChildImage.setImageResource(R.drawable.icon);
        text.setText(children.getmText());
        convertView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity, children.getmText(),
                        Toast.LENGTH_SHORT).show();
            }
        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
