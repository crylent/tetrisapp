package com.example.tetrisapp

interface TetrisListener {
    fun onMove()
    fun onPointsEarned()
    fun onFinish()
}