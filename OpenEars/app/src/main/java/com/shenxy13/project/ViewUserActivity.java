package com.shenxy13.project;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Locale;
public class ViewUserActivity extends AppCompatActivity {
    @SuppressWarnings("unchecked")
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user);
        RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("users").child(RuntimeDatastore.displayedUser).child("displayName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                TextView username = findViewById(R.id.viewusername);
                username.setText((String) dataSnapshot.getValue());
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("users").child(RuntimeDatastore.displayedUser).child("permissions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                TextView perms = findViewById(R.id.viewuserperm);
                switch ((int) (long) (dataSnapshot.getValue())) {
                    case (int) RuntimeDatastore.PERMISSION_OWNER:
                        perms.setText(getString(R.string.owner));
                        break;
                    case (int) RuntimeDatastore.PERMISSION_ADMIN:
                        perms.setText(getString(R.string.admin));
                        break;
                    default:
                        perms.setText(getString(R.string.member));
                }
                RuntimeDatastore.displayedUserPermissions = (long) dataSnapshot.getValue();
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("users").child(RuntimeDatastore.displayedUser).child("score_public").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                TextView score = findViewById(R.id.viewuserscore);
                score.setText(String.format(Locale.ENGLISH, "%s%d", getString(R.string.show_contribution), (long) dataSnapshot.getValue() / 5000));
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        RuntimeDatastore.database.getReference().child("users").child(RuntimeDatastore.displayedUser).child("organisations").child(RuntimeDatastore.currentOrganisation).child("posts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Object> children;
                if (dataSnapshot.getValue() == null) children = new HashMap<>();
                else children = (HashMap<String, Object>) dataSnapshot.getValue();
                TableLayout comments = findViewById(R.id.viewusercomments);
                for (final String s: children.keySet()) {
                    TableLayout card = new TableLayout(getApplicationContext());
                    card.setBackgroundColor(getResources().getColor(R.color.colorAppBar, null));
                    TableLayout.LayoutParams params = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, (int) (10 * RuntimeDatastore.dpRatio), 0, 0);
                    card.setLayoutParams(params);
                    params = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins((int) (5 * RuntimeDatastore.dpRatio), (int) (5 * RuntimeDatastore.dpRatio), (int) (5 * RuntimeDatastore.dpRatio), (int) (5 * RuntimeDatastore.dpRatio));
                    TextView disName = new TextView(getApplicationContext());
                    disName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                    disName.setLayoutParams(params);
                    disName.setTextColor(getResources().getColor(R.color.colorText, null));
                    disName.setText((String) children.get(s));
                    card.addView(disName);
                    card.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View view) {
                            RuntimeDatastore.currentPost = s.substring(1);
                            if (s.charAt(0) == 'f') {
                                RuntimeDatastore.currentPostType = RuntimeDatastore.TYPE_FEEDBACK;
                                startActivity(new Intent(getApplicationContext(), FeedbackActivity.class));
                            } else {
                                RuntimeDatastore.currentPostType = RuntimeDatastore.TYPE_SUGGESTION;
                                startActivity(new Intent(getApplicationContext(), SuggestionActivity.class));
                            }
                            finish();
                        }
                    });
                    comments.addView(card);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        Toolbar appbar = findViewById(R.id.viewuserbar);
        appbar.setTitleTextColor(getResources().getColor(R.color.colorText, null));
        setSupportActionBar(appbar);
    }
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        if (RuntimeDatastore.currentPermissions >= RuntimeDatastore.PERMISSION_ADMIN) getMenuInflater().inflate(R.menu.viewuseradbar, menu);
        else getMenuInflater().inflate(R.menu.backbar, menu);
        return true;
    }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.back:
                if (RuntimeDatastore.currentPostType == RuntimeDatastore.TYPE_FEEDBACK) startActivity(new Intent(getApplicationContext(), FeedbackActivity.class));
                else if (RuntimeDatastore.currentPostType == RuntimeDatastore.TYPE_SUGGESTION) startActivity(new Intent(getApplicationContext(), SuggestionActivity.class));
                else startActivity(new Intent(getApplicationContext(), ListUsersActivity.class));
                finish();
                return true;
            case R.id.adminusergo:
                startActivity(new Intent(getApplicationContext(), AdminUserActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
