package Handlers;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.Redefinable;

/**
 * Created by Gabriel Brolo on 16/10/2017.
 */
public class Term {
    private String term;
    int lineIndex;
    static public final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";

    /**
     * Sets a new line an a new line index for post evaluation
     * @param line
     * @param lineIndex
     */
    public void setLineAndLineIndex(String line, int lineIndex) {
        this.term = line;
        this.lineIndex = lineIndex;
    }

    public String isTerm() {
        String[] splitted = term.split(" ");

        for (String factor : splitted) {
            Reader.factor.setLineAndLineIndex(factor, lineIndex);
            Reader.res = Reader.getFactor().isFactor();

            if (!Reader.res.equals("YES")) {
                return Reader.res;
            } else {
                System.out.println("<<Done Factor specification!>>");
            }
        }
        System.out.println("<<Done Term specification!>>");
        return "YES";
    }
}
