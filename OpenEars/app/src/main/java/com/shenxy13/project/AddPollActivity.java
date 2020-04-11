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
import android.widget.TableLayout;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
public class AddPollActivity extends AppCompatActivity {
    private ArrayList<EditText> children;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_poll);
        Toolbar appbar = findViewById(R.id.addpollbar);
        appbar.setTitleTextColor(getResources().getColor(R.color.colorText, null));
        setSupportActionBar(appbar);
        children = new ArrayList<>();
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
    public void addPollOption(View view) {
        TableLayout t = findViewById(R.id.addpolloptions);
        EditText editText = new EditText(getApplicationContext());
        editText.setHintTextColor(getResources().getColor(R.color.colorText, null));
        editText.setTextColor(getResources().getColor(R.color.colorText, null));
        editText.setHint(String.format(Locale.ENGLISH, "%s%d", getString(R.string.poll_option), t.getChildCount() + 1));
        t.addView(editText);
        children.add(editText);
    }
    public void removePollOption(View view) {
        if (children.size() == 0) return;
        TableLayout t = findViewById(R.id.addpolloptions);
        t.removeViewAt(t.getChildCount() - 1);
        children.remove(children.size() - 1);
    }
    public void createPoll(View view) {
        EditText title = findViewById(R.id.addpollquestion);
        final String key = RuntimeDatastore.generateKey(), question = title.getText().toString();
        if (question.equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.empty_error), Toast.LENGTH_SHORT).show();
            return;
        }
        RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("polls").child("pollslist").child(key).setValue(question).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    HashMap<String, Long> opts = new HashMap<>();
                    for (EditText e: children) opts.put(e.getText().toString(), 0L);
                    opts.remove("");
                    RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("polls").child(key).child("options").setValue(opts).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) startActivity(new Intent(getApplicationContext(), ListPollsActivity.class));
                            else Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
