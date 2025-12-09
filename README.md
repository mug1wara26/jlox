# JLox (WIP)

JLox is a Java implementation of Lox, a language from the book
[Crafting Interpreters](https://craftinginterpreters.com). This repository
documents my implementation of Lox as I go through the book. The implementation
uses a tree-walking interpreter over the AST of the source code.

## Chapters

1. Scanning :white_check_mark:
2. Representing Code :white_check_mark:
3. Parsing Expressions
4. Evaluating Expressions
5. Statements and State
6. Control Flow
7. Functions
8. Resolving and Binding
9. Classes
10. Inheritance

## Additional implementations

I have deviated from the book by adding these features to my implementation of
lox:

- Comment blocks using `/*` and `*/`, with support for nested comments.
- String interpolation, by treating curly braces in strings as syntactic sugar
  for string concatenation.
- Escape characters for a small set of characters.
- While not an actual feature of the language, my AstPrinter prints the AST like
  a file tree, instead of using S-expressions, which I find to be hard to read.
- Use a Pratt Parser instead of a recursive descent parser.
