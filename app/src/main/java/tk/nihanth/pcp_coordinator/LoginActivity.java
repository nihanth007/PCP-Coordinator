package tk.nihanth.pcp_coordinator;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {


    EditText username;
    EditText password;
    JsonObject student;

    JsonObject login = new JsonObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        username = (EditText) findViewById(R.id.LoginUsername);
        password = (EditText) findViewById(R.id.LoginPassword);

        Button login = (Button) findViewById(R.id.LoginButton);
        Button faceLogin = (Button) findViewById(R.id.FaceLogin);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginNow(v);
            }
        });

        faceLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this,FaceLogin.class);
                startActivity(i);
            }
        });
    }

    public void LoginNow(View view){

        if(password.getText().toString().length()==0  && username.getText().toString().length()==0)
            Toast.makeText(LoginActivity.this,"Please Enter Your Username and Password Before you try to Login",Toast.LENGTH_SHORT).show();
        else if(password.getText().toString().length()==0 )
            Toast.makeText(LoginActivity.this,"Please Enter Your Password Before you try to Login",Toast.LENGTH_SHORT).show();
        else if(username.getText().toString().length()==0)
            Toast.makeText(LoginActivity.this,"Please Enter Your Username Before you try to Login",Toast.LENGTH_SHORT).show();
        else {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Login");
            progressDialog.setMessage("Logging in to the Server");
            progressDialog.show();

            Log.d("Username  ",username.getText().toString());
            Log.d("Password  ",password.getText().toString());

            SharedPreferences mPrefs = getSharedPreferences("PCP",0);
            String AuthToken = mPrefs.getString("FirebaseInstanceId","ThisIsTheDefaultValue");
            login.addProperty("username",username.getText().toString());
            login.addProperty("password",password.getText().toString());
            login.addProperty("AuthToken",AuthToken);

            Log.d("Login Data " , login.toString());

            final RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);

            String url = "http://52.183.88.200/Coordinator/Login/";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    JsonParser parser = new JsonParser();
                    student = (JsonObject) parser.parse(response);

                    if(student.has("isPresent")){
                        if(student.has("Error"))
                            Toast.makeText(LoginActivity.this,student.get("Error").toString(),Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(LoginActivity.this,"Failed to Login with Provided Details",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Log.i("Data",student.toString());
                        SharedPreferences myPrefs = getSharedPreferences("PCP",0);
                        SharedPreferences.Editor editor = myPrefs.edit();
                        editor.putString("PersonData",student.toString()).apply();
                        editor.commit();
                        Intent i = new Intent(LoginActivity.this,HomeActivity.class);
                        JsonElement jsonElement = parser.parse(response);
                        i.putExtra("coordinator",jsonElement.toString());
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                    }
                    progressDialog.dismiss();
                    requestQueue.stop();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("Error ",error.toString());
                    requestQueue.stop();
                }
            }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("data",login.toString());
                    return params;
                }
            };
            requestQueue.add(stringRequest);

        }

    }
}
