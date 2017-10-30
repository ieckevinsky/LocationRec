package com.hylanda.loc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.IconifyAction;

import com.hylanda.segment.HLDictManager;
import com.hylanda.segment.HLSwknl;
import com.hylanda.segment.SegmentResult;
import com.hylanda.tool.Locinfo;
import com.hylanda.tool.Poi;

import sun.print.resources.serviceui;

public class TestNewLocRec {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CombineLocRec CombineLocRec = com.hylanda.loc.CombineLocRec.getInstance() ;
		try {
			String regin = "dict/regindict.txt";
			String hotpoint = "dict/hotpointdict.txt";
			String foreign = "dict/foreigndict.txt";
			String place = "dict/placedict.txt";
			String black = "dict/blackdict.txt";
			String type = "dict/regindict.txt";
			String diff = "dict/regindict.txt";
			String direct = "dict/regindict.txt";
			String part = "dict/regindict.txt";
			String model = "dict/regindict.txt";
			String suffix = "dict/regindict.txt";
			Map<String, String > params = new HashMap<String,String>();
			params.put("regin", "dict/regindict.txt");
			params.put("hotpoint", "dict/hotpointdict.txt");
			params.put("foreign", "dict/foreigndict.txt");
			params.put("place", "dict/placedict.txt");
			params.put("black", "dict/blackdict.txt");
			params.put("type", "dict/类型语素.txt");
			params.put("diff", "dict/区别性语素.txt");
			params.put("direct", "dict/方位语素.txt");
			params.put("event", "/root/Desktop/event.txt");
			params.put("part", "dict/部位语素.txt");
			params.put("model","/usr/java/Java_workspace/CRFLoc/model");
			params.put("suffix", "dict/地名后缀.txt");

			CombineLocRec.init(params);		
			System.out.println("init finish ...");
			String Text = "　　桂林生活网讯(通讯员朱晶晶 蒋昊锦 林木昌)近日，县政府组织县工商质监局、县环保局、县食药监局、县市容局、县畜牧局等多部门开展漓江风景名胜区大圩段“四乱一脏”调查统计工作。 　　本次调查主要对毛洲村委、大圩村委、大圩社区、嵅村村委、秦岸村委、茯荔村委范围内“四乱一脏”的调查统计，建立和完善本辖区“四乱一脏” 情况台账和档案，形成“一帐一档”。县食药局联合县工商质监局、市容局负责“四乱一脏”中“乱经营”调查统计工作，主要对在上述范围内进行非法经营摊点摊位、鱼餐馆、船餐馆等进行调查登记。 　　此次调查进一步摸清了漓江风景名胜区大圩段内“四乱一脏”的现状，掌握了各类违法违规行为的底数，推动了漓江风景名胜区大圩段各部门综合执法，探索了一套行之有效的漓江风景名胜区综合行政执法机制，进一步夯实了漓江风景名胜区大圩段行政执法基础。 　　本报讯(记者黄柏秀 文/摄)10月18日上午9时，举国关注、举世瞩目的中国共产党第十九次全国代表大会在北京隆重开幕，我县党政机关干部、社会各界群众通过电视、网络、广播等不同方式收听收看了十九大开幕式盛况，认真聆听习近平代表十八届中央委员会所作的工作报告，广大干部群众倍感振奋，深受鼓舞。大家表示，一定要认真学习、深刻领会报告精神内涵，用习近平总书记的报告精神武装头脑，指导各项工作，在各自的工作岗位上创造新业绩。 　　上午9时，县委委员、县四家班子领导在县行政办公大楼二楼党政视频会议室集中收看了党的十九大开幕式盛况，认真聆听了习近平总书记在中国共产党第十九次全国代表大会上的报告，大家边看边听边作记录。中午12时30分，开幕式一结束，县委委员、四家班子领导立即在行政大楼二楼五会议室举行学习习近平总书记在中国共产党第十九次全国代表大会上的报告座谈会，进行学习讨论。县委书记赵奇玲、县长周春涌、县人大常委会主任唐火祯、县政协主席赵国平代表县委、政府、人大、政协一一发言表态。大家在发言中一致认为，习近平总书记的报告气势恢宏、意蕴深厚，使人警醒，催人奋进!这个报告使我们对我国过去五年的辉煌成就有了更清楚的认识，也为此感到骄傲和自豪，报告对全党在新的历史起点做好党和国家各项工作，指明了前进方向，明确了行动指南，为决胜全面小康、实现中国梦凝聚起必胜信心和磅礴力量，充分展现了共产党人的本色初衷、雄心壮志和使命担当。大家表示，作为党员领导干部，在今后的工作中，一定身体力行、率先垂范，认真研读报告，带头学习和领会掌握习近平总书记在中国共产党第十九次全国代表大会上的报告的精神实质，坚定信心，结合实际，立足本职工作，更好地服从服务于改革发展大局，促进经济社会又好又快发展。 　　赵奇玲在学习座谈会上说，党的十九大是在我国进入全面建成小康社会决定性阶段召开的一次十分重要的会议，这是一次高举旗帜、继往开来、团结奋进的大会。习总书记代表十八届中央委员会作的报告，主题鲜明、内涵丰富、思想深刻、博大精深，是新时期、新阶段中国共产党的政治宣言，是夺取全面建成小康社会新胜利的行动纲领，是我们今后的工作指南和行为准则。认真学习宣传贯彻习总书记的讲话精神，关系党和国家工作全局，关系中国特色社会主义事业长远发展，对于动员全党全国各族人民在以习近平同志为核心的党中央领导下，高举中国特色社会主义伟大旗帜，满怀信心为全面建成小康社会、夺取中国特色社会主义新胜利而奋斗，具有重大现实意义和深远历史意义。我们一定要认真学习、深刻领会、全面把握，切实将思想、认识和行动统一到党的十九大精神上来，把力量凝聚到实现党的十九大确定的各项任务上来，为实现党的十九大确定的奋斗目标和工作任务而奋斗。赵奇玲表示，作为县委的主要领导，会坚决拥护习总书记所做报告的部署和要求，进一步深刻体会报告的精神内涵，进一步消化报告的内容，变为指引灵川加快发展的行动指南，切实用总书记报告精神武装头脑。赵奇玲强调，全县各级各部门要认真学习习近平总书记的报告精神;要迅速掀起学习宣传贯彻热潮;要迅速形成强大宣传声势;要坚持学以致用推动发展。 ▲县委委员、县四家班子领导在县行政办公大楼二楼党政视频会议室集中收看党的十九大开幕式。 　　本报讯(记者黄柏秀 文欢 莫保义 郭丽洁)“现在，我宣布，桂林天海塑业有限公司圆织成套生产线智能化升级扩能项目开工!”10月16日上午9：30，在桂林天海塑业有限公司圆织成套生产线智能化升级扩能项目开工现场，县委书记赵奇玲宏亮的声音响起，接着，台上台下掌声如雷，礼炮齐放，数百名领导群众喜笑颜开，共同庆祝这历史性的一刻。 　　为迎接中国共产党第十九次全国代表大会胜利召开，我县隆重举行10月份新项目集中开工仪式。位于我县境内的桂林天海塑业有限公司圆织成套生产线智能化升级扩能项目、广西立大节能玻璃有限公司高新技术遮阳节能玻璃及节能门窗生产项目、灵青东路项目、潭下镇社会福利院4大项目同天分别在各项目地举行开工仪式，项目总投资4.69亿元。赵奇玲、周春涌、唐火祯、赵国平等县四家班子领导分别参加了4个项目的开工仪式。 　　据悉，桂林天海塑业有限公司落户灵川已有12年，其规模和效益不断发展，产值从600多万元提高到7000多万元;税收由当年的20多万元提高到现在的每年400多万元，工人由30多人扩展到300多人，各项经营指标都翻了10倍以上。公司沿着供给侧改革的发展潮流，进一步提高产品品质，积极实施走出去的发展思路，努力开拓国外市场。正在实施的“圆织成套生产线智能化升级扩能项目”项目总投资1.11亿元，占地42.99亩，采用德国、奥地利等国先进的设备，项目分两期建设完成，主要建设标准厂房、办公楼、宿舍楼、智能化生产线及相关配套设施，建成后年总产值可达1.29亿元，年创利税1000多万元，新增就业人员200余人。 　　在八里街工业园区机械装备制造产业园举行开工庆典仪式的广西立大节能玻璃幕墙有限责任公司高新技术遮阳节能玻璃及节能幕墙门窗生产项目占地53亩，计划总投资1.08亿元，项目建成后，可实现年产值近2亿元，年上缴税收500万元以上，可促进就业400人以上。该公司主要从事节能幕墙产品制作安装、钢化(弯钢玻璃)、白玻中空玻璃、阳光镀膜中空玻璃等，企业已通过IS9001国际质量体系认证和安全玻璃中国国家强制性(CCC)产品认证，为国内多家大型企业进行配套服务，深受好评。 　　位于我县灵川镇的桂林独秀水泥总厂棚户区危旧房改造片区配套基础设施(灵青东路)项目总投资1.9亿元，建设年限2017年至2018年。工程全长1.725公里，线路走向大致为东西走向，起点接桂黄路与灵青路平交口，作为灵青路的延长线，终点与滨江北路相交。道路红线宽度为40米，设计行车时速为40公里/小时，双向四车道，采用沥青混凝土路面。主要建设内容为道路工程、桥涵工程、排水工程、照明工程、交通工程、绿化工程、电力工程、给水工程等。项目建成后，可以进一步完善县城道路路网，同时也为桂林高铁经济产业园和新区市政道路建设提供便利的交通条件，减轻桂黄公路交通压力，同时将进一步提升我县县域经济发展水平。 　　位于潭下镇潭下敬老院内的灵川县社会福利综合服务中心(一期)老年养护楼项目工程于2016年立项，2016年列为中央预算内投资项目，占地面积32亩，总建筑面积12400平方米，总投资约2999.98万元。主要建设老年养护楼(包含老年养护楼、五保楼、后勤楼、连廊等工程)。配套建设给排水、消防、电气、道路、绿化等附属工程及设备采购。建成后可容200多张床位，是一所集养生养老休闲为一体的综合老年养护中心。 　　除此之外，还有桂林(灵川)天湖水利电业设备有限公司新建年产10000台变压器项目、宝鸿公司标准厂房建设、灵川县八里街九年一贯制学校、灵川县桂黄公路生态景观大通道(八里街-县城)建设项目(二期)、特色街区改造建设项目(灵南路、灵北路)、桂林八里电子物流商务综合项目(中豪国际)、马岭路、桂黄东二路、百花南二路、八里街川东五路、八里街八里四路等在10月竣工项目共11项，总投资达12.89亿元。项目内容广泛涉及工业、物流、房地产、基础设施、教育、卫生等多个行业。 　　重大项目是拉动我县经济社会发展的重要引擎。近年来，我县立足实际，把思维的兴奋点、精力的投放点、工作的着力点集聚到谋划、招引和推进重大项目上来，通过集聚要素促发展，立足更高服务层次转变工作思路，紧扣做活土地文章、破解融资难题、做优承接平台等关键环节，抓紧量化项目实施进度，强化动态跟踪督查，紧锣密鼓抓开工，环环相扣抓推进，攻坚克难抓竣工，确定一批重大项目按期实现开竣工，形成一批新的重要增长点，以此带动和促进全县重大项目投资增长，确保全面完成全年投资和项目建设目标任务，以优异成绩迎接党的十九大胜利召开。 　　▲县委书记赵奇玲宣布桂林天海塑业有限公司圆织成套生产线智能化升级扩能项目开工。 　　▲县长周春涌宣布广西立大节能玻璃幕墙有限责任公司高新技术遮阳节能玻璃及节能幕墙门窗生产项目开工。 　　▲县人大常委会主任唐火祯宣布项目开工。 　　▲县政协主席赵国平与其他领导为潭下福利院开工仪式剪彩。 　　▲灵南路和灵北路道路全长1.2公里，宽32米。改造内容包括对路面进行“白改黑”，增加排水管、绿化带、夜景灯光，设置具有灵川代表性的民俗特色小品和雕塑。立面改造共计44栋，改造总面积63092平方米。 　　▲总投资7.64亿元的桂黄公路改造提升工程二期项目，今年1-9月完成投资1.295亿元，已经完成总工程量的95%。 　　▲桂林八里电子物流商务综合项目(中豪国际)建筑面积4.5万平米，主要建30层综合办公大楼、物流仓储区及相关配套设施。 　　▲占地35000平方米，总投资1.1亿元的八里街九年一贯制学校已经完工投入使用。 　　▲10月16日，桂林天海塑业有限公司圆织成套生产线智能化升级扩能项目开工现场。 　　图片均由记者李桂柏 黄柏秀 廖梓杰　文欢 莫保义 郭丽洁 摄 　　本报讯(记者刘健)10月18日，自治区信访局局长卢万兵一行，到我县调研考察信访维稳系统工作情况，县领导赵奇玲、周春涌、范远明陪同调研。 　　当天，卢万兵一行来到定江镇和县国土资源局，实地调研了基层信访工作的具体方向，了解了信访群体、个案的基本情况，并在县信访局召开座谈会。会上，卢万兵肯定了县委、县政府领导高度重视信访维稳的工作精神，并将把我县定为自治区信访工作联系县，推广我县的工作经验。卢万兵强调，要对现有的信访维稳形势作出科学有效的判断;要精准分类，使工作更有针对性和时效性;要加大信访制度的改革，强化依法逐级走访，严格实行分类，优化信访结构;要加强上级信访部门对特别案件的复查复核;要加强法制化建设的力度，建立律师参与和第三方介入的机制。 　　赵奇玲、周春涌分别就我县信访维稳工作的具体措施和成果作了汇报发言。 　　县领导陈世章、秦卫民参加了当天的座谈会。 　　本报讯(记者刘健)10月16日，我县在县行政大楼七楼常务会议室召开信访维稳工作会议，市委常委、市委秘书长赵仲华出席会议。县领导赵奇玲、周春涌、范远明、罗颖、刘云堂、曾秀维、石玉碧、赵珂参加会议。 　　在详细了解了我县信访维稳工作进展情况和取得的实际成绩后，赵仲华强调，要高度重视信访维稳工作，各级领导班子要亲自抓，建立起一岗双责的工作责任意识;要落实责任，加强管控力度;要学习宣传十九大精神，严格把握、管控好舆论舆情。 　　赵奇玲汇报了我县关于信访维稳工作方面的举措和进展。她表示为迎接党的十九大胜利召开，我县实行县领导大接访制度，以大接访为平台，充分了解百姓的诉求，努力营造相关氛围，组织各乡镇、部门签订了责任状并学习了上级关于信访维稳工作的精神，强力推动问题的解决。 　　周春涌在会上作补充汇报。 　　开展扶贫攻坚 加快小康进程 　　本报讯(记者韦语韬 通讯员黎峭)10月18日下午，由市人大常委会副主任石春莲率领的考察组约50人，在县人大常委会主任唐火祯的陪同下考察了我县灵川镇双潭村人大代表之家。 　　考察组走进双潭村人大代表之家的人大代表办公场所和活动室，对有关办公硬件设施基本齐全、软件材料基本齐备感到十分满意。 　　据了解，双潭村现有县人大代表2名，镇人大代表7名。建立起来的双潭人大代表之家，硬件上达到“六有”，软件方面建立了“两册五簿一档案”，并制定了“六项”活动制度，确保了人大代表开展活动有计划、有落实、有记录、有效果。" ;
			//String Text = "测试地名节点，,京路受暴风雪袭击";
			HLDictManager instance = HLDictManager.getInstance();
			instance.setDictPath("/usr/java/Java_workspace/so/");
			if(!instance.load()){
	
				System.out.println("load dict err");
				
	
			}
			HLSwknl swknl = new HLSwknl();
			
			FileWriter writer = new FileWriter("/root/Desktop/resultloc.txt");
			File file = new File("/root/Desktop/corpus.txt");
			InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"utf-8");
			BufferedReader read = new BufferedReader(reader);
			String LineTxt = "" ;
			while((LineTxt = read.readLine())!=null){
				
				if (CombineLocRec.eventOccurSen.serachEvent(LineTxt)) {
					LineTxt = LineTxt.replaceAll("\t", "");
					writer.write(LineTxt+"\t"+"rec result "+"\t");
					Text = LineTxt ;
					long handle  = swknl.getSegHandle(Text, swknl.MODE_MAX,false, swknl.MAKE_FORSEARCH);
					//List<Locinfo> result_end = CombineLocRec.rec_loc(Text,handle) ;
					Poi baseking = CombineLocRec.getbaseking(Text);
					List<Locinfo> result_end = CombineLocRec.rec_loc_seg(Text ,handle ,swknl,baseking);
					//List<Locinfo> result_end = CombineLocRec.rec_loc_nohandle(Text);
					String hrecall = "" ;
					String dict = "" ;
					for(Locinfo locinfo : result_end ){
						System.out.println(locinfo.poi.getStartPos()+"\t"+
								locinfo.poi.getName()+"\t"+
								locinfo.poi.getAreaID()+"\t"+
								locinfo.poi.getMapLevel()+"\t"+
								locinfo.poi.getMapString()+"\t"+
								locinfo.poi.getTypeMap().get("coordinates_baidu")+"\t"+locinfo.poi.getTypeMap().get("coordinates_gaode")+"\t"+
								locinfo.identify_type);
						if (locinfo.identify_type.equals("dict")) {
							dict +=  locinfo.poi.getName()+"," ;
						}else {
							hrecall += locinfo.poi.getName()+",";
						}
						
					}
					writer.write("dict_rec: "+dict+"\t"+"hrecall_rec: "+hrecall);
					writer.write("\r\n");
					writer.flush();
				}
				
			}
			writer.close();
			read.close();
					
			

			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
