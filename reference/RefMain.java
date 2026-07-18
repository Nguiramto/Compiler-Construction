import java.nio.file.*;
import java.util.*;

public class RefMain {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: java RefMain <path-to-.afri-file>");
            return;
        }
        String src = new String(Files.readAllBytes(Paths.get(args[0])));

        RefLexer lexer = new RefLexer(src);
        List<RefLexer.Token> tokens = lexer.tokenize();

        RefParser parser = new RefParser(tokens);
        parser.parseProgram();

        for (String e : lexer.lexicalErrors) System.out.println(e);
        for (String e : parser.syntaxErrors) System.out.println(e);

        int total = lexer.lexicalErrors.size() + parser.syntaxErrors.size();
        if (total == 0) {
            System.out.println("Scanning completed successfully.");
            System.out.println("Parsed statements:");
            for (String s : parser.statements) System.out.println(s);
            System.out.println("Program parsed successfully.");
        } else {
            System.out.println("Compilation failed with " + total + " errors.");
        }
    }
}
