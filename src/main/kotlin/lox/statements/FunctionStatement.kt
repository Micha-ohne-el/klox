package lox.statements

import lox.Token

class FunctionStatement(
    val name: Token,
    val params: List<Token>,
    val body: List<Statement>
) : Statement() {
    override fun accept(visitor: Visitor<Unit>) = visitor.visit(this)
}
