package rpc;

import java.io.BufferedReader;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RpcHelper {
	public static void writeJSONObject(HttpServletResponse response , JSONObject obj) {
		try {
			// 1. 修改content type
			response.setContentType("application/json");
			// 2. 加入header response.addHeader
			response.addHeader("Access-Control-Allow-Origin", "*");
			// 3. 获取输出流 PrintWriter
			PrintWriter out = response.getWriter();
			// 4. 写入
			out.print(obj);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void writeJSONArray(HttpServletResponse response , JSONArray array) {
		try {
			// 1. 修改content type
			response.setContentType("application/json");
			// 2. 加入header
			response.addHeader("Access-Control-Allow-Origin", "*");
			// 3. 获取输出流
			PrintWriter out = response.getWriter();
			// 4. 写入
			out.print(array);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Read the http request and parse it as a JSONObject.
	 * @param request
	 * @return
	 */
	public static JSONObject readJsonObject(HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader reader = request.getReader();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			reader.close();
			return new JSONObject(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
