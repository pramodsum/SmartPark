package aa.spsm.smartpark;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ParkingListView extends ArrayAdapter<ParkingLocation> {
    public int layoutResourceId;
    public ArrayList<ParkingLocation> places;
    
    public ParkingListView(int layoutResourceId, ArrayList<ParkingLocation> places) {
        super(MainActivity.context, layoutResourceId, places);
        this.layoutResourceId = layoutResourceId;
        this.places = places;
    }
 
	@Override
    public View getView(int rowIndex, View convertView, ViewGroup parent) {
        View row = convertView;
        if(null == row) {
            LayoutInflater layout = ((Activity) MainActivity.context).getLayoutInflater();
            row = layout.inflate(R.layout.list_item, null);
        }
        
        LinearLayout right = new LinearLayout(MainActivity.context);
        right.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        
        ParkingLocation place = places.get(rowIndex);
        if(null != place) {
            TextView name = (TextView) row.findViewById(R.id.name);
            name.setGravity(Gravity.CENTER_VERTICAL);
            if(null != name) name.setText(place.name);

            TextView address = (TextView) row.findViewById(R.id.address);
            address.setGravity(Gravity.CENTER_VERTICAL);
            if(null != address) address.setText(place.address);

            TextView price = (TextView) row.findViewById(R.id.price);
            if(null != price) price.setText("$" + Double.toString(place.price) + "/hr");
//            else price.setText("Unknown");

            TextView distance = (TextView) row.findViewById(R.id.distance);
            if(null != distance) distance.setText(Double.toString(place.distance) + " miles away");
            
            TextView spaces = (TextView) row.findViewById(R.id.spaces_left);
    		if(place.free_spaces > 0) 
    			spaces.setText(place.free_spaces + " spaces left");
    		else {
    			spaces.setText("Full :(");
    			spaces.setTextColor(Color.RED);
    		}
        }
        
//        System.out.println("Adding row: " + place.name);
        MainActivity.availLocs.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        MainActivity.availLocs.setText("Available Parking Locations (" + places.size() + ")");
        return row;
    }
}