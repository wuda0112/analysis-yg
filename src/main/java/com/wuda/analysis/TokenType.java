package com.wuda.analysis;

/**
 * token(单词)的类型.
 * 
 * @author wuda
 *
 */
public enum TokenType {
	/**
	 * 正常的.
	 */
	NORMAL("normal");

	private String typeKey;

	private TokenType(String typeKey) {
		this.typeKey = typeKey;
	}

	/**
	 * 获取对应的类型.
	 * 
	 * @param typeKey
	 *            类型
	 * @return 对应的类型
	 */
	public static TokenType getByTypeKey(String typeKey) {
		if (typeKey == null || typeKey.isEmpty()) {
			return null;
		}
		typeKey = typeKey.toLowerCase().trim();
		TokenType[] types = TokenType.values();
		for (TokenType tokenType : types) {
			if (tokenType.typeKey.equals(typeKey)) {
				return tokenType;
			}
		}
		return null;
	}

	/**
	 * 展示types.
	 * 
	 * @return types,用逗号隔开.
	 */
	static String showTypes() {
		StringBuilder builder = new StringBuilder();
		TokenType[] types = TokenType.values();
		for (TokenType type : types) {
			builder.append(type.typeKey);
			builder.append(",");
		}
		return builder.substring(0, builder.length() - 1);
	}

}
