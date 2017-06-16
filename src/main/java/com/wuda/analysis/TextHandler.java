package com.wuda.analysis;

import java.io.IOException;
import java.io.Reader;

/**
 * 文本处理器.可以和{@link TextHandlerFilter}组成文本处理链,而在此链中{@link TextHandler}
 * 只能处于链的最底端,它以最原始的文本内容作为输入.{@link TextHandler}将处理后的结果放入
 * {@link TextHandlerSharedAttribute}中,供后续的{@link TextHandlerFilter}或者其他调用者使用.
 * 
 * @author wuda
 *
 */
public abstract class TextHandler {

	/** The text source for this TextHandler. */
	protected Reader input = ILLEGAL_STATE_READER;

	/** Pending reader: not actually assigned to input until reset() */
	private Reader inputPending = ILLEGAL_STATE_READER;

	/**
	 * 保存处理结果.
	 */
	protected TextHandlerSharedAttribute attribute;

	/**
	 * 以原始的文本作为输入,然后将处理结果放入{@link TextHandlerSharedAttribute}中,供后续的
	 * {@link TextHandlerFilter}或者其他调用者使用.
	 * 
	 * @param attribute
	 *            将每次的处理结果放入attribute中
	 */
	protected TextHandler(TextHandlerSharedAttribute attribute) {
		this.attribute = attribute;
	}

	/**
	 * Expert: Set a new reader on the TextHandler. Typically, an consumer will
	 * use this to re-use a previously created TextHandler.
	 */
	public final void setReader(Reader input) {
		if (input == null) {
			throw new NullPointerException("input must not be null");
		} else if (this.input != ILLEGAL_STATE_READER) {
			throw new IllegalStateException("TextHandler contract violation: close() call missing");
		}
		this.inputPending = input;
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
		input = inputPending;
		inputPending = ILLEGAL_STATE_READER;
		attribute.clearAttributes();
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
		inputPending = input = ILLEGAL_STATE_READER;
		attribute.clearAttributes();
	}

	private static final Reader ILLEGAL_STATE_READER = new Reader() {
		@Override
		public int read(char[] cbuf, int off, int len) {
			throw new IllegalStateException("TextHandler contract violation: reset()/close() call missing, "
					+ "reset() called multiple times, or subclass does not call super.reset(). "
					+ "Please see Javadocs of TextHandler class for more information about the correct consuming workflow.");
		}

		@Override
		public void close() {
		}
	};

}
