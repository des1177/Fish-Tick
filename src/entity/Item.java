package entity;

import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Item of event
 * @author andaluo
 *
 */
public class Item {
	private String itemId;
	private String name;
	private double rating;
	private String address;
	private Set<String> categories;
	private String imageURL;
	private String url;
	private double distance;
	
	
	/**
	 * Builder pattern to create a class
	 * @param builder
	 */
	private Item(ItemBuilder builder) {
		this.itemId = builder.itemId;
		this.name = builder.name;
		this.rating = builder.rating;
		this.address = builder.address;
		this.categories = builder.categories;
		this.imageURL = builder.imageURL;
		this.url = builder.url;
		this.distance = builder.distance;	}
	
	/**
	 * Return a JSONObject containing current info
	 * @return
	 */
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("item_id", itemId);
			obj.put("name", name);	
			obj.put("rating", rating);
			obj.put("address", address);
			obj.put("categories", new JSONArray(categories));
			obj.put("image_url", imageURL);
			obj.put("url", url);
			obj.put("distance", distance);
			} catch (Exception e) {
				e.printStackTrace();
		}
		return obj;
	}
	
	/**
	 * A helper class to build the Item class
	 * @author andaluo
	 *
	 */
	public static class ItemBuilder{
		private String itemId;
		private String name;
		private double rating;
		private String address;
		private Set<String> categories;
		private String imageURL;
		private String url;
		private double distance;
		
		/**
		 * Construct the Item using builder
		 * @return
		 */
		public Item build() {
			return new Item(this);
		}
		
		/**
		 * return the builder itself to support continuous operation.
		 * @param itemId
		 * @return
		 */
		public ItemBuilder setItemId(String itemId) {
			this.itemId = itemId;
			return this;
		}
		public ItemBuilder setName(String name) {
			this.name = name;
			return this;
		}
		public ItemBuilder setRating(double rating) {
			this.rating = rating;
			return this;
		}
		public ItemBuilder setAddress(String address) {
			this.address = address;
			return this;
		}
		public ItemBuilder setCategories(Set<String> categories) {
			this.categories = categories;
			return this;
		}
		public ItemBuilder setImageUrl(String imageURL) {
			this.imageURL = imageURL;
			return this;
		}
		public ItemBuilder setUrl(String url) {
			this.url = url;
			return this;
		}
		public ItemBuilder setDistance(double distance) {
			this.distance = distance;
			return this;
		}
		
	}
	public String getItemId() {
		return itemId;
	}
	public String getName() {
		return name;
	}
	public double getRating() {
		return rating;
	}
	public String getAddress() {
		return address;
	}
	public Set<String> getCategories() {
		return categories;
	}
	public String getImageURL() {
		return imageURL;
	}
	public String getUrl() {
		return url;
	}
	public double getDistance() {
		return distance;
	}
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((itemId == null) ? 0 : itemId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Item other = (Item) obj;
		if (itemId == null) {
			if (other.itemId != null)
				return false;
		} else if (!itemId.equals(other.itemId))
			return false;
		return true;
	}

	public static void main(String[] args) {
		Item item = new Item.ItemBuilder().setAddress("abc").setDistance(19).build();
	}
}
