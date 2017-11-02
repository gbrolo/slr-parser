package Handlers;

import StateMachine.AFN;
import StateMachine.DFA;
import StateMachine.Transformation;
import jdk.nashorn.internal.ir.ReturnNode;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * StructureReader
 * Created by Gabriel Brolo on 25/08/2017.
 *
 * Checks if file is sintactically correct
 */
public class StructureReader {
    private String line;
    private int index;
    private int lineIndex;
    private String[] splitted;
    private String programIdent;
    private String currentSpec;
    private boolean error;
    private boolean foundEND;
    private boolean foundCHARACTERS;
    private boolean foundKEYWORDS;
    private boolean foundTOKENS;
    private boolean foundPRODUCTIONS;
    private boolean foundIGNORE;

    public StructureReader() {
        error = false;
        foundEND = false;
        foundCHARACTERS = false;
        foundKEYWORDS = false;
        foundTOKENS = false;
        foundPRODUCTIONS = false;
        foundIGNORE= false;
    }

    /**
     * Sets the line and index for analysis
     * @param line
     * @param index
     * @param lineIndex
     */
    public void setLineAndIndex(String line, int index, int lineIndex) {
        this.line = line;
        this.index = index;
        this.lineIndex = lineIndex;
    }

    /**
     * Checks structure of line
     * @return
     */
    public String analyzeStructure() {
        // check first line
        splitted = splitLineBySpace(line);
        if (this.index == 0) {

            // since this is the first line we expect: COMPILER ident
            if (splitted.length > 2) {
                error = true;
                return "Error in line: " + lineIndex + ". File must start only with reserved word COMPILER and an ident. Word limit exceded.";
            } else {
                // check for COMPILER
                if (splitted[0].equals("COMPILER")){
                    // true now check ident
                    // validate simulation results
                    if (!splitted[1].matches("[a-zA-Z0-9]*")) {
                        // ident invalid
                        return "Error in line: " + lineIndex + ". Provided invalid ident.";
                    } else {
                        programIdent = splitted[1]; // valid program ident, save for later to compare with END ident
                    }
                } else {
                    error = true;
                    return "Error in line: " + lineIndex + ". File must start with reserved word COMPILER.";
                }
                System.out.println("<<Done checking first line!>>");
            }
        } else {
            // not first line

            // empty body, but provided header and footer
            if ((index == 1) && (splitted[0].equals("END"))) {
                if (!splitted[1].equals(programIdent+".")) {
                    error = true;
                    return "Error in line: " + lineIndex + ". Program ident at END is not the same as in COMPILER or missing '.'";
                } else {
                    foundEND = true;
                }
            }

            if (!line.equals("")) {
                if (!foundEND) {
                    // check for ScannerSpecification identifiers
                    // if CHARACTERS
                    if ((splitted[0].equals("CHARACTERS"))) {
                        if ((!foundKEYWORDS) && (!foundTOKENS) && (!foundPRODUCTIONS)) {
                            foundCHARACTERS = true;
                            currentSpec = "CHARACTERS";
                            //System.out.println("Found characters");
                        } else {
                            error = true;
                            return "Error in line: " + lineIndex + ". CHARACTERS can't be specified after KEYWORDS or TOKENS.";
                        }
                    }

                    // if KEYWORDS
                    if ((splitted[0].equals("KEYWORDS"))) {
                        if (!foundTOKENS && !foundPRODUCTIONS) {
                            foundKEYWORDS = true;
                            currentSpec = "KEYWORDS";
                            //System.out.println("Found keywords");
                        } else {
                            error = true;
                            return "Error in line: " + lineIndex + ". KEYWORDS can't be specified after TOKENS.";
                        }
                    }

                    // if TOKENS
                    if ((splitted[0].equals("TOKENS"))) {
                        if (!foundPRODUCTIONS) {
                            foundTOKENS = true;
                            currentSpec = "TOKENS";
                        } else {
                            error = true;
                            return "Error in line: " + lineIndex + ". TOKENS can't be specified after PRODUCTIONS.";
                        }
                    }

                    // if PRODUCTIONS
                    if ((splitted[0].equals("PRODUCTIONS"))) {
                        foundPRODUCTIONS = true;
                        currentSpec = "PRODUCTIONS";
                    }

                    // whitespace
                    if (splitted[0].equals("IGNORE")) {
                        foundIGNORE = true;
                        Reader.whiteSpaceDecl.setLineAndLineIndex(line, lineIndex);
                        Reader.res = Reader.getWhiteSpaceDecl().checkForWhiteSpace();

                        if (!Reader.res.equals("YES")) {
                            return Reader.res;
                        } else {
                            System.out.println("<<Done WhiteSpaceDecl specification!>>");
                        }
                    } else {
                        foundIGNORE = false;
                    }

                    // analyze tokenDecl
                    if ((currentSpec.equals("TOKENS")) && !line.equals("TOKENS") && (!splitted[0].equals("END"))) {
                        if (!foundIGNORE) {
                            Reader.tokenDecl.setLineAndLineIndex(line, lineIndex);
                            Reader.res = Reader.getTokenDecl().isTokenDecl();
                            if (!Reader.res.equals("YES")) {
                                if (Reader.res.equals("")) {
                                    writeToLexerGen(line);
                                }
                                return Reader.res;
                            } else {
                                writeToLexerGen(line);
                            }
                        }
                    }

                    // analyze productions
                    if ((currentSpec.equals("PRODUCTIONS")) && !line.equals("PRODUCTIONS") && (!splitted[0].equals("END"))) {
                        if (!foundIGNORE) {
                            Reader.production.setLineAndLineIndex(line, lineIndex);
                            Reader.res = Reader.getProduction().isProduction();
                            if (!Reader.res.equals("YES")) {
                                return Reader.res;
                            } else {
                                System.out.println("<<Done ParserSpecification!>>");
                            }
                        }
                    }

                    // analyze keywordDecl
                    if ((currentSpec.equals("KEYWORDS")) && !line.equals("KEYWORDS") && (!splitted[0].equals("END"))) {
                        if (!foundIGNORE) {
                            Reader.keywordDecl.setLineAndLineIndex(line, lineIndex);
                            Reader.res = Reader.getKeywordDecl().isKeywordDecl();
                            if (!Reader.res.equals("YES")) {
                                return Reader.res;
                            }
                        }
                    }

                    // analyze characters
                    if ((currentSpec.equals("CHARACTERS")) && !line.equals("CHARACTERS") && (!splitted[0].equals("END"))) {
                        if (!foundIGNORE) {
                            Reader.setDecl.setLineAndLineIndex(line, lineIndex);
                            Reader.res = Reader.getSetDecl().isSetDecl();

                            if (!Reader.res.equals("YES")) {
                                return Reader.res;
                            }
                        }
                    }

                    // in case ScannerSpecification does not contain anything
                    if ((splitted[0].equals("END"))) {
                        if (!splitted[1].equals(programIdent+".")) {
                            error = true;
                            return "Error in line: " + lineIndex + ". Program ident at END is not the same as in COMPILER or missing '.'";
                        } else {
                            foundEND = true;
                        }
                    }

                    foundIGNORE = false;

                } else if (index != 1) {
                    error = true;
                    return "Error in line: " + lineIndex + ". END already found.";
                }
            }

        }

        return "";
    }

    private String[] splitLineBySpace(String line) {
        return line.split("\\s+");
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
            String input = "            lex.addToken(" + "\"" + line + "\"" + ");";
            lines.add(position, input);
            Files.write(pathOut, lines, StandardCharsets.UTF_8);
            Reader.lexerInputLine++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean getError() { return this.error; }
    public boolean didFoundEND() { return this.foundEND; }
}
