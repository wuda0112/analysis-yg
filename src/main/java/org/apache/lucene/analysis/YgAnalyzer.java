package org.apache.lucene.analysis;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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
	private boolean enumerateAll = true;

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		YgTokenizer source = new YgTokenizer();
		source.setEnumerateAll(enumerateAll);
		PatternCaptureGroupAndReplaceTokenFilter patternCaptureGroupAndReplaceTokenFilter = null;
		getPatterns();
		getPatternNames();
		if (patternArray != null && patternNamesArray != null && patternArray.length == patternNamesArray.length) {
			patternCaptureGroupAndReplaceTokenFilter = new PatternCaptureGroupAndReplaceTokenFilter(source, false, true,
					getPatterns(), getPatternNames());
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
	 * @param stopwordFileName
	 *            停止词文件文件名称
	 * @return factory
	 * @throws IOException
	 *             处理文件异常
	 */
	private StopFilterFactory getStopFilterFactory(String stopwordFileName) throws IOException {
		if (stopwordFileName == null) {
			return null;
		}
		File file = new File(FileDictionaryHandler.directory, stopwordFileName);
		if (!file.exists() || file.isDirectory()) {
			return null;
		}
		Map<String, String> filterArgs = new HashMap<String, String>();
		filterArgs.put("words", stopwordFileName);
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
				stopFilterFactory = getStopFilterFactory(Constant.stopword_file_name);
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

	/**
	 * 生成pattern的次数.
	 */
	private static AtomicInteger actualGenPatternCount = new AtomicInteger(0);

	/**
	 * 尝试获取生成正则表达式的机会.
	 * 
	 * @return true-获得了机会,false-没有机会
	 */
	private final boolean tryGetGenPatternChance() {
		int count;
		while ((count = actualGenPatternCount.get()) == 0) {
			if (actualGenPatternCount.compareAndSet(count, count + 1)) {
				return true;
			}
		}
		return false;
	}

	private static LinkedList<Pattern> patterns = new LinkedList<>();
	private static Pattern[] patternArray = null;
	private static LinkedList<String> patternNames = new LinkedList<>();
	private static String[] patternNamesArray = null;

	static {
		Pattern pattern1 = Pattern.compile("([a-zA-z\\d]+)");// 英文和数字
		patterns.addLast(pattern1);
		patternNames.addLast(Constant.type_enn);
	}

	private Pattern[] getPatterns() {
		if (patternArray != null && patternArray.length == 2) {
			return patternArray;
		}
		if (FileDictionaryHandler.quantifierLoadComplete.get() == true && !patternNames.contains(Constant.type_dw)) {
			if (tryGetGenPatternChance()) {
				Pattern quantifierPattern = Pattern.compile(getQuantifierPatternRegex(), Pattern.CASE_INSENSITIVE);
				patterns.addFirst(quantifierPattern);
				patternNames.addFirst(Constant.type_dw);
			}
		}
		patternArray = new Pattern[patterns.size()];
		return patterns.toArray(patternArray);
	}

	private String[] getPatternNames() {
		if (patternNamesArray != null && patternNamesArray.length == 2) {
			return patternNamesArray;
		}
		patternNamesArray = new String[patternNames.size()];
		return patternNames.toArray(patternNamesArray);
	}

	/**
	 * 获取数量词正则表达式.
	 * 
	 * @return 数量词正则表达式
	 */
	private String getQuantifierPatternRegex() {
		FileDictionaryHandler handler = new FileDictionaryHandler();
		List<String> quantifiers = handler.getQuantifiers();
		Collections.sort(quantifiers, new Comparator<String>() {// 按单位长短排序,长的拍前面
			@Override
			public int compare(String o1, String o2) {
				if (o1.length() > o2.length()) {
					return -1;
				} else if (o1.length() < o2.length()) {
					return 1;
				}
				return 0;
			}
		});
		StringBuilder regex = new StringBuilder("(");
		regex.append(Constant.default_numeral_regex);
		if (quantifiers == null || quantifiers.isEmpty()) {
			regex.append(Constant.default_quantifier_regex);
		} else {
			regex.append("(?:");
			for (String quantifier : quantifiers) {
				if (quantifier == null) {
					continue;
				}
				quantifier = quantifier.trim();
				if (quantifier.equals("(") || quantifier.equals(")")) {
					continue;
				}
				regex.append(quantifier);
				regex.append("|");
			}
			regex.deleteCharAt(regex.length() - 1);// 删除最后一个“|”
			regex.append(")");
		}
		regex.append(")");
		return regex.toString();
	}
}
