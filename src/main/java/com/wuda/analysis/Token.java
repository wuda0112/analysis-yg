package com.wuda.analysis;

import java.util.Set;

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
	 * token类型.
	 */
	private Set<String> types = null;

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
	 * @return the types
	 */
	public Set<String> getTypes() {
		return types;
	}

	/**
	 * @param types
	 *            the types to set
	 */
	public void setTypes(Set<String> types) {
		this.types = types;
	}

	/**
	 * 获取字符串形式的类型,多个类型之间用","隔开.
	 * 
	 * @return null-如果不存在类型
	 */
	public String getTypeString() {
		if (types == null || types.isEmpty()) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		for (String type : types) {
			builder.append(type);
			builder.append(",");
		}
		return builder.substring(0, builder.length() - 1);
	}
}
