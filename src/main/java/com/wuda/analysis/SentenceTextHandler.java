package com.wuda.analysis;

import java.io.IOException;

/**
 * 将文本分割成句.
 * 
 * @author wuda
 *
 */
public class SentenceTextHandler extends TextHandler {

	public SentenceTextHandler(TextHandlerSharedAttribute attribute) {
		super(attribute);
	}

	/**
	 * 如果返回true,则返回的token是一个句子.
	 */
	@Override
	public boolean incrementToken() throws IOException {
		if (hasSentence()) {
			return true;
		}
		return false;
	}

	private int startOffset = 0;
	private int currentIndex = 0;// 输入流input当前已经读取到的位置

	/**
	 * 将文本分割成句子.
	 * 
	 * @return true-如果还可以分出句子
	 * @throws IOException
	 */
	private boolean hasSentence() throws IOException {
		if (input == null) {
			return false;
		}
		attribute.clearAttributes();
		int ch = -1;
		while ((ch = input.read()) != -1) {
			currentIndex++;
			if (AnalysisUtil.isSentencePunctuation(ch)/* 是句子标点 */ || Character.isWhitespace(ch)
					|| Character.isSpaceChar(ch)) {
				if (attribute.getTokenLength() > 0) {
					break;
				} else {
					startOffset = currentIndex;
					continue;
				}
			}
			attribute.tokenAppend((char) ch);
		}
		if (attribute.getTokenLength() > 0) {
			attribute.setType("sentence");
			attribute.setStartOffset(startOffset);
			attribute.setEndOffset(startOffset + attribute.getTokenLength() + 1);// endOffset不一定等于currentIndex,因为如果是标点符号时,标点符号是不包含的
			startOffset = currentIndex;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		startOffset = currentIndex = 0; // 状态重置,以满足重用
	}

}
