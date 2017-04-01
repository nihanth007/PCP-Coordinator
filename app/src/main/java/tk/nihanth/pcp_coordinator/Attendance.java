package tk.nihanth.pcp_coordinator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mantra.mfs100.FingerData;
import com.mantra.mfs100.MFS100;
import com.mantra.mfs100.MFS100Event;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


public class Attendance extends AppCompatActivity implements MFS100Event {

    private static int mfsVer = 41;
    MFS100 mfs100 = new MFS100(this,mfsVer);
    TextView connectionStatus;
    ImageView fingerImage;
    TextView statusMessage;
    byte[] iso = null;
    FingerData fingerData = new FingerData();
    String encodedIso = null;
    byte[] iso1 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        mfs100.SetApplicationContext(this);
        connectionStatus = (TextView) findViewById(R.id.DeviceConnectionStatus);
        fingerImage = (ImageView) findViewById(R.id.FingerImage);
        statusMessage = (TextView) findViewById(R.id.StatusMessageAttendance);

        Button capture = (Button) findViewById(R.id.FingerCaptureButton);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusMessage.setText("");
                int initialize = mfs100.Init();
                if(initialize != 0){
                    statusMessage.setText("Error Initializing Device");
                    return;
                }
                /*int ret = mfs100.StartCapture(80,0,true);
                if(ret != 0){
                    statusMessage.setText("Error Starting Capture");
                    return;
                }*/

                int ret = mfs100.AutoCapture(fingerData,10000,true,true);
                if(ret != 0){
                    statusMessage.setText("Error Starting Capture");
                    return;
                }else{
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

                    File file = new File(getFilesDir(),"isoTemplate.fmr");
                    try {
                        if(file.exists()){
                            boolean t = file.delete();
                            t = file.createNewFile();
                        }else{
                            boolean t = file.createNewFile();
                        }

//                        FileOutputStream fileOutputStream = new FileOutputStream(file);
//                        fileOutputStream.write(iso);
//                        fileOutputStream.close();

                        encodedIso = Base64.encodeToString(iso,0);
//                        FileWriter fileWriter = new FileWriter(file);
//                        fileWriter.write(encodedIso);
//                        fileWriter.close();

                        statusMessage.setText("Success \n" + encodedIso);

                    } catch (FileNotFoundException e) {
                        statusMessage.setText(e.getMessage());
                    } catch (IOException e) {
                        statusMessage.setText(e.getMessage());
                    }
                }


            }
        });
        Button login = (Button) findViewById(R.id.AttendanceSubmitButton);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusMessage.setText("");
                int initialize = mfs100.Init();
                if(initialize != 0){
                    statusMessage.setText("Error Initializing Device");
                    return;
                }

                int ret = mfs100.AutoCapture(fingerData,10000,true,true);
                if(ret != 0){
                    statusMessage.setText("Error Starting Capture");
                    return;
                }else{
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

                    File file = new File(getFilesDir(),"isoTemplate.fmr");
                    if(!file.exists()){
                        statusMessage.setText("File Not Found");
                        return;
                    }

//                        FileReader fileReader = new FileReader(file);
//                        char[] buf = new char[(int) file.length()];
//                        len = fileReader.read(buf);
//                        String encodedIso = buf.toString();
                    iso1 = Base64.decode(encodedIso,0);

//                        FileInputStream fileInputStream = new FileInputStream(file);
//                        iso1 = new byte[(int) file.length()];
//                        int res = fileInputStream.read(iso1);

                    int verify = mfs100.MatchISO(iso,iso1);
                    if(verify >= 1400){
                        statusMessage.setText("Finger Matched");
                    }
                    else{
                        statusMessage.setText(String.valueOf(verify) + "\n" + mfs100.GetLastError());
                    }
                }

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mfs100 !=null)
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
