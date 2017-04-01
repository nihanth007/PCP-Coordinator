package tk.nihanth.pcp_coordinator;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;

import tk.nihanth.pcp_coordinator.Models.Coordinator;

public class RegistrationActivity extends AppCompatActivity {

    EditText id;
    EditText fname;
    EditText lname;
    EditText password;
    EditText email;
    EditText dob;
    RadioButton genderMale;
    RadioButton genderFemale;
    EditText mobile;
    EditText currentAddress;
    EditText permanentAddress;
    EditText country;
    EditText centreId;
    EditText courseId;
    EditText parentName;

    Button RegisterButton;

    JsonElement element;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        final ProgressDialog progressDialog = new ProgressDialog(this);

        id = (EditText) findViewById(R.id.StudentId);
        fname = (EditText) findViewById(R.id.StudentFirstName);
        lname = (EditText) findViewById(R.id.StudentLastName);
        password = (EditText) findViewById(R.id.StudentPassword);
        email = (EditText) findViewById(R.id.StudentEmail);
        dob = (EditText) findViewById(R.id.StudentDOB);
        genderMale = (RadioButton) findViewById(R.id.StudentGenderMale);
        genderFemale = (RadioButton) findViewById(R.id.StudentGenderFemale);
        mobile = (EditText) findViewById(R.id.StudentMobile);
        currentAddress = (EditText) findViewById(R.id.StudentCurrentAddress);
        permanentAddress = (EditText) findViewById(R.id.StudentPermanentAddress);
        parentName = (EditText) findViewById(R.id.StudentParentName);
        country = (EditText) findViewById(R.id.StudentCountry);
        centreId = (EditText) findViewById(R.id.StudentCentreId);
        courseId = (EditText) findViewById(R.id.StudentCourseId);

        Intent i = getIntent();
        String temp = i.getStringExtra("coordinator");
        JsonParser parser = new JsonParser();
        element = parser.parse(temp);

        Gson gson = new Gson();
        final Coordinator c = gson.fromJson(element,Coordinator.class);

        RegisterButton = (Button) findViewById(R.id.RegisterStudentButton);

        RegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Registering Student");
                progressDialog.show();
                RequestQueue requestQueue = Volley.newRequestQueue(RegistrationActivity.this);
                final String gender = (genderMale.isSelected())?"M":"F";
                String url = "http://52.183.88.200/Coordinator/RegisterStudent";
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                progressDialog.dismiss();

                                if(response.equals("Success")){
                                    Toast.makeText(RegistrationActivity.this,"Student Successfully Registered",Toast.LENGTH_SHORT).show();
                                    Intent i = new Intent(RegistrationActivity.this,FingerRegisterActivity.class);
                                    i.putExtra("coordinator",element.toString());
                                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(i);
                                    finish();
                                }
                                else{
                                    Toast.makeText(RegistrationActivity.this,"Student Registration Failed.\nPlease Try Again Later",Toast.LENGTH_SHORT).show();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(RegistrationActivity.this,"Server Error.\nPlease Try Again Later",Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }){

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("id",String.valueOf(id.getText()));
                        jsonObject.addProperty("FirstName",String.valueOf(fname.getText()));
                        jsonObject.addProperty("LastName",String.valueOf(lname.getText()));
                        jsonObject.addProperty("Password",String.valueOf(password.getText()));
                        jsonObject.addProperty("Email",String.valueOf(email.getText()));
                        jsonObject.addProperty("DOB",String.valueOf(dob.getText()));
                        jsonObject.addProperty("Phone",String.valueOf(mobile.getText()));
                        jsonObject.addProperty("Gender",gender);
                        jsonObject.addProperty("CurrentAddress",String.valueOf(currentAddress.getText()));
                        jsonObject.addProperty("PermanentAddress",String.valueOf(permanentAddress.getText()));
                        jsonObject.addProperty("Country",String.valueOf(country.getText()));
                        jsonObject.addProperty("ParentName",String.valueOf(parentName.getText()));
                        jsonObject.addProperty("CentreId",String.valueOf(centreId.getText()));
                        jsonObject.addProperty("CourseId",String.valueOf(courseId.getText()));
                        jsonObject.addProperty("AuthToken","DefaultValue");
                        jsonObject.addProperty("ISOtemplate","DefaultValue");
                        params.put("data",jsonObject.toString());
                        Log.d("data",params.toString());
                        return params;
                    }
                };

                requestQueue.add(stringRequest);

            }
        });
    }
}
