package StateMachine;

import Syntax.ExpressionSimplifier;
import Syntax.PostFix;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * AFN
 * An AFN constructed from a regExp using McNaughton-Yamada-Thompson's algorithm.
 * Output @AFN.txt: a txt file with the contents of the AFN.
 * Created by Gabriel Brolo on 22/07/2017.
 */
public class AFN {
    private PostFix postFix; // to handle infix to postfix
    private String regExp; // the regExp in infix
    private String postFixRegExp; // the regExp in postfix
    private List<Character> symbolList; // AFN's symbol list
    private List<Transition> transitionsList; // AFN's transitions list
    private List<State> finalStates; // AFN's acceptation states list
    private List<State> initialState; // AFN's initial state
    private List<String> states; // AFN's states

    public static int stateCount; // id for States
    private int closureCont;

    /* To save state reference */
    private State saveFinal;
    private Stack<State> stackInitial;
    private Stack<State> stackFinal;
    private boolean dContainsFinalState;

    /* Abbrebiations and yuxtaposition concatenation */
    private ExpressionSimplifier expressionSimplifier;

    /**
     * Constructor
     * @param regExp
     */
    public AFN(String regExp) {
        stateCount = 0;
        this.regExp = regExp;
        expressionSimplifier = new ExpressionSimplifier(this.regExp);
        postFix = new PostFix();
        postFixRegExp = PostFix.infixToPostfix(expressionSimplifier.getRegExp());
        //System.out.println("Simplified: " + expressionSimplifier.getRegExp());
        symbolList = new LinkedList<Character>();
        transitionsList = new LinkedList<Transition>();
        finalStates = new LinkedList<State>();
        initialState = new LinkedList<State>();
        states = new LinkedList<String>();
        stackInitial = new Stack<>();
        stackFinal = new Stack<>();
        closureCont = 0;
        computeSymbolList();
        regExpToAFN();
        computeStateList();
        computeInitialState();
    }

    /**
     * Simulates AFN to search if a string belongs to the language
     * @param input
     * @return
     */
    public String extendedDelta(String input) {
        System.out.println("Starting NFA Simulation...");
        long start = System.nanoTime();
        List<State> currentStateList = initialState;
        List<State> tmpStateList = new LinkedList<>();
        for (int i = 0; i < input.length(); i ++) {
            if (tmpStateList.size() > 0) {
                currentStateList = tmpStateList;
            }
            for (int j = 0; j < currentStateList.size(); j++) {
                String currentChar = Character.toString(input.charAt(i));
                State currentState = currentStateList.get(j);
                for (int k = 0; k < transitionsList.size(); k++) {
                    if ((transitionsList.get(k).getInitialState() == currentState) &&
                            (transitionsList.get(k).getTransitionSymbol().equals(currentChar))) {
                        State nextState = transitionsList.get(k).getFinalState();
                        tmpStateList = eClosure(nextState, new LinkedList<>(), nextState);
                        tmpStateList.add(nextState);
                        if (tmpStateList.contains(finalStates.get(0))) {
                            dContainsFinalState = true;
                        }
                        System.out.println("State List: " + tmpStateList.toString());
                        k = transitionsList.size();
                    } else if ((transitionsList.get(k).getInitialState() == currentState) &&
                            (transitionsList.get(k).getTransitionSymbol().equals("ε"))) {
                        State nextState = transitionsList.get(k).getFinalState();
                        tmpStateList = eClosure(nextState, currentStateList, nextState);
                        tmpStateList.add(nextState);
                        k = transitionsList.size();
                    }
                    closureCont = 0;
                }
            }
        }

        //System.out.println("NFA Simulation terminated.");

        long finish = System.nanoTime();
        long duration = (finish - start);

        // traverse the list of final states to see if currentState is a final state
        String isInLanguage = "";
        if(currentStateList.contains(finalStates.get(0))) {
            isInLanguage = "YES";
        } else {
            isInLanguage = "NO";
        }

        if (dContainsFinalState) {
            isInLanguage = "YES";
        }

        dContainsFinalState = false;

        String output = "ACCEPTED WORD?: " + isInLanguage + ".\nSEARCH TOOK " + duration + "ns.";
        return output;
    }

    /**
     * Runs the ε-closure for a determined state
     * @param initialState
     * @param tmpClosure
     * @return
     */
    private List<State> eClosure(State initialState, List<State> tmpClosure, State previousState) {
        // initialState is finalState of current Transition
        int cont = 0;
        for (int i = 0; i < this.transitionsList.size(); i++) {
            if (transitionsList.get(i).getInitialState() == initialState) {
                if (transitionsList.get(i).getTransitionSymbol().equals("ε")) {
                    if (!tmpClosure.contains(transitionsList.get(i).getFinalState())) {
                        tmpClosure.add(transitionsList.get(i).getFinalState());
                        closureCont = 0;
                    }
                    if(!tmpClosure.contains(initialState)) {
                        if (transitionsList.get(i).getFinalState().getStateId() != previousState.getStateId()) {
                            eClosure(transitionsList.get(i).getFinalState(), tmpClosure, initialState);
                        }
                    }
                    if (tmpClosure.contains(initialState) && closureCont == 0) {
                        if (transitionsList.get(i).getFinalState().getStateId() != previousState.getStateId()) {
                            eClosure(transitionsList.get(i).getFinalState(), tmpClosure, initialState);
                        }
                        closureCont = 1;
                    }
                }
            }
        }
        return tmpClosure;
    }

    /**
     * Checks for symbols in postFixRegExp and adds them to symbolList
     */
    private void computeSymbolList() {
        for (int i = 0; i < postFixRegExp.length(); i++) {
            if(!PostFix.precedenceMap.containsKey(postFixRegExp.charAt(i))) {
                if (!symbolList.contains(postFixRegExp.charAt(i))) {
                    symbolList.add(postFixRegExp.charAt(i));
                    Collections.sort(symbolList);
                }
            }
        }
    }

    /**
     * Adds AFN's states to stateList
     */
    public void computeStateList() {
        for (int i = 0; i < transitionsList.size(); i++) {
            if (!states.contains(transitionsList.get(i).getInitialState().toString())) {
                states.add(transitionsList.get(i).getInitialState().toString());
            }

            if (!states.contains(transitionsList.get(i).getFinalState().toString())) {
                states.add(transitionsList.get(i).getFinalState().toString());
            }
        }
    }

    /**
     * Adds AFN's initial state to list
     */
    public void computeInitialState() {
        State initial = stackInitial.pop();
        initial.setInitial(true);
        stackInitial.push(initial);
        initialState.add(stackInitial.pop());
    }

    /**
     * The real deal.
     * Parses the expression to an AFN.
     */
    private void regExpToAFN(){
        // valid expression stored at postFixRegExp
        // new transition for first symbol
        // from here, every symbol has a Transition that represents it

        // Traverse the rest of the expression
        for (int i = 0; i < postFixRegExp.length(); i ++) {
            // if character at i is in symbolList
            if (symbolList.contains(postFixRegExp.charAt(i))) {
                Transition tr1 = new Transition(Character.toString(postFixRegExp.charAt(i)));
                transitionsList.add(tr1);

                State initialState = tr1.getInitialState();
                State finalState = tr1.getFinalState();

                stackInitial.push(initialState);
                stackFinal.push(finalState);
            } else if (Character.toString(postFixRegExp.charAt(i)).equals("|")) {
                // if char is OR
                State lowerInitial = stackInitial.pop();
                State lowerFinal = stackFinal.pop();
                State upperInitial = stackInitial.pop();
                State upperFinal = stackFinal.pop();

                unify(upperInitial, upperFinal, lowerInitial, lowerFinal);
            } else if (Character.toString(postFixRegExp.charAt(i)).equals("*")) {
                //if char is kleene
                State initialState = stackInitial.pop();
                State finalState = stackFinal.pop();

                kleene(initialState, finalState);
            } else if (Character.toString(postFixRegExp.charAt(i)).equals(".")) {
                // if char is concatenation
                saveFinal = stackFinal.pop();
                State finalState = stackFinal.pop();
                State initialState = stackInitial.pop();

                concatenate(finalState, initialState);
            }

            if (i == postFixRegExp.length()-1) {
                State finalState = stackFinal.pop();
                finalState.setFinal(true);
                stackFinal.push(finalState);
                finalStates.add(stackFinal.pop());
                if (symbolList.contains('ε')) {
                    symbolList.remove(symbolList.indexOf('ε'));
                }
            }
        }
    }

    /**
     * Union | in Thompson's algorithm
     * @param upperInitialState
     * @param upperFinalState
     * @param lowerInitialState
     * @param lowerFinalState
     */
    private void unify(State upperInitialState, State upperFinalState, State lowerInitialState, State lowerFinalState) {
        // create two new states
        State in = new State(stateCount);
        State out = new State(stateCount);

        // create new transitions
        Transition tr1 = new Transition("ε", in, upperInitialState);
        Transition tr2 = new Transition("ε", in, lowerInitialState);

        Transition tr3 = new Transition("ε", upperFinalState, out);
        Transition tr4 = new Transition("ε", lowerFinalState, out);

        transitionsList.add(tr1);
        transitionsList.add(tr2);
        transitionsList.add(tr3);
        transitionsList.add(tr4);

        stackInitial.push(in);
        stackFinal.push(out);
    }

    /**
     * Concatenation in Thompson's algorithm
     * @param initialState
     * @param finalState
     */
    private void concatenate(State initialState, State finalState) {
        Transition tr1 = new Transition("ε", initialState, finalState);
        transitionsList.add(tr1);

        stackFinal.push(saveFinal);
        saveFinal = null;
    }

    /**
     * Kleene star in Thompson's algorithm
     * @param initialState
     * @param finalState
     */
    private void kleene(State initialState, State finalState) {
        State in = new State(stateCount);
        State out = new State(stateCount);

        Transition tr1 = new Transition("ε", finalState, initialState); // upper kleene part
        Transition tr2 = new Transition("ε", in, out); // lower kleene part
        Transition tr3 = new Transition("ε", in, initialState); // initial to initial of expression
        Transition tr4 = new Transition("ε", finalState, out); // final of expression to out

        transitionsList.add(tr1);
        transitionsList.add(tr2);
        transitionsList.add(tr3);
        transitionsList.add(tr4);

        stackInitial.push(in);
        stackFinal.push(out);
    }

    public List<Character> getSymbolList () {
        return this.symbolList;
    }

    public List<Transition> getTransitionsList () {
        return this.transitionsList;
    }

    public List<State> getFinalStates () {
        return this.finalStates;
    }

    public List<String> getStates () {
        return this.states;
    }

    public List<State> getInitialState () {
        return this.initialState;
    }

    public String getPostFixRegExp() { return this.postFixRegExp; }

}
