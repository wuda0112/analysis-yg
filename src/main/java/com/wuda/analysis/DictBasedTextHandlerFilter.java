package com.wuda.analysis;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * 基于词典的分词.不是单词的文本也会被返回,并且这些文本的{@link TextHandlerSharedAttribute#isWord()}等于<code>false</code>.
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
	 * 是否枚举所有的单词.
	 */
	private boolean enumerateAll = false;

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
		if (segmenter == null) {
			segmenter = new YgSegmenter();
			FileDictionaryHandler dictionaryHandler = new FileDictionaryHandler();
			Trie dictionary = dictionaryHandler.getDictionary();// 获取词典,词典是多线程异步加载的,不会阻塞
			segmenter.setDictionary(dictionary);
			segmenter.setEnumerateAll(enumerateAll);
		}
		if (iterator != null && iterator.hasNext()) {
			fillSharedAttr();
			return true;
		}
		if (input.incrementToken()) {
			if (attribute.isWord()) {//从下一层传过来的已经是一个word
				return true;
			}
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
	 * 填充属性.
	 */
	private void fillSharedAttr() {
		attribute.clearAttributes();
		Token token = iterator.next();
		attribute.tokenAppend(token.getValue());
		attribute.setType(token.getTypes());
		attribute.setStartOffset(baseCoord + token.getStartOffset());
		attribute.setEndOffset(baseCoord + token.getEndOffset());
		attribute.setWord(token.isWord());
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		tokens = null;
		iterator = null;
		baseCoord = 0;
	}

	/**
	 * @return the enumerateAll
	 */
	public boolean isEnumerateAll() {
		return enumerateAll;
	}

	/**
	 * @param enumerateAll
	 *            the enumerateAll to set
	 */
	public void setEnumerateAll(boolean enumerateAll) {
		this.enumerateAll = enumerateAll;
	}
}
