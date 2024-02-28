# **KModbus**

<p>
    <img alt="ViewCount" src="https://views.whatilearened.today/views/github/crowforkotlin/KModbus.svg">
</p>

- **Support Rtu、Tcp、Ascii**
- **Built-in polling and reconnection processing mechanism**

```kotlin
repositories { mavenCentral() }

implementation("com.kotlincrow.android.component:KModbus:1.0")

// In addition, you also need to introduce libSerial Port.so into your project, which can be found in Release
```

|                                   ![](docs/img/KModbus_Preview.gif)                                   |
|:-----------------------------------------------------------------------------------------------------:|
| Left->Tcp 1000ms (HoldingRegisters - GB2312 - String)  /  Right ->Rtu 50ms (HoldingRegisters - Int16) |

```kotlin
class MainActivity : AppCompatActivity() {

    private val mKModbusRtu = KModbusRtu()
    private val mKModbusTcp = KModbusTcp()
    private val mKModbusAscii = KModbusAscii()

    override fun onDestroy() {
        super.onDestroy()

        // Clear all context to prevent any references. You can also continue to use the object after clearing it to continue your tasks later.
        mKModbusRtu.cancelAll()
        mKModbusAscii.cancelAll()
        mKModbusTcp.cancelAll()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // You can open different serial ports by constructing multiple KModbusRtu, the same goes for TCP and ASCII
        initRTU(ttySNumber = 0, BaudRate.S_9600)
        initAscii(ttySNumber = 3, BaudRate.S_9600)
        initTcp(host = "192.168.1.101", port = 502)
    }

    private fun initRTU(ttySNumber: Int, baudRate: Int) {
        mKModbusRtu.apply {
            // The openSerialPort function has multiple overloads. You can customize the incoming control, serial port, baud rate, check mode, stop bit, and data bit.
            openSerialPort(ttysNumber = ttySNumber, baudRate = baudRate, parity = SerialPortParityFunction.NONE, stopBit = 1, dataBit = 8)

            // Set the listener for data returned from the slave station in master mode
            addOnMasterReceiveListener { arrays -> "Rtu : ${resolveMasterResp(arrays, ModbusEndian.ARRAY_BIG_BYTE_BIG)}".info() }

            // If you want to poll and write multiple data, you can add the data to the queue in the same way as listOf.
            setOnDataWriteReadyListener { listOf(buildMasterOutput(READ_HOLDING_REGISTERS, 1, 0, 1)) }

            // Set the reading behavior to: master mode. If it is slave mode, it means that the data sent by the master station will be read.
            startRepeatReceiveDataTask(kModbusBehaviorType = KModbusType.MASTER)

            // Enable polling tasks for writing data, with built-in timeout mechanism
            startRepeatWriteDataTask(interval = 1000L, timeOut = 1000L) { "Rtu time out!".info() }

            // If you do not enable polling writing, you can also manually control the writing of data yourself.
            /* 
             mKModbusRtu.writeData(buildMasterOutput(
                 function = KModbusFunction.WRITE_SINGLE_REGISTER,
                 slaveAddress = 6,
                 startAddress = 0,
                 count = 2,
                 value = 0
             ))
             */
        }
    }

    private fun initTcp(host: String, port: Int) {
        mKModbusTcp.apply{
            startTcp(host, port, retry = true) { ins, ops ->
                addOnMasterReceiveListener {  arrays -> "Tcp : ${resolveMasterResp(arrays, ModbusEndian.ARRAY_BIG_BYTE_BIG)}".info() }
                setOnDataWriteReadyListener { listOf(buildMasterOutput(READ_HOLDING_REGISTERS, 1, 0, 1)) }
                startRepeatReceiveDataTask(ins, KModbusType.MASTER)
                startRepeatWriteDataTask(ops, interval = 1000L, timeOut = 1000L) { "Tcp time out!".info() }
            }
        }
    }

    private fun initAscii(ttySNumber: Int, baudRate: Int) {
        mKModbusAscii.apply {
            openSerialPort(ttySNumber, baudRate)
            addOnMasterReceiveListener { arrays -> "Ascii : ${arrays.toHexList()}".info() }
            setOnDataWriteReadyListener { listOf(buildMasterOutput(READ_HOLDING_REGISTERS, 1, 0, 1)) }
            startRepeatReceiveDataTask(KModbusType.MASTER)
            startRepeatWriteDataTask(interval = 1000L, timeOut = 1000L) { "Ascii time out!".info() }
        }
    }
}
```

```kotlin 
fun main() {
    // If you want to write floating point data to a register
    KModbusRtu().buildMasterOutput(
        function = KModbusFunction.WRITE_HOLDING_REGISTERS,
        slaveAddress = 1,
        startAddress = 1,
        count = 2,
        values = 123.789f.toIntArray()
    )
}
```

[Docs Here](https://www.kotlincrow.com/2023/10/07/Modbus/): 