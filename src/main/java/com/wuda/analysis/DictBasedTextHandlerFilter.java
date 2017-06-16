package com.wuda.analysis;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * 基于词典的分词.
 * 
 * @author wuda
 *
 */
public class DictBasedTextHandlerFilter extends TextHandlerFilter {

	/**
	 * 分词器.
	 */
	private YgSegmenter segmenter;
	/**
	 * 字典处理类.
	 */
	private FileDictionaryHandler dictionaryHandler = null;

	/**
	 * 词典所在的目录.
	 */
	private String dictDir = null;

	/**
	 * 所有的token(单词).
	 */
	private List<Token> tokens = null;

	/**
	 * token迭代器.
	 */
	private Iterator<Token> iterator = null;

	private int baseCoord = 0;

	public DictBasedTextHandlerFilter(TextHandler input, TextHandlerSharedAttribute attribute) {
		super(input, attribute);
	}

	/**
	 * 基于词典分词,返回的token是词典中的一个单词.
	 */
	@Override
	public boolean incrementToken() throws IOException {
		if (this.dictDir == null) {
			throw new NoDictException(
					"此handler是基于词典的,所以请先调用loadDictFrom(String dictDir, boolean isAsynLoadDict)方法加载词典");
		}
		if (iterator != null && iterator.hasNext()) {
			fillSharedAttr();
			return true;
		}
		if (input.incrementToken()) {
			baseCoord = attribute.getStartOffset();
			tokens = segmenter.getTokens(attribute.getTokenString());// 此时的token中保存的是句子,获取分词
			if (tokens == null || tokens.isEmpty()) {
				return false;
			}
			iterator = tokens.iterator();
			fillSharedAttr();
			return true;
		}
		return false;
	}

	/**
	 * 加载词典
	 * 
	 * @param dictDir
	 *            词典所在目录
	 */
	public void loadDictFrom(String dictDir, boolean isAsynLoadDict) {
		if (dictDir == null) {
			throw new NoDictException("没有词典目录.既然是基于词典的分词,请设置正确的词典目录.");
		}
		this.dictDir = dictDir;
		segmenter = new YgSegmenter();
		dictionaryHandler = new FileDictionaryHandler();
		dictionaryHandler.setDirectory(dictDir);
		dictionaryHandler.setIsAsynLoadDict(isAsynLoadDict);
		Trie dictionary = dictionaryHandler.getDictionary();// 获取词典,词典是多线程异步加载的
		segmenter.setDictionary(dictionary);
	}

	/**
	 * 填充属性.
	 */
	private void fillSharedAttr() {
		attribute.clearAttributes();
		Token token = iterator.next();
		attribute.tokenAppend(token.getValue());
		attribute.setType(token.getTypeString());
		attribute.setStartOffset(baseCoord + token.getStartOffset());
		attribute.setEndOffset(baseCoord + token.getEndOffset() + 1);
	}

	/**
	 * @return the dictDir
	 */
	public String getDictDir() {
		return dictDir;
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		tokens = null;
		iterator = null;
		baseCoord = 0;
	}
}
