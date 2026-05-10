package com.kr.eqai

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    
    private lateinit var etPrompt: EditText
    private lateinit var btnGenerate: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var videoView: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etPrompt = findViewById(R.id.et_prompt)
        btnGenerate = findViewById(R.id.btn_generate)
        progressBar = findViewById(R.id.progress_bar)
        videoView = findViewById(R.id.video_view)

        btnGenerate.setOnClickListener {
            val prompt = etPrompt.text.toString()
            if (prompt.isNotEmpty()) {
                Toast.makeText(this, "Generate: $prompt", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.VISIBLE
                // TODO: Panggil API AI Video lu di sini
            } else {
                Toast.makeText(this, "Prompt kosong!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
