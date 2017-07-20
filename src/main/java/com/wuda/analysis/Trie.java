package com.wuda.analysis;

import java.util.concurrent.ConcurrentHashMap;

import com.wuda.Constant;
import com.wuda.utils.tree.BasicTree;
import com.wuda.utils.tree.BasicTree.Node;

/**
 * 前缀树.只用于处理字符串.
 * 
 * @author wuda
 *
 */
public class Trie {

	/**
	 * 通过常规树型结构实现前缀树.
	 */
	private BasicTree tree = new BasicTree();

	/**
	 * 节点缓存.
	 */
	private ConcurrentHashMap<String, Node> caches_shard_one = new ConcurrentHashMap<>(Constant.initialCapacity);

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
		char[] array = token.toCharArray();
		token=null;
		Node parent = tree.getRoot();
		Node child = null;
		for (char c : array) {
			c = Character.toLowerCase(c);
			child = find(parent, c);
			if (child == null) {
				child = tree.createNode(c);
				try {
					// tree.createRelationShip(parent, child);数据量大的时候,这里只能注释掉

					// 将父子节点的关系放入缓存,加速查找
					caches_shard_one.put(getCacheKey(parent, child.getElement()), child);
				} catch (Exception e) {
					// 不会发生
				}
			}
			parent = child;
		}
		array = null;
		/**
		 * 因为java是对象引用,所以最后的child对象一定是token的最后一个字符所在的节点.
		 */
		child.setTokenEnd();
		if (tokenType != null) {
			child.appendTokenType(tokenType);
		}
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
	private String getCacheKey(Node parent, char child) {
		return parent.getId() + "" + child;
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
	public Node find(Node parent, char c) {
		String cacheKey = getCacheKey(parent, c);
		/**
		 * 正常情况肯定是使用:tree.find(parent, c)来查找,但是这里使用了缓存
		 */
		return caches_shard_one.get(cacheKey);
	}
	
	/**
	 * 获取根节点.
	 * 
	 * @return root
	 */
	public Node getRoot() {
		return tree.getRoot();
	}

	/**
	 * 获取缓存大小,map中key的个数.
	 * 
	 * @return map中key的个数
	 */
	int getCacheSize() {
		return caches_shard_one.size();
	}
}
