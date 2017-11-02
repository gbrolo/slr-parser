package Handlers;

import StateMachine.AFN;
import StateMachine.DFA;
import StateMachine.Transformation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Stack;

/**
 * Reader.java
 * Created by Gabriel Brolo on 25/08/2017.
 *
 * Reads file with cocol language.
 */
public class Reader {
    private String fileRoute;
    public static String exitCode;

    public AFN globalNFA;
    public static DFA identDFA;
    public static DFA stringDFA;
    public static DFA charDFA;
    public static DFA numberDFA;
    public Transformation transformation;

    public final String ident = "(a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z|A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z)((a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z|A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z)|(0|1|2|3|4|5|6|7|8|9))*";
    public final String string = "\"(a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z|A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z|0|1|2|3|4|5|6|7|8|9)*\"";
    public final String number =  "(0|1|2|3|4|5|6|7|8|9)(0|1|2|3|4|5|6|7|8|9)*";
    public final String charE = "\'(a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z|A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z|0|1|2|3|4|5|6|7|8|9)\'";
    public static final String any = "a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z|A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z|0|1|2|3|4|5|6|7|8|9|\'|\"|;|:|\\|{|}|[|]|<|>|,|-|!|@|#|$|%|&|~|`";

    static public final String CHAR_aZ09 = "abcdefghijklmnñopqrstuvwxyzABCDEFGHIJKLMNÑOPQRSTUVWXYZ0123456789";

    public static SetDecl setDecl;
    public static KeywordDecl keywordDecl;
    public static Set set;
    public static BasicSet bs;
    public static Char ch;
    public static Symbol symbol;
    public static TokenDecl tokenDecl;
    public static TokenExpr tokenExpr;
    public static TokenFactor tokenFactor;
    public static TokenTerm tokenTerm;
    public static WhiteSpaceDecl whiteSpaceDecl;
    public static Production production;
    public static Expression expression;
    public static Term term;
    public static Factor factor;

    public static String res;
    public static int lexerInputLine;
    public static int loopcount;
    public static Stack<String> factorStack;

    public static Lexer lex;

    public Reader () {}

    public Reader(String fileRoute) throws IOException {
        this.fileRoute = fileRoute;
        this.exitCode = "0";
        this.lexerInputLine = 17;

        // automata creation
        globalNFA = new AFN(string);
        transformation = new Transformation(globalNFA.getTransitionsList(),globalNFA.getSymbolList(), globalNFA.getFinalStates(), globalNFA.getInitialState());
        stringDFA = new DFA(transformation.getDfaTable(), transformation.getDfaStates(),transformation.getDfaStatesWithNumbering(),transformation.getSymbolList());

//        globalNFA = new AFN(ident);
//        transformation = new Transformation(globalNFA.getTransitionsList(),globalNFA.getSymbolList(), globalNFA.getFinalStates(), globalNFA.getInitialState());
//        identDFA = new DFA(transformation.getDfaTable(), transformation.getDfaStates(),transformation.getDfaStatesWithNumbering(),transformation.getSymbolList());

        globalNFA = new AFN(number);
        transformation = new Transformation(globalNFA.getTransitionsList(),globalNFA.getSymbolList(), globalNFA.getFinalStates(), globalNFA.getInitialState());
        numberDFA = new DFA(transformation.getDfaTable(), transformation.getDfaStates(),transformation.getDfaStatesWithNumbering(),transformation.getSymbolList());

        globalNFA = new AFN(charE);
        transformation = new Transformation(globalNFA.getTransitionsList(),globalNFA.getSymbolList(), globalNFA.getFinalStates(), globalNFA.getInitialState());
        charDFA = new DFA(transformation.getDfaTable(), transformation.getDfaStates(),transformation.getDfaStatesWithNumbering(),transformation.getSymbolList());

        setDecl = new SetDecl();
        keywordDecl = new KeywordDecl();
        set = new Set();
        bs = new BasicSet();
        ch = new Char();
        symbol = new Symbol();
        tokenDecl = new TokenDecl();
        tokenExpr = new TokenExpr();
        tokenFactor = new TokenFactor();
        tokenTerm = new TokenTerm();
        whiteSpaceDecl = new WhiteSpaceDecl();
        production = new Production();
        expression = new Expression();
        term = new Term();
        factor = new Factor();
        factorStack = new Stack<>();

        lex = new Lexer();
    }

    /**
     * Read file, line by line
     * @param fileRoute
     * @throws IOException
     */
    public void readCocolFile(String fileRoute) throws IOException {
        this.fileRoute = fileRoute;
        // read .txt file
        try (BufferedReader br = new BufferedReader((new InputStreamReader(getClass().getResourceAsStream(fileRoute))))){
            // handle lines as indexes
            int i = 0; // internal index
            int j = 1; // lines index
            StructureReader sr = new StructureReader();
            for(String line; (line = br.readLine()) != null; ) {
                if (this.exitCode.equals("1"))
                    break;
                // check for structure
                sr.setLineAndIndex(line, i, j);
                String result = sr.analyzeStructure(); // structure analysis result
                if (!result.equals("")) {
                    this.exitCode = "1";
                    System.out.println(result);
                } // if theres no error, nothing will be printed
                // check for error
                if (sr.getError()) {
                    this.exitCode = "1";
                    break;
                }
                if (!line.equals("")) { i++; }
                j++;
            }
            if (!sr.didFoundEND()) {
                this.exitCode = "1";
                System.out.println("Fatal error: missing END declaration.");
            }
        } catch (Exception e) {
            this.exitCode = "1";
            if (!res.equals("YES") && (res != null)) {
                System.out.println(res + "\nError loading file at Reader.readCocolFile(). Caused by: " + e);
            } else {
                System.out.println("Error loading file at Reader.readCocolFile(). Caused by: " + e);
            }
        }
    }

    public void readFileToLex(String fileRoute, Lexer lexer) throws IOException {
        this.fileRoute = fileRoute;
        try (BufferedReader br = new BufferedReader((new InputStreamReader(getClass().getResourceAsStream(fileRoute))))) {
            int index = 0;
            for(String line; (line = br.readLine()) != null; ) {
                if (this.exitCode.equals("1"))
                    break;

                String lexResult = lexer.lexLine(line);
                if (!lexResult.equals("")) {
                    // There's an error
                    this.exitCode = "1";
                    System.out.println(lexResult + "at line: " + index + ".");
                }

                index++;
            }

            if (this.exitCode.equals("0")) {
                System.out.println("Result: " + lexer.getLexedValues());
            }

        } catch (Exception e) {
            this.exitCode = "1";
            System.out.println("Error loading file at Reader.readFileToLex()" + " Caused by: " + e);
        }

    }

    public static DFA getIdentDFA() { return identDFA; }
    public static DFA getStringDFA() { return stringDFA; }
    public static DFA getCharDFA() { return charDFA; }
    public static DFA getNumberDFA() { return numberDFA; }
    public static SetDecl getSetDecl() { return setDecl; }
    public static KeywordDecl getKeywordDecl() { return keywordDecl; }
    public static Set getSet() { return set; }
    public static BasicSet getBs() { return bs; }
    public static Char getCh() { return ch; }
    public static Symbol getSymbol() { return symbol; }
    public static TokenDecl getTokenDecl() { return tokenDecl; }
    public static TokenExpr getTokenExpr() { return tokenExpr; }
    public static TokenFactor getTokenFactor() { return tokenFactor; }
    public static TokenTerm getTokenTerm() { return tokenTerm; }
    public static WhiteSpaceDecl getWhiteSpaceDecl() { return whiteSpaceDecl; }
    public static Production getProduction() { return production; }
    public static Expression getExpression() { return expression; }
    public static Term getTerm() { return term; }
    public static Factor getFactor() { return factor; }
}
