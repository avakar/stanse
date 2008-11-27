package cz.muni.stanse.automatonchecker;

import java.util.Set;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

import cz.muni.stanse.xml2cfg.ControlFlowGraph;

public final class AutomatonChecker extends cz.muni.stanse.checker.Checker {

    // public section

    public AutomatonChecker(
                      final Set<cz.muni.stanse.xml2cfg.ControlFlowGraph> CFGs,
                      final org.dom4j.Document XMLdefinition) throws Exception {
        super(CFGs);
        XMLAutomatonDefinition = new XMLAutomatonDefinition(
                                                XMLdefinition.getRootElement());
    }

    @Override
    public String getName() {
        return "Automaton checker [" +
               getXMLAutomatonDefinition().getAutomatonName() +
               "]";
    }

    @Override
    public List<cz.muni.stanse.checker.CheckerError> check() throws Exception {
        final HashMap<cz.muni.stanse.xml2cfg.CFGEdge,PatternLocation>
            edgeLocationDictionary = PatternLocationBuilder.
                   buildPatternLocations(getCFGs(),getXMLAutomatonDefinition());

        final LinkedList<PatternLocation> progressQueue =
                new LinkedList<PatternLocation>();
        for (final ControlFlowGraph cfg : getCFGs())
            progressQueue.add(edgeLocationDictionary.get(cfg.getEntryEdge()));

        while (!progressQueue.isEmpty()) {
            final PatternLocation currentLocation = progressQueue.remove();
            if (!currentLocation.hasUnprocessedAutomataStates())
                continue;
            final boolean successorsWereAffected =
                currentLocation.processUnprocessedAutomataStates();
            if (successorsWereAffected)
                progressQueue.addAll(
                        currentLocation.getSuccessorPatternLocations());
        }
        return CheckerErrorBuilder.buildErrorList(edgeLocationDictionary);
    }

    // private section

    private XMLAutomatonDefinition getXMLAutomatonDefinition() {
        return XMLAutomatonDefinition;
    }

    private final XMLAutomatonDefinition XMLAutomatonDefinition;
}