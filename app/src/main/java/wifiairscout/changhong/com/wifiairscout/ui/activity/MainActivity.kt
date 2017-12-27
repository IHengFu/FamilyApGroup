package wifiairscout.changhong.com.wifiairscout.ui.activity

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import wifiairscout.changhong.com.wifiairscout.App
import wifiairscout.changhong.com.wifiairscout.R
import wifiairscout.changhong.com.wifiairscout.model.HouseData
import wifiairscout.changhong.com.wifiairscout.model.WifiDevice
import wifiairscout.changhong.com.wifiairscout.model.response.GetClientResponse
import wifiairscout.changhong.com.wifiairscout.model.response.GetClientStatusResponse
import wifiairscout.changhong.com.wifiairscout.preferences.Preferences
import wifiairscout.changhong.com.wifiairscout.service.StartService
import wifiairscout.changhong.com.wifiairscout.task.GenericTask
import wifiairscout.changhong.com.wifiairscout.ui.adapter.DeviceViewPagerAdapter
import wifiairscout.changhong.com.wifiairscout.ui.view.DragViewGroup1
import wifiairscout.changhong.com.wifiairscout.ui.view.DragViewPager
import wifiairscout.changhong.com.wifiairscout.utils.CommUtils
import wifiairscout.changhong.com.wifiairscout.utils.FileUtils
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), ViewPager.OnPageChangeListener {


    private val viewPager: DragViewPager<WifiDevice> by lazy { findViewById<DragViewPager<WifiDevice>>(R.id.vp_device) }

    private val layout_apartment: DragViewGroup1 by lazy { findViewById<DragViewGroup1>(R.id.layout_apartment) }
    private val mArrayDevices = ArrayList<WifiDevice>()

    private var mUdpTask: GenericTask? = null

    companion object {
        const val REQUEST_OPRATION = 2 //设置请求的code
    }

    private val deviceAdapter: DeviceViewPagerAdapter by lazy { DeviceViewPagerAdapter(this, mArrayDevices) }

    private var mProgressDialog: ProgressDialog? = null
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.mipmap.ic_setting)
        EventBus.getDefault().register(this)
        if (!App.sTest)
            startService(Intent(this, StartService::class.java))
        else {
            mArrayDevices.addAll(getDevices())
        }

        initViewPager()
        resetHouseStructure()

        layout_apartment.setOnItemClickListener(object : AdapterView.OnItemClickListener {
            override fun onItemClick(adapterView: AdapterView<*>?, view1: View, i: Int, l: Long) {
                if (view1.getTag() != null) {
                    val intent = Intent(this@MainActivity, DeviceDetailActivity::class.java)
                    intent.putExtra(Intent.EXTRA_DATA_REMOVED, view1.getTag() as WifiDevice)
                    startActivity(intent)
                }
            }
        })

        CommUtils.transparencyBar(this);
//        StartService.startServcie(this, StartService.ACTION_LOAD_DEVICE)

        startFlushRssi()

        val r1 = Rect(0, 0, 10, 10);
        val r2 = Rect(9, 9, 11, 11)
        System.err.println(" test ${r1.contains(r2)}")

        mProgressDialog = ProgressDialog.show(this, null, getString(R.string.notice_download_data), true, false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_channel_set, R.id.action_optimize -> {
                Toast.makeText(this@MainActivity, R.string.notice_wait_for_develop, Toast.LENGTH_SHORT).show()
                return true
            }
            android.R.id.home -> {
                startActivityForResult(Intent(this@MainActivity, HouseStyleChoiceActivity::class.java), REQUEST_OPRATION)
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onDestroy() {
        mUdpTask?.cancle()
        EventBus.getDefault().unregister(this)
        stopFlushRssi()
        super.onDestroy()
    }

    fun getDevices(): ArrayList<WifiDevice> {

        if (App.sTest) {
            val result = ArrayList<WifiDevice>(6)
            for (i in 0 until 4) {
                var mac: String? = ""
                for (j in 0 until 5) {
                    mac += String.format("%02x", (Math.random() * 0xff).toByte()).toUpperCase()
                    mac += ":"
                }
                mac += String.format("%02x", (Math.random() * 0xff).toByte()).toUpperCase()

                val d = WifiDevice(App.TYPE_DEVICE_CLIENT, WifiDevice.toStringIp((Math.random() * Integer.MAX_VALUE).toInt()),
                        mac, getString(R.string.client) + i, 0)
                d.rssi = Math.min((Math.random() * 80 - 120).toInt(), App.MAX_RSSI.toInt()).toByte()
                result.add(d)
            }
            run {
                val wifiinfo = App.sInstance.wifiInfo
                result.add(0, WifiDevice(App.TYPE_DEVICE_PHONE, WifiDevice.toStringIp(wifiinfo.ipAddress), wifiinfo.macAddress,
                        getString(R.string.my_phone), 0))
                result.get(0).rssi = wifiinfo.rssi.toByte()

                val dhcp = App.sInstance.dhcpInfo
                result.add(0, WifiDevice(App.TYPE_DEVICE_WIFI, WifiDevice.toStringIp(dhcp.ipAddress),
                        wifiinfo.bssid, getString(R.string.wifi), 0))
                result.get(0).rssi = 100.toByte()

            }


            return result;
        }

        return intent.getParcelableArrayListExtra(Intent.EXTRA_DATA_REMOVED)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != AppCompatActivity.RESULT_OK)
            return

        if (requestCode == REQUEST_OPRATION) {
            resetHouseStructure()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true) //在ui线程执行
    fun onDataSynEvent(event: GetClientResponse) {
        Log.e(javaClass.simpleName, "event---->" + event.devices)
        StartService.startServcie(this, StartService.ACTION_LOAD_MASTER)
        StartService.startServcie(this, StartService.ACTION_LOAD_DEVICE_STATUS)

        if (event.devices == null || event.devices.isEmpty()) {
            mArrayDevices.clear()
            deviceAdapter.notifyDataSetChanged()
            return
        }
        mArrayDevices.addAll(event.devices)
        deviceAdapter.notifyDataSetChanged()

        if (mProgressDialog != null) {
            mProgressDialog?.dismiss()
            mProgressDialog = null
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true) //在ui线程执行
    fun onDataSynEvent(event: GetClientStatusResponse) {
        var isChanged = false
        if (event.devices != null)
            for (i in mArrayDevices) loop@ {
                for (j in event.devices) {
                    if (i.mac.equals(j.mac)) {
                        i.eat(j)
                        deviceAdapter.notifyDataSetChanged()
                        isChanged = true
                        break
                    }
                }
            }

        for (device in mArrayDevices) {
            Log.e(javaClass.simpleName, "==>>>mac = ${device.mac} rssi = ${device.rssi}")
        }
        if (isChanged)
            layout_apartment.refresh()
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true) //在ui线程执行
    fun onDataSynEvent(event: WifiDevice) {
        for (d in mArrayDevices) {
            if (d.mac.equals(event.mac)) {
                d.eat(event)
                deviceAdapter.notifyDataSetChanged()
                layout_apartment.refresh()
                return
            }
        }

        mArrayDevices.add(event)
        layout_apartment.refresh()
        deviceAdapter.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        android.os.Process.killProcess(android.os.Process.myPid())
        super.onBackPressed()
        finish()
    }

    override fun onPageScrollStateChanged(p0: Int) {
//        Log.e(javaClass.simpleName, "onPageScrollStateChanged$p0")
    }

    override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
//        Log.e(javaClass.simpleName, "onPageScrolled$p0,$p1,$p2")
    }

    override fun onPageSelected(p0: Int) {
//        Log.e(javaClass.simpleName, "onPageSelected$p0")
    }

    private var flashCountTimer: CountDownTimer? = null
    /**test**/
    private fun startFlushRssi() {
        if (flashCountTimer == null)
            flashCountTimer = object : CountDownTimer(System.currentTimeMillis(), 5000) {
                override fun onFinish() {
                    System.err.println("onFinish")
                    finish()
                }

                override fun onTick(p0: Long) {
                    System.err.println("refreshed")


                    if (App.sTest) {
                        for (device in mArrayDevices) {
                            if (device.type == App.TYPE_DEVICE_CLIENT) {
                                device.rssi = (device.rssi + Math.random() * 10 - 5).toByte()
                                if (device.rssi < App.MIN_RSSI)
                                    device.rssi = App.MIN_RSSI
                                else if (device.rssi > App.MAX_RSSI)
                                    device.rssi = App.MAX_RSSI
                            } else if (device.rssi == App.TYPE_DEVICE_PHONE) {
                                device.rssi = App.sInstance.wifiInfo.rssi.toByte()
                            }
                            layout_apartment.refresh()
                        }
                        val response = GetClientStatusResponse(ByteArray(2), mArrayDevices);

                        EventBus.getDefault().postSticky(response);
                    } else {
                        StartService.startServcie(this@MainActivity, StartService.ACTION_LOAD_DEVICE_STATUS)
                    }
                }

            }
        flashCountTimer?.start()
    }

    private fun stopFlushRssi() {
        flashCountTimer?.cancel()
    }

    private fun initViewPager() {
        deviceAdapter.setOnItemClickListener(object : DeviceViewPagerAdapter.OnItemClickListener {
            override fun onItemClick(device: WifiDevice, position: Int) {
                val intent = Intent(this@MainActivity, DeviceDetailActivity::class.java)
                intent.putExtra(Intent.EXTRA_DATA_REMOVED, device)
                System.err.println(device.toString())
                startActivity(intent)
            }

            override fun onItemLongClick(view: View, device: WifiDevice, position: Int): Boolean {
                viewPager.onLongPress(view, device)
                return true
            }

        })
        viewPager.setAdapter(deviceAdapter)
        viewPager.addOnPageChangeListener(this)
        viewPager.setDropListener(object : DragViewPager.OnDragDropListener<WifiDevice> {
            override fun onStartDrag() {
                layout_apartment.setDraggedRectShow(true)
                layout_apartment.postInvalidate()
            }

            override fun onDrop(t: WifiDevice?, x: Int, y: Int) {
                System.err.println("ondropd[${t.toString()}],($x,$y)")
                layout_apartment.setDraggedRectShow(false)
                layout_apartment.addDevice(t, x, y)
            }
        })
    }

    /**刷新房屋构造**/
    private fun resetHouseStructure() {
        initHouseStructure(Preferences.getIntance().houseStyle)
    }

    /**重新设置房屋布局图*/
    private fun initHouseStructure(index: Int) {
        val filename = resources.getStringArray(R.array.pathOfHouseStyle)[index]
        val content = FileUtils.getTextFromAssets(this, filename, App.CHARSET)
        var housedata = HouseData(content)
        layout_apartment.setHouseData(housedata)
    }
}
