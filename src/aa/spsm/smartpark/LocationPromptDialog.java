package aa.spsm.smartpark;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

public class LocationPromptDialog extends DialogFragment {
	private String address = "";
    
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
    
    float getDist(Address addr, Location loc) {
    	float[] arr = new float[3];
    	Location.distanceBetween(loc.getLatitude(), loc.getLongitude(), 
        		addr.getLatitude(), addr.getLongitude(), arr);   
    	return arr[0];
    }
    
    private boolean validate_address(String txt) {
    	address = txt;
    	
    	if(txt.equals("")) return false;
		Geocoder geoCoder = new Geocoder(MainActivity.context, Locale.getDefault());    
		try {
		    List<Address> addresses = geoCoder.getFromLocationName(txt, 3);
		    if (addresses.size() > 0) {
		    	Iterator<Address> it = addresses.iterator();
		    	while (it.hasNext()) {
		    		System.out.println(it.next().describeContents());
		    	}
		    	address = addresses.get(0).toString();
		    	MainActivity.address = addresses.get(0);
		    	return true;
		    }
		} catch (IOException e) {
		    e.printStackTrace();
		    return false;
		}
         
    	return false;
    }
    
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		MainActivity.dialogtype = "LocationPromptDialog";
	    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.context);
	    LayoutInflater inflater = LayoutInflater.from(MainActivity.context);
	    View layout = inflater.inflate(R.layout.intro_activity, null);
	    builder.setView(layout);
	    
	    final EditText input_address = (EditText) layout.findViewById(R.id.editText1);
	    final RadioButton radio1 = (RadioButton) layout.findViewById(R.id.currentloc_radio);
	    final RadioButton radio2 = (RadioButton) layout.findViewById(R.id.address_radio);
	    final Button btn = (Button) layout.findViewById(R.id.intro_select);
	    
	    if(MainActivity.address != null) 
	    	input_address.setText(MainActivity.address.getAddressLine(0));
	    
	    radio1.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View arg0, MotionEvent arg1) {
				radio1.setChecked(true);
				radio2.setChecked(false);
				MainActivity.selected = 0;
				input_address.setEnabled(false);
				return false;
			}
	    });
	    
	    radio2.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View arg0, MotionEvent arg1) {
				radio2.setChecked(true);
				radio1.setChecked(false);
				MainActivity.selected = 1;
				input_address.setEnabled(true);
				return false;
			}
	    });
	    
	    btn.setOnClickListener(new OnClickListener() {
			@SuppressWarnings("deprecation")
			public void onClick(View arg0) {
				if(MainActivity.selected == 1) {
					if(validate_address(input_address.getText().toString())) {
						System.out.println("ADDR: " + address);
						mListener.onDialogPositiveClick(LocationPromptDialog.this);
						return;
					}
					
					final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.context).create();
					alertDialog.setTitle("Validation Error");
					alertDialog.setMessage("This address could not be mapped to a destination.");
					alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
					   public void onClick(DialogInterface dialog, int which) {
					      alertDialog.dismiss();
					   }
					});
					alertDialog.show();
					input_address.requestFocus();
				}
				else mListener.onDialogPositiveClick(LocationPromptDialog.this);
			}
	    });
	    return builder.create();
	}
}
