package lox

import lox.expressions.*

class AstPrinter : Expression.Visitor<String> {
    fun print(expression: Expression) = expression.accept(this)

    override fun visit(literalExpression: LiteralExpression): String {
        return (literalExpression.value ?: "nil").toString()
    }

    override fun visit(prefixExpression: PrefixExpression): String {
        return parenthesize(prefixExpression.operator.lexeme, prefixExpression.right)
    }

    override fun visit(binaryExpression: BinaryExpression): String {
        with (binaryExpression) {
            return parenthesize(operator.lexeme, left, right)
        }
    }

    override fun visit(groupingExpression: GroupingExpression): String {
        return parenthesize("group", groupingExpression.expression)
    }

    override fun visit(variableExpression: VariableExpression): String {
        return "(var ${variableExpression.name.lexeme})"
    }

    override fun visit(assignmentExpression: AssignmentExpression): String {
        return "(assign ${assignmentExpression.name.lexeme} ${assignmentExpression.value.accept(this)})"
    }

    override fun visit(shortingExpression: ShortingExpression): String {
        with (shortingExpression) {
            return parenthesize(operator.lexeme, left, right)
        }
    }

    override fun visit(callExpression: CallExpression): String {
        return parenthesize(
            "call ${callExpression.callee.accept(this)} with",
            *callExpression.arguments.toTypedArray()
        )
    }


    private fun parenthesize(name: String, vararg expressions: Expression): String {
        val parts = expressions.joinToString(" ") {expr -> expr.accept(this)}
        return "($name $parts)"
    }
}
