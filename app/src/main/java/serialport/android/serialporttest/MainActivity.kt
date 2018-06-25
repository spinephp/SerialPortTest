package serialport.android.serialporttest

import android.os.Handler
import android.util.Log;
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import serialport.android.serialportlibrary.mySerialPort

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    var mBuffer: ByteArray = ByteArray(1024)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Example of a call to a native method
        sample_text.text = stringFromJNI()

        var handler = Handler()
        //串口数据监听事件
        var serialPortUtils = mySerialPort("/dev/ttys0",9600,0)
        serialPortUtils.open()
        serialPortUtils.setOnDataReceiveListener(object : mySerialPort.OnDataReceiveListener {
            //开线程更新UI
            internal var runnable: Runnable = Runnable { textView_status.setText("size：" + mBuffer.size.toString() + "数据监听：" + String(mBuffer)) }

            override fun onDataReceive(buffer: ByteArray, size: Int) {
                Log.d(TAG, "进入数据监听事件中。。。" + String(buffer))
                //
                //在线程中直接操作UI会报异常：ViewRootImpl$CalledFromWrongThreadException
                //解决方法：handler
                //
                mBuffer = buffer
                handler.post(runnable)
            }
        })
    }

    //开线程更新UI
    var runnable: Runnable = Runnable { textView_status.setText("size：" + mBuffer.size.toString() + "数据监听：" + String(mBuffer)) }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
