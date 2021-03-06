/*
 * Licensed under GPLv2
 */

package cparser.AST;

import java.util.Vector;

/**
 * The basic member of AST tree
 *
 * @author Jiri Slaby
 */
public abstract class Node {
	final Vector<Node> children = new Vector<Node>();
	int line = 0, column = 0;

	protected ComplexType type = null; /* if typed node, a type */
	protected Object eval = null; /* evaluated value of this node */

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	public void setAttrSS(final String attr, final String value) {
		throw new UnsupportedOperationException();
	}

	public void setAttrIS(final int key, final String value) {
		throw new UnsupportedOperationException();
	}

	public void setAttrII(final int key, final int value) {
		throw new UnsupportedOperationException();
	}

	public void addChild(final Node child) {
		children.add(child);
	}

	public Vector<Node> getChildren() {
		return children;
	}

	public Node getChild(int idx) {
		return children.get(idx);
	}

	public void clearChildren() {
		for (final Node child : children)
			child.clearChildren();
		children.clear();
	}

	public void compute() {
		for (final Node child : children)
			child.compute();
	}

	public void createCFG() {
		for (final Node child : children)
			child.createCFG();
	}

	public Object getType() {
		return type;
	}

	public Object getEval() {
		return eval;
	}

	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * To be overriden by nested classes if something should appear in the
	 * XML attributes for this node
	 * @param sb where to put the attributes
	 */
	void XMLAttributes(final StringBuilder sb) {
	}

	void XMLChildren(final StringBuilder sb) {
		boolean terminate = children.size() > 1;
		for (final Node n : children) {
			sb.append(n.toXML());
			if (terminate)
				sb.append('\n');
		}
	}

	public String toXML(){
		final StringBuilder sb = new StringBuilder("<");
		final String name = getClass().getSimpleName();
		final String XMLname = Character.toLowerCase(name.charAt(0)) +
			name.substring(1);
		sb.append(XMLname);
		XMLAttributes(sb);
		if (line > 0)
			sb.append(" bl=\"").append(line).append('"');
		else
			sb.append(" nobl=\"1\"");
		if (column > 0)
			sb.append(" bc=\"").append(column).append('"');
		else
			sb.append(" nobc=\"1\"");
		sb.append('>');
		XMLChildren(sb);
		sb.append("</").append(XMLname).append('>');
		return sb.toString();
	}
}
