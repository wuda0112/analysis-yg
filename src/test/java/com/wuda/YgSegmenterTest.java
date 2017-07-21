package com.wuda;

import java.io.IOException;

import com.wuda.analysis.FileDictionaryHandler;
import com.wuda.analysis.YgSegmenter;
import com.wuda.utils.graph.TokenGraph;

public class YgSegmenterTest {

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		FileDictionaryHandler handler = new FileDictionaryHandler();
		handler.setDirectory("e:/dict");
		handler.setIsAsynLoadDict(false);
		handler.loadAll(); // 从词典目录加载词典,同步加载词典
		long end = System.currentTimeMillis();
		System.out.println("\n===============加载词典完成,用时:" + (end - startTime) + "毫秒================================\n");
		YgSegmenter seg = new YgSegmenter();
		seg.setDictionary(handler.getDictionary());
		String text = "百岁山矿泉水饮用水";// 根据你自己的词典的内容测试分词清单
		TokenGraph graph = seg.getTokenGraph(text.toCharArray());
		try {
			graph.toDot("e:/1.dot");
			graph.clear();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
