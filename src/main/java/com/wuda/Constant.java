package com.wuda;

import java.util.regex.Pattern;

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
	 * 同义词文件的名称,不需要路径,只需要名称.
	 */
	public final static String synonym_file_name = "synonyms.dict";
	/**
	 * 停止词文件的名称,不需要路径,只需要名称.
	 */
	public final static String stopword_file_name = "yg_stopwords.dict";
	/**
	 * 为了兼容ik的停止词.
	 */
	public final static String ik_stopword_file_name = "stopword.dic";

	/**
	 * 分词时容易引起歧义的词,不需要路径,只需要名称.
	 */
	public final static String ambiguous_file_name = "ambiguous.dict";

	/**
	 * token类型是英文字母或者数字,其他标点符号组成
	 */
	public final static String fixed_token_type_enn = "enn";
	/**
	 * 提取{@ #fixed_token_type_enn}这样类型的token的正则表达式.
	 */
	public final static Pattern fixed_token_type_enn_regex = Pattern.compile("([a-zA-z\\d\\.-_]+)");// 英文和数字

	/**
	 * 单位,比如:1毫升.
	 */
	public final static String fixed_token_type_dw = "dw";
	/**
	 * 提取单位的正则表达式.
	 */
	public final static Pattern fixed_token_type_dw_regex = Pattern.compile(
			"((\\d+\\.?\\d*(?:ml|小包|cm|xl|kg|毫安|cc|公分|千克|克拉|mm|包|只|f|听|片|℃|g|l|n|卷|瓶|度|斤|层|盒|元|个|m|米|件|克|罐|月|a|抽|种|岁|册|枚|款|寸|升|k)))");// 单位

	/**
	 * not a word.
	 */
	public final static String fixed_token_type_not_a_word = "notw";

	/**
	 * 缓存词典的map的初始化容量.
	 */
	public final static int initialCapacity = 60000000;

}
