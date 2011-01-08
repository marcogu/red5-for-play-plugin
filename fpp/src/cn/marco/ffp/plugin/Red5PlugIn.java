package cn.marco.ffp.plugin;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class Red5PlugIn {
	
	public static final Map<String, List<Class>> remotingDestination = new Hashtable<String, List<Class>>();
	
	private static final long START_TIME = System.currentTimeMillis();
	
	public static long getUpTime() {
	    return System.currentTimeMillis() - START_TIME;
	}
	

	public static void rtmpServerStart(){
		
	}

}
