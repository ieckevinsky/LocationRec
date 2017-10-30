package com.hylanda.loc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hylanda.loc.MultiLevelNormalize.NormalResult;
import com.hylanda.lochighcall.HighCallLoc;
import com.hylanda.segment.HLSwknl;
import com.hylanda.segment.SegmentResult;
import com.hylanda.segment.SegmentWord;
import com.hylanda.sevenfeature.CreateTrainData;
import com.hylanda.sevenfeature.CreateTrainData.Features;
import com.hylanda.tool.Locinfo;
import com.hylanda.tool.Poi;
import com.sun.org.apache.bcel.internal.generic.DASTORE;
import com.sun.xml.internal.bind.v2.schemagen.xmlschema.Occurs;

import sun.net.www.content.audio.wav;

public class CombineLocRec {
	private static  Logger logger = LoggerFactory.getLogger(CombineLocRec.class);
	public EventOccurSen eventOccurSen = new EventOccurSen() ;
	public  MultiLevelNormalize normalize = MultiLevelNormalize.getInstance();
	public  CreateTrainData createTrainData = new CreateTrainData();
	public  HighCallLoc highCallLoc = new HighCallLoc();
	//public Tagger tagger ;
	private static CombineLocRec instance = new CombineLocRec();

	public static CombineLocRec getInstance() {
		return instance;
	}

	public  void init(Map<String, String> params) throws IOException {

		// event init 
		eventOccurSen.init(params.get("event"));
		
		// dictbase init
		normalize.init(params.get("regin"),params.get("hotpoint") ,params.get("foreign") ,params.get("place") ,params.get("black") );
		
		// crf init

		
		// highcall init
		highCallLoc.init_normalName(params.get("suffix"));

		logger.info("Done ...");

	}

	public  List<Locinfo> rec_loc_nohandle(String Text ) {
		// 识别
		List<NormalResult> NormalResultlist = new ArrayList<NormalResult>();
		NormalResultlist = normalize.normalize(Text, 2, false);
		List<Poi> list_dict = new ArrayList<Poi>();
		list_dict = normalize.standardizedResult(NormalResultlist);

		// hrecall
		
		List<Poi> list_crf = new ArrayList<Poi>();
		List<Poi> list_highcall = new ArrayList<Poi>();
	
		Map<Integer, Locinfo> ressult =  new HashMap<Integer ,Locinfo>();
		ressult = combineResult( list_dict, list_highcall , list_crf);
		ressult = sortByComparatorkey(ressult);
		List<Locinfo> result_end =  new ArrayList<Locinfo>();
		for(Map.Entry<Integer, Locinfo> entry : ressult.entrySet()){
			if (entry.getValue().poi.getMapString().equals("中华人民共和国")) {
				entry.getValue().poi.setMapString("中国");
				result_end.add(entry.getValue());
			}else{
				result_end.add(entry.getValue());
			}			

		}
		return  result_end ;
	}
	
	public Poi getbaseking(String baseText){
		List<NormalResult>  NormalBaseResultlist = new ArrayList<NormalResult>();
		NormalBaseResultlist = normalize.normalize(baseText,2, false);
	
		List<Poi> baseresult = new ArrayList<Poi>();
		baseresult = normalize.standardizedResult(NormalBaseResultlist );

		Poi baseking = normalize.baseLine(baseresult); 
		if (baseking.getMapString() == null) {
			logger.info("baseking is null ");
		}else {
			
			logger.info(baseking.getName()+"\t"+baseking.getStartPos()+"\t"+baseking.getMapLevel()+"\t"+baseking.getAreaID()+"\t"+baseking.getMapString()+"\t"+baseking.getTypeMap().get("coordinates_baidu")+"\t"+baseking.getTypeMap().get("coordinates_gaode")+"\t"+baseking.getType());
		}
		return baseking ;
	}

	public  List<Locinfo> rec_loc_seg(String Text ,long handle ,HLSwknl swknl, Poi baseking) {
		// 识别
//		List<NormalResult> NormalResultlist = new ArrayList<NormalResult>();
//		NormalResultlist = normalize.normalize(Text, 2, false);
//		List<Poi> list_dict = new ArrayList<Poi>();
//		list_dict = normalize.standardizedResult(NormalResultlist);

		List<Poi> list_dict = new ArrayList<Poi>();
		if (baseking.getMapString() == null) {		
			List<NormalResult> NormalResultlist = new ArrayList<NormalResult>();
			NormalResultlist = normalize.normalize(Text, 2, false);			
			list_dict = normalize.standardizedResult(NormalResultlist);			
		}else{			
			List<NormalResult>  NormalResultlist = new ArrayList<NormalResult>();
			NormalResultlist = normalize.normalize(Text,2, false);	
			list_dict = normalize.standardizedResult(NormalResultlist , baseking );	

		}

		// hrecall
		
		List<Poi> list_crf = new ArrayList<Poi>();
		List<Poi> list_highcall = new ArrayList<Poi>();
		
		Map<Integer, List<String> > text_map = eventOccurSen.spiltText(swknl,handle);
		for(Map.Entry<Integer, List<String> > entrytext : text_map.entrySet()){

		 
		list_highcall = highCallLoc.search_loc(entrytext.getValue());
		
		
		for(Poi poi : list_highcall){
			int startPos = entrytext.getKey() + poi.getStartPos();
			poi.setStartPos(startPos);
		}
		}
	
		Map<Integer, Locinfo> ressult =  new HashMap<Integer ,Locinfo>();
		ressult = combineResult( list_dict, list_highcall , list_crf);
		ressult = sortByComparatorkey(ressult);
		List<Locinfo> result_end =  new ArrayList<Locinfo>();
		for(Map.Entry<Integer, Locinfo> entry : ressult.entrySet()){
			if (entry.getValue().poi.getMapString().equals("中华人民共和国")) {
				entry.getValue().poi.setMapString("中国");
				result_end.add(entry.getValue());
			}else{
				result_end.add(entry.getValue());
			}			

		}
		return  result_end ;
	}
	
	private  Map sortByComparatorkey(Map unsortMap) {

		List list = new LinkedList(unsortMap.entrySet());

		// sort list based on comparator
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o1)).getKey()).compareTo(((Map.Entry) (o2)).getKey());
			}
		});

		// put sorted list into map again
		Map sortedMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
	
	
	private  Map sortByComparator(Map unsortMap) {

		List list = new LinkedList(unsortMap.entrySet());

		// sort list based on comparator
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
			}
		});

		// put sorted list into map again
		Map sortedMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

	
	
	public  Map<Integer, Locinfo>  combineResult(List<Poi> list_dictbased_loc, List<Poi> list_highcall_loc,
			List<Poi> list_crf_loc) {
		Set<Integer> set = new HashSet<Integer>();
		
		Map<Integer, Locinfo> result = new HashMap<Integer, Locinfo>();
		for (Poi poi : list_dictbased_loc) {
			Locinfo locinfo = new Locinfo();
			locinfo.poi = poi;
			locinfo.identify_type = "dict";
			result.put(poi.getStartPos(), locinfo);
			for (int i = poi.getStartPos(); i < poi.getStartPos() + poi.getName().length(); i++) {
				set.add(i);
			} 
		}
		for (Poi poi : list_highcall_loc) {

			if (!result.containsKey(poi.getStartPos())) {

				boolean flag = false;
				for (int i = poi.getStartPos(); i < poi.getStartPos() + poi.getName().length(); i++) {
					if (set.contains(i)) {
						flag = true;
						break;
					}

				}

				if (!flag) {
					Locinfo locinfo = new Locinfo();
					findNeighbor(list_dictbased_loc ,  poi );
					locinfo.poi = poi;					
					locinfo.identify_type = "hrecall";
					result.put(poi.getStartPos(), locinfo);
					for (int i = poi.getStartPos(); i < poi.getStartPos() + poi.getName().length(); i++) {
						set.add(i);
					} 
				}

			}

		}
		return result ;
	}

	public  void findNeighbor(List<Poi> list_dictbased_loc , Poi poi ){
		List<Integer> startpos_list = new ArrayList<Integer>();
		boolean flag = false ;
		if (list_dictbased_loc.isEmpty()) {
			poi.setAreaID("000000");
			poi.setMapLevel(-1);
			poi.setMapString("中国-"+poi.getName());
			Map<String, String> typeMap = new HashMap<String, String>();
			typeMap.put("coordinates_baidu","");
			typeMap.put("coordinates_gaode","");
			poi.setTypeMap(typeMap);
		}else{
			for (Poi poi_dict : list_dictbased_loc) {
				if (poi.getStartPos()-poi_dict.getStartPos() > 0) {
					poi.setAreaID(poi_dict.getAreaID());
					poi.setMapLevel(-1);
					if (poi_dict.getMapLevel() == -1) {
						String[] temp = poi_dict.getMapString().split("-");
						if (temp.length == 1) {
							poi.setMapString(poi.getName());
						}else{
							StringBuffer mapstring = new StringBuffer() ;
							for(int i = 0 ; i < temp.length-1 ; i++){
							 
								mapstring.append(temp[i]+"-");
							}
							poi.setMapString(mapstring.toString()+poi.getName());
							
						}
						
					}else{
						poi.setMapString(poi_dict.getMapString()+"-"+poi.getName());
					}
						
					Map<String, String> typeMap = new HashMap<String, String>();
					typeMap.put("coordinates_baidu","");
					typeMap.put("coordinates_gaode","");
					poi.setTypeMap(typeMap);
					flag = true ;
				}else{
					if (!flag) {
						poi.setAreaID(poi_dict.getAreaID());
						poi.setMapLevel(-1);
						if (poi_dict.getMapLevel() == -1) {
							String[] temp = poi_dict.getMapString().split("-");
							if (temp.length == 1) {
								poi.setMapString(poi.getName());
							}else{
								StringBuffer mapstring = new StringBuffer() ;
								for(int i = 0 ; i < temp.length-1 ; i++){
									 
									mapstring.append(temp[i]+"-");
								}
								poi.setMapString(mapstring.toString()+poi.getName());
								
							}
							
						}else{
							poi.setMapString(poi_dict.getMapString()+"-"+poi.getName());
						}
						Map<String, String> typeMap = new HashMap<String, String>();
						typeMap.put("coordinates_baidu","");
						typeMap.put("coordinates_gaode","");
						poi.setTypeMap(typeMap);
						break ;
					}
					
				}
				 
			}
		}

		
	}
	
}
