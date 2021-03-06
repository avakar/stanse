package cz.muni.stanse.lockchecker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.dom4j.Element;

import com.inamik.utils.SimpleTableFormatter;
import com.inamik.utils.TableFormatter;

import cz.muni.stanse.codestructures.CFGHandle;
import cz.muni.stanse.codestructures.CFGNode;

/**
 * This class represents summary for a given function with given entered state
 *
 * @author Radim Cebis
 */
class FunctionStateSummary {
	// var id, var state, map of occurrences with counters (how many times control flows through it
	private Map<String, Occurrences> varOccurrences = new HashMap<String, Occurrences>();
	// for stored states use StateRepository containing unmodifiable stance instance
	private StateRepository repos;
	// keeps outputstate
	private State outputState;
	// keeps errors of this function
	private ErrorHolder errHolder;
	private Map<CFGNode, CFGHandle> dictionary;
	private CFGNode startNode;
	private State startState;
	private Configuration conf;

	/**
	 * Constructs function state summary for a given startNode and startState
	 *
	 * @param dictionary
	 * @param repos State repository
	 * @param startNode this summary's function start node
	 * @param startState this summary's function start state
	 * @param conf Configuration
	 */
	public FunctionStateSummary(final Map<CFGNode, CFGHandle> dictionary,
			final StateRepository repos, final CFGNode startNode,
			final State startState, final Configuration conf) {
		this.errHolder = new ErrorHolder(dictionary, startState,
			startNode);
		this.dictionary = dictionary;
		this.repos = repos;
		this.startNode = startNode;
		this.startState = startState;
		this.conf = conf;
	}

	/**
	 * Sets output state of this function
	 * @param outputState
	 */
	public void setOutputState(State outputState) {
		this.outputState = repos.get(outputState);
	}

	/**
	 * @return output state of this function
	 */
	public State getOutputState() {
		return outputState;
	}

	/**
	 * Adds variable's occurrence
	 *
	 * @param increment how much increment the occurrence - should be 1 or -1
	 * @param variable identifier
	 * @param state for which add an occurrence
	 * @param node which contains the occurrence
	 */
	private void addVarOccurrence(int increment, final String variable,
			final State state, final CFGNode node) {
		Occurrences stateOccurrence = varOccurrences.get(variable);
		if (stateOccurrence == null) {
			stateOccurrence = new Occurrences(repos);
			varOccurrences.put(variable, stateOccurrence);
		}
		stateOccurrence.addOccurrence(state, node, increment);
	}

	/**
	 * @return error holder of this summary
	 */
	public ErrorHolder getErrHolder() {
		return errHolder;
	}

	/**
	 * Changes variable occurrence for the given node and state
	 *
	 * @param increment how much increment the occurrence - should be 1 or -1
	 * @param node containing the occurrence
	 * @param state for which occurrence should be changed
	 */
	public void changeVarsOccurrence(int increment,
			final CFGNode node, final State state) {
		// count occurrences
		final Element el = node.getElement();

		for(final String id : Util.getIDsInElement(el, conf))
			addVarOccurrence(increment, id, state, node);
	}

	/**
	 * @return all states of all variables in this summary
	 */
	private Set<State> getAllStates() {
		final Set<State> res = new HashSet<State>();
		for(final Occurrences occ : varOccurrences.values())
			res.addAll(occ.getAllStates());
		return res;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Function: ").
			append(dictionary.get(startNode).getFunctionName()).
			append('\n');
		builder.append("Function entered in state: ").
			append(startState).append('\n');
		builder.append("Function left in state: ").append(outputState).
			append('\n');

		final Set<State> allStates = getAllStates();

		// true = show border
		TableFormatter tf = new SimpleTableFormatter(true).nextRow().
			nextCell().addLine("Variable/State");
		for(final State state : allStates)
			tf.nextCell().addLine(state.toString());

		tf.nextCell().nextCell().addLine("SUM").nextRow();
		for (final Entry<String, Occurrences> entry :
				varOccurrences.entrySet()) {
			tf.nextCell().addLine(entry.getKey());
			int sum = 0;
			for (final State state : allStates) {
				int occ = 0;
				int flows = 0;
				if (entry.getValue() != null && entry.
						getValue().get(state)!= null) {
					for (final Counter i : entry.getValue().
							get(state).values())
						flows += i.get();
					occ += entry.getValue().get(state).
						keySet().size();
				}
				sum += occ;
				tf.nextCell().addLine(Integer.toString(occ) +
					" occurrences, " +
					Integer.toString(flows) + " flows");
			}
			tf.nextCell().nextCell().addLine(String.valueOf(sum)).
				nextRow();
		}

		final String[] table = tf.getFormattedTable();
		for (int i = 0, size = table.length; i < size; i++) {
			builder.append(table[i]);
			builder.append('\n');
		}
		builder.append("/////////////////////////////////////////////\n");
		return builder.toString();
	}



	/**
	 * Joins this summary with the summary of the called function
	 * @param summary of the called function
	 * @param varTransformations between this and called function
	 */
	public void join(FunctionStateSummary summary,
			VarTransformations varTransformations) {
		for (final Entry<String, Occurrences> entry :
				summary.varOccurrences.entrySet()) {
			String varId = varTransformations.transform(
				entry.getKey(), false);
			// this variable id needs to be changed when joined
			if (varId != null) {
				add(varId, entry.getValue(), varTransformations);
			/*
			 * if it is not a parameter and it is not a local
			 * variable then join it
			 */
			} else if(!dictionary.get(summary.startNode).
					getSymbols().contains(entry.getKey())) {
				add(entry.getKey(), entry.getValue(),
					varTransformations);
			}
		}
	}

	private void add(final String varId, final Occurrences occurrencesToAdd,
			final VarTransformations varTransformations) {
		Occurrences stateOccurrence = varOccurrences.get(varId);
		if (stateOccurrence == null) {
			stateOccurrence = new Occurrences(repos);
			varOccurrences.put(varId, stateOccurrence);
		}
		stateOccurrence.addOcurrences(occurrencesToAdd, varTransformations);
	}

	/**
	 * @return map of variable occurrences (identifier, Occurrences)
	 */
	public Map<String, Occurrences> getVarOccurrences() {
		return varOccurrences;
	}

	/**
	 * @return dictionary used to translate between node and handle
	 */
	public Map<CFGNode, CFGHandle> getDictionary() {
		return dictionary;
	}

	/**
	 * @return start node of this summary's function
	 */
	public CFGNode getStartNode() {
		return startNode;
	}

	/**
	 * @return start state of this summary's function
	 */
	public State getStartState() {
		return startState;
	}
}
