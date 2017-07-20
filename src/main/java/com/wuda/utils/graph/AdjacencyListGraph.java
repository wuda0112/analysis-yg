package com.wuda.utils.graph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.wuda.analysis.Token;

/**
 * 图的邻接表实现.
 * 
 * @author wuda
 *
 */
public class AdjacencyListGraph implements Graph {
	/**
	 * 图中所有顶点的集合.
	 */
	private LinkedList<Vertex> vertices = new LinkedList<Vertex>();

	private Map<Integer, LinkedList<Vertex>> map = new HashMap<>();

	public AdjacencyListGraph() {
		Token element = new Token();
		element.setStartOffset(-1);
		element.setEndOffset(-1);
		add(element);
	}

	/**
	 * 往图中添加一个元素.
	 * 
	 * @param element
	 *            element
	 */
	public void add(Token element) {
		Vertex current = new Vertex(element);
		vertices.addLast(current);
		LinkedList<Vertex> vertexs = map.get(element.getEndOffset());
		if (vertexs == null) {
			vertexs = new LinkedList<>();
			vertexs.add(current);
		}
		LinkedList<Vertex> parents = getParents(element);
		if (parents != null) {
			for (Vertex parent : parents) {
				parent.createRelationshipTo(current, RelationshipType.OUT);
			}
		}
	}

	public void finish() {

	}

	/**
	 * 获取当前顶点的父亲顶点.
	 * 
	 * @param element
	 *            element
	 * @return 父亲顶点,null或者没有
	 */
	public LinkedList<Vertex> getParents(Token element) {
		return map.get(element.getStartOffset() - 1);
	}

	/**
	 * Dumps an Graph to a GraphViz's dot language description for visualization.
	 * Example of use:
	 * 
	 * <pre>
	 * graph.toDot("e:/graph.dot");
	 * </pre>
	 * 
	 * and then, from command line:
	 * 
	 * <pre>
	 * dot -Tpng -o e:/out.png e:/graph.dot
	 * </pre>
	 * 
	 * @see <a href="http://www.graphviz.org/">graphviz project</a>
	 * @param filePath
	 *            dot文件完整路径
	 * @throws IOException
	 *             操作文件时异常
	 */
	public void toDot(String filePath) throws IOException {
		StringBuilder builder = new StringBuilder("digraph g {\n");
		for (Vertex v : vertices) {
			LinkedList<Relationship> relationships = v.getRelationships();
			for (Relationship relationship : relationships) {
				builder.append(relationship.getStartVertex().getElement());
				builder.append(" -> ");
				builder.append(relationship.getEndVertex().getElement());
				builder.append(" [label=\"");
				builder.append(relationship.getId());
				builder.append(" ");
				builder.append(relationship.getRelationshipType());
				builder.append("\"]");
				builder.append(";\n");
			}
		}
		builder.append("}");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(new File(filePath)));
			writer.write(builder.toString());
			writer.flush();
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
}
