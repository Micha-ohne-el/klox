package lox.statements

abstract class Statement {
    abstract fun accept(visitor: Visitor<Unit>)

    interface Visitor<R> {
        fun visit(expressionStatement: ExpressionStatement)
        fun visit(printStatement: PrintStatement)
        fun visit(variableStatement: VariableStatement)
        fun visit(blockStatement: BlockStatement)
        fun visit(ifStatement: IfStatement)
        fun visit(whileStatement: WhileStatement)
        fun visit(functionStatement: FunctionStatement)
        fun visit(returnStatement: ReturnStatement)
        fun visit(classStatement: ClassStatement)
    }
}
