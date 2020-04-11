package com.shenxy13.project;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
public class RegisterActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }
    public void register(View view) {
        EditText email = findViewById(R.id.registeremail), password = findViewById(R.id.registerpassword), confirm = findViewById(R.id.registerconfirm);
        final String em = email.getText().toString(), pw = password.getText().toString(), cf = confirm.getText().toString();
        if (em.equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.empty_error), Toast.LENGTH_SHORT).show();
            return;
        }
        if (pw.equals(cf)) {
            RuntimeDatastore.auth.createUserWithEmailAndPassword(em, pw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        RuntimeDatastore.database.getReference().child("organisations").child("Open Ears").child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                HashMap<String, Object> data = new HashMap<>();
                                data.put("permissions", RuntimeDatastore.PERMISSION_USER);
                                data.put("displayName", RuntimeDatastore.auth.getUid());
                                data.put("score_public", 0);
                                data.put("score_secret", 0);
                                data.put("score_total", 1000000);
                                RuntimeDatastore.database.getReference().child("organisations").child("Open Ears").child("users").child(RuntimeDatastore.auth.getUid()).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            RuntimeDatastore.database.getReference().child("users").child(RuntimeDatastore.auth.getUid()).child("organisations").child("Open Ears").child("permissions").setValue(RuntimeDatastore.PERMISSION_USER).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        RuntimeDatastore.database.getReference().child("emails").child(em.split("\\.")[0]).setValue(RuntimeDatastore.auth.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) startActivity(new Intent(getApplicationContext(), OrganisationListActivity.class));
                                                                else Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                    else Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } else Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong) : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong) : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                }
            });
        } else Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
    }
    public void goToSignIn(View view) {
        startActivity(new Intent(getApplicationContext(), SignInActivity.class));
    }
}