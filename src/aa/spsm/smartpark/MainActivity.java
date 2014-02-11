package aa.spsm.smartpark;

import java.util.ArrayList;

import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import aa.spsm.smartpark.R;
import android.app.*;
import android.content.*;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;

import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 
 * @author pramodsum
 *
 */
public class MainActivity extends Activity implements 
		LocationPromptDialog.NoticeDialogListener,
		ListItemDialog.NoticeDialogListener {
	
    protected static LocationClient mLocationClient;
    protected static Location mCurrentLocation;
    static Context context;
    static TextView availLocs;
    static QueryNearbyStructures queryNearby;
    CheckGooglePlayServices checkPlay;
    static ListView parkingList;
    static ArrayList<ParkingLocation> places = new ArrayList<ParkingLocation>();
	static MapFragment mFragment;
	static GoogleMap mMap;
    static int selected = 0; 
    static Address address;
    static String dialogtype;
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		context = this;
		checkPlay = new CheckGooglePlayServices();
		queryNearby = new QueryNearbyStructures();
		initViews();
		initMap();
		
		DialogFragment dialog = new LocationPromptDialog();
        dialog.show(getFragmentManager(), "LocationPromptDialog");
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	}
	
	protected void populateFromCurrLoc() {
		places.clear();
		Location prev = mCurrentLocation;
		
		mCurrentLocation = mLocationClient.getLastLocation();
	    
		if(prev != mCurrentLocation) {
			queryNearby.setLocation(mLocationClient.getLastLocation());
			queryNearby.execute();
		}
		else {
			Toast.makeText(context, "Your location hasn't changed.", Toast.LENGTH_SHORT).show();
		}
	}
	
	protected void populateFromAddress() {
		places.clear();
		queryNearby.setLocation(address);
		queryNearby.execute();
	}
	
	private void initMap() {
		mFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
		mFragment.setRetainInstance(true);
		mMap = mFragment.getMap();
		mMap.setMyLocationEnabled(true);
		mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		mMap.getUiSettings().setZoomControlsEnabled(false);
	}
	
	private void initViews() {
		availLocs = (TextView) findViewById(R.id.textView1);
		parkingList = (ListView) findViewById(R.id.listView1);
    	parkingList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {
				DialogFragment dialog = new ListItemDialog();
				((ListItemDialog) dialog).setDest(places.get(pos));
		        dialog.show(getFragmentManager(), "ListItemDialog");
			}
    	});
		
		mLocationClient = new LocationClient(this, checkPlay, checkPlay);
    	mLocationClient.connect();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.find_parking:
	        	DialogFragment dialog = new LocationPromptDialog();
	            dialog.show(getFragmentManager(), "LocationPromptDialog");
	            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	            return true;
//	        case R.id.help:
//	            showHelp();
//	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	public void onDialogPositiveClick(DialogFragment dialog) {
		
		if(dialogtype.equals("LocationPromptDialog")) {
			dialog.dismiss();
			if(selected == 0) {
				Toast.makeText(context, "Current Location Selected", Toast.LENGTH_SHORT).show();
				populateFromCurrLoc();
			}
			else {
				Toast.makeText(context, "Address Selected: " + address.getAddressLine(0), Toast.LENGTH_SHORT).show();
				populateFromAddress();
			}
		}
		else if(dialogtype.equals("ListItemDialog")) {
			
		}
	}

	public void onDialogNegativeClick(DialogFragment dialog) {
		dialog.dismiss();
	}
}
