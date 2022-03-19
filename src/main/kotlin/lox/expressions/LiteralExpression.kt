package lox.expressions

class LiteralExpression(
    val value: Any?
) : Expression() {
    override fun <R> accept(visitor: Visitor<R>) = visitor.visit(this)
}
