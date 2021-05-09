package rpc;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

/**
 * Servlet implementation class SearchItem
 */
@WebServlet("/search")
public class SearchItem extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchItem() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * get the info from request, query, and response
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String userId = request.getParameter("user_id");
		double lat = Double.parseDouble(request.getParameter("lat"));
		double lon = Double.parseDouble(request.getParameter("lon"));
		
		String keyword = request.getParameter("term");
		
		// Connect db first
		DBConnection conn = DBConnectionFactory.getConnection();
		List<Item> items = conn.searchItems(lat, lon, keyword);
		
		Set<String> favorite = conn.getFavoriteItemIds(userId);
		conn.close();
		
		JSONArray array = new JSONArray();
		try {
			for (Item item : items) {
				JSONObject obj = item.toJSONObject();
				
				// check if current item is a favorite item
				// this field is required by front end to control the fav status
				obj.put("favorite", favorite.contains(item.getItemId()));
				array.put(obj);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		RpcHelper.writeJSONArray(response, array);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
