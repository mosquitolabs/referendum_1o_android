package com.referendum.uoctubre.fragments;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidmapsextensions.ClusteringSettings;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.androidmapsextensions.OnMapReadyCallback;
import com.androidmapsextensions.SupportMapFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.referendum.uoctubre.BuildConfig;
import com.referendum.uoctubre.R;
import com.referendum.uoctubre.main.Constants;
import com.referendum.uoctubre.model.ColegiElectoral;
import com.referendum.uoctubre.utils.MapClusterOptionsProvider;

import java.lang.reflect.Type;
import java.util.List;


public class VoteFragment extends BaseFragment implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    public static final String TAG = "vota_fragment";

    public static final int LOCATION_PERMISSION_ID = 1001;
    private static final int GPS = 10;

    private BottomSheetBehavior bsb;
    private TextView txtLocalitat, txtAdresa, txtNomLocal, txtNoPunts;

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mGoogleMap;

    private List<ColegiElectoral> mColegiElectorals;
    private ColegiElectoral mSelectedColegiElectoral;

    public static VoteFragment newInstance() {
        Bundle args = new Bundle();

        VoteFragment fragment = new VoteFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vote, container, false);

        SupportMapFragment mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);

        LinearLayout bottomSheet = view.findViewById(R.id.bottomsheet);
        txtLocalitat = bottomSheet.findViewById(R.id.txtLocalitat);
        txtAdresa = bottomSheet.findViewById(R.id.txtAdresa);
        txtNomLocal = bottomSheet.findViewById(R.id.txtNomLocal);
        txtNoPunts = view.findViewById(R.id.txtNoPunts);

        bsb = BottomSheetBehavior.from(bottomSheet);

        mMapFragment.getExtendedMapAsync(this);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GPS) {
            Handler gpsHandler = new Handler();
            gpsHandler.postDelayed(new Runnable() {
                public void run() {
                    startLocation();
                }
            }, 2000);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_ID && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocation();
        }
    }

    public void startLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_ID);
        } else {
            Location mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (mLocation == null) {
                showGpsDialog();
            } else {
                mGoogleMap.setMyLocationEnabled(true);

                //Move Camera to myLocation
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()))
                        .zoom(10)
                        .build();

                mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);

        buildGoogleApiClient();
        mGoogleApiClient.connect();

        startMap(googleMap);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void startMap(final GoogleMap googleMap) {
        LatLng latLng = new LatLng(41.736054, 1.707176);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 7.5f));

        googleMap.setClustering(new ClusteringSettings().clusterOptionsProvider(new MapClusterOptionsProvider(getResources())).clusterSize(128).addMarkersDynamically(true));

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {
                if (isAdded() && marker != null) { //Should not be null, but in some rare unlikely case it appears to be
                    if (marker.isCluster()) {
                        List<Marker> markers = marker.getMarkers();
                        LatLngBounds.Builder builder = LatLngBounds.builder();
                        for (Marker m : markers) {
                            builder.include(m.getPosition());
                        }
                        LatLngBounds bounds = builder.build();
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, getResources().getDimensionPixelSize(R.dimen.map_cluster_padding)));
                        return true;
                    } else {
                        showColegiElectoralData((ColegiElectoral) marker.getData());
                    }
                }
                return false;
            }
        });

        loadColegis();
    }

    public void addMarkers(List<ColegiElectoral> colegiElectorals) {
        if (mGoogleMap != null && colegiElectorals != null) {
            mGoogleMap.clear();
            for (ColegiElectoral colegiElectoral : colegiElectorals) {
                mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(colegiElectoral.getLat(), colegiElectoral.getLon()))
                        .data(colegiElectoral));
            }
        }
    }

    public void onConnected(@Nullable Bundle bundle) {
        startLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        //Nothing
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //Nothing
    }

    public void showGpsDialog() {
        new AlertDialog.Builder(getActivity(), R.style.GpsDialog)
                .setTitle(R.string.enable_gps_title)
                .setMessage(R.string.enable_gps_message)
                .setPositiveButton(R.string.button_options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), GPS);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public void showColegiElectoralData(ColegiElectoral colegiElectoral) {
        LatLng latLng = new LatLng(colegiElectoral.getLat(), colegiElectoral.getLon());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12);
        mGoogleMap.animateCamera(cameraUpdate);

        txtNomLocal.setText(colegiElectoral.getLocal());
        txtAdresa.setText(colegiElectoral.getAdresa());
        txtLocalitat.setText(colegiElectoral.getMunicipi());

        bsb.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void loadColegis() {
        String colegis = (BuildConfig.DEBUG ? "colegis_mock.json" : "colegis.json");

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(colegis);
        storageRef.getBytes(Constants.MAX_FIREBASE_DOWNLOAD_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<ColegiElectoral>>() {
                }.getType();
                List<ColegiElectoral> colegisElectorals = gson.fromJson(new String(bytes), listType);
                mColegiElectorals = colegisElectorals;
                if (colegisElectorals != null) {
                    if (colegisElectorals.size() > 0) {
                        txtNoPunts.setVisibility(View.GONE);
                        addMarkers(colegisElectorals);
                    } else {
                        txtNoPunts.setVisibility(View.VISIBLE);
                    }
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getActivity(), getString(R.string.load_error), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void searchStation(int codiPostal) {
        boolean found = false;
        if (mColegiElectorals == null || mColegiElectorals.size() == 0) {
            Toast.makeText(getActivity(), R.string.no_college_found, Toast.LENGTH_SHORT).show();
        } else {
            for (ColegiElectoral colegi : mColegiElectorals) {
                if (colegi.getCp() == codiPostal) {
                    mSelectedColegiElectoral = colegi;
                    found = true;
                    break;
                }
            }

            if (found) {
                showColegiElectoralData(mSelectedColegiElectoral);
            }
            else{
                Toast.makeText(getActivity(), R.string.no_college_found, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
