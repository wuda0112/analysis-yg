package com.wuda.analysis;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.wuda.utils.tree.BasicTree.Node;

/**
 * 将文本分割成句;同时在此过程中预先【提取了容易产生歧义的词】.
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
	private int currentPosition = 0;// 输入流input当前已经读取到的位置

	/**
	 * 将文本分割成句子,在此过程中预先【提取了容易产生歧义的词】.
	 * 
	 * @return true-如果还可以分出句子,获取提取出【容易产生歧义的词】
	 * @throws IOException
	 */
	private boolean hasSentence() throws IOException {
		if (input == null) {
			return false;
		}
		attribute.clearAttributes();
		if (everMatchAmbiguous.get()) {// 有歧义词
			fillAmbiguousAttr();
			resetAmbiguousInfo();
			return true;
		}
		int ch = -1;
		startOffset = currentPosition;
		while ((ch = input.read()) != -1) {
			currentPosition++;
			if (AnalysisUtil.isSentencePunctuation(ch)/* 是句子标点 */ || Character.isWhitespace(ch)
					|| Character.isSpaceChar(ch)) {
				if (attribute.getTokenLength() > 0) {
					break;
				} else {
					startOffset = currentPosition;
					continue;
				}
			}
			ch=Character.toLowerCase(ch);
			attribute.tokenAppend((char) ch);
			if (tryExtractAmbiguous((char) ch)) {
				break;
			}
		}
		if (attribute.getTokenLength() > 0) {
			int end = 0;
			int newLength = -1;
			if (everMatchAmbiguous.get()) {
				if (startOffset == startOffset + ambiguousStart) {// 整个字符串都是歧义词
					fillAmbiguousAttr();
					resetAmbiguousInfo();
					return true;
				} else {
					/**
					 * [startOffset,end)是普通文本,[ambiguousStart,length)是歧义词.
					 */
					end = startOffset + ambiguousStart;
					newLength = end - startOffset;
					try {
						ambiguousToken = attribute.substring(ambiguousStart);
					} catch (Exception e) {
						System.out.println(
								"end:" + end + "\tstartOffset:" + startOffset + "\tattr:" + attribute.getTokenString());
					}

				}
			} else {
				end = startOffset + attribute.getTokenLength();
			}

			attribute.setType("sentence");
			if (newLength != -1) {// 当有【歧义词】被提取
				attribute.setLength(newLength);
			}
			attribute.setStartOffset(startOffset);
			attribute.setEndOffset(end);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		startOffset = currentPosition = 0; // 状态重置,以满足重用
		resetAmbiguousInfo();
	}

	/**
	 * 以下属性用于提取【容易产生歧义】的词.
	 */
	private Trie ambiguousDict = FileDictionaryHandler.ambiguous;
	private Node ambiguousParent = ambiguousDict.getRoot();// 用于提取【容易产生歧义的词】
	private int ambiguousStart = 0;
	private AtomicBoolean everMatchAmbiguous = new AtomicBoolean(false);
	private String ambiguousToken = null;
	private String ambiguousTokenType = null;

	/**
	 * 尝试提取【容易产生歧义】的词.
	 * 
	 * @param ch
	 *            char
	 * @return true如果已经提取到了
	 */
	private boolean tryExtractAmbiguous(char ch) {
		Node ambiguousChild = ambiguousDict.find(ambiguousParent, ch);
		if (ambiguousChild == null) {
			ambiguousParent = ambiguousDict.getRoot();
			ambiguousStart = -1;
		} else {
			ambiguousParent = ambiguousChild;
			if (ambiguousStart == -1) {
				ambiguousStart = currentPosition - startOffset - 1;
			}
			if (ambiguousChild.isTokenEnd()) {
				everMatchAmbiguous.compareAndSet(false, true);
				ambiguousTokenType = ambiguousChild.getTypes();
				ambiguousToken = attribute.substring(ambiguousStart);
				return true;
			}
		}
		return false;
	}

	/**
	 * 用于提取【容易产生歧义】的词所用到的 属性重置.
	 */
	private void resetAmbiguousInfo() {
		ambiguousParent = ambiguousDict.getRoot();
		ambiguousStart = 0;
		everMatchAmbiguous.set(false);
		ambiguousToken = null;
		ambiguousTokenType = null;
	}

	/**
	 * 填充歧义词信息.
	 */
	private void fillAmbiguousAttr() {
		attribute.clearAttributes();
		attribute.tokenAppend(ambiguousToken);
		attribute.setStartOffset(startOffset + ambiguousStart);
		attribute.setEndOffset(currentPosition);
		attribute.setWord(true);
		attribute.setType(ambiguousTokenType);
	}

}
