import java.util.*;

/**
 * Reference recursive-descent parser - implements exactly the same grammar
 * as AfriLang.cup, hand-written in plain Java so the grammar can be
 * exercised without installing Java CUP. Not a replacement for the real
 * .cup deliverable.
 */
public class RefParser {

    private final List<RefLexer.Token> tokens;
    private int pos = 0;
    public final List<String> statements = new ArrayList<>();
    public final List<String> syntaxErrors = new ArrayList<>();

    public RefParser(List<RefLexer.Token> tokens) { this.tokens = tokens; }

    private RefLexer.Token peek() { return tokens.get(pos); }
    private RefLexer.Token advance() { return tokens.get(pos++); }
    private boolean check(RefLexer.Type t) { return peek().type == t; }

    private RefLexer.Token expect(RefLexer.Type t, String what) {
        if (check(t)) return advance();
        RefLexer.Token bad = peek();
        syntaxErrors.add("Syntax error at line " + bad.line + ": expected " + what +
            " but found '" + (bad.type == RefLexer.Type.EOF ? "end of file" : bad.text) + "'");
        // error-recovery: do not consume, let caller decide how to resync
        return null;
    }

    /** Skip tokens until the next SEMI, RBRACE, or EOF, to resynchronise after an error. */
    private void recover() {
        while (!check(RefLexer.Type.SEMI) && !check(RefLexer.Type.RBRACE) && !check(RefLexer.Type.EOF)) advance();
        if (check(RefLexer.Type.SEMI)) advance();
    }

    public void parseProgram() {
        parseStatementList(false);
        if (!check(RefLexer.Type.EOF)) {
            syntaxErrors.add("Syntax error at line " + peek().line + ": unexpected token '" + peek().text + "'");
        }
    }

    private void parseStatementList(boolean insideBlock) {
        while (!check(RefLexer.Type.EOF) && !(insideBlock && check(RefLexer.Type.RBRACE))) {
            int before = syntaxErrors.size();
            parseStatement();
            if (syntaxErrors.size() > before) {
                recover();
            }
        }
    }

    private void parseStatement() {
        switch (peek().type) {
            case PATIENT:
                parseDeclaration();
                requireSemi();
                break;
            case TEMPERATURE:
            case WEIGHT:
                parseAssignment();
                requireSemi();
                break;
            case IDENTIFIER:
                parseAssignment();
                requireSemi();
                break;
            case ADMIT:
            case DISCHARGE:
            case APPOINTMENT:
                parseTransaction();
                requireSemi();
                break;
            case SHOW:
                parseOutput();
                requireSemi();
                break;
            case IF:
                parseConditional();
                break;
            default:
                syntaxErrors.add("Syntax error at line " + peek().line + ": unexpected token '" +
                    peek().text + "'");
                advance();
        }
    }

    private void requireSemi() {
        if (expect(RefLexer.Type.SEMI, "';'") == null) {
            syntaxErrors.remove(syntaxErrors.size() - 1);
            RefLexer.Token prev = tokens.get(Math.max(0, pos - 1));
            syntaxErrors.add("Syntax error at line " + prev.line + ": Missing semicolon after statement");
        }
    }

    private void record(String desc) {
        statements.add((statements.size() + 1) + ". " + desc);
    }

    private void parseDeclaration() {
        advance(); // PATIENT
        RefLexer.Token name = expect(RefLexer.Type.IDENTIFIER, "identifier");
        expect(RefLexer.Type.AGE, "'age'");
        expect(RefLexer.Type.ASSIGN, "'='");
        RefLexer.Token val = expect(RefLexer.Type.INTEGER, "integer");
        if (name != null && val != null) {
            record("Patient declaration: " + name.text + " (age " + val.text + ")");
        }
    }

    private void parseAssignment() {
        RefLexer.Type kind = peek().type;
        if (kind == RefLexer.Type.TEMPERATURE || kind == RefLexer.Type.WEIGHT) {
            advance();
            RefLexer.Token name = expect(RefLexer.Type.IDENTIFIER, "identifier");
            expect(RefLexer.Type.ASSIGN, "'='");
            Double val = parseExpression();
            if (name != null) {
                record((kind == RefLexer.Type.TEMPERATURE ? "Temperature record: " : "Weight record: ") +
                    name.text + " = " + val);
            }
        } else {
            RefLexer.Token name = advance(); // IDENTIFIER
            expect(RefLexer.Type.ASSIGN, "'='");
            Double val = parseExpression();
            record("Variable assignment: " + name.text + " = " + val);
        }
    }

    private void parseTransaction() {
        RefLexer.Type kind = peek().type;
        advance();
        if (kind == RefLexer.Type.ADMIT || kind == RefLexer.Type.DISCHARGE) {
            RefLexer.Token name = expect(RefLexer.Type.IDENTIFIER, "identifier");
            if (name != null) record((kind == RefLexer.Type.ADMIT ? "Admission: " : "Discharge: ") + name.text);
        } else { // APPOINTMENT
            RefLexer.Token name = expect(RefLexer.Type.IDENTIFIER, "identifier");
            expect(RefLexer.Type.ON, "'on'");
            RefLexer.Token date = expect(RefLexer.Type.STRING, "string literal");
            if (name != null && date != null) {
                record("Appointment: " + name.text + " on \"" + date.text + "\"");
            }
        }
    }

    private void parseOutput() {
        advance(); // SHOW
        if (check(RefLexer.Type.PATIENT) || check(RefLexer.Type.TEMPERATURE) || check(RefLexer.Type.WEIGHT)) {
            RefLexer.Type kind = advance().type;
            RefLexer.Token name = expect(RefLexer.Type.IDENTIFIER, "identifier");
            if (name != null) {
                String label = kind == RefLexer.Type.PATIENT ? "Patient enquiry: " :
                               kind == RefLexer.Type.TEMPERATURE ? "Temperature enquiry: " : "Weight enquiry: ";
                record(label + name.text);
            }
        } else {
            RefLexer.Token name = expect(RefLexer.Type.IDENTIFIER, "identifier");
            if (name != null) record("Variable enquiry: " + name.text);
        }
    }

    private void parseConditional() {
        advance(); // IF
        parseExpression();
        parseComparator();
        parseExpression();
        expect(RefLexer.Type.LBRACE, "'{'");
        record("Conditional block entered");
        parseStatementList(true);
        expect(RefLexer.Type.RBRACE, "'}'");
    }

    private void parseComparator() {
        if (check(RefLexer.Type.GT) || check(RefLexer.Type.LT) || check(RefLexer.Type.GE) ||
            check(RefLexer.Type.LE) || check(RefLexer.Type.EQ) || check(RefLexer.Type.NE)) {
            advance();
        } else {
            syntaxErrors.add("Syntax error at line " + peek().line + ": expected a comparison operator");
        }
    }

    private Double parseExpression() {
        Double left = parseTerm();
        while (check(RefLexer.Type.PLUS) || check(RefLexer.Type.MINUS)) {
            RefLexer.Type op = advance().type;
            Double right = parseTerm();
            if (left != null && right != null) {
                left = (op == RefLexer.Type.PLUS) ? left + right : left - right;
            }
        }
        return left;
    }

    private Double parseTerm() {
        Double left = parseFactor();
        while (check(RefLexer.Type.TIMES) || check(RefLexer.Type.DIVIDE)) {
            RefLexer.Type op = advance().type;
            Double right = parseFactor();
            if (left != null && right != null) {
                left = (op == RefLexer.Type.TIMES) ? left * right : left / right;
            }
        }
        return left;
    }

    private Double parseFactor() {
        if (check(RefLexer.Type.INTEGER)) return Double.parseDouble(advance().text);
        if (check(RefLexer.Type.DECIMAL)) return Double.parseDouble(advance().text);
        if (check(RefLexer.Type.IDENTIFIER)) { advance(); return 0.0; }
        if (check(RefLexer.Type.LPAREN)) {
            advance();
            Double v = parseExpression();
            expect(RefLexer.Type.RPAREN, "')'");
            return v;
        }
        syntaxErrors.add("Syntax error at line " + peek().line + ": expected an expression, found '" +
            peek().text + "'");
        advance();
        return null;
    }
}
