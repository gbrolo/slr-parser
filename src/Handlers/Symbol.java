package Handlers;

/**
 * Symbol
 * Basic Symbol Structure
 *
 * Created by Gabriel Brolo on 09/09/2017.
 */
public class Symbol {
    private String symbol;
    private int lineIndex;
    private String error;

    public Symbol() {  }

    /**
     * Sets a new line an a new line index for post evaluation
     * @param line
     * @param lineIndex
     */
    public void setLineAndLineIndex(String line, int lineIndex) {
        this.symbol = line;
        this.lineIndex = lineIndex;
    }

    public String isSymbol() {
        // if it's not string nor ident nor char, then it's not a Symbol
        if (isString() || isIdent() || isChar()) {
            System.out.println("<<Done Symbol specification!>>");
            return "YES";
        } else {
            return error;
        }
    }

    public boolean isIdent() {
        if (!symbol.replaceAll("\\s+","").matches("[a-zA-Z0-9]*")) {
            error = "Error in line: " + lineIndex + ". Provided indent not valid.";
            return false;
        }else {
            return true;
        }
    }

    private boolean isString() {
        //!Reader.getStringDFA().extendedDelta(basicSet.replaceAll("\\s+","")).equals("YES")
        if (!symbol.replaceAll("\\s+","").matches("\"[^\"]*\"")) {
            error = "Error in line: " + lineIndex + ". Provided string not valid.";
            return false;
        }else {
            return true;
        }
    }

    private boolean isChar() {
        if (!symbol.replaceAll("\\s+","").matches("'[^'|\']*'")) {
            error = "Error in line: " + lineIndex + ". Provided char not valid.";
            return false;
        }else {
            return true;
        }
    }

}
