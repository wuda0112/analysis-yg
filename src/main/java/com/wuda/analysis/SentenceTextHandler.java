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
			if (isSentencePunctuation(ch)) {// 是句子标点
				if (attribute.getTokenLength() > 0) {
					break;
				} else {
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

	/**
	 * 判断字符是否句分割标点.
	 * 
	 * @param character
	 *            字符
	 * @return true-如果是
	 */
	private boolean isSentencePunctuation(int character) {
		if (character == 0x3002 || character == 0xFF1F || character == 0xFF01 || character == 0xFF0C
				|| character == 0xFF1B || character == 33 || character == 39 || character == 44 || character == 46
				|| character == 59) {
			return true;
		}
		return false;
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		startOffset = currentIndex = 0; // 状态重置,以满足重用
	}

}
