package lox.expressions

class GroupingExpression(
    val expression: Expression
) : Expression() {
    override fun <R> accept(visitor: Visitor<R>) = visitor.visit(this)
}
