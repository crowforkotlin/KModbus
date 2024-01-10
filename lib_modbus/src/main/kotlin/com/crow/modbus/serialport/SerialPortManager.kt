@file:Suppress("unused", "SpellCheckingInspection", "MemberVisibilityCanBePrivate")

package com.crow.modbus.serialport

import com.crow.modbus.interfaces.ISerialPortExt
import com.crow.modbus.interfaces.ISerialPortFailure
import com.crow.modbus.interfaces.ISerialPortSuccess
import com.crow.modbus.tools.error
import com.crow.modbus.tools.info
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.Executors

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Package: com.crow.modbus
 * @Time: 2023/9/23 11:31
 * @Author: CrowForKotlin
 * @Description: SerialPortManager
 * @formatter:on
 **************************/
internal open class SerialPortManager internal constructor(): SerialPort(), ISerialPortExt {

    internal interface ICompleteRepeat { suspend fun onComplete(scope: CoroutineScope): Long }

    internal val mReadJob: Job = SupervisorJob()
    internal var  mWriteJob: Job = SupervisorJob()
    internal val mReadContext = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher() + mReadJob + CoroutineExceptionHandler { _, cause -> cause.stackTraceToString().error() })
    internal val mWriteContext = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher() + mWriteJob + CoroutineExceptionHandler { _, cause  -> cause.stackTraceToString().error() })
    internal var mFileInputStream: BufferedInputStream? = null
    internal var mFileOutputStream: BufferedOutputStream? = null
    private var mSuccessListener = arrayListOf<ISerialPortSuccess>()
    private var mFailureListener = arrayListOf<ISerialPortFailure>()

    /**
     * ● 修改文件权限为可读、可写、可执行
     *
     * ● 2023-09-23 11:41:26 周六 上午
     */
    private fun changeFilePermissions(file: File): Boolean {
        return (file.takeIf { it.exists() } ?: false).runCatching {

            // 获取ROOT权限
            val su = Runtime.getRuntime().exec("/system/bin/su")

            // 修改文件属性为 [可读 可写 可执行]
            val cmd = "chmod 777 ${file.absolutePath}\nexit\n"

            // 将命令写入 su 进程的输出流
            su.outputStream.write(cmd.toByteArray())

            // 如果 su 进程返回值为 0 并且文件可读、可写、可执行，则返回 true
            (su.waitFor() == 0 && file.canRead() && file.canWrite() && file.canExecute())
        }
            .onFailure { catch ->
                when (catch) {
                    is IOException -> "No root permission!".error()
                    else -> catch.stackTraceToString().error()
                }
            }
            .getOrElse { false }
    }

    /**
     * ● 打开串口
     *
     * ● 2023-09-23 16:02:30 周六 下午
     */
    override fun openSerialPort(path: String, baudRate: Int) {

        val device = File(path)
        if (mFileInputStream != null && mFileOutputStream != null) {
            "openSerialPort : 串口已经开启了，请先关闭！".error()
            mFailureListener.forEach { it.onFailure(device, SerialPortState.NO_READ_WRITE_PERMISSION) }
            return
        }

        // 校验串口权限
        if (!device.canRead() || !device.canWrite()) {
            if (!changeFilePermissions(device)) {
                "openSerialPort : 没有读写权限!".error()
                mFailureListener.forEach { it.onFailure(device, SerialPortState.NO_READ_WRITE_PERMISSION) }
                return
            }
        }

        mFileDescriptor = open(device.absolutePath, baudRate, SerialPortParityFunction.NONE, 1, 8)
        mFileInputStream = BufferedInputStream(FileInputStream(mFileDescriptor))
        mFileOutputStream = BufferedOutputStream(FileOutputStream(mFileDescriptor))
        mSuccessListener.forEach { it.onSuccess(device) }
        "串口已经打开 状态 ：${mFileDescriptor?.valid()}".info()
    }
    override fun openSerialPort(ttysNumber: Int, baudRate: Int) {
        openSerialPort("/dev/ttyS$ttysNumber", baudRate)
    }

    /**
     * ● 重新打开串口
     *
     * ● 2024-01-03 18:19:08 周三 下午
     * @author crowforkotlin
     */
    override fun reOpenSerialPort(com: Int, baudRate: Int) {
        closeSerialPort()
        openSerialPort("/dev/ttyS${com}", baudRate)
    }
    override fun reOpenSerialPort(path: String, baudRate: Int) {
        closeSerialPort()
        openSerialPort(path, baudRate)
    }

    /**
     * ● 关闭串口
     *
     * ● 2023-09-23 16:02:12 周六 下午
     */
    override  fun closeSerialPort(): Boolean {
        "◉ 正在关闭串口".info()
        return runCatching {
            mFileDescriptor = null
            mFileInputStream?.close()
            mFileOutputStream?.close()
            mFileInputStream = null
            mFileOutputStream = null
            removeAllOpenSuccessListener()
            removeAllOpenFailureListener()
            true
        }
            .onSuccess { "关闭串口成功".info() }
            .onFailure { catch -> "close serial port exception! ${catch.stackTraceToString()}".error() }
            .getOrElse { false }
    }

    fun addOpenSuccessListener(listener: ISerialPortSuccess) { mSuccessListener.add(listener) }
    fun removeOpenSuccessListener(listener: ISerialPortSuccess) { mSuccessListener.remove(listener) }
    fun removeAllOpenSuccessListener() { mSuccessListener.clear() }
    fun addOpenFailureListener(listener: ISerialPortFailure) { mFailureListener.add(listener) }
    fun removeOpenFailureListener(listener: ISerialPortFailure) { mFailureListener.remove(listener) }
    fun removeAllOpenFailureListener() { mFailureListener.clear() }

    /**
     * ● 重复写入
     *
     * ● 2023-12-01 10:46:03 周五 上午
     * @author crowforkotlin
     */
    internal open fun writeRepeat(interval: Long, onWrite: suspend () -> ByteArray?) {
        mWriteJob.cancelChildren()
        mWriteContext.launch {
            while (true) {
                delay(interval)
                writeBytes(onWrite() ?: continue)
            }
        }
    }

    /**
     * ● 重复读取
     *
     * ● 2023-12-01 10:46:33 周五 上午
     * @author crowforkotlin
     */
    internal open fun onReadRepeat(onReceive: suspend (ByteArray) -> Unit) {
        val maxReadSize = 548
        onReadRepeatEnv { bis ->
            if (null != bis) {
                val bytes = ByteArray(maxReadSize)
                var mReadedBytes = 0
                repeat(15) {
                    delay(20)
                    if (bis.available() > 0) {
                        mReadedBytes += withContext(Dispatchers.IO) { bis.read(bytes, mReadedBytes, bytes.size - mReadedBytes) }
                    }
                }
                onReceive(bytes)
            } else {
                delay(1000)
            }
        }
    }

    /**
     * ● 循环读取环境
     *
     * ● 2024-01-10 19:50:14 周三 下午
     * @author crowforkotlin
     */
    internal inline fun onReadRepeatEnv(crossinline onRepat: suspend (BufferedInputStream?) -> Unit) {
        mReadJob.cancelChildren()
        mReadContext.launch {
            if (mFileInputStream == null) {
                "The read stream has not been opened yet. Maybe the serial port is not open?".error()
                return@launch
            }
            while (true) { onRepat(mFileInputStream) }
        }
    }

    /**
     * ● 写入字节
     *
     * ● 2023-12-01 10:47:02 周五 上午
     * @author crowforkotlin
     */
    open fun writeBytes(bytes: ByteArray): Boolean {
        mFileOutputStream?.let {
            it.write(bytes)
            it.flush()
        }
        return true
    }

    /**
     * ● 取消所有任务
     *
     * ● 2023-12-01 10:47:20 周五 上午
     * @author crowforkotlin
     */
    open fun cancelAllJob() {
        mReadJob.cancel()
        mWriteJob.cancel()
        mReadContext.cancel()
        mWriteContext.cancel()
    }

    /**
     * ● 串口是否开启
     *
     * ● 2023-12-01 10:47:38 周五 上午
     * @author crowforkotlin
     */
    fun isSerialPortNotEnable() = mFileDescriptor == null
}