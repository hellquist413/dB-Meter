package com.example.dbmeter

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import kotlin.math.log10
import kotlin.math.roundToInt

private const val LOG_TAG = "MIC-RECORD-TEST"

class MainActivity : AppCompatActivity(), Timer.OnTimerTickListener {

    private var recorder: MediaRecorder? = null
    private var readDbValue: String = ""
    private var fileName: String = ""
    private lateinit var timer: Timer
    // val dbMeasureArray = arrayListOf<Double>()
    var dbMeasureArray = DoubleArray(50)
    var iTwo = 0
    var maxDb = 0
    var minDb = 0

    // -----------------------------------------------------------------//
    // -----------------------------------------------------------------//
    // REQUEST PERMISSIONS TO MICROPHONE AND WRITE EXTERNAL STORAGE
    // -----------------------------------------------------------------//
    // -----------------------------------------------------------------//

    private fun hasWriteExternalStoragePermission() =
        ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun hasMicrophonePermission() =
        ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    private fun requestPermissions() {
        var permissionsToRequest = mutableListOf<String>()
        if(!hasMicrophonePermission()){
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }
        if(!hasWriteExternalStoragePermission()){
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if(permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 0)
        }
    }

    // -----------------------------------------------------------------//
    // -----------------------------------------------------------------//
    // Add new measurement dB value to dbMeasureArray array
    // -----------------------------------------------------------------//
    // -----------------------------------------------------------------//


    fun addDbToArray(newNumberToArray: Double) {

        var newNumber = 0.0

        newNumber = newNumberToArray

    if (iTwo <= 49) {
        dbMeasureArray.set(index=iTwo, value=newNumber)
        iTwo++
    } else {
        iTwo = 0
        dbMeasureArray.set(index=iTwo, value=newNumber)
        iTwo++
    }
        // println(Arrays.toString(dbMeasureArray))
    }

    // -----------------------------------------------------------------//
    // -----------------------------------------------------------------//
    // Calculate average DB from dbMeasureArray
    // -----------------------------------------------------------------//
    // -----------------------------------------------------------------//


    fun calculateAverageDb(): Double {

        var sum = 0.0
        for (num in dbMeasureArray) {
            sum += num
        }
        val averageDbValue = sum / dbMeasureArray.size
        // println("The average is: %.0f".format(averageDbValue))
        // println("${averageDbValue}")
        // println(dbMeasureArray.size)
        return averageDbValue
    }

    // -----------------------------------------------------------------//
    // -----------------------------------------------------------------//
    // Update textview
    // -----------------------------------------------------------------//
    // -----------------------------------------------------------------//

    fun updateAverageDbTextview(averageDbValue: Int) {
        //  Update textview
        var averageDbOut = 0
        val tvDbViewerAverage: TextView = findViewById(R.id.textViewAverageDb) as TextView

        if(averageDbValue <= 0) {
            averageDbOut = 0
        } else {
            averageDbOut = averageDbValue
        }
        tvDbViewerAverage.setText("${averageDbOut}" + " dB")
    }

    fun updateMaxDbTextview(maxDbValue: Int) {
        //  Update MAX DB Textview

        if(maxDbValue > maxDb) {
            maxDb = maxDbValue
            val tvDbViewerAverage: TextView = findViewById(R.id.textViewMaxDb) as TextView
            tvDbViewerAverage.setText("${maxDb}" + " dB")
        } else {
            maxDb = maxDb
        }
    }

    fun updateMinDbTextview(minDbValue: Int) {
        //  Update MAX DB Textview

        if(minDbValue < minDb || minDb <= 0) {
            if(minDbValue < 0) {
                minDb = 0
            } else {
                minDb = minDbValue
            }
            val tvDbViewerAverage: TextView = findViewById(R.id.textViewMinDb) as TextView
            tvDbViewerAverage.setText("${minDb}" + " dB")
        } else {
            minDb = minDb
        }
    }



    // -----------------------------------------------------------------//
    // -----------------------------------------------------------------//
    // PREPARE RECORDER
    // -----------------------------------------------------------------//
    // -----------------------------------------------------------------//

    fun startRecording() {

        fileName = "${externalCacheDir?.absolutePath}/audiorecordtest.mp3"
        recorder = MediaRecorder()

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(fileName)
            try {
                prepare()
                // Log.d(LOG_TAG, "prepare success")
            } catch (e: IOException) {
                // Log.d(LOG_TAG, "prepare failed")
            }

            start()
        }
    }


    // -----------------------------------------------------------------//
    // -----------------------------------------------------------------//
    // START TIMER & UPDATE SCREEN
    // -----------------------------------------------------------------//
    // -----------------------------------------------------------------//

    override fun onTimerTick(duration: String) {
        // println(duration)

        var readDbValueInt = 0
        val referenceAmp = 0.5
        var readDbValueCalc = 0.0
        var formattedDbToString = ""
        var getAverage = 0.0
        val tvDbViewer: TextView = findViewById(R.id.tvDbViewer) as TextView


        readDbValue = recorder?.maxAmplitude.toString()
        readDbValueInt = readDbValue.toInt()

        // Calculate SPL - Sound Pressure Level - value
        readDbValueCalc = (20 * log10(readDbValueInt / referenceAmp))


        addDbToArray(readDbValueCalc)

        formattedDbToString = readDbValueCalc.toString()
        formattedDbToString = formattedDbToString.substringBefore(".")

        if (formattedDbToString == "-Infinity") {
            formattedDbToString = "0"
        }


        // Update TextView
        getAverage = calculateAverageDb()
        // println(getAverage.roundToInt())
        updateAverageDbTextview(getAverage.roundToInt())
        updateMaxDbTextview(readDbValueCalc.roundToInt())
        updateMinDbTextview(readDbValueCalc.roundToInt())
        tvDbViewer.setText(((formattedDbToString) + " dB"))

        //Draw waveform
        WaveformView.addAmplitude(readDbValueCalc.toFloat())
        // println(readDbValueCalc.toFloat())


    }

    // -----------------------------------------------------------------//
    // -----------------------------------------------------------------//
    // CREATE APPLICATION INSTANCE
    // -----------------------------------------------------------------//
    // -----------------------------------------------------------------//

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions()
        startRecording()


        timer = Timer(this)
        timer.start()
        // Log.d(LOG_TAG, Arrays.toString(dbMeasureArray))

        // -----------------------------------------------------------------//
        // -----------------------------------------------------------------//
        // RESET VALUES ON CLICK
        // -----------------------------------------------------------------//
        // -----------------------------------------------------------------//


        // RESET MAX VALUE
        val resetMax: TextView = findViewById(R.id.textViewMaxDb) as TextView
        resetMax.setOnClickListener() {
            maxDb = 0
        }
        // RESET MIN VALUE
        val resetMin: TextView = findViewById(R.id.textViewMinDb) as TextView
        resetMin.setOnClickListener() {
            minDb = 0
        }

        // RESET AVERAGE VALUE
        val resetAverage: TextView = findViewById(R.id.textViewAverageDb) as TextView
        resetAverage.setOnClickListener() {
            dbMeasureArray = DoubleArray(50) { 0.0 }
            // Log.d(LOG_TAG, Arrays.toString(dbMeasureArray))
        }

        val resetAll: Button = findViewById(R.id.resetAll) as Button
        resetAll.setOnClickListener() {
            dbMeasureArray = DoubleArray(50) { 0.0 }
            minDb = 0
            maxDb = 0
            // Log.d(LOG_TAG, Arrays.toString(dbMeasureArray))

        }

    }


}