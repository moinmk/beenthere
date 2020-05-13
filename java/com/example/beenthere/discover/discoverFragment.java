package com.example.beenthere.discover;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.beenthere.R;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;


public class discoverFragment extends Fragment implements OnMapReadyCallback {
    private OnFragmentInteractionListener mListener;
    private String TAG="beenthere";
    String placeToAdd;
    private ArrayList<String> followingList = new ArrayList<>();
    private ArrayList<String> visitedUserIds = new ArrayList<>();

    private GoogleMap mMap;//map object
    private FusedLocationProviderClient fusedLocationProviderClient;//class used to fetch current lcoation
    private PlacesClient placesClient;//for suggestion in search bar
    private Location lastKnownLocation;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage firebaseStorage;
    private FirebaseUser currentUser;

    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 1;
    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private String[] mLikelyPlaceAttributions;
    private LatLng[] mLikelyPlaceLatLngs;
    float maxLikelihood=0;
    int i=0;

    private View mapView;
    private Button addAsVisited;

    final float DEFAULT_ZOOM=15;

    public discoverFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //startActivity(new Intent(getActivity(),FriendsList.class));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_discover, container, false);
        addAsVisited=view.findViewById(R.id.addAsVisited);
        db=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        firebaseStorage=FirebaseStorage.getInstance();
        currentUser = mAuth.getCurrentUser();

        addAsVisited.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPlaceAsVisited();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapView=mapFragment.getView();
        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(getActivity());
        if (!Places.isInitialized()) {
            Places.initialize(getActivity(), getString(R.string.google_maps_key));
        }
        placesClient=Places.createClient(getActivity());

        //==============autocomplete fragment to search a place==================
        AutocompleteSessionToken token=AutocompleteSessionToken.newInstance();
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setCountry("IN");//set country to india
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG,Place.Field.RATING));//setting field which is needed to be fetched
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(final Place place) {
                visitedUserIds.clear();
                final CollectionReference collectionReference=db.collection("usersdata");
                //fetching current users data to fetch following list
                collectionReference.document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(task.isSuccessful()){
                            DocumentSnapshot userDocumet=task.getResult();
                            if(userDocumet.exists()){
                                followingList=(ArrayList<String>)userDocumet.getData().get("following");
                                if(followingList.size()==0){
                                    displayBottmsheet(place);
                                    return;
                                }

                                for(final String id:followingList){
                                    //fetching user's visited place list
                                    collectionReference.document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if(task.isSuccessful()){
                                                DocumentSnapshot document=task.getResult();
                                                if(document.exists()){
                                                    ArrayList<String> placesid=new ArrayList<>();
                                                    placesid=(ArrayList<String>)document.getData().get("visitedplaces");
                                                    if(placesid.contains(place.getId())){
                                                        visitedUserIds.add(id);
                                                    }
                                                }
                                            }
                                            Toast.makeText(getActivity(), ""+visitedUserIds, Toast.LENGTH_LONG).show();
                                            if(i==followingList.size()-1){
                                                displayBottmsheet(place);
                                            }
                                            i++;
                                        }
                                    });

                                }

                            }
                        }

                    }
                });
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        return view;
    }

    private void displayBottmsheet(Place place) {
        float ratings;

        try {
            if (place.getRating() > 0) {
                ratings = place.getRating().floatValue();
            } else {
                ratings = 0.0f;
            }
        }catch (NullPointerException e){
            ratings=0.0f;
        }
        bottomsheetDialog bottmsheet=new bottomsheetDialog(place,ratings,visitedUserIds,currentUser.getUid());
        bottmsheet.show(getChildFragmentManager(),"bottomsheet");
        //Toast.makeText(getActivity(), "rating"+place.getRating(), Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Place: " + place.getName() + ", " + place.getId()+","+place.getLatLng());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
        //mMap.addMarker(new MarkerOptions().position(place.getLatLng()).icon(BitmapDescriptorFactory.fromResource(R.drawable.markerlocation)));
        mMap.addMarker(new MarkerOptions().position(place.getLatLng()));

    }

    private void addPlaceAsVisited() {
        DocumentReference documentReference=db.collection("usersdata").document(currentUser.getUid());
        documentReference.update("visitedplaces", FieldValue.arrayUnion(placeToAdd)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getActivity(), "place added as visited", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getActivity(), "error adding place data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);//display my location button

        //checking if gps is on or off
        LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE );
        boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!statusOfGPS){
            Toast.makeText(getActivity(), "start gps for better experience", Toast.LENGTH_LONG).show();
        }

        //displaying mylocation button at below part of map
        if(mapView!=null && mapView.findViewById(Integer.parseInt("1"))!=null){
            View locationButton=((View)mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams=(RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
            layoutParams.setMargins(0,0,50,200);
        }

        getDeviceLocation();

    }

    private void getDeviceLocation() {
        fusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if(task.isSuccessful()){
                            lastKnownLocation=(Location) task.getResult();
                            if(lastKnownLocation!=null){
                                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLongitude(),lastKnownLocation.getAltitude())),DEFAULT_ZOOM);
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude())));
                                mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));

                                //======================================

                                final List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,Place.Field.NAME, Place.Field.ADDRESS,
                                        Place.Field.TYPES,
                                        Place.Field.LAT_LNG);

                                // Get the likely of places the device can be at
                                @SuppressWarnings("MissingPermission")
                                final FindCurrentPlaceRequest request =
                                        FindCurrentPlaceRequest.builder(placeFields).build();
                                Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
                                placeResponse.addOnCompleteListener(getActivity(),
                                        new OnCompleteListener<FindCurrentPlaceResponse>() {
                                            @Override
                                            public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                                                if (task.isSuccessful()) {
                                                    FindCurrentPlaceResponse response = task.getResult();
                                                    // Set the count, handling cases where less than 5 entries are returned.
                                                    int count;
                                                    if (response.getPlaceLikelihoods().size() < M_MAX_ENTRIES) {
                                                        count = response.getPlaceLikelihoods().size();
                                                    } else {
                                                        count = M_MAX_ENTRIES;
                                                    }
                                                    //Toast.makeText(getActivity(), "count:"+count, Toast.LENGTH_SHORT).show();
                                                    int i = 0;
                                                    mLikelyPlaceNames = new String[count];
                                                    mLikelyPlaceAddresses = new String[count];
                                                    mLikelyPlaceAttributions = new String[count];
                                                    mLikelyPlaceLatLngs = new LatLng[count];

                                                    for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                                                        Place currPlace = placeLikelihood.getPlace();
                                                        mLikelyPlaceNames[i] = currPlace.getName();
                                                        mLikelyPlaceAddresses[i] = currPlace.getAddress();
                                                        mLikelyPlaceAttributions[i] = (currPlace.getAttributions() == null) ?
                                                                null : (" "+currPlace.getAttributions());
                                                        mLikelyPlaceLatLngs[i] = currPlace.getLatLng();

                                                        String currLatLng = (mLikelyPlaceLatLngs[i] == null) ?
                                                                "" : mLikelyPlaceLatLngs[i].toString();



                                                        if(placeLikelihood.getLikelihood()>0.10){
                                                            addAsVisited.setVisibility(View.VISIBLE);
                                                        }
                                                        placeToAdd=currPlace.getId();

                                                        Log.i(TAG, String.format("Place " + currPlace.getName()
                                                                + " has likelihood: " + placeLikelihood.getLikelihood()
                                                                + "type"+placeLikelihood.getPlace().getTypes()
                                                                + " at " + currLatLng));
                                                        //Toast.makeText(getActivity(), "current place1"+currPlace.getName(), Toast.LENGTH_LONG).show();

                                                        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude())));
                                                        mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
                                                        mMap.addMarker(new MarkerOptions().position(currPlace.getLatLng()));


                                                        i++;
                                                        if (i > (count - 1)) {
                                                            break;
                                                        }
                                                    }

                                                } else {
                                                    Exception exception = task.getException();
                                                    if (exception instanceof ApiException) {
                                                        ApiException apiException = (ApiException) exception;
                                                        Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                                                        Toast.makeText(getActivity(), "place not found", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
                                        });

                            }

                        }else{
                            Toast.makeText(getActivity(), "unable to get last location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
