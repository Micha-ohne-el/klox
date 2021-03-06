package lox.expressions

abstract class Expression {
    abstract fun <R> accept(visitor: Visitor<R>): R

    interface Visitor<R> {
        fun visit(literalExpression: LiteralExpression): R
        fun visit(prefixExpression: PrefixExpression): R
        fun visit(binaryExpression: BinaryExpression): R
        fun visit(groupingExpression: GroupingExpression): R
        fun visit(variableExpression: VariableExpression): R
        fun visit(assignmentExpression: AssignmentExpression): R
        fun visit(shortingExpression: ShortingExpression): R
        fun visit(callExpression: CallExpression): R
        fun visit(getExpression: GetExpression): R
        fun visit(setExpression: SetExpression): R
        fun visit(thisExpression: ThisExpression): R
        fun visit(superExpression: SuperExpression): R
    }
}
