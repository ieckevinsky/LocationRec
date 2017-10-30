package com.hylanda.loc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;

import com.hylanda.tool.Poi;
/*
 * 计算文章最相关地名
 * */
public class LocHotRate {
 
	private static LocHotRate instance = new LocHotRate();
	public static LocHotRate getInstance() {
		return instance;
	}
	 
	public  Poi sortHotRate(List<Poi> result) {
		Map<String, Integer> level1 = new HashMap<String, Integer>();
		Map<String, Integer> level2 = new HashMap<String, Integer>();
		Map<String, Integer> level3 = new HashMap<String, Integer>();
		Map<String, Integer> level4 = new HashMap<String, Integer>();
		Map<String, Integer> level5 = new HashMap<String, Integer>();

		for (Poi poi : result) {

			String[] temp = poi.getMapString().split("-");

			if (temp.length > 0) {
				String key = temp[0];
				if (level1.containsKey(temp[0])) {
					level1.put(key, level1.get(key) + 1);
				} else {
					level1.put(key, 1);
				}
			}

			if (temp.length > 1) {
				String key = temp[0] + "-" + temp[1];
				if (level2.containsKey(key)) {
					level2.put(key, level2.get(key) + 1);
				} else {
					level2.put(key, 1);
				}
			}

			if (temp.length > 2) {
				String key = temp[0] + "-" + temp[1] + "-" + temp[2];
				if (level3.containsKey(key)) {
					level3.put(key, level3.get(key) + 1);
				} else {
					level3.put(key, 1);
				}
			}
			if (temp.length > 3) {
				String key = temp[0] + "-" + temp[1] + "-" + temp[2] + "-" + temp[3];
				if (level4.containsKey(key)) {
					level4.put(key, level4.get(key) + 1);
				} else {
					level4.put(key, 1);
				}
			}

			if (temp.length > 4) {
				String key = temp[0] + "-" + temp[1] + "-" + temp[2] + "-" + temp[3] + "" + temp[4];
				if (level5.containsKey(key)) {
					level5.put(key, level5.get(key) + 1);
				} else {
					level5.put(key, 1);
				}
			}

		}

		String pre = pickupMaxValue(level1, "");
		String key = pickupMaxValue(level2, pre);
		if (!key.equals("")) {
			pre = key;
		}
		key = pickupMaxValue(level3, pre);
		if (!key.equals("")) {
			pre = key;
		}
		key = pickupMaxValue(level4, pre);
		if (!key.equals("")) {
			pre = key;
		}
		key = pickupMaxValue(level5, pre);
		if (!key.equals("")) {
			pre = key;
		}
		// System.out.println(pre);
		String locname = "";
		Poi respoi = new Poi();
		for (Poi poi : result) {
			if (poi.getMapString().equals(pre)) {
				if (locname.length() < poi.getName().length()) {
					locname = poi.getName();
					respoi = poi;
				}
			}
		}
		System.out.println(respoi.getName() + "\t" + respoi.getStartPos() + "\t" + respoi.getMapLevel() + "\t"
				+ respoi.getAreaID() + "\t" + respoi.getMapString() + "\t"
				+ respoi.getTypeMap().get("coordinates_baidu") + "\t" + respoi.getTypeMap().get("coordinates_gaode"));

		return respoi;
	}

	private  String pickupMaxValue(Map<String, Integer> map, String pre) {
		String key = "";
		int value = 0;
		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			if (entry.getKey().startsWith(pre)) {
				if (entry.getValue() > value) {
					key = entry.getKey();
					value = entry.getValue();
				}
			}

		}

		return key;
	}
}
