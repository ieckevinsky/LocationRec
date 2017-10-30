package com.hylanda.sevenfeature;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.swing.text.StyledEditorKit.ForegroundAction;

import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hylanda.segment.HLDictManager;
import com.hylanda.segment.HLSwknl;
import com.hylanda.segment.SegmentResult;
import com.hylanda.segment.SegmentWord;

public class PosTagging {

	public static final int  NATURE_D_A	 = 0x40000000;	//	形容词 形语素
	public static final int  NATURE_D_B	 = 0x20000000;	//	区别词 区别语素
	public static final int  NATURE_D_C	 = 0x10000000;	//	连词 连语素
	public static final int NATURE_D_D	 = 0x08000000;	//	副词 副语素
	public static final int NATURE_D_E	 = 0x04000000;	//	产品词
	public static final int NATURE_D_F	 = 0x02000000;	//	方位词 方位语素
	public static final int NATURE_D_I	 = 0x01000000;	//	成语
	public static final int NATURE_D_L	 = 0x00800000;	//	习语
	public static final int NATURE_A_M	 = 0x00400000;	//	数词 数语素
	public static final int NATURE_D_MQ	 = 0x00200000;	//	数量词
	public static final int NATURE_D_N	 = 0x00100000;	//	名词 名语素
	public static final int NATURE_D_O	 = 0x00080000;	//	拟声词
	public static final int NATURE_D_P	 = 0x00040000;	//	介词
	public static final int NATURE_A_Q	 = 0x00020000;	//	量词 量语素
	public static final int NATURE_D_R	 = 0x00010000;	//	代词 代语素
	public static final int NATURE_D_S	 = 0x00008000;	//	处所词
	public static final int NATURE_D_T	 = 0x00004000;	//	时间词
	public static final int NATURE_D_U	 = 0x00002000;	//	助词 助语素
	public static final int NATURE_D_V	 = 0x00001000;	//	动词 动语素
	public static final int NATURE_D_W	 = 0x00000800;	//	标点符号
	public static final int NATURE_D_X	 = 0x00000400;	//	非语素字
	public static final int NATURE_D_Y	 = 0x00000200;	//	语气词 语气语素
	public static final int NATURE_D_Z	 = 0x00000100;	//	状态词
	public static final int NATURE_A_NR	 = 0x00000080;	//	人名
	public static final int NATURE_A_NS	 = 0x00000040;	//	地名
	public static final int NATURE_A_NT	 = 0x00000020;	//	机构团体
	public static final int NATURE_A_NX	 = 0x00000010;	//	外文字符
	public static final int NATURE_A_NZ	 = 0x00000008;	//	其他专名
	public static final int NATURE_D_H	 = 0x00000004;	//	前接成分
	public static final int NATURE_D_K	 = 0x00000002;	//	后接成分
	 
	private static  Logger logger = LoggerFactory.getLogger(PosTagging.class);
	
	public static Map<String, Integer> tagmap = new HashMap<String,Integer>();
	public static int count = 0;
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		initTags();
//		List<String> tagresult = wordSegment("天津市举办第三届全运会");
//		for(String str : tagresult){
//			System.out.print(str+",");
//		}
		readCorpus();
	}
	
	public static void initTags() {
		tagmap.put("D_A",NATURE_D_A);//	形容词 形语素
		tagmap.put("D_B",NATURE_D_B);//	区别词 区别语素
		tagmap.put("D_C",NATURE_D_C);//	连词 连语素
		tagmap.put("D_D",NATURE_D_D);//	副词 副语素
		tagmap.put("D_E",NATURE_D_E);//	产品词
		tagmap.put("D_F",NATURE_D_F);//	方位词 方位语素
		tagmap.put("D_I",NATURE_D_I);//	成语
		tagmap.put("D_L",NATURE_D_L);//	习语
		tagmap.put("A_M",NATURE_A_M);//	数词 数语素
		tagmap.put("D_MQ",NATURE_D_MQ);//	数量词
		tagmap.put("D_N",NATURE_D_N);//	名词 名语素
		tagmap.put("D_O",NATURE_D_O);//	拟声词
		tagmap.put("D_P",NATURE_D_P);//	介词
		tagmap.put("A_Q",NATURE_A_Q);//	量词 量语素
		tagmap.put("D_R",NATURE_D_R);//	代词 代语素
		tagmap.put("D_S",NATURE_D_S);//	处所词
		tagmap.put("D_T",NATURE_D_T);//	时间词
		tagmap.put("D_U",NATURE_D_U);//	助词 助语素
		tagmap.put("D_V",NATURE_D_V);//	动词 动语素
		tagmap.put("D_W",NATURE_D_W);//	标点符号
		tagmap.put("D_X",NATURE_D_X);//	非语素字
		tagmap.put("D_Y",NATURE_D_Y);//	语气词 语气语素
		tagmap.put("D_Z",NATURE_D_Z);//	状态词
		tagmap.put("A_NR",NATURE_A_NR);//	人名
		tagmap.put("A_NS",NATURE_A_NS);//	地名
		tagmap.put("A_NT",NATURE_A_NT);//	机构团体
		tagmap.put("A_NX",NATURE_A_NX);//	外文字符
		tagmap.put("A_NZ",NATURE_A_NZ);//	其他专名
		tagmap.put("D_H",NATURE_D_H);//	前接成分
		tagmap.put("D_K",NATURE_D_K);//	后接成分
	
		logger.info("tagmap size :" + tagmap.size());
		
		if (!tagmap.isEmpty()) {
			logger.info("tag init ok ");
		}else{
			logger.info("tag init error ");
		}
//		
//		HLDictManager instance = HLDictManager.getInstance();
//		instance.setDictPath("/usr/java/Java_workspace/so/");
//		if(!instance.load()){
//
//			System.out.println("load dict err");
//			
//
//		}
	}
	
	public static void readCorpus() throws IOException, FileNotFoundException{
		FileOutputStream fos = new FileOutputStream("D:/读写txt/crfloc/hylandaTestdata5000.txt");
		OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
		FileOutputStream fos1 = new FileOutputStream("D:/读写txt/crfloc/hylandTraindata5000.txt");
		OutputStreamWriter osw1 = new OutputStreamWriter(fos1, "UTF-8");
		File file = new File("D:/读写txt/回溯数据/new/extract100000_1.txt");
		InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"utf-8");
		BufferedReader read = new BufferedReader(reader);
		String LineTxt = "" ;
		
		while ((LineTxt = read.readLine()) != null) {
			
			if (count<5000) {
				wordSegment(LineTxt.replaceAll("\r\n", ""),osw1);	
			}
			 else if (count >= 5000 && count <= 10000) {
				wordSegment(LineTxt.replaceAll("\r\n", ""),osw);		
			 
			}			
			else if (count>10000){
				break;
				
			}
			//System.out.println(count);
		}
		osw.close();
		read.close();
	}
	
	public static List<String> wordSegment(String Text,OutputStreamWriter osw) throws IOException{
		List<String> taglist = new ArrayList<String>();
		HLSwknl swknl = new HLSwknl();
		SegmentResult segRes = //swknl.segment("地名地址信息;普通地名;村庄级地名");
		swknl.segment(Text,swknl.MODE_MAX,swknl.MAKE_DICT_PROP, 0);
		SegmentWord[] words = segRes.getWords();
		 
		for(SegmentWord word:words){				
			String postag = posTag(word);
			taglist.add(postag);
		}
		
		wirteCorpus(osw,words ,taglist);
		
		
		return taglist;
	}
	
	public static List<String> wordSegmentNowrite(String Text) throws IOException{
		List<String> taglist = new ArrayList<String>();
		HLSwknl swknl = new HLSwknl();
		SegmentResult segRes = //swknl.segment("地名地址信息;普通地名;村庄级地名");
		swknl.segment(Text,swknl.MODE_MAX,swknl.MAKE_DICT_PROP, 0);
		SegmentWord[] words = segRes.getWords();
		 
		for(SegmentWord word:words){				
			String postag = posTag(word);
			taglist.add(postag);
		}
		
		List<String> newtaglist = new ArrayList<String>();
		 
		for(String str : taglist){
			newtaglist.add(str);
			 
		}
			modifyPosTag(newtaglist);
		
		List<String> resultlist = new ArrayList<String>();
		for(int i = 0 ;i < words.length ; i++){
				//System.out.println(words[i].getWord()+" "+taglist.get(i)+" "+newtaglist.get(i));
				resultlist.add(words[i].getWord()+" "+taglist.get(i)+" "+newtaglist.get(i));
		}
		
		return resultlist;
	}
	
	
	public static void 	modifyPosTag(List<String> list){
		Boolean flag = false ;
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
	}
	
	public static String posTag(SegmentWord word){

		for(Map.Entry<String, Integer> entry : tagmap.entrySet()){
			if ( (word.getFlags() & entry.getValue()) == entry.getValue()) {
				System.out.println(word.getWord()+"\t"+entry.getKey()+":"+(word.getFlags() & entry.getValue()));
				return entry.getKey();
			}
		}
		return null;
		
	}
	
	public static void wirteCorpus(OutputStreamWriter osw,SegmentWord[] words , List<String> taglist) throws IOException{
		
		Boolean flag = false ;
		List<String> newtaglist = new ArrayList<String>();
		 
		for(String str : taglist){
			newtaglist.add(str);
			if (str.equals("A_NS")) {
				flag = true ;
			}
		}
		if (flag) {
			modifyPosTag(newtaglist);
			for(int i = 0 ; i < words.length;i++){
				osw.write(words[i].getWord()+" "+taglist.get(i)+" "+newtaglist.get(i)+"\r\n");
				osw.flush();
				System.out.println(words[i].getWord()+" "+taglist.get(i)+" "+newtaglist.get(i));
				
				
			}
			osw.write("\r\n");
			osw.flush();
			count++;
		}
			
		 
		
	}
}
