package Handlers;

import StateMachine.AFN;
import StateMachine.DFA;
import StateMachine.Transformation;

/**
 * BasicSet
 * Defines the structure of a BasicSet
 *
 * Created by Gabriel Brolo on 26/08/2017.
 */
public class BasicSet {
    static public final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";
    private String basicSet;
    private int lineIndex;
    private String error;

    public BasicSet() { }

    /**
     * Sets a new line an a new line index for post evaluation
     * @param line
     * @param lineIndex
     */
    public void setLineAndLineIndex(String line, int lineIndex) {
        this.basicSet = line;
        this.lineIndex = lineIndex;
    }

    /**
     * Checks if expression is BasicSet
     * @return "YES" iff expression is a BasicSet
     */
    public String isBasicSet() {
        // if it's not string nor ident nor char, then it's not a basic set
        if (isString() || isIdent() || isCharPart()) {
            System.out.println("<<Done BasicSet specification!>>");
            return "YES";
        } else {
            return error;
        }
    }

    /**
     * Checks if expression has the form of the Char part on a BasicSet
     * @return "YES" iff expression has the form of a Char part of the BasicSet
     */
    private boolean isCharPart() {
        // check whether Char[".."Char] is true or not

        // if it is only a char
        Reader.ch.setLineAndLineIndex(basicSet, lineIndex);
        if (!Reader.getCh().isChar().equals("YES")) {
            // it's not char, now check if it's Char..Char]
            String[] splitted = splitLine();
            Reader.ch.setLineAndLineIndex(splitted[0], lineIndex);
            if (!Reader.getCh().isChar().equals("YES")) {
                error = Reader.getCh().isChar();
                return false;
            } else {
                // now check for ".."
                if (!splitted[1].equals("..")) {
                    error = "Error in line: " + lineIndex + ". No '..' found";
                    return false;
                } else {
                    // now check for last char
                    Reader.ch.setLineAndLineIndex(splitted[2], lineIndex);
                    if (!Reader.getCh().isChar().equals("YES")) {
                        error = Reader.getCh().isChar();
                        return false;
                    } else {
                        return true;
                    }
                }
            }
        } else {
            return true;
        }
    }

    /**
     * Checks if expression is a String
     * @return true if expression is String
     */
    private boolean isString() {
        //!Reader.getStringDFA().extendedDelta(basicSet.replaceAll("\\s+","")).equals("YES")
        if (!basicSet.replaceAll("\\s+","").matches("\"[^\"]*\"")) {
            error = "Error in line: " + lineIndex + ". Provided string not valid.";
            return false;
        }else {
            return true;
        }
    }

    /**
     * Checks if expression is an Ident
     * @return true if expression is an Ident
     */
    private boolean isIdent() {
        if (!basicSet.replaceAll("\\s+","").matches("[a-zA-Z0-9]*")) {
            error = "Error in line: " + lineIndex + ". Provided indent not valid.";
            return false;
        }else {
            return true;
        }
    }

    /**
     * splits the line according to special characters
     * @return array of String[] with the splitted values
     */
    private String[] splitLine() { return basicSet.split(String.format(WITH_DELIMITER, "..")); }
}
