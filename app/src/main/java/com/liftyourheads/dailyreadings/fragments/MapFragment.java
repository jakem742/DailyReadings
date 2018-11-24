package com.liftyourheads.dailyreadings.fragments;
import static com.liftyourheads.dailyreadings.activities.MainActivity.curReading;
import static com.liftyourheads.dailyreadings.activities.MainActivity.reading;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.List;

import com.liftyourheads.dailyreadings.R;
import com.liftyourheads.dailyreadings.activities.MainActivity;
import com.liftyourheads.dailyreadings.utils.IconUtils;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;


//// MAPBOX BASED MAP ////
//Style related imports


public class MapFragment extends Fragment implements View.OnTouchListener {

    Marker[] markers;
    MapboxMap map;
    MapView mMapView;
    FrameLayout mapOverlay;
    String TAG = "Mapbox Instance";
    //String JSON_CHARSET = "UTF-8";
    View view;

    private OnFragmentInteractionListener mListener;

    public MapFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (view == null) {
            Mapbox.getInstance(requireActivity(), getString(R.string.mapbox_access_token));

            view = inflater.inflate(R.layout.fragment_map, container, false);

            mMapView = view.findViewById(R.id.bibleMapView);
            mapOverlay = view.findViewById(R.id.mapOverlay);
            mMapView.onCreate(savedInstanceState);

            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(MapboxMap mapboxMap) {

                    map = mapboxMap;

                    Log.i(TAG, "Map ready. Loading markers");

                    setMarkers(curReading);

                    zoomCamera();

                    ////////// OFFLINE MAP DATA STORAGE /////////


                }
            });
        } else { mMapView.onCreate(savedInstanceState); }

        return view;
    }

    public void setMarkers(int readingNumber) {
        List<String[]> places = reading[readingNumber].getPlaces();
        int tagNumber = 0;
        markers = new Marker[places.size()];

        map.clear();

        //IconFactory iconFactory = IconFactory.getInstance(getContext());
        //Icon icon = iconFactory.fromResource(R.drawable.map_circle_custom);

        //Icon icon = IconUtils.drawableToIcon(getContext(),R.drawable.map_circle_custom,255,"marker");

        // Add some markers to the map, and add a data object to each marker.

        for( String[] place : places) {

            Double latitude = Double.parseDouble(place[1].replaceAll("[^\\d.]", ""));
            Double longitude = Double.parseDouble(place[2].replaceAll("[^\\d.]", ""));
            LatLng mLatLng = new LatLng(latitude, longitude);

            markers[tagNumber] = map.addMarker(new MarkerOptions()
                    .setPosition(mLatLng)
                    .setTitle(place[0]));
                    //.setIcon(icon));

            markers[tagNumber].setId(tagNumber++);
        }

        //FeatureCollection featureCollection = FeatureCollection;

    }

    public Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap;
        int px = getResources().getDimensionPixelSize(R.dimen.map_dot_marker_size);

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public void zoomCamera() {

        //Zoom to fit markers
        CameraUpdate cu;

        if ( markers.length > 0 ) { //Check if markers do exist for this reading

            mapOverlay.setVisibility(View.INVISIBLE);
            map.getUiSettings().setAllGesturesEnabled(true);

            if (markers.length < 2) { //If there's only one point, zoom in to a reasonable distance
                cu = CameraUpdateFactory.newLatLngZoom(markers[0].getPosition(), 6F);

            } else { //Automatically zoom out to fit all points

                LatLngBounds.Builder builder = new LatLngBounds.Builder();

                for (Marker marker : markers) {
                    try {
                        builder.include(marker.getPosition());
                    } catch (Exception e) {
                        //Log.i("Marker Info", "Failed to process marker " + marker.getTitle());
                        e.printStackTrace();

                    }
                }

                LatLngBounds bounds = builder.build();

                // Calculate distance between northeast and southwest
                float[] results = new float[1];
                android.location.Location.distanceBetween(bounds.getNorthEast().getLatitude(), bounds.getNorthEast().getLongitude(),
                        bounds.getSouthWest().getLatitude(), bounds.getSouthWest().getLongitude(), results);

                if (results[0] < 3000) { // distance is less than 1 km -> set to zoom level 8
                    cu = CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 8F);
                } else {
                    int padding = 200; // offset from edges of the map in pixels
                    cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

                }
            }


        } else {
            //There aren't any points to see! Grey out the map area
            cu = CameraUpdateFactory.newLatLngZoom(new LatLng(33.813,33.781), 4F);
            mapOverlay.setVisibility(View.VISIBLE);
            map.getUiSettings().setAllGesturesEnabled(false);
        }

        map.animateCamera(cu,1500);

    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        Log.i(TAG,"Detaching Map");
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }



    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        Log.i(TAG,"Pausing Map");
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onStop() {
        Log.i(TAG,"Stopping Map");
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //mMapView.onDestroy();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    /*
    public void getOfflineMaps() {


        // Set up the OfflineManager
        OfflineManager offlineManager = OfflineManager.getInstance(getContext());

        // Create a bounding box for the offline region
        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                .include(new LatLng(47.415, 53.514966)) // Northeast
                .include(new LatLng(9.550219, -11.37934023)) // Southwest
                .build();

        // Define the offline region
        OfflineTilePyramidRegionDefinition definition = new OfflineTilePyramidRegionDefinition(
                map.getStyleUrl(),
                latLngBounds,
                10,
                20,
                getActivity().getResources().getDisplayMetrics().density);

        // Implementation that uses JSON to store Biblical World as the offline region name.
        byte[] metadata;

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Offline Map Region 1", "Biblical World");
            String json = jsonObject.toString();
            metadata = json.getBytes(JSON_CHARSET);
        } catch (Exception exception) {
            Log.e(TAG, "Failed to encode metadata: " + exception.getMessage());
            metadata = null;
        }

        // Create the region asynchronously
        offlineManager.createOfflineRegion(definition, metadata,
                new OfflineManager.CreateOfflineRegionCallback() {
                    @Override
                    public void onCreate(OfflineRegion offlineRegion) {
                        offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE);

                        // Monitor the download progress using setObserver
                        offlineRegion.setObserver(new OfflineRegion.OfflineRegionObserver() {
                            @Override
                            public void onStatusChanged(OfflineRegionStatus status) {

                                // Calculate the download percentage
                                double percentage = status.getRequiredResourceCount() >= 0
                                        ? (100.0 * status.getCompletedResourceCount() / status.getRequiredResourceCount()) :
                                        0.0;

                                if (status.isComplete()) {
                                    // Download complete
                                    Log.d(TAG, "Region downloaded successfully.");
                                } else if (status.isRequiredResourceCountPrecise()) {
                                    Log.d(TAG, Double.toString(percentage));
                                }
                            }

                            @Override
                            public void onError(OfflineRegionError error) {
                                // If an error occurs, print to logcat
                                Log.e(TAG, "onError reason: " + error.getReason());
                                Log.e(TAG, "onError message: " + error.getMessage());
                            }

                            @Override
                            public void mapboxTileCountLimitExceeded(long limit) {
                                // Notify if offline region exceeds maximum tile count
                                Log.e(TAG, "Mapbox tile count limit exceeded: " + limit);
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error: " + error);
                    }
                });

    }
    */
}
