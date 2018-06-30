# SerialPortTest
概述
串口Android平台SDK (serialportsdk.jar) 为第三方串口应用提供了文档易用的串口API调用服务，使第三方客户端无需了解复杂的API调用过程。
整体架构
主要类说明： [1]
1、mySerialPort： 串口API 接口类，对外提供串口API的调用，包括打开、关闭、读和写等串口API调用，其中读串口封装了回调接口，通过创建线程来调用mySerialPort中的接口方法。
2、HexUtils：串与16进制字符数组相互转换工具类。
接口说明
Class mySerialPort：

构造函数
接口名称： mySerialPort(path:String，baudrate：Int,flags:Int): SerialPort?

参数名称
作用
Path
串口设备路径，如 “/dev/ttyS0”
baudrate
串口通信的波特率
flags

返回结果：如成功返回 mySerialPort 类型的值，否则返回 null。

打开串口
接口名称：fun open(): SerialPort?

参数名称
作用
无

返回结果：如打开成功返回 SerialPort 类型的值，否则返回 null。

关闭串口
接口名称：fun close()
参数名称
作用
无

返回结果：无。

串口发送字符串
接口名称：fun send(data: String)

参数名称
作用
data
要发送的字符串。
返回结果：无。

串口发送字符数组
接口名称：fun send(data:ByteArray?)

参数名称
作用
data
16进制字符数组。
返回结果：无。

串口发送重置指令
接口名称：fun reset()


参数名称
作用
无

返回结果：无。

串口发送恢复出厂设置指令
接口名称：fun recovery()

参数名称
作用
无

返回结果：无。

串口发送读网络地址指令
接口名称：fun getNetAddress()

参数名称
作用
无

返回结果：无。

串口发送写网络地址指令
接口名称：fun setNetAddress(data:ByteArray)

参数名称
作用
data
包含网络地址的字节数组
返回结果：无。

串口发送读扩展地址指令
接口名称：fun getExtAddress()

参数名称
作用
无

返回结果：无。

串口发送写扩展地址指令
接口名称：fun setExtAddress(data:ByteArray)


参数名称
作用
data
包含扩展地址的字节数组
返回结果：无。

串口发送读目标地址指令
接口名称：getTarAddress()

参数名称
作用
无

返回结果：无。

串口发送写目标地址指令
接口名称：fun setTarAddress(data:ByteArray)

参数名称
作用
data
包含目标地址的字节数组
返回结果：无。

串口发送读工作信道指令
接口名称：fun getChain()

参数名称
作用
无

返回结果：无。

串口发送写工作信道指令
接口名称：fun setChain()

参数名称
作用
无

返回结果：无。

串口发送读网络ID指令
接口名称：fun getNetId()

参数名称
作用
无

返回结果：无。

串口发送写网络ID指令
接口名称：fun setNetId(data:ByteArray)

参数名称
作用
data
包含网络ID的字节数组
返回结果：无。

串口发送读重传次数指令
接口名称：fun getResendTimes()
 
参数名称
作用
无

返回结果：无。

串口发送写重传次数指令
接口名称：fun setResendTimes()

参数名称
作用
无

返回结果：无。

串口发送读超时时间指令
接口名称：fun getTimeout()

参数名称
作用
无

返回结果：无。

串口发送写超时时间指令
接口名称：fun setTimeout()

参数名称
作用
无

返回结果：无。

设置串口接收监听程序
接口名称：fun setOnDataReceiveListener(object :dataReceiveListener：OnDataReceiveListener ）

参数名称
作用
dataReceiveListener
串口接收监听程序
返回结果：无。

串口接收监听程序
接口名称：fun OnDataReceiveListener（）

参数名称
作用
无

返回结果：无。

Class HexUtils. ：

String转byte[]
接口名称：fun hexStringToBytes(hexString: String?): ByteArray?

参数名称
作用
hexString
16进制字符串
 返回结果：16进制字符数组。
Byte[] 转 String
接口名称：fun bytesToHexString(src: ByteArray?): String?

参数名称
作用
src
16进制字节数组。
返回结果：16进制字符串。


实例分析
package com.example.gll.testserialport  
import android.support.v7.app.AppCompatActivity 
import android.os.Bundle 
import android.util.Log 
import  android.os.Handler 
import kotlinx.android.synthetic.main.activity_main.* 
import serialport.android.serialportlibrary.mySerialPort  
class MainActivity : AppCompatActivity() { 
  private val TAG = "MainActivity" 
  private var sport:mySerialPort? = null 
  var mBuffer:ByteArray = ByteArray(1024) 
  override fun onCreate(savedInstanceState: Bundle?) { 
  super.onCreate(savedInstanceState) 
  setContentView(R.layout.activity_main) 
  var handler = Handler() 
  sport = mySerialPort("/dev/ttyS0",9600,0) 
  sport?.setOnDataReceiveListener(object : mySerialPort.OnDataReceiveListener{ 
    internal var runnable:Runnable = Runnable { textView_status.setText("size:"+mBuffer.size.toString()+"数据监听:"+String(mBuffer)) }  
    override fun onDataReceive(buffer: ByteArray, size: Int) { 
      Log.d(TAG,"进入数据监听中。。。"+ String(buffer)) 
      mBuffer = buffer 
      handler.post(runnable) 
    }         
  })  
  sport?.open() 
  sport?.send("popopopo")  
}  
override fun onDestroy() { 
  super.onDestroy() 
  sport?.close() 
} 
}

SDK使用说明
	•	1、将Androidstudio中的项目切换为project
	•	2、找到app下的libs，将serialportsdk.jar包复制粘贴进去
	•	3、jar包复制进去后，选中你的jar包
	•	4、右键，在弹出的菜单中选择add as library
	•	5、弹出模块选择，你的jar包是给哪个模块用的就选哪个
	•	6、点击ok后项目会自动编译
7、打开app下的build.gradle就可以看到新增了 implementation
	•	 files('libs/serialportsdk.jar')。这样jar包就可以使用了。
文档更新时间: 2018-06-15


