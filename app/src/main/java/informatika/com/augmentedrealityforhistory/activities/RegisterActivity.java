package informatika.com.augmentedrealityforhistory.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.HashMap;
import java.util.Map;

import informatika.com.augmentedrealityforhistory.R;

/**
 * Created by USER on 7/20/2016.
 */
public class RegisterActivity extends AppCompatActivity {
    private RequestQueue mRequestQueue;
    private ProgressDialog dialog;

    EditText usernameEditText;
    EditText emailEditText;
    EditText passwordEditText;
    EditText confirmPasswordEditText;
    Button submitButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        dialog = new ProgressDialog(this);
        usernameEditText = (EditText) findViewById(R.id.editTextRegisterUsername);
        emailEditText = (EditText) findViewById(R.id.editTextRegisterEmail);
        passwordEditText = (EditText) findViewById(R.id.editTextRegisterPassword);
        confirmPasswordEditText = (EditText) findViewById(R.id.editTextRegisterConfirmPassword);
        submitButton = (Button) findViewById(R.id.buttonRegisterSubmit);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(confirmPasswordEditText.getText().toString().equals(passwordEditText.getText().toString())) {
                    dialog.setMessage("Registering...");
                    dialog.show();
                    System.out.println("username : " + usernameEditText.getText());
                    System.out.println("email : " + emailEditText.getText());
                    System.out.println("password : " + passwordEditText.getText());
                    System.out.println("confim password : " + confirmPasswordEditText.getText());
                    postRegisterData();
                } else {
                    Toast.makeText(RegisterActivity.this, "confirm password harus sama dengan password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void postRegisterData(){
        String url = "http://192.168.1.107:3000/api/UserForHistories";
        System.out.println("backend : "+url);
        mRequestQueue = Volley.newRequestQueue(this);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", ""+usernameEditText.getText());
            jsonObject.put("password", ""+passwordEditText.getText());
            jsonObject.put("email", ""+emailEditText.getText());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest myReq = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("response retrieved : "+response.toString());
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        RegisterActivity.this.finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(RegisterActivity.this, "error on register", Toast.LENGTH_SHORT).show();
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
        myReq.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mRequestQueue.add(myReq);
    }
}
