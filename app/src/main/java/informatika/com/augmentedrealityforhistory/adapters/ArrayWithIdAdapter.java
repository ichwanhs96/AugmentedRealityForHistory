package informatika.com.augmentedrealityforhistory.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import informatika.com.augmentedrealityforhistory.R;
import informatika.com.augmentedrealityforhistory.models.ArrayWithId;

/**
 * Created by USER on 7/28/2016.
 */
public class ArrayWithIdAdapter extends ArrayAdapter<ArrayWithId> {

    public ArrayWithIdAdapter(Context context, ArrayList<ArrayWithId> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ArrayWithId arrayWithId = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.view_dialog_select_content, parent, false);
        }
        // Lookup view for data population
        TextView textViewDialogSelectContent = (TextView) convertView.findViewById(R.id.textViewDialogSelectContent);
        // Populate the data into the template view using the data object
        textViewDialogSelectContent.setText(arrayWithId.getmText());
        // Return the completed view to render on screen
        return convertView;
    }
}
