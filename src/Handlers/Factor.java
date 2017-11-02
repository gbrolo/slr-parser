package Handlers;

/**
 * Created by Gabriel Brolo on 16/10/2017.
 */
public class Factor {
    private String factor;
    int lineIndex;
    static public final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";

    /**
     * Sets a new line an a new line index for post evaluation
     * @param line
     * @param lineIndex
     */
    public void setLineAndLineIndex(String line, int lineIndex) {
        this.factor = line;
        this.lineIndex = lineIndex;
    }

    public String isFactor() {
        // we start by checking if its a SemAction
        if (!factor.matches("\\(\\..*\\.\\)")) {

            if (!factor.matches("(([a-zA-Z0-9]*)|(\"[^\"]*\")|('[^'|\']*'))((\\<\\..*\\.\\>)?)('?)")) {
                if (!isVariation()) {
                    Reader.loopcount = 0;
                    return "Error in line: " + lineIndex + ". Provided invalid Factor.";
                } else {
                    return "YES";
                }
            } else {
                return "YES";
            }

//            // its not, so we check for any of the (, {, [ variations
//            if (!isVariation()) {
//                // its not, so we check if its a Symbol[Attributes]
//                Reader.loopcount = 0;
//                // first check if it has the form
//                if (!factor.matches("(([a-zA-Z0-9]*)|(\"[^\"]*\")|('[^'|\']*'))((\\<\\..*\\.\\>)?)")) {
//                    return "Error in line: " + lineIndex + ". Provided invalid Factor.";
//                } else {
//                    return "YES";
//                }
//            } else {
//                return "YES";
//            }
        } else {
            return "YES";
        }
    }

    public boolean isVariation() {
        Reader.loopcount++;

        if (Reader.loopcount < 2) {
            Reader.factorStack.push(factor);
            String subs = factor.substring(1, factor.length()-1);
            Reader.expression.setLineAndLineIndex(subs, lineIndex);
            Reader.res = Reader.getExpression().isExpression();

            String stackPop = Reader.factorStack.pop();

            if (((stackPop.matches("\\(.*\\)")) || (stackPop.matches("\\[.*\\]")) || (stackPop.matches("\\{.*\\}"))) &&
                    Reader.res.equals("YES")) {
                return true;
            } else return false;

//            if (Reader.res.equals("YES")) {
//                if ((factor.matches("\\(.*\\)")) || (factor.matches("\\[.*\\]")) || (factor.matches("\\{.*\\}"))) {
//                    return true;
//                } else return false;
//            } else return false;
        } else {
            return false;
        }
    }
}
