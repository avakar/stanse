/* vim: cin
 *
 * tree walker
 *
 * Copyright (c) 2008 Jiri Slaby <jirislaby@gmail.com>
 *
 * Licensed under GPLv2.
 */

tree grammar XMLEmitter;
options {
	tokenVocab=GNUCa;
	ASTLabelType=StanseTree;
}

scope Symbols {
	List<String> variables;
	List<String> variablesOld;
}

@header {
package cz.muni.stanse.cparser;

import cz.muni.stanse.utils.xmlpatterns.XMLAlgo;

import java.io.IOException;

import java.util.LinkedList;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
}
@members {
	protected Document xmlDocument = DocumentHelper.createDocument();
	protected DocumentFactory xmlFactory = DocumentFactory.getInstance();
	private int uniqCnt;
	private boolean symbolsEnabled = true;
	private boolean isFunParam = false;
	private List<String> params = new LinkedList<String>();
	private List<String> paramsOld = new LinkedList<String>();

	/* configuration */
	final private Boolean normalizeTypes = false;
	final private Boolean uniqueVariables = true;
	final private Boolean uniqueVariablesDebug = false;

	private Element newElement(String text) {
		return xmlFactory.createElement(text);
	}
	private Element newElement(String text, StanseTree start) {
		return newElement(text).
			addAttribute("bl", Integer.toString(start.getLine())).
			addAttribute("bc", Integer.toString(start.getCharPositionInLine()));
	}
	private Element newListElement(List<Element> els, String text) {
		Element e = newElement(text);
		els.add(e);
		return e;
	}
	private Element newListElement(List<Element> els, String text,
			StanseTree start) {
		Element e = newElement(text, start);
		els.add(e);
		return e;
	}
	private Element newEmptyStatement(CommonTree ct) {
		Element e = xmlFactory.createElement("emptyStatement");
		e.addAttribute("bl", Integer.toString(ct.getLine()));
		e.addAttribute("bc", Integer.toString(
					ct.getCharPositionInLine()));
		return e;
	}
	private void setAttributes(StanseTree start, Element e) {
		e.addAttribute("bl", Integer.toString(start.getLine())).
		addAttribute("bc", Integer.toString(start.getCharPositionInLine()));
		start.setElement(e);
	}
	private Element addElementBin(Element dest, String name, Element e1, Element e2) {
		Element e = dest.addElement(name);
		e.add(e1);
		e.add(e2);
		return e;
	}
	private Element newElementBin(String name, Element e1, Element e2) {
		Element e = newElement(name);
		e.add(e1);
		e.add(e2);
		return e;
	}
	private void addElementCond(Element dest, Element src) {
		if (src != null)
			dest.add(src);
	}
	private void addAllElements(Element dest, List<Element> src) {
		if (src == null)
			return;
		for (Element el: src)
			dest.add(el);
	}

	/* TODO check bitfields */
	private List<Element> typeNormalize(List<Element> tss) {
		if (!normalizeTypes || tss.isEmpty())
			return tss;

		final String[][] rewrite = {
			{ "signed short", "short" },
			{ "short int", "short" },
			{ "signed short int", "short" },
			{ "unsigned short int", "unsigned short" },
			{ "signed", "int" },
			{ "signed int", "int" },
			{ "unsigned int", "unsigned" },
			{ "signed long", "long" },
			{ "long int", "long" },
			{ "signed long int", "long" },
			{ "unsigned long int", "unsigned long" },
			{ "signed long long", "long long" },
			{ "long long int", "long long" },
			{ "signed long long int", "long long" },
			{ "unsigned long long int", "unsigned long long" },
		};
		StringBuilder sb = new StringBuilder();
		Boolean hadBase = false;

		for (Element ts: tss) {
			if (ts.element("baseType") == null) {
				if (hadBase)
					throw new RuntimeException("non-baseType among baseType?");
				continue;
			}
			hadBase = true;
			sb.append(ts.element("baseType").getText()).append(" ");
		}
		if (sb.length() == 0) /* no baseType */
			return tss;
		sb.setLength(sb.length() - 1);

		String type = sb.toString();
		for (String[] rule: rewrite)
			if (rule[0].equals(type)) {
				tss = new LinkedList<Element>();
				for (String baseType: rule[1].split(" "))
					newListElement(tss, "typeSpecifier").addElement("baseType").addText(baseType);
				break;
			}
		return tss;
	}

	private void clearFunParams() {
		params.clear();
		paramsOld.clear();
	}
	private void processFunParams() {
		$Symbols::variables.addAll(params);
		$Symbols::variablesOld.addAll(paramsOld);
	}

	private String renameVariable(String old) {
		if (!uniqueVariables)
			return old;
		if ($Symbols.size() == 1 && !isFunParam) { /* forward decls */
			int idx = $Symbols[0]::variablesOld.lastIndexOf(old);
			if (idx >= 0)
				return $Symbols[0]::variables.get(idx);
		}
		String new_ = old;

		while (true) {
			int a;
			for (a = $Symbols.size() - 1; a >= 0; a--)
				if ($Symbols[a]::variables.contains(new_))
					break;
			if (a < 0)
				break;
			new_ = old + "_" + uniqCnt++;
		}
		return new_;
	}

	private void pushSymbol(String old, String new_) {
		if (!uniqueVariables || !symbolsEnabled)
			return;
		if (isFunParam) {
			paramsOld.add(old);
			params.add(new_);
			return;
		}
		/* forward decl already pushed one */
		if ($Symbols.size() == 1 && $Symbols::variables.contains(new_))
			return;

		$Symbols::variablesOld.add(old);
		$Symbols::variables.add(new_);

		if (uniqueVariablesDebug) {
			for (int a = 0; a < $Symbols.size() - 1; a++) {
				System.out.print($Symbols[a]::variablesOld);
				System.out.print(" ");
			}
			System.out.println($Symbols::variablesOld);
			for (int a = 0; a < $Symbols.size() - 1; a++) {
				System.out.print($Symbols[a]::variables);
				System.out.print(" ");
			}
			System.out.println($Symbols::variables + " added: " +
					new_);
		}
	}

	private String findVariable(String old) {
		if (!uniqueVariables)
			return old;
		/* find topmost variable */
		for (int a = $Symbols.size() - 1; a >= 0; a--) {
			int idx = $Symbols[a]::variablesOld.lastIndexOf(old);
			if (idx >= 0)
				return $Symbols[a]::variables.get(idx);
		}
		return old; /* throw an exception? */
	}
}

translationUnit returns [Document d]
scope Symbols;
@init {
	Element root = xmlDocument.addElement("translationUnit");
	$Symbols::variables = new LinkedList<String>();
	$Symbols::variablesOld = new LinkedList<String>();
}
	: ^(TRANSLATION_UNIT (eds=externalDeclaration {root.add($eds.e);} )*) {
		xmlDocument.setRootElement(root);
		$d = xmlDocument;
	}
	;

externalDeclaration returns [Element e]
@init {
	$e = newElement("externalDeclaration", $externalDeclaration.start);
}
@after {
	clearFunParams();
}
	: functionDefinition	{ $e.add($functionDefinition.e); }
	| declaration		{ $e.add($declaration.e); }
	;

functionDefinition returns [Element e]
@init {
	$e = newElement("functionDefinition", $functionDefinition.start);
	$functionDefinition.start.setElement($e);
}
	: ^(FUNCTION_DEFINITION declarationSpecifiers declarator {
		$e.add($declarationSpecifiers.e);
		$e.add($declarator.e);
	} functionDefinitionBody[$e])
	;

/* we need a scope even here */
functionDefinitionBody[Element fd]
scope Symbols;
@init {
	List<Element> ds = new LinkedList<Element>();
	$Symbols::variables = new LinkedList<String>();
	$Symbols::variablesOld = new LinkedList<String>();
	processFunParams();
}
	: (d=declaration {ds.add($d.e);})* compoundStatement {
		addAllElements($fd, ds);
		$fd.add($compoundStatement.e);
		$fd.addAttribute("el", Integer.toString(
			$compoundStatement.start.getChild(0).getLine()));
	}
	;

declaration returns [Element e]
@init {
	List<Element> ids = new LinkedList<Element>();
	$e = newElement("declaration", $declaration.start);
}
	: ^('typedef' declarationSpecifiers? (id=initDeclarator {ids.add($id.e);})*) {
		Element ds;
		if ($declarationSpecifiers.e != null)
			$e.add(ds = $declarationSpecifiers.e);
		else
			ds = $e.addElement("declarationSpecifiers");
		ds.addAttribute("storageClass", "typedef");
		addAllElements($e, ids);
	}
	| ^(DECLARATION declarationSpecifiers (id=initDeclarator {ids.add($id.e);})*) {
		$e.add($declarationSpecifiers.e);
		addAllElements($e, ids);
	}
	;

declarationSpecifiers returns [Element e]
@init {
	List<Element> tss = new LinkedList<Element>();
	List<String> tqs = new LinkedList<String>();
	List<String> scs = new LinkedList<String>();
	List<String> fss = new LinkedList<String>();
}
	: ^(DECLARATION_SPECIFIERS ^(XTYPE_SPECIFIER (ts=typeSpecifier {tss.add($ts.e);})*) ^(XTYPE_QUALIFIER (tq=typeQualifier {tqs.add($tq.s);})*) ^(XSTORAGE_CLASS (sc=storageClassSpecifier {scs.add($sc.s);}|fs=functionSpecifier {fss.add($fs.s);})*)) {
		$e = newElement("declarationSpecifiers",
				$declarationSpecifiers.start);
		addAllElements($e, typeNormalize(tss));
		for (String str: tqs)
			$e.addAttribute(str, "1");
		for (String str: scs)
			$e.addAttribute("storageClass", str);
		for (String str: fss)
			$e.addAttribute("function", str);
	}
	;

declarator returns [Element e]
	: ^(DECLARATOR pointer? directDeclarator) {
		$e = newElement("declarator", $declarator.start);
		addAllElements($e, $pointer.els);
		addAllElements($e, $directDeclarator.els);
	}
	;

directDeclarator returns [List<Element> els]
@init {
	$els = new LinkedList<Element>();
}
	: IDENTIFIER {
		String newName = renameVariable($IDENTIFIER.text);
		if (!newName.equals($IDENTIFIER.text))
			newListElement($els, "oldId").addText($IDENTIFIER.text);
		newListElement($els, "id").addText(newName);
		pushSymbol($IDENTIFIER.text, newName);
	}
	| declarator { newListElement($els, "declarator", $declarator.start).
			add($declarator.e); }
	| directDeclarator1 { $els = $directDeclarator1.els; } /* XXX is here the + needed? */
	;

directDeclarator1 returns [List<Element> els]
@init {
	List<String> tqs = new LinkedList<String>();
	List<Element> l = new LinkedList<Element>();
	$els = new LinkedList<Element>();
}
	: ^(ARRAY_DECLARATOR (dd=directDeclarator) ('static' {tqs.add("static");}|asterisk='*')? (tq=typeQualifier {tqs.add(tq);})* expression?) {
		$els = $dd.els;
		Element e = newListElement($els, "arrayDecl");
		for (String t: tqs)
			e.addAttribute(t, "1");
		if (asterisk != null)
			e.addElement("asterisk");
		addElementCond(e, $expression.e);
	}
	| ^(FUNCTION_DECLARATOR (IDENTIFIER { /* we need to process the id before params */
		String newName = renameVariable($IDENTIFIER.text);
		if (!newName.equals($IDENTIFIER.text))
			newListElement($els, "oldId").addText($IDENTIFIER.text);
		newListElement($els, "id").addText(newName);
		pushSymbol($IDENTIFIER.text, newName);
	}|declarator) {isFunParam = true;} (pl=parameterTypeList|(i=identifier {l.add(i);})*)) {
		isFunParam = false;
		if ($IDENTIFIER == null)
			$els.add($declarator.e);
		Element e = newListElement($els, "functionDecl");
		addAllElements(e, pl != null ? pl : l);
	}
	;

initDeclarator returns [Element e]
	: ^(INIT_DECLARATOR declarator initializer?) {
		$e = newElement("initDeclarator", $initDeclarator.start);
		$initDeclarator.start.setElement($e);
		$e.add($declarator.e);
		addElementCond($e, $initializer.e);
	}
	;

initializer returns [Element e]
@init {
	$e = newElement("initializer", $initializer.start);
}
	: ^(INITIALIZER expression)	{ $e.add($expression.e); }
	| INITIALIZER /* just <initializer/> */
	| ^(INITIALIZER initializerList){ addAllElements($e, $initializerList.els); }
	;

initializerList returns [List<Element> els]
@init {
	$els = new LinkedList<Element>();
}
	: ((d=designator {$els.add($d.e);})* initializer {
		$els.add($initializer.e);
	})+
	;

designator returns [Element e]
@init {
	$e = newElement("designator", $designator.start);
}
	: ^(DESIGNATOR ^('...' e1=expression e2=expression)) {
		Element range = $e.addElement("expression").
			addElement("rangeExpression");
		range.add($e1.e);
		range.add($e2.e);
	}
	| ^(DESIGNATOR ^(BRACKET_DESIGNATOR expression)) { $e.add($expression.e); }
	| ^(DESIGNATOR IDENTIFIER)	{ $e.addElement("id").addText($IDENTIFIER.text); }
	| IDENTIFIER			{ $e.addElement("id").addText($IDENTIFIER.text); }
	;

compoundStatement returns [Element e]
scope Symbols;
@init {
	List<Element> els = new LinkedList<Element>();
	$Symbols::variables = new LinkedList<String>();
	$Symbols::variablesOld = new LinkedList<String>();
}
	: ^(COMPOUND_STATEMENT CS_END (d=declaration {els.add($d.e);}|fd=functionDefinition{els.add($fd.e);}|st=statement{els.add($st.e);})*) {
		$e = newElement("compoundStatement", $compoundStatement.start);
		addAllElements($e, els);
		if (els.size() == 0)
			$e.add(newEmptyStatement($compoundStatement.start));
		$compoundStatement.start.setElement($e);
	}
	;

parameterTypeList returns [List<Element> els]
@init {
	$els = new LinkedList<Element>();
}
	: (p=parameterDeclaration {$els.add($p.e);})+ VARARGS? {
		if ($VARARGS != null) {
			Element va = newElement("parameter");
			va.addElement("varArgs");
			$els.add(va);
		}
	}
	;

parameterDeclaration returns [Element e]
	: ^(PARAMETER declarationSpecifiers declarator? abstractDeclarator?) {
		$e = newElement("parameter", $parameterDeclaration.start);
		$e.add($declarationSpecifiers.e);
		addElementCond($e, $declarator.e);
		addElementCond($e, $abstractDeclarator.e);
	}
	;

abstractDeclarator returns [Element e]
@init {
	$e = newElement("abstractDeclarator", $abstractDeclarator.start);
}
	: ^(ABSTRACT_DECLARATOR pointer directAbstractDeclarator?) {
		addAllElements($e, $pointer.els);
		addAllElements($e, $directAbstractDeclarator.els);
	}
	| ^(ABSTRACT_DECLARATOR directAbstractDeclarator) { addAllElements($e, $directAbstractDeclarator.els); }
	;

directAbstractDeclarator returns [List<Element> els]
@init {
	$els = new LinkedList<Element>();
}
	: abstractDeclarator (a=arrayOrFunctionDeclarator {$els.add($a.e);})* {
		$els.add(0, $abstractDeclarator.e);
	}
	| (a=arrayOrFunctionDeclarator {$els.add($a.e);})+
	;

arrayOrFunctionDeclarator returns [Element e]
	: ^(ARRAY_DECLARATOR (expression?|ast='*')) {
		$e = newElement("arrayDecl", $arrayOrFunctionDeclarator.start);
		if ($expression.e != null)
			$e.add($expression.e);
		else if ($ast != null)
			$e.addElement("asterisk");
	}
	| ^(FUNCTION_DECLARATOR parameterTypeList?) {
		$e = newElement("functionDecl",
				$arrayOrFunctionDeclarator.start);
		addAllElements($e, $parameterTypeList.els);
	}
	;

identifier returns [Element e]
	: ^(PARAMETER IDENTIFIER)	{
		String newName = renameVariable($IDENTIFIER.text);
		$e = newElement("id");
		if (!newName.equals($IDENTIFIER.text))
			$e.addAttribute("oldId", $IDENTIFIER.text);
		$e.addText(newName);
		pushSymbol($IDENTIFIER.text, newName);
	}
	;

/* TYPES */

typeName returns [Element e]
@init {
	List<specifierQualifier_return> sqs = new LinkedList<specifierQualifier_return>();
}
	: ^(TYPE_NAME (sq=specifierQualifier {sqs.add(sq);})+ abstractDeclarator?) {
		List <Element> tss = new LinkedList<Element>();
		$e = newElement("typeName", $typeName.start);
		for (specifierQualifier_return sqr: sqs)
			if (sqr.qual != null)
				$e.addAttribute(sqr.qual, "1");
			else
				tss.add(sqr.spec);
		for (Element el: typeNormalize(tss))
			$e.add(el);
		addElementCond($e, $abstractDeclarator.e);
	}
	;

specifierQualifier returns [Element spec, String qual]
	: ^(XTYPE_SPECIFIER typeSpecifier)	{ $spec = $typeSpecifier.e; }
	| ^(XTYPE_QUALIFIER typeQualifier)	{ $qual = $typeQualifier.s; }
	;

typeQualifier returns [String s]
	: 'const'	{ $s = "const"; }
	| 'restrict'	{ $s = "restrict"; }
	| 'volatile'	{ $s = "volatile"; }
	;

typeSpecifier returns [Element e]
@init {
	$e = newElement("typeSpecifier", $typeSpecifier.start);
}
	: ^(BASETYPE 'void')	{ $e.addElement("baseType").addText("void"); }
	| ^(BASETYPE 'char')	{ $e.addElement("baseType").addText("char"); }
	| ^(BASETYPE 'short')	{ $e.addElement("baseType").addText("short"); }
	| ^(BASETYPE 'int')	{ $e.addElement("baseType").addText("int"); }
	| ^(BASETYPE 'long')	{ $e.addElement("baseType").addText("long"); }
	| ^(BASETYPE 'float')	{ $e.addElement("baseType").addText("float"); }
	| ^(BASETYPE 'double')	{ $e.addElement("baseType").addText("double"); }
	| ^(BASETYPE SIGNED)	{ $e.addElement("baseType").addText("signed"); }
	| ^(BASETYPE 'unsigned'){ $e.addElement("baseType").addText("unsigned"); }
	| ^(BASETYPE '_Bool')	{ $e.addElement("baseType").addText("_Bool"); }
	| ^(BASETYPE COMPLEX)	{ $e.addElement("baseType").addText("_Complex"); }
	| ^(BASETYPE XID)
	| ^(BASETYPE '_Imaginary')	{ $e.addElement("baseType").addText("_Imaginary"); }
	| structOrUnionSpecifier{ $e.add($structOrUnionSpecifier.e); }
	| enumSpecifier		{ $e.add($enumSpecifier.e); }
	| typedefName		{ $e.addElement("typedef").addElement("id").addText($typedefName.s); }
	| typeofSpecifier
	;

structOrUnionSpecifier returns [Element e]
@init {
	List<Element> sds = new LinkedList<Element>();
	symbolsEnabled = false;
}
	: ^(structOrUnion ^(XID IDENTIFIER?) (sd=structDeclaration {sds.add($sd.e);})*) {
		$e = newElement($structOrUnion.s);
		if ($IDENTIFIER != null)
			$e.addAttribute("id", $IDENTIFIER.text);
		addAllElements($e, sds);
		symbolsEnabled = true;
	}
	;

structOrUnion returns [String s]
	: 'struct'	{ $s = "struct"; }
	| 'union'	{ $s = "union"; }
	;

structDeclaration returns [Element e]
@init {
	List<specifierQualifier_return> sqs = new LinkedList<specifierQualifier_return>();
	List<Element> sds = new LinkedList<Element>();
}
	: ^(STRUCT_DECLARATION (sq=specifierQualifier {sqs.add(sq);})+ (sd=structDeclarator {sds.add($sd.e);})*) {
		List <Element> tss = new LinkedList<Element>();
		$e = newElement("structDeclaration", $structDeclaration.start);
		for (specifierQualifier_return sqr: sqs)
			if (sqr.qual != null)
				$e.addAttribute(sqr.qual, "1");
			else
				tss.add(sqr.spec);
		for (Element el: typeNormalize(tss))
			$e.add(el);
		addAllElements($e, sds);
	}
	;

structDeclarator returns [Element e]
	: ^(STRUCT_DECLARATOR declarator? expression?) {
		$e = newElement("structDeclarator", $structDeclarator.start);
		addElementCond($e, $declarator.e);
		addElementCond($e, $expression.e);
	}
	;

enumSpecifier returns [Element e]
@init {
	List<Element> ens = new LinkedList<Element>();
}
	: ^('enum' (^(XID IDENTIFIER))? (en=enumerator {ens.add($en.e);})*) {
		$e = newElement("enum", $enumSpecifier.start);
		if ($IDENTIFIER != null)
			$e.addAttribute("id", $IDENTIFIER.text);
		addAllElements($e, ens);
	}
	;

enumerator returns [Element e]
	: ^(ENUMERATOR IDENTIFIER expression?) {
		$e = newElement("enumerator", $enumerator.start);
		$e.addAttribute("id", $IDENTIFIER.text);
		addElementCond($e, $expression.e);
	}
	;

typedefName returns [String s]
	: IDENTIFIER	{ $s = $IDENTIFIER.text; }
	;

typeofSpecifier
	: ^(TYPEOF expression)
	| ^(TYPEOF typeName)
	;

storageClassSpecifier returns [String s]
	: 'extern'	{ $s = "extern"; }
	| 'static'	{ $s = "static"; }
	| 'auto'	{ $s = "auto"; }
	| 'register'	{ $s = "register"; }
	| '__thread'	{ $s = "__thread"; }
	;

functionSpecifier returns [String s]
	: 'inline'	{ $s = "inline"; }
	;

pointer returns [List<Element> els]
@init {
	List<String> tqs = new LinkedList<String>();
	$els = new LinkedList<Element>();
}
	: ^(POINTER (tq=typeQualifier {tqs.add(tq);})* ptr=pointer?) {
		Element e = newElement("pointer");
		for (String t: tqs)
			e.addAttribute(t, "1");
		$els.add(e);
		if ($ptr.els != null)
			$els.addAll($ptr.els);
	}
	;

/* TYPES END */

/* STATEMENTS */

statement returns [Element e]
@after {
	setAttributes($statement.start, $e);
}
	: labeledStatement	{ $e=$labeledStatement.e; }
	| compoundStatement	{ $e=$compoundStatement.e; }
	| expressionStatement	{ $e=newElement("expressionStatement");$e.add($expressionStatement.e != null ? $expressionStatement.e : newEmptyStatement($expressionStatement.start)); }
	| selectionStatement	{ $e=$selectionStatement.e; }
	| iterationStatement	{ $e=$iterationStatement.e; }
	| jumpStatement		{ $e=$jumpStatement.e; }
	| asmStatement		{ $e=$asmStatement.e; }
	;

labeledStatement returns [Element e]
	: ^(LABEL IDENTIFIER statement) {
		$e = newElement("labelStatement", $labeledStatement.start);
		$e.add($statement.e);
		$e.addAttribute("id", $IDENTIFIER.text);
	}
	| ^('case' expression statement) {
		$e = newElement("caseLabelStatement", $labeledStatement.start);
		$e.add($expression.e);
		$e.add($statement.e);
	}
	| ^('default' statement) {
		$e = newElement("defaultLabelStatement",
				$labeledStatement.start);
		$e.add($statement.e);
	}
	;

expressionStatement returns [Element e]
	: ^(EXPRESSION_STATEMENT expression?) { $e = $expression.e; }
	;

selectionStatement returns [Element e]
	: ^('if' expression s1=statement s2=statement?) {
		$e = newElement(s2 == null ? "ifStatement" : "ifElseStatement",
				$selectionStatement.start);
		$e.add($expression.e);
		$e.add($s1.e);
		addElementCond($e, $s2.e);
	}
	| ^('switch' expression statement) {
		$e = newElement("switchStatement", $selectionStatement.start);
		$e.add($expression.e);
		$e.add($statement.e);
	}
	;

iterationStatement returns [Element e]
	: ^('while' expression statement) {
		$e = newElement("whileStatement", $iterationStatement.start);
		$e.add($expression.e);
		$e.add($statement.e);
	}
	| ^('do' statement expression) {
		$e = newElement("doStatement", $iterationStatement.start);
		$e.add($statement.e);
		$e.add($expression.e);
	}
	| ^('for' declaration? (^(E1 e1=expression))? ^(E2 e2=expression?) ^(E3 e3=expression?) statement) {
		$e = newElement("forStatement", $iterationStatement.start);
		if ($declaration.e != null)
			$e.add($declaration.e);
		else if (e1 != null)
			$e.add($e1.e);
		else
			$e.addElement("expression");
		if (e2 != null)
			$e.add($e2.e);
		else
			$e.addElement("expression");
		if (e3 != null)
			$e.add($e3.e);
		else
			$e.addElement("expression");
		$e.add($statement.e);
	}
	;

jumpStatement returns [Element e]
	: ^('goto' IDENTIFIER) {
		$e = newElement("gotoStatement", $jumpStatement.start);
		$e.addElement("expression").addElement("id").addText($IDENTIFIER.text);
	}
	| ^('goto' XU expression) {
		$e = newElement("gotoStatement", $jumpStatement.start);
		$e.addElement("expression").addElement("derefExpression").add($expression.e);
	}
	| 'continue'	{ $e = newElement("continueStatement",
			$jumpStatement.start); }
	| 'break'	{ $e = newElement("breakStatement",
			$jumpStatement.start); }
	| ^('return' expression?) {
		$e = newElement("returnStatement", $jumpStatement.start);
		addElementCond($e, $expression.e);
	}
	;

asmStatement returns [Element e]
	: ASM		{ $e = newElement("gnuAssembler", $asmStatement.start); }
	;

/* STATEMENTS END */

/* EXPRESSIONS */

expression returns [Element e]
@init {
	List<Element> exs = new LinkedList<Element>();
	Element exp;
}
@after {
	setAttributes($expression.start, $e);
}
	: ^(ASSIGNMENT_EXPRESSION assignmentOperator e1=expression e2=expression) {
		String op = $assignmentOperator.text;
		$e = newElementBin("assignExpression", $e1.e, $e2.e);
		if (!op.equals("="))
			$e.addAttribute("op", op.substring(0, op.length() - 1));
	}
	| ^(CONDITIONAL_EXPRESSION ^(E1 e1=expression) ^(E2 e2=expression?) ^(E3 e3=expression)) {
		$e=newElement("conditionalExpression");
		$e.add($e1.e);
		addElementCond($e, $e2.e);
		$e.add($e3.e);
	}
	| ^(CAST_EXPRESSION tn=typeName e1=expression) { $e=newElementBin("castExpression", $tn.e, $e1.e); }
	| ^(ARRAY_ACCESS e1=expression e2=expression) { $e=newElementBin("arrayAccess", $e1.e, $e2.e); }
	| ^(FUNCTION_CALL e1=expression (e2=expression {exs.add($e2.e);})*) {
		$e=newElement("functionCall");
		$e.add($e1.e);
		for (Element el: exs)
			$e.add(el);
	}
	| ^(COMPOUND_LITERAL tn=typeName initializerList?) {
		Element me = newElement("initializer");
		$e=newElementBin("compoundLiteral", $tn.e, me);
		addAllElements(me, $initializerList.els);
	}
	| ^(',' e1=expression e2=expression)	{ $e=newElementBin("commaExpression", $e1.e, $e2.e); }
	| ^('++' e1=expression)		{ $e=newElement("prefixExpression");$e.addAttribute("op", "++").add($e1.e); }
	| ^('--' e1=expression)		{ $e=newElement("prefixExpression");$e.addAttribute("op", "--").add($e1.e); }
	| ^(unaryOp e1=expression)	{ $e=newElement("prefixExpression");$e.addAttribute("op", $unaryOp.op).add($e1.e); }
	| ^('sizeof' (e1=expression|tn=typeName))	{ $e=newElement("sizeofExpression"); $e.add(e1 != null ? $e1.e : $tn.e); }
	| ^('__alignof__' (e1=expression|tn=typeName))	{ $e=newElement("allignofExpression"); $e.add(e1 != null ? $e1.e : $tn.e); }
	| ^('.' e1=expression IDENTIFIER)	{
		$e=newElement("dotExpression");
		$e.add($e1.e);
		$e.addElement("member").addText($IDENTIFIER.text);
	}
	| ^('->' e1=expression IDENTIFIER) {
		$e=newElement("arrowExpression");
		$e.add($e1.e);
		$e.addElement("member").addText($IDENTIFIER.text);
	}
	| ^(AU e1=expression)	{ $e=newElement("addrExpression"); $e.add($e1.e); }
	| ^(XU e1=expression)	{ $e=newElement("derefExpression"); $e.add($e1.e); }
	| ^(PP e1=expression)	{ $e=newElement("postfixExpression"); $e.addAttribute("op", "++").add($e1.e); }
	| ^(MM e1=expression)	{ $e=newElement("postfixExpression"); $e.addAttribute("op", "--").add($e1.e); }
	| binaryExpression	{ $e=$binaryExpression.e; }
	| primaryExpression	{ $e=$primaryExpression.e; }
	;

binaryExpression returns [Element e]
@after {
	$e = newElement("binaryExpression", $binaryExpression.start);
	$e.add($e1.e);
	$e.add($e2.e);
	$e.addAttribute("op", op.getText());
}
	: ^(op='||' e1=expression e2=expression)
	| ^(op='&&' e1=expression e2=expression)
	| ^(op='|' e1=expression e2=expression)
	| ^(op='&' e1=expression e2=expression)
	| ^(op='^' e1=expression e2=expression)
	| ^(op='==' e1=expression e2=expression)
	| ^(op='!=' e1=expression e2=expression)
	| ^(op='<=' e1=expression e2=expression)
	| ^(op='<' e1=expression e2=expression)
	| ^(op='>=' e1=expression e2=expression)
	| ^(op='>' e1=expression e2=expression)
	| ^(op='>>' e1=expression e2=expression)
	| ^(op='<<' e1=expression e2=expression)
	| ^(op='+' e1=expression e2=expression)
	| ^(op='-' e1=expression e2=expression)
	| ^(op='*' e1=expression e2=expression)
	| ^(op='/' e1=expression e2=expression)
	| ^(op='%' e1=expression e2=expression)
	;

primaryExpression returns [Element e]
	: IDENTIFIER		{ $e = newElement("id"); $e.addText(findVariable($IDENTIFIER.text)); }
	| constant		{ $e = $constant.e; }
	| sTRING_LITERAL	{ $e = newElement("stringConst", $sTRING_LITERAL.start); $e.addText($sTRING_LITERAL.text); }
	| compoundStatement	{ $e = $compoundStatement.e; }
	| ^(BUILTIN_OFFSETOF typeName offsetofMemberDesignator) {
		$e = newElement("offsetofExpression", $primaryExpression.start);
		$e.add($typeName.e);
		$e.add($offsetofMemberDesignator.e);
	}
	;

sTRING_LITERAL returns [String text]
@init {
	List<String> sls = new LinkedList<String>();
}
	: ^(STR_LITERAL (STRING_LITERAL {sls.add($STRING_LITERAL.text);})+) {
		StringBuilder sb = new StringBuilder();
		for (String str: sls) /* crop the quotation */
			sb.append(str.substring(1, str.length() - 1));
		$text = sb.toString();
	}
	;

constant returns [Element e]
	:	ICONSTANT { $e = newElement("intConst"); $e.addText($ICONSTANT.text); }
	|	RCONSTANT { $e = newElement("realConst"); $e.addText($RCONSTANT.text); }
	;

offsetofMemberDesignator returns [Element e]
@init {
	Element e1;
}
	: id1=IDENTIFIER {
		$e = newElement("expression");
		$e.addElement("id").addText(id1.getText());
	}	( ('.' id2=IDENTIFIER {
			e1 = newElement("dotExpression");
			e1.add($e);
			e1.addElement("member").addText(id2.getText());
			$e = newElement("expression");
			$e.add(e1);
		})
		| ('[' expression ']' {
			e1 = newElement("arrayAccess");
			e1.add($e);
			e1.add($expression.e);
			$e = newElement("expression");
			$e.add(e1);
		})
		)*
	;

assignmentOperator
	: '='
	| '*='
	| '/='
	| '%='
	| '+='
	| '-='
	| '<<='
	| '>>='
	| '&='
	| '^='
	| '|='
	;

unaryOp returns [String op]
	: PU		{ $op = "+"; }
	| MU		{ $op = "-"; }
	| '~'		{ $op = "~"; }
	| '!'		{ $op = "!"; }
	| LABREF	{ $op = "&&"; }
	| '__real__'	{ $op = "__real"; }
	| '__imag__'	{ $op = "__imag"; }
	;

/* EXPRESSIONS END */
