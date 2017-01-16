package com.tmspl.trackingapp.activity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tmspl.trackingapp.R;
import com.tmspl.trackingapp.extras.App;
import com.tmspl.trackingapp.extras.AppUtils;
import com.tmspl.trackingapp.firebasemodel.UserModel;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = AppUtils.APP_TAG + MainActivity.class.getName();

    @BindView(R.id.et_user_name)
    EditText etUserName;
    @BindView(R.id.et_password)
    EditText etPassword;
    @BindView(R.id.btn_login)
    Button btnLogin;
    @BindView(R.id.btn_register)
    Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String userName = etUserName.getText().toString().trim();
                final String password = etPassword.getText().toString().trim();

                if (userName.isEmpty() && password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "enter valid user name and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                UserModel.tryLogin(FirebaseDatabase.getInstance().getReference(),
                        userName, password,
                        new UserModel.LoginResponseCallback() {
                            @Override
                            public void onLoginResult(DataSnapshot dataSnapshot, int loginResult) {

                                if (loginResult == SUCCESS) {

                                    String role = String.valueOf(dataSnapshot.child("role").getValue());
                                    if (role.equals("Rider")) {
                                        Intent intent = new Intent(MainActivity.this, RiderHomeActivity.class);
                                        ((App) getApplication()).setUserModel(UserModel.fromMap(dataSnapshot.getKey(),
                                                (Map<String, Object>) dataSnapshot.getValue()));
                                        intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    } else {
                                        startActivity(new Intent(MainActivity.this, UserHomeActivity.class));
                                    }
                                }

                            }
                        });
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });
    }
}
