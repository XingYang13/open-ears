package com.shenxy13.project;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
public class StartScreenActivity extends AppCompatActivity {
    private static boolean interrupted = false, request_complete = false;
    final private static int STORAGE_PERMISSION = 1;
    final private static String[] STORAGE_REQ = {Manifest.permission.READ_EXTERNAL_STORAGE};
    @Override protected void onCreate(Bundle savedInstanceState) {
        interrupted = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
        request_complete = false;
        ActivityCompat.requestPermissions(this, STORAGE_REQ, STORAGE_PERMISSION);
        ImageView v = findViewById(R.id.start_animation);
        final AnimationDrawable anim = (AnimationDrawable) v.getBackground();
        anim.start();
        new Thread(new Runnable() {
            @Override public void run() {
                long time = System.currentTimeMillis();
                while (System.currentTimeMillis() - time <= 1500 || !request_complete);
                if (interrupted) return;
                if (RuntimeDatastore.auth.getCurrentUser() != null) {
                    startActivity(new Intent(getApplicationContext(), OrganisationListActivity.class));
                    return;
                }
                startActivity(new Intent(getApplicationContext(), SignInActivity.class));
            }
        }).start();
        RuntimeDatastore.dpRatio = getApplicationContext().getResources().getDisplayMetrics().density;
    }
    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean granted = false;
        switch (requestCode){
            case STORAGE_PERMISSION:
                granted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                request_complete = true;
                break;
            default:
                break;
        }
        if (!granted || Build.VERSION.SDK_INT > 28) Toast.makeText(getApplicationContext(), getString(R.string.image_not_work), Toast.LENGTH_SHORT).show();
    }
    public void revealTheTruth(View view) {
        startActivity(new Intent(getApplicationContext(), AboutAppActivity.class));
        interrupted = true;
    }
    public void findTheGuilty(View view) {
        startActivity(new Intent(getApplicationContext(), AboutDeveloperActivity.class));
        interrupted = true;
    }
}