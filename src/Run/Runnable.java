package Run;

import Handlers.Reader;
import StateMachine.AFN;
import StateMachine.DFA;
import StateMachine.Transformation;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Runnable.
 * Main file. Run it to load .txt file with language specification.
 */
public class Runnable {

    public static void main(String []args) {

        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("<<--Welcome to the Syntax Reader-->>\nPlease, enter the filename to scan, " +
                    "starting with \"/\" (current directory is" +
                    "../src):");
            String path = scanner.nextLine();
            //String path = "/example1.txt";
            System.out.println("Started " + path + "...");
            long start = System.nanoTime();
            Reader reader = new Reader(path);
            reader.readCocolFile(path);

            if (!Reader.exitCode.equals("1")) {
                System.out.println("<<Lexer generated!>>");
            }

            System.out.println("Process finished with exit code " + Reader.exitCode);
            long end = System.nanoTime();
            double duration = ((end - start)/ 1000000)*1e-3; // time in seconds
            System.out.println("Execution took: " + duration + " seconds.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
