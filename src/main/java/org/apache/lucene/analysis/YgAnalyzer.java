package org.apache.lucene.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;

import com.wuda.analysis.FileDictionaryHandler;

/**
 * lucene analyzer实现,完成分词在lucene中的使用.
 * 
 * @author wuda
 *
 */
public class YgAnalyzer extends Analyzer {

	/**
	 * 词典所在的目录.
	 */
	private String dictDir = null;

	/**
	 * 是否异步加载词典.
	 */
	private boolean isAsynLoadDict = true;

	/**
	 * yg analyzer是一个机遇词典的分析器,所以指定一个词典目录来构造实例.
	 * 
	 * @param dictDir
	 *            词典所在的目录
	 */
	public YgAnalyzer(String dictDir) {
		this.dictDir = dictDir;
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		YgTokenizer source = new YgTokenizer(dictDir,isAsynLoadDict);
		TokenStream tok = new LowerCaseFilter(source);
		TokenStreamComponents components = new TokenStreamComponents(source, tok);
		return components;
	}

	/**
	 * @return the dictDir
	 */
	public String getDictDir() {
		return dictDir;
	}

	/**
	 * @return the isAsynLoadDict
	 */
	public boolean isAsynLoadDict() {
		return isAsynLoadDict;
	}

	/**
	 * @param isAsynLoadDict
	 *            the isAsynLoadDict to set
	 */
	public void setAsynLoadDict(boolean isAsynLoadDict) {
		this.isAsynLoadDict = isAsynLoadDict;
	}
	
	/**
	 * 提前先加载词典,异步的,不会有任何阻塞.
	 */
	public void loadDictAdvance(){
		FileDictionaryHandler dictionaryHandler=new FileDictionaryHandler();
		dictionaryHandler.setDirectory(dictDir);
		dictionaryHandler.setIsAsynLoadDict(true);
		dictionaryHandler.getDictionary();
	}
}
