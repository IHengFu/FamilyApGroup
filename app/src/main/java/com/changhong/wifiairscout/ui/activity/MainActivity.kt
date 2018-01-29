package com.changhong.wifiairscout.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import com.changhong.wifiairscout.App
import com.changhong.wifiairscout.R
import com.changhong.wifiairscout.db.DBHelper
import com.changhong.wifiairscout.db.dao.DeviceLocationDao
import com.changhong.wifiairscout.db.dao.ProgrammeDao
import com.changhong.wifiairscout.db.data.DeviceLocation
import com.changhong.wifiairscout.db.data.ProgrammeGroup
import com.changhong.wifiairscout.interfaces.OnItemDragListener
import com.changhong.wifiairscout.model.HouseData
import com.changhong.wifiairscout.model.MessageData
import com.changhong.wifiairscout.model.MessageDataFactory
import com.changhong.wifiairscout.model.WifiDevice
import com.changhong.wifiairscout.model.response.*
import com.changhong.wifiairscout.preferences.Preferences
import com.changhong.wifiairscout.service.StartService
import com.changhong.wifiairscout.task.GenericTask
import com.changhong.wifiairscout.task.TaskListener
import com.changhong.wifiairscout.task.TaskResult
import com.changhong.wifiairscout.task.UDPTask
import com.changhong.wifiairscout.ui.adapter.DeviceViewPagerAdapter
import com.changhong.wifiairscout.ui.view.DefaultInputDialog
import com.changhong.wifiairscout.ui.view.DragViewGroup
import com.changhong.wifiairscout.ui.view.DragViewPager
import com.changhong.wifiairscout.utils.CommUtils
import com.changhong.wifiairscout.utils.FileUtils
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : BaseActivtiy(), ViewPager.OnPageChangeListener, View.OnClickListener {

    private val viewPager: DragViewPager<WifiDevice> by lazy { findViewById<DragViewPager<WifiDevice>>(R.id.vp_device) }
    private val deviceAdapter: DeviceViewPagerAdapter by lazy { DeviceViewPagerAdapter(this, mArrayDevices) }

    private val layout_apartment: DragViewGroup by lazy { findViewById<DragViewGroup>(R.id.layout_apartment) }

    private val mArrayDevices = ArrayList<WifiDevice>()

    private var mUdpTask: GenericTask? = null

    companion object {
        const val REQUEST_OPRATION = 2 //设置请求的code
        const val REQUEST_DEVICE_DETAIL = 8 //设置请求的code

    }

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
        if (!App.sTest) {
            startService(Intent(this, StartService::class.java))
            showProgressDialog(getString(R.string.notice_download_data), false, null)
        } else {
            mArrayDevices.addAll(getDevices())
        }

        initViewPager()
        resetHouseStructure()

        layout_apartment.setOnItemClickListener { adapterView, view1, i, l ->
            val intent = Intent(this@MainActivity, DeviceLocationDetailActivity::class.java)
            val location = view1.getTag() as DeviceLocation
            intent.putExtra(Intent.EXTRA_DATA_REMOVED, location.wifiDevice)
            intent.putExtra(Intent.EXTRA_ASSIST_INPUT_DEVICE_ID, location.mac)
            intent.putExtra(Intent.EXTRA_TEXT, location.nickName)
            startActivityForResult(intent, REQUEST_DEVICE_DETAIL)
        }

        layout_apartment.setOnItemDragListener { view, index -> StartService.startService(this@MainActivity, StartService.ACTION_LOAD_DEVICE_STATUS) }

        CommUtils.transparencyBar(this);

        findViewById<View>(R.id.btn_start_scan).setOnClickListener(this)
        findViewById<View>(R.id.btn_optimization).setOnClickListener(this)

        startFlushRssi()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_channel_set -> {
                askForChangeChannel()
                return true
            }
            android.R.id.home -> {
                startActivityForResult(Intent(this@MainActivity, OperationActivity::class.java), REQUEST_OPRATION)
                return true
            }
            R.id.action_save_programme -> {
                askForSaveProgramme()
                return true
            }
            R.id.action_load_programme -> {
                askForLoadProgramme()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        mUdpTask?.cancle()
        EventBus.getDefault().unregister(this)
        stopFlushRssi()
        DBHelper.getHelper(this).close()
        super.onDestroy()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_start_scan -> doStartScan()
            R.id.btn_optimization -> startActivity(Intent(this, ChannelConditionActivity::class.java))
        }
    }

    private fun doStartScan() {
//        StartService.startService(this@MainActivity, StartService.ACTION_LOAD_DEVICE_STATUS)
        mUdpTask = UDPTask().execute(MessageDataFactory.getAllClientStatus(), object : TaskListener<MessageData> {
            override fun getName(): String? {
                return null
            }


            override fun onPreExecute(task: GenericTask?) {
                showProgressDialog(getString(R.string.notice_get_device_status), false, null)
            }

            override fun onPostExecute(task: GenericTask?, result: TaskResult?) {
                hideProgressDialog()
            }

            override fun onProgressUpdate(task: GenericTask?, param: MessageData?) {
                val gcsr = GetClientStatusResponse(param?.msgBody)
                for (device in gcsr.devices) {
                    System.err.println("mac = ${device.mac} rssi = ${device.rssi}")
                    device.rssi = Math.min(device.rssi - 100, -20).toByte()
                    System.err.println("==>mac = ${device.mac} rssi = ${device.rssi}")
                }
                EventBus.getDefault().postSticky(gcsr)
            }

            override fun onCancelled(task: GenericTask?) {
                hideProgressDialog()
            }

        })
    }

    fun getDevices(): ArrayList<WifiDevice> {

        if (App.sTest) {
            val result = ArrayList<WifiDevice>(6)
            for (i in 0 until 4) {
                var mac: String = ""
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


            return result
        }

        return intent.getParcelableArrayListExtra(Intent.EXTRA_DATA_REMOVED)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK)
            return
        if (requestCode == REQUEST_OPRATION) {
            layout_apartment.setPointState(Preferences.getIntance().mapShowStyle == 0)
        } else if (requestCode == REQUEST_DEVICE_DETAIL) {
            val mac = data?.getStringExtra(Intent.EXTRA_ASSIST_INPUT_DEVICE_ID)
            val nickname = data?.getStringExtra(Intent.EXTRA_TEXT)
            layout_apartment.exportData().forEach { it ->
                if (it.mac.equals(mac)) {
                    it.nickName = nickname
                    layout_apartment.refresh()
                    return@forEach
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true) //在ui线程执行
    fun onDataSynEvent(event: GetClientResponse) {
        Log.e(javaClass.simpleName, "event---->" + event.devices)
        StartService.startService(this, StartService.ACTION_LOAD_MASTER)
        StartService.startService(this, StartService.ACTION_LOAD_DEVICE_STATUS)

        if (event.devices == null || event.devices.isEmpty()) {
            mArrayDevices.clear()
            deviceAdapter.notifyDataSetChanged()
            return
        }
        mArrayDevices.addAll(event.devices)
        deviceAdapter.notifyDataSetChanged()

        hideProgressDialog()
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

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true) //在ui线程执行
    fun onDataSynEvent(event: SetChannelResponse) {

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
            flashCountTimer = object : CountDownTimer(System.currentTimeMillis(), 10000) {
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
                        StartService.startService(this@MainActivity, StartService.ACTION_LOAD_DEVICE_STATUS)
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
        //TODO
//        layout_apartment.setHouseData(housedata)
    }

    private fun askForSaveProgramme() {
        val data = layout_apartment.exportData()

        if (data == null || data.isEmpty()) {
            showToast(getString(R.string.no_data))
            return
        }
        val pDao = ProgrammeDao(this)
        if (pDao.rowNums >= App.MAX_NUM_PROGRAMME) {
            showToast(getString(R.string.notice_programme_max_limit))
            return
        }

        val dialog = DefaultInputDialog(this)
        dialog.setTitle(R.string.action_save_programme)
        dialog.setOnCommitListener { dialogInstance, msg, var3 ->
            if (TextUtils.isEmpty(msg)) {
                return@setOnCommitListener
            }


            val recode = pDao.queryByName(msg)
            if (recode != null && !recode.isEmpty()) {
                showToast(getString(R.string.notice_containe_same_name))
                return@setOnCommitListener
            }

            val dDao = DeviceLocationDao(this)

            val groupId = System.currentTimeMillis()
            var avageRssi = App.MIN_RSSI;

            var sum: Int = 0
            var count = 0
            layout_apartment.exportData()?.forEach {
                it.group = groupId
                dDao.add(it)

                if (it.wifiDevice != null && it.wifiDevice?.type != App.TYPE_DEVICE_WIFI) {
                    sum = sum + it?.wifiDevice?.rssi?.toInt()!!
                    count++;
                }
            }

            if (count != 0)
                avageRssi = (sum / count).toByte();

            val programme = ProgrammeGroup(0, msg, groupId, avageRssi)
            pDao.add(programme)
            dialogInstance.dismiss()
        }
        dialog.show()
    }

    private fun askForLoadProgramme() {
        val pDao = ProgrammeDao(this)
        val all = pDao.all
        if (all == null || all.isEmpty()) {
            showToast(getString(R.string.no_data))
            return
        }
        val items = arrayOfNulls<String>(all.size)
        for (i in all.indices) {
            items[i] = "${getString(R.string.programme)}:${all[i].name}\n${getString(R.string.average)}:${all[i].rssi.toInt()} "
        }
        var choice = -1
        AlertDialog.Builder(this)
                .setTitle("${getString(R.string.action_load_programme)} (${items.size})")
                .setSingleChoiceItems(items, -1, { dialogInterface, i -> choice = i })
                .setPositiveButton(R.string.action_cancel, null)
                .setNegativeButton(R.string.action_load, { dialogInterface, i ->
                    if (choice == -1) {
                        showToast(getString(R.string.no_data))
                        return@setNegativeButton
                    }
                    loadProgramme(all[choice])
                })
                .setNeutralButton(R.string.action_delete, { dialogInterface, i ->
                    if (choice == -1) {
                        showToast(getString(R.string.no_data))
                        return@setNeutralButton
                    }
                    pDao.delete(all[choice])
                })
                .create().show()

    }

    private fun loadProgramme(programmeGroup: ProgrammeGroup) {
        val dDao = DeviceLocationDao(this)
        val recode = dDao.queryByName(programmeGroup.group)
        Log.e("==~", "==~" + recode.toString())
        if (recode != null && !recode.isEmpty())
            for (i in mArrayDevices) loop@ {
                for (j in recode) {
                    if (i.mac.equals(j.mac)) {
                        j.wifiDevice = i
                        break
                    }
                }
            }

        layout_apartment.importData(recode)


    }

    private fun askForChangeChannel() {
        val dialog = DefaultInputDialog(this)
        val NUMBER_OF_CHANNEL: IntArray
        val title: String
        if (App.sInstance.curWlanIdx.toInt() == 0) {
            NUMBER_OF_CHANNEL = resources.getIntArray(R.array.channel_5g_cn)
            val temp = StringBuilder()
            NUMBER_OF_CHANNEL.forEach { it -> temp.append(it).append(',') }
            temp.deleteCharAt(temp.length - 1)
            title = String.format(getString(R.string.title_change_channel_5g, temp.toString()))
        } else {
            title = getString(R.string.title_change_channel)
            NUMBER_OF_CHANNEL = IntArray(13)
            for (i in NUMBER_OF_CHANNEL.indices) {
                NUMBER_OF_CHANNEL[i] = i + 1
            }
        }
        dialog.setTitle(title)
        dialog.setInputType(InputType.TYPE_CLASS_NUMBER)
        dialog.setTab(R.string.tab_input_channel_number)
        dialog.setOnCommitListener { dialogInstance, msg, var3 ->
            if (msg == null || msg.length < 1 || msg.length > 2) {
                showToast(getString(R.string.title_change_channel))
                return@setOnCommitListener
            }
            val channel = Integer.parseInt(msg)
            if (App.sInstance.curWlanIdx.toInt() == 0) {
                var isCorrect = false
                NUMBER_OF_CHANNEL.forEach { it ->
                    if (channel == it) {
                        isCorrect = true
                        return@forEach
                    }
                }
                if (isCorrect) {
                    showToast(title)
                    return@setOnCommitListener
                }

            } else {
                if (channel < 1 || channel > 13) {
                    showToast(title)
                    return@setOnCommitListener
                }
            }

            dialogInstance.dismiss()

            UDPTask().execute(MessageDataFactory.setChannel(Integer.parseInt(msg), App.sInstance.curWlanIdx, App.sInstance.masterMac), object : TaskListener<BaseResponse> {
                override fun getName(): String? {
                    return null
                }

                override fun onPreExecute(task: GenericTask?) {
                    showProgressDialog(getString(R.string.request_sending), true, { dialogInterface -> task?.cancle() })
                }

                override fun onPostExecute(task: GenericTask?, result: TaskResult?) {
                    hideProgressDialog()
                    if (result != TaskResult.OK)
                        showToast(task?.exception?.message?.toString())
                    else {
                        StartService.startService(this@MainActivity, StartService.ACTION_LOAD_MASTER)
                        showToast(getString(R.string.request_send_complete))
                    }
                }

                override fun onProgressUpdate(task: GenericTask?, param: BaseResponse?) {
                }

                override fun onCancelled(task: GenericTask?) {
                    hideProgressDialog()
                }
            })
        }
        dialog.show()
    }
}
