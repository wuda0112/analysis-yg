package org.apache.lucene.analysis;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.CharsRefBuilder;

import com.wuda.Constant;

/**
 * CaptureGroup uses Java regexes to emit multiple tokens - one for each capture
 * group in one or more patterns.
 *
 * 首先使用正则表达式捕获组提取token,然后使用英文的逗号","来替换这些满足正则表达式捕获组的token.如果
 * <i>preserveOriginal</i>设置为true,则返回原始的token;如果<i>returnReplaced</i>
 * 设置为true,则返回替换后的token. 例如
 * 
 * <pre>
 * 正则表达式是:"(ui)",当与字符串"abuiab"匹配时,返回token "ui",并且原始字符串被替换为"ab,,ab";
 * 如果preserveOriginal为true,则还会返回token "abuiab",如果returnReplaced为true,则被替换后的"ab,,ab"也会返回.
 * 
 * </pre>
 * 
 */
public class PatternCaptureGroupAndReplaceTokenFilter extends TokenFilter {

	private final CharTermAttribute charTermAttr = addAttribute(CharTermAttribute.class);
	private final PositionIncrementAttribute posAttr = addAttribute(PositionIncrementAttribute.class);
	private final TypeAttribute typeAttr = addAttribute(TypeAttribute.class);
	private State state;
	private final Matcher[] matchers;
	private final CharsRefBuilder spare = new CharsRefBuilder();
	private final int[] groupCounts;
	private final boolean preserveOriginal;
	private final boolean returnReplaced;
	private int[] currentGroup;
	private int currentMatcher;
	private String[] patternNames;

	private AtomicBoolean everMatch = new AtomicBoolean(false);

	public static final char replacement = ',';

	/**
	 * token filter
	 * 
	 * @param input
	 *            the input {@link TokenStream}
	 * @param preserveOriginal
	 *            set to true to return the original token even if one of the
	 *            patterns matches
	 * @param returnReplaced
	 *            设置为true时,返回替换后的token
	 * @param patterns
	 *            an array of {@link Pattern} objects to match against each token
	 * @param patternNames
	 *            pattern 的名称,如果需要知道当前的token是匹配哪一个pattern返回的,就可以通过
	 *            {@link TypeAttribute#type()}获取,此方法将返回pattern的name.
	 */

	public PatternCaptureGroupAndReplaceTokenFilter(TokenStream input, boolean preserveOriginal, boolean returnReplaced,
			Pattern[] patterns, String[] patternNames) {
		super(input);
		this.preserveOriginal = preserveOriginal;
		this.returnReplaced = returnReplaced;
		this.matchers = new Matcher[patterns.length];
		this.groupCounts = new int[patterns.length];
		this.currentGroup = new int[patterns.length];
		for (int i = 0; i < patterns.length; i++) {
			this.matchers[i] = patterns[i].matcher("");
			this.groupCounts[i] = this.matchers[i].groupCount();
			this.currentGroup[i] = -1;
		}
		this.patternNames = patternNames;
	}

	private boolean nextCapture() {
		int min_offset = Integer.MAX_VALUE;
		currentMatcher = -1;
		Matcher matcher;

		for (int i = 0; i < matchers.length; i++) {
			matcher = matchers[i];
			if (currentGroup[i] == -1) {
				currentGroup[i] = matcher.find() ? 1 : 0;
			}
			if (currentGroup[i] != 0) {
				while (currentGroup[i] < groupCounts[i] + 1) {
					final int start = matcher.start(currentGroup[i]);
					final int end = matcher.end(currentGroup[i]);
					if (start == end || preserveOriginal && start == 0 && spare.length() == end) {
						currentGroup[i]++;
						continue;
					}
					if (start < min_offset) {
						min_offset = start;
						currentMatcher = i;
					}
					break;
				}
				if (currentGroup[i] == groupCounts[i] + 1) {
					currentGroup[i] = -1;
					i--;
				}
			}
		}
		if (currentMatcher != -1) {
			everMatch.compareAndSet(false, true);
		}
		return currentMatcher != -1;
	}

	@Override
	public boolean incrementToken() throws IOException {

		if (currentMatcher != -1 && nextCapture()) {
			assert state != null;
			clearAttributes();
			restoreState(state);
			final int start = matchers[currentMatcher].start(currentGroup[currentMatcher]);
			final int end = matchers[currentMatcher].end(currentGroup[currentMatcher]);

			posAttr.setPositionIncrement(0);
			charTermAttr.copyBuffer(spare.chars(), start, end - start);
			currentGroup[currentMatcher]++;

			setTypeAttr();

			replaceCapture(start, end);

			return true;
		}

		if (returnReplaced && everMatch.get() && spare.length() > 0) {// 返回替换后的内容
			clearAttributes();
			charTermAttr.copyBuffer(spare.chars(), 0, spare.length());
			typeAttr.setType(Constant.NOT_A_WORD);
			spare.clear();
			everMatch.set(false);
			return true;
		}

		if (!input.incrementToken()) {
			return false;
		}else {
			String type=typeAttr.type();
			if(type==null || !type.equals(Constant.NOT_A_WORD)) {//是单词,不进行正则表达式匹配
				currentMatcher=-1;
				everMatch.set(false);
				return true;
			}
		}

		char[] buffer = charTermAttr.buffer();
		int length = charTermAttr.length();
		spare.copyChars(buffer, 0, length);
		state = captureState();

		for (int i = 0; i < matchers.length; i++) {
			matchers[i].reset(spare.get());
			currentGroup[i] = -1;
		}

		if (preserveOriginal) {
			currentMatcher = 0;
		} else if (nextCapture()) {
			final int start = matchers[currentMatcher].start(currentGroup[currentMatcher]);
			final int end = matchers[currentMatcher].end(currentGroup[currentMatcher]);

			// if we start at 0 we can simply set the length and save the copy
			if (start == 0) {
				charTermAttr.setLength(end);
			} else {
				charTermAttr.copyBuffer(spare.chars(), start, end - start);
			}
			currentGroup[currentMatcher]++;

			setTypeAttr();

			replaceCapture(start, end);
		}
		return true;

	}

	@Override
	public void reset() throws IOException {
		super.reset();
		state = null;
		currentMatcher = -1;
	}

	private void replaceCapture(int start, int end) {
		for (int j = start; j < end; j++) {
			spare.setCharAt(j, replacement);
		}
	}

	private void setTypeAttr() {
		if (patternNames != null) {
			if (patternNames.length != matchers.length) {
				throw new IllegalStateException("pattren和它的名称不对应");
			}
			typeAttr.setType(patternNames[currentMatcher]);
		}

	}

	/**
	 * @return the replacement
	 */
	public char getReplacement() {
		return replacement;
	}

}
