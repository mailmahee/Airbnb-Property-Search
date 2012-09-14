import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import au.com.bytecode.opencsv.CSVReader;


//Uses CSV parse library here: http://opencsv.sourceforge.net/

public class Solution {
	private static final String PROPERTIES = "Properties";
	private static final String DATES = "Dates";
	private static final String SEARCHES = "Searches";
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd"); 
	
	private ArrayList<AirbnbProperty> propertyList = new ArrayList<AirbnbProperty>();
	private ArrayList<AirbnbDate> dateList = new ArrayList<AirbnbDate>();
	private ArrayList<AirbnbSearchCriteria> searchList = new ArrayList<AirbnbSearchCriteria>();

	public void parseInputFile() {
		try {
			CSVReader reader = new CSVReader(new FileReader("input00.txt"));
			
			boolean inPropertySection = false;
			boolean inDateSection = false;
			boolean inSearchSection = false;
			
			String [] nextLine;
		    while ((nextLine = reader.readNext()) != null) {
		    	if(nextLine[0].equals(PROPERTIES)) {
		    		inPropertySection = true;
		    		inDateSection = false;
					inSearchSection = false;
		    	} else if(nextLine[0].equals(DATES)) {
		    		inPropertySection = false;
		    		inDateSection = true;
					inSearchSection = false;
		    	} else if(nextLine[0].equals(SEARCHES)) {
		    		inPropertySection = false;
		    		inDateSection = false;
					inSearchSection = true;
		    	} else {
		    		if(inPropertySection) {
			    		propertyList.add(new AirbnbProperty(nextLine[0], nextLine[1], nextLine[2], nextLine[3]));
			    	}
			    	
			    	if(inDateSection) {
			    		dateList.add(new AirbnbDate(nextLine[0], nextLine[1], nextLine[2], nextLine[3]));
			    	}
			    	
			    	if(inSearchSection) {
			    		searchList.add(new AirbnbSearchCriteria(nextLine[0], nextLine[1], nextLine[2], nextLine[3], nextLine[4]));
			    	}
			    	
			    	ArrayList<AirbnbSearchOutput> allSearchMatches = new ArrayList<AirbnbSearchOutput>();
			    	for(AirbnbSearchCriteria searchCriteria : searchList) {
			    		ArrayList<AirbnbSearchOutput> matches = findPropertyMatches(searchCriteria);
			    		allSearchMatches.addAll(matches);
			    	}
			    	
			    	for(AirbnbSearchOutput output : allSearchMatches) {
			    		System.out.println(output.toString());
			    	}
		    	}
		    
		    }  
		    
		    reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	   
	}
	
	private ArrayList<AirbnbSearchOutput> findPropertyMatches(AirbnbSearchCriteria searchCriteria) {
		ArrayList<AirbnbSearchOutput> results = new ArrayList<AirbnbSearchOutput>();
		Collections.sort(propertyList, new AirbnbPropertyComparator());
		
		for(AirbnbProperty property : propertyList) {	
			int totalPrice = 0;
			
			if(isInGeographicRange(searchCriteria, property) && isAvailableInDateRange(searchCriteria, property, totalPrice)) {
				AirbnbSearchOutput searchOutput = new AirbnbSearchOutput(searchCriteria.getSearchId(), property.getPropertyId(), totalPrice);
				results.add(searchOutput);
			}
		}

		Collections.sort(results, new AirbnbSearchOutputComparator());
		
		//update the rank ID, post-sorting by total price
		int rank = 0;
		for(AirbnbSearchOutput result : results) {
			result.setRank(rank);
			rank++;
		}
		
		if(results.size() > 10) {
			//grab the top 10 values
			ArrayList<AirbnbSearchOutput> rtnList = new ArrayList<AirbnbSearchOutput>();
			for(int i = 0; i < 10; i++) {
				rtnList.add(results.get(i));
			}
			
			return rtnList;
		} else {
			return results;
		}
	}
	
	private boolean isInGeographicRange(AirbnbSearchCriteria searchCriteria, AirbnbProperty property) {
		float propertyLong = property.getLng();
		float propertyLat = property.getLat();
		float searchLong = searchCriteria.getLng();
		float searchLat = searchCriteria.getLat();
		
		if(propertyLong > searchLong - 1 && propertyLong < searchLong + 1) {
			if(propertyLat > searchLat - 1 && propertyLat < searchLat + 1) {
				return true;
			}
		}
		
		return false;
	}
	
	
	//Uses JODA for date comparison: http://joda-time.sourceforge.net/
	
	private boolean isAvailableInDateRange(AirbnbSearchCriteria searchCriteria, AirbnbProperty property, int totalPrice) {
		ArrayList<AirbnbDate> availableDates = new ArrayList<AirbnbDate>();
		for(AirbnbDate date : dateList) {
			if(date.getPropertyId() == property.getPropertyId() && date.getAvailability() == 1) {
				availableDates.add(date);
			}
		}
		
		DateTime start = new DateTime(searchCriteria.getCheckin());
		DateTime end = new DateTime(searchCriteria.getCheckout());
		Interval interval = new Interval(start, end);
		
		DateTime test = new DateTime(2010, 5, 25, 16, 0, 0, 0);
		System.out.println(interval.contains(test));

	    gcal.setTime(start);
	    
	    //iterate through all dates in search criteria range and confirm they are all available
	    while (gcal.getTime().before(end)) {
	        gcal.add(Calendar.DAY_OF_YEAR, 1);
	        if(!availableDates.contains(gcal.getTime())) {
	        	return false;
	        } else {
	        	int nightlyPrice = availableDates.get(availableDates.indexOf(gcal.getTime())).getPrice();
	        	totalPrice += nightlyPrice;
	        }
	    }
	    
	    return true;
	}
	
	//main method
	public static void main(String[] args) {
		Solution solution = new Solution();
		solution.parseInputFile();
	}
	
	public class AirbnbPropertyComparator implements Comparator<AirbnbProperty> {
		@Override
		public int compare(AirbnbProperty p1, AirbnbProperty p2) {
			int compare = (p1.getNightlyPrice() > p2.getNightlyPrice()) ? 1 : 0;
			if(compare == 0) {
				compare = (p1.getPropertyId() > p2.getPropertyId()) ? 1 : 0;
				if(compare == 0){
				    compare = (p1.getPropertyId() == p2.getPropertyId()) ? 0 : -1;
				}
			}
			
			return compare;
		}
	}
	
	public class AirbnbSearchOutputComparator implements Comparator<AirbnbSearchOutput> {
		@Override
		public int compare(AirbnbSearchOutput s1, AirbnbSearchOutput s2) {
			return (new Integer(s1.getTotalPrice())).compareTo(new Integer(s2.getTotalPrice()));
		}
	}

	public class AirbnbDate {
		private int propertyId;
		private Date date; 	//a date in the format YYYY-MM-DD
		private int availability; //0 if the property is unavailable, 1 if the property is available
		private int price;  //price for the night; an integer in dollars (no cents)
		
		public AirbnbDate(String propertyId, String date, String availability, String price) {
		    try {
		    	this.propertyId = Integer.parseInt(propertyId);
				this.date = dateFormat.parse(date);
				this.availability = Integer.parseInt(availability);
				this.price = Integer.parseInt(price);
			} catch (ParseException e) {
				e.printStackTrace();
			} 
		}

		public int getPropertyId() {
			return propertyId;
		}

		public void setPropertyId(int propertyId) {
			this.propertyId = propertyId;
		}

		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}

		public int getAvailability() {
			return availability;
		}

		public void setAvailability(int availability) {
			this.availability = availability;
		}

		public int getPrice() {
			return price;
		}

		public void setPrice(int price) {
			this.price = price;
		}
	}
	
	public class AirbnbProperty {
		private int propertyId;
		private float lat;
		private float lng;
		private int nightlyPrice; //the standard nightly price; an integer in dollars (no cents)
		
		AirbnbProperty(String propertyId, String lat, String lng, String nightlyPrice) {
			this.propertyId = Integer.parseInt(propertyId);
			this.lat = Float.parseFloat(lat);
			this.lng = Float.parseFloat(lng);
			this.nightlyPrice = Integer.parseInt(nightlyPrice);
		}

		public int getPropertyId() {
			return propertyId;
		}

		public void setPropertyId(int propertyId) {
			this.propertyId = propertyId;
		}

		public float getLat() {
			return lat;
		}

		public void setLat(float lat) {
			this.lat = lat;
		}

		public float getLng() {
			return lng;
		}

		public void setLng(float lng) {
			this.lng = lng;
		}

		public int getNightlyPrice() {
			return nightlyPrice;
		}

		public void setNightlyPrice(int nightlyPrice) {
			this.nightlyPrice = nightlyPrice;
		}
	}
	
	public class AirbnbSearchCriteria {
		private int searchId;
		private float lat;
		private float lng;
		private Date checkin; //a date in the format YYYY-MM-DD
		private Date checkout; //a date in the format YYYY-MM-DD
		
		public AirbnbSearchCriteria(String searchId, String lat, String lng, String checkin, String checkout) {
			try {
				this.searchId = Integer.parseInt(searchId);
				this.lat = Float.parseFloat(lat);
				this.lng = Float.parseFloat(lng);
				this.checkin = dateFormat.parse(checkin);
				this.checkout = dateFormat.parse(checkout);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		public int getSearchId() {
			return searchId;
		}

		public void setSearchId(int searchId) {
			this.searchId = searchId;
		}

		public float getLat() {
			return lat;
		}

		public void setLat(float lat) {
			this.lat = lat;
		}

		public float getLng() {
			return lng;
		}

		public void setLng(float lng) {
			this.lng = lng;
		}

		public Date getCheckin() {
			return checkin;
		}

		public void setCheckin(Date checkin) {
			this.checkin = checkin;
		}

		public Date getCheckout() {
			return checkout;
		}

		public void setCheckout(Date checkout) {
			this.checkout = checkout;
		}
	}
	
	public class AirbnbSearchOutput {
		private int searchId;
		private int rank; 	//integer (starting with 1, max of 10)
		private int propertyId;
		private int totalPrice;		//the total price for the stay in dollars
		
		public AirbnbSearchOutput(int searchId, int propertyId, int totalPrice) {
			this.searchId = searchId;
			this.propertyId = propertyId;
			this.totalPrice = totalPrice;
		}
		
		public String toString() {
			return searchId + "," + rank + "," + propertyId + "," + totalPrice;
		}

		public int getSearchId() {
			return searchId;
		}

		public void setSearchId(int searchId) {
			this.searchId = searchId;
		}

		public int getRank() {
			return rank;
		}

		public void setRank(int rank) {
			this.rank = rank;
		}

		public int getPropertyId() {
			return propertyId;
		}

		public void setPropertyId(int propertyId) {
			this.propertyId = propertyId;
		}

		public int getTotalPrice() {
			return totalPrice;
		}

		public void setTotalPrice(int totalPrice) {
			this.totalPrice = totalPrice;
		}
	}

}
