package LR0;

/**
 * Created by Gabriel Brolo on 26/10/2017.
 */
public class LR0Transition {
    private LR0Node initialNode;
    private LR0Node finalNode;
    private String trSymbol;

    public LR0Transition(String trSymbol) {
        this.trSymbol = trSymbol;
        initialNode = new LR0Node();
        finalNode = new LR0Node();

        // link between nodes
        initialNode.addNextNode(finalNode);
        finalNode.addPreviousNode(initialNode);
    }

    public LR0Transition(String trSymbol, LR0Node initialNode, LR0Node finalNode) {
        this.trSymbol = trSymbol;
        this.initialNode = initialNode;
        this.finalNode = finalNode;

        // link between nodes
        initialNode.addNextNode(finalNode);
        finalNode.addPreviousNode(initialNode);
    }

    public LR0Node getInitialNode() { return initialNode; }
    public LR0Node getFinalNode() { return finalNode; }
    public String getTrSymbol() { return trSymbol; }

    public void setTrSymbol(String trSymbol) { this.trSymbol = trSymbol; }

    public String toString() { return initialNode.toString() + " - " + trSymbol + " - " + finalNode.toString(); }
}
