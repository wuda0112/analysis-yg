package com.wuda.analysis;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wuda.Constant;

/**
 * 从文本中获取词典.有几个重要的内容需要了解
 * <ol>
 * <li>词典是延迟加载的,即调用{@link #getDictionary()}
 * 方法返回的词典,在返回的瞬间可能是一个空词典,也可能只包含部分单词,但是在此后的时候会继续加载</li>
 * <li>推荐在全局只使用此类的一个实例,但是如果是多个实例,然后调用{@link #getDictionary()}方法获取词典,返回的词典在全局也是
 * <Strong>同一个对象</Strong>,因为保存单词的对象是一个静态成员变量</li>
 * <li>即使在多个实例,多线程环境中多次调用{@link #getDictionary()}方法,
 * <Strong>真正去词典文件中加载词典也只会是一次</Strong></li>
 * </ol>
 * 
 * @author wuda
 *
 */
public class FileDictionaryHandler implements DictionaryHandler {

	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 词典,使用静态final修饰,目的是保证词典全量的只被加载一次(当然不是static final就有这样的效果,靠代码保证),增量的才会实时加载.
	 */
	private static final Trie dict = new Trie();

	/**
	 * 实际上真正去文件中加载词典的次数,即使{@link #loadAll()}方法被调用多次,也不一定真正去文件中加载单词.
	 */
	private static AtomicInteger actualLoadDictCount = new AtomicInteger(0);

	/**
	 * 保存词典的目录.只能是目录.
	 */
	private String directory;

	/**
	 * 是否异步加载词典.
	 */
	private boolean isAsynLoadDict = true;

	/**
	 * 是否异步加载词典.
	 * 
	 * @return true-如果是
	 */
	public boolean isAsynLoadDict() {
		return isAsynLoadDict;
	}

	/**
	 * 决定是否异步加载词典.只有在{@link #getDictionary()}方法调用之前设置才有效.
	 * 
	 * @return true-如果是
	 */
	public void setIsAsynLoadDict(boolean isAsynLoadDict) {
		this.isAsynLoadDict = isAsynLoadDict;
	}

	/**
	 * 加载所有的单词.多线程延迟加载,即当{@link #getDictionary()}
	 * 返回词典对象后,可能也只包含部分单词,因为还在加载中.并且很重要的一点是,词典只会加载一次,即使多次调用此方法,或者多线程调用此方法,
	 * 都只会真正意义上的去加载词典文件一次.
	 * 
	 */
	private void loadAll() {
		if (directory == null || directory.isEmpty()) {
			throw new DictionaryHandleException("请先指定词典所在的目录");
		}
		File dictDir = new File(directory);
		if (dictDir.isDirectory() == false) {
			throw new DictionaryHandleException("不是目录?或者此目录不存在?");
		}
		if (tryGetLoadAllDictChance() == false) {// 没有获得机会(其他线程已经加载了词典,不需要重复加载词典)
			return;
		}
		File[] files = dictDir.listFiles();
		if (files == null || files.length < 1) {
			return;
		}
		/**
		 * 多线程加载单词.
		 */
		ExecutorService executors = null;
		if (isAsynLoadDict) {
			executors = Executors.newCachedThreadPool();
		}
		for (File file : files) {
			Worker worker = new Worker();
			worker.setFile(file);
			if (isAsynLoadDict && executors != null) {
				executors.execute(worker);
			} else {
				worker.run();
			}

		}
		if (isAsynLoadDict && executors != null) {
			executors.shutdown();
		}
	}

	/**
	 * 从文件中加载词典.
	 * 
	 * @param file
	 *            文件
	 */
	private void loadFromFile(File file) {
		if (file == null || file.isDirectory()) {
			return;
		}
		List<String> lines = null;
		try {
			lines = FileUtils.readLines(file, Constant.CHARSET_UTF8);
		} catch (IOException e) {
			logger.warn(e.getMessage() + "\t解析词典文件错误", e);
		}
		if (lines == null || lines.isEmpty()) {
			return;
		}
		String tokenType = lines.get(0);// 第一行是【TokenType】,表明这个文件中的所有token(单词)都是这种类型.
		if (tokenType == null) {
			StringBuilder builder = new StringBuilder("文件的第一行必须只能指定单词类型..如果已经指定了单词类型,则有可能是编码导致,请设置编码为【UTF-8无BOM格式编码】.");
			builder.append("file:");
			builder.append(file.toString());
			String msg = builder.toString();
			logger.warn(msg);
			throw new UnknownTokenTypeException(msg);
		}
		synchronized (this) {
			String line = null;
			for (int i = 1; i < lines.size(); i++) {
				line = lines.get(i);
				if (line != null && !line.isEmpty()) {
					dict.add(line, tokenType);
				}
				lines.set(i, null);// 释放内存,类似于list.clear()
			}
		}
		lines = null;// 释放内存
	}

	/**
	 * 加载改变的单词.
	 * 
	 */
	private void loadChanged() {
		if (directory == null) {
			return;
		}
		Path dir = Paths.get(directory);
		WatchService watcher = null;
		try {
			watcher = FileSystems.getDefault().newWatchService();
			dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
		}
		for (;;) {
			WatchKey watchKey = null;
			try {
				watchKey = watcher.take();
			} catch (InterruptedException e) {
				logger.warn(e.getMessage(), e);
			}
			if (watchKey != null) {
				for (WatchEvent<?> event : watchKey.pollEvents()) {
					WatchEvent.Kind<?> kind = event.kind();
					if (kind == StandardWatchEventKinds.ENTRY_CREATE || kind == StandardWatchEventKinds.ENTRY_MODIFY) {
						// The filename is the
						// context of the event.
						@SuppressWarnings("unchecked")
						WatchEvent<Path> ev = (WatchEvent<Path>) event;
						Path filename = ev.context();
						File file = new File(directory, filename.toString());
						loadFromFile(file);

						/**
						 * log
						 */
						StringBuilder builder = new StringBuilder("词典目录发生变化,kind=");
						builder.append(kind.name());
						builder.append(",变化的是：");
						builder.append(filename.toString());
						logger.info(builder.toString());
					}
				}
				watchKey.reset();// 重新监听
			}
		}

	}

	/**
	 * 获取词典.同一个jvm只会获取到同一个词典对象,词典也只会加载一次(变化的内容会一直加载),并且是异步延迟加载的.
	 */
	@Override
	public Trie getDictionary() {
		if (actualLoadDictCount.get() == 0) {
			loadAll();
			/**
			 * 监控词典目录.
			 */
			startLoadChangedThread();
		}
		return dict;
	}

	/**
	 * 尝试获取加载所有词典的机会.
	 * 
	 * @return true-获得了机会,false-没有机会
	 */
	private final boolean tryGetLoadAllDictChance() {
		int count;
		while ((count = actualLoadDictCount.get()) == 0) {
			if (actualLoadDictCount.compareAndSet(count, count + 1)) {
				return true;
			}
		}
		return false;
	}

	private class Worker implements Runnable {
		private File file = null;

		/**
		 * @param file
		 *            the file to set
		 */
		public void setFile(File file) {
			this.file = file;
		}

		@Override
		public void run() {
			loadFromFile(file);
		}
	}

	/**
	 * @return the directory
	 */
	public String getDirectory() {
		return directory;
	}

	/**
	 * @param directory
	 *            the directory to set
	 */
	public void setDirectory(String directory) {
		this.directory = directory;
	}

	/**
	 * 启动线程,监控词典目录.
	 */
	private void startLoadChangedThread() {
		Thread thread = new WatchDirThread();
		thread.setName(Constant.ANALYSIS_NAME + "-watch-dict-dir");
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * 监听词典目录.
	 * 
	 * @author wuda
	 *
	 */
	class WatchDirThread extends Thread {

		@Override
		public void run() {
			loadChanged();
		}
	}
}
