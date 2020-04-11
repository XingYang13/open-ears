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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
public class JoinOrganisationActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_organisation);
        Toolbar appbar = findViewById(R.id.joinorgbar);
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
                startActivity(new Intent(getApplicationContext(), OrganisationListActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void previewOrganisation(View view) {
        EditText on = findViewById(R.id.joinorgname);
        final String name = on.getText().toString();
        if (name.equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.empty_error), Toast.LENGTH_SHORT).show();
            return;
        }
        RuntimeDatastore.database.getReference().child("organisations").child(name).child("public").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (Boolean.TRUE.equals(dataSnapshot.getValue())) {
                    DatabaseReference ref = RuntimeDatastore.database.getReference().child("organisations").child(name);
                    final TextView title = findViewById(R.id.jointitle), subtitle = findViewById(R.id.joinsubtitle);
                    final ImageView imageView = findViewById(R.id.joinimage);
                    title.setText(name);
                    ref.child("description").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            subtitle.setText((String) (dataSnapshot.getValue()));
                        }
                        @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    ref.child("hasicon").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (Boolean.TRUE.equals(dataSnapshot.getValue())) Glide.with(getApplicationContext()).load(RuntimeDatastore.store.getReference().child(name).child("icon.png")).into(imageView);
                            else imageView.setImageResource(R.drawable.grouplogo);
                        }
                        @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else Toast.makeText(getApplicationContext(), getString(R.string.org_hidden), Toast.LENGTH_SHORT).show();
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void joinOrganisation(View view) {
        EditText on = findViewById(R.id.joinorgname);
        final String name = on.getText().toString();
        if (name.equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.empty_error), Toast.LENGTH_SHORT).show();
            return;
        }
        RuntimeDatastore.database.getReference().child("organisations").child(name).child("public").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (Boolean.TRUE.equals(dataSnapshot.getValue())) {
                    HashMap<String, Object> hsh = new HashMap<>();
                    hsh.put("permissions", RuntimeDatastore.PERMISSION_USER);
                    RuntimeDatastore.database.getReference().child("users").child(RuntimeDatastore.auth.getUid()).child("organisations").child(name).setValue(hsh).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                HashMap<String, Object> data = new HashMap<>();
                                data.put("permissions", RuntimeDatastore.PERMISSION_USER);
                                data.put("displayName", RuntimeDatastore.auth.getUid());
                                data.put("score_public", 0);
                                data.put("score_secret", 0);
                                data.put("score_total", 1000000);
                                RuntimeDatastore.database.getReference().child("organisations").child(name).child("users").child(RuntimeDatastore.auth.getUid()).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) startActivity(new Intent(getApplicationContext(), OrganisationListActivity.class));
                                        else Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else Toast.makeText(getApplicationContext(), getString(R.string.org_hidden), Toast.LENGTH_SHORT).show();
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
