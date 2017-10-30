package com.hylanda.sevenfeature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hylanda.loc.MultiLevelNormalize;
import com.hylanda.segment.HLSwknl;
import com.hylanda.segment.SegmentResult;
import com.hylanda.segment.SegmentWord;
import com.sun.org.apache.xml.internal.security.Init;

public class CreateTrainData {
	private static  Logger logger = LoggerFactory.getLogger(CreateTrainData.class);
	public static Set<String> type_set = new HashSet<String>();
	public static Set<String> diff_set = new HashSet<String>();
	public static Set<String> direct_set = new HashSet<String>();
	public static Set<String> part_set = new HashSet<String>();
	public static PosTagging posTagging = new PosTagging();
	public static int count = 0 ;
	public static int current = 0;
	public static class Features{
		public String word ;
		public String words ;
		public String postag ;
		public String istype ;
		public String isdiff ;
		public String isdirect ;
		public String ispart ;
		public String tag;
	}
	
	public static void init(String type ,String diff ,String direct ,String part) throws IOException{
		inittype(type);
		initdiff(diff);
		initdirect(direct);
		initpart(part);
		posTagging.initTags();
	}
	
	public static void inittype(String type){
		//File file = new File("dict/类型语素.txt");
		
		if (type != null) {
			File file = new File(type);
			if (file.isFile() && file.exists()  ) { 
		InputStreamReader reader;
		try {			
			reader = new InputStreamReader(new FileInputStream(file),"utf-8");
			BufferedReader read = new BufferedReader(reader);
			String LineTxt = "" ;
			while((LineTxt = read.readLine())!=null){
				type_set.add(LineTxt);
			}
			logger.info("type_set size :" + type_set.size());
			if (!type_set.isEmpty()) {
				logger.info("typedict init ok " );
			}else{
				logger.info("typedict init error " );
			}
			read.close();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			logger.info("typedict init error " );
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			logger.info("typedict init error " );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.info("typedict init error " );
		}
		}else{
			logger.info("typedict.txt not exit " );
		}
		}else{
			logger.info("typedict path is null " );
		}
	}
	public static void initdiff(String diff) {
		//File file = new File("dict/区别性语素.txt");
		if (diff != null) {
			File file = new File(diff);
			if (file.isFile() && file.exists()  ) { 
		InputStreamReader reader;
		try {
			reader = new InputStreamReader(new FileInputStream(file),"utf-8");
			BufferedReader read = new BufferedReader(reader);
			String LineTxt = "" ;
			while((LineTxt = read.readLine())!=null){
				diff_set.add(LineTxt);
			}
			logger.info("diff_set size :" + diff_set.size());
			if (!diff_set.isEmpty()) {
				logger.info("diffdict init ok " );
			}else{
				logger.info("diffdict init error " );
			}
			read.close();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			logger.info("diffdict init error " );
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			logger.info("diffdict init error " );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.info("diffdict init error " );
		}
			}else {
				logger.info("diffdict.txt is not exit " );
			}
		}else {
			logger.info("diffdict path is empty " );
		}
	
	}
	public static void initdirect(String direct){
		//File file = new File("dict/方位语素.txt");
		if (direct != null) {
			File file = new File(direct);
			if (file.isFile() && file.exists()  ) { 
 
		InputStreamReader reader;
		try {
			reader = new InputStreamReader(new FileInputStream(file),"utf-8");
			BufferedReader read = new BufferedReader(reader);
			String LineTxt = "" ;
			while((LineTxt = read.readLine())!=null){
				direct_set.add(LineTxt);
			}
			logger.info("direct_set size :" + direct_set.size());
			if (!direct_set.isEmpty()) {
				logger.info("directdict init ok " );
			}else{
				logger.info("directdict init error " );
			}
			read.close();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			logger.info("directdict init error " );
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			logger.info("directdict init error " );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.info("directdict init error " );
		}
			}else {
				logger.info("directdict is not exit " );
			}
			
		}else {
			logger.info("directdictpath is null" );
		}
	
}
	public static void initpart(String part){
		//File file = new File("dict/部位语素.txt");
		if (part != null) {
			File file = new File(part);
			if (file.isFile() && file.exists()  ) { 
 
		InputStreamReader reader;
		try {
			reader = new InputStreamReader(new FileInputStream(file),"utf-8");
			BufferedReader read = new BufferedReader(reader);
			String LineTxt = "" ;
			while((LineTxt = read.readLine())!=null){
				part_set.add(LineTxt);
			}
			
			logger.info("part_set size :" + part_set.size());
			if (!part_set.isEmpty()) {
				logger.info("partdict init ok " );
			}else{
				logger.info("partdict init error " );
			}
			read.close();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			logger.info("partdict init error " );
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			logger.info("partdict init error " );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.info("partdict init error " );
		}
			}else {
				logger.info("partdict is not exit " );
			}
		}else{
			logger.info("partdictpath is null " );
		}
	
	}
	
	public List<Features> gernateFeatures(String Text,long handle){	
		List<Features> list_features = new ArrayList<Features>();
		List<String> list;
		list = wordSegmentNowrite(Text, handle);
		for (String str : list) {
			String[] temp = str.split(" ");
			for(int i = 0 ; i < temp[0].length();i++){
				Features features = new Features();
				features.word = String.valueOf(temp[0].charAt(i));
				features.words = temp[0];
				features.postag =temp[1];
				features.istype = def_istype(features.word,features.words);
				features.isdiff = def_isdiff(features.word,features.words);
				features.isdirect = def_isdirect(features.word,features.words);
				features.ispart = def_ispart(features.word,features.words);
				if (temp[0].length()>1) {
					if (temp[2].equals("B_A_NS")) {
						if (i==0) {
							features.tag = temp[2] ;
						}else{
							features.tag = "I_A_NS" ;
						}
					}else{
						features.tag = temp[2] ;
					}
				}else{
					features.tag = temp[2] ;
				}
				
				list_features.add(features);
			}
			
		}
		return list_features ;

		
	}
	
	
	public static List<Features> gernateFeatures(String Text) throws IOException{	
		List<Features> list_features = new ArrayList<Features>();
		List<String> list = wordSegmentNowrite(Text);
		for (String str : list) {
			String[] temp = str.split(" ");
			for(int i = 0 ; i < temp[0].length();i++){
				Features features = new Features();
				features.word = String.valueOf(temp[0].charAt(i));
				features.words = temp[0];
				features.postag =temp[1];
				features.istype = def_istype(features.word,features.words);
				features.isdiff = def_isdiff(features.word,features.words);
				features.isdirect = def_isdirect(features.word,features.words);
				features.ispart = def_ispart(features.word,features.words);
				if (temp[0].length()>1) {
					if (temp[2].equals("B_A_NS")) {
						if (i==0) {
							features.tag = temp[2] ;
						}else{
							features.tag = "I_A_NS" ;
						}
					}else{
						features.tag = temp[2] ;
					}
				}else{
					features.tag = temp[2] ;
				}
				
				list_features.add(features);
			}
			
		}
		return list_features ; 
	}
	public static String def_istype(String word , String words){
		 for(String str : type_set){
			 if (str.equals(word) || str.equals(words)) {
				return "Y" ;
			}
		 }
		 return "N";
	}
	public static String def_isdiff(String word , String words){
		 for(String str : diff_set){
			 if (str.equals(word) || str.equals(words)) {
				return "Y" ;
			}
		 }
		 return "N";
	
	}
	public static String def_isdirect(String word , String words){

		 for(String str : direct_set){
			 if (str.equals(word) || str.equals(words)) {
				return "Y" ;
			}
		 }
		 return "N";
	
	}
	public static String def_ispart(String word , String words){

		 for(String str : part_set){
			 if (str.equals(word) || str.equals(words)) {
				return "Y" ;
			}
		 }
		 return "N";
	
	}
	
	public static String posTag(SegmentWord word){

		for(Map.Entry<String, Integer> entry : posTagging.tagmap.entrySet()){
			if ( (word.getFlags() & entry.getValue()) == entry.getValue()) {
			//	System.out.println(word.getWord()+"\t"+entry.getKey()+":"+(word.getFlags() & entry.getValue()));
				return entry.getKey();
			}
		}
		return "D_N";
		
	}
	
	public static List<String> wordSegmentNowrite(String Text,long handle) {
		List<String> taglist = new ArrayList<String>();
		HLSwknl swknl = new HLSwknl();
		SegmentResult segRes = swknl.segment(handle, swknl.MODE_MAX,swknl.MAKE_FORSEARCH, 0);
		SegmentWord[] words = segRes.getWords();
		

		for(SegmentWord word:words){				
			String postag = posTag(word);
			taglist.add(postag);
		}
		
		List<String> newtaglist = new ArrayList<String>();
		boolean flag = false ;
		for(String str : taglist){
			newtaglist.add(str);
			if (str.equals("A_NS")) {
				flag = true ;
			} 
		}
		if (flag) {
			count++;
		} 
		modifyPosTag(newtaglist);
		
		List<String> resultlist = new ArrayList<String>();
		for(int i = 0 ;i < words.length ; i++){
			resultlist.add(words[i].getWord()+" "+taglist.get(i)+" "+newtaglist.get(i));
		}
		
		return resultlist;
	}
	
	public static List<String> wordSegmentNowrite(String Text) {
		List<String> taglist = new ArrayList<String>();
		HLSwknl swknl = new HLSwknl();
		SegmentResult segRes = swknl.segment(Text, swknl.MODE_MAX,swknl.MAKE_DICT_PROP, 0);
		//System.out.println(Text); 
		SegmentWord[] words = segRes.getWords();

		 
		for(SegmentWord word:words){				
			String postag = posTag(word);
			taglist.add(postag);
		}
		
		List<String> newtaglist = new ArrayList<String>();
		boolean flag = false ;
		for(String str : taglist){
			newtaglist.add(str);
			if (str.equals("A_NS")) {
				flag = true ;
			} 
		}
		if (flag) {
			count++;
		} 
		modifyPosTag(newtaglist);
		
		List<String> resultlist = new ArrayList<String>();
		for(int i = 0 ;i < words.length ; i++){
			resultlist.add(words[i].getWord()+" "+taglist.get(i)+" "+newtaglist.get(i));
		}
		
		return resultlist;
	}
	
	public static void 	modifyPosTag(List<String> list){
	 
		for(int i = 0 ; i< list.size();i++){
			if (list.get(i).startsWith("A_NS")) {
				if (i == 0) {
					if (list.get(i).equals("A_NS")) {
						 if (list.size()>1 ) {
							 if (list.get(i+1).equals("A_NS")) {
								 list.set(i, "B_A_NS") ;
							}else{
								list.set(i, "S_A_NS") ;
							}							 
						}else {
							list.set(i, "S_A_NS") ;
						}
						
					}
				}
				else if (i > 0 && i < list.size()-1) {

					if (list.get(i-1).equals("B_A_NS")  ) {
						if (list.get(i+1).equals("A_NS")) {
							list.set(i, "I_A_NS") ;
						}else{
							list.set(i, "E_A_NS") ;
						} 
					}
					else if (list.get(i-1).equals("I_A_NS")) {
						if (list.get(i+1).equals("A_NS")) {
							list.set(i, "I_A_NS") ;
						}else{
							list.set(i, "E_A_NS") ;
						} 
					}else {
						if (list.get(i+1).equals("A_NS")) {
							list.set(i, "B_A_NS") ;
						}else{
							list.set(i, "S_A_NS") ;
						} 
					}
				}
				else if (i == list.size()-1) {
					if (list.get(i-1).equals("B_A_NS")||list.get(i-1).equals("I_A_NS")) {
						list.set(i, "E_A_NS") ;
					}else {
						list.set(i, "S_A_NS") ;
					}
				} 
			}
		}
		for(int i = 0 ; i < list.size() ; i++){
			if (list.get(i).contains("A_NS")) {
				if (list.get(i).equals("S_A_NS")) {
					list.set(i, "B_A_NS") ;
				}
				else if (list.get(i).equals("E_A_NS")) {
					list.set(i, "I_A_NS") ;
				}
			}else{
				list.set(i, "O");
			}
		}
		
	}
	
	public static void readCorpus() throws IOException, IOException{
		
		FileOutputStream fostrain = new FileOutputStream("D:/读写txt/crfloc/CRFTraindata2000.txt");
		OutputStreamWriter trainwriter = new OutputStreamWriter(fostrain, "UTF-8");
		FileOutputStream fostest = new FileOutputStream("D:/读写txt/crfloc/CRFTestdata3000.txt");
		OutputStreamWriter testwriter = new OutputStreamWriter(fostest, "UTF-8");
		
		File file = new File("D:/读写txt/回溯数据/new/extract100000_1.txt");
		InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"utf-8");
		BufferedReader read = new BufferedReader(reader);
		String LineTxt = "" ;
		while((LineTxt = read.readLine())!= null){
			List<Features> result = gernateFeatures(LineTxt);
			if (count <2000) {
				if (count > current) {
					for(Features fea : result){
						String output = fea.word+" "+fea.words+" "+fea.postag+" "+fea.istype+" "+fea.isdiff+" "+fea.isdirect+" "+fea.ispart+" "+fea.tag;
						System.out.println(output);
						trainwriter.write(output+"\r\n");
						trainwriter.flush();
					}
					trainwriter.write("\r\n");
					trainwriter.flush();
				}
				
			}
			else if (count >=2000 && count <= 5000) {
				if (count > current) {
					for(Features fea : result){
						String output = fea.word+" "+fea.words+" "+fea.postag+" "+fea.istype+" "+fea.isdiff+" "+fea.isdirect+" "+fea.ispart+" "+fea.tag;
						System.out.println(output);
						testwriter.write(output+"\r\n");
						testwriter.flush();
					}
					testwriter.write("\r\n");
					testwriter.flush();
				}
				
			
			}
			else {
				break;
			}
			 
		}
		trainwriter.close();
		read.close();
		
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		init("dict/类型语素.txt","dict/区别性语素.txt","dict/方位语素.txt","dict/部位语素.txt");
		//readCorpus();
		sentencetest();
	}

	public static void sentencetest() throws IOException{

		
		//FileOutputStream fostrain = new FileOutputStream("D:/BaiduNetdiskDownload/人民日报语料/CRFpeoplenewspaperSentences.txt");
		FileOutputStream fostrain = new FileOutputStream("D:/BaiduNetdiskDownload/精加工地名语料/精加工一百万语料/features.txt");
		OutputStreamWriter trainwriter = new OutputStreamWriter(fostrain, "UTF-8");
		
		File file = new File("D:/BaiduNetdiskDownload/精加工地名语料/精加工一百万语料/sentence.txt");
		InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"utf-8");
		BufferedReader read = new BufferedReader(reader);
		String LineTxt = "" ;
		while((LineTxt = read.readLine())!= null){
			List<Features> result = gernateFeatures(LineTxt);
			
					for(Features fea : result){
						String output = fea.word+" "+fea.words+" "+fea.postag+" "+fea.istype+" "+fea.isdiff+" "+fea.isdirect+" "+fea.ispart+" "+fea.tag;
						//System.out.println(output);
						trainwriter.write(output+"\r\n");
						trainwriter.flush();
						count++;
					}
					trainwriter.write("\r\n");
					trainwriter.flush();
					count++;
//			if (count > 100000) {
//				break;
//			}
			if (count % 50000  == 0) {
				System.out.println(count);
			}
			 
		}
		trainwriter.close();
		read.close();
		
	
	}
	
}
