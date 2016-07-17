package informatika.com.augmentedrealityforhistory.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.widget.ExpandableListView;

import informatika.com.augmentedrealityforhistory.R;
import informatika.com.augmentedrealityforhistory.adapters.ExpandableListHistoryAdapter;
import informatika.com.augmentedrealityforhistory.models.Group;

/**
 * Created by USER on 7/18/2016.
 */
public class ListHistoryActivity extends AppCompatActivity {
    SparseArray<Group> groups = new SparseArray<Group>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listhistory);
        createData();
        ExpandableListView listView = (ExpandableListView) findViewById(R.id.listView);
        ExpandableListHistoryAdapter adapter = new ExpandableListHistoryAdapter(this,
                groups);
        listView.setAdapter(adapter);
    }

    public void createData() {
        for (int j = 0; j < 5; j++) {
            Group group = new Group("Test " + j);
            for (int i = 0; i < 5; i++) {
                group.children.add("Sub Item" + i);
            }
            groups.append(j, group);
        }
    }
}
