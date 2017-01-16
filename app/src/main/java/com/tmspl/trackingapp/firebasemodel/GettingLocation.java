package com.tmspl.trackingapp.firebasemodel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.support.annotation.NonNull;

import com.tmspl.trackingapp.extras.AppUtils;
import com.tmspl.trackingapp.extras.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Rakshit on 13-01-2017 at 12:39.
 */

public class GettingLocation {

    private static final String TAG = AppUtils.APP_TAG + GettingLocation.class.getSimpleName();

    String lat, lng;

    public String getLat() {
        return lat;
    }

    public String getLng() {
        return lng;
    }

    public GettingLocation(String lat, String lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public static GettingLocation fromMap(final Map<String, String> latLngData) {
        return new GettingLocation(latLngData.get("lat"),
                latLngData.get("lng"));
    }

    private Map toMap() {
        return new HashMap() {
            {
                put("lat", lat);
                put("lng", lng);
            }
        };
    }

    public static void addLatLng(final GettingLocation gettingLocation, final String uid, final DatabaseReference dbRef,
                                 final OnAddLatLngCallback callback) {
        dbRef.getRoot();
        final String key = dbRef.child("location").push().getKey();
        final Map childUpdates = new HashMap();

        childUpdates.put("/location/" + key + "/", gettingLocation.toMap());
        childUpdates.put("rider-location/" + uid + "/" + key + "/", key);

        dbRef.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    callback.onLocationAdded(task, OnAddLatLngCallback.CallbackStatus.SUCCESS);
                } else {
                    callback.onLocationAdded(task, OnAddLatLngCallback.CallbackStatus.FAILED);
                }
            }
        });
    }


    public static void getLatLng(String userId, final OnGetLocationCallback callback) {
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.getRoot();

        dbRef.child("rider-location").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "OnDataChange :" + dataSnapshot.getValue());

                Map latLngKeysMap = (Map) dataSnapshot.getValue();

                Set<Map.Entry<Object, String>> locationKeysIterator = latLngKeysMap.entrySet();
                Iterator<Map.Entry<Object, String>> noteKeysIterator = locationKeysIterator.iterator();
                final List<String> latLngKeys = new LinkedList<String>();

                while (noteKeysIterator.hasNext()) {
                    latLngKeys.add(noteKeysIterator.next().getValue());
                }

                dbRef.getRoot();

                for (String latLngKey : latLngKeys) {
                    dbRef.child("location").child(latLngKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            callback.onGetNotesSuccess(fromMap((Map<String, String>) dataSnapshot.getValue()));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            callback.onGetNotesFailed(databaseError);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onGetNotesFailed(databaseError);
            }
        });
    }

    public interface OnAddLatLngCallback {
        enum CallbackStatus {
            SUCCESS,
            FAILED,
        }

        void onLocationAdded(Task task, CallbackStatus status);

    }

    public interface OnGetLocationCallback {

        void onGetNotesSuccess(GettingLocation gettingLocation);

        void onGetNotesFailed(DatabaseError databaseError);

    }
}
