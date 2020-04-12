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
import android.widget.Switch;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
public class AdminViewActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view);
        RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("description").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                EditText description = findViewById(R.id.adminviewdesc);
                description.setText((String) dataSnapshot.getValue());
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("public").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Switch publi = findViewById(R.id.adminviewpublic);
                publi.setChecked((boolean) dataSnapshot.getValue());
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        Toolbar appbar = findViewById(R.id.adminviewbar);
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
    public void updateData(View view) {
        EditText description = findViewById(R.id.adminviewdesc);
        Switch publi = findViewById(R.id.adminviewpublic);
        RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("description").setValue(description.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
        RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("public").setValue(publi.isChecked()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void addUsers(View view) {
        EditText users = findViewById(R.id.useremails);
        String ems = users.getText().toString();
        String[] emsList = ems.split(" ");
        for (String s : emsList) {
            RuntimeDatastore.database.getReference().child("emails").child(s.split("\\.")[0]).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        final String key = (String) dataSnapshot.getValue();
                        HashMap<String, Object> hsh = new HashMap<>();
                        hsh.put("permissions", RuntimeDatastore.PERMISSION_USER);
                        RuntimeDatastore.database.getReference().child("users").child(key).child("organisations").child(RuntimeDatastore.currentOrganisation).setValue(hsh).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    HashMap<String, Object> data = new HashMap<>();
                                    data.put("permissions", RuntimeDatastore.PERMISSION_USER);
                                    data.put("displayName", key);
                                    data.put("score_public", 0);
                                    data.put("score_secret", 0);
                                    data.put("score_total", 1000000);
                                    RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("users").child(key).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (!task.isSuccessful()) Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong) : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong) : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else Toast.makeText(getApplicationContext(), getString(R.string.user_not_exist), Toast.LENGTH_SHORT).show();
                }
                @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
