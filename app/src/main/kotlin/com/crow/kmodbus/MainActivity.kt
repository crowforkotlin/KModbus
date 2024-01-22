@file:Suppress("PrivatePropertyName", "SpellCheckingInspection", "LocalVariableName")
@file:OptIn(ExperimentalStdlibApi::class, ExperimentalStdlibApi::class, ExperimentalStdlibApi::class)

package com.crow.kmodbus

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.crow.modbus.KModbusASCIIMaster
import com.crow.modbus.KModbusRtu
import com.crow.modbus.model.KModbusType
import com.crow.modbus.serialport.BaudRate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MainActivity : AppCompatActivity() {

    private val mKModbusRTU = KModbusRtu()

    private val IO = CoroutineScope(Dispatchers.IO)


    private val kModbusASCIIMaster = KModbusASCIIMaster.getInstance()

    @OptIn(ExperimentalStdlibApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mKModbusRTU.openSerialPort(0, BaudRate.S_9600)
        /*mKModbusRTU.addOnMasterReceiveListener {
            val data = mKModbusRTU.resolveMasterResp(it, )
            "Read Bytes ${it.mValues?.toHexList()}".info()
        }*/
        mKModbusRTU.setOnDataWriteReadyListener { null }
//        mKModbusRTU.runRepeatWriteDataTask(1000)
        mKModbusRTU.runRepeatReceiveDataTask(KModbusType.SLAVE)
    }


}