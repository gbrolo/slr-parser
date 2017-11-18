import Handlers.Lexer;
import Handlers.Reader;

import java.util.Scanner;

/**
 * AUTOGENERATED FILE
 * Generates a Lexer program to lex a specified input file.
 * @author: brolius
 */
public class LexerGenerator {

    public static void main (String[] args) {
        try {
            Lexer lex = new Lexer();
            /* Add specifications for lex here */

            lex.addCharacter("digit", "\"0123456789\"");
            lex.addCharacter("openingPar", "'('");
            lex.addCharacter("closingPar", "')'");
            lex.addKeyword("if", "\"if\"");
            lex.addKeyword("else", "\"else\"");
            lex.addKeyword("print", "\"print\"");
            lex.addToken("int=digit{digit}");
            lex.addToken("plus=\"+\"");
            lex.addToken("mult=\"*\"");
            lex.addToken("sum=int(plus)int");
            lex.addToken("multiplication=int(mult)int");
            lex.addToken("op=openingPar");
            lex.addToken("cp=closingPar");
            lex.addProduction("E=T\'+\'E");
            lex.addProduction("E=T");
            lex.addProduction("T=int\'*\'T");
            lex.addProduction("T=int");
            lex.addProduction("T=(E)");
            lex.addWhiteSpace("\" \"");

            /* Ended lex specifications */
            lex.extendProductions();
            lex.applyClosures();
            lex.buildDFA();
            Reader reader = new Reader();
            System.out.println("<<--Welcome to the Lexer-->>\nPlease, enter the filename to lex, " +
                    "starting with \"/\" (current directory is" +
                    "../src):");
            Scanner scanner = new Scanner(System.in);
            String fileroute = scanner.nextLine();
            reader.exitCode = "0";
            reader.readFileToLex(fileroute, lex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
