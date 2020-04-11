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
public class AdminSuggestionsActivity extends AppCompatActivity {
    @SuppressWarnings("unchecked")
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_suggestions);
        Toolbar appbar = findViewById(R.id.adminsuggestionsbar);
        appbar.setTitleTextColor(getResources().getColor(R.color.colorText, null));
        setSupportActionBar(appbar);
        RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("adminSuggestions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Object> suggs;
                if (dataSnapshot.getValue() == null) suggs = new HashMap<>();
                else suggs = (HashMap<String, Object>) dataSnapshot.getValue();
                TableLayout tb = findViewById(R.id.adminsuggestionstable);
                tb.removeAllViews();
                for (final String s: suggs.keySet()) {
                    TableLayout card = new TableLayout(getApplicationContext());
                    card.setBackgroundColor(getResources().getColor(R.color.colorAppBar, null));
                    TableLayout.LayoutParams params = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, 0, (int) (5 * RuntimeDatastore.dpRatio));
                    card.setLayoutParams(params);
                    params = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins((int) (5 * RuntimeDatastore.dpRatio), (int) (5 * RuntimeDatastore.dpRatio), (int) (5 * RuntimeDatastore.dpRatio), (int) (5 * RuntimeDatastore.dpRatio));
                    TextView disName = new TextView(getApplicationContext());
                    disName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                    disName.setLayoutParams(params);
                    disName.setTextColor(getResources().getColor(R.color.colorText, null));
                    disName.setText((String) suggs.get(s));
                    card.addView(disName);
                    card.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View view) {
                            RuntimeDatastore.currentPost = s;
                            startActivity(new Intent(getApplicationContext(), SuggestionActivity.class));
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
                startActivity(new Intent(getApplicationContext(), SuggestionActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
