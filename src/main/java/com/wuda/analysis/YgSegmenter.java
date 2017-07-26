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
		int currentPosition = 0;

		LinkedList<Token> tokensPerRound = new LinkedList<>();
		TokenGraph graph = new TokenGraph();

		while (startOffset < inputLength) {
			tokensPerRound.clear();

			Token sigle = getToken(input, null, startOffset, startOffset, false);
			tokensPerRound.addLast(sigle);

			parent = dictionary.getRoot();
			for (currentPosition = startOffset; currentPosition < inputLength; currentPosition++) {
				char c = input[currentPosition];
				Node child = dictionary.find(parent, c);
				if (child == null) { // 没有找到
					break;
				}
				if (child.isTokenEnd()) {// 匹配一个单词
					Token token = getToken(input, child, startOffset, currentPosition, true);
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
		if (current.getEndOffset() - current.getStartOffset() <= 4) {
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
		int currentPosition = 0;

		LinkedList<Token> tokensPerRound = new LinkedList<>();

		int latestTokenEndPosition = -1;// 最近的一个token的结束位置

		int roundCount = 0;

		while (startOffset < inputLength) {
			parent = dictionary.getRoot();// 从root开始查找
			tokensPerRound.clear();
			boolean ennAndItsPreOrNextAlsoEnn = false;
			for (currentPosition = startOffset; currentPosition < endOffset; currentPosition++) {
				char c = input[currentPosition];
				Node child = dictionary.find(parent, c);
				if (child == null) { // 没有找到
					/**
					 * 尝试提取连着的英文字母和数字,如果有提取到,则返回向前推进的字符数,如果返回-1,则表示没有提取到.
					 */
					int advanceCount = tryExtractEnToken(tokensPerRound, startOffset, currentPosition, input);
					if (advanceCount != -1) {
						/**
						 * 尝试提取成功,则最后一个token的位置一定变化.
						 */
						latestTokenEndPosition = tokensPerRound.peekLast().getEndOffset() - 1;
					}
					break;
				}
				if (child.isTokenEnd()) {// 匹配一个单词

					if (ennAndItsPreOrNextAlsoEnn(startOffset, currentPosition, input)) {
						/**
						 * 这个单词是把英文或者数字截断组成的,不合理.
						 * <p>
						 * 假设:在词典中,【ok】是一个词,【joky】不在词典中;现在输入文本就是【joky】,如果纯按词典分词的话,则会分成【j,ok,y】这三个token,这样是不合理的,因此这个方法就是为了避免这种情况而产生的.
						 * </p>
						 */
						ennAndItsPreOrNextAlsoEnn = true;
						break;
					}

					/**
					 * 上一个单词与当前单词之间的文本,并不是单词,但是也要返回.
					 */
					if (startOffset > latestTokenEndPosition + 1) {
						Token token = getToken(input, null, latestTokenEndPosition + 1, startOffset - 1, false);
						allTokens.addLast(token);
						latestTokenEndPosition = currentPosition;
					}

					Token token = getToken(input, child, startOffset, currentPosition, true);
					handleTokenPerRound(tokensPerRound, token);
					if (currentPosition > latestTokenEndPosition) {
						latestTokenEndPosition = currentPosition;
					}
				}
				parent = child;
			}
			int[] offset = nextRoundOffset(startOffset, currentPosition, tokensPerRound, latestTokenEndPosition,
					inputLength, ennAndItsPreOrNextAlsoEnn);
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
			latestTokenEndPosition = currentPosition;
		}
		return allTokens;
	}

	/**
	 * 当前的字符是否英文或者数字.
	 * 
	 * @param ch
	 *            char
	 * @return true- 如果是英文和数字
	 */
	private boolean isLetterOrNumber(char ch) {
		if (ch >= 48 && ch <= 57) {// 数字
			return true;
		} else if (ch >= 65 && ch <= 90) {// 大写字母
			return true;
		} else if (ch >= 97 && ch <= 122) { // 小写字母
			return true;
		}
		return false;
	}

	/**
	 * 注意这里的语义是：当上一个token的最后一个字符是纯英文或者数字,并且当前字符也是英文或者数字时,尝试提取连着的英文字母和数字,如果有提取到,则返回向前推进的字符数,如果返回-1,则表示没有提取到.
	 * 
	 * @param tokensPerRound
	 *            这一轮已经提取的token
	 * @param startOffset
	 *            这一轮开始offset
	 * @param currentPosition
	 *            这一轮当前的下标
	 * @param input
	 *            完整的输入串
	 * @return -1则表示尝试失败
	 */
	private int tryExtractEnToken(LinkedList<Token> tokensPerRound, int startOffset, int currentPosition, char[] input) {
		if (tokensPerRound.size() < 1) {
			return -1;
		}
		if (currentPosition < 1 || currentPosition >= input.length) {
			// 这里基本上不会发生,为了安全多检测一次,确保不会数组越界
			return -1;
		}
		Token last = tokensPerRound.peekLast();
		if (last.getStartOffset() == startOffset && last.getEndOffset() <= currentPosition) {
			/**
			 * 当前字符和他紧邻的上一个字符必须同时都是英文字符或数字.
			 */
			if (isLetterOrNumber(input[currentPosition]) && isLetterOrNumber(input[currentPosition - 1])) {// 当前和上一个字符都是英文字母或者数字
				int advanceCount = 1;
				for (currentPosition++; currentPosition < input.length; currentPosition++) {
					if (!isLetterOrNumber(input[currentPosition])) {
						break;
					}
					advanceCount++;
				}
				Token token = getToken(input, null, startOffset, currentPosition - 1, true);
				handleTokenPerRound(tokensPerRound, token);
				return advanceCount;
			}
		}
		return -1;
	}

	/**
	 * 当[startOffset,currentPosition]之间的内容是英文或者数字,并且这块内容的前一个<Strong>or</Strong>后一个字符也是英文或者数字时,返回true.
	 * 
	 * @param startOffset
	 *            包含
	 * @param currentPosition
	 *            包含
	 * @param input
	 *            input
	 * @return true or false
	 */
	private boolean ennAndItsPreOrNextAlsoEnn(int startOffset, int currentPosition, char[] input) {
		boolean isLetterOrNumber = false;
		for (int i = startOffset; i <= currentPosition; i++) {
			isLetterOrNumber = isLetterOrNumber(input[i]);
			if (!isLetterOrNumber) {
				return false;
			}
		}
		int pre = startOffset == 0 ? -1 : startOffset - 1;
		boolean preIsLetterOrNumber = false;
		if (pre != -1) {
			preIsLetterOrNumber = isLetterOrNumber(input[pre]);
		}
		int next = currentPosition == input.length - 1 ? -1 : currentPosition + 1;
		boolean nextIsLetterOrNumber = false;
		if (next != -1) {
			nextIsLetterOrNumber = isLetterOrNumber(input[next]);
		}
		if (isLetterOrNumber && (preIsLetterOrNumber || nextIsLetterOrNumber)) {
			return true;
		}
		return false;
	}

	/**
	 * 计算下一次启动的位置和结束的位置.
	 * 
	 * @return 下一次启动和结束的位置
	 */
	private int[] nextRoundOffset(int currentStartOffset, int currintIndex, LinkedList<Token> tokensPerRound,
			int latestTokenEndPosition, int inputLength, boolean ennAndItsPreOrNextAlsoEnn) {
		int nextStartOffset = 0;
		int nextEndOffset = Integer.MAX_VALUE;
		if (ennAndItsPreOrNextAlsoEnn) {
			nextStartOffset = currintIndex + 2;
		} else {
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
						nextStartOffset = first.getEndOffset();
						nextEndOffset = last.getEndOffset();
					}
				} else {
					if (currentStartOffset <= latestTokenEndPosition) {
						nextStartOffset = latestTokenEndPosition + 1;
					} else {
						nextStartOffset = currentStartOffset + 1;
					}
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
			if (lastNode != null) {
				token.setTypes(lastNode.getTypes());
			} else {
				// do nothing
			}
		} else {
			token.setTypes(Constant.fixed_token_type_not_a_word);
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
