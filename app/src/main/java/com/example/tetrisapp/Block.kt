package com.example.tetrisapp

import android.graphics.Point
import android.util.Log
import androidx.core.graphics.plus
import java.util.Random

class Block(private val tetris: Tetris) {
    private val complexityChances = listOf(0.1, 0.2, 0.4, 0.2, 0.1)
    private val chancesCum
        get() = List(complexityChances.size) { complexityChances.subList(0, it).sum() }

    private val position = Point(0, 0)
    private var cells = mutableListOf<Point>().apply {
        add(Point(0, 0))
        var (right, left, down) = listOf(0, 0, 0)
        repeat(randComplexity()) {
            val nextPossible = listOf(
                last() + Point(1, 0),
                last() + Point(-1, 0),
                last() + Point(0, 1),
                last() + Point(0, -1)
            ).filter { nextPosition ->
                !any { it.x == nextPosition.x && it.y == nextPosition.y }
            }
            val next = nextPossible[Random().nextInt(nextPossible.size)]
            add(next)
            if (next.x > right) right = next.x
            else if (next.x < left) left = next.x
            else if (next.y > down) down = next.y
        }
        position.x = Random().nextInt(SIZE_X - right + left) - left
        position.y = -down
    }.toList()

    val points = cells.size

    private val colors = listOf(
        R.drawable.cell1,
        R.drawable.cell2,
        R.drawable.cell3,
        R.drawable.cell4,
        R.drawable.cell5,
        R.drawable.cell6,
    )
    val color = colors.random()

    val calculated
        get() = cells.map { Point(position.x + it.x, position.y + it.y) }

    fun moveRight() { move(1) }
    fun moveLeft() { move(-1) }

    private fun move(shift: Int) {
        if(canBePlaced(newPosition = Point(position.x + shift, position.y))) {
            position.x += shift
        }
    }

    fun fall() = canBePlaced(newPosition = Point(position.x, position.y + 1)).also { condition ->
        if (condition) position.y += 1
        else {
            calculated.forEach {
                if (it.y < 0) {
                    tetris.callOnFinish()
                    return@also
                }
                else tetris.matrix.apply {
                    this[it.y][it.x] = color
                }
            }
        }
    }

    fun turnCkw() {
        turn(-1, 1)
    }

    fun turnAcw() {
        turn(1, -1)
    }

    private fun turn(m1: Int, m2: Int) {
        val newState = cells.map { Point(it.y * m1, it.x * m2) }
        if (canBePlaced(newState)) cells = newState
    }

    private fun canBePlaced(newState: List<Point> = cells, newPosition: Point = position): Boolean {
        newState.map { Point(newPosition.x + it.x, newPosition.y + it.y) }.forEach {
            if (it.y < 0) return@forEach
            if (it.x < 0 || it.x >= SIZE_X || it.y >= SIZE_Y || tetris.matrix[it.y][it.x] != 0)
                return false
        }
        return true
    }

    private fun randComplexity(): Int {
        val rand = Random().nextDouble()
        for (complexity in chancesCum.indices) {
            if (rand < chancesCum[complexity]) return complexity
        }
        return 0
    }
}