package org.elasticsearch;

import org.apache.lucene.analysis.YgAnalyzer;
import org.apache.lucene.analysis.YgTokenizer;
import org.elasticsearch.common.settings.Settings;

/**
 * 不是elasticsearch插件所必须的类,只是实现插件时自己定义的工具类.
 * 
 * @author wuda
 *
 */
public class YgUtil {

	/**
	 * 如果把此分词组件设置成[defualt]时,在配置文件中词典目录的参数名称.
	 */
	private final static String yg_dict_dir_default = "index.analysis.analyzer.default.dict_dir";

	/**
	 * 如果把此分词组件设置成[yg]时,在配置文件中词典目录的参数名称.
	 */
	private final static String yg_dict_dir_yg = "index.analysis.analyzer.yg.dict_dir";

	/**
	 * 在配置文件中除去分组后词典目录的参数名称(被es的参数搞晕了).
	 */
	private final static String yg_dict_dir_short_name = "dict_dir";

	/**
	 * 如果把此分词组件设置成[defualt]时,在配置文件中是否异步加载词典的参数名称.
	 */
	private final static String yg_is_asyn_load_dict_default = "index.analysis.analyzer.default.is_asyn_load_dict";

	/**
	 * 如果把此分词组件设置成[yg]时,在配置文件中是否异步加载词典的参数名称.
	 */
	private final static String yg_is_asyn_load_dict_yg = "index.analysis.analyzer.yg.is_asyn_load_dict";

	/**
	 * 在配置文件中除去分组后是否异步加载词典的参数名称(被es的参数搞晕了).
	 */
	private final static String yg_is_asyn_load_dict_short_name = "is_asyn_load_dict";

	/**
	 * 获取YgAnalyzer实例.
	 * 
	 * @param settings
	 *            从elasticsearch配置文件中获取的配置信息
	 * @return YgAnalyzer实例
	 */
	public static YgAnalyzer getYgAnalyzer(Settings settings) {
		String dictDir = getDictDir(settings);// 获取词典目录
		YgAnalyzer analyzer = new YgAnalyzer(dictDir);
		boolean isAsynLoadDict = isAsynLoadDict(settings);// 是否异步加载词典
		analyzer.setAsynLoadDict(isAsynLoadDict);
		if (isAsynLoadDict) {
			analyzer.loadDictAdvance();// 提前加载词典,es启动时,此方法就会执行
		}
		return analyzer;
	}

	/**
	 * 获取YgTokenizer实例.
	 * 
	 * @param settings
	 *            从elasticsearch配置文件中获取的配置信息
	 * @return YgTokenizer实例
	 */
	public static YgTokenizer getYgTokenizer(Settings settings) {
		String dictDir = getDictDir(settings);// 获取词典目录
		boolean isAsynLoadDict = isAsynLoadDict(settings);// 是否异步加载词典
		YgTokenizer tokenizer = new YgTokenizer(dictDir, isAsynLoadDict);
		return tokenizer;
	}

	/**
	 * 是否异步加载词典.
	 * 
	 * @param settings
	 *            配置信息
	 * @return true-是,false-不是
	 */
	private static boolean isAsynLoadDict(Settings settings) {
		String isAsynLoadDict = settings.get(yg_is_asyn_load_dict_default);
		if (isAsynLoadDict == null) {
			isAsynLoadDict = settings.get(yg_is_asyn_load_dict_yg);
		}
		if (isAsynLoadDict == null) {
			isAsynLoadDict = settings.get(yg_is_asyn_load_dict_short_name);
		}
		if (isAsynLoadDict != null && isAsynLoadDict.isEmpty() == false) {
			isAsynLoadDict = isAsynLoadDict.trim();
			if (isAsynLoadDict.equalsIgnoreCase("true") || isAsynLoadDict.equalsIgnoreCase("false")) {
				return Boolean.getBoolean(isAsynLoadDict);
			}
		}
		return true;// 默认true
	}

	/**
	 * 获取词典目录
	 * 
	 * @param settings
	 *            配置信息
	 * @return 词典所在目录,null-如果没有配置
	 */
	private static String getDictDir(Settings settings) {
		String dictDir = settings.get(yg_dict_dir_default);
		if (dictDir == null) {
			dictDir = settings.get(yg_dict_dir_yg);
		}
		if (dictDir == null) {
			dictDir = settings.get(yg_dict_dir_short_name);
		}
		return dictDir;
	}

}
