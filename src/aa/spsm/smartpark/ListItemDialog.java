package aa.spsm.smartpark;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

public class ListItemDialog extends DialogFragment {
	private MapFragment mFragment;
	private List<String> directions;
	private GoogleMap mMap;
	private Location origin;
	private ParkingLocation dest;
	private View layout;
	private ExpandableListView list;
    
    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
    
    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;
    
    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }
    
    void setDest(ParkingLocation loc) {
    	dest = loc;
    }
    
    void getDirections() {
    	origin = MainActivity.mLocationClient.getLastLocation();
    	ParserTask parser = new ParserTask();
    	parser.execute();
    }
    
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		MainActivity.dialogtype = "LocationPromptDialog";
	    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.context);
	    LayoutInflater inflater = LayoutInflater.from(MainActivity.context);
	    layout = inflater.inflate(R.layout.expanded_list_item, null);
		
		mFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.structure_map);
		mFragment.setRetainInstance(true);
		mMap = mFragment.getMap();
		mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		mMap.getUiSettings().setZoomControlsEnabled(false);
		getDirections();
		
		TextView name = (TextView) layout.findViewById(R.id.structure_name);
		name.setText(dest.name);
		TextView spaces = (TextView) layout.findViewById(R.id.structure_spaces);
		if(dest.free_spaces > 0) 
			spaces.setText(dest.free_spaces + " spaces left");
		else {
			spaces.setText("Sorry :( Structure is full");
			spaces.setTextColor(Color.RED);
		}
		TextView addr = (TextView) layout.findViewById(R.id.structure_address);
		addr.setText(dest.address);
		TextView price = (TextView) layout.findViewById(R.id.structure_price);
		price.setText("$" + dest.price + "/hr");
		
	    builder.setView(layout)
		    .setPositiveButton("Take me here!", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	                ListItemDialog.this.getDialog().cancel();
	            }
		    })
	        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	            	ListItemDialog.this.getDialog().cancel();
	            }
	        }); 
	    return builder.create();
	}
	
	private class ParserTask extends AsyncTask<Object, String, Boolean> {
	    private StringBuilder query = new StringBuilder();

		@Override
		protected void onPreExecute() {
		    super.onPreExecute();
		}

		protected void onProgressUpdate(String... progress) {
		     Log.d("ANDRO_ASYNC",progress[0]);
		}
	
		@Override
		protected void onPostExecute(final Boolean result) {
			super.onPostExecute(result);
			assert result;
			query.append("http://maps.googleapis.com/maps/api/directions/json?");
			query.append("origin=" + origin.getLatitude() + "," + origin.getLongitude() + "&");
			query.append("destination=" + dest.latlng.latitude + "," + dest.latlng.longitude + "&");
			query.append("sensor=true&"); // Must be true if queried from a device with GPS
			new QueryGoogleDirections().execute(query.toString());
		}
		
		private class QueryGoogleDirections extends AsyncTask<String, String, String> {
		 
		    @Override
		    protected String doInBackground(String... args) {
		        HttpClient httpclient = new DefaultHttpClient();
		        HttpResponse response;
		        String responseString = null;
		        try {
		            response = httpclient.execute(new HttpGet(args[0]));
		            StatusLine statusLine = response.getStatusLine();
		            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
		                ByteArrayOutputStream out = new ByteArrayOutputStream();
		                response.getEntity().writeTo(out);
		                out.close();
		                responseString = out.toString();
		            } else {
		                //Closes the connection.
		                response.getEntity().getContent().close();
		                throw new IOException(statusLine.getReasonPhrase());
		            }
		        } catch (ClientProtocolException e) {
		            Log.e("ERROR", e.getMessage());
		        } catch (IOException e) {
		            Log.e("ERROR", e.getMessage());
		        }
		        return responseString;
		    }
		    
			@Override
		    protected void onPostExecute(String result) {
		        super.onPostExecute(result);
				final LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
				PolylineOptions rectLine = new PolylineOptions().color(Color.BLUE);
				directions = new ArrayList<String>();
				
				JSONObject json = null;
				try {
					json = new JSONObject(result);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            JSONArray arrRoutes = null;
				try {
					arrRoutes = json.getJSONArray("routes");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            ArrayList<LatLng> listGeopoints = new ArrayList<LatLng>();

	            // no routes found
	            if (arrRoutes.length() == 0) {
	            	//TODO add toast here 
	            	Toast.makeText(MainActivity.context, "No routes found", Toast.LENGTH_SHORT).show();
	                return;
	            }

	            JSONArray arrLegs = null;
				try {
					arrLegs = arrRoutes.getJSONObject(0).getJSONArray("legs");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            JSONObject firstLeg = null;
				try {
					firstLeg = arrLegs.getJSONObject(0);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            JSONArray arrSteps = null;
				try {
					arrSteps = firstLeg.getJSONArray("steps");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            int len = arrSteps.length();
	            JSONObject leg = null;

	            // get instructions
	            for (int i = 0; i < len; ++i) {
	                try {
						leg = arrSteps.getJSONObject(i);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                // location = leg.getJSONObject("start_location");
	                String encoded = null;
					try {
						encoded = leg.getJSONObject("polyline").getString("points");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					ArrayList<LatLng> temp = decodePoly(encoded);
					for(int j = 0; j < temp.size(); j++) {
		                listGeopoints.add(temp.get(j));
		                rectLine.add(temp.get(j));
					}

	                try {
	                	System.out.println(Html.fromHtml(leg.getString("html_instructions")).toString());
						directions.add(Html.fromHtml(leg.getString("html_instructions")).toString());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                try {
						Log.d("html_instructions", "" + Html.fromHtml(leg.getString("html_instructions")));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            }
				
	            JSONObject duration = null;
				try {
					duration = json.getJSONObject("duration");
					System.out.println("DURATION: " + duration);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
				String dur = null;
				try {
					dur = duration.get("text").toString();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            
				list = (ExpandableListView) layout.findViewById(R.id.directions_list);
				list.setAdapter(new DirectionsListAdapter(directions, dur));
	            
	            boundsBuilder.include(new LatLng(origin.getLatitude(), origin.getLongitude()));
	            
				boundsBuilder.include(dest.latlng);
				
				Bitmap init = BitmapFactory.decodeResource(MainActivity.context.getResources(), R.drawable.car);
				BitmapDescriptor bm = BitmapDescriptorFactory.fromBitmap(
						Bitmap.createScaledBitmap(init, init.getWidth()/12, init.getHeight()/12, false));
				mMap.addMarker(new MarkerOptions().position(listGeopoints.get(0)).icon(bm));
				
				init = BitmapFactory.decodeResource(MainActivity.context.getResources(), R.drawable.parkingicon);
				bm = BitmapDescriptorFactory.fromBitmap(
						Bitmap.createScaledBitmap(init, init.getWidth()/12, init.getHeight()/12, false));
				mMap.addMarker(new MarkerOptions().position(dest.latlng).icon(bm));
				
				mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100));
		    	mMap.addPolyline(rectLine);
		    }
		}

	    private ArrayList<LatLng> decodePoly(String encoded) {
	        ArrayList<LatLng> poly = new ArrayList<LatLng>();
	        int index = 0, len = encoded.length();
	        int lat = 0, lng = 0;
	        while (index < len) {
	            int b, shift = 0, result = 0;
	            do {
	                b = encoded.charAt(index++) - 63;
	                result |= (b & 0x1f) << shift;
	                shift += 5;
	            } while (b >= 0x20);
	            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
	            lat += dlat;
	            shift = 0;
	            result = 0;
	            do {
	                b = encoded.charAt(index++) - 63;
	                result |= (b & 0x1f) << shift;
	                shift += 5;
	            } while (b >= 0x20);
	            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
	            lng += dlng;

	            LatLng position = new LatLng((double) lat / 1E5, (double) lng / 1E5);
	            poly.add(position);
	        }
	        return poly;
	    }

		@Override
		protected Boolean doInBackground(Object... arg0) {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
