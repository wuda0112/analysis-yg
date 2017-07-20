package com.wuda.analysis;

import java.util.List;
import java.util.Set;

/**
 * 词典处理类.
 * 
 * @author wuda
 *
 */
public interface DictionaryHandler {

	/**
	 * 获取词典.
	 * 
	 * @return 词典
	 */
	public Trie getDictionary();

	/**
	 * 获取停止词.
	 * 
	 * @return 停止词集合
	 */
	public Set<String> getStopwords();

	/**
	 * 获取数词.
	 * 
	 * @return 数词集合.
	 */
	public Set<String> getNumerals();

	/**
	 * 获取量词.
	 * 
	 * @return 量词集合
	 */
	public List<String> getQuantifiers();
}
