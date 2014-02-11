package aa.spsm.smartpark;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class DirectionsListAdapter extends BaseExpandableListAdapter {
	private List<String> directions = new ArrayList<String>();
	private String duration;
	LayoutInflater inflater;
	
	DirectionsListAdapter(List<String> directions, String duration) {
		this.directions = directions;
		this.duration = duration;
		inflater = LayoutInflater.from(MainActivity.context);
	}
	
	public Object getChild(int groupPosition, int childPosition) {
		return directions.get(childPosition);
	}

	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
        final String instruction = (String) getChild(groupPosition, childPosition);
        
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_expandable_list_item_1, null);
        }

	    TextView item = (TextView) convertView.findViewById(R.id.instruction);
//	    item.setTextAppearance(MainActivity.context, android.R.style.TextAppearance_DeviceDefault_Small);
	    item.setText(instruction);
        return convertView;
	}

	public int getChildrenCount(int groupPosition) {
		return directions.size();
	}

	public Object getGroup(int groupPosition) {
		return directions;
	}

	public int getGroupCount() {
		return 1;
	}

	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
	    if (convertView == null) {
	        convertView = inflater.inflate(android.R.layout.simple_expandable_list_item_1, parent, false);
	    }
	    
	    TextView title = (TextView) convertView.findViewById(R.id.instruction);
	    title.setTextAppearance(MainActivity.context, android.R.style.TextAppearance_DeviceDefault_Medium);
	    title.setTypeface(null, Typeface.BOLD);
	    title.setText("v Directions (" + duration + ")");
	    return convertView;
	}

	public boolean hasStableIds() {
		return true;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}
}