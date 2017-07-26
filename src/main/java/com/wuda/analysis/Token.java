package com.wuda.analysis;

/**
 * token指任何的文本片段,不一定就是单词.
 * 
 * @author wuda
 *
 */
public class Token {
	/**
	 * 分出来的字符串.
	 */
	private String value = null;
	/**
	 * 在原始文本中的开始位置.
	 */
	private int startOffset = 0;
	/**
	 * 在原始文本中的结束位置.
	 */
	private int endOffset = 0;
	/**
	 * 当此字段值为true时,表示此token是一个单词(短语等等).
	 */
	private boolean isWord = false;

	/**
	 * token类型.
	 */
	private String types = null;

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
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
	 * 多个type之间用","隔开.
	 * 
	 * @return the types
	 */
	public String getTypes() {
		return types;
	}

	/**
	 * 多个type之间用","隔开.
	 * 
	 * @param types
	 *            the types to set
	 */
	public void setTypes(String types) {
		this.types = types;
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

	@Override
	public String toString() {
		return value + "\ttype:" + types + "\t(" + startOffset + "," + endOffset + ")";
	}
}
