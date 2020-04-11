package com.shenxy13.project;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
public class SignInActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
    }
    public void signIn(View view) {
        EditText email = findViewById(R.id.signinemail), password = findViewById(R.id.signinpassword);
        String em = email.getText().toString(), pw = password.getText().toString();
        if (em.equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.empty_error), Toast.LENGTH_SHORT).show();
            return;
        }
        RuntimeDatastore.auth.signInWithEmailAndPassword(em, pw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) startActivity(new Intent(getApplicationContext(), OrganisationListActivity.class));
                else Toast.makeText(getApplicationContext(), (task.getException() == null ? getString(R.string.somethingwrong)  : task.getException().getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void goToRegister(View view) {
        startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
    }
}