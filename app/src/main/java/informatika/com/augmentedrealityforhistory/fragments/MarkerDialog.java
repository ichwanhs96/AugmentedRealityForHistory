package informatika.com.augmentedrealityforhistory.fragments;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import informatika.com.augmentedrealityforhistory.R;
import informatika.com.augmentedrealityforhistory.activities.MapsActivity;
import informatika.com.augmentedrealityforhistory.activities.OverlayActivity;
import informatika.com.augmentedrealityforhistory.models.HistoryImage;
import informatika.com.augmentedrealityforhistory.models.ListHistoryResponseContainer;
import informatika.com.augmentedrealityforhistory.resources.ResourceClass;
import informatika.com.augmentedrealityforhistory.util.GsonRequest;

/**
 * Created by USER on 7/11/2016.
 */
public class MarkerDialog extends DialogFragment {
    private RequestQueue mRequestQueue;
    private Button mapButton;
    private CarouselView carouselView;
    private List<String> imageLinks;
    private List<Location> imageLocations;
    private HashMap<Integer, Bitmap> imageBitmaps;
    private int positionCallback;
    private boolean isCallbackFullfiled = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_marker_dialog, container);

        mapButton = (Button) view.findViewById(R.id.mapButton);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                startActivity(intent);
            }
        });
        getDialog().setTitle(ResourceClass.arcontents.get(ResourceClass.currentContentId).title);

        imageLinks = new ArrayList<>();
        imageBitmaps = new HashMap<>();
        imageLocations = new ArrayList<>();
        TextView textViewMarkerDialogContentDescription = (TextView) view.findViewById(R.id.textViewMarkerDialogContentDescription);
        textViewMarkerDialogContentDescription.setText(ResourceClass.arcontents.get(ResourceClass.currentContentId).description);

        getContentImages();

        carouselView = (CarouselView) view.findViewById(R.id.carouselView);
        carouselView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                System.out.println("image location lat : " + imageLocations.get(position).getLatitude() + ", lng : " + imageLocations.get(position).getLongitude());
                ((OverlayActivity)getActivity()).imagePlaceTakenLocation = imageLocations.get(position);
                ((OverlayActivity)getActivity()).bitmapForMarker = imageBitmaps.get(position);
                ((OverlayActivity)getActivity()).updateBitmapForMarker = true;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        return view;
    }

    private void getImage(String url, final int position) {
        mRequestQueue = Volley.newRequestQueue(getActivity());
        ImageRequest request = new ImageRequest(url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                imageBitmaps.put(position, response);
                callbackCaraoselImageListener(position);
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

    private void callbackCaraoselImageListener(int position){
        if(positionCallback < position){
            positionCallback = position;
        }
        if(positionCallback == (imageLinks.size()-1) && imageBitmaps.size() == imageLinks.size() && !isCallbackFullfiled) {
            carouselView.setImageListener(new ImageListener() {
                @Override
                public void setImageForPosition(int position, ImageView imageView) {
                    imageView.setImageBitmap(imageBitmaps.get(position));
                }
            });
            carouselView.setPageCount(imageLinks.size());
            ((OverlayActivity)getActivity()).imagePlaceTakenLocation = imageLocations.get(0);
            ((OverlayActivity)getActivity()).bitmapForMarker = imageBitmaps.get(0);
            ((OverlayActivity)getActivity()).updateBitmapForMarker = true;
            isCallbackFullfiled = true;
        }
    }

    private void getContentImages(){
        String url = ResourceClass.url+"Contents/"+ResourceClass.currentContentId+"/Images";
        mRequestQueue = Volley.newRequestQueue(getActivity());
        GsonRequest<HistoryImage[]> myReq = new GsonRequest<HistoryImage[]>(
                Request.Method.GET,
                url,
                HistoryImage[].class,
                new com.android.volley.Response.Listener<HistoryImage[]>() {
                    @Override
                    public void onResponse(HistoryImage[] response) {
                        for(HistoryImage historyImage : response){
                            imageLinks.add(historyImage.url);
                            Location location = new Location("");
                            location.setLatitude(historyImage.location.getLat());
                            location.setLongitude(historyImage.location.getLng());
                            imageLocations.add(location);
                        }

                        for(int i = 0; i < imageLinks.size(); i++){
                            getImage(imageLinks.get(i), i);
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), "content images cant be retrieved", Toast.LENGTH_SHORT).show();
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
}
