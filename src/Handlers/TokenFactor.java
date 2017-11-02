package Handlers;

/**
 * TokenFactor
 * Defines a TokenFactor structure.
 *
 * Created by Gabriel Brolo on 09/09/2017.
 */
public class TokenFactor {
    private String tokenFactor;
    private int lineIndex;
    static public final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";
    private String error;
    private String[] splitted;

    public TokenFactor () {
        tokenFactor = "";
        splitted = splitLine();
    }

    /**
     * Sets a new line an a new line index for post evaluation
     * @param line
     * @param lineIndex
     */
    public void setLineAndLineIndex(String line, int lineIndex) {
        this.tokenFactor = line;
        this.lineIndex = lineIndex;
    }

    public String isTokenFactor() {
        Reader.symbol.setLineAndLineIndex(tokenFactor, lineIndex);
        Reader.res = Reader.getSymbol().isSymbol();

        if (!Reader.res.equals("YES")) {
            if (!checkForOr()) {
                error = Reader.res;
                if (!isTokenExprVariation()) {
                    return error;
                } else {
                    return "YES";
                }
            } else {
                return "YES";
            }
        } else {
            return "YES";
        }
    }

    private boolean checkForOr() {
        boolean flag = false;
        Reader.loopcount++;
        if (tokenFactor.contains("|") && !tokenFactor.contains("(") && !tokenFactor.contains("[") && !tokenFactor.contains("{") && !tokenFactor.contains("\"") && !tokenFactor.contains("\'")) {
            String[] splitted = tokenFactor.split("\\|");
            for (String str : splitted) {
                if (Reader.loopcount > 1) {
                    error = Reader.res;
                    return false;
                }
                Reader.tokenExpr.setLineAndLineIndex(str, lineIndex);
                Reader.res = Reader.getTokenExpr().isTokenExpr();

                if (!Reader.res.equals("YES")) {
                    error = Reader.res;
                    return false;
                } else {
                    flag = true;
                }
            }
        }
        Reader.loopcount = 0;
        return flag;
    }

    private boolean isTokenExprVariation() {
        if (isVariation("(", ")") || isVariation("[", "]") || isVariation("{", "}")) {
            System.out.println("<<Done TokenExpr specification!>>");
            return true;
        } else {
            return false;
        }
    }

    private boolean isVariation(String opener, String closer) {
        splitted = splitLine();
        if (!splitted[0].replaceAll("\\s+","").equals(opener)) {
            error = "Error in line: " + lineIndex + ". No "+ opener + " provided.";
            return false;
        } else {
            Reader.tokenExpr.setLineAndLineIndex(splitted[1], lineIndex);
            Reader.res = Reader.getTokenExpr().isTokenExpr();

            if (!Reader.res.equals("YES")) {
                error = "Error in line: " + lineIndex + ". Invalid TokenExpr";
                return false;
            } else {
                if (!splitted[2].replaceAll("\\s+","").equals(closer)) {
                    error = "Error in line: " + lineIndex + ". No "+ closer + " provided.";
                    return false;
                } else {
                    return true;
                }
            }
        }
    }

    private String[] splitLine() { return tokenFactor.split(String.format(WITH_DELIMITER, "\\(|\\)|\\[|\\]|\\{|\\}")); }
}
