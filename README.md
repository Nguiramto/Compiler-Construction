# AfriLang — Community Health Centre Scenario (Scenario E)

A mini compiler front end for recording patient details, temperature/weight
readings, appointments, and admissions/discharges at a community health
centre.

## ⚠️ Important note on what was tested where

This project was built in a sandbox that has a Java **runtime** (`java`) but
no **JDK** (`javac`) and no internet access, so JFlex, Java CUP, and a real
compiler could not be installed or run here. That means:

- `src/AfriLang.flex` and `src/AfriLang.cup` are the **real, required
  deliverables** — hand-written, complete JFlex/CUP source. They have **not**
  been run through the actual JFlex/CUP generators yet.
- `reference/` contains a **plain-Java, dependency-free re-implementation**
  of the exact same tokens and grammar (a hand-rolled lexer + recursive-descent
  parser). It exists only so the grammar logic could be traced/reasoned about
  without the real tools, and so you have something to compare output against.
  **It is not a substitute for actually running JFlex and CUP** — the
  assignment requires genuine use of both tools ("At least one use of JFlex
  and Java CUP is compulsory").
- You must complete the real build (steps below) on a machine with a JDK and
  internet access, confirm it compiles and runs, and take the screenshots for
  submission.

## 1. Language Specification (Task 1)

**Scenario:** Community Health Centre.
**Purpose:** record patients, vital signs, appointments, and admission status.

**Keywords:** `patient`, `admit`, `discharge`, `temperature`, `weight`,
`appointment`, `show`, `age`, `on`, `if`

**Operators/delimiters:** `= + - * / > < >= <= == != ; ( ) { }`

**Identifier rule:** starts with a letter, then letters/digits/underscore.

**Statement forms:**
1. Entity declaration — `patient Musa age = 35;`
2. Attribute assignment — `temperature Musa = 38.5;` / `weight Musa = 62.5;`
3. Plain arithmetic assignment — `bmi = 62.5 / 2.89;`
4. Transactions — `admit Amina;` `discharge Kato;` `appointment Musa on "2026-07-16";`
5. Output — `show patient Musa;` `show temperature Musa;`
6. Conditional — `if temperature_reading >= 38.0 { ... }`
7. Comments — `// like this`

## 2. Required Project Structure

```
AfriLang/
├── src/AfriLang.flex        (JFlex scanner spec — write once, generate Lexer.java)
├── src/AfriLang.cup         (CUP grammar spec — write once, generate parser.java, sym.java)
├── src/Main.java            (driver; unchanged, references generated classes)
├── generated/                (put Lexer.java, parser.java, sym.java here after generation)
├── tests/*.afri              (6 test programs, already included)
├── screenshots/              (put your console-output screenshots here)
└── reference/                 (bonus hand-written demo — not for submission)
```

## 3. Build Instructions (run this on your own machine)

1. Download the tools:
   - `jflex-1.8.2.jar` from https://jflex.de/download.html
   - `java-cup-11b.jar` and `java-cup-11b-runtime.jar` from
     https://www2.cs.tum.edu/projects/cup/

2. Generate the scanner:
   ```
   java -jar jflex-1.8.2.jar -d generated src/AfriLang.flex
   ```

3. Generate the parser (produces `parser.java` and `sym.java` in `generated/`):
   ```
   java -jar java-cup-11b.jar -parser parser -symbols sym -destdir generated src/AfriLang.cup
   ```

4. Compile everything:
   ```
   javac -cp java-cup-11b-runtime.jar -d out src/Main.java generated/*.java
   ```

5. Run against a test file:
   ```
   java -cp out:java-cup-11b-runtime.jar Main tests/valid_test1.afri
   ```

## 4. Expected Output (traced by hand against the grammar)

For `tests/valid_test1.afri`:
```
Scanning completed successfully.
Parsed statements:
1. Patient declaration: Musa (age 35)
2. Temperature record: Musa = 38.5
3. Appointment: Musa on "2026-07-16"
4. Patient enquiry: Musa
Program parsed successfully.
```

For `tests/lexical_error.afri` (contains `37@5`):
```
Lexical error at line 3, column 27: Illegal character '@'
Compilation failed with 1 errors.
```

For `tests/syntax_error.afri` (missing `;` after `age = 22`):
```
Syntax error at line 2: Missing semicolon after statement
Compilation failed with 1 errors.
```

Run all six `.afri` files, capture the terminal output (or screenshot it),
and drop them in `screenshots/`.

## 5. Trying the reference implementation now (no JDK/JFlex/CUP needed later)

If you want to sanity-check the grammar logic before setting up JFlex/CUP,
compile and run the plain-Java reference version:
```
cd reference
javac *.java
java RefMain ../tests/valid_test1.afri
java RefMain ../tests/lexical_error.afri
java RefMain ../tests/syntax_error.afri
java RefMain ../tests/multiple_errors.afri
```
This uses the identical token/grammar rules as `AfriLang.flex`/`AfriLang.cup`,
just written by hand instead of generated — it will not appear in your CUP-
generated `parser.java`, so don't submit `reference/` as your JFlex/CUP work.
