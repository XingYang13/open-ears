package com.shenxy13.project;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
public class AddSuggestionActivity extends AppCompatActivity {
    final private static int RESULT_LOAD_IMAGE = 13;
    private ArrayList<File> imageFiles;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_suggestion);
        Toolbar appbar = findViewById(R.id.addsuggestionbar);
        appbar.setTitleTextColor(getResources().getColor(R.color.colorText, null));
        setSupportActionBar(appbar);
        imageFiles = new ArrayList<>();
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
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            imageFiles.add(new File(picturePath));
            TableLayout imgs = findViewById(R.id.addsuggestionimages);
            ImageView imv = new ImageView(getApplicationContext());
            TableLayout.LayoutParams params = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, (int) (5 * RuntimeDatastore.dpRatio), 0, 0);
            imv.setLayoutParams(params);
            imv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imv.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            imgs.addView(imv);
        }
    }
    public void addImageToSuggestion(View view) {
        startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), RESULT_LOAD_IMAGE);
    }
    public void removeImageFromSuggestion(View view) {
        if (imageFiles.size() == 0) return;
        TableLayout imgs = findViewById(R.id.addsuggestionimages);
        imgs.removeViewAt(imgs.getChildCount() - 1);
        imageFiles.remove(imageFiles.size() - 1);
    }
    public void submitSuggestion(View view) {
        final HashMap<String, Object> postdata = new HashMap<>(), images = new HashMap<>(), childdata = new HashMap<>();
        for (File file: imageFiles) {
            String key = RuntimeDatastore.generateKey();
            images.put(key, true);
            RuntimeDatastore.store.getReference().child(RuntimeDatastore.currentOrganisation).child(key).putFile(Uri.fromFile(file)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (!task.isSuccessful()) Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                }
            });
        }
        postdata.put("images", images);
        EditText text = findViewById(R.id.addsuggestiontext);
        final String ptext = text.getText().toString(), key = RuntimeDatastore.generateKey();
        postdata.put("text", ptext);
        postdata.put("poster", RuntimeDatastore.auth.getUid());
        postdata.put("parent", RuntimeDatastore.currentPost);
        childdata.put("text", ptext);
        childdata.put("postScore", RuntimeDatastore.myCurrentScore / 500);
        RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("suggestions").child(RuntimeDatastore.currentPost).child("children").child(key).setValue(childdata).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    RuntimeDatastore.database.getReference().child("organisations").child(RuntimeDatastore.currentOrganisation).child("suggestions").child(key).setValue(postdata).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                RuntimeDatastore.database.getReference().child("users").child(RuntimeDatastore.auth.getUid()).child("organisations").child(RuntimeDatastore.currentOrganisation).child("posts").child("s" + key).setValue(ptext).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            RuntimeDatastore.currentPost = key;
                                            startActivity(new Intent(getApplicationContext(), SuggestionActivity.class));
                                            finish();
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