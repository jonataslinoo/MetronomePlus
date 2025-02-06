package br.com.jonatas.metronomeplus.ui

import android.content.res.AssetManager
import android.media.AudioManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.com.jonatas.metronomeplus.databinding.ActivityMainBinding
import br.com.jonatas.metronomeplus.model.Beat
import br.com.jonatas.metronomeplus.model.BeatState

class MainActivity : BaseActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private var bpm: Int = 120
    private var beats: Int = 4
    private var tones: MutableList<Beat> = mutableListOf()
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setTextHelloC()
        setDefaultStreamValues()
        native_onInit(assets)
        setupMetronome()
        setupConfigs()
    }

    override fun onDestroy() {
        super.onDestroy()
        native_onEnd()
    }

    private fun setupConfigs() {
        native_SetBPM(bpm)
        native_SetBeats(tones.map { it }.toTypedArray())
    }

    private fun setupMetronome() {
        //TODO verificar a utilizar de coroutines aqui para poder atualizar a interface sem travamentos.
        binding.btnPlayPause.setOnClickListener {
            if (isPlaying) {
                binding.btnPlayPause.text = "Play"
                isPlaying = false
                native_onStopPlaying()
            } else {
                isPlaying = true
                binding.btnPlayPause.text = "Pause"
                native_onStartPlaying()
            }
        }

        //Incrementa / decrementa a quantidade de bpms
        binding.btnMoreOne.setOnClickListener {
            bpm += 1
            binding.tvBpm.text = bpm.toString()
            native_SetBPM(bpm)
        }
        binding.btnMoreThen.setOnClickListener {
            bpm += 10
            binding.tvBpm.text = bpm.toString()
            native_SetBPM(bpm)
        }
        binding.btnMinusOne.setOnClickListener {
            bpm -= 1
            binding.tvBpm.text = bpm.toString()
            native_SetBPM(bpm)
        }
        binding.btnMinusThen.setOnClickListener {
            bpm -= 10
            binding.tvBpm.text = bpm.toString()
            native_SetBPM(bpm)
        }

        // Incrementa / decrementa a quantidade de batidas

        tones = mutableListOf(
            Beat(BeatState.Accent),
            Beat(BeatState.Normal),
            Beat(BeatState.Normal),
            Beat(BeatState.Normal)
        )

        binding.btnMoreOneBeat.setOnClickListener {
            beats += 1
            binding.beatCounter.text = beats.toString()

            tones.add(Beat(BeatState.Normal))

            native_SetBeats(tones.map { it }.toTypedArray())
        }
        binding.btnMinusOneBeat.setOnClickListener {
            beats -= 1
            binding.beatCounter.text = beats.toString()

            tones.removeAt(tones.lastIndex)

            native_SetBeats(tones.map { it }.toTypedArray())
        }
    }

    private fun setTextHelloC() {
        binding.tvHello.apply {
            text = "${text} ${helloC()}"
        }
    }

    private fun setDefaultStreamValues() {
        val myAudioMgr = getSystemService(AUDIO_SERVICE) as AudioManager
        val sampleRateStr = myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)
        val defaultSampleRate = sampleRateStr.toInt()
        val framesPerBurstStr =
            myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER)
        val defaultFramesPerBurst = framesPerBurstStr.toInt()

        native_setDefaultStreamValues(defaultSampleRate, defaultFramesPerBurst)
    }

    private external fun helloC(): String
    private external fun native_onInit(assetManager: AssetManager)
    private external fun native_onEnd()
    private external fun native_SetBPM(bpm: Int)
    private external fun native_SetBeats(beats: Array<Beat>)
    private external fun native_onStartPlaying()
    private external fun native_onStopPlaying()
    private external fun native_setDefaultStreamValues(
        defaultSampleRate: Int,
        defaultFramesPerBurst: Int
    )

    companion object {
        init {
            System.loadLibrary("metronomeplus-lib")
        }
    }
}