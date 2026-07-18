import java_cup.runtime.*;
import java.io.FileReader;
import java.io.IOException;

/**
 * AfriLang - Community Health Centre scenario
 * Entry point that wires the JFlex-generated Lexer to the
 * Java CUP-generated parser and reports results.
 *
 * Usage: java -cp .:java-cup-11b-runtime.jar Main tests/valid_test1.afri
 */
public class Main {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java Main <path-to-.afri-file>");
            return;
        }

        String path = args[0];
        ComplexSymbolFactory sf = new ComplexSymbolFactory();

        try (FileReader reader = new FileReader(path)) {
            Lexer lexer = new Lexer(reader, sf);
            parser p = new parser(lexer, sf);

            p.parse();

            int lexicalErrors = lexer.getErrorCount();
            int syntaxErrors = p.getErrorCount();

            if (lexicalErrors == 0 && syntaxErrors == 0) {
                System.out.println("Scanning completed successfully.");
                System.out.println("Parsed statements:");
                for (String s : p.getStatements()) {
                    System.out.println(s);
                }
                System.out.println("Program parsed successfully.");
            } else {
                System.out.println("Compilation failed with " + (lexicalErrors + syntaxErrors) + " errors.");
            }

        } catch (IOException e) {
            System.out.println("Could not read file: " + path);
        } catch (Exception e) {
            System.out.println("Compilation failed: " + e.getMessage());
        }
    }
}
