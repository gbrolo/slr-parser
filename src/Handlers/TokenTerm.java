package Handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TokenTerm
 * Defines a TokenTerm structure.
 *
 * Created by Gabriel Brolo on 09/09/2017.
 */
public class TokenTerm {
    private String tokenTerm;
    private int lineIndex;
    static public final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";

    public TokenTerm() {  }

    /**
     * Sets a new line an a new line index for post evaluation
     * @param line
     * @param lineIndex
     */
    public void setLineAndLineIndex(String line, int lineIndex) {
        this.tokenTerm = line;
        this.lineIndex = lineIndex;
    }

    public String isTokenTerm() {
        Reader.tokenFactor.setLineAndLineIndex(tokenTerm, lineIndex);
        Reader.res = Reader.getTokenFactor().isTokenFactor();

        if (!Reader.res.equals("YES")) {
            tokenTerm = handleDoubleQuotes(tokenTerm);
            fixInnerBoundaries();
            String[] splitted = splitLine();

            for (String split : splitted) {
                split = split.replace("Ĝ", "{");
                split = split.replace("Ğ", "}");
                split = split.replace("Ď", "[");
                split = split.replace("Đ", "]");
                if (split.contains("«") || split.contains("→") || split.contains("↠")) {
                    split = split.replace("«", "(");
                    split = split.replace("↠", "{");
                    split = split.replace("→", "[");
                } else if (split.contains("»") || split.contains("←") || split.contains("↞")) {
                    split = split.replace("»", ")");
                    split = split.replace("↠", "{");
                    split = split.replace("→", "[");
                } else if (split.contains("˧")) {
                    split = split.replace("˧", "\'");
                }

                Reader.tokenFactor.setLineAndLineIndex(split, lineIndex);
                Reader.res = Reader.getTokenFactor().isTokenFactor();

                if (!Reader.res.equals("YES")) {
                    return Reader.res;
                }
            }

            return "YES";

        } else {
            return "YES";
        }
    }

//    private String[] splitLine() {
//        Matcher m = Pattern.compile("(([{\\[(\"']\\w+[}\\])\"'])|(\\w+))").matcher(tokenTerm);
//        List<String> matches = new ArrayList<>();
//
//        while (m.find())
//            matches.add(m.group());
//
//        String[] matchesArray = matches.toArray(new String[0]);
//        return matchesArray;
//    }

    private void fixInnerBoundaries() {
        // work first with {
        for (int i = 0; i < tokenTerm.length(); i++) {
            String currChar = Character.toString(tokenTerm.charAt(i));
            if (currChar.equals("{")) {
                boolean stop = true;
                for (int j = i + 1; j < tokenTerm.length(); j++) {
                    String innerChar = Character.toString(tokenTerm.charAt(j));
                    if (innerChar.equals("{")) {
                        String leftmost = tokenTerm.substring(0, j);
                        String rightmost = tokenTerm.substring(j+1, tokenTerm.length());
                        tokenTerm = leftmost + "Ĝ" + rightmost;
                        stop = false;
                    }

                    if (innerChar.equals("}")) {
                        if (!stop) {
                            String leftmost = tokenTerm.substring(0, j);
                            String rightmost = tokenTerm.substring(j+1, tokenTerm.length());
                            tokenTerm = leftmost + "Ğ" + rightmost;
                            stop = true;
                        } else {
                            i = j;
                            break;
                        }
                    }

                    if (innerChar.equals("[")) {
                        String leftmost = tokenTerm.substring(0, j);
                        String rightmost = tokenTerm.substring(j+1, tokenTerm.length());
                        tokenTerm = leftmost + "Ď" + rightmost;
                    }

                    if (innerChar.equals("]")) {
                        String leftmost = tokenTerm.substring(0, j);
                        String rightmost = tokenTerm.substring(j+1, tokenTerm.length());
                        tokenTerm = leftmost + "Đ" + rightmost;
                    }

//                    if ((innerChar.equals("}")) && stop) {
//                        i = j;
//                        j = tokenTerm.length();
//                    }

                }
            }
        }

        // now work with [
        for (int i = 0; i < tokenTerm.length(); i++) {
            String currChar = Character.toString(tokenTerm.charAt(i));
            if (currChar.equals("[")) {
                boolean stop = true;
                for (int j = i + 1; j < tokenTerm.length(); j++) {
                    String innerChar = Character.toString(tokenTerm.charAt(j));
                    if (innerChar.equals("[")) {
                        String leftmost = tokenTerm.substring(0, j);
                        String rightmost = tokenTerm.substring(j+1, tokenTerm.length());
                        tokenTerm = leftmost + "Ď" + rightmost;
                        stop = false;
                    }

                    if (innerChar.equals("]")) {
                        if (!stop) {
                            String leftmost = tokenTerm.substring(0, j);
                            String rightmost = tokenTerm.substring(j+1, tokenTerm.length());
                            tokenTerm = leftmost + "Đ" + rightmost;
                            stop = true;
                        } else {
                            i = j;
                            break;
                        }
                    }

                    if (innerChar.equals("{")) {
                        String leftmost = tokenTerm.substring(0, j);
                        String rightmost = tokenTerm.substring(j+1, tokenTerm.length());
                        tokenTerm = leftmost + "Ĝ" + rightmost;
                    }

                    if (innerChar.equals("}")) {
                        String leftmost = tokenTerm.substring(0, j);
                        String rightmost = tokenTerm.substring(j+1, tokenTerm.length());
                        tokenTerm = leftmost + "Ğ" + rightmost;
                    }

//                    if ((innerChar.equals("]")) && stop) {
//                        i = j;
//                        break;
//                    }

                }
            }
        }
    }

    private String[] splitLine() {
        String[] strings = tokenTerm.split(String.format(WITH_DELIMITER, "\\(|\\)|\\[|\\]|\\{|\\}|\""));
        List<String> rStrings = new ArrayList<>();

        String res = Reader.res;

        for (int i=0; i < strings.length-1; i++) {
            if (strings[i].equals("(") || strings[i].equals("[") || strings[i].equals("{") || strings[i].equals("\"") || strings[i].equals("\'")) {
                String expression = strings[i] + strings[i+1] + strings[i+2];
                i = i+2;
                rStrings.add(expression);
            } else {
                rStrings.add(strings[i]);
            }
        }

        return rStrings.toArray(new String[0]);
    }

    private String handleDoubleQuotes(String string) {
        String ch = "";
        if (string.contains("\"") || string.contains("\'")) {
            String op = "";
            if (string.contains("\"")) {
                op = "\"";
            } else if (string.contains("\'")) {
                op = "\'";
            }
            string = string.replace("\"\"", "");
            string = string.replace("\'\'", "");
            for (int i=0; i < string.length(); i++) {
                if (Character.toString(string.charAt(i)).equals(op)) {
                    int j = i+1;
                    while (!Character.toString(string.charAt(j)).equals(op)) {
                        ch = Character.toString(string.charAt(j));
                        String inbetween = "";

                        String leftmost = string.substring(0,j);
                        String rightmost = string.substring(j+1, string.length());
                        if (ch.equals("(") || ch.equals("[") || ch.equals("{")) {
                            if (ch.equals("(")) {
                                inbetween = "«";
                            } else if (ch.equals("[")) {
                                inbetween = "→";
                            } else if (ch.equals("{")) {
                                inbetween = "↠";
                            }
                        } else if (ch.equals(")") || ch.equals("]") || ch.equals("}")) {
                            if (ch.equals(")")) {
                                inbetween = "»";
                            } else if (ch.equals("]")) {
                                inbetween = "←";
                            } else if (ch.equals("}")) {
                                inbetween = "↞";
                            }
                        } else if (ch.equals("\'")){
                            inbetween = "˧";
                        } else {
                            inbetween = ch;
                        }

                        ch = leftmost + inbetween + rightmost;
                        string = ch;
                        j++;
                    }
                    i = j+1;
                }
            }
        } else {
            return string;
        }
        return ch;
    }
}
