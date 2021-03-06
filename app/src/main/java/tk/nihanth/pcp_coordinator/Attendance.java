package tk.nihanth.pcp_coordinator;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.mantra.mfs100.FingerData;
import com.mantra.mfs100.MFS100;
import com.mantra.mfs100.MFS100Event;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import tk.nihanth.pcp_coordinator.Models.Coordinator;


public class Attendance extends AppCompatActivity implements MFS100Event {

    private static int mfsVer = 41;
    MFS100 mfs100 = new MFS100(this, mfsVer);
    TextView connectionStatus;
    ImageView fingerImage;
    TextView statusMessage;
    byte[] iso = null;
    FingerData fingerData = new FingerData();
    String encodedIso = null;
    byte[] iso1 = null;
    JsonElement element;

    EditText personId;
    EditText courseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        mfs100.SetApplicationContext(this);
        connectionStatus = (TextView) findViewById(R.id.DeviceConnectionStatus);
        fingerImage = (ImageView) findViewById(R.id.FingerImage);
        statusMessage = (TextView) findViewById(R.id.StatusMessageAttendance);

        final ProgressDialog progressDialog = new ProgressDialog(this);

        Intent i = getIntent();
        String temp = i.getStringExtra("coordinator");
        JsonParser parser = new JsonParser();
        element = parser.parse(temp);
        Gson gson = new Gson();
        final Coordinator c = gson.fromJson(element, Coordinator.class);

        final String centreId = c.CentreId;

        personId = (EditText) findViewById(R.id.PersonAttendanceId);
        courseId = (EditText) findViewById(R.id.PersonAttendanceCourseId);
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        Button capture = (Button) findViewById(R.id.FingerCaptureButton);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusMessage.setText("");
                int initialize = mfs100.Init();
                if (initialize != 0) {
                    statusMessage.setText("Error Initializing Device");
                    return;
                }

                int ret = mfs100.AutoCapture(fingerData, 10000, true, true);
                if (ret != 0) {
                    statusMessage.setText("Error Starting Capture");
                    return;
                } else {
                    byte[] tempData = new byte[2000];
                    int len = mfs100.ExtractISOTemplate(fingerData.RawData(), tempData);
                    if (len <= 0) {
                        if (len == 0) {
                            statusMessage.setText("Failed to extract ISO Template");
                        } else {
                            statusMessage.setText(mfs100.GetErrorMsg(len));
                        }
                        return;
                    } else {
                        iso = new byte[len];
                        System.arraycopy(tempData, 0, iso, 0, len);
                    }

                    statusMessage.setText("Success \n");
                }


            }
        });
        Button login = (Button) findViewById(R.id.AttendanceSubmitButton);
        login.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                statusMessage.setText("");
                int initialize = mfs100.Init();
                if (initialize != 0) {
                    statusMessage.setText("Error Initializing Device");
                    return;
                }
                progressDialog.setMessage("Verifying Fingerprint");
                progressDialog.show();
                String url = "http://nihanth.westus2.cloudapp.azure.com/Student/IsoTemplateRequest";
                final RequestQueue queue = Volley.newRequestQueue(Attendance.this);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if (response != "Student Data Not Found") {
                                    iso1 = Base64.decode(response, 0);
                                    int verify = mfs100.MatchISO(iso, iso1);
                                    if (verify >= 1400) {
                                        progressDialog.setMessage("Person Verified.\nPosting Attendance");

                                        String url = "http://52.183.88.200/Student/StudentAttendance";
                                        StringRequest request = new StringRequest(Request.Method.POST, url,
                                                new Response.Listener<String>() {
                                                    @Override
                                                    public void onResponse(String response) {
                                                        progressDialog.dismiss();
                                                        if(response.equals("Success"))
                                                            Toast.makeText(Attendance.this,"Attendance Successfully Posted",Toast.LENGTH_SHORT).show();
                                                        else{
                                                            Toast.makeText(Attendance.this,response,Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                },
                                                new Response.ErrorListener() {
                                                    @Override
                                                    public void onErrorResponse(VolleyError error) {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(Attendance.this,"Server Error. Try Again Later",Toast.LENGTH_SHORT).show();
                                                    }
                                                }){
                                            Calendar c = Calendar.getInstance();
                                            int date = c.get(Calendar.DATE);
                                            int month = c.get(Calendar.MONTH);
                                            int year = c.get(Calendar.YEAR);
                                            String dateStr = String.valueOf(date) + "-" + String.valueOf(month) + "-" + String.valueOf(year);

                                            @Override
                                            protected Map<String, String> getParams() throws AuthFailureError {
                                                Map<String,String> params = new HashMap<>();
                                                JsonObject jsonObject = new JsonObject();
                                                jsonObject.addProperty("Date",dateStr);
                                                jsonObject.addProperty("PersonId", String.valueOf(personId.getText()));
                                                jsonObject.addProperty("CourseId", String.valueOf(courseId.getText()));
                                                jsonObject.addProperty("CentreId",centreId);
                                                params.put("data",jsonObject.toString());
                                                return  params;
                                            }
                                        };
                                        queue.add(request);
                                    } else {
                                        statusMessage.setText("Finger Not Matched");
                                        progressDialog.dismiss();
                                    }
                                } else {
                                    Toast.makeText(Attendance.this, "Student Data Not Found", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("CoordinatorId", c.id);
                        jsonObject.addProperty("Password", c.Password);
                        jsonObject.addProperty("StudentId", String.valueOf(personId.getText()));
                        params.put("data", jsonObject.toString());
                        return params;
                    }
                };

                requestQueue.add(stringRequest);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mfs100 != null)
            mfs100.Dispose();
    }

    @Override
    public void OnDeviceAttached(int i, int i1, boolean b) {

        connectionStatus.setTextColor(getResources().getColor(R.color.colorPrimary));
        connectionStatus.setText("True");
    }

    @Override
    public void OnPreview(FingerData fingerData) {
        final Bitmap bitmap = BitmapFactory.decodeByteArray(
                fingerData.FingerImage(),
                0,
                fingerData.FingerImage().length
        );

        fingerImage.setImageBitmap(bitmap);

    }


    @Override
    public void OnCaptureCompleted(boolean b, int i, String s, FingerData fingerData) {
        statusMessage.setText("Hello World");
    }

    @Override
    public void OnDeviceDetached() {
        connectionStatus.setTextColor(getResources().getColor(R.color.colorAccent));
        connectionStatus.setText("False");
    }

    @Override
    public void OnHostCheckFailed(String s) {

    }

}
