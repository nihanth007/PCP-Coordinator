package tk.nihanth.pcp_coordinator;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.microsoft.projectoxford.face.*;
import com.microsoft.projectoxford.face.contract.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import tk.nihanth.pcp_coordinator.Models.Student;

public class FaceLogin extends AppCompatActivity {

    private Bitmap bitmap;
    private ProgressDialog progressDialog;
    private final int CAPTURE = 1;
    private FaceServiceClient faceServiceClient = new FaceServiceRestClient("3ce50159bc674b818c7a47756f8f9f61");

    ImageView imageView;
    Button capture;
    Button login ;
    EditText personid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_login);

        progressDialog = new ProgressDialog(this);
        capture = (Button) findViewById(R.id.FaceCaptureButton);
        login = (Button) findViewById(R.id.FaceLoginButton);
        imageView = (ImageView) findViewById(R.id.FaceImage);
        personid = (EditText) findViewById(R.id.FacePersonId);

        login.setEnabled(false);

        if(!hasCamera()){
            capture.setEnabled(false);
        }
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(i,CAPTURE);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

                AsyncTask<InputStream,String,Face[]> detect = new AsyncTask<InputStream, String, Face[]>() {

                    @Override
                    protected void onPreExecute() {
                        progressDialog.setMessage("Detecting Face");
                        progressDialog.show();
                    }

                    @Override
                    protected Face[] doInBackground(InputStream... params) {
                        try{
                            Log.d("Test","Background Task is Being Executed ..............");
                            Log.d("Test", (String.valueOf(params[0].available())));
                            return faceServiceClient.detect(params[0],true,false,null);
                        }
                        catch(Exception e){
                            Toast.makeText(FaceLogin.this,"Could Not Connect to Face Login Service try again later",Toast.LENGTH_SHORT).show();
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(Face[] faces) {
                        if (faces != null) {
                            progressDialog.dismiss();
                            verify(faces[0]);
                        }
                        else{
                            progressDialog.dismiss();
                            Toast.makeText(FaceLogin.this,"Could not Detect Any Faces",Toast.LENGTH_LONG).show();
                        }
                    }
                };

                detect.execute(byteArrayInputStream);

            }
        });

    }

    private void verify(Face faceNow) {
        final String faceId1 = faceNow.faceId.toString();
        progressDialog.setMessage("Verifying Face with the Server...");
        progressDialog.show();
        String url = "http://52.183.88.200/Coordinator/VerifyFace/";
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JsonParser parser = new JsonParser();
                        JsonElement jsonElement =  parser.parse(response);
                        if(jsonElement.isJsonObject()){
                            Intent i = new Intent(FaceLogin.this,HomeActivity.class);
                            i.putExtra("coordinator",jsonElement.toString());
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            finish();
                        }

                        progressDialog.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(FaceLogin.this, "Error with Server" ,Toast.LENGTH_SHORT).show();
                    }
                }){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("FaceId",faceId1);
                jsonObject.addProperty("PersonId",personid.getText().toString());
                params.put("data",jsonObject.toString());
                Log.i("params",params.toString());
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private boolean hasCamera() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==CAPTURE && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(bitmap);
            login.setEnabled(true);
        }
    }
}
