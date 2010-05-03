package cz.muni.stanse.pointeranalyzer.shapirohorwitz;

import cz.muni.stanse.codestructures.*;
import java.util.*;
import org.dom4j.*;
import cz.muni.stanse.pointeranalyzer.PointsToAnalyzer;
import cz.muni.stanse.utils.Pair;

/**
 *
 * @author Michal Strehovsky
 */
public final class ShapiroHorwitzAnalyzer implements PointsToAnalyzer {

    private TypeTable typeTable;

    private CategorizationProvider categorizationProvider;

    public ShapiroHorwitzAnalyzer(CategorizationProvider catProvider) {

        categorizationProvider = catProvider;

        typeTable = new TypeTable(categorizationProvider);

    }

    public void analyze(Collection<CFGHandle> cfgs) {

        for (CFGHandle cfg: cfgs) {
            addFunction(cfg);
        }

        for (CFGHandle cfg: cfgs) {
            //System.out.printf("*********** in function %s\n", cfg.getFunctionName());

            for (CFGNode node: cfg.getAllNodes()) {
                if (!node.getElement().getName().equals("exit"))
                    handleStatement(cfg, node.getElement());
            }
        }

        typeTable.toDotFile();
    }

    public Set<Pair<CFGHandle, String>> getPointsToSetOf(CFGHandle cfg, String id) {
        return typeTable.getPointsToSetOf(cfg, id);
    }

    private void addFunction(CFGHandle cfg) {

        Element functionDecl = cfg.getElement().element("declarator").element("functionDecl");

        List<String> parameterNames = new ArrayList<String>();

        for (Object o: functionDecl.elements()) {
            Element e = (Element)o;
            parameterNames.add(e.selectSingleNode("declarator/id").getText());
        }

        typeTable.addFunction(cfg, parameterNames);
    }

    private LocationPointerType handleAssignment(CFGHandle cfg, Element lhs, Element rhs) {

        LocationPointerType lhsType = handleExpression(cfg, lhs);
        LocationPointerType rhsType = handleExpression(cfg, rhs);

        rhsType.unifyWith(lhsType);

        return rhsType;
    }

    private AbstractLocation getSymbolAbstractLocation(CFGHandle cfg, String name) {
        if (cfg.isSymbolLocal(name)) {
            return typeTable.getTypeOf(cfg, name);
        } else {
            return typeTable.getTypeOf(name);
        }
    }

    private LocationPointerType getSymbolType(CFGHandle cfg, String name) {
        return ((LocationPointerType)getSymbolAbstractLocation(cfg, name).getType());
    }

    private LocationPointerType handleId(CFGHandle cfg, Element id) {
        return getSymbolType(cfg, id.getText());
    }

    private LocationPointerType handleAddrExpression(CFGHandle cfg, Element addrExpression) {

        // this can be either address of a varible or address of a field in an array
        Element addressedExpression = (Element)addrExpression.elements().get(0);
        if (addressedExpression.getName().equals("arrayAccess")) {
            // evaluate the type of array accessor (we don't really care about it, but it might have side effects)
            handleExpression(cfg, (Element)addressedExpression.elements().get(1));

            // the real addressed variable is this
            addressedExpression = (Element)addressedExpression.elements().get(0);
        }

        assert addressedExpression.getName().equals("id");

        String name = addressedExpression.getText();
        AbstractLocation idLocation = getSymbolAbstractLocation(cfg, name);

        return new LocationPointerType(
                categorizationProvider,
                idLocation,
                new AbstractLocation(idLocation.getCategory(), "addrExpressionDummyLambda"));
    }

    private LocationPointerType handleFunctionCall(CFGHandle cfg, Element functionCall) {
        LocationPointerType refFunction = handleExpression(cfg, (Element)functionCall.elements().get(0));

        AbstractLocation function = refFunction.getLambda();
        FunctionPointerType functionType = (FunctionPointerType)function.getType();

        // calling a function without any knowledge about the called function?
        if (functionType == null) {

            // TODO: do something in this case, throw is not a good idea,
            // and the new LocationPointerType is not a good idea either
            //throw new UnsupportedOperationException();
            return new LocationPointerType(categorizationProvider);
        }

        for (int i = 1; i < functionCall.elements().size(); i++) {
            LocationPointerType parameterVariable =
                    (LocationPointerType)functionType.parameters.get(i - 1).getType();

            parameterVariable.unifyWith(handleExpression(cfg, (Element)functionCall.elements().get(i)));
        }

        return (LocationPointerType)functionType.returnParameter.getType();
    }

    private LocationPointerType handleDerefExpression(CFGHandle cfg, Element derefExpression) {
        return handleExpression(cfg, (Element)derefExpression.elements().get(0)).dereference();
    }
    
    private LocationPointerType handleConditionalExpression(CFGHandle cfg, Element conditionalExpression) {
        
        handleExpression(cfg, (Element)conditionalExpression.elements().get(0));

        return handleAssignment(cfg,
                (Element)conditionalExpression.elements().get(1),
                (Element)conditionalExpression.elements().get(2));
    }
    
    private LocationPointerType handleCommaExpression(CFGHandle cfg, Element commaExpression) {
        
        // evaluate the left side because it might have side effects
        handleExpression(cfg, (Element)commaExpression.elements().get(0));

        // evaluate the right side and return evaluated type
        return handleExpression(cfg, (Element)commaExpression.elements().get(1));
    }
    
    private LocationPointerType handleArrayAccess(CFGHandle cfg, Element arrayAccess) {
        
        // we don't really care about the accessor, but it might have side effects, so evaluate it
        handleExpression(cfg, (Element)arrayAccess.elements().get(1));

        return handleExpression(cfg, (Element)arrayAccess.elements().get(0));
    }

    private LocationPointerType handleExpression(CFGHandle cfg, Element expr) {
        //System.out.printf("eval: %s\n", expr.getName());

        if (expr.getName().equals("initDeclarator")) {
            return handleAssignment(cfg,
                    (Element)expr.selectSingleNode("declarator/id"),
                    (Element)expr.selectSingleNode("initializer/*[1]"));
        }

        if (expr.getName().equals("assignExpression")) {
            return handleAssignment(cfg,
                    (Element)expr.elements().get(0),
                    (Element)expr.elements().get(1));
        }

        if (expr.getName().equals("id")) {
            return handleId(cfg, expr);
        }

        if (expr.getName().equals("arrayAccess")) {
            return handleArrayAccess(cfg, expr);
        }

        if (expr.getName().equals("intConst")
                || expr.getName().equals("sizeofExpression")
                || expr.getName().equals("stringConst")) {
            return new LocationPointerType(categorizationProvider);
        }

        if (expr.getName().equals("addrExpression")) {
            return handleAddrExpression(cfg, expr);
        }

        if (expr.getName().equals("derefExpression")) {
            return handleDerefExpression(cfg, expr);
        }

        if (expr.getName().equals("binaryExpression")) {
            // binary expression works just like an assignment
            return handleAssignment(cfg,
                    (Element)expr.elements().get(0),
                    (Element)expr.elements().get(1));
        }

        if (expr.getName().equals("functionCall")) {
            return handleFunctionCall(cfg, expr);
        }

        if (expr.getName().equals("postfixExpression")) {
            return handleExpression(cfg, (Element)expr.elements().get(0));
        }

        if (expr.getName().equals("prefixExpression")) {
            return handleExpression(cfg, (Element)expr.elements().get(0));
        }

        if (expr.getName().equals("dotExpression")) {
            return handleExpression(cfg, (Element)expr.elements().get(0));
        }

        if (expr.getName().equals("arrowExpression")) {
            return handleDerefExpression(cfg, expr);
        }

        if (expr.getName().equals("commaExpression")) {
            return handleCommaExpression(cfg, expr);
        }

        if (expr.getName().equals("conditionalExpression")) {
            return handleConditionalExpression(cfg, expr);
        }

        throw new UnsupportedOperationException(expr.getName());
    }


    private void handleReturnStatement(CFGHandle cfg, Element returnStatement) {
        // don't care about return, unless it returns something
        if (returnStatement.elements().size() == 0) {
            return;
        }

        LocationPointerType containingFunction = (LocationPointerType)
                typeTable.getTypeOf(cfg.getFunctionName()).getType();

        LocationPointerType returnVariable = (LocationPointerType)((FunctionPointerType)
                containingFunction.getLambda().getType()).returnParameter.getType();

        returnVariable.unifyWith(handleExpression(cfg, (Element)returnStatement.elements().get(0)));
    }

    private void handleStatement(CFGHandle cfg, Element statement) {
        if (statement.getName().equals("returnStatement")) {
            handleReturnStatement(cfg, statement);
            return;
        }

        if (statement.getName().equals("emptyStatement")) {
            return;
        }

        if (statement.getName().equals("breakStatement")) {
            return;
        }

        if (statement.getName().equals("assert")) {
            return;
        }

        // probably an expression statement
        handleExpression(cfg, statement);
    }

}
