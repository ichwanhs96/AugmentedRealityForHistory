package informatika.com.augmentedrealityforhistory.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import informatika.com.augmentedrealityforhistory.R;
import informatika.com.augmentedrealityforhistory.resources.ResourceClass;

/**
 * Created by USER on 7/20/2016.
 */
public class RegisterActivity extends AppCompatActivity {
    private RequestQueue mRequestQueue;
    private ProgressDialog dialog;

    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button submitButton;
    private CheckBox checkBoxIsTeacher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        dialog = new ProgressDialog(this);
        dialog.setCanceledOnTouchOutside(false);
        usernameEditText = (EditText) findViewById(R.id.editTextRegisterUsername);
        emailEditText = (EditText) findViewById(R.id.editTextRegisterEmail);
        passwordEditText = (EditText) findViewById(R.id.editTextRegisterPassword);
        confirmPasswordEditText = (EditText) findViewById(R.id.editTextRegisterConfirmPassword);
        submitButton = (Button) findViewById(R.id.buttonRegisterSubmit);
        checkBoxIsTeacher = (CheckBox) findViewById(R.id.checkBoxIsTeacher);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(confirmPasswordEditText.getText().toString().equals(passwordEditText.getText().toString())) {
                    dialog.setMessage("Registering...");
                    dialog.show();
                    postRegisterData();
                } else {
                    Toast.makeText(RegisterActivity.this, "confirm password harus sama dengan password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void postRegisterData(){
        String url = ResourceClass.url+"UserForHistories";
        System.out.println("backend : "+url);
        mRequestQueue = Volley.newRequestQueue(this);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", ""+usernameEditText.getText());
            jsonObject.put("password", ""+passwordEditText.getText());
            jsonObject.put("email", ""+emailEditText.getText());
            if(checkBoxIsTeacher.isEnabled()){
                jsonObject.put("isTeacher", true);
            } else {
                jsonObject.put("isTeacher", false);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest myReq = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(RegisterActivity.this, "register success", Toast.LENGTH_SHORT).show();
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        RegisterActivity.this.finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse networkResponse = error.networkResponse;
                        if(networkResponse.statusCode == 422){
                            Toast.makeText(RegisterActivity.this, "Username/Email sudah terpakai", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegisterActivity.this, "error on register", Toast.LENGTH_SHORT).show();
                        }
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        myReq.setRetryPolicy(new DefaultRetryPolicy(60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mRequestQueue.add(myReq);
    }
}
