package com.wuda;

import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import com.wuda.analysis.DictBasedTextHandlerFilter;
import com.wuda.analysis.SentenceTextHandler;
import com.wuda.analysis.TextHandlerSharedAttribute;

public class TextHandlerTest {

	public static void main(String[] args) {
		TextHandlerSharedAttribute attribute = new TextHandlerSharedAttribute();
		SentenceTextHandler bottomHandler = new SentenceTextHandler(attribute);// 文本先分割成句子
		/* 在bottomHandler和topFilter中间还可以有多个TextHandlerFilter来组成链 */
		DictBasedTextHandlerFilter topFilter = new DictBasedTextHandlerFilter(bottomHandler, attribute);// 在把句子根据词典分词
		int i = 0;
		while (i < 2) { // 调用两次是为了测试重用功能
			i++;
			try {
				String text = "java开发人员,中华人民,你好好人";// 根据你自己的词典的内容测试分词清单
				Reader input = new StringReader(text);

				bottomHandler.setReader(input);
				topFilter.reset(); // 同一个handler链可以重复使用
				System.out.println("\n=============加载词典...====================\n");
				long startTime = System.currentTimeMillis();
				topFilter.loadDictFrom("e:/dict", false); // 从词典目录加载词典,同步加载词典
				long end = System.currentTimeMillis();
				System.out.println(
						"\n===============加载词典完成,用时:" + (end - startTime) + "毫秒================================\n");
				/**
				 * 如果异步加载,代码执行到这里是有可能词典中还没有内容,导致不能正确分词,因此可以在这里等等,让词典加载一会儿.
				 * 如果词典很大的话, 也可以时间设置的久点
				 */
				// Thread.sleep(3000);
				while (topFilter.incrementToken()) {
					System.out.println(attribute.getTokenString() + "\tstartOffset:" + attribute.getStartOffset()
							+ "\tendOffset:" + attribute.getEndOffset() + "\ttype:" + attribute.getType());

				}
				topFilter.close();

				System.out.println(
						"\n========================以下是lucen的standard文本处理======================================\n");

				StandardTokenizer standard = new StandardTokenizer();
				Reader input2 = new StringReader(text);
				standard.setReader(input2);
				standard.reset();
				CharTermAttribute charTermAttr = standard.addAttribute(CharTermAttribute.class);
				OffsetAttribute offsetAttr = standard.addAttribute(OffsetAttribute.class);
				TypeAttribute typeAttr = standard.addAttribute(TypeAttribute.class);
				PositionIncrementAttribute posIncrAtt = standard.addAttribute(PositionIncrementAttribute.class);
				while (standard.incrementToken()) {
					System.out.println(charTermAttr.toString() + "\tstartOffset:" + offsetAttr.startOffset()
							+ "\tendOffset:" + offsetAttr.endOffset() + "\ttype:" + typeAttr.type() + "\tposition:"
							+ posIncrAtt.getPositionIncrement());

				}
				standard.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
