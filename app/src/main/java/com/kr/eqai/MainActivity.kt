package com.kr.eqai

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var etPrompt: EditText
    private lateinit var btnGenerate: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var videoView: VideoView

    private val HF_SPACE_URL = "https://username-ai-video-server.hf.space/run/predict"
    private val client = OkHttpClient.Builder()
       .readTimeout(300, java.util.concurrent.TimeUnit.SECONDS)
       .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etPrompt = findViewById(R.id.et_prompt)
        btnGenerate = findViewById(R.id.btn_generate)
        progressBar = findViewById(R.id.progress_bar)
        videoView = findViewById(R.id.video_view)

        btnGenerate.setOnClickListener {
            val prompt = etPrompt.text.toString().trim()
            if (prompt.isEmpty()) {
                toast("Prompt kosong prof!")
                return@setOnClickListener
            }
            generateVideo(prompt)
        }
    }

    private fun generateVideo(prompt: String) {
        setLoading(true)
        val json = JsonObject().apply {
            add("data", Gson().toJsonTree(listOf(prompt)))
        }
        val body = Gson().toJson(json).toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder().url(HF_SPACE_URL).post(body).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { toast("Gagal: ${e.message}"); setLoading(false) }
            }
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    runOnUiThread { toast("Error ${response.code}"); setLoading(false) }
                    return
                }
                val resultUrl = Gson().fromJson(response.body?.string(), JsonObject::class.java)
                   .getAsJsonArray("data").get(0).asJsonObject.get("url").asString
                downloadVideo("https://username-ai-video-server.hf.space/file=$resultUrl")
            }
        })
    }

    private fun downloadVideo(url: String) {
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                 runOnUiThread { toast("Download gagal"); setLoading(false) }
            }
            override fun onResponse(call: Call, response: Response) {
                val videoFile = File(cacheDir, "temp_video.mp4")
                val fos = FileOutputStream(videoFile)
                fos.write(response.body!!.bytes())
                fos.close()
                runOnUiThread { playVideo(videoFile.path) }
            }
        })
    }

    private fun playVideo(path: String) {
        setLoading(false)
        videoView.setVideoPath(path)
        videoView.start()
        toast("Video Dola AI jadi!")
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnGenerate.isEnabled = !isLoading
        btnGenerate.text = if (isLoading) "Dola AI lagi mikir..." else "Generate Video AI"
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
