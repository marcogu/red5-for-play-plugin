package cn.marco.ffp.utils;

import java.util.ArrayList;
import java.util.List;

public class PluginUtil {
	
	public static List<Object> getListFromArray(Object[] obj){
		if(obj == null )
			return null;
		List<Object> aryList = new ArrayList<Object>(obj.length);
		for(int i=0;i<obj.length; i++){
			aryList.add(obj[i]);
		}
		return aryList;
	}

}
