package com.tmspl.trackingapp.firebasemodel;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tmspl.trackingapp.extras.AppUtils;
import com.tmspl.trackingapp.extras.Log;
import com.tmspl.trackingapp.extras.Util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rakshit.sathwara on 1/12/2017.
 */

public class UserModel {

    public static final String TAG = AppUtils.APP_TAG + UserModel.class.getSimpleName();

    private String user_id, name, password, role;

    public UserModel() {
    }

    public UserModel(String user_id, String name, String password, String role) {
        this.user_id = user_id;
        this.name = name;
        this.password = Util.MD5(password);
        this.role = role;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public static UserModel fromMap(String userId, final Map<String, Object> map) {
        return new UserModel(userId, map.get("password").toString(), map.get("name").toString(),
                map.get("role").toString());
    }

    public static void getUserList(final GetUserListListener userListListener) {
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.getRoot();
        dbRef.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userListListener.onGetUserList((Map<String, Object>) dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public interface GetUserListListener {
        void onGetUserList(Map<String, Object> userListMap);
    }


    public static void register(final DatabaseReference dbRef, final String name,
                                final String password, final String role,
                                final UserRegistrationCallback userRegistrationCallback) {

        Log.i(TAG, "INSIDE REGISTER FUNCTION ");

        dbRef.getRoot();
        checkIfUserExist(dbRef, name, new UserExistCheckCallback() {
            @Override
            public void onCheckComplete(DataSnapshot dataSnapshot, int existStatus) {
                Log.i(TAG, "INSIDE ON CHECK COMPLETE LISTENER FOR CHECK IF USER EXIST");
                if (existStatus == UserExistCheckCallback.NOT_EXIST) {
                    Log.i(TAG, "REGISTERING USER");
                    //REGISTER USER
                    dbRef.getRoot();
                    Map<String, String> userDataMap = new HashMap<>();
                    userDataMap.put("name", name);
                    userDataMap.put("password", Util.MD5(password));
                    userDataMap.put("role", role);
                    Map<String, Object> newUser = new HashMap();
                    newUser.put("/user/" + name + "/", userDataMap);
                    dbRef.updateChildren(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.i(TAG, "ON COMPLETION OF REGESTERING USER");
                            if (task.isSuccessful()) {
                                Log.i(TAG, "SUCCESSFULLY REGISTERED");
                                userRegistrationCallback.onRegistrationComplete(UserRegistrationCallback.REGISTERED);
                            } else {
                                Log.i(TAG, "FAILED TO REGISTER");
                                userRegistrationCallback.onRegistrationComplete(UserRegistrationCallback.FAILED);
                            }
                        }
                    });
                } else if (existStatus == UserExistCheckCallback.EXIST) {
                    Log.i(TAG, "DUPLICATE USER");
                    userRegistrationCallback.onRegistrationComplete(UserRegistrationCallback.DUPLICATE);
                }
            }
        });
    }

    private static void checkIfUserExist(DatabaseReference dbRef, final String userId, final UserExistCheckCallback callback) {
        Log.i(TAG, "INSIDE CHECK IF USER EXIST FUNCTION ");
        dbRef.getRoot();
        dbRef.child("user").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "ON DATA CHANGED LISTENER FROM CHECK IF USER EXIST FUNCTION");
                if (dataSnapshot.exists()) {
                    Log.i(TAG, "USER EXIST");
                    callback.onCheckComplete(dataSnapshot, UserExistCheckCallback.EXIST);
                } else {
                    Log.i(TAG, "USER NOT EXIST");
                    callback.onCheckComplete(dataSnapshot, UserExistCheckCallback.NOT_EXIST);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public interface UserExistCheckCallback {
        int EXIST = 1;
        int NOT_EXIST = 0;

        void onCheckComplete(DataSnapshot dataSnapshot, int existStatus);
    }

    public interface UserRegistrationCallback {
        int REGISTERED = 1;
        int FAILED = 0;
        int DUPLICATE = -1;

        void onRegistrationComplete(int registrationStatus);
    }

    public interface LoginResponseCallback {
        int SUCCESS = 1;
        int FAILED = 0;

        void onLoginResult(DataSnapshot dataSnapshot, int loginResult);
    }

    public static void tryLogin(final DatabaseReference dbRef, final String userId, final String password,
                                final LoginResponseCallback callback) {

        checkIfUserExist(dbRef, userId, new UserExistCheckCallback() {
            @Override
            public void onCheckComplete(DataSnapshot dataSnapshot, int existStatus) {
                if (existStatus == UserExistCheckCallback.EXIST) {
                    final Map map = (Map) dataSnapshot.getValue();
                    if (map != null && map.size() != 0) {
                        final String pwd = map.get("password").toString();
                        if (Util.MD5(password).equals(pwd)) {
                            callback.onLoginResult(dataSnapshot, LoginResponseCallback.SUCCESS);
                        } else {
                            callback.onLoginResult(dataSnapshot, LoginResponseCallback.FAILED);
                        }
                    } else {
                        callback.onLoginResult(dataSnapshot, LoginResponseCallback.FAILED);
                    }
                } else {
                    callback.onLoginResult(dataSnapshot, LoginResponseCallback.FAILED);
                }
            }
        });

    }

}
