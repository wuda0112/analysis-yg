package com.wuda.analysis;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.wuda.Constant;
import com.wuda.utils.tree.BasicTree.Node;

/**
 * 分词器.使用前缀树作为词典.
 * 
 * @author wuda
 *
 */
public class YgSegmenter {

	/**
	 * 词典.
	 */
	private Trie dictionary = null;
	/**
	 * 是否枚举所有的单词.
	 */
	private boolean enumerateAll = true;

	/**
	 * 检查此分词器是否有词典.
	 */
	private void checkDict() {
		if (dictionary == null) {
			throw new NoDictException("没有为此分词器设置词典");
		}
	}

	/**
	 * 从输入中获取token(单词).
	 * 
	 * @param input
	 *            输入
	 * @return 所有token,null-如果没有任何token(单词)
	 */
	public List<Token> getTokens(String input) {
		if (input == null || input.isEmpty()) {
			return null;
		}
		char[] chars = input.toCharArray();
		return getTokens(chars);
	}

	/**
	 * 从输入中获取token(单词).
	 * 
	 * @param input
	 *            输入
	 * @return 所有token,null-如果没有任何token(单词)
	 */
	public List<Token> getTokens(char[] input) {
		checkDict();
		if (input == null || input.length < 1) {
			return null;
		}
		int inputLength = input.length;
		LinkedList<Token> tokens = new LinkedList<>();
		Node parent = dictionary.getRoot();// 从root开始查找
		int startOffset = 0;
		int currentIndex = 0;

		int latestTokenEndPosition = -1;// 最近的一个token的结束位置

		while (startOffset < inputLength) {
			AtomicBoolean everMatch = new AtomicBoolean(false);
			AtomicInteger firstNotSingleTokenEndPosition = new AtomicInteger(-1);// 当前这次匹配中,第一个token的结束位置
			parent = dictionary.getRoot();
			for (currentIndex = startOffset; currentIndex < inputLength; currentIndex++) {
				char c = input[currentIndex];
				Node child = dictionary.find(parent, c);
				if (child == null) { // 没有找到
					break;
				}
				if (child.isTokenEnd()) {// 匹配一个单词
					/**
					 * 上一个单词与当前单词之间的文本,并不是单词,但是也要返回.
					 */
					if (startOffset > latestTokenEndPosition + 1) {
						Token token = getToken(input, null, latestTokenEndPosition + 1, startOffset - 1, false);
						tokens.addLast(token);
						latestTokenEndPosition = currentIndex;
					}

					Token token = getToken(input, child, startOffset, currentIndex, true);
					append(tokens, token, enumerateAll);
					latestTokenEndPosition = currentIndex;
					everMatch.compareAndSet(false, true);
					if (currentIndex - startOffset >= 1) {// 不是单字
						firstNotSingleTokenEndPosition.compareAndSet(-1, currentIndex);
					}
				}
				parent = child;
			}
			startOffset = advance(startOffset, everMatch, firstNotSingleTokenEndPosition.get(), latestTokenEndPosition);
		}
		/**
		 * 最后一个单词与最后一个字符(包含)之间的内容,并不是单词,但是也要返回.
		 */
		int lastCharIndex = inputLength - 1;
		if (lastCharIndex > latestTokenEndPosition) {
			Token token = getToken(input, null, latestTokenEndPosition + 1, lastCharIndex, false);
			tokens.addLast(token);
			latestTokenEndPosition = currentIndex;
		}
		return tokens;
	}

	/**
	 * 计算下一次启动的位置.
	 * 
	 * @param currentStartOffset
	 * @param everMatch
	 * @param firstNotSingleTokenEndPosition
	 * @return 下一次启动的位置
	 */
	private int advance(int currentStartOffset, AtomicBoolean everMatch, int firstNotSingleTokenEndPosition,
			int latestTokenEndPosition) {
		int nextStartOffset = currentStartOffset;
		if (enumerateAll) {
			nextStartOffset++;
		} else {
			if (everMatch.get()) {
				nextStartOffset = latestTokenEndPosition + 1;
			} else {
				nextStartOffset++;
			}
		}
		return nextStartOffset;
	}

	/**
	 * 追加token.
	 * 
	 * @param tokens
	 *            已经存在的token
	 * @param current
	 *            本次追加的token
	 * @param enumerateAll
	 *            如果是false,则具有包含关系的token只能存在更大者,被包含的token被移除.
	 */
	private void append(LinkedList<Token> tokens, Token current, boolean enumerateAll) {
		if (enumerateAll) {
			tokens.addLast(current);
			return;
		}
		Token last = tokens.peekLast();
		if (last == null) {
			tokens.addLast(current);
			return;
		}
		String lastTokenTypes = last.getTypes();
		boolean lastTokenHasImportantType = false;
		if (lastTokenTypes != null && !lastTokenTypes.isEmpty()) {
			if (lastTokenTypes.contains(Constant.important_type_npc)) {
				lastTokenHasImportantType = true;
			} else if (lastTokenTypes.contains(Constant.important_type_npb)) {
				lastTokenHasImportantType = true;
			} else if (lastTokenTypes.contains(Constant.important_type_npu)) {
				lastTokenHasImportantType = true;
			}
		}

		String currentTokenTypes = current.getTypes();
		boolean currentTokenHasImportantType = false;
		if (currentTokenTypes != null && !currentTokenTypes.isEmpty()) {
			if (currentTokenTypes.contains(Constant.important_type_npc)) {
				currentTokenHasImportantType = true;
			} else if (currentTokenTypes.contains(Constant.important_type_npb)) {
				currentTokenHasImportantType = true;
			} else if (currentTokenTypes.contains(Constant.important_type_npu)) {
				currentTokenHasImportantType = true;
			}
		}

		if (last.getStartOffset() >= current.getStartOffset() && last.getEndOffset() <= current.getEndOffset()) {// 当前token包含上一个token
			if (!lastTokenHasImportantType || currentTokenHasImportantType) {
				tokens.removeLast();
			}
			tokens.addLast(current);
		} else if (last.getStartOffset() <= current.getStartOffset() && last.getEndOffset() >= current.getEndOffset()) {// 上一个token包含上当前token
			if (currentTokenHasImportantType && !lastTokenHasImportantType) {
				tokens.add(current);
			}
		} else {
			tokens.addLast(current);
		}
	}

	/**
	 * 生成token.
	 * 
	 * @param chars
	 *            chars
	 * @param lastNode
	 *            组成token的最后一个节点
	 * @param startOffset
	 *            在chars中的开始偏移
	 * @param currentOffset
	 *            在chars中的结尾偏移量
	 * @param isWord
	 *            是否单词
	 * @return Token
	 */
	private Token getToken(char[] chars, Node lastNode, int startOffset, int currentOffset, boolean isWord) {
		Token token = new Token();
		token.setValue(new String(chars, startOffset, currentOffset - startOffset + 1));
		token.setStartOffset(startOffset);
		token.setEndOffset(currentOffset);
		token.setWord(isWord);
		if (isWord) {
			token.setTypes(lastNode.getTypes());
		} else {
			token.setTypes(Constant.NOT_A_WORD);
		}
		return token;
	}

	/**
	 * 获取此分词器所使用的词典.
	 * 
	 * @return the dictionary
	 */
	public Trie getDictionary() {
		return dictionary;
	}

	/**
	 * 为此分词器指定一个词典.
	 * 
	 * @param dictionary
	 *            the dictionary to set
	 */
	public void setDictionary(Trie dictionary) {
		this.dictionary = dictionary;
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
