package lox.statements

open class ExpressionStatement(
    val expression: lox.expressions.Expression
) : Statement() {
    override fun accept(visitor: Visitor<Unit>) = visitor.visit(this)
}
