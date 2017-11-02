package LR0;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Gabriel Brolo on 26/10/2017.
 */
public class LR0Node {
    // to check if its an initial or a final state
    private boolean initialState;
    private boolean finalState;

    // control
    private int id;
    private List<LR0Node> previousNodes;
    private List<LR0Node> nextNodes;

    // lr0
    private List<String> extProductions;
    private List<String> transitionSymbols;
    private String firstProd;

    public LR0Node() {
        this.id = LR0DFA.numberOfNodes;
        LR0DFA.numberOfNodes++;
        previousNodes = new LinkedList<>();
        nextNodes = new LinkedList<>();
        extProductions = new LinkedList<>();
        transitionSymbols = new LinkedList<>();
    }

    public LR0Node(List<String> extProductions) {
        this.id = LR0DFA.numberOfNodes;
        LR0DFA.numberOfNodes++;
        previousNodes = new LinkedList<>();
        nextNodes = new LinkedList<>();
        this.extProductions = extProductions;
        transitionSymbols = new LinkedList<>();
    }

    public void setInitialState(boolean initialState) { this.initialState = initialState; }
    public void setFinalState(boolean finalState) { this.finalState = finalState; }
    public void addPreviousNode(LR0Node node) { previousNodes.add(node); }
    public void addNextNode(LR0Node node) { nextNodes.add(node); }
    public void addProduction(String production) { extProductions.add(production); }
    public void addTransitionSymbol(String symbol) { transitionSymbols.add(symbol); }
    public void setFirstProd(String firstProd) { this.firstProd = firstProd; }

    public boolean isInitialState() { return initialState; }
    public boolean isFinalState() { return finalState; }
    public int getId() { return id; }
    public List<LR0Node> getPreviousNodes() { return previousNodes; }
    public List<LR0Node> getNextNodes() { return nextNodes; }
    public List<String> getExtProductions() { return extProductions; }
    public List<String> getTransitionSymbols() { return transitionSymbols; }
    public String getFirstProd() { return firstProd; }

    public String toString() { return String.valueOf(id); }
    public String showContent() { return extProductions.toString(); }

}
