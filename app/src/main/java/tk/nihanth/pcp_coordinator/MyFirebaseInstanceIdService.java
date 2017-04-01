package tk.nihanth.pcp_coordinator;


import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("Refreshed token: " , refreshedToken);

        SharedPreferences mPrefs = getSharedPreferences("PCP", 0);
        SharedPreferences.Editor editPrefs = mPrefs.edit();
        editPrefs.putString("FirebaseInstanceId",refreshedToken).apply();
        editPrefs.commit();
    }
}
