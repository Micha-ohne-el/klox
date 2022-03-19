package lox.statements

open class PrintStatement(
    val expression: lox.expressions.Expression
) : Statement() {
    override fun accept(visitor: Visitor<Unit>) = visitor.visit(this)
}
