package com.shenxy13.project;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
public class OrganisationListActivity extends AppCompatActivity {
    @SuppressWarnings("unchecked")
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organisation_list);
        Toolbar appbar = findViewById(R.id.orglistbar);
        appbar.setTitleTextColor(getResources().getColor(R.color.colorText, null));
        setSupportActionBar(appbar);
        RuntimeDatastore.database.getReference().child("users").child(RuntimeDatastore.auth.getUid()).child("organisations").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final HashMap<String, Object> subscriptions;
                if (dataSnapshot.getValue() == null) subscriptions = new HashMap<>();
                else subscriptions = (HashMap<String, Object>) dataSnapshot.getValue();
                TableLayout table = findViewById(R.id.orgtable);
                table.removeAllViews();
                for (final String s: subscriptions.keySet()) {
                    LinearLayout lin = new LinearLayout(getApplicationContext());
                    lin.setBackgroundColor(getResources().getColor(R.color.colorButtons, null));
                    TableLayout.LayoutParams params = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, (int) (RuntimeDatastore.dpRatio * 60));
                    params.setMargins(0, (int) (10 * RuntimeDatastore.dpRatio), 0, 0);
                    lin.setLayoutParams(params);
                    final ImageView imageView = new ImageView(getApplicationContext());
                    RuntimeDatastore.database.getReference().child("organisations").child(s).child("hasicon").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (Boolean.TRUE.equals(dataSnapshot.getValue())) Glide.with(getApplicationContext()).load(RuntimeDatastore.store.getReference().child(s).child("icon.png")).into(imageView);
                            else imageView.setImageResource(R.drawable.grouplogo);
                        }
                        @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    LinearLayout.LayoutParams param = new LinearLayout.LayoutParams((int) (RuntimeDatastore.dpRatio * 50), (int) (RuntimeDatastore.dpRatio * 50));
                    param.setMargins((int) (5 * RuntimeDatastore.dpRatio), (int) (5 * RuntimeDatastore.dpRatio), (int) (5 * RuntimeDatastore.dpRatio), (int) (5 * RuntimeDatastore.dpRatio));
                    imageView.setLayoutParams(param);
                    lin.addView(imageView);
                    TextView text = new TextView(getApplicationContext());
                    param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    text.setLayoutParams(param);
                    text.setText(s);
                    text.setTextColor(getResources().getColor(R.color.colorBackground, null));
                    text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    text.setGravity(Gravity.CENTER);
                    lin.addView(text);
                    lin.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            String str = ((TextView) ((LinearLayout) v).getChildAt(1)).getText().toString();
                            RuntimeDatastore.currentPermissions = (Long) ((HashMap<String, Object>) subscriptions.get(str)).get("permissions");
                            RuntimeDatastore.currentOrganisation = str;
                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                        }
                    });
                    table.addView(lin);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.orglistbar, menu);
        return true;
    }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.orglistso:
                RuntimeDatastore.auth.signOut();
                startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                finish();
                return true;
            case R.id.orglistaddorg:
                startActivity(new Intent(getApplicationContext(), CreateOrganisationActivity.class));
                return true;
            case R.id.orglistjoin:
                startActivity(new Intent(getApplicationContext(), JoinOrganisationActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}