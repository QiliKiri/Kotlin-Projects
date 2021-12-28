// This calculator only supports integer additon, subtraction, multiplication and division
package calculator

import java.math.BigInteger

class Calculator {
    private val variables = HashMap<String, BigInteger>()

    private fun interpretOperator(operator: String): String {
        return if (operator.contains("[-+]".toRegex())) {
            var minusSigns = 0
            for (char in operator.toCharArray()) {
                if (char == '-') minusSigns++
            }

            if (minusSigns % 2 == 0) "+" else "-"
        } else {
            operator
        }
    }

    private fun twoNumberCalculate(operand1: BigInteger, operator: String, operand2: BigInteger): BigInteger {
        return when (operator) {
            "+" -> operand1 + operand2
            "-" -> operand1 - operand2
            "*" -> operand1 * operand2
            "/" -> operand1.divide(operand2)
            else -> throw IllegalArgumentException("No such operator!")
        }
    }

    private fun checkNumber(str: String): Boolean {
        return try {
            str.toBigInteger()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun replaceVariableToValue(inputList: MutableList<String>): MutableList<String> {
        val updated = inputList.toMutableList()

        for (i in 0..inputList.lastIndex) {
            val item = inputList[i]
            // if it's not variable, skip it
            if (!item.contains("[a-zA-Z]+".toRegex())) continue

            if (variables.contains(item)) {
                updated[i] = variables[item].toString()
            } else {
                throw IllegalArgumentException()
            }
        }

        return updated
    }

    private fun operatorPrecedence(operator1: String, operator2: String): String {
        return if (operator2 == "+" || operator2 == "-") {
            if (operator1 == "+" || operator1 == "-") "Equal" else "Lower"
        } else {
            if (operator1 == "+" || operator1 == "-") "Higher" else "Equal"
        }
    }

    private fun infixToPostfix(input: String): MutableList<String> {
        var infixStack = input
            .replace("(", "( ")
            .replace(")", " )")
            .split(" ")
            .filter { it.isNotBlank() }
            .toMutableList()
        try {
            infixStack = replaceVariableToValue(infixStack)
        } catch (e: IllegalArgumentException) {
            //println("caused by replaceVariableToValue")
            throw IllegalArgumentException()
        }

        val operatorStack = mutableListOf<String>()
        val postfixStack = mutableListOf<String>()

        for (item in infixStack) {
            when {
                checkNumber(item) -> postfixStack.add(item)

                !item.matches("[*/]|[+-]+|[()]".toRegex()) -> {
                    //println("caused by infixToPostfix, $item should not appear")
                    throw IllegalArgumentException()
                }

                (operatorStack.isEmpty() || item == "(" || operatorStack[operatorStack.lastIndex] == "(") -> operatorStack.add(item)

                item == ")" -> {
                    while (true) {
                       if (operatorStack.isEmpty()) {
                            throw IllegalArgumentException()
                        } else if (operatorStack[operatorStack.lastIndex] == "(") {
                           operatorStack.removeAt(operatorStack.lastIndex)
                           break
                       } else {
                            val operator = operatorStack[operatorStack.lastIndex]
                            operatorStack.removeAt(operatorStack.lastIndex)
                            postfixStack.add(operator)
                        }
                    }
                }

                operatorPrecedence(operatorStack[operatorStack.lastIndex], item) == "Higher" -> operatorStack.add(item)

                operatorPrecedence(operatorStack[operatorStack.lastIndex], item) != "Higher" -> {
                    while (true) {
                        if (operatorStack.isEmpty()) {
                            break
                        } else if (operatorStack[operatorStack.lastIndex] == "(") {
                            break
                        } else if (operatorPrecedence(operatorStack[operatorStack.lastIndex], item) == "Higher") {
                            break
                        } else {
                            val operator = operatorStack[operatorStack.lastIndex]
                            postfixStack.add(operator)
                            operatorStack.removeAt(operatorStack.lastIndex)
                        }
                    }
                    operatorStack.add(item)

                }

                else -> break
            }

        }

        if (operatorStack.contains("(") || operatorStack.contains(")")) {
            //println("caused by infixToPostfix, $operatorStack is not correct")
            throw IllegalArgumentException()
        }

        if (operatorStack.isNotEmpty()) {
            operatorStack.reverse()
            postfixStack.addAll(operatorStack)
            operatorStack.clear()
        }

        return postfixStack
    }

    private fun calculate(input: String) {
        val postfix: MutableList<String>
        try {
            postfix = infixToPostfix(input)
        } catch (e: IllegalArgumentException) {
            //println("caused by infixToPostfix")
            println("Invalid expression")
            return
        }

        val stack = mutableListOf<BigInteger>()

        for (item in postfix) {
            try {
                if (checkNumber(item)) {
                    stack.add(item.toBigInteger())
                } else if (item.contains("[*/+-]".toRegex())) {
                    val operand1 = stack[stack.lastIndex]
                    stack.removeAt(stack.lastIndex)

                    val operand2 = stack[stack.lastIndex]
                    stack.removeAt(stack.lastIndex)

                    val operator = interpretOperator(item)

                    val result = twoNumberCalculate(operand2, operator, operand1)

                    stack.add(result)
                } else {
                    if (variables[item] != null) {
                        val value = variables[item]!!
                        stack.add(value)
                    }
                }
            } catch (e: Exception) {
                //println("caused by calculate ${e.message}")
                println("Invalid expression")
                return
            }
        }

        println(stack[0])
    }

    private fun variableAssignment(input: String) {
        if (input.contains("=.*=".toRegex()) || input.contains("(?<==).*\\d+[a-zA-Z]+".toRegex()) || input.contains("(?<==).*[a-zA-Z]+\\d+".toRegex())) {
            println("Invalid assignment")
            return
        }

        if (input.contains("\\d+.*(?==)".toRegex()) || input.contains("^[^a-zA-Z]+".toRegex())) {
            println("Invalid identifier")
            return
        }

        // assign variable to variable
        if (input.contains(("[a-zA-Z]+.*=.*[a-zA-Z]+".toRegex()))) {
            val variable1: String
            val variable2: String

            var regex = Regex("[a-zA-Z]+.*(?==)")
            val v1 = regex.find(input)

            regex = Regex("(?<==).*[a-zA-Z]")
            val v2 = regex.find(input)

            if (v1 != null && v2 != null) {
                variable1 = v1.value.trim()
                variable2 = v2.value.trim()
                try {
                    variables[variable1] = variables[variable2]!!
                } catch (e: Exception) {
                    println("Unknown variable")
                    return
                }
            }
        }

        // assign integer to variable
        if (input.contains("[a-zA-Z]+\\s*=\\s*-?\\d+".toRegex())) {
            val variable: String
            val value: BigInteger

            var regex = Regex("[a-zA-Z]+.*(?==)")
            val v= regex.find(input)

            regex = Regex("(?<==).*-?\\d+")
            val num = regex.find(input)

            if (v != null && num != null) {
                variable = v.value.trim()
                value = num.value.trim().toBigInteger()
                variables[variable] = value
                //println("assign ${variables[variable]} to $variable")
            }
        }
    }

    fun start() {
        while (true) {
            val input = readLine()!!.trim()
            when {
                input == "/exit" -> {
                    println("Bye!")
                    break
                }

                input == "/help" -> {
                    println("program supports multiplication *, integer division / and parentheses (...). " +
                            "Variables and unary minus or plus operator are also supported.")
                }

                input.isBlank() -> continue

                input.startsWith("/") -> println("Unknown command")

                input.contains("=") -> variableAssignment(input)

                !input.contains("[+*/-=]".toRegex()) -> {
                    if (variables.contains(input)) {
                        println(variables[input])
                    } else {
                        println("Unknown variable")
                    }
                }

                input.contains("[a-zA-Z]+".toRegex()) && !input.contains("[+*/-]+".toRegex())-> {
                    // enter one variable
                    if (variables.contains(input)) {
                        val value: BigInteger? = variables[input]
                        println(value)
                    } else {
                        println("Unknown variable")
                    }
                }

                input.contains("[+*/-]+".toRegex()) -> {
                    //println("enter calculation")
                    calculate(input)
                    //println("finish calculation")
                }

                input.matches("-?\\d+".toRegex()) -> {
                    //println("enter one number")
                    try {
                        println(input.toBigInteger())
                    } catch (e: Exception) {
                        println("Invalid expression")
                    }

                }

                else -> continue
            }
        }
    }
}

fun main() {
    val calculator = Calculator()
    calculator.start()
}
