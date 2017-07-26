package com.wuda.analysis;

import com.wuda.Constant;

/**
 * 在{@link TextHandler}与{@link TextHandlerFilter}组成的处理链中,所有的handler共享这一个类的实例.
 * 
 * @author wuda
 *
 */
public class TextHandlerSharedAttribute {

	/**
	 * token值.
	 */
	private final StringBuilder token = new StringBuilder();
	/**
	 * 范围是[startOffset,endOffset)
	 */
	private int startOffset, endOffset = 0;
	/**
	 * token类型.
	 */
	private String type = null;

	/**
	 * 当此字段值为true时,表示此token是一个单词(短语等等).
	 */
	private boolean isWord = false;

	/**
	 * 清除属性值.
	 */
	public void clearAttributes() {
		token.delete(0, token.length());
		startOffset = 0;
		endOffset = 0;
		isWord = false;
		type = null;
	}

	/**
	 * Appends the specified string.
	 * 
	 * @param str
	 *            string
	 */
	public void tokenAppend(String str) {
		token.append(str);
	}

	/**
	 * Appends the specified char.
	 * 
	 * @param c
	 *            char
	 */
	public void tokenAppend(char c) {
		token.append(c);
	}

	/**
	 * Appends the specified chars.
	 * 
	 * @param chars
	 *            source chars
	 * @param offset
	 *            the index of the first char to append
	 * @param len
	 *            the number of chars to append.
	 */
	public void tokenAppend(char[] chars, int offset, int len) {
		token.append(chars, offset, len);
	}

	/**
	 * token的长度.
	 * 
	 * @return 长度
	 */
	public int getTokenLength() {
		return token.length();
	}

	/**
	 * 获取token.
	 * 
	 * @return string
	 */
	public String getTokenString() {
		return token.toString();
	}

	/**
	 * @return the startOffset
	 */
	public int getStartOffset() {
		return startOffset;
	}

	/**
	 * @param startOffset
	 *            the startOffset to set
	 */
	public void setStartOffset(int startOffset) {
		this.startOffset = startOffset;
	}

	/**
	 * @return the endOffset
	 */
	public int getEndOffset() {
		return endOffset;
	}

	/**
	 * @param endOffset
	 *            the endOffset to set
	 */
	public void setEndOffset(int endOffset) {
		this.endOffset = endOffset;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		if (isWord) {
			return type;
		}
		return Constant.fixed_token_type_not_a_word;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the isWord
	 */
	public boolean isWord() {
		return isWord;
	}

	/**
	 * @param isWord
	 *            the isWord to set
	 */
	public void setWord(boolean isWord) {
		this.isWord = isWord;
	}

	/**
	 * 参考{@link StringBuilder#setLength(int)}.
	 * 
	 * @param newLength
	 *            newLength
	 */
	public void setLength(int newLength) {
		token.setLength(newLength);
	}

	/**
	 * 参考{@link StringBuilder#substring(int)}
	 * 
	 * @param start
	 *            start
	 * @return sub string
	 */
	public String substring(int start) {
		return token.substring(start);
	}

}
