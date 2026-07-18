/* =========================================================================
 * AfriLang.flex
 * Scenario E: Community Health Centre
 *
 * JFlex specification for the AfriLang lexical scanner.
 * Generates: Lexer.java  (must be run through JFlex to produce this class)
 *
 * Build (after installing JFlex, e.g. jflex-1.8.2.jar):
 *   java -jar jflex-1.8.2.jar -d generated src/AfriLang.flex
 * ========================================================================= */

import java_cup.runtime.*;
import java.util.HashMap;

%%

%class Lexer
%unicode
%cup
%line
%column

%{
    /* ----------------------------------------------------------------
       Symbol factory supplied by java_cup.runtime so that every token
       carries its line and column number through to the parser.
       ---------------------------------------------------------------- */
    private ComplexSymbolFactory symbolFactory;

    public Lexer(java.io.Reader in, ComplexSymbolFactory sf) {
        this(in);
        this.symbolFactory = sf;
    }

    private Symbol symbol(String name, int sym) {
        return symbolFactory.newSymbol(name, sym,
                new ComplexSymbolFactory.Location(yyline + 1, yycolumn + 1),
                new ComplexSymbolFactory.Location(yyline + 1, yycolumn + yylength()));
    }

    private Symbol symbol(String name, int sym, Object val) {
        return symbolFactory.newSymbol(name, sym,
                new ComplexSymbolFactory.Location(yyline + 1, yycolumn + 1),
                new ComplexSymbolFactory.Location(yyline + 1, yycolumn + yylength()), val);
    }

    /* Keyword lookup table -> terminal symbol constants from sym.java */
    private static final HashMap<String, Integer> keywords = new HashMap<>();
    static {
        keywords.put("patient",     sym.PATIENT);
        keywords.put("admit",       sym.ADMIT);
        keywords.put("discharge",   sym.DISCHARGE);
        keywords.put("temperature", sym.TEMPERATURE);
        keywords.put("weight",      sym.WEIGHT);
        keywords.put("appointment", sym.APPOINTMENT);
        keywords.put("show",        sym.SHOW);
        keywords.put("age",         sym.AGE);
        keywords.put("on",          sym.ON);
        keywords.put("if",          sym.IF);
    }

    private int errorCount = 0;
    public int getErrorCount() { return errorCount; }
%}

%eofval{
    return symbolFactory.newSymbol("EOF", sym.EOF);
%eofval}

LineTerminator = \r|\n|\r\n
WhiteSpace     = {LineTerminator} | [ \t\f]
Digit          = [0-9]
Letter         = [a-zA-Z]
Identifier     = {Letter}({Letter}|{Digit}|"_")*
Integer        = {Digit}+
Decimal        = {Digit}+"."{Digit}+
StringLiteral  = \"([^\"\\\n]|\\.)*\"
Comment        = "//"[^\r\n]*

%%

<YYINITIAL> {

  {Comment}          { /* single-line comment: ignored */ }
  {WhiteSpace}       { /* whitespace: ignored */ }

  /* ---- keywords (checked before generic identifier) ---- */
  "patient"          { return symbol("PATIENT", sym.PATIENT); }
  "admit"            { return symbol("ADMIT", sym.ADMIT); }
  "discharge"        { return symbol("DISCHARGE", sym.DISCHARGE); }
  "temperature"      { return symbol("TEMPERATURE", sym.TEMPERATURE); }
  "weight"           { return symbol("WEIGHT", sym.WEIGHT); }
  "appointment"      { return symbol("APPOINTMENT", sym.APPOINTMENT); }
  "show"             { return symbol("SHOW", sym.SHOW); }
  "age"              { return symbol("AGE", sym.AGE); }
  "on"               { return symbol("ON", sym.ON); }
  "if"               { return symbol("IF", sym.IF); }

  /* ---- literals ---- */
  {Decimal}          { return symbol("DECIMAL", sym.DECIMAL, Double.parseDouble(yytext())); }
  {Integer}          { return symbol("INTEGER", sym.INTEGER, Integer.parseInt(yytext())); }
  {StringLiteral}    { String raw = yytext();
                       return symbol("STRING", sym.STRING, raw.substring(1, raw.length() - 1)); }
  {Identifier}       { return symbol("IDENTIFIER", sym.IDENTIFIER, yytext()); }

  /* ---- operators & delimiters ---- */
  "="                { return symbol("ASSIGN", sym.ASSIGN); }
  "+"                { return symbol("PLUS", sym.PLUS); }
  "-"                { return symbol("MINUS", sym.MINUS); }
  "*"                { return symbol("TIMES", sym.TIMES); }
  "/"                { return symbol("DIVIDE", sym.DIVIDE); }
  ">="               { return symbol("GE", sym.GE); }
  "<="               { return symbol("LE", sym.LE); }
  "=="               { return symbol("EQ", sym.EQ); }
  "!="               { return symbol("NE", sym.NE); }
  ">"                { return symbol("GT", sym.GT); }
  "<"                { return symbol("LT", sym.LT); }
  ";"                { return symbol("SEMI", sym.SEMI); }
  "("                { return symbol("LPAREN", sym.LPAREN); }
  ")"                { return symbol("RPAREN", sym.RPAREN); }
  "{"                { return symbol("LBRACE", sym.LBRACE); }
  "}"                { return symbol("RBRACE", sym.RBRACE); }

  /* ---- anything else is illegal ---- */
  .                  {
                        errorCount++;
                        System.out.println("Lexical error at line " + (yyline + 1) +
                            ", column " + (yycolumn + 1) + ": Illegal character '" + yytext() + "'");
                     }
}
