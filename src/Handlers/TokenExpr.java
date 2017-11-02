package Handlers;

/**
 * TokenExpr
 * Defines a TokenExpr structure.
 *
 * Created by Gabriel Brolo on 09/09/2017.
 */
public class TokenExpr {
    private String tokenExpr;
    private int lineIndex;
    static public final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";

    public TokenExpr() {  }

    public void setLineAndLineIndex(String line, int lineIndex) {
        this.tokenExpr = line;
        this.lineIndex = lineIndex;
    }

    public String isTokenExpr() {
        Reader.tokenTerm.setLineAndLineIndex(tokenExpr, lineIndex);
        Reader.res = Reader.getTokenTerm().isTokenTerm();

        if (!Reader.res.equals("YES")) {
            String[] splitted = splitLine();

            Reader.tokenTerm.setLineAndLineIndex(splitted[0], lineIndex);
            Reader.res = Reader.getTokenTerm().isTokenTerm();

            if (!Reader.res.equals("YES")) {
                return "Error in line: " + lineIndex + ". Invalid TokenTerm provided.";
            } else {
                for (int i = 1; i < splitted.length-1; i++) {
                    if (i%2 != 0) {
                        if (!splitted[i].equals("|")) {
                            return "Error in line: " + lineIndex + ". No '|' provided inbetween TokenTerm declarations";
                        }
                    } else {
                        Reader.tokenTerm.setLineAndLineIndex(splitted[i], lineIndex);
                        Reader.res = Reader.getTokenTerm().isTokenTerm();

                        if (!Reader.res.equals("YES")){
                            return Reader.res;
                        } else {
                            return "YES";
                        }
                    }
                }
            }
        } else {
            return "YES";
        }

        return "";
    }

    private String[] splitLine() { return tokenExpr.split(String.format(WITH_DELIMITER, "\\|")); }
}
