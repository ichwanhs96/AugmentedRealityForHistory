package informatika.com.augmentedrealityforhistory.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import informatika.com.augmentedrealityforhistory.R;

/**
 * Created by USER on 7/23/2016.
 */
public class AddHistory extends Fragment {
    private FloatingActionButton fabAddHistory;
    private FloatingActionButton fabRemoveHistory;
    private Button buttonHistorySubmit;
    private int contentCounter = 0;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_history, container, false);
        buttonHistorySubmit = (Button) v.findViewById(R.id.buttonHistorySubmit);
        fabAddHistory = (FloatingActionButton) v.findViewById(R.id.fabAddHistory);
        fabRemoveHistory = (FloatingActionButton) v.findViewById(R.id.fabRemoveHistory);

        buttonHistorySubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "submit clicked", Toast.LENGTH_SHORT).show();
            }
        });

        fabAddHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "add fab clicked", Toast.LENGTH_SHORT).show();
                View childView = inflater.inflate(R.layout.view_content_add_history, null);

                Spinner spinnerSelectContent = (Spinner) childView.findViewById(R.id.spinnerSelectContent);

                List<String> poiArray = new ArrayList<String>();
                poiArray.add("itb");
                poiArray.add("ui");
                poiArray.add("ugm");
                // Create an ArrayAdapter using the string array and a default spinner layout
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, poiArray);
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Apply the adapter to the spinner
                spinnerSelectContent.setAdapter(adapter);

                ViewGroup viewGroup = (ViewGroup) getView().findViewById(R.id.linearLayoutAddHistory);
                childView.setId(contentCounter);
                viewGroup.addView(childView);
                contentCounter++;
            }
        });

        fabRemoveHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "remove fab clicked", Toast.LENGTH_SHORT).show();
                if(contentCounter > 0){
                    ViewGroup viewGroup = (ViewGroup) getView().findViewById(R.id.linearLayoutAddHistory);
                    contentCounter--;
                    LinearLayout linearLayout = (LinearLayout) viewGroup.findViewById(contentCounter);
                    viewGroup.removeView(linearLayout);
                }
            }
        });
        return v;
    }
}
