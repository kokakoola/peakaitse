package ee.netgroup.mainfuse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JSonSerializer {

	private Gson ser = new GsonBuilder().serializeNulls().create();

	public String toJson(Object obj) {
		return ser.toJson(obj);
	}

	public Object fromJSon(String zz) throws JSONException {
		zz = zz.trim();
		Object o;
		if (zz.length() == 0)
			o = new JSONObject(zz);
		else switch(zz.charAt(0)) {
		case '[': o = new JSONArray(zz); break;
		default: o = new JSONObject(zz); break;
		}
		return fromJSonObj(o);
	}

	private Object fromJSonObj(Object jo) throws JSONException {
		if (jo == null) return null;
		else if (jo instanceof String)
			return jo;
		else if (jo instanceof JSONArray) {
			JSONArray arr = (JSONArray) jo;
			int len = arr.length();
			ArrayList<Object> ret = new ArrayList<Object>(len);
			for(int i = 0; i < len; i++)
				ret.add(fromJSonObj(arr.get(i)));
			return ret;
		}
		else if (jo instanceof JSONObject) {
			JSONObject map = (JSONObject) jo;
			HashMap<String, Object> ret = new HashMap<>();
			Iterator<String> keys = map.keys();
			while(keys.hasNext()) {
				String key = keys.next();
				if(map.isNull(key))
					ret.put(key, null);
				else
					ret.put(key, fromJSonObj(map.get(key)));
			}
			return ret;
		}
		else if (jo instanceof Integer)
			return new Long((long)((Integer)jo).intValue());
		else if (jo instanceof Long || jo instanceof Double || jo instanceof Boolean)
			return jo;
		else return jo.toString();
	}
}
