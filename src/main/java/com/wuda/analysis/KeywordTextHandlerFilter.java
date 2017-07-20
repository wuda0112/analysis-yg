package com.wuda.analysis;

import java.io.IOException;

/**
 * 只返回{@link TextHandlerSharedAttribute#isWord()}等于<code>true</code>的内容.
 * 
 * @author wd
 *
 */
public class KeywordTextHandlerFilter extends TextHandlerFilter {

	protected KeywordTextHandlerFilter(TextHandler input, TextHandlerSharedAttribute attribute) {
		super(input, attribute);
	}

	@Override
	public boolean incrementToken() throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

}
