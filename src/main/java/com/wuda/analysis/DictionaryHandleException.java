package com.wuda.analysis;

/**
 * 处理词典时的一些异常信息.
 * 
 * @author wuda
 *
 */
public class DictionaryHandleException extends RuntimeException{

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;
	
	/** Constructs a new runtime exception with {@code null} as its
     * detail message.  The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     */
    public DictionaryHandleException() {
        super();
    }
	
	/** Constructs a new runtime exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param   message   the detail message. The detail message is saved for
     *          later retrieval by the {@link #getMessage()} method.
     */
    public DictionaryHandleException(String message) {
        super(message);
    }

}
