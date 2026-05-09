package com.kr.eqai

import android.Manifest
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    private val seekBars = mutableListOf<SeekBar>()
    private val freqLabels = arrayOf("31Hz", "62Hz", "125Hz", "250Hz", "500Hz", "1kHz", "2kHz", "4kHz", "8kHz", "16kHz")
    private lateinit var aiStatus: TextView
    private lateinit var labelBass: TextView
    private lateinit var labelVirtual: TextView

    private val aiPresets = mapOf(
        "EDM" to shortArrayOf(600, 400, 200, 0, 0, 200, 400, 500, 400, 300),
        "Rock" to shortArrayOf(400, 300, 0, -100, -200, 200, 400, 500, 400, 300),
        "Jazz" to shortArrayOf(0, 0, 0, 100, 200, 200, 100, 0, 0, 0),
        "Pop" to shortArrayOf(-100, 0, 200, 300, 200, 0, -100, -100, 0, 100),
        "Vocal" to shortArrayOf(-200, -100, 0, 200, 400, 400, 200, 0, -100, -200),
        "Bass" to shortArrayOf(800, 600, 400, 200, 0, -100, -200, -200, -200, -200)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        aiStatus = findViewById(R.id.ai_status)
        labelBass = findViewById(R.id.label_bass)
        labelVirtual = findViewById(R.id.label_virtual)

        requestPermission()
        setupAudioEffects()
        setupManualBassControls()
        setupAIButton()
        setupPresets()
    }

    private fun setupAudioEffects() {
        try {
            equalizer = Equalizer(0, 0).apply { enabled = true }
            bassBoost = BassBoost(0, 0).apply { enabled = true }
            virtualizer = Virtualizer(0, 0).apply { enabled = true }

            val eqLayout = findViewById<LinearLayout>(R.id.eq_layout)
            val minLevel = equalizer!!.bandLevelRange[0]
            val maxLevel = equalizer!!.bandLevelRange[1]

            for (i in 0 until equalizer!!.numberOfBands.coerceAtMost(10)) {
                val bandLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    gravity = android.view.Gravity.CENTER
                }

                val seekBar = SeekBar(this).apply {
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 350)
                    rotation = -90f
                    max = maxLevel - minLevel
                    progress = (equalizer!!.getBandLevel(i.toShort()) - minLevel)
                    progressTintList = android.content.res.ColorStateList.valueOf(0xFF42A5F5.toInt())
                }

                val label = TextView(this).apply {
                    text = freqLabels.getOrNull(i)?: "Band$i"
                    setTextColor(0xFFFFFFFF.toInt())
                    textSize = 9f
                    gravity = android.view.Gravity.CENTER
                }

                seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) equalizer?.setBandLevel(i.toShort(), (progress + minLevel).toShort())
                    }
                    override fun onStartTrackingTouch(sb: SeekBar?) {}
                    override fun onStopTrackingTouch(sb: SeekBar?) {}
                })

                bandLayout.addView(seekBar)
                bandLayout.addView(label)
                eqLayout.addView(bandLayout)
                seekBars.add(seekBar)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "AudioEffect Error: ${e.message}", Toast.LENGTH_LONG).show()
        }

        findViewById<SwitchMaterial>(R.id.switch_eq).setOnCheckedChangeListener { _, isChecked ->
            equalizer?.enabled = isChecked
            bassBoost?.enabled = isChecked
            virtualizer?.enabled = isChecked
        }
    }

    private fun setupManualBassControls() {
        // Bass Boost: 0-1000 = 0-100%
        findViewById<SeekBar>(R.id.seek_bass).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                bassBoost?.setStrength(progress.toShort())
                labelBass.text = "Bass Boost: ${progress/10}%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Virtualizer: 0-1000 = 0-100%
        findViewById<SeekBar>(R.id.seek_virtual).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                virtualizer?.setStrength(progress.toShort())
                labelVirtual.text = "3D Surround: ${progress/10}%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupAIButton() {
        findViewById<MaterialButton>(R.id.btn_ai).setOnClickListener {
            runAIAnalysis()
        }
    }

    private fun runAIAnalysis() {
        aiStatus.text = "AI Status: Analyzing audio..."
        aiStatus.postDelayed({
            val genres = aiPresets.keys.toList()
            val detectedGenre = genres[Random.nextInt(genres.size)]
            aiStatus.text = "AI Status: Detected $detectedGenre"
            Toast.makeText(this, "AI: $detectedGenre EQ Applied!", Toast.LENGTH_SHORT).show()
            aiPresets[detectedGenre]?.let { applyPreset(it) }
            aiStatus.postDelayed({ aiStatus.text = "AI Status: Standby" }, 2000)
        }, 2500)
    }

    private fun setupPresets() {
        findViewById<MaterialButton>(R.id.btn_flat).setOnClickListener { applyPreset(shortArrayOf(0,0,0,0)) }
        findViewById<MaterialButton>(R.id.btn_bass).setOnClickListener { applyPreset(aiPresets["Bass"]!!) }
        findViewById<MaterialButton>(R.id.btn_vocal).setOnClickListener { applyPreset(aiPresets["Vocal"]!!) }
        findViewById<MaterialButton>(R.id.btn_rock).setOnClickListener { applyPreset(aiPresets["Rock"]!!) }
    }

    private fun applyPreset(levels: ShortArray) {
        val minLevel = equalizer?.bandLevelRange?.get(0)?: 0
        levels.forEachIndexed { i, level ->
            if (i < seekBars.size) {
                equalizer?.setBandLevel(i.toShort(), level)
                seekBars[i].progress = level - minLevel
            }
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS), 123)
    }

    override fun onDestroy() {
        equalizer?.release()
        bassBoost?.release()
        virtualizer?.release()
        super.onDestroy()
    }
}
