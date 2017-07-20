package com.wuda;

/**
 * 常量.
 * 
 * @author wuda
 *
 */
public class Constant {

	/**
	 * 禁止实例化.
	 */
	private Constant() {

	}

	/**
	 * utf-8字符集.
	 */
	public final static String CHARSET_UTF8 = "UTF-8";
	/**
	 * 分词器名称.
	 */
	public final static String ANALYSIS_NAME = "yg";
	/**
	 * not a word.
	 */
	public final static String NOT_A_WORD = "notw";
	/**
	 * 同义词文件的名称,不需要路径,只需要名称.
	 */
	public final static String synonym_file_name = "synonyms.dict";
	/**
	 * 停止词文件的名称,不需要路径,只需要名称.
	 */
	public final static String stopword_file_name = "stopwords.dict";

	/**
	 * 数词文件的名称,不需要路径,只需要名称.
	 */
	public final static String numeral_file_name = "numerals.dict";
	/**
	 * 量词文件的名称,不需要路径,只需要名称.
	 */
	public final static String quantifier_file_name = "quantifiers.dict";
	/**
	 * 默认的数词正则.
	 */
	public final static String default_numeral_regex = "\\d+\\.?\\d*";
	/**
	 * 默认的量词正则.
	 */
	public final static String default_quantifier_regex = "(?:ml|mm|cmd)";

	public final static String important_type_npc = "npc";
	public final static String important_type_npu = "npu";
	public final static String important_type_npb = "npb";
	/**
	 * 缓存词典的map的初始化容量.
	 */
	public final static int initialCapacity = 60000000;
	/**
	 * token类型是英文字母和数字
	 */
	public final static String type_enn = "enn";

	/**
	 * token类型单位.
	 */
	public final static String type_dw = "dw";
}
