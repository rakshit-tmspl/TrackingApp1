package com.tmspl.trackingapp.extras;

import android.app.Application;

import com.tmspl.trackingapp.firebasemodel.UserModel;

/**
 * Created by Rakshit on 13-01-2017 at 12:24.
 */

public class App extends Application {

    public static final String TAG = AppUtils.APP_TAG + App.class.getSimpleName();

    private UserModel userModel;

    public UserModel getUserModel() {
        return userModel;
    }

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }
}
