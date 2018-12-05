package ketank.myapplication;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,RoutingListener {

    private GoogleMap mMap;
    PlaceAutocompleteFragment autocompleteFragment;
    PlaceAutocompleteFragment destination;
    LatLng source,dest;
    private ProgressDialog progressDialog;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark,R.color.primary,R.color.primary_light,R.color.accent,R.color.primary_dark_material_light};

    Marker sourceMarker,destMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        polylines = new ArrayList<>();






         autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
         autocompleteFragment.setHint("Source");

        destination = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.plc);
        destination.setHint("Destination");



    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(getApplicationContext(),marker.getTitle(),Toast.LENGTH_LONG).show();
        return false;
    }
});


        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i("Myjiofpoop", "Place: " + place.getName());

                    if(sourceMarker!=null)
                        sourceMarker.remove();
                 source = place.getLatLng();



               sourceMarker=  mMap.addMarker(new MarkerOptions().position(source).title(place.getName()+"\nlat="+place.getLatLng().latitude+"\nlong="+place.getLatLng().longitude));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(source, 15.0f));



                if(destMarker!=null) {
                    Routing routing = new Routing.Builder()
                            .key("AIzaSyBPs9eolVNUuDJgOz1M8zn7GozvShe1Ghk")
                            .travelMode(Routing.TravelMode.DRIVING)
                            .withListener(MapsActivity.this)
                            .waypoints(source, dest)
                            .build();

                    routing.execute();

                    progressDialog = ProgressDialog.show(MapsActivity.this, "Please wait.",
                            "Fetching route information.", true);
                }


            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("Myjiofpoop", "An error occurred: " + status);
            }
        });


        destination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i("Myjiofpoop", "Place: " + place.getName());

                if(destMarker!=null)
                    destMarker.remove();

                dest = place.getLatLng();

             destMarker=   mMap.addMarker(new MarkerOptions().position(dest).title(place.getName()+"\nlat="+place.getLatLng().latitude+"\nlong="+place.getLatLng().longitude));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dest, 15.0f));



                if(sourceMarker!=null) {
                    Routing routing = new Routing.Builder()
                            .key("AIzaSyBPs9eolVNUuDJgOz1M8zn7GozvShe1Ghk")
                            .travelMode(Routing.TravelMode.DRIVING)
                            .withListener(MapsActivity.this)
                            .waypoints(source, dest)
                            .build();

                    routing.execute();

                    progressDialog = ProgressDialog.show(MapsActivity.this, "Please wait.",
                            "Fetching route information.", true);
                }

                }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("Myjiofpoop", "An error occurred: " + status);
            }
        });
    }


    @Override
    public void onRoutingFailure(RouteException e) {
        progressDialog.dismiss();

            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        progressDialog.dismiss();
        CameraUpdate center = CameraUpdateFactory.newLatLng(source);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);

        mMap.moveCamera(center);


        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue(),Toast.LENGTH_SHORT).show();
        }



    }

    @Override
    public void onRoutingCancelled() {

    }
}
