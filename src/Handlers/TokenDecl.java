package Handlers;

import java.util.ArrayList;

/**
 * TokenDecl
 * Defines a TokenDecl structure
 *
 * Created by Gabriel Brolo on 09/09/2017.
 */
public class TokenDecl {
    private String line;
    private int lineIndex;
    static public final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";

    public TokenDecl() {  }

    /**
     * Sets a new line an a new line index for post evaluation
     * @param line
     * @param lineIndex
     */
    public void setLineAndLineIndex(String line, int lineIndex) {
        this.line = line;
        this.lineIndex = lineIndex;
    }

    public String isTokenDecl() {
        String[] splitted = splitLine();

        if (!line.replaceAll("\\s+","").matches("[a-zA-Z0-9]*")){
            // it's not an ident, so check for combination
            if (!splitted[0].replaceAll("\\s+","").matches("[a-zA-Z0-9]*")) {
                return "Error in line: " + lineIndex + ". Invalid ident.";
            } else {
                if (!splitted[1].replaceAll("\\s+","").equals("=")) {
                    return "Error in line: " + lineIndex + ". No '=' provided.";
                } else {
                    // it has an '=', so continue
                    Reader.tokenExpr.setLineAndLineIndex(splitted[2].replaceAll("\\s+",""), lineIndex);
                    Reader.res = Reader.getTokenExpr().isTokenExpr();

                    if (!Reader.res.equals("YES")) {
                        return Reader.res;
                    } else {
                        if (!splitted[3].equals(".")) {
                            return "Error in line: " + lineIndex + ". No '.' provided.";
                        } else {
                            return "YES";
                        }
                    }
                }
            }

        } else {
            return "YES";
        }

    }

    private String[] splitLine() {
        line = line.replace("..", "**");
        line = line.replace("\'.\'", "\'$\'");
        String[] splitted = line.split(String.format(WITH_DELIMITER, "=|\\."));
        ArrayList<String> newSplit = new ArrayList<>();
        for (String str : splitted) {
            str = str.replace("**", "..");
            str = str.replace("\'$\'", "\'.\'");
            newSplit.add(str);
        }
        return newSplit.toArray(new String[0]);
    }
}
