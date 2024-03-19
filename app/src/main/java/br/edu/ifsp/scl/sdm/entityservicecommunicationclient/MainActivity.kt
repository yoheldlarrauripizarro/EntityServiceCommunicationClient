package br.edu.ifsp.scl.sdm.entityservicecommunicationclient

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.scl.sdm.entityservicecommunication.IncrementBoundServiceInterface
import br.edu.ifsp.scl.sdm.entityservicecommunicationclient.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val amb: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var incrementBoundServiceIntent: Intent
    private var counter = 0
    private var ibService: IncrementBoundServiceInterface? = null

    private val incrementBoundServiceConnection = object : ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.v(getString(R.string.app_name),"Client bound to the service.")
            service?.also {
                ibService = IncrementBoundServiceInterface.Stub.asInterface(service)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.v(getString(R.string.app_name),"Client unbound to the service.")
            ibService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(amb.root)

        incrementBoundServiceIntent = Intent().apply {
            component = ComponentName(
                "br.edu.ifsp.scl.sdm.entityservicecommunicaction",
                    "br.edu.ifsp.scl.sdm.entityservicecommunicaction.IncrementBoundService"
            )
        }

        if (!bindService(
            incrementBoundServiceIntent,
            incrementBoundServiceConnection,
            BIND_AUTO_CREATE
            )){
            Toast.makeText(this,"Service unavailable", Toast.LENGTH_SHORT).show()
            finish()
        }

        with(amb) {
            mainTb.apply {
                getString(R.string.app_name).also { setTitle(it) }
                setSupportActionBar(this)
            }
            incrementBt.setOnClickListener {
                Thread{
                    ibService?.increment(counter)?.also {
                        counter = it
                        runOnUiThread {
                            Toast.makeText(this@MainActivity,
                                "You Clicked $counter times.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }.start()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(incrementBoundServiceConnection)
    }
}