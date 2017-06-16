package com.wuda.analysis;

import java.util.ArrayList;
import java.util.List;

import com.wuda.analysis.Trie.NodeElement;
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
		List<Token> tokens = new ArrayList<>();
		Node<NodeElement> parent = dictionary.getRoot();// 从root开始查找
		int startOffset = 0;
		int currentIndex = 0;
		while (startOffset < inputLength) {
			for (currentIndex = startOffset; currentIndex < inputLength; currentIndex++) {
				char c = input[currentIndex];
				Node<NodeElement> child = dictionary.find(parent, c);
				if (child == null) { // 没有找到
					startOffset++;
					parent = dictionary.getRoot();
					break;
				}
				if (child.getElement().isTokenEnd()) {
					Token token = getToken(input, child, startOffset, currentIndex);
					tokens.add(token);
				}
				parent = child;
			}
		}
		return tokens;
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
	 * @return Token
	 */
	private Token getToken(char[] chars, Node<NodeElement> lastNode, int startOffset, int currentOffset) {
		Token token = new Token();
		token.setValue(new String(chars, startOffset, currentOffset - startOffset + 1));
		token.setStartOffset(startOffset);
		token.setEndOffset(currentOffset);
		token.setTypes(lastNode.getElement().getTokenTypes());
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

}
