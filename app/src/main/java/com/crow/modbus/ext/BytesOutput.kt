package com.crow.modbus.ext


import java.io.ByteArrayOutputStream

class BytesOutput : ByteArrayOutputStream() {

    fun writeInt8(byte: Byte) {
        this.write(byte.toInt())
    }

    fun writeInt8(int32: Int) {
        this.write(fromInt8(int32))
    }

    fun writeInt16(int32: Int) {
        val bytes: Bytes = fromInt16(int32)
        this.write(bytes, 0, bytes.size)
    }

    fun writeInt16Reversal(int32: Int) {
        val bytes: Bytes = fromInt16LittleEndian(int32)
        this.write(bytes, 0, bytes.size)
    }

    fun writeInt32(int32: Int) {
        val bytes: ByteArray = fromInt32(int32)
        this.write(bytes, 0, bytes.size)
    }

    fun writeBytes(bytes: Bytes, len: Int) {
        this.write(bytes, 0, len)
    }
}
