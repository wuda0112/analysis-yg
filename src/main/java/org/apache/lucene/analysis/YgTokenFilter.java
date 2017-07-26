package org.apache.lucene.analysis;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import com.wuda.Constant;

public class YgTokenFilter extends TokenFilter {

	private CharTermAttribute charTermAttr = addAttribute(CharTermAttribute.class);
	private TypeAttribute typeAttr = addAttribute(TypeAttribute.class);

	private LinkedList<String> tokens = null;
	private Iterator<String> iterator = null;
	private String type = null;

	protected YgTokenFilter(TokenStream input) {
		super(input);
	}

	@Override
	public final boolean incrementToken() throws IOException {
		if (fillAttr()) {
			return true;
		}
		while (input.incrementToken()) {
			fillToken();
			if (fillAttr()) {
				return true;
			} else {
				continue;
			}
		}
		return false;
	}

	private void fillToken() {
		doClear();
		tokens = new LinkedList<>();
		String type = typeAttr.type();
		this.type = type;
		String token = charTermAttr.toString();
		if (type != null && type.equalsIgnoreCase(Constant.fixed_token_type_not_a_word)) {
			char[] charArray = token.toCharArray();
			for (char c : charArray) {
				if (c != PatternCaptureGroupAndReplaceTokenFilter.replacement) {
					tokens.addLast(new String(new char[] { c }));
				}
			}
		} else if (type != null
				&& (type.equalsIgnoreCase(Constant.fixed_token_type_dw) || type.equalsIgnoreCase(Constant.fixed_token_type_enn))) {
			/**
			 * 由正则表达式提取的内容.
			 */
			if (token.indexOf(PatternCaptureGroupAndReplaceTokenFilter.replacement) == -1) {
				tokens.addLast(token);
			}
		} else {
			tokens.addLast(token);
		}
		iterator = tokens.iterator();
	}

	private void doClear() {
		if (tokens != null) {
			tokens.clear();
			tokens = null;
		}
		if (iterator != null) {
			iterator = null;
		}
		this.type = null;
	}

	private boolean fillAttr() {
		if (iterator != null && iterator.hasNext()) {
			clearAttributes();
			charTermAttr.append(iterator.next());
			typeAttr.setType(type);
			return true;
		}
		return false;
	}

	@Override
	public void close() throws IOException {
		input.close();
		doClear();
	}

}
