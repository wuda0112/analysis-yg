package com.wuda.utils.graph;

/**
 * 图中两个顶点之间的关系.关系是有方向的,方向总是从startVertex指向 {@link #endVertex}.
 * 
 * @author wuda
 *
 */
public class Relationship {
	/**
	 * 结束节点.
	 */
	private Vertex endVertex = null;
	/**
	 * 关系类型.
	 */
	private RelationshipType relationshipType = null;

	/**
	 * 在遍历过程中,关系所处的状态.
	 */
	private RelationshipStatus status = RelationshipStatus.UNDETERMINED;

	/**
	 * 在遍历时,关系所处的状态.
	 * 
	 * @author wuda
	 *
	 */
	static enum RelationshipStatus {
		UNDETERMINED, CROSS, TREE, FORWARD, BACKWARD;
	}

	/**
	 * 创建一个<i>startVertex</i>到<i>endVertex</i>方向的的关系.
	 * 
	 * @param endVertex
	 *            end
	 * @param relationshipType
	 *            关系类型
	 */
	Relationship(Vertex endVertex, RelationshipType relationshipType) {
		this.endVertex = endVertex;
		this.relationshipType = relationshipType;
	}

	/**
	 * 获取结束节点.
	 * 
	 * @return the endVertex
	 */
	public Vertex getEndVertex() {
		return endVertex;
	}

	/**
	 * 获取关系类型.
	 * 
	 * @return the relationshipType
	 */
	public RelationshipType getRelationshipType() {
		return relationshipType;
	}

	/**
	 * 获取在遍历过程中的状态.
	 * 
	 * @return the status
	 */
	RelationshipStatus getStatus() {
		return status;
	}

	/**
	 * 设置在遍历过程中的状态.
	 * 
	 * @param status
	 *            the status to set
	 */
	void setStatus(RelationshipStatus status) {
		this.status = status;
	}

}
