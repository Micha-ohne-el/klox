package lox

class AstPrinter : Expression.Visitor<String> {
    fun print(expression: Expression) = expression.accept(this)

    override fun visit(literalExpression: Expression.Literal): String {
        return (literalExpression.value ?: "nil").toString()
    }

    override fun visit(prefixExpression: Expression.Prefix): String {
        return parenthesize(prefixExpression.operator.lexeme, prefixExpression.right)
    }

    override fun visit(binaryExpression: Expression.Binary): String {
        with (binaryExpression) {
            return parenthesize(operator.lexeme, left, right)
        }
    }

    override fun visit(groupingExpression: Expression.Grouping): String {
        return parenthesize("group", groupingExpression.expression)
    }


    private fun parenthesize(name: String, vararg expressions: Expression): String {
        val parts = expressions.joinToString(" ") {expr -> expr.accept(this)}
        return "($name $parts)"
    }
}
