package com.hylanda.lochighcall;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.ws.WebServiceProvider;

import org.omg.CORBA.INTERNAL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hylanda.common.trie.MatchPattern;
import com.hylanda.common.trie.Pattern;
import com.hylanda.loc.MultiLevelNormalize;
import com.hylanda.segment.HLDictManager;
import com.hylanda.segment.HLSwknl;
import com.hylanda.segment.SegmentResult;
import com.hylanda.segment.SegmentWord;
import com.hylanda.tool.Poi;
import com.hylanda.wumanbertrie.WumanberTrie;


public class HighCallLoc {
	public static Set<Pattern<Integer>> vKey = new HashSet<Pattern<Integer>>();
	public static WumanberTrie<Integer> m_WumanTrie = new WumanberTrie<Integer>();
 
	public static List<String> resultlist = new ArrayList<String>();
	private static  Logger logger = LoggerFactory.getLogger(HighCallLoc.class);
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	 
			//init_normalName("D:/读写txt/回溯数据/地名后缀.txt");
			// readCorpus("D:/读写txt/回溯数据/new/outputresult_count_wloc_yes_loc_no.txt");
//		HighCallLoc highCallLoc = new HighCallLoc();
//		highCallLoc.init_normalName("地名后缀.txt");
//			HLSwknl swknl = new HLSwknl();
//			long handle  = swknl.getSegHandle("我爱于家堡", swknl.MODE_MAX,false, 0);
//			highCallLoc.singletest(handle);
			//readCorpus("D:/读写txt/回溯数据/new/compare1.txt");

	}

	public  List<Poi> singletest(long handle){
		HLSwknl swknl = new HLSwknl();
		return search_loc( swknl,handle);
	}
	
	public  Map<Integer, Integer> compute_startpos(SegmentWord[] words ){
		int startpos = 0 ;
		Map<Integer, Integer> teMap = new HashMap<Integer,Integer>();
		for(int i = 0 ; i < words.length ; i++ ){
			int nowpos = startpos ; 
			teMap.put(i,nowpos );
			startpos += words[i].getWord().length();
			
		}
		return teMap;
	}
	public static Map<Integer, Integer> compute_startpos(List<String> words ){
		int startpos = 0 ;
		Map<Integer, Integer> teMap = new HashMap<Integer,Integer>();
		for(int i = 0 ; i < words.size() ; i++ ){
			int nowpos = startpos ; 
			teMap.put(i,nowpos );
			startpos += words.get(i).length();
			
		}
		return teMap;
	}
	
	
	public static void readCorpus(String readpath) throws IOException, IOException{
		HLSwknl swknl = new HLSwknl();
		File file = new File(readpath);
		InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"utf-8");
		BufferedReader read = new BufferedReader(reader);
		String LineTxt = "" ;
		//List<String> newlist = new ArrayList<String>();
		while((LineTxt = read.readLine())!=null){
			 
//			String[] temp = LineTxt.split("\t");
//			StringBuffer tempstring = new StringBuffer();
//			for(int i= 1 ;i < temp.length ; i++ ){
//				tempstring.append(temp[i]);
//			}
//			if (tempstring.toString().contains("村")) {
//				newlist.add(temp[0]+"\t"+tempstring.toString());
//			}
		//	HLSwknl swknl,long handle
		//	search_loc( swknl,LineTxt);
		}
		read.close();
		System.out.println("first finish ... "); 
		//System.out.println(newlist.size());
//	    int[] reult2 =  randomArray(0,newlist.size(),50);  
//
//	    for (int i : reult2) { 	     
//	    	search_loc( swknl,newlist.get(i).split("\t")[1]);
//	     
//	    	
//	    }
	}
	
	public static int[] randomArray(int min,int max,int n){  
	    int len = max-min+1;  
	      
	    if(max < min || n > len){  
	        return null;  
	    }  
	      
	    //初始化给定范围的待选数组  
	    int[] source = new int[len];  
	       for (int i = min; i < min+len; i++){  
	        source[i-min] = i;  
	       }  
	         
	       int[] result = new int[n];  
	       Random rd = new Random();  
	       int index = 0;  
	       for (int i = 0; i < result.length; i++) {  
	        //待选数组0到(len-2)随机一个下标  
	           index = Math.abs(rd.nextInt() % len--);  
	           //将随机到的数放入结果集  
	           result[i] = source[index];  
	           //将待选数组中被随机到的数，用待选数组(len-1)下标对应的数替换  
	           source[index] = source[len];  
	       }  
	       return result;  
	}  
	
	public  List<Poi>  search_loc(HLSwknl swknl,long handle){
		SegmentResult segRes = swknl.segment(handle, HLSwknl.MODE_MAX, HLSwknl.MAKE_FORSEARCH, 0);
		SegmentWord[] words = segRes.getWords();
		Map<Integer, Integer> pos_map = compute_startpos(words);
		List<Poi> list_highcall_loc = new ArrayList<Poi>();
		StringBuffer output = new StringBuffer();
		for(int i = 0 ; i < words.length ; i++){
			SegmentWord word = words[i];
			boolean flag = findMaybe(word.getWord());
			if (flag) {
				if (word.getWord().length()>2) {
					output.append(word.getWord()+",");
					Poi poi = new Poi();
					poi.setName(word.getWord());
					poi.setStartPos(pos_map.get(i));
					
					list_highcall_loc.add(poi);
					
				}else{
					if (i > 0) {
						SegmentWord preix = words[i-1];
						output.append(preix.getWord()+" "+word.getWord()+",");
						Poi poi = new Poi();
						poi.setName(preix.getWord()+word.getWord());
						poi.setStartPos(pos_map.get(i-1));
						
						list_highcall_loc.add(poi);
					}else{
						if (word.getWord().length()>1) {
							output.append(word.getWord()+",");
							Poi poi = new Poi();
							poi.setName(word.getWord());
							poi.setStartPos(pos_map.get(i));
							
							list_highcall_loc.add(poi);
						}
						
					}
				}
				
			}
		}
		
		return list_highcall_loc ;
	}

	public  List<Poi>  search_loc(List<String> words){

		Map<Integer, Integer> pos_map = compute_startpos(words);
		List<Poi> list_highcall_loc = new ArrayList<Poi>();
		//StringBuffer output = new StringBuffer();
		for(int i = 0 ; i < words.size() ; i++){
			String word = words.get(i);
			boolean flag = findMaybe(word);
			if (flag) {
				if (word.length()>2) {
					//output.append(word+",");
					Poi poi = new Poi();
					poi.setName(word);
					poi.setStartPos(pos_map.get(i));
					
					list_highcall_loc.add(poi);
					
				}else{
					if (i > 0) {
						String preix = words.get(i-1);
						if (isChinesePunctuation(preix)) {
							//output.append(word+",");
							Poi poi = new Poi();
							poi.setName(word);
							poi.setStartPos(pos_map.get(i));
							
							list_highcall_loc.add(poi);
						}else {
							//output.append(preix+" "+word+",");
							Poi poi = new Poi();
							poi.setName(preix+word);
							poi.setStartPos(pos_map.get(i-1));							
							list_highcall_loc.add(poi);
						}
						
					}else{
						if (word.length()>1) {
							//output.append(word+",");
							Poi poi = new Poi();
							poi.setName(word);
							poi.setStartPos(pos_map.get(i));
							
							list_highcall_loc.add(poi);
						}
						
					}
				}
				
			}
		}
		
		return list_highcall_loc ;
	}
	
	   public  boolean isChinesePunctuation(String Text) {
		   boolean flag = false ;
		   for(int i = 0 ; i < Text.length() ; i++){
		        Character.UnicodeBlock ub = Character.UnicodeBlock.of(Text.charAt(i));
		        if (ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
		                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
		                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
		                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS
		                || ub == Character.UnicodeBlock.VERTICAL_FORMS
		                //|| ub == Character.UnicodeBlock.
		        		) {
		        	 
		        	
		            return true;
		        } else {
		        	if (isEnPunc(Text.charAt(i))) {
		        		return true;
					}
		            return false;
		        }
		   }
		   return flag ;
	    }					
	   
	   public boolean isEnPunc(char ch){    
	        if (0x21 <= ch && ch <= 0x22) return true;    
	      if (ch == 0x27 || ch == 0x2C) return true;    
	      if (ch == 0x2E || ch == 0x3A) return true;    
	      if (ch == 0x3B || ch == 0x3F) return true;    
	    
	      return false;    
	    }
	//初始化 地名通名词典
	
	public  void init_normalName(String readpath) {

		if (readpath != null) {
			 
			File file = new File(readpath);
			if (file.isFile() && file.exists()  ) {
		 
		InputStreamReader reader;
		try {
			reader = new InputStreamReader(new FileInputStream(file),"utf-8");
			BufferedReader read = new BufferedReader(reader);
			String LineTxt = "";
			while((LineTxt = read.readLine())!=null){
				vKey.add(new Pattern<Integer>(LineTxt, 0));
			}
			read.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("loc normalName init error ");
		}

		m_WumanTrie.build(vKey);
		if (!vKey.isEmpty() && m_WumanTrie != null) {
			logger.info("loc normalName init ok ");
		}else{
			logger.error("loc normalName init error ");
		}
			}else {
				logger.error("locnormalName.txt is not exit ");
			}
		}else {
			logger.error("locnormalNamepath is null ");
		}
		
	}
	
	private static Boolean findMaybe(String Text){
		Set<MatchPattern<Integer>> setResult = new TreeSet<MatchPattern<Integer>>();
		m_WumanTrie.match(Text, setResult);

		for (MatchPattern<Integer> p : setResult) {

			// WumanTrie
			int iStart = p.offset;
			int iEnd = p.offset + p.len;
			int iScore = p.pattern.value;
			
			if (iEnd == Text.length()) {	
				return true ;
			}
		} 	
		return false;
	
	}
}
