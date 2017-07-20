package com.wuda.utils.tree;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基本的树型结构.<Strong>first-child next-sibling</Strong>方式的实现.树中的节点有一些限制条件
 * <ul>
 * <li>兄弟节点之间,{@link Node#element}是唯一的,即不能存在任意两个兄弟节点的{@link Node#element}
 * 一样.但是如果节点不是兄弟关系,则他们的{@link Node#element}是可以一样的.</li>
 * <li>root节点是默认生成的,它的{@link Node#element}等于"root",而且至少为root绑定一个子节点,即至少调用一次
 * {@link BasicTree#createRelationShip(Node, Node)},然后第一个Node等于
 * {@link BasicTree#getRoot()}</li>
 * </ul>
 * 
 * @author wuda
 *
 */
public class BasicTree {

	/**
	 * 树的根节点.
	 */
	private Node root = new Node((char) -1);

	/**
	 * 创建一个新的节点.
	 * 
	 * @param element
	 *            节点的元素
	 * 
	 * @return a new node
	 */
	public Node createNode(char element) {
		return new Node(element);
	}

	/**
	 * 创建两个节点的关系.很明显,第一个是父节点,第二个是子节点.
	 * 
	 * @param parent
	 *            父节点
	 * @param child
	 *            子节点
	 * @throws AlreadyHasParentException
	 *             子节点已经拥有父节点
	 * @throws DuplicateElementException
	 *             在给定的父节点下,已经有一个子节点的{@link Node#getElement()}和child的相同
	 */
	public void createRelationShip(Node parent, Node child)
			throws AlreadyHasParentException, DuplicateElementException {
		if (parent == null) {
			throw new NullPointerException("父节点不能为空");
		}
		if (child.parent != null && !child.parent.equals(parent)) {
			throw new AlreadyHasParentException("child 已经拥有了一个父节点,并且这个父节点不是当前提供的父节点");
		}
		if (find(parent, child.element) != null) {
			throw new DuplicateElementException("在给定的父节点下,已经有一个子节点的元素和child的元素相同");
		}
		if (parent.firstChild == null) {
			parent.firstChild = child;
		} else {
			Node sibling = parent.firstChild;
			while (sibling != null && sibling.nextSibling != null) {
				sibling = sibling.nextSibling;
			}
			sibling.nextSibling = child;
		}
		child.parent = parent;
	}

	/**
	 * 寻找父节点下的指定元素的子节点.
	 * 
	 * @param parent
	 *            父节点
	 * @param childElement
	 *            子节点的元素
	 * @return 子节点,null-如果没有找到
	 */
	public Node find(Node parent, char childElement) {
		if (parent == null) {
			return null;
		}
		Node child = parent.firstChild;
		while (child != null) {
			if (Character.compare(child.element, childElement) == 0) {// 找到
				return child;
			}
			child = child.nextSibling;
		}
		return null;
	}

	/**
	 * 获取树的根节点.
	 * 
	 * @return the root
	 */
	public Node getRoot() {
		return root;
	}

	/**
	 * 树的节点.
	 * 
	 * @param T
	 *            元素的类型
	 * 
	 * @author wuda
	 *
	 */
	public static class Node {

		private final static AtomicInteger idGen = new AtomicInteger(-1);

		private int id = 0;

		/**
		 * 当前节点的第一个子节点.
		 */
		private Node firstChild = null;
		/**
		 * 当前节点的兄弟节点.
		 */
		private Node nextSibling = null;
		/**
		 * 当前节点的父节点.
		 */
		private Node parent = null;
		/**
		 * 兄弟节点之间,element是唯一的,即不能存在任意两个兄弟节点的element一样.如果节点不是兄弟关系, 则他们的element是可以一样的.
		 */
		private char element;

		/**
		 * 是否token的结尾.
		 */
		private boolean isTokenEnd = false;
		/**
		 * 如果是token的结尾,此token的所有类型.
		 */
		private Set<String> tokenTypes = null;

		/**
		 * 构建一个节点.
		 * 
		 * @param element
		 *            节点的元素.
		 */
		Node(char element) {
			this.element = element;
			id = idGen.incrementAndGet();
		}

		/**
		 * @return the firstChild
		 */
		Node getFirstChild() {
			return firstChild;
		}

		/**
		 * @param firstChild
		 *            the firstChild to set
		 */
		void setFirstChild(Node firstChild) {
			this.firstChild = firstChild;
		}

		/**
		 * @return the nextSibling
		 */
		Node getNextSibling() {
			return nextSibling;
		}

		/**
		 * @param nextSibling
		 *            the nextSibling to set
		 */
		void setNextSibling(Node nextSibling) {
			this.nextSibling = nextSibling;
		}

		/**
		 * 获取当前节点的父节点.
		 * 
		 * @return the parent
		 */
		public Node getParent() {
			return parent;
		}

		/**
		 * @param parent
		 *            the parent to set
		 */
		void setParent(Node parent) {
			this.parent = parent;
		}

		/**
		 * 获取节点的元素.
		 * 
		 * @return the element
		 */
		public char getElement() {
			return element;
		}

		/**
		 * @return the isTokenEnd
		 */
		public boolean isTokenEnd() {
			return isTokenEnd;
		}

		/**
		 * 设置成token end,表示此节点中的元素是token(单词)的最后一个字符.比如【工程师】这个token(单词),则【师】
		 * 所在的节点的元素被设置成token end.
		 */
		public void setTokenEnd() {
			this.isTokenEnd = true;
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

		/**
		 * @return the tokenTypes
		 */
		public Set<String> getTokenTypes() {
			return tokenTypes;
		}

		/**
		 * 多个type之间用“,”隔开.
		 * 
		 * @return types
		 */
		public String getTypes() {
			if (tokenTypes != null && !tokenTypes.isEmpty()) {
				if (tokenTypes.size() == 1) {
					return tokenTypes.iterator().next();
				}
				StringBuilder builder = new StringBuilder();
				for (String type : tokenTypes) {
					builder.append(type);
					builder.append(",");
				}
				return builder.substring(0, builder.length() - 1);
			}
			return null;
		}

		/**
		 * 获取节点的深度.
		 * 
		 * @return the depth
		 */
		public int getDepth() {
			int tmpDepth = 0;
			Node parent = this.parent;
			while (parent != null) {
				tmpDepth++;
				parent = parent.parent;
			}
			return tmpDepth;
		}

		/**
		 * @return the id
		 */
		public int getId() {
			return id;
		}
	}
}
