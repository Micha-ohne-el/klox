package lox.statements

abstract class Statement {
    abstract fun accept(visitor: Visitor<Unit>)

    interface Visitor<R> {
        fun visit(expressionStatement: ExpressionStatement)
        fun visit(printStatement: PrintStatement)
        fun visit(variableStatement: VariableStatement)
        fun visit(blockStatement: BlockStatement)
    }
}
