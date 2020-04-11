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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Locale;
public class AdminUserActivity extends AppCompatActivity {
    private long userPrivateScore, userPublicScore;
    private static ValueEventListener listener;
    private static DatabaseReference ref;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user);
        listener = new ValueEventListener() {
            @SuppressWarnings("unchecked")
            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Object> data;
                if (dataSnapshot.getValue() == null) data = new HashMap<>();
                else data = (HashMap<String, Object>) dataSnapshot.getValue();
                TextView username = findViewById(R.id.adminusername), perms = findViewById(R.id.adminuserperm);
                username.setText((String) data.get("displayName"));
                RuntimeDatastore.displayedUserPermissions = (long) data.get("permissions");
                switch ((int) RuntimeDatastore.displayedUserPermissions) {
                    case (int) RuntimeDatastore.PERMISSION_OWNER:
                        perms.setText(getString(R.string.owner));
                        break;
                    case (int) RuntimeDatastore.PERMISSION_ADMIN:
                        perms.setText(getString(R.string.admin));
                        break;
                    default:
                        perms.setText(getString(R.string.member));
                }
                EditText sesame = findViewById(R.id.sesamescore);
                userPrivateScore = (long) data.get("score_secret");
                userPublicScore = (long) data.get("score_public");
                sesame.setText(String.format(Locale.ENGLISH, "%d", userPrivateScore / 5000));
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        ref = RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("users").child(RuntimeDatastore.displayedUser);
        ref.addValueEventListener(listener);
        Toolbar appbar = findViewById(R.id.adminuserbar);
        appbar.setTitleTextColor(getResources().getColor(R.color.colorText, null));
        setSupportActionBar(appbar);
    }
    @Override protected void onDestroy() {
        if (listener != null && ref != null) ref.removeEventListener(listener);
        super.onDestroy();
    }
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.backbar, menu);
        return true;
    }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.back:
                startActivity(new Intent(getApplicationContext(), ViewUserActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void updateSesame(View view) {
        EditText sesame = findViewById(R.id.sesamescore);
        long score = Long.parseLong(sesame.getText().toString()) * 5000 - userPrivateScore;
        ref.child("score_total").setValue(RuntimeDatastore.max(0, userPrivateScore + userPublicScore + score)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
        ref.child("score_secret").setValue(userPrivateScore + score).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void promoteToOwner(View view) {
        if (RuntimeDatastore.currentPermissions < RuntimeDatastore.PERMISSION_OWNER) Toast.makeText(getApplicationContext(), getString(R.string.insufficient_permissions), Toast.LENGTH_SHORT).show();
        else if (RuntimeDatastore.displayedUserPermissions < RuntimeDatastore.PERMISSION_OWNER) {
            ref.child("permissions").setValue(RuntimeDatastore.PERMISSION_OWNER).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()) Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                }
            });
            RuntimeDatastore.database.getReference().child("users").child(RuntimeDatastore.displayedUser).child("organisations").child(RuntimeDatastore.currentOrganisation).child("permissions").setValue(RuntimeDatastore.PERMISSION_OWNER).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()) Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    public void promoteToAdmin(View view) {
        if (RuntimeDatastore.currentPermissions < RuntimeDatastore.PERMISSION_OWNER) Toast.makeText(getApplicationContext(), getString(R.string.insufficient_permissions), Toast.LENGTH_SHORT).show();
        else if (RuntimeDatastore.displayedUserPermissions < RuntimeDatastore.PERMISSION_ADMIN) {
            ref.child("permissions").setValue(RuntimeDatastore.PERMISSION_ADMIN).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()) Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                }
            });
            RuntimeDatastore.database.getReference().child("users").child(RuntimeDatastore.displayedUser).child("organisations").child(RuntimeDatastore.currentOrganisation).child("permissions").setValue(RuntimeDatastore.PERMISSION_ADMIN).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()) Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                }
            });
        } else Toast.makeText(getApplicationContext(), getString(R.string.already_admin), Toast.LENGTH_SHORT).show();
    }
    public void demoteToUser(View view) {
        if (RuntimeDatastore.currentPermissions < RuntimeDatastore.PERMISSION_OWNER) Toast.makeText(getApplicationContext(), getString(R.string.insufficient_permissions), Toast.LENGTH_SHORT).show();
        else if (RuntimeDatastore.displayedUserPermissions < RuntimeDatastore.PERMISSION_OWNER) {
            ref.child("permissions").setValue(RuntimeDatastore.PERMISSION_USER).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()) Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                }
            });
            RuntimeDatastore.database.getReference().child("users").child(RuntimeDatastore.displayedUser).child("organisations").child(RuntimeDatastore.currentOrganisation).child("permissions").setValue(RuntimeDatastore.PERMISSION_USER).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()) Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                }
            });
        } else Toast.makeText(getApplicationContext(), getString(R.string.demote_owner), Toast.LENGTH_SHORT).show();
    }
    public void removeFromGroup(View view) {
        if (RuntimeDatastore.currentPermissions > RuntimeDatastore.displayedUserPermissions) {
            ref.removeEventListener(listener);
            ref.setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        RuntimeDatastore.database.getReference().child("users").child(RuntimeDatastore.displayedUser).child("organisations").child(RuntimeDatastore.currentOrganisation).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    RuntimeDatastore.displayedUserPermissions = 0;
                                    RuntimeDatastore.displayedUser = null;
                                    startActivity(new Intent(getApplicationContext(), ListUsersActivity.class));
                                    finish();
                                } else Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                }
            });
        } else Toast.makeText(getApplicationContext(), getString(R.string.insufficient_permissions), Toast.LENGTH_SHORT).show();
    }
}
