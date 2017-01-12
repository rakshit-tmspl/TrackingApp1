package com.tmspl.trackingapp.extras;

/**
 * Created by rakshit.sathwara on 1/12/2017.
 */

public class Log {

    static boolean showLog = true;

    public static void i(final String TAG, final String msg) {
        if (showLog) {
            android.util.Log.i(TAG, msg);
        }
    }
}
