/*
 * Licensed under GPLv2
 */

package cparser.AST;

/**
 * @author Jiri Slaby
 */
public class StructDeclaration extends Node {
	private Modifiers mods = new Modifiers();

	@Override
	public void setAttr(final String attr, final String value) {
		if (!mods.setTQ(attr))
			throw new RuntimeException(
				"structDecl: invalid attribute: " + attr);
	}

	@Override
	void XMLAttributes(final StringBuilder sb) {
		for (final String tq : mods.getTQList())
			sb.append(' ').append(tq).append("=\"1\"");
	}
}
