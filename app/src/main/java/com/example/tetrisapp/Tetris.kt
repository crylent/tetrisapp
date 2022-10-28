package com.example.tetrisapp

import java.util.Timer
import kotlin.concurrent.timer

const val SIZE_X = 12
const val SIZE_Y = 18

const val DELAY: Long = 500

class Tetris {
    var matrix = createMatrix()
        private set

    lateinit var currBlock: Block
        private set

    private lateinit var gameTimer: Timer

    var points = 0
        private set

    private fun createMatrix() = List(SIZE_Y) { MutableList(SIZE_X) { 0 } }

    fun start() {
        matrix = createMatrix()
        points = 0
        createBlock()
        gameTimer = timer(period = DELAY) {
            if(!currBlock.fall()) {
                createBlock()
            }
            callOnMove()
        }
    }

    private fun createBlock() {
        currBlock = Block(this)
        earnPoints(currBlock.points)
        callOnMove()
    }

    fun moveLeft() { currBlock.moveLeft(); callOnMove() }
    fun moveRight() { currBlock.moveRight(); callOnMove() }
    fun turnRight() { currBlock.turnCkw(); callOnMove() }
    fun turnLeft() { currBlock.turnAcw(); callOnMove() }

    private val listeners = mutableListOf<TetrisListener>()

    fun addListener(listener: TetrisListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: TetrisListener) {
        listeners.remove(listener)
    }

    private fun checkRows() {
        matrix.forEach { row ->
            if (row.all { it != 0 }) {
                matrix.apply {
                    earnPoints(SIZE_X)
                    for (y in matrix.indexOf(row) - 1 downTo 1) { // remove row
                        for (x in 0 until SIZE_X) {
                            matrix[y + 1][x] = matrix[y][x]
                        }
                    }
                }
            }
        }
    }

    private fun earnPoints(p: Int) {
        points += p
        listeners.forEach {
            it.onPointsEarned()
        }
    }

    private fun callOnMove() {
        checkRows()
        listeners.forEach {
            it.onMove()
        }
    }

    fun callOnFinish() {
        gameTimer.cancel()
        listeners.forEach {
            it.onFinish()
        }
    }
}