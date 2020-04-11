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
import android.widget.LinearLayout;
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
public class SuggestionActivity extends AppCompatActivity {
    private String parent;
    private long myCurrentVote, postCurrentScore, userCurrentPrivateScore, userCurrentPublicScore;
    private boolean userExists, adminLiked;
    private static ValueEventListener v1, v2, v3, v4;
    private static DatabaseReference ref1, ref2, ref3, ref4;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestion);
        Toolbar appbar = findViewById(R.id.suggestionbar);
        appbar.setTitleTextColor(getResources().getColor(R.color.colorText, null));
        setSupportActionBar(appbar);
        loadUserInterface();
    }
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        if (RuntimeDatastore.currentPermissions >= RuntimeDatastore.PERMISSION_ADMIN) getMenuInflater().inflate(R.menu.suggestionadbar, menu);
        else getMenuInflater().inflate(R.menu.suggestionbar, menu);
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
            case R.id.addsuggestion:
                startActivity(new Intent(getApplicationContext(), AddSuggestionActivity.class));
                return true;
            case R.id.adminsuggestions:
                startActivity(new Intent(getApplicationContext(), AdminSuggestionsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void loadUserInterface() {
        if (ref1 != null && v1 != null) ref1.removeEventListener(v1);
        if (ref2 != null && v2 != null) ref2.removeEventListener(v2);
        if (ref3 != null && v3 != null) ref3.removeEventListener(v3);
        if (ref4 != null && v4 != null) ref4.removeEventListener(v4);
        LinearLayout lin = findViewById(R.id.buttonbar);
        if (lin.getChildCount() == 4) lin.removeViewAt(3);
        v1 = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) myCurrentVote = 0;
                else myCurrentVote = (long) dataSnapshot.getValue();
                Button up = findViewById(R.id.suggestionupvote), down = findViewById(R.id.suggestiondownvote);
                if (myCurrentVote > 0) up.setBackgroundColor(getResources().getColor(R.color.colorAppBar, null));
                else up.setBackgroundColor(getResources().getColor(R.color.colorButtons, null));
                if (myCurrentVote < 0) down.setBackgroundColor(getResources().getColor(R.color.colorAppBar, null));
                else down.setBackgroundColor(getResources().getColor(R.color.colorButtons, null));
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        ref1 = RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("suggestions").child(RuntimeDatastore.currentPost).child("votes").child(RuntimeDatastore.auth.getUid());
        ref1.addValueEventListener(v1);
        RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("suggestions").child(RuntimeDatastore.currentPost).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressWarnings("unchecked")
            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final HashMap<String, Object> postdata, img, children;
                if (dataSnapshot.getValue() == null) postdata = new HashMap<>();
                else postdata = (HashMap<String, Object>) dataSnapshot.getValue();
                parent = (String) postdata.get("parent");
                final TextView post = findViewById(R.id.suggestionrootpost), username = findViewById(R.id.suggestionuser);
                post.setText(postdata.get("text") != null ? (String) postdata.get("text") : "");
                TableLayout images = findViewById(R.id.suggestionrootimages), comments = findViewById(R.id.suggestioncomments);
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
                            TextView votes = findViewById(R.id.suggestionvotes);
                            votes.setText(String.format(Locale.ENGLISH, "%d", postCurrentScore / 5000));
                        }
                        @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    };
                    ref3 = RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("suggestions").child(parent).child("children").child(RuntimeDatastore.currentPost).child("postScore");
                    ref3.addValueEventListener(v3);
                    if (RuntimeDatastore.currentPermissions >= RuntimeDatastore.PERMISSION_ADMIN) {
                        LinearLayout lin = findViewById(R.id.buttonbar);
                        final Button button = new Button(getApplicationContext());
                        button.setBackgroundColor(getResources().getColor(R.color.colorButtons, null));
                        button.setTextColor(getResources().getColor(R.color.colorBackground, null));
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.setMargins((int) (10 * RuntimeDatastore.dpRatio), 0, 0, 0);
                        button.setLayoutParams(params);
                        button.setText(getString(R.string.in_consideration));
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override public void onClick(View view) {
                                if (adminLiked) {
                                    ref4.setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override public void onComplete(@NonNull Task<Void> task) {
                                            if (!task.isSuccessful()) Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    adminLiked = false;
                                } else {
                                    ref4.setValue(post.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override public void onComplete(@NonNull Task<Void> task) {
                                            if (!task.isSuccessful()) Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    adminLiked = true;
                                }
                            }
                        });
                        lin.addView(button);
                        v4 = new ValueEventListener() {
                            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() == null) button.setBackgroundColor(getResources().getColor(R.color.colorButtons, null));
                                else button.setBackgroundColor(getResources().getColor(R.color.colorAppBar, null));
                                adminLiked = dataSnapshot.getValue() != null;
                            }
                            @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        };
                        ref4 = RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("adminSuggestions").child(RuntimeDatastore.currentPost);
                        ref4.addValueEventListener(v4);
                    }
                } else {
                    username.setText(getString(R.string.app_name));
                    TextView votes = findViewById(R.id.suggestionvotes);
                    votes.setText(String.format(Locale.ENGLISH, "%d", 0));
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void downvoteSuggestion(View view) {
        if (!RuntimeDatastore.currentPost.equals("root")) {
            if (myCurrentVote > 0) upvoteSuggestion(view);
            if (myCurrentVote != 0) {
                changeScore(-myCurrentVote);
                myCurrentVote = 0;
            } else changeScore(RuntimeDatastore.min(-1, -RuntimeDatastore.myCurrentScore / 169));
        } else Toast.makeText(getApplicationContext(), getString(R.string.vote_on_root), Toast.LENGTH_SHORT).show();
    }
    public void upvoteSuggestion(View view) {
        if (!RuntimeDatastore.currentPost.equals("root")) {
            if (myCurrentVote < 0) downvoteSuggestion(view);
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
            ref2.child("score_total").setValue(RuntimeDatastore.max(0, userCurrentPrivateScore + userCurrentPublicScore + changeScore + 1000000)).addOnCompleteListener(new OnCompleteListener<Void>() {
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