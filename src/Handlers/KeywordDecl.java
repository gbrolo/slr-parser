package Handlers;

import StateMachine.AFN;
import StateMachine.DFA;
import StateMachine.Transformation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * KeywordDecl
 * Defines the Structure of a KeywordDecl
 *
 * Created by Gabriel Brolo on 25/08/2017.
 */
public class KeywordDecl {
    private String line;
    private int lineIndex;
    static public final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";

    public KeywordDecl() { }

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
     * The actual thing, checks if expression is KeywordDecl
     * @return "YES" iff expression is KeywordDecl, else returns error message
     */
    public String isKeywordDecl() {
        line = line.replace("\".\"", "\"dotExp\"");
        String[] splitted = splitLine();

        if (!splitted[0].replaceAll("\\s+","").matches("[a-zA-Z0-9]*")) {
            // ident invalid
            return "Error in line: " + lineIndex + ". Provided invalid ident.";
        } else {
            // ident valid, now check for '='
            if (!splitted[1].equals("=")) {
                return "Error in line: " + lineIndex + ". No '=' after ident in KeywordDecl.";
            } else {
                if (!splitted[2].replaceAll("\\s+","").matches("\"[^\"]*\"")) {
                    // string invalid
                    return "Error in line: " + lineIndex + ". Provided invalid string.";
                } else {
                    // string valid, now check for '.'
                    splitted[2] = splitted[2].replace("dotExp", ".");
                    try {
                        if (!splitted[3].equals(".")) {
                            return "Error in line: " + lineIndex + ". Production not finished with '.'.";
                        } else {
                            // valid '.'
                            System.out.println("<<Done SetDecl specification!>>");

                            // add Keyword to Lexer
//                            Reader.lex.addKeyword(splitted[2].replaceAll("\\s+|\"",""), splitted[0].replaceAll("\\s+",""));
                            writeToLexerGen(splitted[2].replaceAll("\\s+",""), splitted[0].replaceAll("\\s+",""));

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
     * Writes expression to Lexer
     * @param ident
     * @param string
     */
    private void writeToLexerGen(String ident, String string) {
        if (ident.contains("\"")){
            ident = ident.replace("\"", "\\\"");
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
            String input = "            lex.addKeyword(" + "\"" + string + "\"" + ", " + "\"" + ident + "\"" + ");";
            lines.add(position, input);
            Files.write(pathOut, lines, StandardCharsets.UTF_8);
            Reader.lexerInputLine++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Splits line into array of strings
     * @return array of String[] with splitted values
     */
    private String[] splitLine() { return line.split(String.format(WITH_DELIMITER, "=|\\.")); }
}
