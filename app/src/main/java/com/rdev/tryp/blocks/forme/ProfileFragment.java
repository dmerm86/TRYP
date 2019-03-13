package com.rdev.tryp.blocks.forme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.rdev.tryp.ContentActivity;
import com.rdev.tryp.R;
import com.rdev.tryp.autocomplete.AutoCompleteAdapter;
import com.rdev.tryp.model.TripPlace;
import com.rdev.tryp.utils.PreferenceManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import afu.org.checkerframework.checker.nullness.qual.NonNull;
import afu.org.checkerframework.checker.nullness.qual.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.rdev.tryp.utils.Utils.closeKeyboard;
import static com.rdev.tryp.utils.Utils.showKeyboard;

@SuppressLint("ValidFragment")
public class ProfileFragment extends Fragment implements AutoCompleteAdapter.onPlacePicked, View.OnClickListener {

    private Geocoder geocoder;
    private PlacesClient placesClient;
    private TripPlace destination, startPos;
    private AutoCompleteAdapter adapter;

    private View view;
    private AppCompatEditText adressTv, adressTv2;
    private RecyclerView autoCompleteRv;
    private AppCompatImageView pickLocationBtn;
    private ImageButton back_btn;
    private TextView mainTextView, homeTextView, workTextView;
    private CardView cardView;
    private RelativeLayout recentFirst, recentSecond, homeAddress, workAddress;

    private final static String KEY_RECENT_FROM_1 = "KEY_RECENT_FROM_1";
    private final static String KEY_RECENT_TO_1 = "KEY_RECENT_TO_1";
    private final static String KEY_RECENT_FROM_2 = "KEY_RECENT_FROM_2";
    private final static String KEY_RECENT_TO_2 = "KEY_RECENT_TO_2";
    private final static String KEY_HOME = "KEY_HOME";
    private final static String KEY_WORK = "KEY_WORK";


    @SuppressLint("ValidFragment")
    public ProfileFragment(TripPlace startPos, TripPlace destination) {
        this.destination = destination;
        this.startPos = startPos;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        initHomeWork(); //TODO: it's for test (must init in settings or another fragment)
        initView();
        initRecentRoutes();
        initAutoComplete();

        adressTv2.requestFocus();
        showKeyboard(getContext());

        return view;
    }

    public void initView() {
        adressTv = view.findViewById(R.id.adress_tv);
        adressTv2 = view.findViewById(R.id.adress_tv_2);
        cardView = view.findViewById(R.id.top_card_view);
        autoCompleteRv = view.findViewById(R.id.autoCompleteRv);
        pickLocationBtn = view.findViewById(R.id.pickLocationBtn);
        back_btn = view.findViewById(R.id.back_btn);
        recentFirst = view.findViewById(R.id.recent_relative_layout);
        recentSecond = view.findViewById(R.id.recent_relative_layout_2);
        homeAddress = view.findViewById(R.id.home_location_relative_layout);
        workAddress = view.findViewById(R.id.work_location_relative_layout);
        homeTextView = view.findViewById(R.id.home_tv);
        workTextView = view.findViewById(R.id.work_tv);

        cardView.setBackgroundResource(R.drawable.card_view_bg);
        autoCompleteRv.setLayoutManager(new LinearLayoutManager(getContext()));
        back_btn.setOnClickListener(this);
        recentSecond.setOnClickListener(this);
        recentFirst.setOnClickListener(this);
        homeAddress.setOnClickListener(this);
        workAddress.setOnClickListener(this);
        homeTextView.setText(PreferenceManager.getString(KEY_HOME));
        workTextView.setText(PreferenceManager.getString(KEY_WORK));
        adressTv.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                adapter.setData(new ArrayList<>());
                mainTextView = adressTv;
            }
        });
        adressTv2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                adapter.setData(new ArrayList<>());
                mainTextView = adressTv2;
            }
        });
    }

    public void initAutoComplete() {
        geocoder = new Geocoder(getContext());

        if (startPos == null) {
            startPos = new TripPlace();
        }
        if (destination == null) {
            destination = new TripPlace();
        }

        adressTv.setText(startPos.getLocale());
        adressTv2.setText(destination.getLocale());

        pickLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adressTv.getText().length() != 0) {
                    Address dest;
                    try {
                        dest = getLocationFromAddress(getContext(), adressTv.getText().toString());
                    } catch (RuntimeException e) {
                        dest = null;
                    }
                    if (dest != null) {
                        startPos.setCoord(new LatLng(dest.getLatitude(), dest.getLongitude()));
                        startPos.setLocale(dest.getThoroughfare() + " " + dest.getLocality() + ", "
                                + dest.getAdminArea() + ", " + dest.getCountryName());
                    } else {
                        Toast.makeText(getContext(), "Please enter right address", Toast.LENGTH_LONG).show();
                        return;
                    }
                } else {
                    Toast.makeText(getContext(), "Please enter destination address", Toast.LENGTH_LONG).show();
                    return;
                }
                if (adressTv2.getText().length() != 0) {
                    Address dest;
                    try {
                        dest = getLocationFromAddress(getContext(), adressTv2.getText().toString());
                    } catch (RuntimeException e) {
                        dest = null;
                    }
                    if (dest != null) {
                        destination.setCoord(new LatLng(dest.getLatitude(), dest.getLongitude()));
                        destination.setLocale(dest.getThoroughfare() + " " + dest.getLocality() + ", "
                                + dest.getAdminArea() + ", " + dest.getCountryName());
                        closeKeyboard(getContext());
                        onDestination(startPos, destination);
                        saveRouteInRecent(startPos, destination);
                    } else {
                        Toast.makeText(getContext(), "Please enter right address", Toast.LENGTH_LONG).show();
                        return;
                    }
                } else {
                    Toast.makeText(getContext(), "Please enter destination address", Toast.LENGTH_LONG).show();
                }
            }
        });

        adapter = new AutoCompleteAdapter(new ArrayList<>(), ProfileFragment.this);
        autoCompleteRv.setAdapter(adapter);

        //Autocomplete Realisation
        adressTv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mainTextView = adressTv;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                placesClient = Places.createClient(getContext());
                FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .setQuery(charSequence.toString())
                        .build();
                placesClient.findAutocompletePredictions(request).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@androidx.annotation.NonNull Task<FindAutocompletePredictionsResponse> task) {
                        adapter.setData(task.getResult().getAutocompletePredictions());
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.setData(new ArrayList<>());
            }
        });

        adressTv2.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mainTextView = adressTv2;
                adapter.setData(new ArrayList<>());
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                placesClient = Places.createClient(getContext());
                FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .setQuery(charSequence.toString())
                        .build();
                placesClient.findAutocompletePredictions(request).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@androidx.annotation.NonNull Task<FindAutocompletePredictionsResponse> task) {
                        adapter.setData(task.getResult().getAutocompletePredictions());
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.setData(new ArrayList<>());
            }
        });

    }

    public void initRecentRoutes() {
        TextView firstFrom, firstTo, secondFrom, secondTo;
        TripPlace place;

        firstFrom = view.findViewById(R.id.from_tv_1);
        firstTo = view.findViewById(R.id.to_tv_1);
        secondFrom = view.findViewById(R.id.from_tv_2);
        secondTo = view.findViewById(R.id.to_tv_2);

        place = PreferenceManager.getTripPlace(KEY_RECENT_FROM_1);
        if (place != null) {
            firstFrom.setText(place.getLocale());
        } else {
            recentFirst.setVisibility(View.INVISIBLE);
            recentFirst.setOnClickListener(null);
        }

        place = PreferenceManager.getTripPlace(KEY_RECENT_TO_1);
        if (place != null) {
            firstTo.setText(place.getLocale());
        }


        place = PreferenceManager.getTripPlace(KEY_RECENT_FROM_2);
        if (place != null) {
            secondFrom.setText(place.getLocale());
        } else {
            recentSecond.setVisibility(View.INVISIBLE);
            recentSecond.setOnClickListener(null);
        }

        place = PreferenceManager.getTripPlace(KEY_RECENT_TO_2);
        if (place != null) {
            secondTo.setText(place.getLocale());
        }
    }

    public Address getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        Address result_location = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }

            result_location = address.get(0);

        } catch (IOException ex) {

            ex.printStackTrace();
        }

        return result_location;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_btn:
                ((ContentActivity)getActivity()).goHome();
                break;
            case R.id.recent_relative_layout:
                getRecentFirst();
                break;
            case R.id.recent_relative_layout_2:
                getRecentSecond();
                break;
            case R.id.home_location_relative_layout:
                setTripPlace(PreferenceManager.getString(KEY_HOME));
                break;
            case R.id.work_location_relative_layout:
                setTripPlace(PreferenceManager.getString(KEY_WORK));
                break;
        }
    }

    private void setTripPlace(String address) {

        if (address == null) {
            return;
        }

        if(mainTextView == null){
            mainTextView = adressTv;
        }

        if(mainTextView.equals(adressTv2)) {
            adressTv2.setText(address);

            if (!adressTv.getText().toString().isEmpty()) {
                onClick(pickLocationBtn);
                return;
            }
        }
        if(mainTextView.equals(adressTv)) {
            adressTv.setText(address);

            if (!adressTv2.getText().toString().isEmpty()) {
                onClick(pickLocationBtn);
                return;
            }
        }
    }

    @Override
    public void onPlace(AutocompletePrediction prediction) {
        mainTextView.setText(prediction.getPrimaryText(null));
        List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME);
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(prediction.getPlaceId(), placeFields);
        placesClient.fetchPlace(request).addOnCompleteListener(new OnCompleteListener<FetchPlaceResponse>() {
            @Override
            public void onComplete(@androidx.annotation.NonNull Task<FetchPlaceResponse> task) {
                if (mainTextView.equals(adressTv2)) {

                    destination.setCoord(task.getResult().getPlace().getLatLng());
                    destination.setLocale(prediction.getFullText(null).toString());

                    if (!adressTv.getText().toString().isEmpty()) {
                        closeKeyboard(getContext());
                        onDestination(startPos, destination);
                        saveRouteInRecent(startPos, destination);
                    }
                }
                if (mainTextView.equals(adressTv)) {
                    startPos.setCoord(task.getResult().getPlace().getLatLng());
                    startPos.setLocale(prediction.getFullText(null).toString());

                    if (!adressTv2.getText().toString().isEmpty()) {
                        closeKeyboard(getContext());
                        onDestination(startPos, destination);
                        saveRouteInRecent(startPos, destination);
                    }
                }
            }
        });
    }

    private void onDestination(TripPlace start, TripPlace end) {
        ((ContentActivity) getActivity()).popBackStack();
        ((ContentActivity) getActivity()).onDestinationPicked(start, end);
    }

    public void saveRouteInRecent(TripPlace start, TripPlace end) {
        TripPlace secondFrom, secondTo;
        secondFrom = PreferenceManager.getTripPlace(KEY_RECENT_FROM_1);
        secondTo = PreferenceManager.getTripPlace(KEY_RECENT_TO_1);

        PreferenceManager.setTripPlace(KEY_RECENT_FROM_1, start);
        PreferenceManager.setTripPlace(KEY_RECENT_TO_1, end);

        PreferenceManager.setTripPlace(KEY_RECENT_FROM_2, secondFrom);
        PreferenceManager.setTripPlace(KEY_RECENT_TO_2, secondTo);

    }

    private void getRecentFirst() {
        TripPlace from, to;
        from = PreferenceManager.getTripPlace(KEY_RECENT_FROM_1);
        to = PreferenceManager.getTripPlace(KEY_RECENT_TO_1);

        onDestination(from, to);
    }

    private void getRecentSecond() {
        TripPlace from, to;
        from = PreferenceManager.getTripPlace(KEY_RECENT_FROM_2);
        to = PreferenceManager.getTripPlace(KEY_RECENT_TO_2);

        onDestination(from, to);
    }

    private void initHomeWork() {
        PreferenceManager.setString(KEY_HOME, "Yaroslava Mydroho, 29, Lviv");
        PreferenceManager.setString(KEY_WORK, "Shevchenka, 37, Lviv");

    }
}