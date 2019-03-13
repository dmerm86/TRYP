package com.rdev.tryp.autocomplete;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import com.rdev.tryp.model.TripPlace;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AdressListFragment extends Fragment implements AutoCompleteAdapter.onPlacePicked, View.OnClickListener {
    Geocoder geocoder;
    AppCompatEditText adressTv;
    TripPlace destination;
    RecyclerView autoCompleteRv;
    AppCompatImageView pickLocationBtn;
    ImageButton back_btn;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        destination = new TripPlace();
    }

    PlacesClient placesClient;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.adress_list_fragment, container, false);
        geocoder = new Geocoder(getContext());
        adressTv = v.findViewById(R.id.adress_tv);
        autoCompleteRv = v.findViewById(R.id.autoCompleteRv);
        autoCompleteRv.setLayoutManager(new LinearLayoutManager(getContext()));
        pickLocationBtn = v.findViewById(R.id.pickLocationBtn);
        back_btn = v.findViewById(R.id.back_btn);
        back_btn.setOnClickListener(this);
        pickLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adressTv.getText().length() != 0) {
                    Address dest = getLocationFromAddress(getContext(), adressTv.getText().toString());
                    if (dest != null) {
                        destination.setCoord(new LatLng(dest.getLatitude(), dest.getLongitude()));
                        destination.setLocale(dest.getThoroughfare() + " " + dest.getLocality() + ", "
                                + dest.getAdminArea() + ", " + dest.getCountryName());
                        ((ContentActivity) getActivity()).onDestinationPicked(null, destination);
                    }
                } else {
                    Toast.makeText(getContext(), "Please enter destination address", Toast.LENGTH_LONG).show();
                }
            }
        });

        //Autocomplete Realisation

        adressTv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

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
                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                        AutoCompleteAdapter adapter = new AutoCompleteAdapter(task.getResult().getAutocompletePredictions(), AdressListFragment.this);
                        autoCompleteRv.setAdapter(adapter);

                    }
                });
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        return v;
    }

    @Override
    public void onPlace(AutocompletePrediction prediction) {
        Log.i("tagggg", "");
        adressTv.setText(prediction.getPrimaryText(null));
        destination.setLocale(prediction.getFullText(null).toString());
        List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME);
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(prediction.getPlaceId(), placeFields);
        ((ContentActivity) getActivity()).onDestinationPicked(null, destination);
        placesClient.fetchPlace(request).addOnCompleteListener(new OnCompleteListener<FetchPlaceResponse>() {
            @Override
            public void onComplete(@NonNull Task<FetchPlaceResponse> task) {
                Log.d("tag", task.getResult().getPlace().getName());
                destination.setCoord(task.getResult().getPlace().getLatLng());
                ((ContentActivity) getActivity()).onDestinationPicked(null, destination);
            }
        });
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
    public void onClick(View view) {
        ((ContentActivity) getActivity()).popBackStack();
    }
}
