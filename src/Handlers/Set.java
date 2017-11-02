package Handlers;

/**
 * Set
 * Defines a Set structure
 *
 * Created by Gabriel Brolo on 26/08/2017.
 */
public class Set {
    private String set;
    int lineIndex;
    static public final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";

    public Set() { }

    /**
     * Sets a new line an a new line index for post evaluation
     * @param line
     * @param lineIndex
     */
    public void setLineAndLineIndex(String line, int lineIndex) {
        this.set = line;
        this.lineIndex = lineIndex;
    }

    /**
     * Checks if expression is a Set
     * @return "YES" iff expression is Set
     */
    public String isSet() {
        // check for BasicSet
        Reader.bs.setLineAndLineIndex(set, lineIndex);
        Reader.res = Reader.getBs().isBasicSet();
        if (!Reader.res.equals("YES")) {
            // it's not a BasicSet, so now check for regexp
            String[] splitted = splitLine();

            Reader.bs.setLineAndLineIndex(splitted[0], lineIndex);
            Reader.res = Reader.getBs().isBasicSet();
            if (!Reader.res.equals("YES")) {
                return Reader.res;
            } else {
                // first BasicSet valid, now check for the rest
                for (int j = 1; j < splitted.length-1; j++) {
                    // check for odd indexes
                    if (j%2 != 0) {
                        if ((splitted[j].equals("+")) || (splitted[j].equals("-"))) {
                            // is valid
                        } else {
                            return "Error in line: " + lineIndex + ". No '+' or '-' provided.";
                        }
                    } else {
                        // check if it's BasicSet
                        Reader.bs.setLineAndLineIndex(splitted[j], lineIndex);
                        Reader.res = Reader.getBs().isBasicSet();
                        if (!Reader.res.equals("YES")) {
                            return Reader.res;
                        }
                    }
                }
                System.out.println("<<Done Set specification!>>");
                return "YES";
            }
        } else {
            System.out.println("<<Done Set specification!>>");
            return "YES";
        }
    }

    private String[] splitLine() { return set.split(String.format(WITH_DELIMITER, "\\+|\\-")); }
}
