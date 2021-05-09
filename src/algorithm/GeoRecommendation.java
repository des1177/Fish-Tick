package algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

public class GeoRecommendation {
	public List<Item> recommendItems(String userId, double lat, double lon) {
		List<Item> recommendedItems = new ArrayList<Item>();
		DBConnection conn = DBConnectionFactory.getConnection();
		// 1. Get all favorite itemIds
		Set<String> favoriteItemIds = conn.getFavoriteItemIds(userId);
		
		// 2. Get all categories by favorite items, sort by count
		// Use a map to store category and its counts
		Map<String, Integer> allCategories = new HashMap<>();
		for (String itemId: favoriteItemIds) {
			Set<String> categories = conn.getCategories(itemId);
			for (String category: categories) {
				allCategories.put(category, allCategories.getOrDefault(category, 0) + 1);
			}
		}
		
		// Sort the category
		// avoid integer overflow
		List<Entry<String, Integer>> categoryList = new ArrayList<>(allCategories.entrySet());
		
		// more occurrences first
		Collections.sort(categoryList, new Comparator<Entry<String, Integer>>(
				) {

					@Override
					public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
						return Integer.compare(o2.getValue(), o1.getValue());
					}
		});
		
		// 3. do search based on category, filter out favorite events, sort by distance
		
		Set<Item> visitedItems = new HashSet<Item>();
		for (Entry<String ,Integer> category: categoryList) {
			List<Item> items = conn.searchItems(lat, lon, category.getKey());
			List<Item> filteredItems = new ArrayList<Item>();
			for (Item item: items) {
				// if not visited again and not already in the favorite list, add to recommendation list
				if (!favoriteItemIds.contains(item.getItemId()) && !visitedItems.contains(item)) {
					filteredItems.add(item);
				}
			}
			
			// sort the items in filtered items. less distance first
			Collections.sort(filteredItems, new Comparator<Item>() {

				@Override
				public int compare(Item o1, Item o2) {
					return Double.compare(o1.getDistance(), o2.getDistance());
				}
			});
			
			visitedItems.addAll(items);
			recommendedItems.addAll(filteredItems);
		}
		
		return recommendedItems;
	}
}
