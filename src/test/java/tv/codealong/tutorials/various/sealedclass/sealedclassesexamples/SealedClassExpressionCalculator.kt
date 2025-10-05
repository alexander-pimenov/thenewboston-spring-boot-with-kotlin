package tv.codealong.tutorials.various.sealedclass.sealedclassesexamples

//6. Expression Calculator
//
// ✅ Математические выражения
sealed class MathExpression {
    abstract fun evaluate(): Double
    abstract fun simplify(): MathExpression

    data class Number(val value: Double) : MathExpression() {
        override fun evaluate(): Double = value
        override fun simplify(): MathExpression = this
    }

    data class Add(val left: MathExpression, val right: MathExpression) : MathExpression() {
        override fun evaluate(): Double = left.evaluate() + right.evaluate()
        override fun simplify(): MathExpression = when {
            left is Number && left.value == 0.0 -> right.simplify()
            right is Number && right.value == 0.0 -> left.simplify()
            else -> Add(left.simplify(), right.simplify())
        }
    }

    data class Multiply(val left: MathExpression, val right: MathExpression) : MathExpression() {
        override fun evaluate(): Double = left.evaluate() * right.evaluate()
        override fun simplify(): MathExpression = when {
            left is Number && left.value == 1.0 -> right.simplify()
            right is Number && right.value == 1.0 -> left.simplify()
            left is Number && left.value == 0.0 -> Number(0.0)
            right is Number && right.value == 0.0 -> Number(0.0)
            else -> Multiply(left.simplify(), right.simplify())
        }
    }

    // Общий метод для всех выражений
    fun prettyPrint(): String = when (this) {
        is Number -> value.toString()
        is Add -> "(${left.prettyPrint()} + ${right.prettyPrint()})"
        is Multiply -> "(${left.prettyPrint()} * ${right.prettyPrint()})"
    }
}

// Использование
fun main() {
    val expression = MathExpression.Add(
        MathExpression.Number(5.0),
        MathExpression.Multiply(
            MathExpression.Number(2.0),
            MathExpression.Number(3.0)
        )
    )

    println("Выражение: ${expression.prettyPrint()}") // (5.0 + (2.0 * 3.0))
    println("Результат: ${expression.evaluate()}")    // 11.0
    println("Упрощённое: ${expression.simplify().prettyPrint()}") // (5.0 + 6.0)
}