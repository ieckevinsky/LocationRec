package com.hylanda.loc;

import java.awt.TexturePaint;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.sound.midi.MidiDevice.Info;

import com.hylanda.common.trie.MatchPattern;
import com.hylanda.common.trie.Pattern;
import com.hylanda.loc.MultiLevelNormalize.NormalResult;
import com.hylanda.tool.Poi;
import com.hylanda.wumanbertrie.WumanberTrie;
import com.sun.jndi.url.iiopname.iiopnameURLContextFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * 多级别词汇归一化处理，比如：地名的行政区划归一化
 *  扫描指定字段文本， 查找市区县关键词，进行映射处理。需要考虑同一地名词汇指向多个行政区划的问题，
 *  		将词典设计为有向交叉树的结构，引入一定的归一化优选处理逻辑。
 *  
 *  输出格式：国家-省市-市/地区-区县， 只会输出前几级，不会中间间隔输出后面的行政区划
 *  		允许外面指定处理字段和输出字段，可以同时处理多个字段。
 *  
 *  地名模块封装，词典格式：   
 *  	地名名称(标准名称) \t 多名称（半角逗号分隔）\t行政区划层级归属（不含自身，多个上级标准名称，用-分隔） 
 *  
 *  每个标准名称有唯一的id编码。 每一个名称词可能指向多个标准名称。
 *  每一个名称的层级归属，要视文本内的多个地名词汇归属有关；进行计数优选。
 * @author yaozhipeng 2017/10/17
 *
 */
public class MultiLevelNormalize {
	private static  Logger logger = LoggerFactory.getLogger(MultiLevelNormalize.class);

	public static class BlackNode{
		public String name = null ;
		public boolean leftneighborhood = false ;
		public boolean rightneighborhood = false ;

	}
	public static class NormalResult{
		public String word = null;
		public int offset = -1;
		public int level = -1;//业务用level值,仅存储用
		public String multiLevel = null;//多级上级（标准名），含自身，用-分隔
		public List<Integer> multiNameIdx = new ArrayList<Integer>();//内部用,指向_listMultiName到下标, 第一次用
		public int levelIdx = -1;//内部用,优选后结果，指向_listLevelNodes到下标，第二次用
		public int oldWordLevel = -1;  //add by haojing
		public String lastword = null;  //合并识别结果的最后一个词
		public String stdmultiLevelintact = null;//完整的 多级上级（标准名）不受限于levelmax，含自身，用-分隔
		public boolean iscombine = false ; // 是否为合并类型地名
	}
	public static class LevelNode{
		public int idx = -1;
		public String stdName = null;//标准名称，输出用
		//public String names = null;//查找使用的多名称，用,分隔
		public int level = -1;//业务用level值,仅存储用
		public String upLevelNames = null;//上级名称，用-分隔（上级名称必须为标准名称）
		public String coordinates_baidu = null ;	//baidu经纬度坐标值
		public String coordinates_gaode = null ;  //gaode经纬度坐标值
		public String areaId = null ;  //行政区划id
		public String type = null;  //地名类型
		public List<Integer> listUpLevel = new ArrayList<Integer>();
	}

	private static MultiLevelNormalize instance = new MultiLevelNormalize();
	public static MultiLevelNormalize getInstance() {
		return instance;
	}
	//标准名称全集，数组到下标很关键
	private List<LevelNode> _listLevelNodes = new ArrayList<LevelNode>();
	private List<BlackNode> listblack = new ArrayList<BlackNode>();
	//普通名称的tire树，词指向到依然是_listMultiName数组下标
	private WumanberTrie<List<Integer>> _nameTrie = new WumanberTrie<List<Integer>>(); ;
	private boolean _initOK = false;
	
	//记录断句位置
	private List<Integer> list_endchar = new ArrayList<Integer>(); 
	private int txtLength = 0; 
	//词典格式：
	//	地名名称(标准名称) \t 多名称（半角逗号分隔）\t行政区划层级归属（不含自身，多个上级标准名称，用-分隔） 
	//public boolean init(String regindict){
	public boolean init(String regindict,String hotpointdict,String foreigndict,String placedict,String blackdict){
		logger.info("now version build on 20171017_yzp...");
		
		if (_initOK) {
			logger.info("load dict before , return ");
			return true;
		}
		
		
		//存储普通名称，指向标准名称数组到下标，可能一名多用
		List<List<Integer>> _listMultiName = new ArrayList<List<Integer>>();
		//普通名称 和 _listMultiName数组下标 映射到map
		Map<String,Integer> _multiNameMap = new HashMap<String, Integer>();
		
		//List<String> lineList = readTxtFile(regindict,"utf-8");
		List<String> lineList = readTxtFile(regindict,"utf-8");
		lineList.addAll( readTxtFile(hotpointdict,"utf-8"));
		lineList.addAll( readTxtFile(foreigndict,"utf-8"));
		lineList.addAll( readTxtFile(placedict,"utf-8"));
		
		logger.info("dict size is : "+lineList.size());
		System.out.println(lineList.size());
		if(lineList.size() == 0)
			return false;
		_listLevelNodes.clear();
		
		//标准名称映射下标，注意，此map中到标准名称为加上上级名称的，如： 中国-天津市-和平区
		Map<String,Integer> stdNameMap = new HashMap<String,Integer>();
		int curIdx = 0;
		
		for(String line:lineList){			
			String[] fields = line.split("\t");
			if(fields.length == 7 && fields[0].length() > 0){
				String key = fields[3]+"-"+fields[0];
				if(fields[3].equals("null")){
					key = fields[0];
				}
				if(!stdNameMap.containsKey(key)){
					stdNameMap.put(key, new Integer(curIdx));
					//组织标准名称
					//System.out.println(line);
					LevelNode oneNode = new LevelNode();
					oneNode.idx = curIdx;
					oneNode.stdName = fields[0];
					String otherNames = fields[1];
					oneNode.level = Integer.valueOf(fields[2]).intValue();
					oneNode.upLevelNames = fields[3];
					oneNode.areaId = fields[4];
					if (fields[5].split(";").length==2) {
						oneNode.coordinates_gaode = fields[5].split(";")[0];
						oneNode.coordinates_baidu = fields[5].split(";")[1];
					}
					else if (fields[5].split(";").length==1) {
						oneNode.coordinates_gaode = fields[5].split(";")[0];
					}
					oneNode.type = fields[6];
					_listLevelNodes.add(oneNode);
					curIdx ++;
					//组织别称
					String[] names = otherNames.split(",");
					for(int i=0;i<names.length;i++){
						int nameListIdx = -1;
						if(_multiNameMap.containsKey(names[i]))
							nameListIdx = _multiNameMap.get(names[i]).intValue();
						else{
							nameListIdx = _listMultiName.size();
							List<Integer> stdIdxList = new ArrayList<Integer>();
							_listMultiName.add(stdIdxList);
							_multiNameMap.put(names[i], new Integer(nameListIdx));
						}
						List<Integer> stdIdxList = _listMultiName.get(nameListIdx);
						stdIdxList.add(oneNode.idx);
					}
				}
			}
		}
		//组织标准名称的上级名称id
		for(LevelNode oneNode : _listLevelNodes){
			
			String[] upLevelNames = oneNode.upLevelNames.split("-");
			StringBuffer sbUpName = new StringBuffer();
			for (int i = 0; i < upLevelNames.length; i++) {
				if(upLevelNames[i].equals("null")){
					break;
				}
				if(i > 0){
					sbUpName.append("-");
				}
				sbUpName.append(upLevelNames[i]);
				//System.out.println(sbUpName.toString());
				if(stdNameMap.containsKey(sbUpName.toString())){
					int upLevelIdx = stdNameMap.get(sbUpName.toString()).intValue();
					oneNode.listUpLevel.add(new Integer(upLevelIdx));
				}else{
					//System.out.println("MultiLevelNomalize: not find up level name! stdName="+oneNode.stdName+",upLeveName="+sbUpName.toString());
				}
			}
		}
		
		//为多别称建立双trie树词典
		Set<String> multiNameSet = _multiNameMap.keySet();
		
		_nameTrie = new WumanberTrie<List<Integer>>();
		Set<Pattern<List<Integer>>> vKey = new HashSet<Pattern<List<Integer>>>();
		for(String multiName:multiNameSet){
			int nameIdx = _multiNameMap.get(multiName);
			//List<List<Integer>> _listMultiName 
			List<Integer> yinshe = _listMultiName.get(nameIdx);
			vKey.add(new Pattern<List<Integer>>(multiName, yinshe));
		}
		_nameTrie.build(vKey);
		initBlack(blackdict);
		 
	//	System.out.println(_listMultiName.size());
//		try {
//			_nameTrie.save("test.data");
//			_nameTrie.clear();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		
		_initOK = _nameTrie != null;

		//初始化
		if(!_initOK){
			logger.info("init error!");
			 
		}else{
			logger.info("init finish ...");	 
			logger.info("regindict is : " + regindict );
			logger.info("hotpointdict is : " + hotpointdict );
			logger.info("foreigndict is : " + foreigndict );
			logger.info("placedict is : " + placedict );
			logger.info("tire size is "+String.valueOf(_listMultiName.size()));	 
			_multiNameMap.clear();
			logger.info("_multiNameMap size is "+String.valueOf(_multiNameMap.size()));	 
			_listMultiName.clear();
			logger.info("_listMultiName size is "+String.valueOf(_listMultiName.size()));	 
			 
		}
//		*/
//		_initOK = true; 
		return _initOK;
	}
	
	/*
	 * 初始化 左右邻黑名单
	 * */
	private Boolean initBlack(String readpath){
		boolean flag = false;
		if (readpath != null) {
		 
		File file = new File(readpath);
		if (file.isFile() && file.exists()) { // 判断文件是否存在
		InputStreamReader reader;
		try {
			reader = new InputStreamReader(new FileInputStream(file),"utf-8");
			BufferedReader read = new BufferedReader(reader);
			String LineTxt = "";
			while((LineTxt = read.readLine())!=null){
				String[] temp = LineTxt.split("\t");
				BlackNode blackNode = new BlackNode();
				if (temp[1].equals("1")) {
					blackNode.name = temp[0];
					blackNode.leftneighborhood = true;
				}
				if (temp[2].equals("1")) {
					blackNode.name = temp[0];
					blackNode.rightneighborhood = true;
				}
				listblack.add(blackNode); 
				flag = true;
			}
			read.close();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			if (flag) {
				logger.info("blacklist init finish ...");	
				logger.info("blacklist size : " + String.valueOf(listblack.size()));
			
			}else{
				logger.info("blacklist size : " + String.valueOf(listblack.size()));
			}
			
		}		
		}else {
			logger.info("blackdict not exist ...");	
		}
		
		return flag;
	}
	
	/**
	 * 功能：Java读取txt文件的内容 步骤：1：先获得文件句柄 
	 * 2：获得文件句柄当做是输入一个字节码流，需要对这个输入流进行读取
	 * 3：读取到输入流后，需要读取生成字节流 4：一行一行的输出。readline()。 
	 * 备注：需要考虑的是异常情况
	 * 
	 * @param filePath
	 */
	protected  List<String> readTxtFile(String filePath, String encoding) {

		List<String> lineList = new ArrayList<String>();
		try {
			if (filePath != null) {
			 
			File file = new File(filePath);
			if (file.isFile() && file.exists()  ) { // 判断文件是否存在
				InputStreamReader read = new InputStreamReader(
						new FileInputStream(file), encoding);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				int iLineCnt = 0;
				while ((lineTxt = bufferedReader.readLine()) != null) {
					if(lineTxt.length() > 0){
						if(iLineCnt == 0){//UTF-8 BOM
							byte[] byLine = lineTxt.getBytes();
							if (byLine.length > 3
									&& (byLine[0]&0xFF) == 0xEF
									&& (byLine[1]&0xFF) == 0xBB
									&& (byLine[2]&0xFF) == 0xBF
									) {
								byte[] byNewLine = new byte[byLine.length-3];
								System.arraycopy(byLine, 3, byNewLine, 0, byLine.length-3);
								lineTxt = new String(byNewLine);
								
							
							}
						}
						lineList.add(lineTxt);
					}
					iLineCnt ++;
				}
				read.close();
			}
			} else {
				logger.info("MultiLevelNormalize: file not found!");
				//System.out.println("MultiLevelNormalize: file not found!");
			}
		} catch (Exception e) {
			logger.info("MultiLevelNormalize: read file content error!");
			//System.out.println("MultiLevelNormalize: read file content error!");
			e.printStackTrace();
		}
		return lineList;
	}
    
	/***
	 * 进行归一化计算，用trie树扫描文本，获得命中词，并进行优选处理
	 * @param text 输入文本
	 * @param onlyBest 是否进行优选处理，如果进行优选，则只返回一个结果
	 * @return 返回多个归一化后到结果
	 */
	public List<NormalResult> normalize(String text, int levelMax, boolean onlyBest){
	
		List<NormalResult> retList = new ArrayList<NormalResult>();
		txtLength = text.length();
		 for(int i =0;i<text.length();i++){
			 if (text.charAt(i) == '。' || text.charAt(i) == '；' || text.charAt(i) == '？' || text.charAt(i) == '！') {
				 list_endchar.add(i);
			}
		 }

		if(_initOK == false){
			return retList;
		}
		// trie树扫描全部候选词
		Set<MatchPattern<List<Integer>>> setResult = new TreeSet<MatchPattern<List<Integer>>>();
		_nameTrie.match(text, setResult);

		// 对结果排序，消重存在子集包含关系的结果词
		class MatchRes {
			public int iStart = 0;
			public int iEnd = 0;
			public List<Integer> iValue = new ArrayList<Integer>();
		}
		List<MatchRes> listRes = new ArrayList<MatchRes>();
		for (MatchPattern<List<Integer>> p : setResult) {
			MatchRes matchRes = new MatchRes();
			matchRes.iStart = p.offset;
			matchRes.iEnd = p.offset + p.len;
			matchRes.iValue = p.pattern.value;
			neighborDepend(matchRes.iStart,matchRes.iEnd,text);
			if (!neighborDepend(matchRes.iStart,matchRes.iEnd,text)) {
				listRes.add(matchRes);
			}
			
		}
		Collections.sort(listRes, new Comparator<MatchRes>() {
			@Override
			// 将起始位置小，长度大的结果排在前面
			public int compare(MatchRes o1, MatchRes o2) {
				if (o1.iStart > o2.iStart)
					return 1;
				if (o1.iStart < o2.iStart)
					return -1;
				if (o1.iEnd < o2.iEnd)
					return 1;
				if (o1.iEnd > o2.iEnd)
					return -1;
				return 0;
			}
		});

		int iStartPre = -1;
		int iEndPre = -1;
		for (MatchRes matchRes : listRes) {
//			if (matchRes.iStart >= iEndPre
//					|| (matchRes.iStart > iStartPre && matchRes.iEnd > iEndPre)) {
				if (matchRes.iStart >= iEndPre  ) {
				// 仅对不被之前包含的结果进行处理
					//for(Integer num : matchRes.iValue ){
						iStartPre = matchRes.iStart;
						iEndPre = matchRes.iEnd;
						NormalResult normalRes = new NormalResult();
						normalRes.word = text.substring(matchRes.iStart, matchRes.iEnd);
						normalRes.offset = matchRes.iStart;
						normalRes.multiNameIdx = matchRes.iValue;
						retList.add(normalRes);
			//		}
				
			}
		}
		normalizeList(retList, levelMax,onlyBest);
		
		// 合并邻近识别结果归属同一行政区划的地名
		//int normalCnt = MultiLevelNormalize.getInstance().normalize(retList, levelMax);
		Map<Integer, Integer> endToPos = new HashMap<Integer, Integer>();
		List<NormalResult> spaceWordList_new = new ArrayList<NormalResult>();
		for (NormalResult nr : retList)
		{
			NormalResult remove = isChild(nr, spaceWordList_new, endToPos);
			if (remove == null)
			{
				int size = spaceWordList_new.size();
				spaceWordList_new.add(nr);
				endToPos.put(nr.offset + nr.word.length(), size);
			}
			else
			{
				NormalResult oneWord = new NormalResult();
				oneWord.word = remove.word + nr.word;
				oneWord.offset = remove.offset;
				oneWord.oldWordLevel = nr.oldWordLevel;
				oneWord.level = nr.level;
				oneWord.levelIdx = nr.levelIdx;
				oneWord.multiLevel = nr.multiLevel;
				oneWord.multiNameIdx = nr.multiNameIdx;
				oneWord.stdmultiLevelintact = nr.stdmultiLevelintact;
				oneWord.lastword = nr.word;
				oneWord.iscombine = true ;
				int size = spaceWordList_new.size();
				spaceWordList_new.add(oneWord);
				endToPos.put(nr.offset + nr.word.length(), size);
				logger.info("combine process exist ...");
			}
		}	
		
		
	 
		return spaceWordList_new;
	}
	
	protected Boolean neighborDepend(Integer start ,Integer end , String text){
		Boolean flag = false ;
		for(BlackNode bNode : listblack){
			if (bNode.leftneighborhood) {
				if (start-bNode.name.length()>=0) {
					if (text.substring(start-bNode.name.length(),start).equals(bNode.name)) {						 
						flag =  true;
						break;
					}
				}
			}
			if (bNode.rightneighborhood) {
				if (end+bNode.name.length()<=text.length()) {
					if (text.substring(end,end+bNode.name.length()).equals(bNode.name)) {						
						flag =  true;
						break;
					}
				}
			}
			
		}
		return flag;
	}
	
	private NormalResult isChild(NormalResult nr, List<NormalResult> spaceWordList, Map<Integer, Integer> endToPos)
	{
		Integer pos = endToPos.get(nr.offset);
		if (pos == null)
		{
			return null;
		}
		else
		{
			NormalResult normalResult = spaceWordList.get(pos);
			if (nr.stdmultiLevelintact == null)
			{
				return null;
			}
			else if (normalResult.stdmultiLevelintact == null)
			{
				return null;
			}
			else if (!nr.stdmultiLevelintact.startsWith(normalResult.stdmultiLevelintact))
			{
				return null;
			}
			else
			{
				endToPos.remove(nr.offset);
				return spaceWordList.remove(pos.intValue());
			}
		}
	}


	/***
	 * 进行归一化计算，输入已查询到中间词到结果，并进行优选处理
	 * @param wordList 输入文本
	 * @param onlyBest 是否进行优选处理，如果进行优选，则只返回一个结果
	 * @return 返回多个归一化后到结果
	 */
	protected int normalizeList(List<NormalResult> wordList, int levelMax, boolean onlyBest){
		//先做基础统计，统计每一个标准层级路径出现到次数
		Map<Integer,Integer> upLevelCntMap = new HashMap<Integer,Integer>();
		
		boolean flag = false;   //标记在一个地名结果中上级行政区划是否已经参加统计
		for(NormalResult normalRes:wordList){
			if(normalRes.multiNameIdx.size() <= 0){
				continue;
			}
			Map<Integer,Integer> subupLevelCntMap = new HashMap<Integer,Integer>();
			List<Integer> nameIdxs =normalRes.multiNameIdx;
			
			for(Integer levelIdx:nameIdxs){
				LevelNode levelNode = _listLevelNodes.get(levelIdx);				
				//先添加当前节点id的计数
				int cnt = 1;
				if(subupLevelCntMap.containsKey(levelNode.idx)){
					cnt = subupLevelCntMap.get(levelNode.idx).intValue();				
					//cnt += 1;
	
				}
				subupLevelCntMap.put(levelNode.idx, cnt);
				//再添加上级节点id的计数
				for(Integer levelId:levelNode.listUpLevel){
					cnt = 1;
					if(subupLevelCntMap.containsKey(levelId)){
						cnt = subupLevelCntMap.get(levelId).intValue();
						//cnt += 1;
					}
					subupLevelCntMap.put(levelId, cnt);
				}
			}
			for(Map.Entry<Integer,Integer> entry : subupLevelCntMap.entrySet() ){
				if (upLevelCntMap.containsKey(entry.getKey())) {
					upLevelCntMap.put(entry.getKey(), upLevelCntMap.get(entry.getKey())+1);
					
				}else{
					upLevelCntMap.put(entry.getKey(), entry.getValue());
				}
			}
		
		}
		
		int getUpLevelCnt = 0;
		//根据上述统计，对于一个名称指向多个路径到词，优选出现最多到层级级别idx
		 
		for(NormalResult normalRes:wordList){
			if(normalRes.multiNameIdx.size() <= 0){
				continue;
			}
			List<Integer> nameIdxs = normalRes.multiNameIdx;			
			if(nameIdxs.size() > 0){
				int bestNameIdx = nameIdxs.get(0).intValue();
				LevelNode bestLevelNode = _listLevelNodes.get(bestNameIdx);
				for(int i=1;i<nameIdxs.size();i++){
					int nextNameIdx = nameIdxs.get(i);
					LevelNode nextLevelNode = _listLevelNodes.get(nextNameIdx);
					if(isBetterNode(bestLevelNode,nextLevelNode,upLevelCntMap)){
					 
						bestNameIdx = nextNameIdx;
						bestLevelNode = nextLevelNode;
					}
				}
				normalRes.levelIdx = bestNameIdx;
				normalRes.level = bestLevelNode.level;
				
				//生成level层级名称字符串
				if(normalRes.level <= levelMax  ){
					if (normalRes.level == 1){
						normalRes.multiLevel = bestLevelNode.stdName;
						normalRes.stdmultiLevelintact  = bestLevelNode.stdName;
					}else{
						if (bestLevelNode.upLevelNames.equals("null")) {
							normalRes.multiLevel = "null";
							normalRes.stdmultiLevelintact  = "null";
						}else{
//							if (bestLevelNode.areaId.equals("0")) {
//								normalRes.multiLevel = bestLevelNode.upLevelNames;
//								normalRes.stdmultiLevelintact  = bestLevelNode.upLevelNames;
//							}else{
								normalRes.multiLevel = bestLevelNode.upLevelNames+"-"+bestLevelNode.stdName;
								normalRes.stdmultiLevelintact  = bestLevelNode.upLevelNames+"-"+bestLevelNode.stdName;
//							}
							
						}
						
					}
				}else{//需要根据指定层级进行限制输出
					if (normalRes.level>0) {
						StringBuffer sbMultiLevel = new StringBuffer();
						StringBuffer sbMultiLevelintact = new StringBuffer();
						sbMultiLevel.append("中国");
						sbMultiLevelintact.append("中国");
						for(Integer upLevelIdx: bestLevelNode.listUpLevel){
							LevelNode upLevel = _listLevelNodes.get(upLevelIdx.intValue());
							if(upLevel.level <= levelMax){
								if(sbMultiLevel.length() > 0)
									sbMultiLevel.append("-");
								sbMultiLevel.append(upLevel.stdName);
							}
							if(sbMultiLevelintact.length() > 0)
								sbMultiLevelintact.append("-");
							sbMultiLevelintact.append(upLevel.stdName);
						}
						normalRes.multiLevel = sbMultiLevel.toString();
						normalRes.stdmultiLevelintact = sbMultiLevelintact.toString()+"-"+bestLevelNode.stdName;
					}else{

						StringBuffer sbMultiLevel = new StringBuffer();						 
						for(Integer upLevelIdx: bestLevelNode.listUpLevel){
							LevelNode upLevel = _listLevelNodes.get(upLevelIdx.intValue());
							if(upLevel.level <= levelMax){
								if(sbMultiLevel.length() > 0)
									sbMultiLevel.append("-");
								sbMultiLevel.append(upLevel.stdName);
							}
						}
						normalRes.multiLevel = sbMultiLevel.toString();
					
					}					
				}
			}
		}
		
		//是否优选，仅输出一个最好结果
		if(onlyBest){
			//仅将各级出现最多，且级别最小到一个保留
			if(wordList.size() > 0){
				NormalResult bestResult = wordList.get(0);
				for(int i=1;i<wordList.size();i++){
					NormalResult nextResult = wordList.get(i);
					if(isBetterResult(bestResult,nextResult,upLevelCntMap)){
						bestResult = nextResult;
					}
				}
				wordList.clear();
				wordList.add(bestResult);
			}
		}
		
//		//新增逻辑，如果识别出来的地名，字典里没有配，则会根据识别地名做自动匹配
//		int wordListSize = wordList.size();
//		for(int i = 0; i < wordListSize; i++)
//		{
//			NormalResult normalRes = wordList.get(i);
//			//五级地名，无映射路径，根据上下文映射解决；
//			if(normalRes.multiNameIdx < 0 && normalRes.oldWordLevel == 5)
//			{
//				//词典里没有配
//				//新增逻辑，如果是根据上下文自动匹配的话，需要在一句话内才做自动匹配
//				//先在左边找
//				boolean findFlag = false;
//				if(i > 0)
//				{
//					NormalResult normalResLeft = wordList.get(i - 1);
//					if(normalResLeft.level != -1 && normalResLeft.level < normalRes.oldWordLevel)
//					{
//						findFlag = true;
//						if(normalRes.level != -1)
//						{
//							normalRes.level = normalRes.oldWordLevel;
//						}
//							
//						normalRes.multiLevel = normalResLeft.multiLevel + "-" + normalRes.word;
//					}
//				}
//				
//				if(!findFlag && i != wordListSize - 1)
//				{
//					//向右看
//					NormalResult normalResRight = wordList.get(i + 1);
//					if(normalResRight.level != -1 && normalResRight.level < normalRes.oldWordLevel)
//					{
//						findFlag = true;
//						if(normalRes.level != -1)
//						{
//							normalRes.level = normalRes.oldWordLevel;	
//						}
//
//						normalRes.multiLevel = normalResRight.multiLevel + "-" + normalRes.word;
//					}
//				}
//			}
//		}
		return getUpLevelCnt;
	}
	
	//最终结果优选，仅将各级出现最多，且级别最小到一个保留
	private boolean isBetterResult(NormalResult bestResult,
			NormalResult nextResult, Map<Integer, Integer> upLevelCntMap) {
		List<Integer> bestLevelIdx = _listLevelNodes.get(bestResult.levelIdx).listUpLevel;
		List<Integer> nextLevelIdx = _listLevelNodes.get(nextResult.levelIdx).listUpLevel;
		int cmpSize = Math.min(bestLevelIdx.size(), nextLevelIdx.size());
		for(int i=0;i<cmpSize;i++){
			int bestCnt = upLevelCntMap.get(bestLevelIdx.get(i)).intValue();
			int nextCnt = upLevelCntMap.get(nextLevelIdx.get(i)).intValue();
			if(bestCnt > nextCnt)
				return false;
			if(bestCnt < nextCnt)
				return true;
		}
		if(nextLevelIdx.size() > bestLevelIdx.size())
			return true;
		return false;
	}

	//根据统计结果，比较两个候选路径，谁更好一些
	//比较标准： 那个候选路径出现到次数多，就更好，不过比较到顺序所从最低级往上比，否则保持不变
	private boolean isBetterNode(LevelNode bestLevelNode,
			LevelNode nextLevelNode, Map<Integer, Integer> upLevelCntMap) {
		int bestCnt = upLevelCntMap.get(bestLevelNode.idx).intValue();
		int nextCnt = upLevelCntMap.get(nextLevelNode.idx).intValue();
		if(bestCnt > nextCnt)
			return false;
		if(bestCnt < nextCnt)
			return true;
		
		List<Integer> bestLevelIdx = bestLevelNode.listUpLevel;
		List<Integer> nextLevelIdx = nextLevelNode.listUpLevel;
		int bestSize = bestLevelIdx.size();
		int nextSize = nextLevelIdx.size();
		int cmpCnt = 1;
		for(;cmpCnt <= bestSize && cmpCnt <= nextSize;cmpCnt++){
			bestCnt = upLevelCntMap.get(bestLevelIdx.get(bestSize-cmpCnt)).intValue();
			nextCnt = upLevelCntMap.get(nextLevelIdx.get(nextSize-cmpCnt)).intValue();
			if(bestCnt > nextCnt)
				return false;
			if(bestCnt < nextCnt)
				return true;
		}
		//if(nextLevelIdx.size() < bestLevelIdx.size())  //疑似错误
		if(nextLevelIdx.size() > bestLevelIdx.size())
			return true;
		return false;
	}

	/***
	 * 进行归一化计算，直接输入待查询词，并进行优选处理
	 * @param text 输入文本
	 * @param onlyBest 是否进行优选处理，如果进行优选，则只返回一个结果
	 * @return 返回多个归一化后到结果
	 */
//	public List<NormalResult> normalize(List<String> wordList, int levelMax, boolean onlyBest){
//		List<NormalResult> retList = new ArrayList<NormalResult>();
//		if(_initOK == false){
//			return retList;
//		}
//		for(String word:wordList){
//			if(_multiNameMap.containsKey(word)){				
//				NormalResult normalRes = new NormalResult();
//				normalRes.word = word;
//				normalRes.offset = -1;
//				normalRes.multiNameIdx = _multiNameMap.get(word).intValue();
//				retList.add(normalRes);
//			}
//		}
//		normalizeList(retList, levelMax, onlyBest);
//		return retList;
//	}
	
	/***
	 * 进行归一化计算，直接输入待查询词，并进行优选处理
	 * @param text 输入文本
	 * @param onlyBest 是否进行优选处理，如果进行优选，则只返回一个结果
	 * @return 返回多个归一化后到结果
	 */
	public int normalize(List<NormalResult> wordList, int levelMax){
		if(_initOK == false){
			return 0;
		}
//		for(NormalResult word:wordList){
//			if(_multiNameMap.containsKey(word.word)){				
//				word.multiNameIdx = _multiNameMap.get(word.word).intValue();
//			}
//		}
		return normalizeList(wordList, levelMax, false);
	}
	
	public List<Poi> standardizedResult(List<NormalResult>  NormalResultlist ){
		List<Poi> result = new ArrayList<Poi>();
		int count = 0;
		for(NormalResult ner : NormalResultlist){
			Poi poi = new Poi();
			poi.setName(ner.word);
			poi.setStartPos(ner.offset);
			poi.setMapLevel(_listLevelNodes.get(ner.levelIdx).level);
			if (_listLevelNodes.get(ner.levelIdx).level<=2) {
				if (_listLevelNodes.get(ner.levelIdx).level==1) {
					poi.setMapString(ner.word);
				}else {
					poi.setMapString(ner.multiLevel);
				}
				
			}else{
				if (ner.lastword == null) {	 
					poi.setMapString(_listLevelNodes.get(ner.levelIdx).upLevelNames+"-"+_listLevelNodes.get(ner.levelIdx).stdName);

				}else{
					poi.setMapString(_listLevelNodes.get(ner.levelIdx).upLevelNames+"-"+_listLevelNodes.get(ner.levelIdx).stdName);		
				}		
			}
		
			poi.setAreaID(_listLevelNodes.get(ner.levelIdx).areaId);
			Map<String, String>  typeMap = new HashMap<String ,String>();
			if (_listLevelNodes.get(ner.levelIdx).coordinates_baidu != null) {
				typeMap.put("coordinates_baidu", _listLevelNodes.get(ner.levelIdx).coordinates_baidu);
				typeMap.put("coordinates_gaode", _listLevelNodes.get(ner.levelIdx).coordinates_gaode);
				typeMap.put("areaId", _listLevelNodes.get(ner.levelIdx).areaId);
			}else{
				//typeMap.put("coordinates_baidu", "null");
				typeMap.put("coordinates_gaode", _listLevelNodes.get(ner.levelIdx).coordinates_gaode);
				typeMap.put("areaId", _listLevelNodes.get(ner.levelIdx).areaId);
			}
			poi.setTypeMap(typeMap);
			poi.setType(_listLevelNodes.get(ner.levelIdx).type);			 
			poi.setIscombinepoi(ner.iscombine);
			 
			result.add(poi);
			count ++;
		}
 
		result = rectifyMapstring(result);   //重新根据邻近行政地名信息修改非行政地名mapstring
		return result;
	}
	
	public List<Poi> standardizedResult(List<NormalResult>  NormalResultlist,Poi baseking ){
		List<Poi> result = new ArrayList<Poi>();
		int count = 0;
		for(NormalResult ner : NormalResultlist){
			Poi poi = new Poi();
			poi.setName(ner.word);
			poi.setStartPos(ner.offset);
			poi.setMapLevel(_listLevelNodes.get(ner.levelIdx).level);
			if (_listLevelNodes.get(ner.levelIdx).level<=2) {
				if (_listLevelNodes.get(ner.levelIdx).level==1) {
					poi.setMapString(ner.word);
				}else {
					poi.setMapString(ner.multiLevel);
				}
				
			}else{
				if (ner.lastword == null) {	 
					poi.setMapString(_listLevelNodes.get(ner.levelIdx).upLevelNames+"-"+_listLevelNodes.get(ner.levelIdx).stdName);

				}else{
					poi.setMapString(_listLevelNodes.get(ner.levelIdx).upLevelNames+"-"+_listLevelNodes.get(ner.levelIdx).stdName);		
				}		
			}
		
			poi.setAreaID(_listLevelNodes.get(ner.levelIdx).areaId);
			Map<String, String>  typeMap = new HashMap<String ,String>();
			if (_listLevelNodes.get(ner.levelIdx).coordinates_baidu != null) {
				typeMap.put("coordinates_baidu", _listLevelNodes.get(ner.levelIdx).coordinates_baidu);
				typeMap.put("coordinates_gaode", _listLevelNodes.get(ner.levelIdx).coordinates_gaode);
				typeMap.put("areaId", _listLevelNodes.get(ner.levelIdx).areaId);
			}else{
				//typeMap.put("coordinates_baidu", "null");
				typeMap.put("coordinates_gaode", _listLevelNodes.get(ner.levelIdx).coordinates_gaode);
				typeMap.put("areaId", _listLevelNodes.get(ner.levelIdx).areaId);
			}
			poi.setTypeMap(typeMap);
			poi.setType(_listLevelNodes.get(ner.levelIdx).type);			 
			poi.setIscombinepoi(ner.iscombine);
			 
			result.add(poi);
			count ++;
		}
 
		result = rectifyMapstring(result , baseking);   //重新根据邻近行政地名信息修改非行政地名mapstring
		return result;
	}
	
	public List<Poi> rectifyMapstring(List<Poi> poilist ,Poi baseking){
		List<Poi> result = new ArrayList<Poi>();		
		for(int i = 0 ; i < poilist.size();i++){
			if (poilist.get(i).getMapLevel() < 0) {
				if (!poilist.get(i).getAreaID().equals("0")) {
					if (poilist.get(i).getType().equals("地名") 
							|| poilist.get(i).getType().equals("村庄名")
							|| poilist.get(i).getType().equals("商业")
							|| poilist.get(i).getType().equals("医院")
							|| poilist.get(i).getType().equals("通用")
							) {
						if (poilist.get(i).isIscombinepoi() == false) {
							boolean flag = false ;
							for(int j = i ; j >= 0 ; j--){   //前向
								if (poilist.get(j).getMapLevel() > 0) {
									poilist.get(i).setAreaID(poilist.get(j).getAreaID());
									poilist.get(i).setMapString(poilist.get(j).getMapString()+"-"+poilist.get(i).getName());
									poilist.get(i).setTypeMap(poilist.get(j).getTypeMap());
									poilist.get(i).setIsrectifyMapstring(true);
									result.add(poilist.get(i));
									flag = true ;
									break ;
								}else if (poilist.get(j).isIscombinepoi() == true) {
									poilist.get(i).setAreaID(poilist.get(j).getAreaID());									
									poilist.get(i).setMapString(poilist.get(j).getMapString().substring(0, poilist.get(j).getMapString().lastIndexOf("-"))+"-"+poilist.get(i).getName());

									String temp = poilist.get(j).getMapString().substring(0, poilist.get(j).getMapString().lastIndexOf("-")).replaceAll("-", "");
									poilist.get(i).setTypeMap(standardizedResult(normalize(temp,2, false)).get(0).getTypeMap());
									
									poilist.get(i).setIsrectifyMapstring(true);
									result.add(poilist.get(i));
									flag = true ;
									break ;
								}
							}
							if (!flag) {
								for(int j = i ; j < poilist.size() ; j++ ){   //后向
									if (poilist.get(j).getMapLevel() > 0) {
										poilist.get(i).setAreaID(poilist.get(j).getAreaID());
										poilist.get(i).setMapString(poilist.get(j).getMapString()+"-"+poilist.get(i).getName());
										poilist.get(i).setTypeMap(poilist.get(j).getTypeMap());
										poilist.get(i).setIsrectifyMapstring(true);
										result.add(poilist.get(i));
										flag = true ;
										break ;
									}else if (poilist.get(j).isIscombinepoi() == true) {
										poilist.get(i).setAreaID(poilist.get(j).getAreaID());										
										poilist.get(i).setMapString(poilist.get(j).getMapString().substring(0, poilist.get(j).getMapString().lastIndexOf("-"))+"-"+poilist.get(i).getName());

										String temp = poilist.get(j).getMapString().substring(0, poilist.get(j).getMapString().lastIndexOf("-")).replaceAll("-", "");
										poilist.get(i).setTypeMap(standardizedResult(normalize(temp,2, false)).get(0).getTypeMap());
										
										poilist.get(i).setIsrectifyMapstring(true);
										result.add(poilist.get(i));
										flag = true ;
										break ;
									}						
								}
							}
							if (!flag) {
								if (poilist.get(i).getMapString().substring(0, poilist.get(i).getMapString().lastIndexOf("-")).equals(baseking.getMapString())) {
									poilist.get(i).setIsrectifyMapstring(false);
									result.add(poilist.get(i));
								}else{
									poilist.get(i).setAreaID(baseking.getAreaID());
									poilist.get(i).setMapString(baseking.getMapString()+"-"+poilist.get(i).getName());
									poilist.get(i).setTypeMap(baseking.getTypeMap());
									poilist.get(i).setIsrectifyMapstring(true);
									result.add(poilist.get(i));
								}
								
							}
						}else {
								poilist.get(i).setIsrectifyMapstring(false);
								result.add(poilist.get(i));
							
						}
					}else{
						poilist.get(i).setIsrectifyMapstring(false);
						result.add(poilist.get(i));}
					
				}else{

					poilist.get(i).setIsrectifyMapstring(false);
					result.add(poilist.get(i));
				}
				
			}else{

				poilist.get(i).setIsrectifyMapstring(false);
				result.add(poilist.get(i));
			}
			
		}
		
		return result ;
	}
	
	
	public List<Poi> rectifyMapstring(List<Poi> poilist){
		List<Poi> result = new ArrayList<Poi>();		
		for(int i = 0 ; i < poilist.size();i++){
			if (poilist.get(i).getMapLevel() < 0) {
				if (!poilist.get(i).getAreaID().equals("0")) {
					if (poilist.get(i).getType().equals("地名") 
							|| poilist.get(i).getType().equals("村庄名")
							|| poilist.get(i).getType().equals("商业")
							|| poilist.get(i).getType().equals("医院")
							|| poilist.get(i).getType().equals("通用")
							) {
						if (poilist.get(i).isIscombinepoi() == false) {
							boolean flag = false ;
							for(int j = i ; j >= 0 ; j--){
								if (poilist.get(j).getMapLevel() > 0) {
									poilist.get(i).setAreaID(poilist.get(j).getAreaID());
									poilist.get(i).setMapString(poilist.get(j).getMapString()+"-"+poilist.get(i).getName());
									poilist.get(i).setTypeMap(poilist.get(j).getTypeMap());
									poilist.get(i).setIsrectifyMapstring(true);
									result.add(poilist.get(i));
									flag = true ;
									break ;
								}else if (poilist.get(j).isIscombinepoi() == true) {
									poilist.get(i).setAreaID(poilist.get(j).getAreaID());									
									poilist.get(i).setMapString(poilist.get(j).getMapString().substring(0, poilist.get(j).getMapString().lastIndexOf("-"))+"-"+poilist.get(i).getName());
									
									//String temp = poilist.get(j).getName().substring(0, poilist.get(j).getMapString().length() - poilist.get(j).getMapString().lastIndexOf("-")-1);
									String temp = poilist.get(j).getMapString().substring(0, poilist.get(j).getMapString().lastIndexOf("-")).replaceAll("-", "");
//									System.out.println(poilist.get(j).getMapString().length());
//									System.out.println(poilist.get(j).getMapString().lastIndexOf("-"));
									poilist.get(i).setTypeMap(standardizedResult(normalize(temp,2, false)).get(0).getTypeMap());
									
									poilist.get(i).setIsrectifyMapstring(true);
									result.add(poilist.get(i));
									flag = true ;
									break ;
								}
							}
							if (!flag) {
								for(int j = i ; j < poilist.size() ; j++ ){
									if (poilist.get(j).getMapLevel() > 0) {
										poilist.get(i).setAreaID(poilist.get(j).getAreaID());
										poilist.get(i).setMapString(poilist.get(j).getMapString()+"-"+poilist.get(i).getName());
										poilist.get(i).setTypeMap(poilist.get(j).getTypeMap());
										poilist.get(i).setIsrectifyMapstring(true);
										result.add(poilist.get(i));
										flag = true ;
										break ;
									}else if (poilist.get(j).isIscombinepoi() == true) {
										poilist.get(i).setAreaID(poilist.get(j).getAreaID());										
										poilist.get(i).setMapString(poilist.get(j).getMapString().substring(0, poilist.get(j).getMapString().lastIndexOf("-"))+"-"+poilist.get(i).getName());
										
										//String temp = poilist.get(j).getName().substring(0, poilist.get(j).getMapString().length() - poilist.get(j).getMapString().lastIndexOf("-") - 1);
										String temp = poilist.get(j).getMapString().substring(0, poilist.get(j).getMapString().lastIndexOf("-")).replaceAll("-", "");
										poilist.get(i).setTypeMap(standardizedResult(normalize(temp,2, false)).get(0).getTypeMap());
										
										poilist.get(i).setIsrectifyMapstring(true);
										result.add(poilist.get(i));
										flag = true ;
										break ;
									}						
								}
							}
							if (!flag) {
								poilist.get(i).setIsrectifyMapstring(false);
								result.add(poilist.get(i));
							}
						}else {
							poilist.get(i).setIsrectifyMapstring(false);
							result.add(poilist.get(i));
						}
					}else{
						poilist.get(i).setIsrectifyMapstring(false);
						result.add(poilist.get(i));
					}
					
				}else{
					poilist.get(i).setIsrectifyMapstring(false);
					result.add(poilist.get(i));
				}
				
			}else{
				poilist.get(i).setIsrectifyMapstring(false);
				result.add(poilist.get(i));
			}
			
		}
		
		return result ;
	}
	
	
	private String fullMapString(int count ,List<Poi> result,Integer txtLength){
		String mapString = "";
 
		int begin =0;
		int end =0;
		if (list_endchar.isEmpty()) {
			list_endchar.add(0);
			list_endchar.add(txtLength);
		}else{
			if (list_endchar.get(0)!=0) {
				list_endchar.add(0, 0);
			}
			if (list_endchar.get(list_endchar.size()-1) != txtLength) {
				list_endchar.add(txtLength);
			}
		}
		
		for(int j = 0; j< list_endchar.size()-1;j++){			 
				begin = list_endchar.get(j);
				end = list_endchar.get(j+1);
				if (count==0) {
					for(int i = count;i< result.size();i++){
						if (!result.get(i).getMapString().equals("null")) {
							if ((result.get(i).getStartPos()>begin && result.get(i).getStartPos()<end)
									&& result.get(count).getStartPos()>begin && result.get(count).getStartPos()<end
									) {
								mapString = result.get(i).getMapString()+"-"+result.get(count).getName() ;					
								break;
							}
							
						}
					}
				}else if (count==result.size()-1) {
					for(int i = count;i>=0;i--){
						if (!result.get(i).getMapString().equals("null")) {
							if ((result.get(i).getStartPos()>begin && result.get(i).getStartPos()<end)
									&& result.get(count).getStartPos()>begin && result.get(count).getStartPos()<end
									) {
							mapString = result.get(i).getMapString()+"-"+result.get(count).getName() ;					
							break;
							}
						}
					}
				}
				else {
					for(int i = count;i>=0;i--){
						if (!result.get(i).getMapString().equals("null")) {
							if ((result.get(i).getStartPos()>=begin && result.get(i).getStartPos()<=end)
									&& result.get(count).getStartPos()>=begin && result.get(count).getStartPos()<=end
									) {
							mapString = result.get(i).getMapString()+"-"+result.get(count).getName() ;					
							break;
							}
						}
					}
					if (mapString.equals("")) {
						for(int i = count;i< result.size();i++){
							if (!result.get(i).getMapString().equals("null")) {
								if ((result.get(i).getStartPos()>=begin && result.get(i).getStartPos()<=end)
										&& result.get(count).getStartPos()>=begin && result.get(count).getStartPos()<=end
										) {
								mapString = result.get(i).getMapString()+"-"+result.get(count).getName() ;					
								break;
								}
							}
						}
					}
					
				}	
		}
		
		if (mapString.equals("")) {
			mapString =	result.get(count).getName();
		}
		return mapString;
		
	}
	/*
	 * 计算文章最相关地名
	 * */
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
				String key = temp[0] + "-" + temp[1] + "-" + temp[2] + "-" + temp[3] + "-" + temp[4];
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
	
	public Poi baseLine(List<Poi> result){
		 List<Poi> bases = new ArrayList<Poi>();
		 Map<String, Integer> 	administrationmap = new HashMap<String ,Integer>();
		 for (Poi poi : result) {
			if (poi.getMapLevel() > 0) {
				bases.add(poi);
				String[] temp = poi.getMapString().split("-");
				for(String str : temp){
					if (administrationmap.containsKey(str)) {
						administrationmap.put(str, administrationmap.get(str)+1);
					}else{
						administrationmap.put(str, 1);
					}
				}
			}
		}
		
		int currentscore = 0 ;
		Poi baseking = new Poi();
		for(Poi poi : bases){
			String[] temp =  poi.getMapString().split("-");
			int score = 0 ; 
			for(String str : temp){
				if (administrationmap.containsKey(str)) {
					score += 1 ; 
				} 
			} 
			if (score > currentscore) {
				baseking = poi; 
			}
			
		
		}
		return baseking ; 
	}
}
