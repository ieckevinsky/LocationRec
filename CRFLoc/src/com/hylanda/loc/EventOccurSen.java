package com.hylanda.loc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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
import java.util.TreeSet;

import org.omg.CORBA.INTERNAL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hylanda.common.trie.MatchPattern;
import com.hylanda.common.trie.Pattern;
import com.hylanda.segment.HLSwknl;
import com.hylanda.segment.SegmentResult;
import com.hylanda.segment.SegmentWord;
import com.hylanda.wumanbertrie.WumanberTrie;
import com.sun.org.apache.regexp.internal.recompile;

public class EventOccurSen {
	private static  Logger logger = LoggerFactory.getLogger(EventOccurSen.class);
	public static Set<Pattern<Integer>> vKey = new HashSet<Pattern<Integer>>();
	public static WumanberTrie<Integer> event_WumanTrie = new WumanberTrie<Integer>();
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		EventOccurSen eventOccurSen = new EventOccurSen();
		eventOccurSen.init("dict/eventdict.txt");
		String Text = "﻿‍邻水县人民政府　　关于加强关门石水库饮用水源保护的通告　沙尘天气。　关门石水库是邻水县城及周边群众的饮用水源，为切实加强关门石水库饮用水源保护沙尘天气。保障人民群众饮水安全，根据《中华人民共和国水法》、《中华人民共和国水污染防治法》、《中华人民共和国环境保护法》沙尘天气。《四川省饮用水水源保护管理条例》等相关法律法规，现将关门石水库饮用水源保护有关事项通告如下：　　一、禁止在水库及保护区内从事游泳、洗涤、烧烤、野炊、垂钓等污染水质的行为。天津市滨海新区大神堂村 沙尘天气。" ;
		Map<Integer, String > text_map = eventOccurSen.spiltText(Text);
		for(Map.Entry<Integer, String> entry : text_map.entrySet()){
			 System.out.println(entry.getKey()+"\t"+entry.getValue());
			 System.out.println(Text.substring(entry.getKey(), entry.getKey()+entry.getValue().length()));
			System.out.println(eventOccurSen.serachEvent(entry.getValue())) ;
		}
	}

	private   Map sortByComparatorkey(Map unsortMap) {

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
	
	public  void init(String readpath){

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
			logger.error("eventdict init error ");
		}

		event_WumanTrie.build(vKey);
		if (!vKey.isEmpty() && event_WumanTrie != null) {
			logger.info("eventdict init ok ");
		}else{
			logger.error("eventdict init error ");
		}
			}else {
				logger.error("eventdict.txt is not exit ");
			}
		}else {
			logger.error("eventdictpath is null ");
		}
		
	}
	public  Map<Integer, String> spiltText(String Text){
		Map<Integer, String> map = new HashMap<Integer,String>();
		String temp = "" ;
		int cursor = 0 ;
		 for(int i =0;i<Text.length();i++){
			 if (Text.charAt(i) == '。' || Text.charAt(i) == '；' || Text.charAt(i) == '？' || Text.charAt(i) == '！') {
				 temp += Text.charAt(i);
				 map.put(cursor, temp);
				 temp = "" ;
				 cursor = i+1 ;
			}else{
				temp += Text.charAt(i);
				map.put(cursor, temp);
			}
		 }
		 Map<Integer, String> result = new HashMap<Integer,String>();
		 for(Map.Entry<Integer, String> entry : map.entrySet()){
			 if (serachEvent(entry.getValue())) {
				 result.put(entry.getKey(), entry.getValue());
			}
		 }
		 result =  sortByComparatorkey(result);
		return result ;
		
	}
	
	public  Map<Integer, List<String>> spiltText(HLSwknl swknl,long handle){
		SegmentResult segRes = swknl.segment(handle, HLSwknl.MODE_MAX, HLSwknl.MAKE_FORSEARCH, 0);
		SegmentWord[] words = segRes.getWords();
		//Map<Integer, String> map = new HashMap<Integer,String>();
		Map<Integer, List<String>> mapwords = new HashMap<Integer,List<String>>();
		List<String> wordlist = new ArrayList<String>();
		int cursor = 0 ;
		for(int i = 0 ; i < words.length ; i++){
			//System.out.println(i+"\t"+words[i].getWord());
			if (words[i].getWord().endsWith("。") || words[i].getWord().endsWith("；") || words[i].getWord().endsWith("？") || words[i].getWord().endsWith("！") ) {
				wordlist.add(words[i].getWord());							
				mapwords.put(cursor, wordlist);
				cursor = 0 ;
				for(int j = 0 ; j <= i ; j++){
					cursor += words[j].getWord().length() ;
				}	
				wordlist = new ArrayList<String>();
			}else {
				wordlist.add(words[i].getWord());
				mapwords.put(cursor, wordlist);
				 
			}
		}
		
		 Map<Integer, List<String>> result = new HashMap<Integer,List<String>>();
		 for(Map.Entry<Integer, List<String>> entry : mapwords.entrySet()){
			//System.out.println("list begin : "+entry.getKey());
			StringBuffer sentence = new StringBuffer();
			 for(String str : entry.getValue()){
				 sentence.append(str); 
			 }
			 if (serachEvent(sentence.toString())) {
				 result.put(entry.getKey(), entry.getValue());
			}
		 }
		 result =  sortByComparatorkey(result);
		 
		return result ;
		
	}
	
	public  boolean  serachEvent(String Text){

		Set<MatchPattern<Integer>> setResult = new TreeSet<MatchPattern<Integer>>();
		event_WumanTrie.match(Text, setResult);

		if (setResult.size() > 0) {
			return true ;
		}
	
		return false ;
	
	}
}
