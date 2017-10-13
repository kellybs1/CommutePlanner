package bit.kellybs1.commute;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;


/**
 * MapsActivity class
 * Author: Brendan Kelly
 * Date: May 2017
 * Main maps activity - auto generated Maps Activity to mange the base app
 */

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, INewRouteConfirmer, IEndRouteConfirmer
{
    private final float START_ZOOM = 13.5f;

    private ImageView imageViewAddPoint;
    private ImageView imageViewEndRoute;
    private ImageView imageViewZoomIn;
    private ImageView imageViewZoomOut;
    private GoogleMap mMap;
    private ArrayList<LatLng> userPoints;
    private ArrayList<Marker> userMarkers;
    private LatLng currentPoint;
    private LocationManager locationManager;
    private String providerName;
    private Criteria defaultCriteria;
    private Resources resR;
    private Location firstLocation;
    private MarkerOptions mainMarkerOptions;
    private Marker mainMarker;
    private ArrayList<TextView> tooltips;
    private boolean tooltipsVisible;

    private boolean debugging;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        debugging = true;

        //auto generated
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // end auto generated

        //init global objects
        resR = getResources();
        defaultCriteria = new Criteria();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mainMarkerOptions = new MarkerOptions();

        //toolbar
        //https://developer.android.com/training/appbar/setting-up.html
        Toolbar myToolbar = (Toolbar) findViewById(R.id.maintoolbar);
        myToolbar.setTitle(resR.getString(R.string.better_app_name));
        setSupportActionBar(myToolbar);

        //init click bindings
        imageViewAddPoint = (ImageView) findViewById(R.id.imageViewAdd);
        imageViewAddPoint.setOnClickListener(new OnAddButtonClickHandler());
        imageViewEndRoute = (ImageView)findViewById(R.id.imageViewEndRoute);
        imageViewEndRoute.setOnClickListener(new OnEndRouteButtonClickHandler());
        imageViewZoomIn = (ImageView) findViewById(R.id.imageViewZoomIn);
        imageViewZoomIn.setOnClickListener(new OnZoomInButtonClickHandler());
        imageViewZoomOut = (ImageView) findViewById(R.id.imageViewZoomOut);
        imageViewZoomOut.setOnClickListener(new OnZoomOutButtonClickHandler());

        //set up tooltips
        tooltips = new ArrayList<TextView>();
        TextView textViewAdd = (TextView) findViewById(R.id.textViewAdd);
        TextView textViewCheck = (TextView) findViewById(R.id.textViewCheck);
        TextView textViewUndo = (TextView) findViewById(R.id.textViewUndo);
        TextView textViewNew = (TextView) findViewById(R.id.textViewNew);
        TextView textViewHelp = (TextView) findViewById(R.id.textViewHelp);
        tooltips.add(textViewAdd);
        tooltips.add(textViewCheck);
        tooltips.add(textViewNew);
        tooltips.add(textViewUndo);
        tooltips.add(textViewHelp);
        //they will be visible at startup
        tooltipsVisible = true;
    }


    //---------------------------------------------------
    //ToolTips
    //---------------------------------------------------

    //makes the tooltips visible
    private void showToolTips()
    {
        //main marker tooltips
        mainMarker.showInfoWindow();
        //textview tooltips
        for (TextView tv : tooltips)
        {
            tv.setEnabled(true);
            tv.setVisibility(View.VISIBLE);
        }
        tooltipsVisible = true;
    }

    //hides the tooltips
    private void hideToolTips()
    {
        //main mark tooltip
        mainMarker.hideInfoWindow();
        //textview tooltips
        for (TextView tv : tooltips)
        {
            tv.setEnabled(false);
            tv.setVisibility(View.INVISIBLE);
        }
        tooltipsVisible = false;
    }

    //---------------------------------------------------
    //Dialogues
    //---------------------------------------------------

    //opens the results dialog fragment
    private void openResultsDialog(ArrayList<String> incidents)
    {
        //get a fragment manager
        FragmentManager fm = getFragmentManager();
        //create new results dialogue
        ResultsDialogue resultDialog = new ResultsDialogue();
        //add data
        Bundle data = new Bundle();
        data.putStringArrayList(resR.getString(R.string.key_bundle_incidents), incidents);
        resultDialog.setArguments(data);
        //now show it
        resultDialog.show(fm, "Results");
    }

    //opens the 'about' dialog fragment
    private void openAboutDialogue()
    {
        //get a fragment manager
        FragmentManager fm = getFragmentManager();
        //create new about dialogue and get fragment manager to show it
        AboutDialog abtDialogue = new AboutDialog();
        abtDialogue.show(fm, "About the App");
    }

    //opens the new route confirmation dialog
    private void openNewRouteDialogue()
    {
        //get a fragment manager
        DialogFragment confirmNewDialogue = new YesNoNewDialogue();
        //create and show dialog fragment
        FragmentManager fm = getFragmentManager();
        confirmNewDialogue.show(fm, "NewRoute");
    }

    //opens the end route confirmation dialog
    private void openEndRouteDialogue()
    {
        //get a fragment manager
        DialogFragment confirmEndDialogue = new YesNoGetTrafficDialogue();
        //create and show dialog fragment
        FragmentManager fm = getFragmentManager();
        confirmEndDialogue.show(fm, "GetTraffic");
    }

    //opens the 'route too short' dialogue
    private void openRouteTooShortDialogue()
    {
        //get a fragment manager
        DialogFragment shortDialogue = new RouteShortDialogue();
        //create and show dialog fragment
        FragmentManager fm = getFragmentManager();
        shortDialogue.show(fm, "RouteTooShort");
    }

    //for when new route confirmation dialog is acted upon
    @Override
    public void DataFromNewConfirmDialog(int which)
    {
        switch (which)
        {
            //no
            case Dialog.BUTTON_NEGATIVE:
                //nothing
                break;
            //yes
            case Dialog.BUTTON_POSITIVE:
                //start new route
                initFreshPath();
                break;
        }
    }

    //for when end route confirmation dialog is acted upon
    @Override
    public void DataFromGetTrafficConfirmDialog(int which)
    {
        switch (which)
        {
            //no
            case Dialog.BUTTON_NEGATIVE:
                //nothing
                break;
            //yes
            case Dialog.BUTTON_POSITIVE:
                String query = getRouteAreaSearchParamString();
                BingMapsJSONQuery bingSrch = new BingMapsJSONQuery();
                bingSrch.execute(query);
                break;
        }
    }

    //---------------------------------------------------
    //Maps
    //---------------------------------------------------

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        //initialise map with onclick listener
        mMap = googleMap;
        //if we've got permissions, get the last noted gps location
        firstLocation = initFirstLocationFromLastLocation();
        //disable the directions and search toolbar  via https://developers.google.com/android/reference/com/google/android/gms/maps/UiSettings
        mMap.getUiSettings().setMapToolbarEnabled(false);

        mMap.setOnMapClickListener(new OnMapClickHandler());

        initFreshPath();
    }


    //behaviour for when map is clicked
    public class OnMapClickHandler implements GoogleMap.OnMapClickListener
    {
        @Override
        public void onMapClick(LatLng latLng)
        {
            //hide the tooltips if they're visible
            if (tooltipsVisible)
                hideToolTips();

            //save the clicked point in global variable
            currentPoint = latLng;
            //debugging output
            if (debugging)
            {
                Log.i("map clicked lat", String.valueOf(currentPoint.latitude));
                Log.i("map clicked lon", String.valueOf(currentPoint.longitude));
            }

            //move the marker to the clicked position
            mainMarker.setPosition(currentPoint);
            mMap.animateCamera(CameraUpdateFactory.newLatLng(currentPoint), resR.getInteger(R.integer.MAP_MOVE_ANIM_DURATION), null);
        }
    }


    //undoes everything about the most recent waypoint
    private void undoWayPoint()
    {
        //if there's a point to undo
        if (userPoints.size() > 1)
        {
            mainMarker.remove();
            //find last index
            int indexToRemove = userPoints.size() - 1;
            //remove from screen, markers, and points
            userMarkers.get(indexToRemove).remove();
            userMarkers.remove(indexToRemove);
            userPoints.remove(indexToRemove);
            //move camera to last valid spot
            int newLastIndex = userPoints.size() - 1;
            currentPoint = userPoints.get(newLastIndex);
            mainMarkerOptions.position(currentPoint);
            //draw underneath other markers (basically a hack to make it not cover the previous waypoint)
            mainMarkerOptions.zIndex(-1.0f);
            mainMarker = mMap.addMarker(mainMarkerOptions);
            mMap.animateCamera(CameraUpdateFactory.newLatLng(currentPoint), resR.getInteger(R.integer.MAP_MOVE_ANIM_DURATION), null);
            //show the info on the most recent marker
            userMarkers.get(newLastIndex).showInfoWindow();
        }
        //if there's only one point just start again
       else if (userPoints.size() == 1)
            initFreshPath();
    }

    //---------------------------------------------------
    //Buttons
    //---------------------------------------------------

    //button click listener for zoomout
    public class OnZoomOutButtonClickHandler implements  View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            //zoomout
            mMap.animateCamera(CameraUpdateFactory.zoomOut());
        }
    }

    //button click listener for zoomout
    public class OnZoomInButtonClickHandler implements View.OnClickListener
    {

        @Override
        public void onClick(View v)
        {
            //zoomin
            mMap.animateCamera(CameraUpdateFactory.zoomIn());
        }
    }


    //button click listener for adding a location to list of waypoints
    public class OnAddButtonClickHandler implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            //add the current map point to the list of points
            userPoints.add(currentPoint);
            //get its current point in the list (the last) in human-counting
            int pointPosInList = userPoints.size();
            //add marker to display - make it blue to differentiate from the main marker
            Marker newMarker = mMap.addMarker(new MarkerOptions().position(currentPoint)
                    .title(String.valueOf(pointPosInList)).icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            //add marker to list
            userMarkers.add(newMarker);
            newMarker.showInfoWindow();
            //debugging output
            if (debugging)
            {
                int userPointsL = userPoints.size();
                for (int i = 0; i < userPointsL; i++)
                {
                    Log.i("Lat", i + " " + String.valueOf(userPoints.get(i).latitude));
                    Log.i("Lon", i + " " + String.valueOf(userPoints.get(i).longitude));
                }
            }
        }
    }


    //button click for finishing the route - means it's time to fetch traffic data
    public class OnEndRouteButtonClickHandler implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            //if the user hasn't added at least two point don't continue
            if (userPoints.size() < 2)
            {
                openRouteTooShortDialogue();
            }
            else
            {
                //yes/no confirmation dialogue
                openEndRouteDialogue();
            }
        }
    }

    //---------------------------------------------------
    //Action Bar
    //https://material.io/icons/
    //---------------------------------------------------
    //inflate custom menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    //handle menu selections
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_new:
                //yes/no confirmation dialogue;
                openNewRouteDialogue();
                return true;

            case R.id.action_undo:
                undoWayPoint();
                return true;

            case R.id.action_help:
                //toggle tooltips
                if (tooltipsVisible)
                    hideToolTips();
                else
                    showToolTips();
                return true;

            case R.id.action_about:
                openAboutDialogue();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                Log.i("Bad menu code", "the user's action was not recognized");
                return super.onOptionsItemSelected(item);
        }
    }


    //---------------------------------------------------
    //Miscellaneous Functionality
    //---------------------------------------------------

    //extracts the rectangle to be sent to bing maps to get traffic info
    private String getRouteAreaSearchParamString()
    {
        //divide the LatLng arraylists into separate lists
        ArrayList<Double> allLats = new ArrayList<Double>();
        ArrayList<Double> allLons = new ArrayList<Double>();

        for (int i = 0; i < userPoints.size(); i++)
        {
            allLats.add(userPoints.get(i).latitude);
            allLons.add(userPoints.get(i).longitude);
        }

        //extract the overall rectangle
        double southLat = Collections.min(allLats);
        double northLat = Collections.max(allLats);
        double westLon = Collections.min(allLons);
        double eastLon = Collections.max(allLons);

        //SouthLatitude, WestLongitude, NorthLatitude, and EastLongitude as defined by bing maps
       String concatValues = String.valueOf(southLat);
        concatValues += ",";
        concatValues += String.valueOf(westLon);
        concatValues += ",";
        concatValues += String.valueOf(northLat);
        concatValues += ",";
        concatValues += String.valueOf(eastLon);

        return concatValues;
    }


    //sets up a new path
    private void initFreshPath()
    {
        userPoints = new ArrayList<LatLng>();
        userMarkers = new ArrayList<Marker>();
        mMap.clear();
        //default to somewhere
        currentPoint = new LatLng(-45.878, 170.502); //it's Dunedin
        //if we got a local location from the gps then set the map there instead
        if (firstLocation != null)
        {
            currentPoint = new LatLng(firstLocation.getLatitude(), firstLocation.getLongitude());
        }

        // Add a marker at start then move the camera
        mainMarkerOptions.position(currentPoint);
        mainMarkerOptions.title(resR.getString(R.string.main_marker_info));
        mainMarker = mMap.addMarker(mainMarkerOptions);
        mainMarker.showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPoint));
        //zoom in a bit, eh?
        mMap.animateCamera(CameraUpdateFactory.zoomTo(START_ZOOM));
    }


    //wrapper method that gets the first location for the map from the last recorded gps location
    private Location initFirstLocationFromLastLocation()
    {
        // set first location null
        Location locationFromGPS = null;
        //if we got permission initialise our location
        if (checkLocationPermission())
        {
            providerName = locationManager.getBestProvider(defaultCriteria, false);
            locationManager.requestSingleUpdate(providerName, new FirstLocationListener(), null);
            locationFromGPS = locationManager.getLastKnownLocation(providerName);
        }
        else
        {
            requestLocationPermission();
        }

        return locationFromGPS;
    }

    //---------------------------------------------------
    //JSON - Bing maps traffic info
    //---------------------------------------------------

    public class BingMapsJSONQuery extends AsyncTask<String, Void, String>
    {
        ProgressDialog progD = new ProgressDialog(MapsActivity.this);

        @Override
        protected void onPreExecute()
        {
            progD.setMessage(resR.getString(R.string.loading_json));
            progD.setCancelable(false);
            progD.show();
        }


        //format: http://dev.virtualearth.net/REST/v1/Traffic/Incidents/37,-105,45,-94?key=YourBingMapsKey
        //A bounding box defines an area by specifying SouthLatitude, WestLongitude, NorthLatitude, and EastLongitude values
        @Override
        protected String doInBackground(String... params)
        {
            //declare outside try block
            String JSONString = null;
            try
            {
                //get input string
                String mapArea = params[0];

                if (debugging)
                    Log.i("mapArea", mapArea + " being sent as mapArea");

                //build url to form query
                String urlString = "http://dev.virtualearth.net/REST/v1/Traffic/Incidents/";
                urlString += mapArea;
                urlString += "?key=";
                urlString += resR.getString(R.string.api_key_bing_maps);

                if (debugging)
                    Log.i("URL", urlString);

                // convert url to object
                URL urlObj = new URL(urlString);
                //create htppurlconnection
                HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
                //send the url
                connection.connect();
                //if it doesn't return 200
                int httpCode = connection.getResponseCode();
                if (httpCode != 200)
                {
                    Log.wtf("HTTP error" , "HTTP ERROR "+ httpCode);
                    return "HTTP error: ," + httpCode;
                }
                //get inputstream from httpurlconnection
                InputStream iStream = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(iStream);
                //set up buffered reader
                BufferedReader buffReader = new BufferedReader(inputStreamReader);
                //read the input
                String responseString;
                StringBuilder sb = new StringBuilder();
                while ((responseString = buffReader.readLine()) != null)
                {
                    sb = sb.append(responseString);
                }
                //assign JSON string the stringbuilder's finished string
                JSONString = sb.toString();
                //Log.i("JSON", JSONString);

            }
            catch(Exception e)
            {
                Log.wtf("JSON error" , "Something went wrong accessing JSON data over HTTP");
                e.printStackTrace();
            }
            //return the string
            return JSONString;
        }


        @Override
        protected void onPostExecute(String s)
        {
            //close loading dialogue
            progD.dismiss();

            try
            {
                //extract traffic results from JSON
                JSONObject trafficData = new JSONObject(s);
                JSONArray resourceSets = trafficData.getJSONArray("resourceSets");
                JSONObject resourceSet0 = resourceSets.getJSONObject(0);
                JSONArray trafficIncidents = resourceSet0.getJSONArray("resources");
                int nIncidents = trafficIncidents.length();

                ArrayList<String> incidents = new ArrayList<String>();
                //if results found
                if (nIncidents > 0)
                {
                    for (int i = 0; i < nIncidents; i++)
                    {
                        //extract indvidual incident data
                        JSONObject currentIncident = trafficIncidents.getJSONObject(i);
                        String desc = currentIncident.getString("description");
                        boolean roadClosedQ = currentIncident.getBoolean("roadClosed");

                        // make the boolean a yes/no string instead of true/false
                        String roadOpen = resR.getString(R.string.road_open);
                        if (roadClosedQ)
                            roadOpen = resR.getString(R.string.road_not_open);

                        String incidentStr = desc + "\n" + roadOpen + ".";
                        //add items to list
                        incidents.add(incidentStr);

                        if (debugging)
                            Log.i("incident", i + " d: " + desc + " | " + "roadClosed: " + roadClosedQ);
                    }
                }
                else
                {
                    //if no results found add feedback and don't break things
                    incidents.add(resR.getString(R.string.no_incidents_found));

                    if (debugging)
                        Log.i("0 results", "Empty set returned from server");
                }

                //now open the results
                openResultsDialog(incidents);
            }
            catch (Exception e)
            {
                Log.wtf("JSON error", "Error parsing JSON information");
                e.printStackTrace();
            }
        }
    }


    //---------------------------------------------------
    //Location Listener
    //---------------------------------------------------
    public class FirstLocationListener implements LocationListener
    {
        /*
        this listener should only be used once: at the start of the app
        to pinpoint the current location or somewhere near-ish to the user's
        location.
        As the requestSingleUpdate method automatically generates the location values
        we want to put in locationFromGPS, this entire class doesn't do anything
        but it must exist as requestSingleUpdate requires a listener object
        */
        @Override
        public void onLocationChanged(Location location) {}
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onProviderDisabled(String provider) {}
    }


    //---------------------------------------------------
    //Permissions
    //---------------------------------------------------

    //checks both fine and coarse location permissions
    public boolean checkLocationPermission()
    {
        //get permissions
        int fineOk = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseOk = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        //set return to default false
        boolean granted = false;

        //if both permissions pass then set it true
        if ((fineOk == PackageManager.PERMISSION_GRANTED) && (coarseOk == PackageManager.PERMISSION_GRANTED))
            granted = true;

        return  granted;
    }

    //Raises an ansynchronous permissions dialogue box
    public void  requestLocationPermission()
    {
        //set up array of permission string names
        String[] permissionsToRequest = new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION };
        //ask for permissions on those items
        ActivityCompat.requestPermissions(this, permissionsToRequest, resR.getInteger(R.integer.LOCATION_REQUEST_CODE));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        int resultsCount = grantResults.length;
        boolean allPassed = true;

        //check the array isn't empty (ie, user cancelled without selecting permissions)
        if (resultsCount == 0)
        {
            allPassed = false;
        }
        else
        {
            //check all permissions passed
            for (int i = 0; i < resultsCount; i++)
            {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED)
                    allPassed = false;
            }
        }

        //if all of our permissions are present we can start
        if (allPassed == true)
        {
            checkLocationPermission();
            initFirstLocationFromLastLocation();
        }
        else
        {
            //gracefully handle failure to get permissions
            Log.w("Permissions denied", "User did not allow access");
            Toast.makeText(MapsActivity.this,"Location permissions denied", Toast.LENGTH_LONG).show();
        }
    }


}

