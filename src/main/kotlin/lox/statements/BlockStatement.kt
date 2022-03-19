package lox.statements

open class BlockStatement(
    val statements: List<Statement>
) : Statement() {
    override fun accept(visitor: Visitor<Unit>) = visitor.visit(this)
}
