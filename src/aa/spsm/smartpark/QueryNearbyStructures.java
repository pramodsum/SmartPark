package aa.spsm.smartpark;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class QueryNearbyStructures {
	private LatLng location;
	BitmapDescriptor bm;
	private ProgressDialog progress;
	
	void setLocation(Location loc) {
		location = new LatLng(loc.getLatitude(), loc.getLongitude());
	}
	
	void setLocation(Address loc) {
		location = new LatLng(loc.getLatitude(), loc.getLongitude());
	}
	
	void execute() {
		ParserTask pt = new ParserTask();
		pt.execute(location.latitude, location.longitude);
	}
	
	private class ParserTask extends AsyncTask<Object, String, Boolean> {
	    private String latitude;
	    private String longitude;
	    private final String APIKEY = "AIzaSyCw3H2OEk1tNuSNyn0QXYYI_YrWYpTGnjY";
	    private StringBuilder query = new StringBuilder();
	    
	    ParserTask() {
	    	latitude = String.valueOf(location.latitude);
	    	longitude = String.valueOf(location.longitude);
	    }

		@Override
		protected void onPreExecute() {
		    super.onPreExecute();
		}
	
		protected Document loadXMLFromString(final String xml) throws Exception {
			final DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			final DocumentBuilder builder = factory.newDocumentBuilder();
	
			final InputSource is = new InputSource(new StringReader(xml));
	;
			return builder.parse(is);
		}
	
		@Override
		protected Boolean doInBackground(final Object... myLocationObjs) {
			if (null != latitude && null != longitude)
				return true;
			else
				return false;
		}
	
		@Override
		protected void onPostExecute(final Boolean result) {
			super.onPostExecute(result);
			assert result;
			query.append("https://maps.googleapis.com/maps/api/place/nearbysearch/xml?");
			query.append("location=" + latitude + "," + longitude + "&");
			query.append("types=parking&");
			query.append("keyword=parking+structures&");
			query.append("rankby=distance&");
			query.append("sensor=true&"); // Must be true if queried from a device with GPS
			query.append("key=" + APIKEY);
			new QueryGooglePlaces().execute(query.toString());
		}
		
		private class QueryGooglePlaces extends AsyncTask<String, Integer, String> {
		 
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
			protected void onPreExecute() {
			    super.onPreExecute();
			    progress = new ProgressDialog(MainActivity.context);
			    progress.show();
			}

			@Override
			protected void onProgressUpdate(Integer... values) {
				super.onProgressUpdate(values);
			    progress.setProgress(values[0]);
			}
		    
			@Override
		    protected void onPostExecute(String result) {
		        super.onPostExecute(result);
		        try {
    				final LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
    				Bitmap init = BitmapFactory.decodeResource(MainActivity.context.getResources(), R.drawable.parkingicon);
    				bm = BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(init, init.getWidth()/12, init.getHeight()/12, false));
		            Document xmlResult = loadXMLFromString(result);
		            NodeList nodeList =  xmlResult.getElementsByTagName("result");
		            
		            for(int i = 0, length = nodeList.getLength(); i < length; i++) {
		                Node node = nodeList.item(i);
		                
		                if(node.getNodeType() == Node.ELEMENT_NODE) {
		                    Element nodeElement = (Element) node;
		                    ParkingLocation place = new ParkingLocation();
		                    
		                    Node name = nodeElement.getElementsByTagName("name").item(0);
	                        Node vicinity = nodeElement.getElementsByTagName("vicinity").item(0);
		                    Node geometryElement = nodeElement.getElementsByTagName("geometry").item(0);
		                    
		                    NodeList locationElement = geometryElement.getChildNodes();
		                    
		                    Element latLngElem = (Element) locationElement.item(1);
		                    
		                    Node lat = latLngElem.getElementsByTagName("lat").item(0);
		                    Node lng = latLngElem.getElementsByTagName("lng").item(0);
	
	                        float[] geometry =  {Float.valueOf(lat.getTextContent()),
	                                Float.valueOf(lng.getTextContent())};
		                    
		                    place.name = name.getTextContent();
		                    place.address = vicinity.getTextContent();
		                    place.latlng = new LatLng(geometry[0], geometry[1]);
		                    float[] arr = new float[3];
		                    Location.distanceBetween(location.latitude, location.longitude, 
		                    		place.latlng.latitude, place.latlng.longitude, arr);
		                    if(arr.length > 0) place.toMiles(arr[0]);
		                    
		                    Geocoder geoCoder = new Geocoder(MainActivity.context, Locale.getDefault());    
		            		try {
		            		    List<Address> addresses = geoCoder.getFromLocationName(place.address, 3);
		            		    if (addresses.size() > 0) {
		            		    	Iterator<Address> it = addresses.iterator();
		            		    	while (it.hasNext()) {
		            		    		System.out.println(it.next().getAddressLine(0));
		            		    	}
		            		    	place.addr = addresses.get(0);
		            		    }
		            		} catch (IOException e) {
		            		    e.printStackTrace();
		            		}
		            		
		                    MainActivity.places.add(place);
		                    
		                    MainActivity.mMap.addMarker(new MarkerOptions()
			                    .position(place.latlng)
			                    .icon(bm)
			                    .title(place.name)
			                    .snippet(Integer.toString(place.free_spaces) + " spaces left"));
		    				boundsBuilder.include(place.latlng);
		                }
		            }
				    
		            ParkingListView placeAdapter = new ParkingListView(R.id.listView1, MainActivity.places);
		            MainActivity.parkingList.setAdapter(placeAdapter);
		            placeAdapter.notifyDataSetChanged();
		            
					LatLng loc = new LatLng(location.latitude, location.longitude);
					boundsBuilder.include(loc);
    				MainActivity.mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100));
    				progress.dismiss();
		 
		        } catch (Exception e) {
		            Log.e("ERROR", e.getMessage());
		        }
		    }
		}
	}
}
