package com.shenxy13.project;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
public class HomeActivity extends AppCompatActivity {
    private static ValueEventListener listener;
    private static DatabaseReference ref;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        if (ref != null && listener != null) ref.removeEventListener(listener);
        listener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) RuntimeDatastore.myCurrentScore = (long) dataSnapshot.getValue();
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        ref = RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("users").child(RuntimeDatastore.auth.getUid()).child("score_total");
        ref.addValueEventListener(listener);
        RuntimeDatastore.currentPost = null;
        RuntimeDatastore.currentPostType = RuntimeDatastore.TYPE_NULL;
        final TextView title = findViewById(R.id.hometitle), subtitle = findViewById(R.id.homesubtitle);
        title.setText(RuntimeDatastore.currentOrganisation);
        final ImageView imageView = findViewById(R.id.homeimage);
        final String s = RuntimeDatastore.currentOrganisation;
        RuntimeDatastore.database.getReference().child("organisations").child(s).child("hasicon").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (Boolean.TRUE.equals(dataSnapshot.getValue())) Glide.with(getApplicationContext()).load(RuntimeDatastore.store.getReference().child(s).child("icon.png")).into(imageView);
                else imageView.setImageResource(R.drawable.grouplogo);
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("description").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                subtitle.setText((String) dataSnapshot.getValue());
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        Toolbar appbar = findViewById(R.id.homebar);
        appbar.setTitleTextColor(getResources().getColor(R.color.colorText, null));
        setSupportActionBar(appbar);
    }
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        if (RuntimeDatastore.currentPermissions >= RuntimeDatastore.PERMISSION_ADMIN) getMenuInflater().inflate(R.menu.homeadbar, menu);
        else getMenuInflater().inflate(R.menu.homebar, menu);
        return true;
    }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.back:
                startActivity(new Intent(getApplicationContext(), OrganisationListActivity.class));
                if (ref != null && listener != null) ref.removeEventListener(listener);
                finish();
                return true;
            case R.id.settings:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                return true;
            case R.id.listppl:
                startActivity(new Intent(getApplicationContext(), ListUsersActivity.class));
                return true;
            case R.id.adminpage:
                startActivity(new Intent(getApplicationContext(), AdminViewActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void leaveCurrentOrganisation(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.leave_confirm));
        builder.setMessage(getString(R.string.irreversible_warning));
        builder.setNegativeButton(getString(R.string.no), null);
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialogInterface, int i) {
                RuntimeDatastore.database.getReference().child("users").child(RuntimeDatastore.auth.getUid()).child("organisations").child(RuntimeDatastore.currentOrganisation).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("users").child(RuntimeDatastore.auth.getUid()).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        startActivity(new Intent(getApplicationContext(), OrganisationListActivity.class));
                                        finish();
                                    } else Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        builder.show();
    }
    public void goToPollsList(View view) {
        RuntimeDatastore.currentPostType = RuntimeDatastore.TYPE_POLL;
        startActivity(new Intent(getApplicationContext(), ListPollsActivity.class));
    }
    public void goToFeedbackRoot(View view) {
        RuntimeDatastore.currentPost = "root";
        RuntimeDatastore.currentPostType = RuntimeDatastore.TYPE_FEEDBACK;
        startActivity(new Intent(getApplicationContext(), FeedbackActivity.class));
    }
    public void goToSuggestionsRoot(View view) {
        RuntimeDatastore.currentPost = "root";
        RuntimeDatastore.currentPostType = RuntimeDatastore.TYPE_SUGGESTION;
        startActivity(new Intent(getApplicationContext(), SuggestionActivity.class));
    }
}
