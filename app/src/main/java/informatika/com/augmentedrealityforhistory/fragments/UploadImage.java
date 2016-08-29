package informatika.com.augmentedrealityforhistory.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import informatika.com.augmentedrealityforhistory.R;
import informatika.com.augmentedrealityforhistory.resources.ResourceClass;
import informatika.com.augmentedrealityforhistory.util.ApiClient;
import informatika.com.augmentedrealityforhistory.util.ApiInterface;
import informatika.com.augmentedrealityforhistory.util.FileUtils;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by USER on 8/28/2016.
 */
public class UploadImage extends DialogFragment {
    private Button buttonChooseImage;
    private Button buttonUploadImage;
    private ImageView imageViewUploadImage;
    private int PICK_IMAGE_REQUEST = 1;
    private Bitmap bitmap;
    private RequestQueue mRequestQueue;
    private ProgressDialog progressDialog;
    private ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
    private Uri filePath;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_upload_image, container, false);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCanceledOnTouchOutside(false);
        buttonChooseImage = (Button) v.findViewById(R.id.buttonChooseImage);
        buttonUploadImage = (Button) v.findViewById(R.id.buttonUploadImage);
        imageViewUploadImage = (ImageView) v.findViewById(R.id.imageViewUploadImage);

        buttonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });

        buttonUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Mohon tunggu...");
                progressDialog.setTitle("Mengunggah gambar");
                progressDialog.show();
                makeNewContainer();
            }
        });

        getDialog().setTitle("Upload gambar");
        return v;
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                //Getting the Bitmap from Gallery
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                //Setting the Bitmap to ImageView
                imageViewUploadImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage(){
        final File file = new File(FileUtils.getPath(getActivity(), filePath));
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
        Call<ResponseBody> call = apiService.upload(ResourceClass.auth_key, body, ResourceClass.user_name);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                UploadImage.this.dismiss();
                Toast.makeText(getActivity(), "Berhasil mengunggah gambar", Toast.LENGTH_SHORT).show();
                ((AddContent)UploadImage.this.getTargetFragment()).changeEditTextImageUrl(ResourceClass.url+"/Containers/"+ResourceClass.user_name+"/download/"+file.getName());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                Toast.makeText(getActivity(), "Gagal mengunggah gambar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void makeNewContainer(){
        String url = ResourceClass.url+"Containers";
        mRequestQueue = Volley.newRequestQueue(getActivity());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", ""+ResourceClass.user_name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest myReq = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        uploadImage();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        uploadImage();
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
        myReq.setRetryPolicy(new DefaultRetryPolicy(60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mRequestQueue.add(myReq);
    }
}
