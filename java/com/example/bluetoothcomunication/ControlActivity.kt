package com.example.bluetoothcomunication

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.util.*

/**
 * @author Marek Frańczak
 * Class responsible for connect and communication with the bluetooth module.
 */

class ControlActivity : AppCompatActivity() {

    //Static variable
    companion object{
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null
        lateinit var m_progress: ProgressDialog
        lateinit var m_bluetoothAdapter: BluetoothAdapter
        var m_isConnected: Boolean = false
        lateinit var m_address: String
    }

    /**
     * Function responsible for create the activity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.control_layout)
        m_address = intent.getStringExtra(MainActivity.EXTRA_ADDRESS).toString()

        ConnectToDivide(this).execute()

        val disconnectButton: Button = findViewById(R.id.ld_disconnect)
        val seekBar: SeekBar = findViewById(R.id.seekBar)
        val textView: TextView = findViewById(R.id.textView)
        var startPoint = 0
        var endPoint = 0

        //SeekButton Listener. Convert the values from button that microcontroller can use it to control motor.
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textView.setText("Angle:" + progress.toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                if(seekBar != null){
                    startPoint = seekBar.progress
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if(seekBar != null){
                    endPoint = seekBar.progress

                val number:Int = (46+seekBar.progress+(seekBar.progress*0.12)).toInt()
                var sNumber: String = number.toString()

                if (sNumber.length<3){
                    sNumber = "0"+sNumber
                }
                    textView.setText("Angle:" + m_progress.toString())
                sendCommand(sNumber)
                }
            }
        })
        disconnectButton.setOnClickListener { disconnect() }
    }

    /**
     * Function for sending messages via bluetooth.
     */
    private fun sendCommand(input: String){
        if(m_bluetoothSocket != null){
            try {
                m_bluetoothSocket!!.outputStream.write(input.toByteArray())
            }catch (e: IOException){
                Toast.makeText(this,"Messeage didn't do", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    /**
     * Function responsible for disconnect bluetooth connection
     */
    private fun disconnect(){
        if(m_bluetoothSocket != null){
            try{
                m_bluetoothSocket!!.close()
                m_bluetoothSocket = null
                m_isConnected = false
            } catch (e: IOException) {
                Toast.makeText(this, "Disconecting went wrong", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
        finish()
    }

    /**
     * Privet class responsible for connection to bluetooth device
     */
    private class ConnectToDivide(c: Context): AsyncTask<Void, Void, String>(){

        private var connectSuccess: Boolean = true
        private val context: Context

        init {
            this.context = c
        }

        //Information about connection status
        override fun onPreExecute() {
            super.onPreExecute()
            m_progress = ProgressDialog.show(context, "Conecting..","please wait")
        }

        //Proper connection to the bluetooth device
        override fun doInBackground(vararg params: Void?): String? {
            try{
                if(m_bluetoothSocket == null || !m_isConnected){
                    m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = m_bluetoothAdapter.getRemoteDevice(m_address)
                    try{
                        m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                        BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                        m_bluetoothSocket!!.connect()
                    }catch (e: SecurityException){
                        e.printStackTrace()
                    }
                }
            } catch (e: IOException){
                connectSuccess = false
                e.printStackTrace()
            }
            return null
        }

        //Disabling notifications when connecting to a bluetooth device
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (!connectSuccess){
                Log.i("data", "couldn't connect")
            } else {
                m_isConnected = true
            }
            m_progress.dismiss()
        }

    }


}

