package tv.codealong.tutorials.various.yandex.atm

import java.util.*

class ATM {

    private var balance: Double = 0.0

    fun deposit(amount: Double) {
        if (amount <= 0) {
            println("Введите положительное число для пополнения")
            return
        }
        this.balance += amount

        println("Пополнение баланса на $amount. Текущий баланс: $balance")
    }

    fun getBalance(): Double {
        return balance
    }

    fun withdraw(amount: Double) {
        if (amount <= 0) {
            println("Введите положительное число для снятия")
            return
        }
        if (amount > balance) {
            println("Недостаточно средств. Текущий баланс: $balance")
            return
        }
        this.balance -= amount

        println("Выдача наличных на сумму $amount успешна. Остаток на счете: $balance")
    }
}

fun main() {
    val atm = ATM()


//консольное приложение
    val scanner = Scanner(System.`in`).useLocale(Locale.US)
    println("Добро пожаловать в банкомат")
    while (true) {
        println("\nВыберите операцию:")
        println("Введите 1 для пополнения баланса")
        println("Введите 2 для снятия наличных")
        println("Введите 3 для просмотра баланса")
        println("Введите 4 для выхода")

        when (scanner.nextInt()) {
            1 -> {
                print("Введите сумму для пополнения: ")
                val amount = scanner.nextDouble()
                atm.deposit(amount)
            }

            2 -> {
                print("Введите сумму для снятия: ")
                val amount = scanner.nextDouble()
                atm.withdraw(amount)
            }

            3 -> println("Текущий баланс: ${atm.getBalance()}")
            4 -> {
                println("Спасибо за использование банкомата!")
                break
            }

            else -> println("Неверный выбор. Пожалуйста, выберите 1, 2, 3 или 4.")
        }
    }
}