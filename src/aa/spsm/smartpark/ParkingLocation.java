package aa.spsm.smartpark;

import java.text.DecimalFormat;
import android.location.Address;
import android.text.format.Time;

import com.google.android.gms.maps.model.LatLng;

public class ParkingLocation {

	Address addr;
	String name;
	LatLng latlng;
	String address;
	double distance;
	double price;
	int free_spaces;
	Time free_after;
	
    void toMiles(float meters) {
    	double miles = meters * 0.000621371;
    	DecimalFormat twoDForm = new DecimalFormat("#.##");
        distance = Double.valueOf(twoDForm.format(miles));
    }
}
