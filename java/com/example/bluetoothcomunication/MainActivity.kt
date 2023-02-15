package com.example.bluetoothcomunication

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import java.util.ArrayList

/**
 * @author Marek Frańczak
 * Main activity in app. Show paired bluetooth devices.
 * Redirects to the activity responsible for communication with chosen device.
 */


class MainActivity : AppCompatActivity() {

    /**
     * Variable that contain BluetoothAdapter object.
     */
    private var m_bluetoothAdapter: BluetoothAdapter? = null
    /**
     * Variable that storage list of bluetooth device.
     */
    private lateinit var m_pairedDivice: Set<BluetoothDevice>
     /**
     * Variable that storage code of successfull bluetooth operation.
     */
    private val REQUEST_ENABLE_BLUETOOTH = 1


    /**
     * Static variable.
     */
    companion object{
        val EXTRA_ADDRESS: String = "device_address"
    }

    /**
     * Function responsible for create the activity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val refButton: Button = findViewById(R.id.select_drive_refresh)
        val selectDeviceList: ListView = findViewById(R.id.select_drive_list)
        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(m_bluetoothAdapter==null){
            Toast.makeText(this,"Don't support bluetooth",Toast.LENGTH_LONG).show()
            return
        }
        try {
            if(!m_bluetoothAdapter!!.isEnabled){
                val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
            }
        } catch (e: SecurityException){
            Toast.makeText(this,"don't have promision",Toast.LENGTH_LONG).show()
            e.stackTrace
        }
        refButton.setOnClickListener { pairedDiviceList() }
    }

    /**
     * Function responsible for display lit of paired device and redirects to activity responsible for communication.
     */
    private fun pairedDiviceList(){
        try {
            m_pairedDivice=m_bluetoothAdapter!!.bondedDevices
            var list: ArrayList<BluetoothDevice> = ArrayList()

            if(!m_pairedDivice.isEmpty()){
                for(divice: BluetoothDevice in m_pairedDivice){
                    list.add(divice)
                }
            } else{
                Toast.makeText(this,"no paired divice found",Toast.LENGTH_LONG).show()
            }

            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
            val selectDeviceList: ListView = findViewById(R.id.select_drive_list)
            selectDeviceList.adapter = adapter
            selectDeviceList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                val device: BluetoothDevice = list[position]
                val address: String = device.address

                val intent = Intent(this, ControlActivity::class.java)
                intent.putExtra(EXTRA_ADDRESS, address)
                startActivity(intent)
            }
        }catch (e:SecurityException){
            Toast.makeText(this,"don't have promision",Toast.LENGTH_LONG).show()
            e.stackTrace
        }

    }

    /**
     * Notifications about loss of communication
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == Activity.RESULT_OK) {
                if (m_bluetoothAdapter!!.isEnabled) {
                    Toast.makeText(this,"Bluetooth has been enabled", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this,"Bluetooth has been disabled",Toast.LENGTH_SHORT).show()
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this,"Bluetooth enabling has been canceled", Toast.LENGTH_SHORT).show()
            }
        }
    }
}