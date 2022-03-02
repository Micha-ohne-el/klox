package lox

abstract class Expression {
    abstract fun <R> accept(visitor: Visitor<R>): R

    open class Literal(
        val value: Any?
    ) : Expression() {
        override fun <R> accept(visitor: Visitor<R>) = visitor.visit(this)
    }

    open class Prefix(
        val operator: Token,
        val right: Expression
    ) : Expression() {
        override fun <R> accept(visitor: Visitor<R>) = visitor.visit(this)
    }

    open class Binary(
        val left: Expression,
        val operator: Token,
        val right: Expression
    ) : Expression() {
        override fun <R> accept(visitor: Visitor<R>) = visitor.visit(this)
    }

    open class Grouping(
        val expression: Expression
    ) : Expression() {
        override fun <R> accept(visitor: Visitor<R>) = visitor.visit(this)
    }

    interface Visitor<R> {
        fun visit(literalExpression: Literal): R
        fun visit(prefixExpression: Prefix): R
        fun visit(binaryExpression: Binary): R
        fun visit(groupingExpression: Grouping): R
    }
}
