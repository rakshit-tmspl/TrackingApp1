package com.tmspl.trackingapp.activity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tmspl.trackingapp.R;
import com.tmspl.trackingapp.extras.App;
import com.tmspl.trackingapp.firebasemodel.GettingLocation;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RiderHomeActivity extends FragmentActivity implements OnMapReadyCallback,
        LocationListener {

    @BindView(R.id.btn_one)
    Button btnOne;
    @BindView(R.id.btn_two)
    Button btnTwo;
    @BindView(R.id.btn_three)
    Button btnThree;
    private GoogleMap mMap;
    Location mLastLocation;
    Marker mCurrLocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_home);
        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final String uid = ((App) getApplication()).getUserModel().getName();



        if (!uid.isEmpty()) {
            btnOne.setOnClickListener(new AddLocation(this, uid));
            btnTwo.setOnClickListener(new AddlawGardenLocation(this, uid));
            btnThree.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getRiderLocation(uid);
                }
            });
        }

    }

    private void getRiderLocation(String uid) {

        if (!uid.isEmpty()) {
            GettingLocation.getLatLng(uid, new GettingLocation.OnGetLocationCallback() {
                @Override
                public void onGetNotesSuccess(GettingLocation gettingLocation) {
                    LatLng latLng = new LatLng(Double.valueOf(gettingLocation.getLat()).doubleValue(),
                            Double.valueOf(gettingLocation.getLng()).doubleValue());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title("Current Position");
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                    mCurrLocationMarker = mMap.addMarker(markerOptions);
                }

                @Override
                public void onGetNotesFailed(DatabaseError databaseError) {

                }
            });
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        } else {
            mMap.setMyLocationEnabled(true);
        }
    }


    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;


        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    class AddLocation implements View.OnClickListener, GettingLocation.OnAddLatLngCallback {

        String uid;
        Context context;

        public AddLocation(final Context context, final String uid) {
            this.context = context;
            this.uid = uid;
        }

        @Override
        public void onClick(View view) {
            final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
            final String lat = "23.0203135";
            final String lng = "72.5539719";

            if (!lat.isEmpty() && !lng.isEmpty()) {
                GettingLocation.addLatLng(new GettingLocation(lat, lng),
                        uid, dbRef, this);
            } else {
                Toast.makeText(RiderHomeActivity.this, "please enter Location", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onLocationAdded(Task task, CallbackStatus status) {
            if (status == CallbackStatus.SUCCESS) {
                Toast.makeText(context, "Location added successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "failed to add location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class AddlawGardenLocation implements View.OnClickListener, GettingLocation.OnAddLatLngCallback {
        String uid;
        Context context;

        public AddlawGardenLocation(final Context context, final String uid) {
            this.context = context;
            this.uid = uid;
        }

        @Override
        public void onClick(View view) {
            final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
            final String lat = "23.0261137";
            final String lng = "72.5612102";

            if (!lat.isEmpty() && !lng.isEmpty()) {
                GettingLocation.addLatLng(new GettingLocation(lat, lng),
                        uid, dbRef, this);
            } else {
                Toast.makeText(RiderHomeActivity.this, "please enter Location", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onLocationAdded(Task task, CallbackStatus status) {
            if (status == CallbackStatus.SUCCESS) {
                Toast.makeText(context, "Location added successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "failed to add location", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
