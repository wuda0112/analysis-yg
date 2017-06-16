/**
 * 提供文本分析的方法.
 */
/**
 * {@link TextHandler}和{@link TextHandlerFilter}组成文本处理链,在此链中{@link TextHandler}
 * 只能处于链的最底端,它以最原始的文本内容作为输入,而{@link TextHandlerFilter}以{@link TextHandler}
 * 作为输入,表示继续链式的处理{@link TextHandler}提供的文本{@link TextHandler}.{@link TextHandler}
 * 将处理后的结果放入 {@link TextHandlerSharedAttribute}中,供后续的{@link TextHandlerFilter}
 * 或者其他调用者使用.在一个文本处理链中,{@link TextHandlerSharedAttribute}贯穿整个链,在链中的所有handler都是访问
 * {@link TextHandlerSharedAttribute}
 * 的同一个实例,这样就避免了多次创建对象的问题,也充分利用了java的对象引用特性.消费者调用
 * {@link TextHandler#incrementToken()}或者
 * {@link TextHandlerFilter#incrementToken()}
 * 方法来询问:是否还有更多的token(token不一定是单词,也可以是句子,文本片段等等),如果返回<code>true</code>
 * ,则表示有,这个token就被放到{@link com.wuda.analysis.TextHandlerSharedAttribute}中.
 * 
 * @author wuda
 */
package com.wuda.analysis;