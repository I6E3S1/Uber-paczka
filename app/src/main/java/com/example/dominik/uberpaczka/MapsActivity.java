package com.example.dominik.uberpaczka;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class MapsActivity extends FragmentActivity implements
        OnMyLocationButtonClickListener,
        OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    TextView textView;

    public HashMap<Integer, String> getHash() {
        return hash;
    }

    HashMap<Integer, String> hash = new HashMap<>();
    GoogMatrixRequest googMatrixRequest;
    /**
     * TODO
     * OBSŁUG KILKU MARKERÓW
     * FRONT
     */


    private boolean mPermissionDenied = false;
    private GoogleMap mMap;
    private String TAG = "MAPS";
    private int inc = 0;
    private PlaceAutocompleteFragment autocompleteFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

//        textView = findViewById(R.id.departure);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);

        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                inc++;

                String g = String.valueOf(place.getName());

                Geocoder geocoder = new Geocoder(getBaseContext());
                List<Address> addresses;

                try {
                    addresses = geocoder.getFromLocationName(g, 3);
                    if (addresses != null && !addresses.equals("")) {
                        String res = search(addresses, mMap);
                        hash.put(inc, res);
                        Log.i(TAG, "Hash map: " +hash.get(1));
                        //textView.setText(res);
                    }

                } catch (Exception e) {
                    Log.i(TAG, "geocoder error: " + g);
                }

                Log.i(TAG, "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });

//        final Button drvier = findViewById(R.id.driver);
//
//        drvier.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//
//                Thread thread = new Thread(new Runnable() {
//
//                    @SuppressLint("SetTextI18n")
//                    @Override
//                    public void run() {
//                        try {
//                            googMatrixRequest = new GoogMatrixRequest();
//                            textView = findViewById(R.id.arrival);
//                            googMatrixRequest.setStr_from(hash.get(1));
//                            googMatrixRequest.setStr_to(hash.get(2));
//                            Log.i("MAPSTEST", "1");
//                            Long distance = googMatrixRequest.transfer();
//                            sleep(3000);
//                            Log.i("MAPSTEST", "Result" + distance);
//                            textView.setText("" + distance);
//
//                        } catch (Exception e) {
//                            Log.i("MAPSTEST", "Blad thread");
//                            e.printStackTrace();
//                        }
//                    }
//                });
//
//                thread.start();
////                android.content.Intent myIntent = new android.content.Intent(v.getContext(), MainActivity.class);
////                startActivity(myIntent);
//
//
//            }
//        });


        /**
         * showing summary fragment on top of google maps screen
         */

        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                Bundle bundle=new Bundle();
                bundle.putString("From", hash.get(1) );
                bundle.putString("Destination", hash.get(2));
                SummaryFragment fragment = new SummaryFragment();
                fragment.setArguments(bundle);
                fragmentTransaction.add(R.id.summary_container, fragment);
                fragmentTransaction.commit();
                view.setVisibility(View.GONE);
            }
        });

    }

    protected String search(List<Address> addresses, GoogleMap map) {

        map.clear();

        String addressText;
        Address address = addresses.get(0);
        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

        addressText = String.format(
                "%s, %s",
                address.getMaxAddressLineIndex() > 0 ? address
                        .getAddressLine(0) : "", address.getCountryName());


        MarkerOptions markerOptions = new MarkerOptions();


        markerOptions.position(latLng);
        markerOptions.title(addressText);

        map.clear();
        map.addMarker(markerOptions);
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        map.animateCamera(CameraUpdateFactory.zoomTo(15));


        return address.getFeatureName();

    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        LatLng warsaw = new LatLng(52.227, 21.021);
        map.moveCamera(CameraUpdateFactory.newLatLng(warsaw));
        map.animateCamera(CameraUpdateFactory.zoomTo(10));
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }


    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

}
