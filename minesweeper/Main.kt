package minesweeper

import kotlin.random.Random

// mine: 1, safe cell: 0
class Minesweeper{
    private val height: Int
    private val width: Int
    private val map: MutableList<MutableList<Int>>
    private val mines: Int
    private val marked: MutableList<MutableList<Int>>
    private val explored: MutableSet<MutableList<Int>>
    private var endGame = false

    init {
        mines = getNumberOfMines()

        val size = getMapSize()
        height = size[0]
        width = size[1]
        map = MutableList(height) { MutableList(width) {0} }

        addMines()

        marked = MutableList(1) { mutableListOf() }
        marked.removeAt(0)

        explored = mutableSetOf(mutableListOf(0, 0))
        explored.remove(mutableListOf(0, 0))

        help()
    }

    private fun help() {
        println("Instruction: If you think a cell is safe, use \"mine\" operation to free the cell.\n" +
                "If you think a cell is mine, use \"mine\" operation to mark the cell, mark a already marked cell twice will remove the mark.\n" +
                "valid move example: \"3 4 free\"; \"3 4 mine\".\n" +
                "type \"\\help\" to get help, type \"\\exit\" to exit the game.")
    }

    private fun getMapSize(): MutableList<Int> {
        val size = MutableList(2) { 0 }
        while (true) {
            println("How big the field do you want? height width:")
            println("(enter two positive integer separated by space, too big the field will lead to slow processing)")
            try {
                val (h, w) = readLine()!!.split(" ")
                size[0] = Integer.parseInt(h)
                size[1] = Integer.parseInt(w)
                break
            } catch (e: Exception) {
                println("Invalid input, please try again")
            }
        }
        return size
    }

    private fun getNumberOfMines(): Int {
        var mines: Int
        while (true) {
            println("How many mines do you want on the field?")
            println("(enter a positive integers)")
            try {
                val num = readLine()!!
                mines = Integer.parseInt(num)
                break
            } catch (e: Exception) {
                println("Invalid input, please try again")
            }
        }
        return mines
    }

    private fun addMines() {
        var numberOfMines = mines
        while (numberOfMines > 0) {
            val num = Random.nextInt(0, height * width - 1)
            val x = num / width
            val y = num % width
            if (map[x][y] != 1) {
                map[x][y] = 1
                numberOfMines--
            }
        }
    }

    private fun searchMines(x: Int, y: Int): Int {
        return when {
            x == 0 && y == 0 -> { // left up corner
                map[0][1] + map[1][0] + map[1][1]
            }
            x == 0 && y == (width - 1) -> { // right up corner
                map[0][y - 1] + map[1][y] + map[1][y - 1]
            }
            x == (height - 1) && y == 0 -> { // left down corner
                map[x - 1][0] + map[x][1] + map[x - 1][1]
            }
            x == (height - 1) && y == (width - 1) -> { // right down corner
                map[x][y - 1] + map[x - 1][y] + map[x - 1][y - 1]
            }
            x == 0 -> { // up bounder
                map[0][y - 1] + map[0][y + 1] + map[1][y] + map[1][y - 1] + map[1][y + 1]
            }
            x == (height - 1) -> { // down bounder
                map[x][y - 1] + map[x][y + 1] + map[x - 1][y] + map[x - 1][y - 1] + map[x - 1][y + 1]
            }
            y == 0 -> { // left bounder
                map[x - 1][0] + map[x + 1][0] + map[x][1] + map[x - 1][1] + map[x + 1][1]
            }
            y == (width - 1) -> { // right bounder
                map[x - 1][y] + map[x + 1][y] + map[x][y - 1] + map[x - 1][y - 1] + map[x + 1][y - 1]
            }
            else -> { // core
                map[x - 1][y] + map[x + 1][y] + map[x][y - 1] + map[x][y + 1] + map[x - 1][y - 1] + map[x + 1][y - 1] + map[x - 1][y + 1] + map[x + 1][y + 1]
            }
        }
    }

    private fun markValidation(x: Int, y: Int): Boolean = !checkExplored(x, y)

    private fun freeValidation(x: Int, y: Int): Boolean = !checkExplored(x, y) && !checkMarked(x, y)

    private fun mark(x: Int, y: Int) {
        if (!checkMarked(x, y)) {
            marked.add(mutableListOf(x, y))
        } else {
            marked.remove(mutableListOf(x, y))
        }
    }

    private fun checkMarked(x: Int, y: Int): Boolean {
        return marked.contains(mutableListOf(x, y))
    }

    private fun explore(_x: Int, _y: Int) {
        val stack: MutableSet<MutableList<Int>> = mutableSetOf(mutableListOf(0, 0))
        stack.remove(mutableListOf(0, 0))
        stack.add(mutableListOf(_x, _y))

        while (stack.isNotEmpty()) {
            val (x, y) = stack.first()
            stack.remove(stack.first())

            if (!checkExplored(x, y)) explored.add(mutableListOf(x, y))

            if (searchMines(x, y) == 0) {
                when {
                    x == 0 && y == 0 -> { // left up corner
                        if (!checkExplored(0, 1)) stack.add(mutableListOf(0, 1))
                        if (!checkExplored(1, 0)) stack.add(mutableListOf(1, 0))
                        if (!checkExplored(1, 1)) stack.add(mutableListOf(1, 1))
                    }
                    x == 0 && y == (width - 1) -> { // right up corner
                        if (!checkExplored(0, y - 1)) stack.add(mutableListOf(0, y - 1))
                        if (!checkExplored(1, y)) stack.add(mutableListOf(1, y))
                        if (!checkExplored(1, y - 1)) stack.add(mutableListOf(1, y - 1))
                    }
                    x == (height - 1) && y == 0 -> { // left down corner
                        if (!checkExplored(x - 1, 0)) stack.add(mutableListOf(x - 1, 0))
                        if (!checkExplored(x, 1)) stack.add(mutableListOf(x, 1))
                        if (!checkExplored(x - 1, 1)) stack.add(mutableListOf(x - 1, 1))
                    }
                    x == (height - 1) && y == (width - 1) -> { // right down corner
                        if (!checkExplored(x, y - 1)) stack.add(mutableListOf(x, y - 1))
                        if (!checkExplored(x - 1, y)) stack.add(mutableListOf(x - 1, y))
                        if (!checkExplored(x - 1, y - 1)) stack.add(mutableListOf(x - 1, y - 1))
                    }
                    x == 0 -> { // up bounder
                        if (!checkExplored(0, y - 1)) stack.add(mutableListOf(0, y - 1))
                        if (!checkExplored(0, y + 1)) stack.add(mutableListOf(0, y + 1))
                        if (!checkExplored(1, y)) stack.add(mutableListOf(1, y))
                        if (!checkExplored(1, y - 1)) stack.add(mutableListOf(1, y - 1))
                        if (!checkExplored(1, y + 1)) stack.add(mutableListOf(1, y + 1))
                    }
                    x == (height - 1) -> { // down bounder
                        if (!checkExplored(x, y - 1)) stack.add(mutableListOf(x, y - 1))
                        if (!checkExplored(x, y + 1)) stack.add(mutableListOf(x, y + 1))
                        if (!checkExplored(x - 1, y)) stack.add(mutableListOf(x - 1, y))
                        if (!checkExplored(x - 1, y - 1)) stack.add(mutableListOf(x - 1, y - 1))
                        if (!checkExplored(x - 1, y + 1)) stack.add(mutableListOf(x - 1, y + 1))
                    }
                    y == 0 -> { // left bounder
                        if (!checkExplored(x - 1, 0)) stack.add(mutableListOf(x - 1, 0))
                        if (!checkExplored(x + 1, 0)) stack.add(mutableListOf(x + 1, 0))
                        if (!checkExplored(x, 1)) stack.add(mutableListOf(x, 1))
                        if (!checkExplored(x - 1, 1)) stack.add(mutableListOf(x - 1, 1))
                        if (!checkExplored(x + 1, 1)) stack.add(mutableListOf(x + 1, 1))
                    }
                    y == (width - 1) -> { // right bounder
                        if (!checkExplored(x - 1, y)) stack.add(mutableListOf(x - 1, y))
                        if (!checkExplored(x + 1, y)) stack.add(mutableListOf(x + 1, y))
                        if (!checkExplored(x, y - 1)) stack.add(mutableListOf(x, y - 1))
                        if (!checkExplored(x - 1, y - 1)) stack.add(mutableListOf(x - 1, y - 1))
                        if (!checkExplored(x + 1, y - 1)) stack.add(mutableListOf(x + 1, y - 1))
                    }
                    else -> { // core
                        if (!checkExplored(x - 1, y)) stack.add(mutableListOf(x - 1, y))
                        if (!checkExplored(x + 1, y)) stack.add(mutableListOf(x + 1, y))
                        if (!checkExplored(x, y - 1)) stack.add(mutableListOf(x, y - 1))
                        if (!checkExplored(x, y + 1)) stack.add(mutableListOf(x, y + 1))
                        if (!checkExplored(x - 1, y - 1)) stack.add(mutableListOf(x - 1, y - 1))
                        if (!checkExplored(x + 1, y - 1)) stack.add(mutableListOf(x + 1, y - 1))
                        if (!checkExplored(x - 1, y + 1)) stack.add(mutableListOf(x - 1, y + 1))
                        if (!checkExplored(x + 1, y + 1)) stack.add(mutableListOf(x + 1, y + 1))
                    }
                }
            }
        }
    }

    private fun checkExplored(x: Int, y: Int): Boolean {
        return explored.contains(mutableListOf(x, y))
    }

    private fun printMap() {
        // top bounder
        for (i in 1..width + 3) {
            print(when (i) {
                1 -> " "
                2, width + 3 -> "|"
                else -> i - 2
            })
        }
        println()
        for (i in 1..width + 3) {
            print(when (i) {
                2, width + 3 -> "|"
                else -> "-"
            })
        }
        println()
        // core
        for (i in 0..map.lastIndex) {
            print("${i + 1}|")
            for (j in 0..map[i].lastIndex) {
                if (!endGame) {
                    print(
                        when {
                            searchMines(i, j) != 0 && checkExplored(i, j) -> searchMines(i, j)
                            checkExplored(i, j) -> "/"
                            checkMarked(i, j) -> "*"
                            else -> "."
                        }
                    )
                } else {
                    print(
                        when {
                            map[i][j] == 1 -> "X"
                            searchMines(i, j) != 0 && checkExplored(i, j) -> searchMines(i, j)
                            checkExplored(i, j) -> "/"
                            checkMarked(i, j) -> "*"
                            else -> "."
                        }
                    )
                }
            }
            print("|\n")
        }
        // bottom bounder
        for (i in 1..width + 3) {
            print(when (i) {
                2, width + 3 -> "|"
                else -> "-"
            })
        }
        println()
    }

    private fun endGame() {
        var minesToBeCheck = mines

        if (minesToBeCheck != marked.size){
            endGame = false
        } else {
            for (i in 0..map.lastIndex) {
                for (j in 0..map[i].lastIndex) {
                    if (map[i][j] == 1 && checkMarked(i, j)) minesToBeCheck--
                }
            }
            if (minesToBeCheck == 0) {
                endGame = true
                println("Congratulations! You found all the mines!")
            }
        }
    }

    private fun playerMove() {
        while (true) {
            println("Set/unset mines marks or claim a cell as free:")
            try {
                val input = readLine()!!
                if (input == "\\help") {
                    help()
                    break
                } else if (input == "\\exit") {
                    endGame = true
                    break
                }
                val (_y, _x, operation) = input.split(" ")
                val y = _y.toInt() - 1
                val x = _x.toInt() - 1

                when (operation) {
                    "free" -> if (freeValidation(x, y) && map[x][y] != 1) {
                        explore(x, y)
                        printMap()
                        break
                    } else {
                        printMap()
                        println("You stepped on a mine and failed!")
                        endGame = true
                        break
                    }
                    "mine" -> if (markValidation(x, y)) {
                        mark(x, y)
                        printMap()
                        break
                    }
                    else -> println("Invalid operation")
                }
            } catch (e: Exception) {
                println("Invalid operation")
            }
        }
    }

    fun gameStart() {
        printMap()
        while (!endGame) {
            endGame()
            playerMove()
        }
    }

}

fun main() {
    val minesweeper = Minesweeper()
    minesweeper.gameStart()
}