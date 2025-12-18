# JLox (WIP)

JLox is a Java implementation of Lox, a language from the book
[Crafting Interpreters](https://craftinginterpreters.com). This repository
documents my implementation of Lox as I go through the book. The implementation
uses a tree-walking interpreter over the AST of the source code.

## Chapters

1. Scanning :white_check_mark:
2. Representing Code :white_check_mark:
3. Parsing Expressions :white_check_mark:
4. Evaluating Expressions :white_check_mark:
5. Statements and State :white_check_mark:
6. Control Flow :white_check_mark:
7. Functions :white_check_mark:
8. Resolving and Binding
9. Classes
10. Inheritance

## Additional implementations

I have deviated from the book by adding these features to my implementation of
lox:

### General

- Support for logging.
- Mixing `System.out` and `System.err` leads to overlapping outputs in the REPL,
  made REPL only use `System.out`.
- Variable declarations work in REPL, semicolon on last statement not needed.
- Result of expression statements are printed to the REPL if it is the last
  statement.
- AstPrinter prints the AST like a file tree, instead of using S-expressions,
  which I find to be hard to read.

### Lexing

- Comment blocks using `/*` and `*/`, with support for nested comments.
- Escape characters for a small set of characters.

### Parsing

- Use a Pratt Parser instead of a recursive descent parser.
- The parse tree should not have group expressions, as parenthesis are handled
  by the Pratt Parser.

### Evaluation

- Support for C comma operator.
- Support for ternaries.
- String interpolation, by making a modal lexer and lexing a template string
  into separate tokens, then parsing the parts into one template string
  expression.

### Functions

- Several native functions, like reading files and splitting strings. All native
  functions are in
  [NativeFunction.java](./src/main/java/lox/interpreter/NativeFunction.java).
- Native functions can specify argument types.
