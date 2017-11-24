package LR0;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * Created by Gabriel Brolo on 26/10/2017.
 */
public class LR0DFA {
    public static int numberOfNodes;
    public static int productionsIds;

    private List<LR0Transition> trList;
    private List<LR0Node> finalNodes;
    private List<LR0Node> nodes;
    private LR0Node initialNode;

    private Stack<LR0Node> nodesFProds;

    public LR0DFA() {
        numberOfNodes = 0;
        productionsIds = 1;
        trList = new LinkedList<>();
        finalNodes = new LinkedList<>();
        nodes = new LinkedList<>();
        nodesFProds = new Stack<>();
    }

    public void addTransition(LR0Transition tr) { trList.add(tr); }
    public void addInitialNode(LR0Node initialNode) { this.initialNode = initialNode; }
    public void addFinalNode(LR0Node finalNode) { finalNodes.add(finalNode); }
    public void addNode(LR0Node node) { nodes.add(node); }

    public LR0Node getInitialNode() { return initialNode; }
    public List<LR0Node> getNodes() { return nodes; }
    public List<LR0Node> getFinalNodes() { return finalNodes; }
    public Stack<LR0Node> getNodesFProds() { return nodesFProds; }
    public List<LR0Transition> getTrList () { return trList; }

    public void recalculateFinalNodes() {
        for (LR0Node node : nodes) {
            List<String> prods = node.getExtProductions();

            for (String prod : prods) {
                if (prod.charAt(prod.length()-1) == '.') {
                    node.setFinalState(true);
                    break;
                }
            }
        }
    }

    public boolean hasThisFirstProd(String fProd) {
        for (LR0Node node : nodes) {
            if (node.getFirstProd().equals(fProd)) {
                nodesFProds.push(node);
                return true;
            }
        }
        // else...
        return false;
    }

    public void reassureInitialNodeIsIn () {
        if (!nodes.contains(initialNode)) {
            nodes.add(initialNode);
        }
    }

    public String toString() {
        String nodesContent = "";
        reassureInitialNodeIsIn();
        recalculateFinalNodes();
        for (LR0Node node : nodes) {
            nodesContent = nodesContent + "\nNODE: " + node.toString() + " HAS: " + node.showContent();
        }
        return "INITIAL NODE: " + initialNode.toString() +
                "\nFINAL NODES: " + finalNodes.toString() +
                "\nNODES CONTENT: \n" + nodesContent +
                "\nTRANSITIONS: " + trList.toString();
    }

}
