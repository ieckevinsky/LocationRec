package com.hylanda.tool;

import java.util.Map;

import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;

public class Poi {
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public int getStartPos() {
		return StartPos;
	}
	public void setStartPos(int startPos) {
		StartPos = startPos;
	}
	public int getMapLevel() {
		return MapLevel;
	}
	public void setMapLevel(int mapLevel) {
		MapLevel = mapLevel;
	}
	public String getMapString() {
		return MapString;
	}
	public void setMapString(String mapString) {
		MapString = mapString;
	}
	public Map<String, String> getTypeMap() {
		return TypeMap;
	}
	public void setTypeMap(Map<String, String> typeMap) {
		TypeMap = typeMap;
	}
	public String getAreaID() {
		return AreaID;
	}
	public void setAreaID(String areaID) {
		AreaID = areaID;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public boolean isIscombinepoi() {
		return iscombinepoi;
	}
	public void setIscombinepoi(boolean iscombinepoi) {
		this.iscombinepoi = iscombinepoi;
	}
	public boolean isIsrectifyMapstring() {
		return isrectifyMapstring;
	}
	public void setIsrectifyMapstring(boolean isrectifyMapstring) {
		this.isrectifyMapstring = isrectifyMapstring;
	}
	String Name	 ;          
	int StartPos ;   
	int MapLevel ; 	
	String MapString;
	Map<String, String > TypeMap;
	String AreaID;
	String type ;
	boolean iscombinepoi ;   //是否是合并过的地名
	boolean isrectifyMapstring ; //是否重新修改过mapstring


	 
	
}
