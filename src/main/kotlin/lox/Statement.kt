package lox

abstract class Statement {
    abstract fun accept(visitor: Visitor<Unit>)

    open class Expression(
        val expression: lox.Expression
    ) : Statement() {
        override fun accept(visitor: Visitor<Unit>) = visitor.visit(this)
    }

    open class Print(
        val expression: lox.Expression
    ) : Statement() {
        override fun accept(visitor: Visitor<Unit>) = visitor.visit(this)
    }

    open class Variable(
        val name: Token,
        val initializer: lox.Expression?
    ) : Statement() {
        override fun accept(visitor: Visitor<Unit>) = visitor.visit(this)
    }

    open class Block(
        val statements: List<Statement>
    ) : Statement() {
        override fun accept(visitor: Visitor<Unit>) = visitor.visit(this)
    }

    interface Visitor<R> {
        fun visit(expressionStatement: Expression)
        fun visit(printStatement: Print)
        fun visit(variableStatement: Variable)
        fun visit(blockStatement: Block)
    }
}
