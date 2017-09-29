package com.referendum.uoctubre.fragments;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.referendum.uoctubre.R;
import com.referendum.uoctubre.UOctubreApplication;
import com.referendum.uoctubre.components.LocalStringButton;
import com.referendum.uoctubre.model.ColegiElectoral;
import com.referendum.uoctubre.model.PollingStationResponse;
import com.referendum.uoctubre.utils.PollingStationDataFetcher;
import com.referendum.uoctubre.utils.StringsManager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class VoteFragment extends BaseFragment implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    public static final String TAG = "vota_fragment";

    public static final int LOCATION_PERMISSION_ID = 1001;
    private static final int GPS = 10;

    private View formLayout;
    private View loadingLayout;
    private View errorLayout;
    private View mapLayout;
    private Button retryButton;
    private TextView errorDescriptionTextView;

    private BottomSheetBehavior bsb;
    private TextView txtLocalitat, txtAdresa, txtNomLocal, txtDistricte, txtSeccio, txtMesa;
    private ImageView icnCalendari;
    private TextInputEditText etNif, etDay, etMonth, etYear, etPostalCode;

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mGoogleMap;

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
        txtDistricte = view.findViewById(R.id.txtDistricte);
        txtSeccio = view.findViewById(R.id.txtSeccio);
        txtMesa = view.findViewById(R.id.txtMesa);
        icnCalendari = view.findViewById(R.id.icnCalendari);

        formLayout = view.findViewById(R.id.form_layout);
        loadingLayout = view.findViewById(R.id.loading_layout);
        errorLayout = view.findViewById(R.id.error_layout);
        mapLayout = view.findViewById(R.id.map_layout);
        errorDescriptionTextView = view.findViewById(R.id.error_description);
        retryButton = view.findViewById(R.id.error_retry_button);
        etNif = view.findViewById(R.id.etId);
        etDay = view.findViewById(R.id.etDay);
        etMonth = view.findViewById(R.id.etMonth);
        etYear = view.findViewById(R.id.etYear);
        etPostalCode = view.findViewById(R.id.etPostalCode);
        LocalStringButton btnSearch = view.findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                searchPollingStation();
            }
        });
        TextView btnPrivacyPolicy = view.findViewById(R.id.btnPrivacyPolicy);
        btnPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                showPrivacyPolicy();
            }
        });

        bsb = BottomSheetBehavior.from(bottomSheet);

        mMapFragment.getMapAsync(this);
        return view;
    }

    private void showPrivacyPolicy() {
        new AlertDialog.Builder(getActivity(), R.style.GpsDialog)
                .setTitle(StringsManager.getString("form_privacy_title"))
                .setMessage(StringsManager.getString("form_privacy_description"))
                .setPositiveButton(StringsManager.getString("button_ok"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GPS) {
            Handler gpsHandler = new Handler();
            gpsHandler.postDelayed(new Runnable() {
                public void run() {
                    if (isFragmentSafe()) {
                        startLocation();
                    }
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
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_ID);
        } else {
            Location mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (mLocation == null) {
                showGpsDialog();
            } else {
                mGoogleMap.setMyLocationEnabled(true);
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
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 7.5f));
    }

    public void onConnected(@Nullable Bundle bundle) {
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
                .setTitle(StringsManager.getString("enable_gps_title"))
                .setMessage(StringsManager.getString("enable_gps_message"))
                .setPositiveButton(StringsManager.getString("button_options"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), GPS);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(StringsManager.getString("button_cancel"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public void showColegiElectoralData(final ColegiElectoral colegiElectoral) {
        LatLng latLng = new LatLng(colegiElectoral.getLat(), colegiElectoral.getLon());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 14);
        mGoogleMap.animateCamera(cameraUpdate);

        txtNomLocal.setText(colegiElectoral.getLocal());
        txtAdresa.setText(colegiElectoral.getAdresa());
        txtLocalitat.setText(colegiElectoral.getMunicipi());
        txtDistricte.setText(StringsManager.getString("data_districte", colegiElectoral.getDistricte()));
        txtSeccio.setText(StringsManager.getString("data_seccio", colegiElectoral.getSeccio()));
        txtMesa.setText(StringsManager.getString("data_mesa", colegiElectoral.getMesa()));

        icnCalendari.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addEventToCalendar(colegiElectoral);
            }
        });

        bsb.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void addEventToCalendar(ColegiElectoral colegiElectoral) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.set(2017, Calendar.OCTOBER, 1, 9, 0);

        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra(CalendarContract.Events.TITLE, StringsManager.getString("notification_title"));
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, calendar.getTimeInMillis());
        intent.putExtra(CalendarContract.Events.ALL_DAY, true);
        String location = colegiElectoral.getLocal() + ": " + colegiElectoral.getAdresa() + ", " + colegiElectoral.getMunicipi();
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, location);

        startActivity(intent);
    }

    public void searchPollingStation() {
        if (isEmpty(etNif) || isEmpty(etDay) || isEmpty(etMonth) || isEmpty(etYear) || isEmpty(etPostalCode)) {
            Toast.makeText(getActivity(), StringsManager.getString("form_error_enter_all_fields"), Toast.LENGTH_SHORT).show();
        } else if (isNotInsideLimit(etDay, 1, 31)) {
            Toast.makeText(getActivity(), StringsManager.getString("form_error_day"), Toast.LENGTH_SHORT).show();
        } else if (isNotInsideLimit(etMonth, 1, 12)) {
            Toast.makeText(getActivity(), StringsManager.getString("form_error_month"), Toast.LENGTH_SHORT).show();
        } else if (isNotInsideLimit(etYear, 1000, 9999)) {
            Toast.makeText(getActivity(), StringsManager.getString("form_error_year"), Toast.LENGTH_SHORT).show();
        } else {
            String nif = etNif.getText().toString().replace("-", "").replace(".", "").replace(" ", "");
            if (nif.length() < 8) {
                Toast.makeText(getActivity(), StringsManager.getString("form_error_nif"), Toast.LENGTH_SHORT).show();
            } else {
                String day = etDay.getText().toString();
                String month = etMonth.getText().toString();
                String year = etYear.getText().toString();
                String postalcode = etPostalCode.getText().toString();

                getPollingStation(nif, day, month, year, postalcode);
            }
        }
    }

    private boolean isNotInsideLimit(TextInputEditText txtInputEditText, int min, int max) {
        int value = convertToInt(txtInputEditText.getText().toString());
        return value < min || value > max;
    }

    public void getPollingStation(final String nif, final String day, final String month, final String year, final String postalCode) {
        errorLayout.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.VISIBLE);
        formLayout.setVisibility(View.GONE);

        new AsyncTask<Void, Void, PollingStationResponse>() {
            @Override
            protected PollingStationResponse doInBackground(Void... voids) {
                PollingStationResponse pollingStationResponse = PollingStationDataFetcher.getUserPollingStation(nif, new Date(convertToInt(year) - 1900, convertToInt(month) - 1, convertToInt(day)), convertToInt(postalCode));

                if (pollingStationResponse.getPollingStation() != null) {
                    String locationName = pollingStationResponse.getPollingStation().getAdresa() + ", " + pollingStationResponse.getPollingStation().getMunicipi() + ", Catalunya";

                    try {
                        List<Address> addressList = new Geocoder(UOctubreApplication.getInstance()).getFromLocationName(locationName, 1, 40.424349, 0.121516, 42.978912, 3.428401);
                        if (addressList.size() > 0) {
                            pollingStationResponse.getPollingStation().setLat(addressList.get(0).getLatitude());
                            pollingStationResponse.getPollingStation().setLon(addressList.get(0).getLongitude());
                        } else {
                            pollingStationResponse.getPollingStation().setLat(0f);
                            pollingStationResponse.getPollingStation().setLon(0f);
                        }
                    } catch (Exception e) {
                        pollingStationResponse.getPollingStation().setLat(0f);
                        pollingStationResponse.getPollingStation().setLon(0f);
                    }
                }

                return pollingStationResponse;
            }

            @Override
            protected void onPostExecute(PollingStationResponse pollingStationResponse) {
                if (isFragmentSafe()) {
                    if (pollingStationResponse.getStatus().equals("error")) {
                        loadingLayout.setVisibility(View.GONE);
                        errorLayout.setVisibility(View.VISIBLE);
                        errorDescriptionTextView.setText(StringsManager.getString("load_error"));
                        retryButton.setText(StringsManager.getString("retry_button"));
                        retryButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                searchPollingStation();
                            }
                        });
                    } else if (pollingStationResponse.getStatus().equals("not_found")) {
                        loadingLayout.setVisibility(View.GONE);
                        errorLayout.setVisibility(View.VISIBLE);
                        errorDescriptionTextView.setText(StringsManager.getString("no_college_found"));
                        retryButton.setText(StringsManager.getString("button_ok"));
                        retryButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                errorLayout.setVisibility(View.GONE);
                                formLayout.setVisibility(View.VISIBLE);
                            }
                        });
                    } else { //OK
                        loadingLayout.setVisibility(View.GONE);
                        mapLayout.setVisibility(View.VISIBLE);
                        if (mGoogleMap != null) {
                            mGoogleMap.clear();
                            if (pollingStationResponse.getPollingStation().getLat() != 0 && pollingStationResponse.getPollingStation().getLon() != 0) {
                                mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(pollingStationResponse.getPollingStation().getLat(), pollingStationResponse.getPollingStation().getLon())));
                            }
                            startLocation();
                            showColegiElectoralData(pollingStationResponse.getPollingStation());
                        }
                    }
                }
            }
        }.execute();
    }

    private int convertToInt(String string) {
        try {
            return Integer.parseInt(string);
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean isEmpty(TextInputEditText txtInputEditText) {
        return txtInputEditText.getText().toString().isEmpty();
    }

    public boolean onBackPressed() {
        if (errorLayout != null && mapLayout != null && (errorLayout.getVisibility() == View.VISIBLE || mapLayout.getVisibility() == View.VISIBLE)) {
            errorLayout.setVisibility(View.GONE);
            mapLayout.setVisibility(View.GONE);
            formLayout.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }
}
