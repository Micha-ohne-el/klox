# Klox
A Kotlin implementation of Lox (the tree-walking interpreter of Part II),
made by following [Crafting Interpreters](https://craftinginterpreters.com)
and just translating all the code to Kotlin.

## Differences
*   I am not using code generation.
    The reason for this is that Kotlin's syntax is concise enough to write
    by hand, as opposed to Java's syntax, which is tedious to write.
*   My Expression and Statement types are structured in a package,
    rather than being nested within their parent class.
*   Some internal names are different, for example:
    `Expr.Unary` is `PrefixExpression`,
    and `Expr.Logical` is `ShortingExpression`.
    I just prefer these names because they make more semantic sense to me.
*   Error messages are worded differently:
    Instead of “Expect ';' after statement.”,
    I use “Expecting ';' after statement.”.

Any other change is likely a mistake on my part.
