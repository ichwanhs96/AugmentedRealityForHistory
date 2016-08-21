package informatika.com.augmentedrealityforhistory.fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import informatika.com.augmentedrealityforhistory.R;
import informatika.com.augmentedrealityforhistory.activities.OverlayActivity;
import informatika.com.augmentedrealityforhistory.adapters.ArrayWithIdAdapter;
import informatika.com.augmentedrealityforhistory.models.ArrayWithId;
import informatika.com.augmentedrealityforhistory.models.Content;
import informatika.com.augmentedrealityforhistory.resources.ResourceClass;

/**
 * Created by USER on 7/27/2016.
 */
public class ChooseContentDialog extends DialogFragment {
    private ListView listView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_choose_content, container, false);
        getDialog().setTitle("Pilih Content");
        listView = (ListView) view.findViewById(R.id.listViewDialogChooseContent);
        ArrayList<ArrayWithId> list = new ArrayList<>();
        for(Map.Entry<String, Content> entry : ResourceClass.arcontents.entrySet()){
            ArrayWithId arrayWithId = new ArrayWithId();
            arrayWithId.setmId(entry.getKey());
            arrayWithId.setmText(entry.getValue().title);
            list.add(arrayWithId);
        }
        ArrayWithIdAdapter adapter = new ArrayWithIdAdapter(getActivity(), list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArrayWithId itemValue = (ArrayWithId) listView.getItemAtPosition(position);
                String currentContentId = ResourceClass.currentContentId;
                ResourceClass.currentContentId = itemValue.getmId();
                ((OverlayActivity) getActivity()).setNextTarget(currentContentId, ResourceClass.currentContentId);
                dismiss();
            }
        });
        return view;
    }
}
