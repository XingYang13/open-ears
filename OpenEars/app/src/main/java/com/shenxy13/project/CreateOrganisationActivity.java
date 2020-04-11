package com.shenxy13.project;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.util.HashMap;
public class CreateOrganisationActivity extends AppCompatActivity {
    final private static int RESULT_LOAD_IMAGE = 1;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_organisation);
        Toolbar appbar = findViewById(R.id.createorgbar);
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
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            File f = new File(picturePath);
            EditText name = findViewById(R.id.createorgname), desc = findViewById(R.id.createorgdesc);
            final String orgname = name.getText().toString(), orgdesc = desc.getText().toString();
            if (orgname.equals("")) {
                Toast.makeText(getApplicationContext(), getString(R.string.empty_error), Toast.LENGTH_SHORT).show();
                return;
            }
            RuntimeDatastore.store.getReference().child(orgname).child("icon.png").putFile(Uri.fromFile(f)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        HashMap<String, Object> userdata = new HashMap<>(), why = new HashMap<>(), axe = new HashMap<>(), r1 = new HashMap<>(), r2 = new HashMap<>();
                        why.put("text", "Have some opinions on the current situation? Please, make a post and share it here!");
                        axe.put("text", "Have some ideas fresh on your mind that you want to implement? Share it with us and we may make your dreams a reality!");
                        r1.put("root", why);
                        r2.put("root", axe);
                        userdata.put("permissions", RuntimeDatastore.PERMISSION_OWNER);
                        userdata.put("displayName", RuntimeDatastore.auth.getUid());
                        userdata.put("score_public", 0);
                        userdata.put("score_secret", 0);
                        userdata.put("score_total", 1000000);
                        HashMap<String, Object> users = new HashMap<>();
                        users.put(RuntimeDatastore.auth.getUid(), userdata);
                        HashMap<String, Object> hsh = new HashMap<>();
                        hsh.put("description", orgdesc);
                        hsh.put("hasicon", true);
                        hsh.put("public", true);
                        hsh.put("users", users);
                        hsh.put("feedback", r1);
                        hsh.put("suggestions", r2);
                        RuntimeDatastore.database.getReference().child("organisations").child(orgname).setValue(hsh).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    final HashMap<String, Object> pdata = new HashMap<>();
                                    pdata.put("permissions", RuntimeDatastore.PERMISSION_OWNER);
                                    RuntimeDatastore.database.getReference().child("users").child(getString(R.string.owner_id)).child("organisations").child(orgname).setValue(pdata).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                RuntimeDatastore.database.getReference().child("users").child(RuntimeDatastore.auth.getUid()).child("organisations").child(orgname).setValue(pdata).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) startActivity(new Intent(getApplicationContext(), OrganisationListActivity.class));
                                                        else Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } else Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    public void createOrgWithoutIcon(View view) {
        EditText name = findViewById(R.id.createorgname), desc = findViewById(R.id.createorgdesc);
        final String orgname = name.getText().toString(), orgdesc = desc.getText().toString();
        if (orgname.equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.empty_error), Toast.LENGTH_SHORT).show();
            return;
        }
        RuntimeDatastore.database.getReference().child("organisations").child(orgname).child("public").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    HashMap<String, Object> userdata = new HashMap<>(), why = new HashMap<>(), axe = new HashMap<>(), r1 = new HashMap<>(), r2 = new HashMap<>();
                    why.put("text", "Have some opinions on the current situation? Please, make a post and share it here!");
                    axe.put("text", "Have some ideas fresh on your mind that you want to implement? Share it with us and we may make your dreams a reality!");
                    r1.put("root", why);
                    r2.put("root", axe);
                    userdata.put("permissions", RuntimeDatastore.PERMISSION_OWNER);
                    userdata.put("displayName", RuntimeDatastore.auth.getUid());
                    userdata.put("score_public", 0);
                    userdata.put("score_secret", 0);
                    userdata.put("score_total", 1000000);
                    HashMap<String, Object> users = new HashMap<>();
                    users.put(RuntimeDatastore.auth.getUid(), userdata);
                    HashMap<String, Object> hsh = new HashMap<>();
                    hsh.put("description", orgdesc);
                    hsh.put("hasicon", false);
                    hsh.put("public", true);
                    hsh.put("users", users);
                    hsh.put("feedback", r1);
                    hsh.put("suggestions", r2);
                    RuntimeDatastore.database.getReference().child("organisations").child(orgname).setValue(hsh).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                final HashMap<String, Object> pdata = new HashMap<>();
                                pdata.put("permissions", RuntimeDatastore.PERMISSION_OWNER);
                                RuntimeDatastore.database.getReference().child("users").child(getString(R.string.owner_id)).child("organisations").child(orgname).setValue(pdata).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            RuntimeDatastore.database.getReference().child("users").child(RuntimeDatastore.auth.getUid()).child("organisations").child(orgname).setValue(pdata).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) startActivity(new Intent(getApplicationContext(), OrganisationListActivity.class));
                                                    else Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } else Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else Toast.makeText(getApplicationContext(), getString(R.string.org_exists), Toast.LENGTH_SHORT).show();
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void createOrgWithIcon(View view) {
        EditText name = findViewById(R.id.createorgname), desc = findViewById(R.id.createorgdesc);
        final String orgname = name.getText().toString(), orgdesc = desc.getText().toString();
        if (orgname.equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.empty_error), Toast.LENGTH_SHORT).show();
            return;
        }
        RuntimeDatastore.database.getReference().child("organisations").child(orgname).child("public").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) Toast.makeText(getApplicationContext(), getString(R.string.org_exists), Toast.LENGTH_SHORT).show();
                else startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), RESULT_LOAD_IMAGE);
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
