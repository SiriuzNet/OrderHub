package com.digitalpurr.orderhub

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.net.URISyntaxException
import java.util.*
import kotlin.concurrent.timerTask
import android.preference.PreferenceManager
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var mWebSocketClient: WebSocketClient? = null
    private var mOptionsMenu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }

    private fun connectWebSocket() {
        val uri: URI
        try {
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            Log.d("Websocket", "Connecting: "+prefs.getString("serverUrl", "127.0.0.1:8080"))
            uri = URI("ws://"+prefs.getString("serverUrl", resources.getString(R.string.pref_default_server_url))+"/websocket")
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            return
        }

        mWebSocketClient = object : WebSocketClient(uri) {
            override fun onOpen(serverHandshake: ServerHandshake) {
                Log.i("Websocket", "Opened")
                mWebSocketClient?.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL)
                runOnUiThread {
                    mOptionsMenu?.findItem(R.id.online_indicator)?.setIcon(android.R.drawable.presence_online)
                }
            }

            override fun onMessage(s: String) {
                Log.d("Websocket", s)
            }

            override fun onClose(i: Int, s: String, b: Boolean) {
                Log.i("Websocket", "Closed $s")
                runOnUiThread {
                    mOptionsMenu?.findItem(R.id.online_indicator)?.setIcon(android.R.drawable.presence_offline)
                }

                Timer().schedule(timerTask {
                    if (mWebSocketClient?.isOpen == false) {
                        Log.d("Websoocket", "Reconnecting...")
                        mWebSocketClient?.reconnect()
                    }
                }, 3000)
            }

            override fun onError(e: Exception) {
                Log.i("Websocket", "Error " + e.message)
                runOnUiThread {
                    mOptionsMenu?.findItem(R.id.online_indicator)?.setIcon(android.R.drawable.presence_offline)
                }
            }
        }
        mWebSocketClient?.connect()
        startPinging()
    }

    private fun startPinging() {
        val timer = Timer()
        timer.schedule(timerTask {
            if (mWebSocketClient?.isOpen == true) { mWebSocketClient?.sendPing(); }
        },0, 10000)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        mOptionsMenu = menu
        thread { connectWebSocket() }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                return true
            }
            R.id.online_indicator -> {
                thread { mWebSocketClient?.reconnect() }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
