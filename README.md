You will be provided with availability and pricing data for a set of rental properties.  Your program will determine the cheapest properties for a given date range in a specific geographic area.<br>

You will read input from STDIN and print output to STDOUT.<br>

Input:<br>

The input is in CSV format with three sections of data.  "Properties", "Dates", and "Searches".  Each section will begin with a single line labeling the section followed by a number of lines with that section's data.<br>

Here is a description of each section and the data columns in contains.<br>

"Properties" - identifies possible properties for rent<br>

  •	property_id - an integer<br>
	•	lat - a float<br>
	•	lng - a float<br>
	•	nightly_price - the standard nightly price; an integer in dollars (no cents)<br>
  
"Dates" - specifies dates when properties are unavailable, or when the nightly pricing differs from the standard nightly price.  If not 
specified here, the property is available at the standard nightly price.<br>

	•	property_id - an integer
	•	date - a date in the format YYYY-MM-DD
	•	availability - 0 if the property is unavailable, 1 if the property is available
	•	price - price for the night; an integer in dollars (no cents)
  
"Searches" - a search to be performed<br>

	•	search_id - an integer
	•	lat - a float
	•	lng - a float<br>
	•	checkin - a date in the format YYYY-MM-DD
	•	checkout - a date in the format YYYY-MM-DD
  
Sample Input:<br>

Properties<br>
1,0.29322858181353,-69.8325156543615,500<br>
2,15.8229075627722,102.378248903003,280<br>
Dates<br>
1,2012-08-15,0,500<br>
1,2012-08-26,1,700<br>
Searches<br>
1,0.9802496106877,-70.146252228624,2012-08-14,2012-08-21<br>
2,0.9802496106877,-70.146252228624,2012-08-26,2012-08-28<br>

Output:<br>

Your program should output the properties that match each search, up to a maximum of 10 properties per search.  Some searches may return no results.<br>

The results should be ordered by cheapest total price for the stay, also matching the availability dates and geographic filter.  (If two properties have the same total price, sort by property_id ascending).  For the geographic filter, use a bounding box that is 2 degrees square in total (ie, +/- 1.0 degrees from each coordinate).  If a property is unavailable for any date during the range, it is not a valid result.  If a property has a variable price in the specified date range, that variable price overrides the base nightly price for that night.  The total price is the sum of the nightly prices for the entire stay.<br>

Note that properties do not need to be available on the checkout date itself, just on the day before.<br>

Your program should produce output with the following columns.  Each result for a given search should appear on it's own line.  A search with zero results does not need to be included in the output.<br>

search_id - integer<br>
rank - integer (starting with 1, max of 10)<br>
property_id - integer<br>
total_price - the total price for the stay in dollars<br>

Sample Output:<br>
2,1,1,1200<br>

You shouldn't use any kind of external database or search engine.  Your program should load the input data into memory and then use your own logic and data structures to perform the searches and generate the output.  Using libraries (such as to parse CSV files) is ok.<br>