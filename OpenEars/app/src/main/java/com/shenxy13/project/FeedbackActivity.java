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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
public class FeedbackActivity extends AppCompatActivity {
    private String parent;
    private long myCurrentVote, postCurrentScore, userCurrentPublicScore, userCurrentPrivateScore;
    private boolean userExists;
    private static ValueEventListener v1, v2, v3;
    private static DatabaseReference ref1, ref2, ref3;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        Toolbar appbar = findViewById(R.id.feedbackbar);
        appbar.setTitleTextColor(getResources().getColor(R.color.colorText, null));
        setSupportActionBar(appbar);
        loadUserInterface();
    }
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feedbackbar, menu);
        return true;
    }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.back:
                if (RuntimeDatastore.currentPost.equals("root")) {
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    finish();
                } else {
                    RuntimeDatastore.currentPost = parent;
                    loadUserInterface();
                }
                return true;
            case R.id.addfeedback:
                startActivity(new Intent(getApplicationContext(), AddFeedbackActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void loadUserInterface() {
        if (ref1 != null && v1 != null) ref1.removeEventListener(v1);
        if (ref2 != null && v2 != null) ref2.removeEventListener(v2);
        if (ref3 != null && v3 != null) ref3.removeEventListener(v3);
        v1 = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) myCurrentVote = 0;
                else myCurrentVote = (long) dataSnapshot.getValue();
                Button up = findViewById(R.id.feedbackupvote), down = findViewById(R.id.feedbackdownvote);
                if (myCurrentVote > 0) up.setBackgroundColor(getResources().getColor(R.color.colorAppBar, null));
                else up.setBackgroundColor(getResources().getColor(R.color.colorButtons, null));
                if (myCurrentVote < 0) down.setBackgroundColor(getResources().getColor(R.color.colorAppBar, null));
                else down.setBackgroundColor(getResources().getColor(R.color.colorButtons, null));
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        ref1 = RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("feedback").child(RuntimeDatastore.currentPost).child("votes").child(RuntimeDatastore.auth.getUid());
        ref1.addValueEventListener(v1);
        RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("feedback").child(RuntimeDatastore.currentPost).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressWarnings("unchecked")
            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final HashMap<String, Object> postdata, img, children;
                if (dataSnapshot.getValue() == null) postdata = new HashMap<>();
                else postdata = (HashMap<String, Object>) dataSnapshot.getValue();
                parent = (String) postdata.get("parent");
                final TextView post = findViewById(R.id.feedbackrootpost), username = findViewById(R.id.feedbackuser);
                post.setText(postdata.get("text") != null ? (String) postdata.get("text") : "");
                TableLayout images = findViewById(R.id.feedbackrootimages), comments = findViewById(R.id.feedbackcomments);
                images.removeAllViews();
                if (postdata.get("images") == null) img = new HashMap<>();
                else img = (HashMap<String, Object>) postdata.get("images");
                for (String s: img.keySet()) {
                    ImageView imv = new ImageView(getApplicationContext());
                    TableLayout.LayoutParams params = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, (int) (5 * RuntimeDatastore.dpRatio), 0, 0);
                    imv.setLayoutParams(params);
                    imv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    Glide.with(getApplicationContext()).load(RuntimeDatastore.store.getReference().child(RuntimeDatastore.currentOrganisation).child(s)).into(imv);
                    images.addView(imv);
                }
                if (postdata.get("children") == null) children = new HashMap<>();
                else children = (HashMap<String, Object>) postdata.get("children");
                comments.removeAllViews();
                ArrayList<PostComment> why = new ArrayList<>();
                for (String s: children.keySet()) {
                    HashMap<String, Object> data = (HashMap<String, Object>) children.get(s);
                    why.add(new PostComment(s, (String) data.get("text"), (long) data.get("postScore")));
                }
                Collections.sort(why);
                for (final PostComment c: why) {
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
                    disName.setText(c.getText());
                    card.addView(disName);
                    card.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View view) {
                            RuntimeDatastore.currentPost = c.getPostId();
                            loadUserInterface();
                        }
                    });
                    comments.addView(card);
                }
                if (postdata.get("poster") != null) {
                    v2 = new ValueEventListener() {
                        @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            HashMap<String, Object> userdata;
                            if (dataSnapshot.getValue() == null) userdata = new HashMap<>();
                            else userdata = (HashMap<String, Object>) dataSnapshot.getValue();
                            userExists = dataSnapshot.getValue() != null;
                            username.setText((String) userdata.get("displayName"));
                            if (!userExists) return;
                            userCurrentPrivateScore = (long) userdata.get("score_secret");
                            userCurrentPublicScore = (long) userdata.get("score_public");
                        }
                        @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    };
                    ref2 = RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("users").child((String) postdata.get("poster"));
                    ref2.addValueEventListener(v2);
                    username.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View view) {
                            RuntimeDatastore.displayedUser = (String) postdata.get("poster");
                            startActivity(new Intent(getApplicationContext(), ViewUserActivity.class));
                        }
                    });
                    v3 = new ValueEventListener() {
                        @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() == null) postCurrentScore = 0;
                            else postCurrentScore = (long) dataSnapshot.getValue();
                            TextView votes = findViewById(R.id.feedbackvotes);
                            votes.setText(String.format(Locale.ENGLISH, "%d", postCurrentScore / 5000));
                        }
                        @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    };
                    ref3 = RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("feedback").child(parent).child("children").child(RuntimeDatastore.currentPost).child("postScore");
                    ref3.addValueEventListener(v3);
                } else {
                    username.setText(getString(R.string.app_name));
                    TextView votes = findViewById(R.id.feedbackvotes);
                    votes.setText(String.format(Locale.ENGLISH, "%d", 0));
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void downvoteFeedback(View view) {
        if (!RuntimeDatastore.currentPost.equals("root")) {
            if (myCurrentVote > 0) upvoteFeedback(view);
            if (myCurrentVote != 0) {
                changeScore(-myCurrentVote);
                myCurrentVote = 0;
            } else changeScore(RuntimeDatastore.min(-1, -RuntimeDatastore.myCurrentScore / 169));
        } else Toast.makeText(getApplicationContext(), getString(R.string.vote_on_root), Toast.LENGTH_SHORT).show();
    }
    public void upvoteFeedback(View view) {
        if (!RuntimeDatastore.currentPost.equals("root")) {
            if (myCurrentVote < 0) downvoteFeedback(view);
            if (myCurrentVote != 0) {
                changeScore(-myCurrentVote);
                myCurrentVote = 0;
            } else changeScore(RuntimeDatastore.max(1, RuntimeDatastore.myCurrentScore / 169));
        } else Toast.makeText(getApplicationContext(), getString(R.string.vote_on_root), Toast.LENGTH_SHORT).show();
    }
    private void changeScore(long changeScore) {
        ref1.setValue(myCurrentVote + changeScore).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
        ref3.setValue(postCurrentScore + changeScore).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
        if (userExists) {
            ref2.child("score_total").setValue(RuntimeDatastore.max(0, userCurrentPrivateScore + userCurrentPublicScore + changeScore)).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()) Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                }
            });
            ref2.child("score_public").setValue(userCurrentPublicScore + changeScore).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()) Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}