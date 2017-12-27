package wifiairscout.changhong.com.wifiairscout.task;

import java.util.HashMap;



/**
 * 
 * @author lds
 *
 */
public class TaskParams {
	
	private HashMap<String, Object> params = null;
	
	public TaskParams() {
		params = new HashMap<String, Object>();
	}
	
	public TaskParams(String key, Object value) {
		this();
		put(key, value);
	}
	
	public void put(String key, Object value) {
		params.put(key, value);
	}
	
	public Object get(String key) {
		return params.get(key);
	}
	
	
    public boolean has(String key) {
        return this.params.containsKey(key);
    }

    public String getString(String key){
        Object object = get(key);
        return object == null ? null : object.toString();
    }
}
