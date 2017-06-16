package org.apache.lucene.analysis;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import com.wuda.analysis.DictBasedTextHandlerFilter;
import com.wuda.analysis.SentenceTextHandler;
import com.wuda.analysis.TextHandlerSharedAttribute;

/**
 * lucene tokenizer实现,完成分词在lucene中的使用.
 * 
 * @author wuda
 *
 */
public class YgTokenizer extends Tokenizer {

	private final CharTermAttribute charTermAttr = addAttribute(CharTermAttribute.class);
	private OffsetAttribute offsetAttr = addAttribute(OffsetAttribute.class);
	private TypeAttribute typeAttr = addAttribute(TypeAttribute.class);
	private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);

	/**
	 * 词典所在的目录.
	 */
	private String dictDir = null;

	/**
	 * 是否异步加载词典.
	 */
	private boolean isAsynLoadDict = true;

	/**
	 * 文本处理器.
	 */
	private DictBasedTextHandlerFilter textHandler = null;
	private SentenceTextHandler bottomHandler = null;
	/**
	 * 文本处理器处理的结果保存在此实例中.
	 */
	private TextHandlerSharedAttribute textHandlerSharedAttribute = null;

	/**
	 * yg tokenizer是一个机遇词典的分析器,所以指定一个词典目录来构造实例.
	 * 
	 * @param dictDir
	 *            词典所在的目录
	 * @param isAsynLoadDict
	 *            是否异步加载词典
	 */
	public YgTokenizer(String dictDir, boolean isAsynLoadDict) {
		super();
		this.dictDir = dictDir;
		this.isAsynLoadDict = isAsynLoadDict;
		tryInitTextHandler();
	}

	@Override
	public boolean incrementToken() throws IOException {
		if (textHandler.incrementToken()) {// 文本处理器表示有token返回
			clearAttributes();
			charTermAttr.append(textHandlerSharedAttribute.getTokenString());
			offsetAttr.setOffset(textHandlerSharedAttribute.getStartOffset(),
					textHandlerSharedAttribute.getEndOffset());
			typeAttr.setType(textHandlerSharedAttribute.getType());
			posIncrAtt.setPositionIncrement(1);
			return true;
		}
		return false;
	}

	private AtomicInteger initTextHandlerCount = new AtomicInteger(0);

	/**
	 * 初始化低层的文本处理器.
	 */
	private boolean tryInitTextHandler() {
		int count = 0;
		while ((count = initTextHandlerCount.get()) == 0) {
			if (initTextHandlerCount.compareAndSet(count, count + 1)) {
				textHandlerSharedAttribute = new TextHandlerSharedAttribute();
				/**
				 * 文本先处理成句子.你肯定很奇怪,说好要处理文本的,但是连文本都没有给此handler,怎么处理?是的,由于lucene
				 * TokenStream API规范,tokenizer是可以重用的,即同一个实例可以处理多次的text
				 * source,因此,同样根据lucene的规范,我们把设置文本的方法放到了reset()方法中.
				 */
				bottomHandler = new SentenceTextHandler(textHandlerSharedAttribute);
				textHandler = new DictBasedTextHandlerFilter(bottomHandler, textHandlerSharedAttribute);// 从句子中获取分词
				textHandler.loadDictFrom(dictDir, isAsynLoadDict);// 如果不是异步加载,就不能在构造方法中调用此方法
				return true;
			}
		}
		return false;
	}

	@Override
	public final void close() throws IOException {
		super.close();
		textHandler.close();
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		bottomHandler.setReader(input);// 因为在lucene规范中,Tokenizer是可以重用的(重用的意思是重新设置一个文本),而重用之前必须调用reset方法
		textHandler.reset();
	}

	/**
	 * @return the dictDir
	 */
	public String getDictDir() {
		return dictDir;
	}

	/**
	 * @return the isAsynLoadDict
	 */
	public boolean isAsynLoadDict() {
		return isAsynLoadDict;
	}
}
