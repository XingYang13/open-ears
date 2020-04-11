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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
public class ListUsersActivity extends AppCompatActivity {
    @SuppressWarnings("unchecked")
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_users);
        Toolbar appbar = findViewById(R.id.listusersbar);
        appbar.setTitleTextColor(getResources().getColor(R.color.colorText, null));
        setSupportActionBar(appbar);
        RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Object> us;
                if (dataSnapshot.getValue() == null) us = new HashMap<>();
                else us = (HashMap<String, Object>) dataSnapshot.getValue();
                ArrayList<User> users = new ArrayList<>();
                for (String key: us.keySet()) {
                    HashMap<String, Object> data = (HashMap<String, Object>) us.get(key);
                    users.add(new User(key, (String) data.get("displayName"), (Long) data.get("permissions"), (Long) data.get("score_total")));
                }
                Collections.sort(users);
                TableLayout tb = findViewById(R.id.userstable);
                tb.removeAllViews();
                for (final User u: users) {
                    TableLayout card = new TableLayout(getApplicationContext());
                    card.setBackgroundColor(getResources().getColor(R.color.colorAppBar, null));
                    TableLayout.LayoutParams params = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, 0, (int) (5 * RuntimeDatastore.dpRatio));
                    card.setLayoutParams(params);
                    params = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins((int) (5 * RuntimeDatastore.dpRatio), (int) (5 * RuntimeDatastore.dpRatio), (int) (5 * RuntimeDatastore.dpRatio), (int) (5 * RuntimeDatastore.dpRatio));
                    TextView disName = new TextView(getApplicationContext()), disRole = new TextView(getApplicationContext());
                    disName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                    disName.setLayoutParams(params);
                    disName.setTextColor(getResources().getColor(R.color.colorText, null));
                    disName.setText(u.getDisplayName());
                    card.addView(disName);
                    disRole.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                    disRole.setLayoutParams(params);
                    disRole.setTextColor(getResources().getColor(R.color.colorText, null));
                    switch ((int) u.getPermissions()) {
                        case (int) RuntimeDatastore.PERMISSION_OWNER:
                            disRole.setText(getString(R.string.owner));
                            break;
                        case (int) RuntimeDatastore.PERMISSION_ADMIN:
                            disRole.setText(getString(R.string.admin));
                            break;
                        default:
                            disRole.setText(getString(R.string.member));
                    }
                    card.addView(disRole);
                    card.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View view) {
                            RuntimeDatastore.displayedUser = u.getUid();
                            startActivity(new Intent(getApplicationContext(), ViewUserActivity.class));
                        }
                    });
                    tb.addView(card);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
}
