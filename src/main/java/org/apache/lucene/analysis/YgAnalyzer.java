package org.apache.lucene.analysis;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.synonym.SynonymFilterFactory;
import org.apache.lucene.analysis.util.FilesystemResourceLoader;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wuda.Constant;
import com.wuda.analysis.FileDictionaryHandler;

/**
 * lucene analyzer实现,完成分词在lucene中的使用.
 * 
 * @author wuda
 *
 */
public class YgAnalyzer extends Analyzer {

	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 同义词factory.
	 */
	public static SynonymFilterFactory synonymFilterFactory = null;
	/**
	 * 同义词factory是否已经创建过.
	 */
	private static final AtomicBoolean synonymFilterFactoryCreated = new AtomicBoolean(false);
	/**
	 * 停止词factory.
	 */
	public static StopFilterFactory stopFilterFactory = null;
	/**
	 * 停止词factory是否已经创建过.
	 */
	private static final AtomicBoolean stopFilterFactoryCreated = new AtomicBoolean(false);

	/**
	 * 是否枚举所有的单词.
	 */
	private boolean enumerateAll = false;

	private static Pattern[] patternArray = null;
	private static String[] patternNamesArray = null;

	static {
		patternArray = new Pattern[2];
		patternNamesArray = new String[2];
		patternArray[0] = Constant.fixed_token_type_enn_regex;
		patternNamesArray[0] = Constant.fixed_token_type_enn;

		patternArray[1] = Constant.fixed_token_type_dw_regex;
		patternNamesArray[1] = Constant.fixed_token_type_dw;
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		YgTokenizer source = new YgTokenizer();
		source.setEnumerateAll(enumerateAll);
		PatternCaptureGroupAndReplaceTokenFilter patternCaptureGroupAndReplaceTokenFilter = null;
		if (patternArray != null && patternNamesArray != null && patternArray.length == patternNamesArray.length) {
			patternCaptureGroupAndReplaceTokenFilter = new PatternCaptureGroupAndReplaceTokenFilter(source, false, true,
					patternArray, patternNamesArray);
		}
		YgTokenFilter ygTokenFilter = null;
		if (patternCaptureGroupAndReplaceTokenFilter != null) {
			ygTokenFilter = new YgTokenFilter(patternCaptureGroupAndReplaceTokenFilter);
		} else {
			ygTokenFilter = new YgTokenFilter(source);
		}
		TokenStream stopFilter = createStopFilter(ygTokenFilter);// 停止词filter
		LowerCaseFilter lowerCaseFilter = null;
		if (stopFilter != null) {
			lowerCaseFilter = new LowerCaseFilter(stopFilter);
		} else {
			lowerCaseFilter = new LowerCaseFilter(ygTokenFilter);
		}
		TokenStream SynonymFilter = createSynonymFilter(lowerCaseFilter);// 同义词filter
		TokenStream last = null;
		if (SynonymFilter == null) {
			last = lowerCaseFilter;
		} else {
			last = SynonymFilter;
		}
		TokenStreamComponents components = new TokenStreamComponents(source, last);
		return components;
	}

	/**
	 * 获取同义词factory.
	 * 
	 * @param synonymFileName
	 *            同义词文件文件名称
	 * @return factory
	 * @throws IOException
	 *             处理文件异常
	 */
	private SynonymFilterFactory getSynonymFilterFactory(String synonymFileName) throws IOException {
		if (synonymFileName == null) {
			return null;
		}
		File file = new File(FileDictionaryHandler.directory, synonymFileName);
		if (!file.exists() || file.isDirectory()) {
			return null;
		}
		Map<String, String> filterArgs = new HashMap<String, String>();
		filterArgs.put("luceneMatchVersion", Version.LATEST.toString());
		filterArgs.put("synonyms", synonymFileName);
		filterArgs.put("expand", "true");
		SynonymFilterFactory factory = new SynonymFilterFactory(filterArgs);
		factory.inform(new FilesystemResourceLoader(Paths.get(FileDictionaryHandler.directory)));
		return factory;
	}

	/**
	 * 创建一个SynonymFilter.
	 * 
	 * @param input
	 *            TokenStream
	 * @return SynonymFilter,如果出现异常则返回null
	 */
	private TokenStream createSynonymFilter(TokenStream input) {
		try {
			if (synonymFilterFactory == null && synonymFilterFactoryCreated.get() == false) {
				synonymFilterFactory = getSynonymFilterFactory(Constant.synonym_file_name);
				synonymFilterFactoryCreated.compareAndSet(false, true);
			}
		} catch (IOException e) {
			synonymFilterFactoryCreated.compareAndSet(false, true);
			logger.warn("获取SynonymFilterFactory异常," + e.getMessage(), e);
			return null;
		}
		if (synonymFilterFactory != null) {
			return synonymFilterFactory.create(input);
		}
		return null;
	}

	/**
	 * 获取停止词factory.
	 * 
	 * @param stopwordFileNames
	 *            停止词文件文件名称,可以是多个,用英文逗号隔开.
	 * @return factory
	 * @throws IOException
	 *             处理文件异常
	 */
	private StopFilterFactory getStopFilterFactory(String stopwordFileNames) throws IOException {
		if (stopwordFileNames == null) {
			return null;
		}
		Map<String, String> filterArgs = new HashMap<String, String>();
		filterArgs.put("words", stopwordFileNames);
		StopFilterFactory factory = new StopFilterFactory(filterArgs);
		factory.inform(new FilesystemResourceLoader(Paths.get(FileDictionaryHandler.directory)));
		return factory;
	}

	/**
	 * 创建一个StopFilter.
	 * 
	 * @param input
	 *            TokenStream
	 * @return StopFilter,如果出现异常则返回null
	 */
	private TokenStream createStopFilter(TokenStream input) {
		try {
			if (stopFilterFactory == null && stopFilterFactoryCreated.get() == false) {
				StringBuilder builder = new StringBuilder(Constant.stopword_file_name);
				stopFilterFactory = getStopFilterFactory(builder.toString());
				stopFilterFactoryCreated.compareAndSet(false, true);
			}
		} catch (IOException e) {
			stopFilterFactoryCreated.compareAndSet(false, true);
			logger.warn("获取StopFilterFactory异常," + e.getMessage(), e);
			return null;
		}
		if (stopFilterFactory != null) {
			return stopFilterFactory.create(input);
		}
		return null;
	}

	/**
	 * @return the enumerateAll
	 */
	public boolean isEnumerateAll() {
		return enumerateAll;
	}

	/**
	 * @param enumerateAll
	 *            the enumerateAll to set
	 */
	public void setEnumerateAll(boolean enumerateAll) {
		this.enumerateAll = enumerateAll;
	}
}
