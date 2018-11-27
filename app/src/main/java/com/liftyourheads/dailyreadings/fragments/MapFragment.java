package com.liftyourheads.dailyreadings.fragments;
import static com.liftyourheads.dailyreadings.activities.MainActivity.curReading;
import static com.liftyourheads.dailyreadings.activities.MainActivity.reading;
import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_CENTER;
import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_ANCHOR_LEFT;
import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_JUSTIFY_LEFT;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textHaloBlur;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textHaloColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textHaloWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textJustify;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textOptional;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.google.gson.JsonObject;
import com.liftyourheads.dailyreadings.R;
import com.liftyourheads.dailyreadings.activities.MainActivity;
import com.liftyourheads.dailyreadings.utils.IconUtils;
import com.mapbox.android.gestures.Utils;
import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.GeoJson;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolDragListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.ColorUtils;

import timber.log.Timber;


//// MAPBOX BASED MAP ////
//Style related imports


public class MapFragment extends Fragment implements View.OnTouchListener {

    Marker[] markers;
    MapboxMap map;
    MapView mMapView;
    FrameLayout mapOverlay;
    ArrayList<LatLng> locations;
    String TAG = "MapFragment";
    //String JSON_CHARSET = "UTF-8";
    View view;
    List<SymbolOptions> symbolOptionsList;
    SymbolManager[] symbolManager;

    private static final String MAKI_ICON_CIRCLE_STROKED = "circle-stroked-15";
    private static final String MAKI_ICON_CIRCLE = "circle-15";
    private static final int ICON_CIRCLE_CUSTOM = R.drawable.map_circle_custom;

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

                    symbolManager = new SymbolManager[3];
//                    for (int i = 0; i<3; i++) symbolManager[i] = null;

                    //setMarkers(curReading);

                    //zoomCamera();

                    createLayers();
                    setCurLayer(curReading);
                    zoomExtents(curReading);
                    setClickListener();

                    //Todo: fix screen rotate crash

                    ////////// OFFLINE MAP DATA STORAGE /////////


                    //setSymbolMarkers(curReading);
                    //zoomCameraSymbols();


                }
            });

        } else { mMapView.onCreate(savedInstanceState); }

        return view;
    }

    public void setClickListener(){

        map.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng point) {
                PointF screenPoint = map.getProjection().toScreenLocation(point);
                List<Feature> features = map.queryRenderedFeatures(screenPoint, "locations-0","locations-1","locations-2");
                if (!features.isEmpty()) {
                    StringBuilder names = new StringBuilder();
                    for (Feature feature : features) {
                         names.append(feature.getStringProperty("name")).append(", ");
                    }
                    //Feature selectedFeature = features.get(0);
                    //String title = selectedFeature.getStringProperty("Name");
                    Toast.makeText(getContext(), "You selected " + names, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void createLayers() {

        SymbolLayer[] symbolLayers = new SymbolLayer [3];
        GeoJsonSource[] layers = new GeoJsonSource[3];

        String readingSourceString;

        //IconFactory iconFactory = IconFactory.getInstance(getContext());
        //Icon icon = iconFactory.fromResource(ICON_CIRCLE_CUSTOM);
        Bitmap custom_icon = drawableToBitmap(getResources().getDrawable(R.drawable.map_circle_custom));

        //Icon icon = IconUtils.drawableToIcon(getContext(),R.drawable.map_circle_custom,255,"marker");

        Bitmap icon = BitmapFactory.decodeResource(getActivity().getResources(),R.drawable.map_circle_custom);

        map.addImage("icon_circle_custom",custom_icon);

        for (Integer i = 0; i < 3; i++) {

            //if (reading[i].placesExist()) {
                Log.i(TAG,"Iterating to layer " + i.toString());
                readingSourceString = reading[i].getPlacesAsString();

                layers[i] = new GeoJsonSource("reading-places-" + i.toString(), FeatureCollection.fromJson(readingSourceString));
                map.addSource(layers[i]);
                symbolLayers[i] = new SymbolLayer("locations-" + i.toString(), "reading-places-" + i.toString());
                symbolLayers[i].setProperties(
                        visibility(VISIBLE),
                        iconImage("icon_circle_custom"),
                        iconAllowOverlap(true),
                        textAllowOverlap(false),
                        textOptional(true),
                        textField("{name}"),
                        textOffset(new Float[]{1f, 0f}),
                        iconAnchor(ICON_ANCHOR_CENTER),
                        iconColor(Color.RED),
                        textColor(getContext().getResources().getColor(R.color.colorDark)),
                        textHaloColor(Color.LTGRAY),
                        textHaloBlur(1.5f),
                        textHaloWidth(1f),
                        textJustify(TEXT_JUSTIFY_LEFT),
                        textAnchor(TEXT_ANCHOR_LEFT),
                        textSize(12f)
                );
                map.addLayer(symbolLayers[i]);
            //}

        }


    }

    public void setCurLayer(int readingNum) {

        Layer curLayer = map.getLayer("locations-" + Integer.toString(readingNum));

        curLayer.setProperties(visibility(VISIBLE));

        Layer[] layers = new Layer[3];

        for (Integer i = 0; i < 3; i++) {

            layers[i] = map.getLayer("locations-" + i.toString());

        }

        switch (readingNum) {
            case 0:
                map.getLayer("locations-1").setProperties(visibility(NONE));
                map.getLayer("locations-2").setProperties(visibility(NONE));
                break;
            case 1:
                map.getLayer("locations-0").setProperties(visibility(NONE));
                map.getLayer("locations-2").setProperties(visibility(NONE));
                break;
            case 2:
                map.getLayer("locations-0").setProperties(visibility(NONE));
                map.getLayer("locations-1").setProperties(visibility(NONE));
                break;
        }

    }

    /*
    public void setSymbolMarkers(int readingNumber) {
        Log.i(TAG, "Setting map markers");
        map.clear();

        if (symbolManager[readingNumber] == null) {
            symbolManager[readingNumber] = new SymbolManager(mMapView, map);


            List<String[]> places = reading[readingNumber].getPlaces();

            // SYMBOL MANAGER //
            // create symbol manager object

            // add click listeners if desired
            symbolManager[readingNumber].addClickListener(symbol -> Toast.makeText(getContext(),
                    String.format("Symbol clicked %s", symbol.getTextField()),
                    Toast.LENGTH_SHORT).show());

            symbolManager[readingNumber].addLongClickListener(symbol ->
                    Toast.makeText(getContext(),
                            String.format("Symbol long clicked %s", symbol.getTextField()),
                            Toast.LENGTH_SHORT
                    ).show());

            // set non-data-driven properties, such as:
            symbolManager[readingNumber].setIconAllowOverlap(true);
            //symbolManager.setIconTranslate(new Float[]{-4f,5f});
            symbolManager[readingNumber].setIconRotationAlignment(ICON_ROTATION_ALIGNMENT_VIEWPORT);
            symbolManager[readingNumber].setTextAllowOverlap(true);
            symbolManager[readingNumber].setTextRotationAlignment(ICON_ROTATION_ALIGNMENT_VIEWPORT);
            symbolManager[readingNumber].setTextTranslateAnchor(TEXT_ANCHOR_RIGHT);
            symbolManager[readingNumber].setTextTranslate(new Float[]{2.5f, 0f});

            symbolOptionsList = new ArrayList<>();
            locations = new ArrayList<>();

            for (String[] place : places) {

                Double latitude = Double.parseDouble(place[1].replaceAll("[^\\d.]", ""));
                Double longitude = Double.parseDouble(place[2].replaceAll("[^\\d.]", ""));
                LatLng mLatLng = new LatLng(latitude, longitude);
                locations.add(mLatLng);
                symbolOptionsList.add(new SymbolOptions()

                        .withLatLng(mLatLng)
                        .withIconImage(MAKI_ICON_CIRCLE)
                        .withTextField(place[0])
                        //.withTextAnchor("right")
                        .withTextOffset(new Float[]{1f, 0f})
                        .withIconAnchor(ICON_ANCHOR_CENTER)
                        .withIconColor(ColorUtils.colorToRgbaString(Color.RED))
                        .withTextJustify(TEXT_JUSTIFY_LEFT)
                        .withTextAnchor(TEXT_ANCHOR_LEFT)
                        .withTextSize(10f));

            }

            symbolManager[readingNumber].create(symbolOptionsList);
        }

    }
    */

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

    public void zoomExtents(int readingNumber) {

        //Zoom to fit markers
        CameraUpdate cu;

        locations = new ArrayList<>();
        List<String[]> places = reading[readingNumber].getPlaces();


        for (String[] place : places) {

            Double latitude = Double.parseDouble(place[1].replaceAll("[^\\d.]", ""));
            Double longitude = Double.parseDouble(place[2].replaceAll("[^\\d.]", ""));
            locations.add( new LatLng(latitude, longitude));

        }

        int numPlaces = locations.size();

        if ( numPlaces > 0 ) { //Check if markers do exist for this reading

            mapOverlay.setVisibility(View.INVISIBLE);
            map.getUiSettings().setAllGesturesEnabled(true);

            if (numPlaces < 2) { //If there's only one point, zoom in to a reasonable distance

                cu = CameraUpdateFactory.newLatLngZoom(locations.get(0), 6F);

            } else { //Automatically zoom out to fit all points

                LatLngBounds.Builder builder = new LatLngBounds.Builder();

                for (LatLng location : locations) {
                    try {
                        builder.include(location);
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

    public void zoomCameraMarkers() {

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


    public void zoomCameraSymbols() {

        //Zoom to fit markers
        CameraUpdate cu;

        if ( locations.size() > 0 ) { //Check if markers do exist for this reading

            mapOverlay.setVisibility(View.INVISIBLE);
            map.getUiSettings().setAllGesturesEnabled(true);

            if (locations.size() < 2) { //If there's only one point, zoom in to a reasonable distance

                cu = CameraUpdateFactory.newLatLngZoom(locations.get(0), 6F);

            } else { //Automatically zoom out to fit all points

                LatLngBounds.Builder builder = new LatLngBounds.Builder();

                for (LatLng location : locations) {
                    try {
                        builder.include(location);
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
