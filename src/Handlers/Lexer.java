package Handlers;

import LR0.LR0DFA;
import LR0.LR0Node;
import LR0.LR0Transition;
import StateMachine.AFN;
import StateMachine.DFA;
import StateMachine.TokenNode;
import StateMachine.Transformation;

import java.util.*;
import java.util.Set;

/**
 * Lexer
 * Lexer object with the capabilities to analyze inputs according to language specification.
 *
 * Created by Gabriel Brolo on 01/09/2017.
 */
public class Lexer {
    private HashMap<String, String> keywordsMap; // <String, ident>
    private HashMap<String, String> charactersMap; // <ident, String>
    private HashMap<String, String> reversedCharactersMap; // <String, ident>
    private HashMap<String, String> identRegexMap; // <ident, regex>
    private HashMap<String, String> tokenRegexMap;
    private HashMap<String, DFA> tokensMap; // <ident, dfa>
    private HashMap<DFA, String> reversedTokensMap; // <dfa, ident>
    private HashMap<String, String> anyRegexMap; // <regex, ident>
    private HashMap<String, List<String>> productions; // <head, body> of productions
    private HashMap<String, List<String>> extProductions; // extended productions por LR(0) automaton
    private HashMap<String, List<String>> first; // <head, symbols>
    private HashMap<String, List<String>> follow; //<head, symbols>
    private HashMap<String, List<String>> lr0Closures; // closures to use in automaton
    private HashMap<Integer, String> specialTerminals; // to have a record of substitution in automaton creation
    private HashMap<String, Integer> revSpTerminals;
    private List<String> specialTerminalsFound;
    private List<String> anyRegexes; // list of regex for ANY
    private List<DFA> characterDFAs; // DFA's of characters
    private List<DFA> tokensDFAs; // DFA's of tokens
    private List<String> whiteSpaceList; // List of whitespace characters
    private HashMap<DFA, String> reversedCharactersDFAs; // <DFA, ident>
    private List<TokenNode> lexedValues; // List of tokens
    private List<TokenNode> temporaryLexedValues;
    private List<String> terminals; // List of terminals inside productions
    private List<String> nonTerminals; // List of non-terminals inside productions
    private List<String> productionSymbols; // List of symbols found in all of the productions
    private List<String> commonTerminals; // List of common terminals
    private List<String[]> equalFollows; // to save equal follows
    private String startSymbol; // Start symbol of productions

    private HashMap<Integer, List<HashMap>> parsingTable;
    private HashMap<String, Integer> productionsIdsMap;
    private HashMap<Integer, String> productionsIdsMapRev;
    private Stack<Object> stackParsing;

    private boolean conflicts;

    private LR0DFA dfa;

    static public final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";

    public int specialTerminalsCounter;

    /**
     * Init
     */
    public Lexer () {
        keywordsMap = new HashMap<>();
        charactersMap = new HashMap<>();
        tokenRegexMap = new HashMap<>();
        reversedCharactersMap = new HashMap<>();
        reversedCharactersDFAs = new HashMap<>();
        tokensMap = new HashMap<>();
        reversedTokensMap = new HashMap<>();
        identRegexMap = new HashMap<>();
        anyRegexMap = new HashMap<>();
        productions = new HashMap<>();
        first = new HashMap<>();
        follow = new HashMap<>();
        extProductions = new HashMap<>();
        lr0Closures = new HashMap<>();
        specialTerminals = new HashMap<>();
        revSpTerminals = new HashMap<>();
        anyRegexes = new LinkedList<>();
        characterDFAs = new LinkedList<>();
        tokensDFAs = new LinkedList<>();
        lexedValues = new LinkedList<>();
        temporaryLexedValues = new LinkedList<>();
        whiteSpaceList = new LinkedList<>();
        terminals = new LinkedList<>();
        nonTerminals = new LinkedList<>();
        productionSymbols = new LinkedList<>();
        commonTerminals = new LinkedList<>();
        equalFollows = new LinkedList<>();
        specialTerminalsFound = new LinkedList<>();

        parsingTable = new HashMap<>();
        productionsIdsMap = new HashMap<>();
        productionsIdsMapRev = new HashMap<>();
        stackParsing = new Stack<>();
        conflicts = false;

        dfa = new LR0DFA();
        specialTerminalsCounter = 0;

        commonTerminals.add("+");
        commonTerminals.add("-");
        commonTerminals.add("*");
        commonTerminals.add("(");
        commonTerminals.add(")");
    }

    public void addProduction(String production) {
        // add symbols, terminals and non-terminals
        addProdSymbolsTerminals(production);

        String prod = production.replaceAll("\\s+","");
        String[] splittedProd = prod.split("=");
        String[] splittedBody = splittedProd[1].split("\\|");

        for(int k = 0; k < splittedBody.length; k++) {
            for (int i = 0; i < splittedBody[k].length(); i++) {
                if (splittedBody[k].charAt(i) == '\'') {
                    if ((i+2) < splittedBody[k].length()) {
                        if (splittedBody[k].charAt(i+2) == '\'') {
                            String leftmost = splittedBody[k].substring(0, i);
                            String rightmost = splittedBody[k].substring(i+3, splittedBody[k].length());
                            String body = leftmost + splittedBody[k].charAt(i+1) + rightmost;
                            splittedBody[k] = body;
                            i = i+2;
                        }
                    }
                }
            }
        }

        List<String> body = new LinkedList<>();
        for (String bd : splittedBody) {
            body.add(bd);
        }
        if (productions.size() == 0) {
            startSymbol = splittedProd[0];
        }

        if (productions.containsKey(splittedProd[0])) {
            List<String> bdlst = productions.get(splittedProd[0]);
            bdlst.addAll(body);
            productions.put(splittedProd[0], bdlst);

            // productionsIdsMap
            for (String bd : bdlst) {
                String product = splittedProd[0] + "→" + bd;
                if (!productionsIdsMap.containsKey(product)) {
                    productionsIdsMap.put(product, dfa.productionsIds);

                    if (!productionsIdsMapRev.containsKey(dfa.productionsIds)) {
                        productionsIdsMapRev.put(dfa.productionsIds, product);
                        dfa.productionsIds++;
                    }
                }


            }
        } else {
            productions.put(splittedProd[0], body);

            // productionsIdsMap
            String product = splittedProd[0] + "→" + body.get(0);
            if (!productionsIdsMap.containsKey(product)) {
                productionsIdsMap.put(product, dfa.productionsIds);

                if (!productionsIdsMapRev.containsKey(dfa.productionsIds)) {
                    productionsIdsMapRev.put(dfa.productionsIds, product);
                    dfa.productionsIds++;
                }
            }


        }
    }

    public void extendProductions() {
        // create a new production S' -> startSymbol
        List<String> initialNode = new LinkedList<>();

        List<String> spBody = new LinkedList<>();
        spBody.add("." + startSymbol);
        //startSymbol = "S'";
        extProductions.put("S'", spBody);

        // add prod to initial node
        initialNode.add(startSymbol + "→" + spBody.get(0));

        // add a '.' to the start of every production body
        for (String symbol : productionSymbols) {
            if (productions.containsKey(symbol)) {
                List<String> bdlist = productions.get(symbol);
                List<String> newBdList = new LinkedList<>();

                for (String bd : bdlist) {
                    String newBd = "." + bd;
                    newBdList.add(newBd);
                    initialNode.add(symbol + "→" + newBd);
                }

                // replace new body list
                extProductions.put(symbol, newBdList);
                lr0Closures.put(symbol, new LinkedList<>());
            }
        }

//        System.out.println("Productions: " + productions.toString());
//        System.out.println("Extended productions: " + extProductions.toString());
//        System.out.println("Terminals: " + terminals.toString());
//        System.out.println("Non terminals: " + nonTerminals.toString());

        LR0Node iNode = new LR0Node(initialNode);
        iNode.setInitialState(true);
        dfa.addInitialNode(iNode);
    }

    public void buildDFA() {
        makeDFA(dfa.getInitialNode());
        List<LR0Node> nodeList = dfa.getNodes();
        for (LR0Node node : nodeList) {
            List<String> prods = node.getExtProductions();
            for (int i = 0; i < prods.size(); i++) {
                String prod = prods.get(i);
                for (String toReplace : specialTerminalsFound) {
                    String replacement = prod.replace(String.valueOf(revSpTerminals.get(toReplace)), toReplace);
                    prod = replacement;
                }

                prods.set(i, prod);
            }
        }

        List<LR0Transition> tList = dfa.getTrList();
        for (LR0Transition tr : tList) {
            for (String toReplace : specialTerminalsFound) {
                String replacement = tr.getTrSymbol().replace(String.valueOf(revSpTerminals.get(toReplace)), toReplace);
                tr.setTrSymbol(replacement);
            }
        }
        dfa.reassureInitialNodeIsIn();

        firstAndFollow();

        // parsing table
        makeParsingTable();
        System.out.println("DFA: \n" + dfa.toString());
        System.out.println("PARSING TABLE: " + parsingTable.toString());
        System.out.println("PRODUCTIONS IDS MAP: " + productionsIdsMap.toString());

        // TODO parse the whole thing
//        Scanner scanner = new Scanner(System.in);
//        System.out.println(">>Welcome to the parser. Insert a string to parse: ");
//        String toParse = scanner.nextLine();
//        parse(dfa.getInitialNode().getId(), toParse);
    }

    public void parse (String in) {
        if (!conflicts) {
            int initialNode = dfa.getInitialNode().getId();
            System.out.println(">>Parsing " + in + "! ");
            stackParsing.clear();
            stackParsing.push(initialNode);
            String input = in + "$";

            // TODO traverse the string and replace terminals with values from LexedValues
            for (TokenNode tn : temporaryLexedValues) {
                if (!terminals.contains(tn.getValue())) {
                    input = input.replace(tn.getValue(),tn.getIdent());
                }
            }


            boolean stop = false;

            // parse until we reach accepting state and $ symbol
            try {
                while (!stop) {
                    // check if last value of stack is an integer
                    if (stackParsing.peek().getClass() == Integer.class) {
                        // it is an integer, so now get the action from the parsing table

                        for (int j = 0; j < input.length(); j++) {
                            String inputBit = "";

                            for (int k = j; k < input.length(); k++) {
                                String currString = Character.toString(input.charAt(k));
                                boolean isLowercase = !currString.equals(currString.toUpperCase());

                                if (!isLowercase && k == 0) {
                                    j = k+1;
                                    inputBit = currString;
                                    break;
                                }

                                if (isLowercase) {
                                    inputBit = inputBit + Character.toString(input.charAt(k));
                                } else {
                                    j = k;
                                    break;
                                }
                            }

                            // inputBit has the input to evaluate
                            List<HashMap> mapList = parsingTable.get(stackParsing.peek());
                            String action = "";

                            for (HashMap<String, String> map : mapList) {
                                if (map.containsKey(inputBit)) {
                                    action = map.get(inputBit);
                                    System.out.println(">>ACTION->> " + action);
                                }
                            }

                            // interpret action
                            if (action.charAt(0) == 'S') {
                                // shift
                                int newState = Integer.parseInt(action.substring(1));
                                stackParsing.push(inputBit);
                                stackParsing.push(newState);
                                String newInput = input.substring(j);
                                input = newInput;
                                break;
                            } else if (action.charAt(0) == 'R') {
                                // reduce
                                int productionId = Integer.parseInt(action.substring(1));
                                String prod = productionsIdsMapRev.get(productionId);

                                // get head
                                String[] splittedProd = prod.split("→");
                                // splittedProd[0] is the head

                                String stoppingFlag = "";

                                for (int k = 0; k < splittedProd[1].length(); k++) {
                                    String currString = Character.toString(splittedProd[1].charAt(k));
                                    boolean isLowercase = !currString.equals(currString.toUpperCase());

                                    if (!isLowercase && k == 0) {
                                        stoppingFlag = currString;
                                        break;
                                    }

                                    if (isLowercase) {
                                        stoppingFlag = stoppingFlag + currString;
                                    } else break;
                                }

                                // pop from stack until we find the stoppingFlag
                                boolean stopPopping = false;

                                while (!stopPopping) {
                                    Object popped = stackParsing.pop();

                                    if (popped.getClass() == String.class) {
                                        if (popped.equals(stoppingFlag)) {
                                            stopPopping = true;
                                            // get new state
                                            int prevState = (int) stackParsing.peek();
                                            stackParsing.push(splittedProd[0]);

                                            List<HashMap> mplst = parsingTable.get(prevState);
                                            String act = "";

                                            for (HashMap<String, String> map : mplst) {
                                                if (map.containsKey(splittedProd[0])) {
                                                    act = map.get(splittedProd[0]);
                                                    stackParsing.push(Integer.parseInt(act));
                                                    break;
                                                }
                                            }

                                        }
                                    }
                                }
                                break;
                            } else if (action.equals("Accept") && input.equals("$")) {
                                // finish parsing
                                stop = true;
                                System.out.println("<<Parsing finished!>>\n>>Parsing results: ACCEPT " + in);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("<<Parsing finished!>>\n>>Parsing results: DENY " + in);
            }
            temporaryLexedValues.clear();
        } else System.out.println("<<CAN'T PARSE STRING. NOT AN SLR GRAMMAR. SEE LOG FOR DETAILS.>>");

    }

    // parsing table
    // TODO conflicts
    public void makeParsingTable() {
        dfa.recalculateFinalNodes();
        // empty table
        for (LR0Node node : dfa.getNodes()) {
            // fill up internal
            List<HashMap> internal = new LinkedList<>();
            parsingTable.put(node.getId(), internal);
        }

        // traverse transition list
        for (LR0Transition tr : dfa.getTrList()) {
            int iState = tr.getInitialNode().getId();
            int fState = tr.getFinalNode().getId();
            String tSymbol = tr.getTrSymbol();

            String signal = "";

            if (terminals.contains(tSymbol)) {
                signal = "S";
            }

            String toWrite = signal + Integer.toString(fState);

            List<HashMap> internal = parsingTable.get(iState);
            HashMap<String, String> hash = new HashMap<>();
            hash.put(tSymbol, toWrite);
            internal.add(hash);
            parsingTable.put(iState, internal);
        }

        // look for transition with startingSymbol
        for (LR0Node node : dfa.getNodes()) {
            List<String> extProd = node.getExtProductions();
            for (String prod : extProd) {
                char last = prod.charAt(prod.length()-1);
                if (last == '.') {
                    if ((prod.charAt(prod.length()-3) == '→')
                            && (Character.toString(prod.charAt(prod.length()-2)).equals(startSymbol))) {
                        HashMap<String, String> hash = new HashMap<>();
                        List<HashMap> internal = parsingTable.get(node.getId());
                        hash.put("$", "Accept");
                        internal.add(hash);
                        parsingTable.put(node.getId(), internal);
                    }
                }
            }
        }

        // Reduce operations
        for (LR0Node node : dfa.getNodes()) {
            if (node.isFinalState()) {
                String lastProd = node.getExtProductions().get(node.getExtProductions().size()-1);
                if (lastProd.charAt(lastProd.length()-1) == '.') {
                    String removeDot = lastProd.substring(0, lastProd.length()-1);

                    try {
                        int index = productionsIdsMap.get(removeDot);
                        String reduce = "R" + index;
                        String head = Character.toString(lastProd.charAt(0));
                        List<String> followSymbols = follow.get(head);

                        HashMap<String, String> hash = new HashMap<>();
                        List<HashMap> internal = parsingTable.get(node.getId());
                        hash.put("$", reduce);
                        internal.add(hash);

                        for (String symbol : followSymbols) {
                            hash = new HashMap<>();
                            hash.put(symbol, reduce);
                            if (!internal.contains(hash)) {
                                internal.add(hash);
                            }
                        }

                        parsingTable.put(node.getId(), internal);

                    } catch (Exception e) {
                    }
                }

            }
        }

        // search for conflicts
        for (int i = 0; i < dfa.numberOfNodes; i++) {
            List<HashMap> mapList = parsingTable.get(i);
            List<String> terminalList = new LinkedList<>();

            for (HashMap<String, String> map : mapList) {
                for (String key : map.keySet()) {
                    if (!terminalList.contains(key)) {
                        terminalList.add(key);
                    } else {
                        System.out.println("<<There's a conflict at the parsing table. Two or more actions at the same terminal" +
                                " " + key + " in state " + i + ">>");
                        conflicts = true;
                    }
                }
            }
        }
    }

    public void makeDFA(LR0Node node) {
        List<String> iProds = node.getExtProductions();

        for (int k = 0; k < iProds.size(); k++) {
            String prod = iProds.get(k);
            String terminalSave = ""; // to save in case of a terminal

            for (int m = 0; m < prod.length(); m++) {
                String currString = Character.toString(prod.charAt(m));
                boolean isLowercase = !currString.equals(currString.toUpperCase());

                if (isLowercase) {
                    terminalSave = terminalSave + currString;
                }
            }

            if (!specialTerminalsFound.contains(terminalSave) && !terminalSave.equals("")) {
                revSpTerminals.put(terminalSave, specialTerminalsCounter);
                specialTerminalsFound.add(terminalSave);
                specialTerminals.put(specialTerminalsCounter, terminalSave);
                String p = prod.replace(terminalSave, String.valueOf(specialTerminalsCounter));
                prod = p;
                specialTerminalsCounter++;
            } else if (specialTerminalsFound.contains(terminalSave) && !terminalSave.equals("")){
                String p = prod.replace(terminalSave, String.valueOf(revSpTerminals.get(terminalSave)));
                prod = p;
            }

            // check if '.' is last char
            if (prod.charAt(prod.length()-1) != '.') {
                // step up '.' by one
                String nProd = "";
                String tSymbol = "";
                String cSymbol = ""; // closure symbol
                for (int j = 0; j < prod.length(); j++) {
                    if ((prod.charAt(j) == '.') && ((j+1) < prod.length())) {
                        // TODO add support for things like T' (with ' ) and terminals in lowercase
                        tSymbol = Character.toString(prod.charAt(j+1));
                        String leftmost = prod.substring(0, j);
                        String middle = Character.toString(prod.charAt(j+1)) + ".";
                        String rightmost = prod.substring(j+2, prod.length());
                        nProd = leftmost + middle + rightmost;

                        // closure symbol
                        if ((j+2) < prod.length()) {
                            cSymbol = Character.toString(prod.charAt(j+2));
                        }
                        break;
                    }
                }

                // start algorithm
                // check if node does not have a transition with that symbol
                if (!node.getTransitionSymbols().contains(tSymbol)) {
                    // it doesn't! so now check if any node on DFA has firstProd equal to nProd
                    if (!dfa.hasThisFirstProd(nProd)) {
                        // there's no such firstProd at any node that is the same as nProd!
                        // so, let's create a new node with firstProd = nProd
                        LR0Node newNode = new LR0Node();
                        newNode.addProduction(nProd);
                        newNode.setFirstProd(nProd);

                        // add the node to DFA and make links
                        dfa.addNode(newNode);
                        LR0Transition nTransition = new LR0Transition(tSymbol, node, newNode);
                        node.addTransitionSymbol(tSymbol);
                        dfa.addTransition(nTransition);

                        // apply closure to cSymbol
                        if (nonTerminals.contains(cSymbol)) {
                            List<String> cSClosure = lr0Closures.get(cSymbol);
                            for (String produc : cSClosure) {
                                newNode.addProduction(produc);
                            }
                        }

                        // recursive call to check new node
                        makeDFA(newNode);
                    } else {
                        // there's a node in which firstProd is nProd.
                        // this means that we have to make a transition between newNode and that node
                        LR0Node stackNode = dfa.getNodesFProds().pop();
                        LR0Transition nTransition = new LR0Transition(tSymbol, node, stackNode);
                        node.addTransitionSymbol(tSymbol);
                        dfa.addTransition(nTransition);
                        // check if stackNode contains nProd, if not, add it
                        if (!stackNode.getExtProductions().contains(nProd)) {
                            stackNode.addProduction(nProd);
                        }
                    }
                } else {
                    // this node has a transition with that symbol
                    // find the finalNode of that transition and add nProd to it
                    for (LR0Transition tr : dfa.getTrList()) {
                        if (tr.getInitialNode().equals(node)) {
                            if (tr.getTrSymbol().equals(tSymbol)) {
                                // collect node and add prod
                                if (!tr.getFinalNode().getExtProductions().contains(nProd)) {
                                    tr.getFinalNode().addProduction(nProd);
                                }
                            }
                        }
                    }
                }
            } else {
                // '.' is the last char! That means that we've reached a final state.
                node.setFinalState(true);
                dfa.addFinalNode(node);
            }
        }
    }

    public void applyClosures() {
        for (String nTerm : nonTerminals) {
            lr0Closure(nTerm);
        }
        System.out.println("Closures: " + lr0Closures.toString());
    }

    public void lr0Closure(String nonTerminal) {
        if (nonTerminals.contains(nonTerminal)) {
            List<String> closure = new LinkedList<>();
            List<String> prodBodys = extProductions.get(nonTerminal);
            // add all productions that start with that non-terminal
            for (String pb : prodBodys) {
                closure.add(nonTerminal + "→" + pb);
            }

            // re-apply closure to production bodies
            for (String pb : prodBodys) {
                // extend string until we find a terminal. Apply closure to that extended string
                String extPb = "";
                for (int j = 1; j < pb.length(); j++) {
                    if (!terminals.contains(Character.toString(pb.charAt(j)))) {
                        extPb = extPb + Character.toString(pb.charAt(j));
                    } else break;
                }

                lr0Closure(extPb);
                // now add it to current
                if (lr0Closures.containsKey(extPb)) {
                    // add to lr0Closures
                    List<String> closures = lr0Closures.get(extPb);
                    for (String cl : closures) {
                        if (!closure.contains(cl)) {
                            closure.add(cl);
                        }
                    }
                }
            }

            // add to lr0Closures
            List<String> closures = lr0Closures.get(nonTerminal);
            for (String cl : closure) {
                if (!closures.contains(cl)) {
                    closures.add(cl);
                }
            }
            lr0Closures.put(nonTerminal, closures);

        }
    }

    public void firstAndFollow() {
        // compute all firsts and follows based on heads of productions
        // all firsts
        for (String symbol : productionSymbols) {
            if (productions.containsKey(symbol)) {
                computeFirst(symbol);
            }
        }

        // all follows
        for (String symbol : productionSymbols) {
            if (productions.containsKey(symbol)) {
                computeFollow(symbol);
            }
        }

        System.out.println("FOLLOW: " + follow.toString());

        // for this time, lets make an internal user interface so that he can compute firsts a and follows
//        Scanner scanner = new Scanner(System.in);
//        boolean flag = false;
//
//        while(!flag) {
//            System.out.println("<<FIRST and FOLLOW computation!>>\n" +
//                    "1. FIRST\n" +
//                    "2. FOLLOW\n" +
//                    "3. EXIT\n");
//            String selected = scanner.nextLine();
//
//            // parse selection
//            if (selected.equals("3")) {
//                // exit code
//                flag = true;
//            } else if (selected.equals("1")) {
//                // compute first
//                System.out.println(">>Please insert a string of symbols: \n");
//                String string = scanner.nextLine();
//                // traverse first map
//                try {
//                    List<String> result = first.get(Character.toString(string.charAt(0)));
//                    // output
//                    System.out.println(">>FIRST(" + string + "): " + result.toString());
//                } catch (Exception e) {
//                    System.out.println(">>Provided invalid symbol.");
//                }
//            } else if (selected.equals("2")) {
//                // compute follow
//                System.out.println(">>Please insert a non-terminal symbol: \n");
//                String nonTerminal = scanner.nextLine();
//
//                if (nonTerminals.contains(nonTerminal)) {
//                    List<String> result = follow.get(nonTerminal);
//                    // output
//                    System.out.println(">>FOLLOW(" + nonTerminal + "): " + result.toString());
//                } else {
//                    System.out.println(">>String provided is not a non-terminal");
//                }
//            } else {
//                System.out.println(">>Select an option from the menu.");
//            }
//        }
    }

    public void computeFirst(String head) {
        // Compute terminals' first, which is the same string
        if (first.size() == 0) {
            for (String terminal : terminals) {
                List<String> terminalFirst = new LinkedList<>();
                terminalFirst.add(terminal);
                first.put(terminal, terminalFirst);
            }
        }

        if (!terminals.contains(head)) {
            List<String> body = productions.get(head);
            List<String> firstList = new LinkedList<>();
            for (String bd : body) {
                String first = "";
                if (terminals.contains(bd)) {
                    first = bd;
                } else {
                    first = Character.toString(bd.charAt(0));
                }
                if (terminals.contains(first) || first.equals("#")) {
                    if (!this.first.containsKey(head)) {
                        if (this.first.containsKey(first)) {
                            firstList.addAll(this.first.get(first));
                        } else {
                            firstList.add(first);
                        }
                    }
                } else if (nonTerminals.contains(first)) {
                    if (!this.first.containsKey(head)) {
                        computeFirst(first);
                        if (this.first.containsKey(first)) {
                            firstList.addAll(this.first.get(first));
                        }
                    }

                }
            }
            if (firstList.size() != 0) {
                first.put(head, firstList);
            }
        } else {
            List<String> firstList = new LinkedList<>();
            firstList.add(head);
            first.put(head, firstList);
        }
        System.out.print("");
    }

    public void computeFollow(String head) {
        // if first time computing Follow, this is follow.size is 0, then fill up follow just with the heads
        if (follow.size() == 0) {
            for (String symbol : productionSymbols) {
                if (productions.containsKey(symbol)) {
                    follow.put(symbol, new LinkedList<>());
                }
            }
        }

        if (nonTerminals.contains(head)) {
            for (String symbol : productionSymbols) {
                if (productions.containsKey(symbol)) {
                    List<String> bdLst = productions.get(symbol);
                    for (String bd : bdLst) {
                        if (bd.contains(head) && bd.length() > 1) {
                            for (int j = 0; j < bd.length(); j++) {
                                String currChar = Character.toString(bd.charAt(j));
                                if ((j+1) < bd.length() && currChar.equals(head)) {
                                    String fl = Character.toString(bd.charAt(j+1));
                                    List<String> follows = follow.get(head);
                                    if (!follows.contains(fl) && !nonTerminals.contains(fl)) {
                                        follows.add(fl);
                                    }
                                    follow.put(head, follows);
                                }
                            }
                        }
                    }
                }
            }

            // list for current follow
            List<String> followList = new LinkedList<>();
            List<String> body = productions.get(head);

            if (head.equals(startSymbol)) {
                List<String> followHead = follow.get(head);
                followHead.add("$");
                follow.put(head, followHead);
            }

            // check for derivations
            List<String> derivs = new LinkedList<>();
            for (String bd : body) {
                if ((!terminals.contains(bd)) && (bd.equals("#"))) {
                    // we need to derive
                    for (String bds : body) {
                        if ((!terminals.contains(bds)) && (!bds.equals("#")) && (!bds.equals(bd))) {
                            String derivation = bds.replace(head, "");
                            derivs.add(derivation);
                        }
                    }
                }
            }
            body.addAll(derivs);

            // for each body in that production check (alpha A beta) or (alpha A)
            for (String bd : body) {
                // check the form
                if ((!terminals.contains(bd)) && (!bd.equals("#"))) {
                    // split the symbols so that we can check the actual form
                    int numberOfSymbols = 0;
                    for (int i = 0; i < bd.length(); i++) {
                        if ((i+1) < bd.length()) {
                            if (bd.charAt(i+1) == '\'') {
                                numberOfSymbols++;
                                i = i+1;
                            } else {
                                numberOfSymbols++;
                            }
                        } else {
                            if (bd.charAt(i) != '\'') {
                                numberOfSymbols++;
                            }
                        }
                    }

                    // find form based on numberOfSymbols
                    if (numberOfSymbols == 3) {
                        // B -> alpha A beta
                        // FOLLOW(A) = FIRST(beta)
                        try {
                            followList.addAll(follow.get(Character.toString(bd.charAt(1))));
                            String beta = "";
                            if (!terminals.contains(Character.toString(bd.charAt(2)))) {
                                if (bd.length() == 4) {
                                    beta = Character.toString(bd.charAt(2)) + Character.toString(bd.charAt(3));
                                } else beta = Character.toString(bd.charAt(2));
                            } else {
                                beta = Character.toString(bd.charAt(2));
                            }
                            followList.addAll(first.get(beta));
                            // remove # from followList, since FOLLOW cant have epsylon
                            followList.remove("#");
                            Set<String> fs = new LinkedHashSet<>(followList);
                            followList.clear();
                            followList.addAll(fs);
                            follow.put(Character.toString(bd.charAt(1)), followList); // FOLLOW(A)
                        } catch (Exception e) {
                        }
                    } else if (numberOfSymbols == 2) {
                        // B -> alpha A
                        // save in list of equals A and B, since FOLLOW(A) = FOLLOW(B)
                        String A = "";
                        String B = head;
                        for (int i = 0; i < bd.length(); i++) {
                            if ((i+1) < bd.length()) {
                                if (bd.charAt(i+1) == '\'') {
                                    A = Character.toString(bd.charAt(i)) + Character.toString(bd.charAt(i+1));
                                    break;
                                }
                            }
                        }
                        if (A.equals("")) {
                            A = Character.toString(bd.charAt(1));
                        }

                        String[] equals = {A, B};
                        equalFollows.add(equals);
                    } else if (numberOfSymbols == 1) {
                        // B -> A
                        String A = bd;
                        String B = head;
                        String[] equals = {A, B};
                        equalFollows.add(equals);
                    }
                }
            }

            // check for FOLLOW(A) = FOLLOW(B) in equalFollows
            for (String[] eqstr : equalFollows) {
                // follow of first contains follow of second
                List<String> followFirst = follow.get(eqstr[0]);
                List<String> followSecond = follow.get(eqstr[1]);

                for (String str : followSecond) {
                    if (!followFirst.contains(str)) {
                        followFirst.add(str);
                    }
                }

                follow.put(eqstr[0], followFirst);
            }
        }

    System.out.print("");
    }

    public void addProdSymbolsTerminals(String production) {
        // add symbols to Symbol List
        String[] splitted = production.split("");
        for (int i = 0; i < splitted.length; i++) {
            String currString = splitted[i];

            if (!currString.equals("=") && !currString.equals("|") && !currString.equals("#")) {
                boolean isLowercase = !currString.equals(currString.toUpperCase());

                if (isLowercase) {
                    String concat = "";
                    for (int j = i; j < splitted.length; j++) {
                        if (splitted[j].equals("'")) {
                            break;
                        }

                        if (!splitted[j].equals(" ")) {
                            concat = concat + splitted[j];
                            i = j;
                        } else {
                            i = j;
                        }
                    }
                    productionSymbols.add(concat);
                } else {
                    //handle chars
                    if (((i+2) < splitted.length) && (currString.equals("'"))) {
                        if (splitted[i+2].equals("'")) {
                            if (!productionSymbols.contains(splitted[i+1]) && (!splitted[i+1].equals("=") && !splitted[i+1].equals("|") && !splitted[i+1].equals("#"))) {
                                productionSymbols.add(splitted[i+1]);
                                i = i+2;
                            }
                        }
                    } else if ((i+1) < splitted.length) {
                        // lookahead for '
                        if ((i+3) < splitted.length && splitted[i+1].equals("'")) {
                            if (splitted[i+3].equals("'") && !commonTerminals.contains(currString)) {
                                if (!productionSymbols.contains(currString)) {
                                    productionSymbols.add(currString);
                                }
                            }
                        } else if (splitted[i+1].equals("'") && !commonTerminals.contains(currString)) {
                            String concat = currString + splitted[i+1];
                            if (!productionSymbols.contains(concat)) {
                                productionSymbols.add(concat);
                            }
                        } else {
                            if (!productionSymbols.contains(currString)) {
                                productionSymbols.add(currString);
                            }
                        }
                    } else {
                        if (!productionSymbols.contains(currString) && (!currString.equals("'"))) {
                            productionSymbols.add(currString);
                        }
                    }
                }

            }
        }

        // add Terminals and non-terminals
        for (String symbol : productionSymbols) {
            boolean hasUppercase = !symbol.equals(symbol.toLowerCase());
            if (commonTerminals.contains(symbol)) {
                if (!terminals.contains(symbol)){
                    terminals.add(symbol);
                }
            } else {
                if (hasUppercase) {
                    if (!nonTerminals.contains(symbol)) {
                        nonTerminals.add(symbol);
                    }
                } else {
                    if (!terminals.contains(symbol)){
                        terminals.add(symbol);
                    }
                }
            }
        }
    }

    /**
     * Adds a TokenDecl, which means that DFA's are generated and stored.
     * @param regex
     */
    public void addToken(String regex) {
        String[] splitted = splitLineByEquals(regex);
        // to make it cleaner we store the splits in two strings
        String ident = splitted[0];
        String expression = splitted[2];

        // manipulate the expression
        // check for single quotes inside separators
        for (int i = 0; i < expression.length(); i++) {
            String currChar = Character.toString(expression.charAt(i));
            if (currChar.equals("(") || currChar.equals("{") || currChar.equals("[")) {
                for (int j = i+1; j < expression.length(); j++) {
                    String cc = Character.toString(expression.charAt(j));
                    if (cc.equals("'")) {
                        String leftmost = expression.substring(0, j);
                        String rightmost = expression.substring(j+1, expression.length());
                        expression = leftmost + "Ċ" + rightmost;
                    }

                    if (cc.equals(")") || cc.equals("}") || cc.equals("]")) {
                        i = j;
                        j = expression.length();
                    }
                }
            }
        }

        String[] splittedExpression = splitExpression(expression);
        String regexp = "";

        for (String se : splittedExpression) {
            se = se.replace("Ċ", "\'");
            String re = "";
            if (se.contains("(") || se.contains("[") || se.contains("{")) {
                String importantExpression = se;
                if (!importantExpression.contains("\"")) {
                    importantExpression = se.substring(1, se.length()-1);
                }
                if (importantExpression.contains("\"") || importantExpression.contains("\'")) {
                    importantExpression = importantExpression.replace("\"", "");
                    importantExpression = importantExpression.replace("\'", "");
                    importantExpression = importantExpression.replace("(", "«");
                    importantExpression = importantExpression.replace(")", "»");
                    importantExpression = importantExpression.replace(".", "ᑔ");
                    importantExpression = importantExpression.replace("*", "Ŏ");
                } else {
                    if (importantExpression.contains("|")) {
                        String[] expSplit = importantExpression.split("\\|");
                        String exp = "";
                        for (String str : expSplit) {
                            String expr = identRegexMap.get(str);
                            if (expr == null) {
                                expr = tokenRegexMap.get(str);
                            }
                            exp = exp + "(" + expr + ")|";
                        }
                        importantExpression = exp;
                        importantExpression = importantExpression.substring(0, importantExpression.length()-1);
                    } else {
                        String toSearch = importantExpression;
                        importantExpression = identRegexMap.get(toSearch);
                        if (importantExpression == null) {
                            importantExpression = tokenRegexMap.get(toSearch);
                        }
                    }
                }

                if (se.contains("{")) {
                    re = "(" + importantExpression + ")*";
                } else if (se.contains("[")) {
                    re = "((" + importantExpression + ")|ε)";
                } else if (se.contains("(")) {
                    re = importantExpression;
                }
            } else {
                if (se.contains("\"")) {
                    se = se.replace("\"", "");
                    se = se.replace(".", "ᑔ");
                    se = se.replace("+", "ᑙ");
                    se = se.replace("-", "ᑛ");
                    se = se.replace("*", "Ŏ");
                    re = re + se;
                } else if (se.contains("\'")) {
                    se = se.replace("\'", "");
                    se = se.replace(".", "ᑔ");
                    se = se.replace("+", "ᑙ");
                    se = se.replace("-", "ᑛ");
                    se = se.replace("*", "Ŏ");
                    re = re + se;
                } else if (se.contains("|")) {
                    se = se.replace("|", "");
                    String expr = identRegexMap.get(se);
                    if (expr == null) {
                        expr = tokenRegexMap.get(se);
                    }
                    re = re + "|(" + expr + ")";
                } else {
                    String expr = identRegexMap.get(se);
                    if (expr == null) {
                        expr = tokenRegexMap.get(se);
                    }
                    re = re + "(" + expr + ")";
                }
            }
            regexp = regexp + re;
        }
        AFN nfa = new AFN(regexp);
        Transformation transformation = new Transformation(nfa.getTransitionsList(), nfa.getSymbolList(), nfa.getFinalStates(), nfa.getInitialState());
        DFA dfa = new DFA(transformation.getDfaTable(), transformation.getDfaStates(),transformation.getDfaStatesWithNumbering(),transformation.getSymbolList());

        tokenRegexMap.put(ident, regexp);
        tokensDFAs.add(dfa);
        tokensMap.put(ident, dfa);
        reversedTokensMap.put(dfa, ident);

    }

    /**
     * Adds a KeywordDecl
     * @param string
     * @param ident
     */
    public void addKeyword(String string, String ident) {
        keywordsMap.put(string, ident);
    }

    /**
     * Adds whitespace to list
     * @param set
     */
    public void addWhiteSpace(String set) {
        String operator = "";

        if (set.contains("+") || set.contains("-")) {
            if (set.contains("+")) {
                operator = "+";
            } else if (set.contains("-")) {
                operator = "-";
            }
            // it's a mixed word that has either a '+' or a '-'
            // so, let's proceed to split the actual word
            String[] splitted = set.split(String.format(WITH_DELIMITER, "\\+|\\-"));
            String re = "";

            String leftmost = "";
            String rightmost = "";

            // now that's splitted, proceed to work first with the leftmost part
            if (splitted[0].contains("\"")) {
                // it means that's a String
                leftmost = splitted[0].replace("\"", "");
                leftmost = leftmost.replace("", "|");
                leftmost = leftmost.substring(1, leftmost.length()-1);
            } else {
                // it's a fixed expression, such as an ident or a char or whatever...
                if (charactersMap.containsKey(splitted[0])) {
                    String identExp = charactersMap.get(splitted[0]);
                    identExp = identExp.replace("\"", "");
                    identExp = identExp.replace("", "|");
                    identExp = identExp.substring(1, identExp.length()-1);

                    leftmost = identExp;
                } else {
                    leftmost = splitted[0];
                }

            }

            // now, proceed to work with the rightmost part
            if (splitted[2].contains("\"")) {
                // it's a String
                rightmost = splitted[2].replace("\"", "");
                rightmost = rightmost.replace("", "|");
                rightmost = rightmost.substring(1, rightmost.length()-1);
            } else {
                // fixed expression
                if (charactersMap.containsKey(splitted[2])) {
                    String identExp = charactersMap.get(splitted[0]);
                    identExp = identExp.replace("\"", "");
                    identExp = identExp.replace("", "|");
                    identExp = identExp.substring(1, identExp.length()-1);

                    rightmost = identExp;
                } else {
                    rightmost = splitted[2];
                }
            }

            // now, we have both expressions that need to be merged, depending on whether the operator is a '+' or a '-'
            if (operator.equals("+")) {
                if (leftmost.contains("|")) {
                    leftmost = leftmost.replace("|", "");

                    for (int i = 0; i < leftmost.length(); i++) {
                        whiteSpaceList.add(Character.toString(leftmost.charAt(i)));
                    }
                } else {
                    whiteSpaceList.add(leftmost);
                }

                if (rightmost.contains("|")) {
                    rightmost = rightmost.replace("|", "");

                    for (int i = 0; i < rightmost.length(); i++) {
                        whiteSpaceList.add(Character.toString(rightmost.charAt(i)));
                    }
                } else {
                    whiteSpaceList.add(rightmost);
                }

            } else if (operator.equals("-")) {
                String[] values = rightmost.split("|");
                for (String val : values) {
                    leftmost = leftmost.replace(val, "");
                    leftmost = leftmost.replace("|", "");
                }

                for (int i = 0; i < leftmost.length(); i++) {
                    whiteSpaceList.add(Character.toString(leftmost.charAt(i)));
                }
            }
        } else if (set.contains("CHR")) {
            String re = set;
            re = re.replace("(", "«");
            re = re.replace(")", "»");
            re = re.replace(".", "ᑔ");
            whiteSpaceList.add(re);
        } else {
            // not a special case, then...
            String re = set;
            if (re.contains("\"")) {
                re = re.replace("\"", "");

                for (int i = 0; i < re.length(); i++) {
                    whiteSpaceList.add(Character.toString(re.charAt(i)));
                }
            } else {
                if (charactersMap.containsKey(re)) {
                    String identExp = charactersMap.get(re);
                    identExp = identExp.replace("\"", "");

                    for (int i = 0; i < identExp.length(); i++) {
                        whiteSpaceList.add(Character.toString(identExp.charAt(i)));
                    }
                } else {
                    whiteSpaceList.add(re);
                }
            }
        }
    }

    /**
     * Adds Character to maps
     * @param ident
     * @param string
     */
    public void addCharacter(String ident, String string) {
        charactersMap.put(ident, string);
        reversedCharactersMap.put(string, ident);

        String operator = "";

        // special characters
        if (string.equals("\'+\'"))
            string = string.replace("\'+\'", "ᑙ");
        if (string.equals("\"+\""))
            string = string.replace("\"+\"", "ᑙ");
        if (string.equals("\'-\'"))
            string = string.replace("\'-\'", "ᑛ");
        if (string.equals("\"-\""))
            string = string.replace("\"-\"", "ᑛ");
        if (string.equals("\'(\'"))
            string = string.replace("\'(\'", "«");
        if (string.equals("\"(\""))
            string = string.replace("\"(\"", "«");
        if (string.equals("\')\'"))
            string = string.replace("\')\'", "»");
        if (string.equals("\")\""))
            string = string.replace("\")\"", "»");

        if (string.contains("ANY")) {
            String[] splitted = string.split(String.format(WITH_DELIMITER, "\\+|\\-"));
            String regex = "";

            String flag = "plus";
            for (int i=0; i < splitted.length; i++) {
                if (!splitted[i].equals("ANY")) {
                    if (splitted[i].equals("+")) {
                        flag = "plus";
                    } else if (splitted[i].equals("-")) {
                        flag = "minus";
                    } else {
                        String expression = "";
                        if (splitted[i].contains("\"") || splitted[i].contains("\'")) {
                            splitted[i] = splitted[i].replace("\'", "");
                            splitted[i] = splitted[i].replace("\"", "");
                            expression = splitted[i];
                        } else {
                            expression = charactersMap.get(splitted[i]);
                            expression = expression.replace("\"", "");
                            expression = expression.replace("\'", "");
                            expression = expression.replace("", "|");
                            expression = expression.substring(1, expression.length()-1);
                        }

                        if (flag.equals("minus")) {
                            String[] expSplit = expression.split("|");
                            for (String es : expSplit) {
                                regex = regex.replace(es, "");
                            }
                            regex = regex.replace("", "|");
                            regex = regex.replace("|||", "|");
                            regex = regex.replace("|||", "|");
                            regex = regex.substring(1, regex.length()-1);
                        } else {
                            regex = regex + expression;
                        }
                    }
                } else {
                    regex = regex + Reader.any;
                }
            }
            AFN nfa = new AFN(regex);
            Transformation transformation = new Transformation(nfa.getTransitionsList(), nfa.getSymbolList(), nfa.getFinalStates(), nfa.getInitialState());
            DFA dfa = new DFA(transformation.getDfaTable(), transformation.getDfaStates(),transformation.getDfaStatesWithNumbering(),transformation.getSymbolList());
            characterDFAs.add(dfa);
            reversedCharactersDFAs.put(dfa, ident);
            identRegexMap.put(ident, regex);
        } else {
            if ((string.contains("+") || string.contains("-")) && (!string.contains(".."))) {
                if (string.contains("+")) {
                    operator = "+";
                } else if (string.contains("-")) {
                    operator = "-";
                }
                // it's a mixed word that has either a '+' or a '-'
                // so, let's proceed to split the actual word
                String[] splitted = string.split(String.format(WITH_DELIMITER, "\\+|\\-"));
                String re = "";

                String leftmost = "";
                String rightmost = "";

                // now that's splitted, proceed to work first with the leftmost part
                if (splitted[0].contains("\"") || splitted[0].contains("\'")) {
                    // it means that's a String
                    leftmost = splitted[0].replace("\"", "");
                    leftmost = leftmost.replace("\'", "");
                    leftmost = leftmost.replace("", "|");
                    leftmost = leftmost.substring(1, leftmost.length()-1);
                } else {
                    // it's a fixed expression, such as an ident or a char or whatever...
                    if (charactersMap.containsKey(splitted[0])) {
                        String identExp = charactersMap.get(splitted[0]);
                        identExp = identExp.replace("\"", "");
                        identExp = identExp.replace("\'", "");
                        identExp = identExp.replace("", "|");
                        identExp = identExp.substring(1, identExp.length()-1);

                        leftmost = identExp;
                    } else {
                        leftmost = splitted[0];
                    }

                }

                // now, proceed to work with the rightmost part
                if (splitted[2].contains("\"") || splitted[2].contains("\'")) {
                    // it's a String
                    rightmost = splitted[2].replace("\"", "");
                    rightmost = rightmost.replace("\'", "");
                    rightmost = rightmost.replace("", "|");
                    rightmost = rightmost.substring(1, rightmost.length()-1);
                } else {
                    // fixed expression
                    if (charactersMap.containsKey(splitted[2])) {
                        String identExp = charactersMap.get(splitted[2]);
                        identExp = identExp.replace("\"", "");
                        identExp = identExp.replace("\'", "");
                        identExp = identExp.replace("", "|");
                        identExp = identExp.substring(1, identExp.length()-1);

                        rightmost = identExp;
                    } else {
                        rightmost = splitted[2];
                    }
                }

                // now, we have both expressions that need to be merged, depending on whether the operator is a '+' or a '-'
                if (operator.equals("+")) {
                    re = "("+leftmost+")"+"("+rightmost+")";
                } else if (operator.equals("-")) {
                    String[] values = rightmost.split("|");
                    for (String val : values) {
                        leftmost = leftmost.replace(val, "");
                        leftmost = leftmost.replace("||", "|");
                    }
                    re = "("+leftmost+")";
                }

                AFN nfa = new AFN(re);
                Transformation transformation = new Transformation(nfa.getTransitionsList(), nfa.getSymbolList(), nfa.getFinalStates(), nfa.getInitialState());
                DFA dfa = new DFA(transformation.getDfaTable(), transformation.getDfaStates(),transformation.getDfaStatesWithNumbering(),transformation.getSymbolList());

                characterDFAs.add(dfa);
                reversedCharactersDFAs.put(dfa, ident);
                identRegexMap.put(ident, re);
            } else if (string.contains("CHR")) {
                String re = string;
                re = re.replace("(", "«");
                re = re.replace(")", "»");
                re = re.replace(".", "ᑔ");
                AFN nfa = new AFN(re);
                Transformation transformation = new Transformation(nfa.getTransitionsList(), nfa.getSymbolList(), nfa.getFinalStates(), nfa.getInitialState());
                DFA dfa = new DFA(transformation.getDfaTable(), transformation.getDfaStates(),transformation.getDfaStatesWithNumbering(),transformation.getSymbolList());
                characterDFAs.add(dfa);
                reversedCharactersDFAs.put(dfa, ident);
                identRegexMap.put(ident, re);
            } else if (string.contains("..")) {
                string = string.replace("\'", "");
                String chars = "";
                String charlist = Reader.CHAR_aZ09;

                if (!string.contains("+") && !string.contains("-")) {
                    String[] charseq = string.split("\\..");
                    String alpha = charseq[0];
                    String omega = charseq[charseq.length-1];

                    for (int i = 0; i < charlist.length(); i++) {
                        if (Character.toString(charlist.charAt(i)).equals(alpha)) {
                            for (int j = i; j < charlist.length(); j++) {
                                chars = chars + Character.toString(charlist.charAt(j));
                                if (Character.toString(charlist.charAt(j)).equals(omega)) {
                                    i = j = charlist.length();
                                }
                            }
                        }
                    }
                } else {
                    String[] splitted = string.split("\\+|-");
                    String[] leftmostPart = splitted[0].split("\\..");
                    String[] rightmostPart = splitted[splitted.length-1].split("\\..");
                    String leftmost = "";
                    String rightmost = "";

                    for (int i = 0; i < charlist.length(); i++) {
                        if (Character.toString(charlist.charAt(i)).equals(leftmostPart[0])) {
                            for (int j = i; j < charlist.length(); j++) {
                                leftmost = leftmost + Character.toString(charlist.charAt(j));
                                if (Character.toString(charlist.charAt(j)).equals(leftmostPart[leftmostPart.length-1])) {
                                    i = j = charlist.length();
                                }
                            }
                        }
                    }

                    for (int i = 0; i < charlist.length(); i++) {
                        if (Character.toString(charlist.charAt(i)).equals(rightmostPart[0])) {
                            for (int j = i; j < charlist.length(); j++) {
                                rightmost = rightmost + Character.toString(charlist.charAt(j));
                                if (Character.toString(charlist.charAt(j)).equals(rightmostPart[rightmostPart.length-1])) {
                                    i = j = charlist.length();
                                }
                            }
                        }
                    }

                    if (string.contains("+")) {
                        chars = "(" + leftmost + ")" + "(" + rightmost + ")";
                    } else if (string.contains("-")) {
                        String out = "";
                        for (int i = 0; i < leftmost.length(); i++) {
                            if (!rightmost.contains(Character.toString(leftmost.charAt(i)))) {
                                out = out + Character.toString(leftmost.charAt(i));
                            }
                        }
                        chars = out;
                    }

                }

                chars = chars.replace("", "|");
                chars = chars.replace(")|(", ")(");
                chars = chars.replace("(|", "(");
                chars = chars.replace("|)", ")");
                chars = chars.substring(1, chars.length()-1);
                AFN nfa = new AFN(chars);
                Transformation transformation = new Transformation(nfa.getTransitionsList(), nfa.getSymbolList(), nfa.getFinalStates(), nfa.getInitialState());
                DFA dfa = new DFA(transformation.getDfaTable(), transformation.getDfaStates(),transformation.getDfaStatesWithNumbering(),transformation.getSymbolList());
                characterDFAs.add(dfa);
                reversedCharactersDFAs.put(dfa, ident);
                identRegexMap.put(ident, chars);
            } else {
                // not a special case, then...
                // create DFA and save it
                String re = string;
                if (re.contains("\"")) {
                    re = re.replace("\"", "");
                }
                if (re.contains("\'")) {
                    re = re.replace("\'", "");
                }
                re = re.replace("", "|");
                re = re.substring(1, re.length()-1);
                AFN nfa = new AFN(re);
                Transformation transformation = new Transformation(nfa.getTransitionsList(), nfa.getSymbolList(), nfa.getFinalStates(), nfa.getInitialState());
                DFA dfa = new DFA(transformation.getDfaTable(), transformation.getDfaStates(),transformation.getDfaStatesWithNumbering(),transformation.getSymbolList());
                characterDFAs.add(dfa);
                reversedCharactersDFAs.put(dfa, ident);
                identRegexMap.put(ident, re);
            }
        }
    }

    /**
     * Checks if word is a Keyword
     * @param word
     * @return true if word is a Keyword
     */
    private boolean isWordAKeyword(String word) {
        if (keywordsMap.containsKey(word)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if word is a Token
     * @param word
     * @return true if word is a Token
     */
    private boolean isWordAToken(String word) {
        boolean flag = false;
        String ident = "";

        word = word.replace("(", "«");
        word = word.replace(")", "»");
        word = word.replace("+", "ᑙ");
        word = word.replace("-", "ᑛ");
        word = word.replace(".", "ᑔ");
        word = word.replace("*", "Ŏ");

        for (DFA dfa : tokensDFAs) {
            if(!dfa.extendedDelta(word).equals("YES")) {
                flag = false;
            } else {
                flag = true;
            }

            if (flag) {
                ident = reversedTokensMap.get(dfa);
                word = word.replace("«", "(");
                word = word.replace("»", ")");
                word = word.replace("ᑙ", "+");
                word = word.replace("ᑛ", "-");
                word = word.replace("ᑔ", ".");
                word = word.replace("Ŏ", "*");
                lexedValues.add(new TokenNode(ident, word));

                temporaryLexedValues.add(new TokenNode(ident, word));
                return true;
            }
        }

        return flag;
    }

    /**
     * Checks if word belongs to character set
     * @param word
     * @return true if each character in word belongs to character set
     */
    private boolean isWordACharacter(String word) {
        boolean flag = false;
        String ident = "";

        if (word.matches("[a-zA-z][0-9]|[0-9][a-zA-z]")) {
            for (DFA dfa : characterDFAs) {
                if(!dfa.extendedDelta(word).equals("YES")) {
                    flag = false;
                } else {
                    flag = true;
                }

                if (flag) {
                    ident = reversedCharactersDFAs.get(dfa);
                    lexedValues.add(new TokenNode(ident, word));
                    return true;
                }
            }
        } else if (word.contains("(")){
            word = word.replace("(", "«");
            word = word.replace(")", "»");

            for (DFA dfa : characterDFAs) {
                if(!dfa.extendedDelta(word).equals("YES")) {
                    flag = false;
                } else {
                    flag = true;
                }

                if (flag) {
                    ident = reversedCharactersDFAs.get(dfa);
                    word = word.replace("˥", "(");
                    word = word.replace("˩", ")");
                    lexedValues.add(new TokenNode(ident, word));
                    return true;
                }
            }
        } else {
            for (DFA dfa: characterDFAs) {
                for (int j = 0; j < word.length(); j++) {
                    String currChar = Character.toString(word.charAt(j));
                    if (!dfa.extendedDelta(currChar).equals("YES")) {
                        // unsuccessfull simulation
                        flag = false;
                        break;
                    } else {
                        flag = true;
                    }
                }

                if (flag) {
                    ident = reversedCharactersDFAs.get(dfa);
                    for (int i = 0; i < word.length(); i++) {
                        //recognizedValues.put(ident, Character.toString(word.charAt(i)));
                        lexedValues.add(new TokenNode(ident, Character.toString(word.charAt(i))));
                    }
                    return true;
                }
            }
        }

        return flag;
    }

    /**
     * Lexes the line: checks for full valid tokens within the line
     * @param line
     * @return "" if line has no errors, which means tokens are recognized and stored. Returns error otherwise.
     */
    public String lexLine(String line) {
        String[] splitted = splitLine(line);
        for (String sp: splitted) {
            // check if word is a Keyword
            for (String white : whiteSpaceList) {
                sp = sp.replace(white, "");
            }
            if (isWordAKeyword(sp)) {
                String ident = keywordsMap.get(sp);
                //recognizedValues.put(ident, sp);
                lexedValues.add(new TokenNode(ident, sp));
            } else if (!isWordAToken(sp) && !sp.equals("")){ // if not, check if it's a valid Character
                System.out.println("Invalid expression \"" + sp + "\" ");
                //return "Invalid expression \"" + sp + "\" ";
            }
        }

        return "";
    }

    /**
     * @return list of lexed tokens
     */
    public String getLexedValues() { return lexedValues.toString(); }

    /**
     * Splits the line by spaces
     * @param line
     * @return
     */
    private String[] splitLine(String line) { return line.split("\\s+"); }

    /**
     * Splits the line by an '=' char.
     * @param line
     * @return
     */
    private String[] splitLineByEquals(String line) { return line.split(String.format(WITH_DELIMITER, "=")); }

    /**
     * Splits expression by finding special overture/closure characters
     * @param exp
     * @return
     */
    private String[] splitExpression(String exp) {
        String[] strings = exp.split(String.format(WITH_DELIMITER, "\\(|\\)|\\[|\\]|\\{|\\}|\"|\'"));
        List<String> rStrings = new ArrayList<>();

        for (int i=0; i < strings.length; i++) {
            if (strings[i].equals("(") || strings[i].equals("[") || strings[i].equals("{") || strings[i].equals("\'")) {
                String expression = strings[i] + strings[i+1] + strings[i+2];
                i = i+2;
                rStrings.add(expression);
            } else if (strings[i].equals("\"")) {
                String expression = strings[i];
                int m = 0;
                for (int k=(i+1); k < strings.length; k++) {
                    expression = expression + strings[k];
                    if (strings[k].equals("\"")) {
                        m = k;
                        break;
                    }
                }
                i=m;
                rStrings.add(expression);
            } else {
                rStrings.add(strings[i]);
            }
        }

        return rStrings.toArray(new String[0]);
    }
}
