package tv.codealong.tutorials.various.sealedclass.sealedclassesexamples

//4. Game Development Example
//
// ✅ Игровая логика
sealed class GameEntity(
    val health: Int,
    val position: Position
) {
    abstract val speed: Int

    // Общий метод для всех сущностей
    fun move(direction: Direction) {
        // Логика движения...
        println("$this движется $direction со скоростью $speed")
    }

    data class Player(
        val name: String,
        val inventory: List<Item>,
        override val speed: Int = 5
    ) : GameEntity(health = 100, position = Position(0, 0))

    data class Enemy(
        val type: EnemyType,
        val damage: Int,
        override val speed: Int = 3
    ) : GameEntity(health = 50, position = Position(10, 10))

    data class NPC(
        val dialog: String,
        val quest: Quest?,
        override val speed: Int = 2
    ) : GameEntity(health = 25, position = Position(5, 5))

    // Метод для обработки столкновений
    fun handleCollision(other: GameEntity) {
        when (this) {
            is Player -> when (other) {
                is Enemy -> takeDamage(other.damage)
                is NPC -> startDialog(other.dialog)
                is Player -> TODO()
            }
            is Enemy -> when (other) {
                is Player -> attack(other)
                else -> {/* ничего */}
            }
            is NPC -> {/* NPC не реагирует */}
        }
    }

    private fun attack(other: Player) {
        TODO("Not yet implemented")
    }

    private fun takeDamage(damage: Int) {
        TODO("Not yet implemented")
    }

    private fun startDialog(dialog: String) {
        TODO("Not yet implemented")
    }
}

class Quest {

}

enum class EnemyType {

}

class Item {

}

class Direction {

}

class Position(val x: Int, val y: Int)
