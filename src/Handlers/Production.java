package Handlers;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by Gabriel Brolo on 16/10/2017.
 */
public class Production {
    private String line;
    private int lineIndex;
    static public final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";

    /**
     * Sets a new line an a new line index for post evaluation
     * @param line
     * @param lineIndex
     */
    public void setLineAndLineIndex(String line, int lineIndex) {
        this.line = line;
        this.lineIndex = lineIndex;
    }

    public String isProduction() {
        String[] splitted = splitLine();
        String[] leftSplit = splitLeftProduction(splitted[0]);

        // work first with leftSplit
        if (!leftSplit[0].replaceAll("\\s+","").matches("[a-zA-Z0-9]*(')?")) {
            return "Error in line: " + lineIndex + ". Provided invalid ident";
        } else {
            // valid ident
            // check for Attributes or SemActions
            if (leftSplit.length > 1) {
                for (int i = 1; i < leftSplit.length; i++) {
                    if (leftSplit[i].equals("<")) {
                        if (!leftSplit[i+1].equals(".")) {
                            return "Error in line: " + lineIndex + ". No \".\" after \"<\"";
                        }
                    } else if (leftSplit[i].equals(">")) {
                        if (!leftSplit[i-1].equals(".")) {
                            return "Error in line: " + lineIndex + ". No \".\" before \">\"";
                        }
                    } else if (leftSplit[i].equals("(")) {
                        if (!leftSplit[i+1].equals(".")) {
                            return "Error in line: " + lineIndex + ". No \".\" after \"(\"";
                        }
                    } else if (leftSplit[i].equals(")")) {
                        if (!leftSplit[i-1].equals(".")) {
                            return "Error in line: " + lineIndex + ". No \".\" before \")\"";
                        }
                    }
                }
            }

            // now continue with splitted
            if (!splitted[1].replaceAll("\\s+","").equals("=")) {
                return "Error in line: " + lineIndex + ". No '=' after ident in Production.";
            } else {
                if (splitted[splitted.length-1].charAt(splitted[splitted.length-1].length()-1) != '.') {
                    return "Error in line: " + lineIndex + ". Missing end point.";
                } else {
                    String subs = splitted[splitted.length-1].substring(0, splitted[splitted.length-1].length()-1);
                    Reader.expression.setLineAndLineIndex(subs, lineIndex);
                    Reader.res = Reader.getExpression().isExpression();

                    if (!Reader.res.equals("YES")) {
                        return "Error in line: " + lineIndex + ". Invalid Expression.";
                    } else {
                        System.out.println("<<Done Production specification!>>");
                        writeToLexerGen(line);
                        return "YES";
                    }
                }

//                String[] dotSplit = splitByDot(splitted[splitted.length-1]);
//                Reader.expression.setLineAndLineIndex(dotSplit[0], lineIndex);
//                Reader.res = Reader.getExpression().isExpression();
//
//                if (!Reader.res.equals("YES")) {
//                    return "Error in line: " + lineIndex + ". Invalid Expression.";
//                } else {
//                    if (!dotSplit[1].equals(".")) {
//                        return "Error in line: " + lineIndex + ". Missing end point.";
//                    } else {
//                        return "YES";
//                    }
//                }
            }

        }
    }

    private void writeToLexerGen(String line) {
        if (line.contains("\"")){
            line = line.replace("\"", "\\\"");
        }

        if (line.contains("\'")) {
            line = line.replace("\'", "\\\'");
        }

        // remove last '.'
        line = line.substring(0, line.length()-1);
        line = line.replaceAll("\\s+","");

        String pt = "";
        File file1 = new File("src/LexerGenerator.java");
        File file2 = new File("src/LexerGenerator.txt");
        Path pathOut = Paths.get(file1.getAbsolutePath());
        if (Reader.lexerInputLine == 17) {
            pt = file2.getAbsolutePath();
        } else {
            pt = file1.getAbsolutePath();
        }
        try {
            Path path = Paths.get(pt);
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            int position = Reader.lexerInputLine;
            String input = "            lex.addProduction(" + "\"" + line + "\"" + ");";
            lines.add(position, input);
            Files.write(pathOut, lines, StandardCharsets.UTF_8);
            Reader.lexerInputLine++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] splitLine() { return line.split(String.format(WITH_DELIMITER, "=")); }

    private String[] splitByDot(String line) { return line.split(String.format(WITH_DELIMITER, "\\.")); }

    private String[] splitLeftProduction(String leftProduction) { return leftProduction.split(String.format(WITH_DELIMITER, "<\\.|\\.>|(\\.|\\.)")); }
}
