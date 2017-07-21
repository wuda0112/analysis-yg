package com.wuda.utils.graph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.wuda.analysis.Token;

/**
 * 邻接表实现,这不是一个通用的数据结构,仅仅满足分词时,生成词图,所以里面的内容就按照分词的规则处理了,没有一点通用性.
 * 
 * @author wuda
 *
 */
public class TokenGraph {
	/**
	 * 图中所有顶点的集合.
	 */
	private LinkedList<Vertex> vertices = new LinkedList<Vertex>();

	private Map<Integer, LinkedList<Vertex>> map = new HashMap<>();

	private int lastIndex = 0;

	private Token start = new Token();

	private Token end = new Token();

	/**
	 * 生成空的token图,包含一个"start"token.
	 */
	public TokenGraph() {
		start.setValue("zds");
		start.setStartOffset(-1);
		start.setEndOffset(0);
		add(start);
	}

	/**
	 * 往图中添加一个元素.
	 * 
	 * @param element
	 *            element
	 */
	public void add(Token element) {
		lastIndex = Math.max(lastIndex, element.getEndOffset());
		Vertex current = new Vertex(element);
		vertices.addLast(current);
		LinkedList<Vertex> parents = getParents(element);
		if (parents != null) {
			for (Vertex parent : parents) {
				parent.createRelationshipTo(current, RelationshipType.OUT);
			}
		}
		endOffsetMapVertex(current);
	}

	/**
	 * 每个顶点中元素的endOffset和顶点相互映射
	 * 
	 * @param v
	 *            v
	 */
	private void endOffsetMapVertex(Vertex v) {
		int elementEndOffset = v.getElement().getEndOffset();
		LinkedList<Vertex> vertexs = map.get(elementEndOffset);
		if (vertexs == null) {
			vertexs = new LinkedList<>();
			vertexs.add(v);
			map.put(elementEndOffset, vertexs);
		} else {
			vertexs.add(v);
		}
	}

	/**
	 * 获取当前顶点的父亲顶点.
	 * 
	 * @param element
	 *            element
	 * @return 父亲顶点,null或者没有
	 */
	private LinkedList<Vertex> getParents(Token element) {
		return map.get(element.getStartOffset());
	}

	/**
	 * token图生成完成.
	 */
	public void finish() {
		end.setValue("zde");
		end.setStartOffset(lastIndex);
		end.setEndOffset(lastIndex + 1);
		add(end);
	}

	/**
	 * 清理,加快gc.
	 */
	public void clear() {
		vertices.clear();
		vertices = null;
		map.clear();
		map = null;
		start = null;
		end = null;
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
		builder.append("graph [fontname=\"FangSong\"];\n");
		builder.append("edge [fontname=\"FangSong\"];\n");
		builder.append("node [shape=box, fontname=\"FangSong\"];\n");
		for (Vertex v : vertices) {
			LinkedList<Relationship> relationships = v.getRelationships();
			for (Relationship relationship : relationships) {
				Token start = v.getElement();
				Token end = relationship.getEndVertex().getElement();
				builder.append(start.getValue());
				builder.append(" -> ");
				builder.append(end.getValue());
				builder.append(" [label=\"");
				builder.append(relationship.getRelationshipType());
				builder.append("\"]");
				builder.append(";\n");
			}
		}
		builder.append("}");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(new File(filePath)), Charset.forName("UTF-8")));
			writer.write(builder.toString());
			writer.flush();
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
}
