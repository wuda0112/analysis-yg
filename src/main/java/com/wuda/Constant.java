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
	public final static String stopword_file_name = "stopwords.dict";
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
	public final static Pattern fixed_token_type_enn_regex = Pattern.compile("([-_a-zA-z\\d\\.]+)");// 英文和数字

	/**
	 * 单位,比如:1毫升.
	 */
	public final static String fixed_token_type_dw = "dw";
	/**
	 * 提取单位的正则表达式.
	 */
	public final static Pattern fixed_token_type_dw_regex = Pattern.compile(
			"(\\d+\\.?\\d*(?:平方公尺|平方公里|平方分米|平方厘米|平方英寸|平方英尺|平方英里|立方公尺|立方分米|立方厘米|立方英寸|立方英尺|平方码|平方米|立方码|立方米|ml|kg|毫升|小包|xl|毫安|cm|cc|公分|千克|克拉|mm|km|周岁|分钟|个月|世纪|位数|像素|公亩|公克|公升|公尺|公担|公斤|公里|公顷|分米|加仑|千米|厘米|周年|小时|平方|平米|年代|年级|月份|毫米|毫克|海里|点钟|盎司|秒钟|英亩|英寸|英尺|英里|阶段|瓶|本|度|页|斤|层|卷|张|架|粒|提|套|个|w|m|号|支|6|l|包|孔|件|岁|g|克|袋|寸|升|双|k|盒|元|f|片|米|罐|月|只|听|℃|a|抽|种|n|册|枚|款|条|杯|桶|v|份|箱|贴|年|例|块|色|对|倍|丈|下|世|两|中|串|亩|人|介|付|代|任|伏|伙|位|具|出|刀|分|划|列|则|刻|剂|剑|副|勺|匙|匹|区|厅|厘|发|口|句|台|叶|名|吨|员|周|品|回|团|圆|圈|地|场|坪|堆|声|壶|处|夜|大|天|头|女|字|宗|室|家|封|尊|尺|尾|局|届|师|帧|幅|幕|幢|座|式|引|成|战|截|户|房|所|扇|手|打|批|把|折|担|拍|招|拨|拳|指|掌|排|撮|文|斗|方|族|日|时|曲|期|朵|村|束|来|枝|枪|柄|柜|栋|栏|株|样|根|格|案|桌|档|桩|梯|棵|楼|次|步|段|毛|毫|池|洲|派|滴|炮|点|版|环|班|瓣|生|男|画|界|盆|盏|盘|相|眼|石|码|碗|碟|磅|科|秒|窝|站|章|笔|等|筐|筒|篇|篓|篮|簇|类|级|组|维|缕|缸|网|群|股|脚|船|艇|艘|节|行|角|言|课|起|趟|路|车|转|轮|辆|辈|连|通|遍|部|里|重|针|钟|钱|锅|门|间|队|隅|集|顶|顷|项|顿|颗|餐|首))",
			Pattern.CASE_INSENSITIVE);// 单位

	/**
	 * not a word.
	 */
	public final static String fixed_token_type_not_a_word = "notw";

	/**
	 * 缓存词典的map的初始化容量.
	 */
	public final static int initialCapacity = 60000000;

}
