package com.wuda.analysis;

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
	 * 清除属性值.
	 */
	public void clearAttributes() {
		token.delete(0, token.length());
		startOffset = 0;
		endOffset = 0;
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
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

}
