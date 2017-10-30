package com.hylanda.sevenfeature;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import com.hylanda.sevenfeature.CreateTrainData.Features;

public class SingleTest {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		CreateTrainData createTrainData = new CreateTrainData();
		createTrainData.init("dict/类型语素.txt","dict/区别性语素.txt","dict/方位语素.txt","dict/部位语素.txt");
		//List<Features> result = createTrainData.gernateFeatures("天津市滨海新区大神堂发生火灾");
		/*for(Features fea : result){
			String output = fea.word+" "+fea.words+" "+fea.postag+" "+fea.istype+" "+fea.isdiff+" "+fea.isdirect+" "+fea.ispart+" "+fea.tag;
			System.out.println(output);

		}*/

	}

}
