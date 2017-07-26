package com.wuda.analysis;

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
	 * 分词时,容易引起歧义的词.
	 * 
	 * @return 词典
	 */
	public Trie getAmbiguous();
}
