package Handlers;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * WhiteSpaceDecl
 * Defines a white space declaration structure
 *
 * Created by Gabriel Brolo on 11/09/2017.
 */
public class WhiteSpaceDecl {
    private String line;
    private int lineIndex;
    private String error;
    static public final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";

    public WhiteSpaceDecl() {  }

    /**
     * Sets a new line an a new line index for post evaluation
     * @param line
     * @param lineIndex
     */
    public void setLineAndLineIndex(String line, int lineIndex) {
        this.line = line;
        this.lineIndex = lineIndex;
    }

    public String checkForWhiteSpace() {
        if (isWhiteSpaceDecl()) {
            String[] splitted = splitLine();
            writeToLexerGen(splitted[1]);
            return "YES";
        } else {
            return error;
        }
    }

    private boolean isWhiteSpaceDecl() {
        String[] splitted = splitLine();

        if (!splitted[0].equals("IGNORE ")) {
            return false;
        } else {
            Reader.set.setLineAndLineIndex(splitted[1], lineIndex);
            Reader.res = Reader.getSet().isSet();

            if (!Reader.res.equals("YES")) {
                error = "Error in line: " + lineIndex + ". Provided invalid Set.";
                return false;
            } else {
                try {
                    if (!splitted[2].equals(".")) {
                        error = "Error in line: " + lineIndex + ". No '.' provided at end of declaration.";
                        return false;
                    } else {
                        return true;
                    }
                } catch (Exception e){
                    error = "Error in line: " + lineIndex + ". No '.' provided at end of declaration.";
                    return false;
                }
            }
        }
    }

    /**
     * Writes line to Lexer
     * @param set
     */
    private void writeToLexerGen(String set) {
        if (set.contains("\"")){
            set = set.replace("\"", "\\\"");
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
            String input = "            lex.addWhiteSpace(" + "\"" + set + "\"" + ");";
            lines.add(position, input);
            Files.write(pathOut, lines, StandardCharsets.UTF_8);
            Reader.lexerInputLine++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] splitLine() { return line.split(String.format(WITH_DELIMITER, "IGNORE |\\.")); }
}
