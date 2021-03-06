package com.ecs.google.maps.v2.fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;

import com.ecs.google.maps.v2.actionbarsherlock.R;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Key;

public class DirectionsInputActivity extends FragmentActivity {

	private static final String PLACES_API_KEY = "AIzaSyB1k1X5cGfk1Wma3ewD2Xg-FmFSOOnK_J4";
	
	
	private static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();

	protected static final int RESULT_CODE = 123;
	private AutoCompleteTextView from;
	private AutoCompleteTextView to;

	private LocationManager locationManager;
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.directions_input);
		
		//to = (EditText) findViewById(R.id.to);
		Button btnLoadDirections = (Button) findViewById(R.id.load_directions);
		
		btnLoadDirections.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent data = new Intent();
				String s = getCurrentPosition();
				data.putExtra("from", s);
				data.putExtra("to", to.getText().toString());
				DirectionsInputActivity.this.setResult(RESULT_CODE, data);
				DirectionsInputActivity.this.finish();
			}
		});
		

		from = (AutoCompleteTextView) findViewById(R.id.from);
		to = (AutoCompleteTextView) findViewById(R.id.to);

		from.setText("Fisherman's Wharf, San Francisco, CA, United States");
		to.setText("2123, Shady Ave, Pittsburgh, PA, United States");

		from.setAdapter(new PlacesAutoCompleteAdapter(this, android.R.layout.simple_dropdown_item_1line));
		to.setAdapter(new PlacesAutoCompleteAdapter(this, android.R.layout.simple_dropdown_item_1line));
		
	}

	  private class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
		    private ArrayList<String> resultList;
		    
		    public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
		        super(context, textViewResourceId);
		    }
		    
		    @Override
		    public int getCount() {
		        return resultList.size();
		    }

		    @Override
		    public String getItem(int index) {
		        return resultList.get(index);
		    }

		    @Override
		    public Filter getFilter() {
		        Filter filter = new Filter() {
		            @Override
		            protected FilterResults performFiltering(CharSequence constraint) {
		                FilterResults filterResults = new FilterResults();
		                if (constraint != null) {
		                    // Retrieve the autocomplete results.
		                    resultList = autocomplete(constraint.toString());
		                    
		                    // Assign the data to the FilterResults
		                    filterResults.values = resultList;
		                    filterResults.count = resultList.size();
		                }
		                return filterResults;
		            }

		            @Override
		            protected void publishResults(CharSequence constraint, FilterResults results) {
		                if (results != null && results.count > 0) {
		                    notifyDataSetChanged();
		                }
		                else {
		                    notifyDataSetInvalidated();
		                }
		            }};
		        return filter;
		    }
		}	
	  
	  private static final String PLACES_AUTOCOMPLETE_API = "https://maps.googleapis.com/maps/api/place/autocomplete/json";
	  
	  private ArrayList<String> autocomplete(String input) {
		    
		  	ArrayList<String> resultList = new ArrayList<String>();
		    
		    try {
		    
	   		 	HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
						 @Override
					     public void initialize(HttpRequest request) {
							 request.setParser(new JsonObjectParser(JSON_FACTORY));
						 }
				     }
	   		 	);
			    
	   		 	GenericUrl url = new GenericUrl(PLACES_AUTOCOMPLETE_API);
		    	url.put("input", input);
		    	url.put("key", PLACES_API_KEY);
		    	url.put("sensor",false);
	   	 
			    HttpRequest request = requestFactory.buildGetRequest(url);
			    HttpResponse httpResponse = request.execute();
			    PlacesResult directionsResult = httpResponse.parseAs(PlacesResult.class);
	
			    List<Prediction> predictions = directionsResult.predictions;
			    for (Prediction prediction : predictions) {
			    	resultList.add(prediction.description);
				}
		    } catch (Exception ex) {
		    	ex.printStackTrace();
		    }
		    return resultList;
		}	
	  
	  public static class PlacesResult {

		    @Key("predictions")
		    public List<Prediction> predictions;

		  }

	  public static class Prediction {
		  @Key("description")
		  public String description;
		  
		  @Key("id")
		  public String id;
		  
	  }
	  
	  private String getCurrentPosition(){
		//get the location Manger
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		//define the criteria how to select the location provider
		Criteria criteria = new Criteria();
		String provider = locationManager.getBestProvider(criteria, false);
		Location location = locationManager.getLastKnownLocation(provider);
		// Initialize the location fields
		provider = locationManager.getBestProvider(criteria, false);
		if (location != null) {
			System.out.println("Provider " + provider
					+ " has been selected.");
			return onLocationChanged(location);
		}
		return null;
	  }
	  
	  private String onLocationChanged(Location location) {
			double lat = location.getLatitude();
			double lng = location.getLongitude();

			Geocoder geocoder;
			List<Address> addresses = null;
			String address = null;
			String city = null;
			String country = null;
			geocoder = new Geocoder(this, Locale.getDefault());
			
			try {
				addresses = geocoder.getFromLocation(lat, lng, 1);
				address = addresses.get(0).getAddressLine(0);
				city = addresses.get(0).getAddressLine(1);
				country = addresses.get(0).getAddressLine(2);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//do sth.
			if(address == null)
				return null;
			return address + " ," + city + " ," + country;
		}
}
