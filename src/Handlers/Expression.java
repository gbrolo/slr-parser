package Handlers;

/**
 * Created by Gabriel Brolo on 16/10/2017.
 */
public class Expression {
    private String expression;
    int lineIndex;
    static public final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";

    /**
     * Sets a new line an a new line index for post evaluation
     * @param line
     * @param lineIndex
     */
    public void setLineAndLineIndex(String line, int lineIndex) {
        this.expression = line;
        this.lineIndex = lineIndex;
    }

    public String isExpression() {
        String[] splitted = expression.split("\\|");

        for (String term : splitted) {
            // quickfix: remove first space
            if (term.charAt(0) == ' ') {
                String newTerm = term.substring(1, term.length());
                term = newTerm;
            }

            if (term.charAt(term.length()-1) == ' ') {
                String newTerm = term.substring(0, term.length()-1);
                term = newTerm;
            }

            // check for Term specification
            Reader.term.setLineAndLineIndex(term, lineIndex);
            Reader.res = Reader.getTerm().isTerm();

            if (!Reader.res.equals("YES")) {
                return Reader.res;
            }
        }

        System.out.println("<<Done Expression specification!>>");
        return "YES";
    }
}
