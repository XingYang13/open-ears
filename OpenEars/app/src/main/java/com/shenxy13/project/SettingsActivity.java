package com.shenxy13.project;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.Locale;
public class SettingsActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        TextView perms = findViewById(R.id.settingstype);
        switch ((int) RuntimeDatastore.currentPermissions) {
            case (int) RuntimeDatastore.PERMISSION_OWNER:
                perms.setText(getString(R.string.owner));
                break;
            case (int) RuntimeDatastore.PERMISSION_ADMIN:
                perms.setText(getString(R.string.admin));
                break;
            default:
                perms.setText(getString(R.string.member));
        }
        RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("users").child(RuntimeDatastore.auth.getUid()).child("displayName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                EditText username = findViewById(R.id.settingsname);
                username.setText((String) dataSnapshot.getValue());
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("users").child(RuntimeDatastore.auth.getUid()).child("score_public").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                TextView score = findViewById(R.id.settingsscore);
                score.setText(String.format(Locale.ENGLISH, "%s%d", getString(R.string.show_contribution), (long) dataSnapshot.getValue() / 5000));
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        Toolbar appbar = findViewById(R.id.settingsbar);
        appbar.setTitleTextColor(getResources().getColor(R.color.colorText, null));
        setSupportActionBar(appbar);
    }
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.backbar, menu);
        return true;
    }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.back:
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void updateSettings(View view) {
        EditText username = findViewById(R.id.settingsname);
        String name = username.getText().toString();
        if (!name.equals("")) {
            RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("users").child(RuntimeDatastore.auth.getUid()).child("displayName").setValue(name).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) Toast.makeText(getApplicationContext(), getString(R.string.updated_settings), Toast.LENGTH_SHORT).show();
                    else Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                }
            });
        } else Toast.makeText(getApplicationContext(), getString(R.string.username_empty), Toast.LENGTH_SHORT).show();
    }
    public void viewProfile(View view) {
        RuntimeDatastore.displayedUser = RuntimeDatastore.auth.getUid();
        startActivity(new Intent(getApplicationContext(), ViewUserActivity.class));
        finish();
    }
}