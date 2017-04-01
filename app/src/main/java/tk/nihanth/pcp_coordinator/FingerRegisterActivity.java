package tk.nihanth.pcp_coordinator;

import android.app.ProgressDialog;
import android.content.Intent;
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

import java.util.HashMap;
import java.util.Map;

import tk.nihanth.pcp_coordinator.Models.Coordinator;

public class FingerRegisterActivity extends AppCompatActivity implements MFS100Event {

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
        setContentView(R.layout.activity_finger_register);
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
        Coordinator c = gson.fromJson(element, Coordinator.class);
        final Coordinator co = c;

        personId = (EditText) findViewById(R.id.PersonAttendanceId);
        courseId = (EditText) findViewById(R.id.PersonAttendanceCourseId);

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
                encodedIso = Base64.encodeToString(iso,0);

            }
        });

        Button registerButton = (Button) findViewById(R.id.FingerRegisterButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestQueue requestQueue = Volley.newRequestQueue(FingerRegisterActivity.this);

                String url = "http://52.183.88.200/Student/RegisterFinger";
                progressDialog.setMessage("Registering Finger");
                progressDialog.show();
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                progressDialog.dismiss();
                                Toast.makeText(FingerRegisterActivity.this,response,Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(FingerRegisterActivity.this,HomeActivity.class);
                                i.putExtra("coordinator",element.toString());
                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                                finish();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(FingerRegisterActivity.this,"Server Error",Toast.LENGTH_SHORT).show();
                            }
                        }){
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("CoordinatorId", co.id);
                        jsonObject.addProperty("Password", co.Password);
                        jsonObject.addProperty("StudentId", String.valueOf(personId.getText()));
                        jsonObject.addProperty("ISO",encodedIso);
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
