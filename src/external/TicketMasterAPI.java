package external;
 
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;


public class TicketMasterAPI {
	private static final String URL = "https://app.ticketmaster.com/discovery/v2/events.json";
	private static final String DEFAULT_KEYWORD = "";
	private static final String API_KEY = "EASr4nf6HHQTsvvdJg0XsbUJX6GsPxUI";
	
	/**
	 * pass lon and lat to ticket master API, ticket master API return a string of all ticket info, 
	 * then we make the string a JSON array
	 */
	public List<Item> search(double lat, double lon, String keyword) { 
		List<Item> ret = new ArrayList<Item>();
		if (keyword == null) keyword = DEFAULT_KEYWORD;
		// translate keyword into URL-supported format
		try {
			keyword = java.net.URLEncoder.encode(keyword, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Get geoPoint
		String geoHash = GeoHash.encodeGeohash(lat, lon, 8);
		// Create query
		// 50 is default search radius
		String query = String.format("apikey=%s&geoPoint=%s&keyword=%s&radius=%s", API_KEY, geoHash, keyword, 50);
		// Create URL
		try {
			// create a HTTP URL connection
			HttpURLConnection connection = (HttpURLConnection) new URL(URL + "?" + query).openConnection();
			// get the response code EG. 200/success, 404/fail
			int responseCode = connection.getResponseCode();
			// print res
			System.out.println("\nSending \"GET\" request to URL : " + URL + "?" + query);
			System.out.println("\nResponse Code: " + responseCode);
			// check responseCode (Implement it later)
			if (responseCode != 200) {
				
			}
			// read and write the response content from ticketMaster API
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream())); // ticketMaster API will return info little by little
			String inputLine;
			StringBuilder response = new StringBuilder(); // ticket info that returned from ticketMaster's API, not a JSON file, it is a string
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			connection.disconnect();
			// write the response to a JSON object
			JSONObject obj = new JSONObject(response.toString());
			// check the result
			if (obj.isNull("_embedded")) {
				return ret;
			}
			// get the events from the whole JSON and return the events field of it as a JSON Array.
			JSONObject embedded = obj.getJSONObject("_embedded");
			JSONArray events = embedded.getJSONArray("events");
			ret = getItemList(events);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret; // each item in ret is a ticket info, ret is a list of tickets
	}

	
	
	/**
	 * Helper methods
	 */

	//  {
	//    "name": "example",
              //    "id": "12345",
              //    "url": "www.example.com",
	//    ...
	//    "_embedded": {
	//	    "venues": [
	//	        {
	//		        "address": {
	//		           "line1": "101 First St,",
	//		           "line2": "Suite 101",
	//		           "line3": "...",
	//		        },
	//		        "city": {
	//		        	"name": "San Francisco"
	//		        }
	//		        ...
	//	        },
	//	        ...
	//	    ]
	//    }
	//    ...
	//  }
	
	
	
	
	/**
	 * get the Address from the JSONObject
	 * @param event
	 * @return A String of address
	 * @throws JSONException
	 */
	private String getAddress(JSONObject event) throws JSONException {
		if (!event.isNull("_embedded")) {
			JSONObject embedded = event.getJSONObject("_embedded");
			
			if (!embedded.isNull("venues")) {
				JSONArray venues = embedded.getJSONArray("venues");
				
				for (int i = 0; i < venues.length(); ++i) {
					JSONObject venue = venues.getJSONObject(i);
					
					StringBuilder sb = new StringBuilder();
					
					if (!venue.isNull("address")) {
						JSONObject address = venue.getJSONObject("address");
						
						if (!address.isNull("line1")) {
							sb.append(address.getString("line1"));
						}
						if (!address.isNull("line2")) {
							sb.append(" ");
							sb.append(address.getString("line2"));
						}
						if (!address.isNull("line3")) {
							sb.append(" ");
							sb.append(address.getString("line3"));
						}
					}
					
					if (!venue.isNull("city")) {
						JSONObject city = venue.getJSONObject("city");
						
						if (!city.isNull("name")) {
							sb.append(" ");
							sb.append(city.getString("name"));
						}
					}
					
					if (!sb.toString().equals("")) {
						return sb.toString();
					}
				}
			}
		}

		return "";
	}


	// {"images": [{"url": "www.example.com/my_image.jpg"}, ...]}
	/**
	 * Get the image URL from a JSONOBject
	 * @param event
	 * @return A String of image URL
	 * @throws JSONException
	 */
	private String getImageUrl(JSONObject event) throws JSONException {
		if (!event.isNull("images")) {
			JSONArray images = event.getJSONArray("images");
			
			for (int i = 0; i < images.length(); ++i) {
				JSONObject image = images.getJSONObject(i);
				
				if (!image.isNull("url")) {
					return image.getString("url");
				}
			}
		}

		return "";
	}

	// {"classifications" : [{"segment": {"name": "music"}}, ...]}
	/**
	 * Get a set of categories from a JSONObject 
	 * @param event
	 * @return A set of string
	 * @throws JSONException
	 */
	private Set<String> getCategories(JSONObject event) throws JSONException {
		Set<String> categories = new HashSet<>();
		if (!event.isNull("classifications")) {
			JSONArray classifications = event.getJSONArray("classifications");
			for (int i = 0; i < classifications.length(); i++) {
				JSONObject classification = classifications.getJSONObject(i);
				if (!classification.isNull("segment")) {
					JSONObject segment = classification.getJSONObject("segment");
					if (!segment.isNull("name")) {
						String name = segment.getString("name");
						categories.add(name);
					}
				}
			}
		}

		return categories;
	}

	// Convert JSONArray to a list of item objects.
	/**
	 * Convert JSONArray to a list of item objects
	 * @param events
	 * @return A list of Item objects
	 * @throws JSONException
	 */
	private List<Item> getItemList(JSONArray events) throws JSONException {
		List<Item> itemList = new ArrayList<>();

		for (int i = 0; i < events.length(); ++i) {
			JSONObject event = events.getJSONObject(i);
			
			ItemBuilder builder = new ItemBuilder();
			
			if (!event.isNull("name")) {
				builder.setName(event.getString("name"));
			}
			
			if (!event.isNull("id")) {
				builder.setItemId(event.getString("id"));
			}
			
			if (!event.isNull("url")) {
				builder.setUrl(event.getString("url"));
			}
			
			if (!event.isNull("rating")) {
				builder.setRating(event.getDouble("rating"));
			}
			
			if (!event.isNull("distance")) {
				builder.setDistance(event.getDouble("distance"));
			}
			
			builder.setCategories(getCategories(event));
			builder.setAddress(getAddress(event));
			builder.setImageUrl(getImageUrl(event));
			
			itemList.add(builder.build());
		}

		return itemList;
	}

	
	/**
	 * print the results of search(latitude, longitude)
	 * @param lat latitude
	 * @param lon longitude
	 */
	private void queryAPI(double lat, double lon) {
		List<Item> events = search(lat, lon, null);
		try {
			for (Item event: events) {
				System.out.println(event.toJSONObject());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		TicketMasterAPI tmAPI = new TicketMasterAPI();
		tmAPI.queryAPI(29.682684, -95.295410);
	}
}

