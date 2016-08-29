package informatika.com.augmentedrealityforhistory.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
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
import informatika.com.augmentedrealityforhistory.resources.ResourceClass;

/**
 * Created by USER on 7/20/2016.
 */
public class LoginActivity extends AppCompatActivity {
    private RequestQueue mRequestQueue;
    private ProgressDialog dialog;

    private Button loginButton;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private TextView registerTextView;
    private CheckBox checkBoxKeepLogin;
    private android.app.FragmentManager fragmentManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(isTokenExist()){
            nextMainMenuActivity();
        } else {
            setContentView(R.layout.activity_login);
            dialog = new ProgressDialog(this);
            dialog.setCanceledOnTouchOutside(false);
            loginButton = (Button) findViewById(R.id.loginButton);
            usernameEditText = (EditText) findViewById(R.id.usernameEditText);
            passwordEditText = (EditText) findViewById(R.id.passwordEditText);
            registerTextView = (TextView) findViewById(R.id.registerTextView);
            checkBoxKeepLogin = (CheckBox) findViewById(R.id.checkBoxKeepLogin);

            fragmentManager = getFragmentManager();

            registerTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nextRegisterActivity();
                }
            });
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.setMessage("Login...");
                    dialog.show();
                    postLoginData();
                }
            });
        }
    }

    private boolean isTokenExist(){
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.AugmentedRealityForHistory_sharedpreference),Context.MODE_PRIVATE);
        ResourceClass.auth_key = sharedPref.getString(getString(R.string.AugmentedRealityForHistory_token), null);
        ResourceClass.user_id = sharedPref.getString(getString(R.string.AugmentedRealityForHistory_user_id), null);
        ResourceClass.user_email = sharedPref.getString(getString(R.string.AugmentedRealityForHistory_email), null);
        ResourceClass.user_name = sharedPref.getString(getString(R.string.AugmentedRealityForHistory_username), null);
        if(ResourceClass.auth_key != null &&
                ResourceClass.user_id != null &&
                ResourceClass.user_email != null &&
                ResourceClass.user_name != null){
            ResourceClass.isTeacher = sharedPref.getBoolean(getString(R.string.AugmentedRealityForHistory_isTeacher), false);
            return true;
        } else return false;
    }

    private void nextRegisterActivity(){
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void nextMainMenuActivity() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    private void postLoginData(){
        String url = ResourceClass.url+"UserForHistories/login";
        System.out.println("backend : "+url);
        mRequestQueue = Volley.newRequestQueue(this);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", usernameEditText.getText());
            jsonObject.put("password", passwordEditText.getText());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest myReq = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            ResourceClass.auth_key = response.getString("id");
                            ResourceClass.user_id = response.getString("userId");
                            ResourceClass.isTeacher = response.getBoolean("isTeacher");
                            ResourceClass.user_name = response.getString("username");
                            ResourceClass.user_email = response.getString("email");
                            if(checkBoxKeepLogin.isChecked()){
                                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.AugmentedRealityForHistory_sharedpreference),Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString(getString(R.string.AugmentedRealityForHistory_token), ResourceClass.auth_key);
                                editor.putString(getString(R.string.AugmentedRealityForHistory_user_id), ResourceClass.user_id);
                                editor.putBoolean(getString(R.string.AugmentedRealityForHistory_isTeacher), ResourceClass.isTeacher);
                                editor.putString(getString(R.string.AugmentedRealityForHistory_username), ResourceClass.user_name);
                                editor.putString(getString(R.string.AugmentedRealityForHistory_email), ResourceClass.user_email);
                                editor.commit();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        nextMainMenuActivity();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LoginActivity.this,"error on login", Toast.LENGTH_SHORT).show();
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
        myReq.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mRequestQueue.add(myReq);
    }
}
