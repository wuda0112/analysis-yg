package com.wuda.analysis;

import java.io.IOException;

/**
 * 文本处理器.可以和{@link TextHandler}组成文本处理链,而在此链中{@link TextHandler} 只能处于链的最底端,
 * {@link TextHandlerFilter}可以是任意多个.{@link TextHandler}将处理后的结果放入
 * {@link TextHandlerSharedAttribute}中,然后{@link TextHandlerFilter}从
 * {@link TextHandlerSharedAttribute}获取token,处理后再将结果放入
 * {@link TextHandlerSharedAttribute}中,供下一个{@link TextHandlerFilter}使用.
 * 
 * @author wuda
 *
 */
public abstract class TextHandlerFilter {

	/** The source of tokens for this filter. */
	protected final TextHandler input;

	/**
	 * 保存处理结果.
	 */
	protected TextHandlerSharedAttribute attribute;

	/**
	 * 以{@link TextHandler}作为输入,然后将处理结果放入{@link TextHandlerSharedAttribute}
	 * 中,供后续的 {@link TextHandlerFilter}或者其他调用者使用.
	 * 
	 * @param input
	 *            The TextHandler
	 * @param attribute
	 *            将每次的处理结果放入attribute中
	 */
	protected TextHandlerFilter(TextHandler input, TextHandlerSharedAttribute attribute) {
		this.input = input;
		this.attribute = attribute;
	}

	/**
	 * This method is called by a consumer before it begins consumption using
	 * {@link #incrementToken()}.
	 * <p>
	 * Resets this TextHandler to a clean state. Stateful implementations must
	 * implement this method so that they can be reused, just as if they had
	 * been created fresh.
	 * <p>
	 * If you override this method, always call {@code super.reset()}, otherwise
	 * some internal state will not be correctly reset.
	 * 
	 * @throws IOException
	 *             IOException
	 */
	public void reset() throws IOException {
		input.reset();
	}

	/**
	 * 调用者使用此方法推动文本处理器下一个token.当返回true时,表示有token,则通过
	 * {@link TextHandlerSharedAttribute}获取token.
	 * 
	 * @return true-如果还有token;false-没有可用的token
	 * @throws IOException
	 *             在处理文本时io异常
	 */
	public abstract boolean incrementToken() throws IOException;

	/**
	 * 关闭资源.
	 * 
	 * @throws IOException
	 *             当{@link #input}关闭时,出现IO异常
	 */
	public void close() throws IOException {
		input.close();
	}

}
