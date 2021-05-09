package db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import db.DBConnection;
import entity.Item;
import entity.Item.ItemBuilder;
import external.TicketMasterAPI;

public class MySQLConnection implements DBConnection{
	
	/**
	 * Connection
	 */
	private Connection conn;
	
	/**
	 * Constructor
	 */
	public MySQLConnection() {
		// Create connection
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(MySQLDBUtil.URL);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	

	/**
	 * close the connection
	 */
	@Override
	public void close() {
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Operate on History table.
	 * insert favorite information.
	 */
	@Override
	public void setFavoriteItems(String userId, List<String> itemIds) {
		if (conn == null) {
			return;
		}
		try {
			String sql = "INSERT IGNORE INTO history (user_id, item_id)VALUES(?, ?)";
			PreparedStatement stmt = conn.prepareStatement(sql);
			for (String itemId: itemIds) {
				stmt.setString(1, userId);
				stmt.setString(2, itemId);
				stmt.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Delete favorite information
	 */
	@Override
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		if (conn == null) {
			return;
		}
		try {
			String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			for (String itemId: itemIds) {
				stmt.setString(1, userId);
				stmt.setString(2, itemId);
				stmt.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}

	/**
	 * Given userId, query history table, get corresponding item_id.
	 */
	@Override
	public Set<String> getFavoriteItemIds(String userId) {
		if (conn == null) return new HashSet<String>();
		Set<String> favoriteItemIds = new HashSet<String>();
		try {
			String sql = "SELECT item_id FROM history WHERE user_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, userId);
			ResultSet res = stmt.executeQuery();
			while (res.next()) {
				favoriteItemIds.add(res.getString("item_id"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return favoriteItemIds;
	}

	/**
	 * Get the favorite items given userId in the history table
	 * userId -> itemId -> favorite items
	 */
	@Override
	public Set<Item> getFavoriteItems(String userId) {
		if (conn == null) return new HashSet<Item>();
		Set<Item> favoriteItems = new HashSet<Item>();
		Set<String> itemIds = getFavoriteItemIds(userId);
		
		try {
			String sql = "SELECT * FROM items WHERE item_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			for (String itemId: itemIds) {
				stmt.setString(1, itemId);
				// executeQuery returns a table(ResultSet)
				ResultSet res = stmt.executeQuery();
				ItemBuilder builder = new ItemBuilder();
				// iterator
				while (res.next()) {
					builder.setItemId(res.getString("item_id"));
					builder.setName(res.getString("name"));
					builder.setAddress(res.getString("address"));
					builder.setImageUrl(res.getString("image_url"));
					builder.setUrl(res.getString("url"));
					builder.setCategories(getCategories(itemId));
					builder.setDistance(res.getDouble("distance"));
					builder.setRating(res.getDouble("rating"));
					
					
					favoriteItems.add(builder.build());
				}
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return favoriteItems;
	}

	/**
	 * Given itemId, query categories table, get corresponding categories
	 */
	@Override
	public Set<String> getCategories(String itemId) {
		if (conn == null) return new HashSet<>();
		Set<String> categories = new HashSet<String>();
		try {
			String sql = "SELECT category FROM categories WHERE item_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, itemId);
			ResultSet res = stmt.executeQuery();
			while (res.next()) {
				categories.add(res.getString("category"));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return categories;
	}

	/**
	 * Search Item. Same as TMAPI search item.
	 */
	@Override
	public List<Item> searchItems(double lat, double lon, String term) {
		// Get the items from TM
		TicketMasterAPI tmAPI = new TicketMasterAPI();
		List<Item> items = tmAPI.search(lat, lon, term);
		// Save the items to the db
		for (Item item: items) {
			saveItem(item);
		}
		return items;
	}

	/**
	 * Save single item into db
	 */
	@Override
	public void saveItem(Item item) {
		if (conn == null) {
			return;
		}
		
		
		try {
			String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement stmt = conn.prepareStatement(sql);
			// Index starts from 1
			stmt.setString(1, item.getItemId());
			stmt.setString(2, item.getName());
			stmt.setDouble(3, item.getRating());
			stmt.setString(4, item.getAddress());
			stmt.setString(5, item.getImageURL());
			stmt.setString(6, item.getUrl());
			stmt.setDouble(7,  item.getDistance());
			stmt.execute();
			
			// set categories
			sql = "INSERT IGNORE INTO categories VALUES(?, ?)";
			stmt = conn.prepareStatement(sql);
			for (String category: item.getCategories()) {
				stmt.setString(1, item.getItemId());
				stmt.setString(2, category);
				stmt.execute();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Get the first name and last name and concat them to full name, given the userId
	 */
	@Override
	public String getFullname(String userId) {
		if (conn == null) return null;
		String name = "";
		try {
			String sql = "SELECT first_name, last_name FROM users WHERE user_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, userId);
			ResultSet res = stmt.executeQuery();
			if (res.next()) name = String.join(" ", res.getString("first_name"), res.getString("last_name"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return name;
	}

	/**
	 * Verify the userId and password
	 */
	@Override
	public boolean verifyLogin(String userId, String password) {
		if (conn == null) return false;
		try {
			String sql = "SELECT user_id FROM users WHERE user_id = ? AND password = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, userId);
			stmt.setString(2, password);
			ResultSet res = stmt.executeQuery();
			if (res.next()) return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
