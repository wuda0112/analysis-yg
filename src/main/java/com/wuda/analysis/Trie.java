package com.wuda.analysis;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wuda.utils.tree.AlreadyHasParentException;
import com.wuda.utils.tree.BasicTree;
import com.wuda.utils.tree.DuplicateElementException;
import com.wuda.utils.tree.BasicTree.Node;

/**
 * 前缀树.只用于处理字符串.
 * 
 * @author wuda
 *
 */
public class Trie {

	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 通过常规树型结构实现前缀树.
	 */
	private BasicTree<NodeElement> tree = new BasicTree<NodeElement>();

	/**
	 * 节点缓存.
	 */
	private ConcurrentHashMap<String, Node<NodeElement>> caches = new ConcurrentHashMap<>();

	/**
	 * 添加一个token.
	 * 
	 * @param token
	 *            token,比如单词,短语等
	 * @param tokenType
	 *            toke 类型.
	 */
	public void add(String token, String tokenType) {
		if (token == null) {
			throw new NullPointerException();
		} else if (token.trim().isEmpty()) {
			throw new IllegalArgumentException("token 不能是空字符");
		}
		if (tokenType == null) {
			StringBuilder builder = new StringBuilder("必须指定单词类型.可选的单词类型有:");
			builder.append(TokenType.showTypes());
			String msg = builder.toString();
			logger.warn(msg);
			throw new UnknownTokenTypeException(msg);
		}
		char[] array = token.toCharArray();
		Node<NodeElement> parent = tree.getRoot();
		Node<NodeElement> child = null;
		NodeElement element = null;
		for (char c : array) {
			c = Character.toLowerCase(c);
			element = new NodeElement();
			element.setValue(c);
			child = tree.find(parent, element);
			if (child == null) {
				child = tree.createNode(element);
				try {
					tree.createRelationShip(parent, child);
					caches.put(getCacheKey(parent, child.getElement().getValue()), child);// 将父子节点的关系放入缓存,加速查找
				} catch (AlreadyHasParentException e) {
					// 不会发生
				} catch (DuplicateElementException e) {
					// 不会发生
				}
			}
			parent = child;
		}
		/**
		 * 因为java是对象引用,所以最后的child对象一定是token的最后一个字符所在的节点.
		 */
		child.getElement().setTokenEnd();
		child.getElement().appendTokenType(tokenType);
	}

	/**
	 * 获取缓存key.
	 * 
	 * @param parent
	 *            父节点
	 * @param child
	 *            子节点的数据
	 * @return key
	 */
	private String getCacheKey(Node<NodeElement> parent, Character child) {
		StringBuilder builder = new StringBuilder();
		builder.append(parent.hashCode());
		builder.append("_");
		builder.append(child.charValue());
		return builder.toString();
	}

	/**
	 * 在指定的父节点下查找指定的字符.
	 * 
	 * @param parent
	 *            父节点
	 * @param c
	 *            字符
	 * @return 字符c所在的节点,null-如果没有找到
	 */
	public Node<NodeElement> find(Node<NodeElement> parent, Character c) {
		String cacheKey = getCacheKey(parent, c);
		/**
		 * 正常情况肯定是使用:tree.find(parent, c)来查找,但是这里使用了缓存
		 */
		return caches.get(cacheKey);
	}

	/**
	 * 执行dfs搜索.
	 * 
	 * @param start
	 *            开始节点
	 * @return 此节点的所有后裔,包括此节点,并且此节点一定是位于集合的第一个位置
	 */
	public List<Node<NodeElement>> dfs(Node<NodeElement> start) {
		return tree.dfs(start);
	}

	/**
	 * 获取根节点.
	 * 
	 * @return root
	 */
	public Node<NodeElement> getRoot() {
		return tree.getRoot();
	}

	/**
	 * 前缀树中,每个节点的元素,即{@link Node#getElement()}所代表的元素.
	 * 
	 * @author wuda
	 *
	 */
	class NodeElement implements Comparable<NodeElement> {
		/**
		 * 元素的值.
		 */
		private Character value = null;
		/**
		 * 是否token的结尾.
		 */
		private AtomicBoolean isTokenEnd = new AtomicBoolean(false);
		/**
		 * 如果是token的结尾,此token的所有类型.
		 */
		private Set<String> tokenTypes = null;

		/**
		 * @return the value
		 */
		public Character getValue() {
			return value;
		}

		/**
		 * @param value
		 *            the value to set
		 */
		public void setValue(Character value) {
			this.value = value;
		}

		/**
		 * @return the isTokenEnd
		 */
		public boolean isTokenEnd() {
			return isTokenEnd.get();
		}

		/**
		 * 设置成token end,表示此节点中的元素是token(单词)的最后一个字符.比如【工程师】这个token(单词),则【师】
		 * 所在的节点的元素被设置成token end.
		 */
		public void setTokenEnd() {
			this.isTokenEnd.compareAndSet(false, true);
		}

		/**
		 * 追加TokenType.
		 * 
		 * @param tokenType
		 *            token type
		 */
		public void appendTokenType(String tokenType) {
			if (tokenTypes == null) {
				tokenTypes = new HashSet<>();
			}
			tokenTypes.add(tokenType);
		}
		
		@Override
		public int compareTo(NodeElement o) {
			return value.compareTo(o.getValue());
		}

		/**
		 * @return the tokenTypes
		 */
		public Set<String> getTokenTypes() {
			return tokenTypes;
		}
	}
}
