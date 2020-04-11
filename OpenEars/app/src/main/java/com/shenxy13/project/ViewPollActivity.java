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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Locale;
public class ViewPollActivity extends AppCompatActivity {
    private HashMap<String, Object> data;
    private String currentSelection;
    private static ValueEventListener listener;
    private static DatabaseReference ref;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_poll);
        if (ref != null && listener != null) ref.removeEventListener(listener);
        Toolbar appbar = findViewById(R.id.viewpollbar);
        appbar.setTitleTextColor(getResources().getColor(R.color.colorText, null));
        setSupportActionBar(appbar);
        RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("polls").child("pollslist").child(RuntimeDatastore.currentPost).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                TextView title = findViewById(R.id.viewpollquestion);
                title.setText((String) dataSnapshot.getValue());
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("polls").child(RuntimeDatastore.currentPost).child("votes").child(RuntimeDatastore.auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressWarnings("unchecked")
            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentSelection = (String) dataSnapshot.getValue();
                listener = new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        data = (HashMap<String, Object>) dataSnapshot.getValue();
                        TableLayout tb = findViewById(R.id.viewpolloptions);
                        tb.removeAllViews();
                        for (final String s: data.keySet()) {
                            TextView tv = new TextView(getApplicationContext());
                            if (s.equals(currentSelection)) tv.setBackgroundColor(getResources().getColor(R.color.colorAppBar, null));
                            else tv.setBackgroundColor(getResources().getColor(R.color.colorButtons, null));
                            tv.setTextColor(getResources().getColor(R.color.colorBackground, null));
                            tv.setText(String.format(Locale.ENGLISH, "%s: %d", s, data.get(s)));
                            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                            TableLayout.LayoutParams params = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                            params.setMargins(0, (int) (10 * RuntimeDatastore.dpRatio), 0, 0);
                            tv.setLayoutParams(params);
                            tv.setOnClickListener(new View.OnClickListener() {
                                @Override public void onClick(View view) {
                                    if (currentSelection != null) {
                                        if (currentSelection.equals(s)) return;
                                        RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("polls").child(RuntimeDatastore.currentPost).child("options").child(currentSelection).setValue((long) data.get(currentSelection) - 1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override public void onComplete(@NonNull Task<Void> task) {
                                                if (!task.isSuccessful()) Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong) : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                    RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("polls").child(RuntimeDatastore.currentPost).child("options").child(s).setValue((long) data.get(s) + 1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override public void onComplete(@NonNull Task<Void> task) {
                                            if (!task.isSuccessful()) Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong) : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("polls").child(RuntimeDatastore.currentPost).child("votes").child(RuntimeDatastore.auth.getUid()).setValue(s).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override public void onComplete(@NonNull Task<Void> task) {
                                            if (!task.isSuccessful()) Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong) : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    currentSelection = s;
                                }
                            });
                            tb.addView(tv);
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                };
                ref = RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("polls").child(RuntimeDatastore.currentPost).child("options");
                ref.addValueEventListener(listener);
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override protected void onDestroy() {
        if (ref != null && listener != null) ref.removeEventListener(listener);
        super.onDestroy();
    }
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.backbar, menu);
        return true;
    }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.back:
                startActivity(new Intent(getApplicationContext(), ListPollsActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
