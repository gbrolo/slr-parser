package StateMachine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Transformation
 * Transforms an NFA to a DFA.
 * Created by Gabriel Brolo on 27/07/2017.
 */
public class Transformation {
    private List<Transition> transitionList; // Transition List coming from AFN
    private List<Character> symbolList; // AFN's symbol List
    private List<State> finalStates; // AFN's final state
    private List<State> initialState; // AFN's initial state
    private HashMap<Integer, HashMap<String, List<State>>> transitionTable; // The first transition table
    private HashMap<List<State>, HashMap<String, List<State>>> dfaTable; // The reduced transition table for the dfa
    private List<List<State>> dfaStates; // states of the dfa without id
    private HashMap<List<State>, Integer> dfaStatesWithNumbering; // states of the dfa with an id for each one of them

    private int closureCont;

    public Transformation(List<Transition> transitionList, List<Character> symbolList, List<State> finalStates, List<State> initialState) {
        this.transitionList = transitionList;
        this.symbolList = symbolList;
        this.finalStates = finalStates;
        this.initialState = initialState;
        symbolList.add('ε');

        transitionTable = new HashMap<Integer, HashMap<String, List<State>>>();
        dfaTable = new HashMap<>();
        dfaStates = new LinkedList<>();
        dfaStatesWithNumbering = new HashMap<>();

        closureCont = 0;

        createTransitionTable();
        createDfaTable();
    }

    /**
     * Creates the transformed table with new states and transitions
     */
    private void createDfaTable() {
        // Do this for the initial state only

        HashMap<String, List<State>> first = transitionTable.get(initialState.get(0).getStateId());
        List<State> firstStateList = first.get("ε");
        dfaStates.add(firstStateList);

        // add rest of table
        for (int k = 0; k < dfaStates.size(); k++) {
            if (!dfaTable.containsKey(dfaStates.get(k))) {
                // add new state to table
                // first, traverse the state list (which is the new dfa state)
                HashMap<String, List<State>> tmpCols = new HashMap<>();

                for (int m = 0; m < this.symbolList.size()-1; m++) { // iterate over symbols
                    String currSymbol = Character.toString(symbolList.get(m));
                    List<State> symbolEpsilonKleene = new LinkedList<State>();
                    for (int i = 0; i < dfaStates.get(k).size(); i++) { // iterate over new state
                        State currState = dfaStates.get(k).get(i);

                        HashMap<String, List<State>> stateInfo = new HashMap<>();
                        stateInfo = transitionTable.get(currState.getStateId());

                        List<State> currSymbolList = stateInfo.get(currSymbol);
                        if (currSymbolList != null) {
                            for (int j = 0; j < currSymbolList.size(); j++) {
                                int currentIndex = currSymbolList.get(j).getStateId();
                                symbolEpsilonKleene.addAll(transitionTable.get(currentIndex).get("ε"));
                            }
                        }
                        tmpCols.put(currSymbol, symbolEpsilonKleene);
                    }
                    if (!dfaStates.contains(symbolEpsilonKleene)) {
                        if (symbolEpsilonKleene.size() > 0) {
                            dfaStates.add(symbolEpsilonKleene);
                        }
                    }
                }
                dfaTable.put(dfaStates.get(k), tmpCols);
            }
        }

        for (int i = 0; i < dfaStates.size(); i++) {
            dfaStatesWithNumbering.put(dfaStates.get(i), i);
        }
    }

    /**
     * Runs the e-closure on a determined state
     * @param initialState
     * @param tmpClosure
     * @return
     */
    private List<State> eClosure(State initialState, List<State> tmpClosure, State previousState) {
        // initialState is finalState of current Transition
        int cont = 0;
        for (int i = 0; i < this.transitionList.size(); i++) {
            if (transitionList.get(i).getInitialState() == initialState) {
                if (transitionList.get(i).getTransitionSymbol().equals("ε")) {
                    if (!tmpClosure.contains(transitionList.get(i).getFinalState())) {
                        tmpClosure.add(transitionList.get(i).getFinalState());
                        closureCont = 0;
                    }
                    if(!tmpClosure.contains(initialState)) {
                        if (transitionList.get(i).getFinalState().getStateId() != previousState.getStateId()) {
                            eClosure(transitionList.get(i).getFinalState(), tmpClosure, initialState);
                        }
                    }
                    if (tmpClosure.contains(initialState) && closureCont == 0) {
                        if (transitionList.get(i).getFinalState().getStateId() != previousState.getStateId()) {
                            eClosure(transitionList.get(i).getFinalState(), tmpClosure, initialState);
                        }
                        closureCont = 1;
                    }
                }
            }
        }
        return tmpClosure;
    }

    /**
     * Creates transition table from NFA.
     */
    private void createTransitionTable() {
        // start transitionTable only with states
        for (int i = 0; i < this.transitionList.size(); i++) {
            if (!transitionTable.containsKey(transitionList.get(i).getInitialState().getStateId())){
                List<State> tmpClosure = new LinkedList<State>();
                HashMap<String, List<State>> tmpCol = new HashMap<String, List<State>>();
                // traverse symbol list
                for (int j = 0; j < symbolList.size(); j++) {
                    String currSymbol = Character.toString(symbolList.get(j)); // get current symbol in symbolList
                    Transition currTransition = transitionList.get(i);

                    if (currTransition.getTransitionSymbol().equals(currSymbol)) {
                        tmpClosure = currTransition.getInitialState().getNextStates();
                        // if it is epsilon
                        if (currSymbol.equals("ε")) {
                            tmpClosure = eClosure(currTransition.getInitialState(), tmpClosure,
                                    currTransition.getInitialState()); // find closures
                            tmpClosure.add(currTransition.getInitialState());
                        }
                    } else {
                        if (currSymbol.equals("ε")) {
                            tmpClosure = new LinkedList<State>();
                            tmpClosure = eClosure(currTransition.getInitialState(), tmpClosure,
                                    currTransition.getInitialState()); // find closures
                            tmpClosure.add(currTransition.getInitialState());
                        } else {
                            tmpClosure = null;
                        }
                    }
                    tmpCol.put(currSymbol,tmpClosure);
                    closureCont = 0;
                }
                transitionTable.put(transitionList.get(i).getInitialState().getStateId(), tmpCol);
            }
        }
        // add final state to transitionTable
        List<State> tmpClosure = new LinkedList<State>();
        HashMap<String, List<State>> tmpCol = new HashMap<String, List<State>>();
        for (int k = 0; k < symbolList.size(); k++) {
            if (Character.toString(symbolList.get(k)).equals("ε")) {
                tmpClosure = new LinkedList<State>();
                tmpClosure.add(finalStates.get(0));
            } else {
                tmpClosure = null;
            }
            tmpCol.put(Character.toString(symbolList.get(k)),tmpClosure);
        }
        transitionTable.put(finalStates.get(0).getStateId(), tmpCol);
    }

    public HashMap<List<State>, HashMap<String, List<State>>> getDfaTable() { return this.dfaTable; }

    public HashMap<List<State>, Integer> getDfaStatesWithNumbering() { return this.dfaStatesWithNumbering; }

    public List<List<State>> getDfaStates() { return this.dfaStates; }

    public List<Character> getSymbolList() { return this.symbolList; }
}
