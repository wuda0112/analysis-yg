# analysis-yg
中文文本分析，基于词典的中文分词实现.并且完成了对lucene和elasticsearch的中文分词插件支持.
## 有三种用法
1. 只用作文本分析，分词等，此时此时完全不用考虑【org.apache.lucene】和【org.elasticsearch】中的内容，或者干脆一点，删除这两个包的内容，并且删除掉【pom.xml】文件中有关【lucene】和【elasticsearch】中的依赖
2. 用作lucene的analysis插件
3. 用作elasticsearch中文分词插件

## 词典
此组件并没有提供默认的词典，而是需要调用者自己提供，提供的方式就是指定【词典所在的目录】.

- 在词典目录中可以有多个词典文件（【全量】）
- 修改已经存在的词典文件，或者新建词典文件,都可以被检测到（【增量】）

### 【词典文件的格式】
词典文件可以是任意简单的文本,每一行代表一个单词,并且【第一行】的内容是这个【词典文件中所有单词的类型】。

比如下面是一个包含了【4个单词】的词典文件，第一行的【正常词】表明这个词典文件中的所有单词都是【正常词】类型,单词的类型还可以是【敏感词、停止词】等等（根据调用者的需求，任意定义都可以，只要调用者自己清楚即可）,这一行的目的就是为了给单词分类，当定义了这些类型后，调用者自己决定如何处理这些类型的单词，比如分词后返回单词并且返回了单词类型，由调用者自己决定如何处理这一类型单词。

```
正常词   --我是注释,表明文件中所有单词的类型
lucene   --我是单词
elasticsearch   --我是单词
lucene开发   --我是单词
elasticsearch开发   --我是单词
```
# 单纯的用于文本分析

```
TextHandlerSharedAttribute attribute = new TextHandlerSharedAttribute();
		SentenceTextHandler bottomHandler = new SentenceTextHandler(attribute);// 文本先分割成句子
		/* 在bottomHandler和topFilter中间还可以有多个TextHandlerFilter来组成链 */
		DictBasedTextHandlerFilter topFilter = new DictBasedTextHandlerFilter(bottomHandler, attribute);// 在把句子根据词典分词
		int i = 0;
		while (i < 2) { // 调用两次是为了测试重用功能
			i++;
			try {
				String text = "java开发人员,中华人民,你好好人";// 根据你自己的词典的内容测试分词清单
				Reader input = new StringReader(text);

				bottomHandler.setReader(input);
				topFilter.reset(); // 同一个handler链可以重复使用
				System.out.println("\n=============加载词典...====================\n");
				long startTime = System.currentTimeMillis();
				topFilter.loadDictFrom("e:/dict", false); // 从词典目录加载词典,同步加载词典
				long end = System.currentTimeMillis();
				System.out.println(
						"\n===============加载词典完成,用时:" + (end - startTime) + "毫秒================================\n");
				/**
				 * 如果异步加载,代码执行到这里是有可能词典中还没有内容,导致不能正确分词,因此可以在这里等等,让词典加载一会儿.
				 * 如果词典很大的话, 也可以时间设置的久点
				 */
				// Thread.sleep(3000);
				while (topFilter.incrementToken()) {
					System.out.println(attribute.getCharTermString() + "\tstartOffset:" + attribute.getStartOffset()
							+ "\tendOffset:" + attribute.getEndOffset() + "\ttype:" + attribute.getType());

				}
				topFilter.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
```

## 执行以上代码后返回结果
只是截取了一部分结果,看到【type】了吗？这个就是单词的类型，就是你在词典文件的第一行设置的内容，而且单词类型还可以是【多个】,这也很好理解吧，你在多个文件中都有这个单词，并且多个文件了单词类型又不一样，而且在现实应用中也确实会有这样的情况.

```
===============加载词典完成,用时:16187毫秒================================

java	startOffset:0	endOffset:4	type:正常词
java开发	startOffset:0	endOffset:6	type:正常词,关键词

```



# lucene插件调用方式

```
YgAnalyzer analyzer = new YgAnalyzer("词典所在的目录,比如：e:/dict");
```


# elasticsearch

在elasticsearch中只能是如下配置，【dict_dir】可以是不同值,【dict_dir】表示词典所在的目录，你自己的词典所在的目录.

1. 此分词组件在elasticsearch中注册的类型是【yg】,如果想把此组件设置成默认的分词组件，可以如下配置

```
index:  
  analysis:                     
    analyzer:
      default:
        type: yg
        dict_dir: e:/dict
```

2. 如果只是普通的一个分词组件,则配置

```
index:  
  analysis:                     
    analyzer:
      yg:
        type: yg
        dict_dir: e:/dict
```


# 注意
- 词典文件的编码必须是【UTF-8无BOM格式编码】，可以用Notepad++转换
- 词典是异步加载的，带来的好处启动速度快；但是你也必须了解，由于加载词典是需要时间的，如果你的词典越大，加载时间就越长，因此第一次或者前面几次的分词所用的词典是不完整的（因为词典有可能还在后台加载，前几次用到的词典只包含了一部分单词，或者第一次使用分词时，词典中都还没有加载到单词，那很有可能分词结果为空的）。针对这个问题，我的解决方案是：
```
对于单纯使用文本分析的应用

DictBasedTextHandlerFilter topFilter; 
topFilter.loadDictFrom("e:/dict", false); // 从词典目录加载词典,同步加载词典，通过参数控制
```

```
对于lucene分词插件

YgAnalyzer analyzer = new YgAnalyzer("词典所在的目录,比如：e:/dict");
analyzer.loadDictAdvance();// 提前加载词典
```

```
对于elasticsearch插件

在实现插件时，已经设置在elasticsearch服务器启动时，就已经在后台默默的加载词典了，【只能尽可能的希望在elasticsearch服务器启动完成后，词典加载完成,至少加载了一部分】

```






