package com.liftyourheads.dailyreadings.fragments;
import static com.liftyourheads.dailyreadings.activities.MainActivity.curReading;
import static com.liftyourheads.dailyreadings.activities.MainActivity.fragmentManager;
import static com.liftyourheads.dailyreadings.activities.MainActivity.fragments;
import static com.liftyourheads.dailyreadings.activities.MainActivity.reading;
import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.properties;
import static com.mapbox.mapboxsdk.style.expressions.Expression.switchCase;
import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_CENTER;
import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_ANCHOR_LEFT;
import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_JUSTIFY_LEFT;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
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

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.liftyourheads.dailyreadings.App;
import com.liftyourheads.dailyreadings.R;
import com.liftyourheads.dailyreadings.activities.MainActivity;
import com.liftyourheads.dailyreadings.utils.SymbolGeneratorUtil;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;



public class MapFragment extends Fragment implements View.OnTouchListener {

    Context mContext;
    MapboxMap map;
    MapView mMapView;
    FrameLayout mapOverlay;
    ArrayList<LatLng> locations;
    String TAG = "MapFragment";
    //String JSON_CHARSET = "UTF-8";
    View view;
    GeoJsonSource[] layers;

    private static final String MAKI_ICON_CIRCLE_STROKED = "circle-stroked-15";
    private static final String MAKI_ICON_CIRCLE = "circle-15";
    private static final int ICON_CIRCLE_CUSTOM = R.drawable.map_circle_custom;
    static String PROPERTY_SELECTED = "selected";
    static String PROPERTY_VERSES = "verses";
    static String PROPERTY_TYPE = "type";
    static String PROPERTY_TITLE = "name";
    static String LAYER_READING = "reading-places-";
    static String LAYER_CALLOUTS = "callout-layer-";
    static String LAYER_LOCATIONS = "locations-";

    FeatureCollection[] featureCollection;
    public HashMap<String, View> viewMap;

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

        Log.i(TAG,"Creating map fragment");
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG,"Creating map view");

        //super.onCreateView(inflater,container,savedInstanceState);

        //if (view == null) {
            Mapbox.getInstance(requireActivity(), getString(R.string.mapbox_access_token));

            view = inflater.inflate(R.layout.fragment_map, container, false);

            mMapView = view.findViewById(R.id.bibleMapView);
            mapOverlay = view.findViewById(R.id.mapOverlay);
            mMapView.onCreate(savedInstanceState);
            viewMap = new HashMap<>();

            mMapView.onCreate(savedInstanceState);

            getMapAsync(getContext());


        //} else { mMapView.onCreate(savedInstanceState); }

        return view;
    }

    public void getMapAsync(Context context) {

        mMapView.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(MapboxMap mapboxMap) {

                Log.i(TAG,"Retrieving map instance");
                map = mapboxMap;

                for (Integer i=0; i < 3; i++){
                    map.removeLayer(LAYER_LOCATIONS + i.toString());
                    map.removeLayer(LAYER_READING + i.toString());
                    map.removeLayer(LAYER_CALLOUTS + i.toString());
                }

                Log.i(TAG, "Map ready. Loading markers");

                setupSources();
                setupIcons();
                setupReadingLayers();
                setupCalloutViews();

                setCurrentLayer(curReading);
                zoomExtents(curReading);
                setClickListener();

                //Todo: fix screen rotate crash

                ////////// OFFLINE MAP DATA STORAGE /////////


                //setSymbolMarkers(curReading);
                //zoomCameraSymbols();


            }
        });

    }

    public void refreshMap() {

        getMapAsync(getContext());

        //setupSources();
        //setupIcons();
        //setupReadingLayers();
        //setupCalloutViews();

    }

    public void setupCalloutViews(){
        HashMap<String, Bitmap> imagesMap = new HashMap<>();
        LayoutInflater inflater = LayoutInflater.from(App.getContext());

        for (int i = 0; i < 3; i++) {
            for (Feature feature : featureCollection[i].features()) {
                View view = inflater.inflate(R.layout.layout_info_window, null);

                String name = feature.getStringProperty(PROPERTY_TITLE);
                TextView titleTv = view.findViewById(R.id.title);
                titleTv.setText(name);

                String type = feature.getStringProperty(PROPERTY_TYPE);
                TextView styleTv = view.findViewById(R.id.type);
                styleTv.setText(type);

                String verses = feature.getStringProperty(PROPERTY_VERSES);
                TextView versesTv = view.findViewById(R.id.verses);
                versesTv.setText(verses);

                //boolean favourite = feature.getBooleanProperty(PROPERTY_FAVOURITE);
                //ImageView imageView = (ImageView) view.findViewById(R.id.logoView);
                //imageView.setImageResource(favourite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);

                Bitmap bitmap = SymbolGeneratorUtil.generate(view);
                imagesMap.put(name, bitmap);
                viewMap.put(name, view);
            }
        }

        Log.i("MapFragment","Finished creating callout views");

        setImageGenResults(viewMap, imagesMap);

        for (Integer i = 0; i < 3; i++) {
            SymbolLayer[] calloutLayer = new SymbolLayer[3];
            calloutLayer[i] = new SymbolLayer(LAYER_CALLOUTS + i.toString(), LAYER_READING + i.toString());
            map.addLayer(calloutLayer[i]
                    .withProperties(
                            iconImage("{name}"),
                            iconAnchor(Property.ICON_ANCHOR_BOTTOM_LEFT),
                            iconOffset(new Float[]{-20.0f, -10.0f}))
                    .withFilter(eq(get(PROPERTY_SELECTED), literal(true))));
        }

            refreshSource();

    }

    public void setClickListener(){

        map.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng point) {
                PointF screenPoint = map.getProjection().toScreenLocation(point);
                List<Feature> features = map.queryRenderedFeatures(screenPoint, "locations-0","locations-1","locations-2");
                if (!features.isEmpty()) {
                    handleClickIcon(screenPoint);
                    //Feature feature = features.get(0);
                    //PointF symbolScreenPoint = map.getProjection().toScreenLocation(convertToLatLng(feature));
                    //handleClickCallout(feature, screenPoint, symbolScreenPoint);
                } else {
                    deselectAll();
                    refreshSource();
                }

            }
        });
    }

    private LatLng convertToLatLng(Feature feature) {
        Point symbolPoint = (Point) feature.geometry();
        return new LatLng(symbolPoint.latitude(), symbolPoint.longitude());
    }

    private void handleClickIcon(PointF screenPoint) {
        List<Feature> features = map.queryRenderedFeatures(screenPoint, "locations-0","locations-1","locations-2");
        if (!features.isEmpty()) {
            String title = features.get(0).getStringProperty(PROPERTY_TITLE);
            List<Feature> featureList = featureCollection[curReading].features();
            for (int i = 0; i < featureList.size(); i++) {
                if (featureList.get(i).getStringProperty(PROPERTY_TITLE).equals(title)) {
                    setSelected(i);
                }
            }
        }
    }

    private void setSelected(int index) {

        deselectAll();

        Feature feature = featureCollection[curReading].features().get(index);
        selectFeature(feature);
        //animateCameraToSelection(feature);
        refreshSource();

    }

    private void refreshSource() {
        if (layers[curReading] != null && featureCollection[curReading] != null) {
            layers[curReading].setGeoJson(featureCollection[curReading]);
        }
    }

    /**
     * Deselects the state of all the features
     */
    private void deselectAll() {
        for (Feature feature : featureCollection[curReading].features()) {
            feature.addBooleanProperty(PROPERTY_SELECTED,false);
        }
    }

    private void selectFeature(Feature feature) {
        feature.addBooleanProperty(PROPERTY_SELECTED, true);
    }

    private void handleClickCallout(Feature feature, PointF screenPoint, PointF symbolScreenPoint) {
        View view = viewMap.get(feature.getStringProperty(PROPERTY_TITLE));
        //View textContainer = view.findViewById(R.id.text_container);

        // create hitbox for textView
        Rect hitRectText = new Rect();
        //textContainer.getHitRect(hitRectText);

        // move hitbox to location of symbol
        hitRectText.offset((int) symbolScreenPoint.x, (int) symbolScreenPoint.y);

        // offset vertically to match anchor behaviour
        hitRectText.offset(0, -view.getMeasuredHeight());

        // hit test if clicked point is in textview hitbox
        if (hitRectText.contains((int) screenPoint.x, (int) screenPoint.y)) {
            // user clicked on text
            String callout = feature.getStringProperty("verses");
            Toast.makeText(getActivity(), callout, Toast.LENGTH_LONG).show();
        } else {
            // user clicked on icon
            List<Feature> featureList = featureCollection[curReading].features();
            for (int i = 0; i < featureList.size(); i++) {
                if (featureList.get(i).getStringProperty(PROPERTY_TITLE).equals(feature.getStringProperty(PROPERTY_TITLE))) {
                    //toggleFavourite(i);
                }
            }
        }
    }


    public void setImageGenResults(HashMap<String, View> viewMap, HashMap<String, Bitmap> imageMap) {
        if (map != null) {
            // calling addImages is faster as separate addImage calls for each bitmap.
            map.addImages(imageMap);
        }
        // need to store reference to views to be able to use them as hitboxes for click events.
        this.viewMap = viewMap;
    }

    private Feature getSelectedFeature() {
        if (featureCollection[curReading] != null) {
            for (Feature feature : featureCollection[curReading].features()) {
                if (feature.getBooleanProperty(PROPERTY_SELECTED)) {
                    return feature;
                }
            }
        }

        return null;
    }

    public void setupSources(){
        layers = new GeoJsonSource[3];
        String readingSourceString;
        featureCollection = new FeatureCollection[3];

        for (Integer i = 0; i < 3; i++) {
                readingSourceString = reading[i].getPlacesAsString();
                featureCollection[i] = FeatureCollection.fromJson(readingSourceString);
                layers[i] = new GeoJsonSource(LAYER_READING + i.toString(),featureCollection[i]);
                map.addSource(layers[i]);
        }

    }

    public void setupIcons() {

        //IconFactory iconFactory = IconFactory.getInstance(getContext());
        //Icon icon = iconFactory.fromResource(ICON_CIRCLE_CUSTOM);
        Bitmap custom_icon = drawableToBitmap(App.getContext().getResources().getDrawable(R.drawable.map_circle_custom));

        //Icon icon = IconUtils.drawableToIcon(getContext(),R.drawable.map_circle_custom,255,"marker");

        //Bitmap icon = BitmapFactory.decodeResource(getActivity().getResources(),R.drawable.map_circle_custom);
        if (map != null) map.addImage("icon_circle_custom",custom_icon);

    }

    public void setupReadingLayers() {

        SymbolLayer[] symbolLayers = new SymbolLayer [3];

        if (map != null) {
            for (Integer i = 0; i < 3; i++) {

                Log.i(TAG, "Iterating to layer " + i.toString());

                map.removeLayer(LAYER_LOCATIONS + i.toString()); //Clear existing layers as needed

                symbolLayers[i] = new SymbolLayer(LAYER_LOCATIONS + i.toString(), LAYER_READING + i.toString());
                symbolLayers[i].setProperties(
                        visibility(VISIBLE),
                        iconImage("icon_circle_custom"),
                        iconAllowOverlap(true),
                        iconAnchor(ICON_ANCHOR_CENTER),
                        iconSize(switchCase(
                                get(PROPERTY_SELECTED),
                                literal(1.5f),
                                literal(1.0f))),
                        textAllowOverlap(false),
                        textOptional(true),
                        textField("{name}"),
                        textOffset(new Float[]{1f, 0f}),
                        textColor(App.getContext().getResources().getColor(R.color.colorDark)),
                        textHaloColor(Color.LTGRAY),
                        textHaloBlur(1.5f),
                        textHaloWidth(1f),
                        textJustify(TEXT_JUSTIFY_LEFT),
                        textAnchor(TEXT_ANCHOR_LEFT),
                        textSize(12f)
                );
                map.addLayer(symbolLayers[i]);

            }
        }


    }

    public void setCurrentLayer(int readingNum) {

        if (map != null) {
            Layer curLayer = map.getLayer(LAYER_LOCATIONS + Integer.toString(readingNum));

            curLayer.setProperties(visibility(VISIBLE));

            Layer[] layers = new Layer[3];

            for (Integer i = 0; i < 3; i++) {

                layers[i] = map.getLayer(LAYER_LOCATIONS + i.toString());

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
        } else {


            Log.i(TAG,"Unable to find map reference!");

        }

    }

    public Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap;
        int px = App.getContext().getResources().getDimensionPixelSize(R.dimen.map_dot_marker_size);

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
        List<Map<String,String>> places = reading[readingNumber].getPlaces();


        for (Map<String,String> place : places) {

            Double latitude = Double.parseDouble(place.get("latitude"));
            Double longitude = Double.parseDouble(place.get("longitude"));
            locations.add( new LatLng(latitude, longitude));

        }

        int numPlaces = locations.size();

        if (map != null) {

            if (numPlaces > 0) { //Check if markers do exist for this reading

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
                cu = CameraUpdateFactory.newLatLngZoom(new LatLng(33.813, 33.781), 4F);
                mapOverlay.setVisibility(View.VISIBLE);
                map.getUiSettings().setAllGesturesEnabled(false);
            }

            map.animateCamera(cu, 1500);

        }

    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        Log.i(TAG,"Attaching Map");
        super.onAttach(context);
        mContext = context;
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

    /*
    @Override
    public void onDestroy() {
        Log.i(TAG,"Destroying Map");
        super.onDestroy();
        //mMapView.onDestroy();
    }
    */

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        mMapView.onCreate(savedInstanceState);

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
