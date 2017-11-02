package Handlers;

import StateMachine.AFN;
import StateMachine.DFA;
import StateMachine.Transformation;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * SetDecl
 * Structure for a SetDecl
 *
 * Created by Gabriel Brolo on 26/08/2017.
 */
public class SetDecl {
    private String line;
    private int lineIndex;
    static public final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";

    public SetDecl() { }

    /**
     * Sets a new line an a new line index for post evaluation
     * @param line
     * @param lineIndex
     */
    public void setLineAndLineIndex(String line, int lineIndex) {
        this.line = line;
        this.lineIndex = lineIndex;
    }

    /**
     * Checks if expression is SetDecl
     * @return "YES" iff expression is SetDecl
     */
    public String isSetDecl() {
        String[] splitted = splitLine();

        // load ident regexp

        if (!splitted[0].replaceAll("\\s+","").matches("[a-zA-Z0-9]*")) {
            return "Error in line: " + lineIndex + ". Provided invalid ident";
        } else {
            // valid ident, now check for '='
            if (!splitted[1].equals("=")) {
                return "Error in line: " + lineIndex + ". No '=' after ident in SetDecl.";
            } else {
                // valid '=', now check for Set
                Reader.set.setLineAndLineIndex(splitted[2], lineIndex);
                Reader.res = Reader.getSet().isSet();

                if (!Reader.res.equals("YES")) {
                    return Reader.res;
                } else {
                    // it's a valid Set, now check for '.'
                    try {
                        if (!splitted[splitted.length-1].equals(".")) {
                            return "Error in line: " + lineIndex + ". Production not finished with '.'.";
                        } else {
                            // valid '.'
                            System.out.println("<<Done SetDecl specification!>>");

                            // Add character expression to Lex
//                            Reader.lex.addCharacter(splitted[0].replaceAll("\\s+",""), splitted[2].replaceAll("\\s+",""));

                            String expr = "";
                            for (int i = 2; i < splitted.length-1; i++) {
                                expr = expr + splitted[i];
                            }
                            writeToLexerGen(splitted[0].replaceAll("\\s+",""), expr.replaceAll("\\s+",""));

//                            if (splitted.length > 4) {
//                                if (splitted[4].equals(".")) {
//                                    String exp = splitted[2] + splitted[3] + splitted[4] + splitted[5];
//                                    writeToLexerGen(splitted[0].replaceAll("\\s+",""), exp.replaceAll("\\s+",""));
//                                }
//                            } else {
//                                writeToLexerGen(splitted[0].replaceAll("\\s+",""), splitted[2].replaceAll("\\s+",""));
//                            }

                            return "YES";
                        }
                    } catch (Exception e) {
                        return "Error in line: " + lineIndex + ". Production not finished with '.'.";
                    }
                }
            }
        }
    }

    /**
     * Writes line to Lexer
     * @param ident
     * @param string
     */
    private void writeToLexerGen(String ident, String string) {
        if (string.contains("\"")){
            string = string.replace("\"", "\\\"");
        }
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
            String input = "            lex.addCharacter(" + "\"" + ident + "\"" + ", " + "\"" + string + "\"" + ");";
            lines.add(position, input);
            Files.write(pathOut, lines, StandardCharsets.UTF_8);
            Reader.lexerInputLine++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] splitLine() { return line.split(String.format(WITH_DELIMITER, "=|\\.")); }
}
