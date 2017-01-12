package com.tmspl.trackingapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.tmspl.trackingapp.R;
import com.tmspl.trackingapp.extras.AppUtils;
import com.tmspl.trackingapp.extras.Log;
import com.tmspl.trackingapp.firebasemodel.UserModel;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = AppUtils.APP_TAG + RegisterActivity.class.getSimpleName();

    @BindView(R.id.et_register_user_name)
    EditText etResisterUserName;
    @BindView(R.id.et_register__password)
    EditText etRegisterPassword;
    @BindView(R.id.sp_role)
    Spinner spRole;
    @BindView(R.id.btn_register_reg)
    Button btnRegisterReg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        List<String> role = new LinkedList<>();
        role.add("User");
        role.add("Rider");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, role);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spRole.setAdapter(dataAdapter);

        btnRegisterReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.i(TAG, "SIGN UP CLICKED");

                int spinnerIndex = spRole.getSelectedItemPosition();

                UserModel.register(FirebaseDatabase.getInstance().getReference(),
                        etResisterUserName.getText().toString().trim().toLowerCase(),
                        etRegisterPassword.getText().toString().trim().toLowerCase(),
                        spRole.getSelectedItem().toString(),
                        new UserModel.UserRegistrationCallback() {
                            @Override
                            public void onRegistrationComplete(int registrationStatus) {
                                if (registrationStatus == UserModel.UserRegistrationCallback.REGISTERED) {
                                    Toast.makeText(RegisterActivity.this, "Registered.!!!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                    finish();
                                } else if (registrationStatus == UserModel.UserRegistrationCallback.DUPLICATE) {
                                    Toast.makeText(RegisterActivity.this, "Duplicate User id.!!!", Toast.LENGTH_SHORT).show();
                                } else if (registrationStatus == UserModel.UserRegistrationCallback.FAILED) {
                                    Toast.makeText(RegisterActivity.this, "Failed.!!!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}
