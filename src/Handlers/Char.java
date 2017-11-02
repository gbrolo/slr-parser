package Handlers;

import StateMachine.AFN;
import StateMachine.DFA;
import StateMachine.Transformation;

/**
 * Char
 * Defines the structure of a Char
 *
 * Created by Gabriel Brolo on 26/08/2017.
 */
public class Char {
    private String exp;
    private int lineIndex;
    static public final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";

    public Char() { }

    /**
     * Sets a new line an a new line index for post evaluation
     * @param line
     * @param lineIndex
     */
    public void setLineAndLineIndex(String line, int lineIndex) {
        this.exp = line;
        this.lineIndex = lineIndex;
    }

    /**
     * Checks if expression is char
     * @return "YES" iff expression is char.
     */
    public String isChar() {
        // check for char

        if (!exp.replaceAll("\\s+","").matches("'[^'|\']*'")) {
            // not char, now check if it is "CHR" '(' number ')'
            String[] splitted = splitLine();

            if (!splitted[0].replaceAll("\\s+","").equals("CHR")) {
                return "Error in line: " + lineIndex + ". 'CHR' prefix not provided.";
            } else {
                // valid CHR prefix provided, now check for '('
                if (!splitted[1].replaceAll("\\s+","").equals("(")) {
                    return "Error in line: " + lineIndex + ". No '(' provided.";
                } else {
                    // valid '('. now check for number

                    if (!Reader.getNumberDFA().extendedDelta(splitted[2].replaceAll("\\s+","")).equals("YES")) {
                        return "Error in line: " + lineIndex + ". Provided number is invalid";
                    } else {
                        // valid number, now check for final ')'
                        if (!splitted[3].replaceAll("\\s+","").equals(")")) {
                            return "Error in line: " + lineIndex + ". No ')' provided.";
                        } else {
                            System.out.println("<<Done Char specification!>>");
                            return "YES";
                        }
                    }
                }
            }

        } else {
            System.out.println("<<Done Char specification!>>");
            return "YES";
        }

    }

    /**
     * splits the line according with special characters
     * @return array of String[] with splitted values.
     */
    private String[] splitLine() { return exp.split(String.format(WITH_DELIMITER, "\\(|\\)")); }
}
