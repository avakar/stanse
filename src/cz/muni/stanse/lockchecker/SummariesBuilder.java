package cz.muni.stanse.lockchecker;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import cz.muni.stanse.codestructures.ArgumentPassingManager;
import cz.muni.stanse.codestructures.CFGHandle;
import cz.muni.stanse.codestructures.CFGNode;
import cz.muni.stanse.codestructures.CFGsNavigator;

/**
 * Class used to build summaries
 *
 * @author Radim Cebis
 */
class SummariesBuilder {
	private ArgumentPassingManager passingManager;
	private Summaries summaries;
	private Map<CFGNode, CFGHandle> dictionary;
	private CallStack callStack = new CallStack();
	private Configuration conf;

	private final static Logger logger =
		Logger.getLogger(SummariesBuilder.class.getName());

	/**
	 * Constructs summaries builder
	 * @param dictionary to translate nodes to handles
	 * @param passingManager passing manager
	 * @param conf configuration
	 */
	public SummariesBuilder(final Map<CFGNode, CFGHandle> dictionary,
			final ArgumentPassingManager passingManager,
			final Configuration conf) {
		super();
		this.summaries = new Summaries(dictionary, conf);
		this.dictionary = dictionary;
		this.passingManager = passingManager;
		this.conf = conf;
	}

	private CFGNode traverseCall(final CFGsNavigator navigator,
			final FunctionStateSummary summary,
			final CFGStates cfgStates, final CFGNode parent) {
		CFGNode propagationNode = parent;
		final CFGNode child = navigator.getCalleeStart(parent);
		cfgStates.propagate(propagationNode, child);

		final VarTransformations varTrans = new VarTransformations();
		for (final Element el : Util.getArguments(parent.getElement())) {
			final String id = VarTransformations.makeArgument(el);
			// TODO change this if you change VarTransformations.makeArgument
			final String newId = passingManager.pass(parent, id,
				child);
			varTrans.addTransformation(newId, id);
		}

		// get entering state for a callee
		State enterState = State.getRenamedCFGState(
			cfgStates.get(child), varTrans, true);
		// remove unusable state
		cfgStates.remove(child);

		/*
		 * do this only if it is not a recursive call, otherwise normal
		 * propagation takes place
		 */
		if (!callStack.contains(child, enterState)) {
			final FunctionStateSummary sum =
				traverse(child, navigator,
					enterState);
			/*
			 * join callee's summary with the caller's - transfer to
			 * caller's namespace
			 */
			summary.join(sum, varTrans);

			// propagate from callee's exit
			propagationNode = navigator.getCalleeEnd(parent);
			/*
			 * save a callee's exit state transferred to the
			 * caller's namespace
			 */
			cfgStates.put(propagationNode,
				State.getRenamedCFGState(sum.getOutputState(),
				varTrans, false));
		}
		return propagationNode;
	}

	/**
	 * Traverses CFG from the start node using the start state as a starting
	 * state
	 * @param startNode from which to start a traverse
	 * @param navigator
	 * @param startState initial state
	 * @return computed FunctionStateSummary for given parameters
	 */
	public FunctionStateSummary traverse(final CFGNode startNode,
			final CFGsNavigator navigator, State startState) {
		// use defensive copy
		startState = new State(startState);

		// hit the cache
		final FunctionSummary fs = summaries.get(startNode);
		FunctionStateSummary summary = fs.getFromCache(startState);
		if (summary != null)
			return summary;

		summary = fs.get(startState);

		final State originalStartState =
			summaries.getRepos().get(startState);
		callStack.add(startNode, originalStartState);

		final CFGStates cfgStates = new CFGStates(startNode, startState,
			summary.getErrHolder(), conf);
		final Deque<CFGNode> stack = new ArrayDeque<CFGNode>();

		stack.addFirst(startNode);
		summary.changeVarsOccurrence(1, startNode, startState);

		while (!stack.isEmpty()) {
			final CFGNode parent = stack.pollFirst();
			// propagate from parent
			CFGNode propagationNode = parent;

			if (navigator.isCallNode(parent)) {
				propagationNode = traverseCall(navigator,
					summary, cfgStates, parent);
			}

			/*
			 * propagate the state to children and change the
			 * occurrences count
			 */
			for (final CFGNode child : parent.getSuccessors()) {
				State oldState = null;
				if (cfgStates.get(child)!= null)
					oldState = new State(cfgStates.get(child));

				if (cfgStates.propagate(propagationNode, child))
					stack.addFirst(child);

				if (oldState != null) {
					// remove old occurrences
					summary.changeVarsOccurrence(-1, child,
						oldState);
				}
				// add new occurrences
				summary.changeVarsOccurrence(1, child,
					cfgStates.get(child));
			}
		}
		callStack.remove(startNode, originalStartState);
		State output = cfgStates.get(
			dictionary.get(startNode).getEndNode());
		/*
		 * this is used when the end node is unreachable from the start
		 * node (it can happen because of compiler optimization).
		 * Use originalStartState then
		 */
		if (output == null)
			output = originalStartState;
		summary.setOutputState(output);
		printCfgStates(dictionary.get(startNode).getFunctionName(),
			startState, summary.getOutputState(), cfgStates);
		return summary;
	}

	/**
	 * Pretty print cfg's node's states
	 *
	 * @param functionName
	 * @param startState
	 * @param endState
	 * @param cfgStates
	 */
	private static void printCfgStates(final String functionName,
			final State startState, final State endState,
			final CFGStates cfgStates) {
		if (!logger.isDebugEnabled())
			return;
		final StringBuilder sb = new StringBuilder();
		sb.append("/////////////////////////////////////////////\n");
		sb.append("/////////////////////////////////////////////\n");
		sb.append("Function ").append(functionName).append(" CFG states\n");
		sb.append("Function entered in state: ").append(startState).append('\n');
		sb.append("Function left in state: ").append(endState).append('\n');
		sb.append(cfgStates);
		logger.debug(sb.toString());
	}

	/**
	 * @return this instance's summaries object
	 */
	public Summaries getSummaries() {
		return summaries;
	}

	@Override
	public String toString() {
		return summaries.toString();
	}
}
