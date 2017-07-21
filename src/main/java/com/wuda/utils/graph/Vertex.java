package com.wuda.utils.graph;

import java.util.LinkedList;

import com.wuda.analysis.Token;

/**
 * 图的顶点.
 * 
 * @author wuda
 *
 */
public class Vertex {

	/**
	 * 顶点中的元素.
	 */
	private Token element = null;

	/**
	 * 可以说是此顶点与“边”的关联关系,也可以说是此顶点的邻接顶点集合.
	 */
	private LinkedList<Relationship> relationships = new LinkedList<Relationship>();

	/**
	 * 在遍历时,顶点所处的状态.
	 */
	private VertexStatus vertexStatus = VertexStatus.UNDISCOVERED;

	/**
	 * 在遍历时,此顶点的父顶点.
	 */
	private Vertex parent = null;

	/**
	 * 标记顶点在遍历过程中的状态.
	 * 
	 * @author wuda
	 *
	 */
	static enum VertexStatus {
		UNDISCOVERED, DISCOVERED, VISITED;
	}

	/**
	 * 构造一个顶点.
	 * 
	 * @param element
	 *            定点中的元素
	 */
	public Vertex(Token Token) {
		this.element = Token;
	}

	/**
	 * 以当前顶点为起始顶点,another为结束顶点,创建两个顶点之间的关系.如果这两个顶点之间已经存在这种类型的关系,则直接返会,
	 * 如果没有的话则创建关系并且返回.
	 * 
	 * @param end
	 *            结束顶点
	 * @param relationshipType
	 *            关系的类型
	 * @return 两个顶点之间的关系对象
	 */
	public Relationship createRelationshipTo(Vertex end, RelationshipType relationshipType) {
		Relationship relationship = new Relationship(end, relationshipType);
		relationships.addLast(relationship);
		return relationship;
	}

	/**
	 * 获取当前顶点到指定顶点之间的所有关系.
	 * 
	 * @param to
	 *            end vertex
	 * @return 所有的关系
	 */
	public LinkedList<Relationship> getRelationships(Vertex to) {
		LinkedList<Relationship> relationships = this.getRelationships();
		if (relationships == null || relationships.isEmpty()) {
			return null;
		}
		LinkedList<Relationship> rels = new LinkedList<Relationship>();
		for (Relationship relationship : relationships) {
			Vertex v = relationship.getEndVertex();
			if (v.equals(to)) {
				rels.add(relationship);
			}
		}
		return rels;
	}

	/**
	 * 返回当前顶点的所有关系,即当前顶点的所有邻接顶点.
	 * 
	 * @return the relationships
	 */
	public LinkedList<Relationship> getRelationships() {
		return relationships;
	}

	/**
	 * 顶点在遍历过程中所处的状态.
	 * 
	 * @return the status
	 */
	VertexStatus getStatus() {
		return vertexStatus;
	}

	/**
	 * 设置顶点在遍历过程中所处的状态.
	 * 
	 * @param status
	 *            the status to set
	 */
	void setStatus(VertexStatus status) {
		this.vertexStatus = status;
	}

	/**
	 * 顶点在遍历过程中的父顶点.
	 * 
	 * @return the parent
	 */
	Vertex getParent() {
		return parent;
	}

	/**
	 * 设置顶点在遍历过程中的父顶点.
	 * 
	 * @param parent
	 *            the parent to set
	 */
	void setParent(Vertex parent) {
		this.parent = parent;
	}

	@Override
	public String toString() {
		return element.toString();
	}

	/**
	 * 返回当前顶点中的元素
	 * 
	 * @return 顶点中的元素
	 */
	public Token getElement() {
		return element;
	}

}
