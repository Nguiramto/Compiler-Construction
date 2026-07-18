import java.util.*;

/**
 * Reference lexer - implements exactly the same token rules as AfriLang.flex,
 * hand-written in plain Java so the grammar can be verified without
 * installing JFlex. Not a replacement for the real .flex deliverable.
 */
public class RefLexer {

    public enum Type {
        PATIENT, ADMIT, DISCHARGE, TEMPERATURE, WEIGHT, APPOINTMENT, SHOW, AGE, ON, IF,
        IDENTIFIER, INTEGER, DECIMAL, STRING,
        ASSIGN, PLUS, MINUS, TIMES, DIVIDE,
        GT, LT, GE, LE, EQ, NE,
        SEMI, LPAREN, RPAREN, LBRACE, RBRACE,
        EOF
    }

    public static class Token {
        public final Type type;
        public final String text;
        public final int line, col;
        public Token(Type type, String text, int line, int col) {
            this.type = type; this.text = text; this.line = line; this.col = col;
        }
        public String toString() { return type + "(" + text + ")@" + line + ":" + col; }
    }

    private static final Map<String, Type> KEYWORDS = new HashMap<>();
    static {
        KEYWORDS.put("patient", Type.PATIENT);
        KEYWORDS.put("admit", Type.ADMIT);
        KEYWORDS.put("discharge", Type.DISCHARGE);
        KEYWORDS.put("temperature", Type.TEMPERATURE);
        KEYWORDS.put("weight", Type.WEIGHT);
        KEYWORDS.put("appointment", Type.APPOINTMENT);
        KEYWORDS.put("show", Type.SHOW);
        KEYWORDS.put("age", Type.AGE);
        KEYWORDS.put("on", Type.ON);
        KEYWORDS.put("if", Type.IF);
    }

    private final String src;
    private int pos = 0, line = 1, col = 1;
    public final List<String> lexicalErrors = new ArrayList<>();

    public RefLexer(String src) { this.src = src; }

    private char peek() { return pos < src.length() ? src.charAt(pos) : '\0'; }
    private char peek(int off) { return pos + off < src.length() ? src.charAt(pos + off) : '\0'; }

    private char advance() {
        char c = src.charAt(pos++);
        if (c == '\n') { line++; col = 1; } else { col++; }
        return c;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (pos < src.length()) {
            char c = peek();

            if (c == ' ' || c == '\t' || c == '\r' || c == '\n') { advance(); continue; }

            if (c == '/' && peek(1) == '/') {
                while (pos < src.length() && peek() != '\n') advance();
                continue;
            }

            int startLine = line, startCol = col;

            if (Character.isLetter(c)) {
                StringBuilder sb = new StringBuilder();
                while (Character.isLetterOrDigit(peek()) || peek() == '_') sb.append(advance());
                String text = sb.toString();
                Type kw = KEYWORDS.get(text);
                tokens.add(new Token(kw != null ? kw : Type.IDENTIFIER, text, startLine, startCol));
                continue;
            }

            if (Character.isDigit(c)) {
                StringBuilder sb = new StringBuilder();
                while (Character.isDigit(peek())) sb.append(advance());
                if (peek() == '.' && Character.isDigit(peek(1))) {
                    sb.append(advance());
                    while (Character.isDigit(peek())) sb.append(advance());
                    tokens.add(new Token(Type.DECIMAL, sb.toString(), startLine, startCol));
                } else {
                    tokens.add(new Token(Type.INTEGER, sb.toString(), startLine, startCol));
                }
                continue;
            }

            if (c == '"') {
                advance();
                StringBuilder sb = new StringBuilder();
                while (pos < src.length() && peek() != '"' && peek() != '\n') sb.append(advance());
                if (peek() == '"') advance();
                tokens.add(new Token(Type.STRING, sb.toString(), startLine, startCol));
                continue;
            }

            if (c == '>' && peek(1) == '=') { advance(); advance(); tokens.add(new Token(Type.GE, ">=", startLine, startCol)); continue; }
            if (c == '<' && peek(1) == '=') { advance(); advance(); tokens.add(new Token(Type.LE, "<=", startLine, startCol)); continue; }
            if (c == '=' && peek(1) == '=') { advance(); advance(); tokens.add(new Token(Type.EQ, "==", startLine, startCol)); continue; }
            if (c == '!' && peek(1) == '=') { advance(); advance(); tokens.add(new Token(Type.NE, "!=", startLine, startCol)); continue; }

            switch (c) {
                case '=': advance(); tokens.add(new Token(Type.ASSIGN, "=", startLine, startCol)); continue;
                case '+': advance(); tokens.add(new Token(Type.PLUS, "+", startLine, startCol)); continue;
                case '-': advance(); tokens.add(new Token(Type.MINUS, "-", startLine, startCol)); continue;
                case '*': advance(); tokens.add(new Token(Type.TIMES, "*", startLine, startCol)); continue;
                case '/': advance(); tokens.add(new Token(Type.DIVIDE, "/", startLine, startCol)); continue;
                case '>': advance(); tokens.add(new Token(Type.GT, ">", startLine, startCol)); continue;
                case '<': advance(); tokens.add(new Token(Type.LT, "<", startLine, startCol)); continue;
                case ';': advance(); tokens.add(new Token(Type.SEMI, ";", startLine, startCol)); continue;
                case '(': advance(); tokens.add(new Token(Type.LPAREN, "(", startLine, startCol)); continue;
                case ')': advance(); tokens.add(new Token(Type.RPAREN, ")", startLine, startCol)); continue;
                case '{': advance(); tokens.add(new Token(Type.LBRACE, "{", startLine, startCol)); continue;
                case '}': advance(); tokens.add(new Token(Type.RBRACE, "}", startLine, startCol)); continue;
                default:
                    lexicalErrors.add("Lexical error at line " + startLine + ", column " + startCol +
                        ": Illegal character '" + c + "'");
                    advance();
                    continue;
            }
        }
        tokens.add(new Token(Type.EOF, "", line, col));
        return tokens;
    }
}
