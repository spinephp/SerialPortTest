package serialport.android.serialportlibrary


import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
//import com.sun.xml.internal.ws.streaming.XMLStreamWriterUtil.getOutputStream
import android_serialport_api.SerialPort


object HexUtils {

    /**
     * 十六进制String转换成Byte[]
     * @param hexString the hex string
     * *
     * @return byte[]
     */
    fun hexStringToBytes(hexString: String?): ByteArray? {
        var hexStr = hexString
        if (hexStr == null || hexStr == "") {
            return null
        }
        hexStr = hexStr.toUpperCase()
        val length = hexStr.length / 2
        val hexChars = hexStr.toCharArray()
        val d = ByteArray(length)
        for (i in 0..length - 1) {
            val pos = i * 2
            d[i] = (charToByte(hexChars[pos]).toInt() shl 4 or charToByte(hexChars[pos + 1]).toInt()).toByte()
        }
        return d
    }

    /**
     * Convert char to byte
     * @param c char
     * *
     * @return byte
     */
    private fun charToByte(c: Char): Byte {

        return "0123456789ABCDEF".indexOf(c).toByte()
    }

    /* 这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
        * @param src byte[] data
        * @return hex string
        */
    fun bytesToHexString(src: ByteArray?): String? {
        val stringBuilder = StringBuilder("")
        if (src == null || src.size <= 0) {
            return null
        }
        for (i in 0..src.size-1) {
            val v = src[i].toInt() and 0xFF
            val hv = Integer.toHexString(v)
            if (hv.length < 2) {
                stringBuilder.append(0)
            }
            stringBuilder.append(hv)
        }
        return stringBuilder.toString()
    }
}

class mySerialPort {
    private val TAG = "SerialPortUtils"
    private var mPath = "/dev/ttys0"
    private var mBaudrate = 9600
    private  var mFlags = 0
    var serialPortStatus = false //是否打开串口标志
    var data_: String? = null
    var threadStatus: Boolean = false //线程状态，为了安全终止线程

    var serialPort: SerialPort? = null
    var inputStream: InputStream? = null
    var outputStream: OutputStream? = null
    //var changeTool = ChangeTool()

    constructor(path:String,baudrate:Int,flags:Int) {
        this.mPath = path
        this.mBaudrate = baudrate
        this.mFlags = flags
    }

    /**
     * 打开串口
     * @return serialPort串口对象
     */
    fun open(): SerialPort? {
        try {
            serialPort = SerialPort(File(this.mPath), this.mBaudrate,this.mFlags)
            this.serialPortStatus = true
            threadStatus = false //线程状态

            //获取打开的串口中的输入输出流，以便于串口数据的收发
            inputStream = serialPort!!.inputStream
            outputStream = serialPort!!.outputStream

            ReadThread().start() //开始线程监控是否有数据要接收
        } catch (e: IOException) {
            Log.e(TAG, "openSerialPort: 打开串口异常：" + e.toString())
            return serialPort
        }

        Log.d(TAG, "openSerialPort: 打开串口")
        return serialPort
    }

    /**
     * 关闭串口
     */
    fun close() {
        try {
            inputStream!!.close()
            outputStream!!.close()

            this.serialPortStatus = false
            this.threadStatus = true //线程状态
            serialPort!!.close()
        } catch (e: IOException) {
            Log.e(TAG, "closeSerialPort: 关闭串口异常：" + e.toString())
            return
        }

        Log.d(TAG, "closeSerialPort: 关闭串口成功")
    }

    /**
     * 发送串口指令（字符串）
     * @param data String数据指令
     */
    fun send(data: String) {

        try {
            val sendData = HexUtils.hexStringToBytes(data) //string转byte[]
            send(sendData)
        } catch (e: IOException) {
            Log.e(TAG, "sendSerialPort: 串口数据发送失败：" + e.toString())
        }

    }

    /**
     * 发送串口指令（字符数组）
     * @param data ByteArray 数据指令
     */
    fun send(data:ByteArray?){
        Log.d(TAG, "sendSerialPort: 发送数据")
        if (data == null || data.size <= 0) {
            return
        }
        try {
            this.data_ = data.toString() //byte[]转string
            if (data.size > 0) {
                outputStream!!.write(data)
                outputStream!!.flush()
                Log.d(TAG, "sendSerialPort: 串口数据发送成功")
            }
        } catch (e: IOException) {
            Log.e(TAG, "sendSerialPort: 串口数据发送失败：" + e.toString())
        }
    }

    /**
     * 发送重启指令
     */
    fun reset(){
        send(byteArrayOf(0x1A,0x03,0x01,0x1E))
    }

    /**
     * 发送恢复出厂设置指令
     */
    fun recovery(){
        send(byteArrayOf(0x1A,0x03,0x02,0x1F))
    }

    /**
     * 发送读网络地址指令
     */
    fun getNetAddress() {
        send(byteArrayOf(0x1A,0x03,0x03,0x20))
    }

    /**
     * 发送写网络地址指令
     * @param data ByteArray 地址数据
     */
    fun setNetAddress(data:ByteArray){
        var cmds = byteArrayOf(0x1A,0x06,0x03,data[0],data[1],0)
        cmds[5] = mod(cmds,5)
        send(cmds)
    }

    /**
     * 发送读扩展地址指令
     */
    fun getExtAddress() {
        send(byteArrayOf(0x1A,0x03,0x04,0x21))
    }

    /**
     * 发送写扩展地址指令
     * @param data ByteArray 地址数据
     */
    fun setExtAddress(data:ByteArray){
        var cmds = byteArrayOf(0x1A,0x06,0x04,data[0],data[1],0)
        cmds[5] = mod(cmds,5)
        send(cmds)
    }

    /**
     * 发送读目标地址指令
     */
    fun getTarAddress() {
        send(byteArrayOf(0x1A,0x03,0x05,0x22))
    }

    /**
     * 发送写目标地址指令
     * @param data ByteArray 地址数据
     */
    fun setTarAddress(data:ByteArray){
        var cmds = byteArrayOf(0x1A,0x06,0x05,data[0],data[1],0)
        cmds[5] = mod(cmds,5)
        send(cmds)
    }

    /**
     * 发送读工作信道指令
     */
    fun getChain(){
        send(byteArrayOf(0x1A,0x03,0x06,0x23))
    }

    /**
     * 发送写工作信道指令
     * @param data ByteArray 数据指令
     */
    fun setChain(){
        send(byteArrayOf(0x1A,0x06,0x06,0x26))
    }

    /**
     * 发送读网络ID指令
     */
    fun getNetId() {
        send(byteArrayOf(0x1A,0x03,0x07,0x24))
    }

    /**
     * 发送写网络ID指令
     * @param data ByteArray 网络ID数据
     */
    fun setNetId(data:ByteArray){
        var cmds = byteArrayOf(0x1A,0x06,0x07,data[0],data[1],0)
        cmds[5] = mod(cmds,5)
        send(cmds)
    }

    /**
     * 发送读重传次数指令
     */
    fun getResendTimes(){
        send(byteArrayOf(0x1A,0x03,0x08,0x25))
    }

    /**
     * 发送写重传次数指令
     * @param data ByteArray 数据指令
     */
    fun setResendTimes(){
        send(byteArrayOf(0x1A,0x06,0x08,0x28))
    }

    /**
     * 发送读超时时间指令
     * @param data ByteArray 数据指令
     */
    fun getTimeout(){
        send(byteArrayOf(0x1A,0x03,0x09,0x26))
    }

    /**
     * 发送写超时时间指令
     */
    fun setTimeout(){
        send(byteArrayOf(0x1A,0x06,0x09,0x27))
    }

    fun mod(data:ByteArray,size:Int):Byte {
        var s:Int = 0
        for (i in 0..size-1){
            s += data[i]
        }
        s = s % 0xff
        return s.toByte()
    }
    /**
     * 单开一线程，来读数据
     */
    private inner class ReadThread : Thread() {
        override fun run() {
            super.run()
            //判断进程是否在运行，更安全的结束进程
            while (!threadStatus) {
                Log.d(TAG, "进入线程run")
                //64   1024
                val buffer = ByteArray(64)
                val size: Int //读取数据的大小
                try {
                    size = inputStream!!.read(buffer)
                    if (size > 0) {
                        Log.d(TAG, "run: 接收到了数据：" + HexUtils.bytesToHexString(buffer))
                        Log.d(TAG, "run: 接收到了数据大小：" + size.toString())
                        val check:Byte = buffer[size-1]
                        buffer[size] = 0
/*
                        if(mod(buffer,size-1) != check) throw IOException("数据较验出错！")
                        if (buffer[1] == 0x83.toByte()) throw IOException("数据读错误！")
                        if (buffer[1] == 0x86.toByte()) throw IOException("数据写错误！")
*/
                        mDataReceiveListener!!.onDataReceive(buffer, size)
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "run: 数据读取异常：" + e.toString())
                }

            }

        }
    }


    //这是写了一监听器来监听接收数据
    var mDataReceiveListener: OnDataReceiveListener? = null

    interface OnDataReceiveListener {
        fun onDataReceive(buffer: ByteArray, size: Int)
    }

    fun setOnDataReceiveListener(dataReceiveListener: OnDataReceiveListener) {
        mDataReceiveListener = dataReceiveListener
    }
}