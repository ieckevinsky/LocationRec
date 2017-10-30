package com.crf.test;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.chasen.crfpp.Tagger;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.PUBLIC_MEMBER;

import com.hylanda.sevenfeature.CreateTrainData;
import com.hylanda.sevenfeature.CreateTrainData.Features;
import com.hylanda.tool.Poi;
import com.sun.org.apache.xml.internal.resolver.helpers.PublicId;

public class test {
	public static CreateTrainData createTrainData = new CreateTrainData();
	
	public static void readCorpus() throws IOException {
		long startTime=System.currentTimeMillis();   //获取开始时间
		Tagger tagger = new Tagger("-m /usr/java/Java_workspace/CRFLoc/model -v 3 -n2");
		FileOutputStream fos = new FileOutputStream("data/locresult.txt");
		OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8");
		File file = new File("data/outputresult_count_wloc_yes_loc_no.txt");
		InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "utf-8");
		BufferedReader read = new BufferedReader(reader);
		String LineTxt = "";
		while ((LineTxt = read.readLine()) != null) {
			System.out.println(LineTxt);
			writer.write(LineTxt + "\tloc:");			
			deal(LineTxt,writer,tagger );
			writer.write("\r\n");
			writer.flush();
		}
		writer.close();
		read.close();
		 
 
		 
		long endTime=System.currentTimeMillis(); //获取结束时间
	
		System.out.println("程序运行时间： "+(endTime-startTime)+"ms");
	}

	public static void deal(String Text,OutputStreamWriter writer,Tagger tagger  ) throws IOException {
		

		// clear internal context
		tagger.clear();

		// List<Features> result =
		// createTrainData.gernateFeatures("天津市的滨海新区大神堂发生火灾");.
		List<Features> result = createTrainData.gernateFeatures(Text);
		
		for (Features fea : result) {
			String output = fea.word + " " + fea.words + " " + fea.postag + " " + fea.istype + " " + fea.isdiff + " "
					+ fea.isdirect + " " + fea.ispart + " " + fea.tag;
			//System.out.println(output);
			tagger.add(output);
		}
		// add context
		 tagger.add("滨 滨海 A_NZ N N N Y O");
		 tagger.add("海 滨海 A_NZ Y N N N O");
		 tagger.add("新 新区 D_N N Y N N O");
		 tagger.add("区 新区 D_N N N N N O");
		 tagger.add(" 大 大 D_D N Y N N O");
		 tagger.add("神 神堂 D_N N N N N O");
		 tagger.add(" 堂 神堂 D_N N N N N O");
		 tagger.add("发 发生 D_V N N N N O");
		 tagger.add("生 发生 D_V N N N N O");
		 tagger.add(" 火 火灾 D_N N N N N O");
		 tagger.add(" 灾 火灾 D_N N N N N O");

		// System.out.println("column size: " + tagger.xsize());
		// System.out.println("token size: " + tagger.size());
		// System.out.println("tag size: " + tagger.ysize());
		//
		// System.out.println("tagset information:");
		// for (int i = 0; i < tagger.ysize(); ++i) {
		// System.out.println("tag " + i + " " + tagger.yname(i));
		// }

		// parse and change internal stated as 'parsed'
		if (!tagger.parse())
			return;

		// System.out.println("conditional prob=" + tagger.prob() + " log(Z)=" +
		// tagger.Z());

		List<String> tag = new ArrayList<String>();
		for (int i = 0; i < tagger.size(); ++i) {
			for (int j = 0; j < tagger.xsize(); ++j) {
				// System.out.print(tagger.x(i, j) + "\t");
			}
			// System.out.print(tagger.y2(i) + "\t");
			tag.add(tagger.y2(i));
			// System.out.print("\n");

			// System.out.print("Details");
			for (int j = 0; j < tagger.ysize(); ++j) {
				// System.out.print("\t" + tagger.yname(j) + "/prob=" +
				// tagger.prob(i,j)
				// + "/alpha=" + tagger.alpha(i, j)
				// + "/beta=" + tagger.beta(i, j));
			}
			// System.out.print("\n");
		}

		int count = -1;
		boolean flag = false;
		List<String> loclist = new ArrayList<String>();

		for (int i = 0; i < tagger.size(); ++i) {
			// System.out.print(tagger.x(i, 0)+"\t"+tagger.x(i, 2) + "\t");
			// System.out.print(tagger.y2(i) + "\t");
			// System.out.print("\n");
			if (tagger.x(i, 2).equals("A_NS") && tagger.y2(i).equals("O")) {
				if (i == 0) {
					tag.set(i, "B_NS");
				} else {
					if (tag.get(i - 1).equals("B_NS") || tag.get(i - 1).equals("I_NS")) {
						tag.set(i, "I_NS");
					} else {
						tag.set(i, "B_NS");
					}
				}
			}
			if (tag.get(i).contains("NS")) {
				if (flag) {
					loclist.set(count, loclist.get(count) + tagger.x(i, 0));
				} else {
					loclist.add(tagger.x(i, 0));
					count++;
				}
				flag = true;
			} else {
				flag = false;
			}

		}
		for (String str : loclist) {
			//System.out.println("loc : " + str);
			writer.write(str+",");
			writer.flush();
		}

		
		// // when -n20 is specified, you can access nbest outputs
		// System.out.println("nbest outputs:");
		// for (int n = 0; n < 10; ++n) {
		// if (! tagger.next()) break;
		// System.out.println("nbest n=" + n + "\tconditional prob=" +
		// tagger.prob());
		// // you can access any information using tagger.y()...
		// }

		// System.out.println("Done");

	}

	public static void test() throws IOException{

		Tagger tagger = new Tagger("-m model -v 3 -n2");
		// clear internal context
		tagger.clear();

		// List<Features> result =
		// createTrainData.gernateFeatures("天津市的滨海新区大神堂发生火灾");.
		String Text = "9月18日7时许 京港澳高速路口北京段一限高架掉落砸中车辆";
		List<Poi> list_crf = new ArrayList<Poi>() ;
		List<Features> result = createTrainData.gernateFeatures(Text);
		// add context
		for (Features fea : result) {
			String output = fea.word + " " + fea.words + " " + fea.postag + " " + fea.istype + " " + fea.isdiff + " "
					+ fea.isdirect + " " + fea.ispart + " " + fea.tag;
			System.out.println(output);
			tagger.add(output);
		}
//		 tagger.add("滨 滨海 A_NZ N N N Y O");
//		 tagger.add("海 滨海 A_NZ Y N N N O");
//		 tagger.add("新 新区 D_N N Y N N O");
//		 tagger.add("区 新区 D_N N N N N O");
//		 tagger.add(" 大 大 D_D N Y N N O");
//		 tagger.add("神 神堂 D_N N N N N O");
//		 tagger.add(" 堂 神堂 D_N N N N N O");
//		 tagger.add("发 发生 D_V N N N N O");
//		 tagger.add("生 发生 D_V N N N N O");
//		 tagger.add(" 火 火灾 D_N N N N N O");
//		 tagger.add(" 灾 火灾 D_N N N N N O");


		 System.out.println("column size: " + tagger.xsize());
		 System.out.println("token size: " + tagger.size());
		 System.out.println("tag size: " + tagger.ysize());
		
		 System.out.println("tagset information:");
		 for (int i = 0; i < tagger.ysize(); ++i) {
		 System.out.println("tag " + i + " " + tagger.yname(i));
		 }

		// parse and change internal stated as 'parsed'
		if (!tagger.parse())
			return;

		 System.out.println("conditional prob=" + tagger.prob() + " log(Z)=" +tagger.Z());

		List<String> tag = new ArrayList<String>();
		for (int i = 0; i < tagger.size(); ++i) {
			for (int j = 0; j < tagger.xsize(); ++j) {
				 System.out.print(tagger.x(i, j) + "\t");
			}
			 System.out.print(tagger.y2(i) + "\t");
			tag.add(tagger.y2(i));
			 System.out.print("\n");

			 System.out.print("Details");
			for (int j = 0; j < tagger.ysize(); ++j) {
				 System.out.print("\t" + tagger.yname(j) + "/prob=" +
				 tagger.prob(i,j)
				 + "/alpha=" + tagger.alpha(i, j)
				 + "/beta=" + tagger.beta(i, j));
			}
			 System.out.print("\n");
		}

		int count = -1;
		boolean flag = false;
		List<String> loclist = new ArrayList<String>();

		Map<Integer, Integer> map_crfloc = new HashMap<Integer,Integer>();
		int cousor = 0 ;
		for (int i = 0; i < tagger.size(); ++i) {
			 System.out.print(tagger.x(i, 0)+"\t"+tagger.x(i, 2) + "\t");
			 System.out.print(tagger.y2(i) + "\t");
			 System.out.print("\n");
			if (tagger.x(i, 2).equals("A_NS") && tagger.y2(i).equals("O")) {
				if (i == 0) {
					tag.set(i, "B_NS");
					
				} else {
					if (tag.get(i - 1).equals("B_NS") || tag.get(i - 1).equals("I_NS")) {
						tag.set(i, "I_NS");
					} else {
						tag.set(i, "B_NS");
					}
				}
			}
			if (tag.get(i).contains("NS")) {
				if (flag) {
					loclist.set(count, loclist.get(count) + tagger.x(i, 0));
					map_crfloc.put(cousor, i+tagger.x(i, 0).length());
				} else {
					loclist.add(tagger.x(i, 0));
					map_crfloc.put(cousor, i+tagger.x(i, 0).length());
					count++;
				}
				flag = true;
			} else {
				flag = false;
				cousor = i+1 ;
			}

		}
		map_crfloc = sortByComparator(map_crfloc);
		for(Map.Entry<Integer, Integer> entry : map_crfloc.entrySet()){
			System.out.println(entry.getKey()+"~"+entry.getValue() +" loc:"+Text.substring(entry.getKey(), entry.getValue()));
			Poi poi = new Poi() ;
			poi.setName(Text.substring(entry.getKey()+1, entry.getValue()+1));
			poi.setStartPos(entry.getKey()+1);
			list_crf.add(poi);
		}
		for (String str : loclist) {
			System.out.println("loc : " + str);
		}

		 // when -n20 is specified, you can access nbest outputs
		 System.out.println("nbest outputs:");
		 for (int n = 0; n < 10; ++n) {
		 if (! tagger.next()) break;
		 System.out.println("nbest n=" + n + "\tconditional prob=" +
		 tagger.prob());
		 // you can access any information using tagger.y()...
		 }

		 System.out.println("Done");

	
	}
	
	
	public static void opentest() throws IOException  {
		Tagger tagger = new Tagger("-m /usr/java/Java_workspace/CRFLoc/model -v 3 -n2");
		FileOutputStream fos = new FileOutputStream("data/openlocresult.txt");
		OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8");
		File file = new File("data/浙江省全部村子.txt");
		InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "utf-8");
		BufferedReader read = new BufferedReader(reader);
		String LineTxt = "";
		while ((LineTxt = read.readLine()) != null) {
			System.out.println(LineTxt);
			writer.write(LineTxt + "\tloc:");			
			deal(LineTxt+"发生交通事故",writer,tagger);
			writer.write("\r\n");
			writer.flush();
		}
		writer.close();
		read.close();
	
	}
	
	private static Map sortByComparator(Map unsortMap) {

		 
        List list = new LinkedList(unsortMap.entrySet());
 
        //sort list based on comparator
        Collections.sort(list, new Comparator() 
        {
             public int compare(Object o1, Object o2) 
             {
          return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
         }
});
 
		        //put sorted list into map again
		Map sortedMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
		    Map.Entry entry = (Map.Entry)it.next();
		    sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
   }
	
	public static void main(String[] argv) throws IOException {
		//readCorpus();
		test();
		//opentest();
	}

	static {
		try {
			try {
			 
				createTrainData.init("dict/类型语素.txt","dict/区别性语素.txt","dict/方位语素.txt","dict/部位语素.txt");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.loadLibrary("CRFPP");
		} catch (UnsatisfiedLinkError e) {
			System.err.println(
					"Cannot load the example native code.\nMake sure your LD_LIBRARY_PATH contains \'.\'\n" + e);
			System.exit(1);
		}
	}

}
