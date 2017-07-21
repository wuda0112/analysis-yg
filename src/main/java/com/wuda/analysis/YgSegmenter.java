package com.wuda.analysis;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wuda.Constant;
import com.wuda.utils.graph.TokenGraph;
import com.wuda.utils.tree.BasicTree.Node;

/**
 * 分词器.使用前缀树作为词典.
 * 
 * @author wuda
 *
 */
public class YgSegmenter {

	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 词典.
	 */
	private Trie dictionary = null;
	/**
	 * 是否枚举所有的单词.
	 */
	private boolean enumerateAll = false;

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
	 * 从输入中获取token(单词)组成的图.
	 * 
	 * @param input
	 *            输入
	 * @return 词图
	 */
	public TokenGraph getTokenGraph(char[] input) {
		checkDict();
		if (input == null || input.length < 1) {
			return null;
		}
		int inputLength = input.length;
		Node parent = dictionary.getRoot();// 从root开始查找
		int startOffset = 0;
		int currentIndex = 0;

		LinkedList<Token> tokensPerRound = new LinkedList<>();
		TokenGraph graph = new TokenGraph();

		while (startOffset < inputLength) {
			tokensPerRound.clear();

			Token sigle = getToken(input, null, startOffset, startOffset, false);
			tokensPerRound.addLast(sigle);

			parent = dictionary.getRoot();
			for (currentIndex = startOffset; currentIndex < inputLength; currentIndex++) {
				char c = input[currentIndex];
				Node child = dictionary.find(parent, c);
				if (child == null) { // 没有找到
					break;
				}
				if (child.isTokenEnd()) {// 匹配一个单词
					Token token = getToken(input, child, startOffset, currentIndex, true);
					handleTokenPerRound(tokensPerRound, token);
				}
				parent = child;
			}
			addToGraph(graph, tokensPerRound);
			startOffset = tokensPerRound.getFirst().getEndOffset();
		}
		graph.finish();
		return graph;
	}

	/**
	 * 将这一轮所有的token加入词图中.
	 * 
	 * @param graph
	 *            词图
	 * @param tokensPerRound
	 *            这一轮提取的所有token
	 */
	private void addToGraph(TokenGraph graph, LinkedList<Token> tokensPerRound) {
		for (Token token : tokensPerRound) {
			graph.add(token);
		}
	}

	/**
	 * 每一轮都有可能分出多个词,处理这一轮的分词.
	 * 
	 * @param tokensPerRound
	 *            这一轮已经提取的token
	 * @param current
	 *            这一轮最新提取的token
	 */
	private void handleTokenPerRound(LinkedList<Token> tokensPerRound, Token current) {
		if (tokensPerRound.isEmpty()) {
			tokensPerRound.addLast(current);
			return;
		}
		if (current.getEndOffset() - current.getStartOffset() <= 3) {
			tokensPerRound.clear();
			tokensPerRound.addLast(current);
		} else {
			tokensPerRound.addLast(current);
		}
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
		LinkedList<Token> allTokens = new LinkedList<>();
		Node parent = null;
		int startOffset = 0;
		int endOffset = inputLength;
		int currentIndex = 0;

		LinkedList<Token> tokensPerRound = new LinkedList<>();

		int latestTokenEndPosition = -1;// 最近的一个token的结束位置

		int roundCount = 0;

		while (startOffset < inputLength) {
			parent = dictionary.getRoot();// 从root开始查找
			tokensPerRound.clear();
			for (currentIndex = startOffset; currentIndex < endOffset; currentIndex++) {
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
						allTokens.addLast(token);
						latestTokenEndPosition = currentIndex;
					}

					Token token = getToken(input, child, startOffset, currentIndex, true);
					handleTokenPerRound(tokensPerRound, token);
					if (currentIndex > latestTokenEndPosition) {
						latestTokenEndPosition = currentIndex;
					}
				}
				parent = child;
			}
			int[] offset = nextRoundOffset(startOffset, tokensPerRound, latestTokenEndPosition, inputLength);
			startOffset = offset[0];
			endOffset = offset[1];
			if (tokensPerRound.size() > 0) {
				allTokens.addAll(tokensPerRound);
			}
			roundCount++;
			if (roundCount > inputLength) {// 正常情况下是不可能的,为了安全,在这里冗余
				logger.warn("what?提取词的次数比文本还长?当前文本是:" + new String(input));
				break;
			}
		}
		tokensPerRound.clear();
		tokensPerRound = null;
		/**
		 * 最后一个单词与最后一个字符(包含)之间的内容,并不是单词,但是也要返回.
		 */
		int lastCharIndex = inputLength - 1;
		if (lastCharIndex > latestTokenEndPosition) {
			Token token = getToken(input, null, latestTokenEndPosition + 1, lastCharIndex, false);
			allTokens.addLast(token);
			latestTokenEndPosition = currentIndex;
		}
		return allTokens;
	}

	/**
	 * 计算下一次启动的位置和结束的位置.
	 * 
	 * @return 下一次启动和结束的位置
	 */
	private int[] nextRoundOffset(int currentStartOffset, LinkedList<Token> tokensPerRound, int latestTokenEndPosition,
			int inputLength) {
		int nextStartOffset = 0;
		int nextEndOffset = Integer.MAX_VALUE;
		if (enumerateAll) {
			nextStartOffset = currentStartOffset + 1;
		} else {
			if (tokensPerRound.size() > 0) {
				Token last = tokensPerRound.getLast();
				Token first = tokensPerRound.getFirst();
				if (tokensPerRound.size() == 1 || last.getEndOffset() <= latestTokenEndPosition
						|| last.getEndOffset() - first.getEndOffset() <= 1) {
					nextStartOffset = latestTokenEndPosition + 1;
				} else {
					nextStartOffset = tokensPerRound.getFirst().getEndOffset();
					nextEndOffset = tokensPerRound.getLast().getEndOffset();
				}
			} else {
				if (currentStartOffset <= latestTokenEndPosition) {
					nextStartOffset = latestTokenEndPosition + 1;
				} else {
					nextStartOffset = currentStartOffset + 1;
				}
			}
		}
		nextEndOffset = Math.min(nextEndOffset, inputLength);
		int[] offset = new int[] { nextStartOffset, nextEndOffset };
		return offset;
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
		token.setEndOffset(currentOffset + 1);// [startOffset,endOffset)
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
