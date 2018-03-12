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
import com.changhong.wifiairscout.App
import com.changhong.wifiairscout.R
import com.changhong.wifiairscout.db.DBHelper
import com.changhong.wifiairscout.db.dao.DeviceLocationDao
import com.changhong.wifiairscout.db.dao.ProgrammeDao
import com.changhong.wifiairscout.db.data.DeviceLocation
import com.changhong.wifiairscout.db.data.ProgrammeGroup
import com.changhong.wifiairscout.interfaces.OnItemDragListener
import com.changhong.wifiairscout.model.DualBandInfo
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
import com.changhong.wifiairscout.ui.view.UserControlDialog
import com.changhong.wifiairscout.utils.CommUtils
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : BaseActivtiy(), ViewPager.OnPageChangeListener, View.OnClickListener {

    private val viewPager: DragViewPager<WifiDevice> by lazy { findViewById<DragViewPager<WifiDevice>>(R.id.vp_device) }
    private val deviceAdapter: DeviceViewPagerAdapter by lazy { DeviceViewPagerAdapter(this, mArrayDevices) }

    private val layout_apartment: DragViewGroup by lazy { findViewById<DragViewGroup>(R.id.layout_apartment) }

    val mArrayDevices = ArrayList<WifiDevice>()

    private var mUdpTask: GenericTask? = null

    private var isFirst = true

    companion object {
        const val REQUEST_OPRATION = 2 //设置请求的code
        const val REQUEST_DEVICE_DETAIL = 8 //设置请求的code

        var sIntance: MainActivity? = null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sIntance = this

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.mipmap.ic_setting)
        EventBus.getDefault().register(this)

        mArrayDevices.add(intent.getParcelableExtra(Intent.EXTRA_DATA_REMOVED))
        if (!App.sTest) {
//            StartService.startService(this, StartService.ACTION_LOAD_MASTER)
            StartService.startService(this, StartService.ACTION_LOAD_DEVICE_STATUS)
            showProgressDialog(getString(R.string.notice_download_data), false, null)
        } else {//test
            mArrayDevices.addAll(getDevices())
        }

        initViewPager()
        CommUtils.transparencyBar(this)

        layout_apartment.setOnItemClickListener { adapterView, view1, _, _ ->
            val intent = Intent(this@MainActivity, DeviceLocationDetailActivity::class.java)
            val location = view1.getTag() as DeviceLocation
            intent.putExtra(Intent.EXTRA_DATA_REMOVED, location.wifiDevice)
            intent.putExtra(Intent.EXTRA_ASSIST_INPUT_DEVICE_ID, location.mac)
            intent.putExtra(Intent.EXTRA_TEXT, location.nickName)
            startActivityForResult(intent, REQUEST_DEVICE_DETAIL)
        }

        layout_apartment.setOnItemDragListener(object : OnItemDragListener {
            override fun onAdd(device: WifiDevice?) {
                deviceAdapter.mHashShown.put(device?.mac!!, true)
                deviceAdapter.notifyDataSetChanged()
            }

            override fun onDelete(device: WifiDevice?) {
                deviceAdapter.mHashShown.remove(device?.mac)
                deviceAdapter.notifyDataSetChanged()
            }

            override fun onMove(view: View?, device: WifiDevice?) {
            }

            override fun onDroped(view: View?, device: WifiDevice?) {
                if (device != null && device.type == App.TYPE_DEVICE_WIFI) {
//                    askForSaveProgramme()
                }
            }
        })


        findViewById<View>(R.id.btn_start_scan).setOnClickListener(this)
        findViewById<View>(R.id.btn_optimization).setOnClickListener(this)

        startFlushRssi()

        isFirst = true

        askInputUser()
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
            return
        }

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
                if (gcsr.devices != null)
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

    private fun getDevices(): ArrayList<WifiDevice> {

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
                        mac, getString(R.string.client) + i)
                d.rssi = Math.min((Math.random() * 80 - 120).toInt(), App.MAX_RSSI.toInt()).toByte()
                result.add(d)
            }
            run {
                val wifiinfo = App.sInstance.wifiInfo
                val frequency = wifiinfo.frequency;

                if (frequency < 5000) {
                    App.sInstance.curWlanIdx = 1
                    App.sInstance.curChannel = ((frequency - 2407) / 5).toByte()
                    if ((frequency - 2407) % 5 > 3)
                        App.sInstance.curChannel++;
                } else {
                    val arrFrequency = resources.getIntArray(R.array.channel_5g_cn_frequency)
                    var min = Int.MAX_VALUE
                    for (i in arrFrequency.indices) {
                        if (Math.abs(frequency - arrFrequency[i]) < min) {
                            min = Math.abs(frequency - arrFrequency[i])
                            App.sInstance.curChannel = i.toByte()
                        }
                    }
                    App.sInstance.curWlanIdx = 0
                }

                result.add(0, WifiDevice(App.TYPE_DEVICE_PHONE, WifiDevice.toStringIp(wifiinfo.ipAddress), wifiinfo.macAddress,
                        getString(R.string.my_phone)))
                result.get(0).rssi = wifiinfo.rssi.toByte()

                val wc = WifiDevice(App.TYPE_DEVICE_CONNECT, "127.00.00.1", "ff:ff:ff:ff:ff:ff", "中继器")
                result.add(0, wc)

//                val dhcp = App.sInstance.dhcpInfo
//                result.add(0, WifiDevice(App.TYPE_DEVICE_WIFI, WifiDevice.toStringIp(dhcp.ipAddress),
//                        wifiinfo.bssid, getString(R.string.wifi)))
//                result.get(0).rssi = 100.toByte()
//
//                val arrDualBInfo = ArrayList<DualBandInfo>()
//                arrDualBInfo.add(DualBandInfo(0, 44, 0, 0, "test1", 0, 0, null))
//                arrDualBInfo.add(DualBandInfo(1, 12, 0, 0, "test1", 0, 0, null))
//                result.get(0).dual_band = 1
//                result.get(0).arrayDualBandInfo = arrDualBInfo
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

                    //add ask
//                    if (it.type == App.TYPE_DEVICE_WIFI)
//                        askForSaveProgramme(nickname)
                    return@forEach
                }
            }
        }
    }

//    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true) //在ui线程执行
//    fun onDataSynEvent(event: GetClientResponse) {
//        Log.e(javaClass.simpleName, "event---->" + event.devices)
//
//        if (event.devices != null) {
//            //clean client
//            val listToDelete = ArrayList<WifiDevice>()
//            mArrayDevices.forEach { device ->
//                if (App.TYPE_DEVICE_CLIENT == device.type || App.TYPE_DEVICE_PHONE == device.type) {
//                    var isContain = false
//                    if (!event.devices.isEmpty()) {
//                        for (device1 in event.devices) {
//                            if (device1.equals(device)) {
//                                isContain = true
//                                device.eat(device1)
//                                break
//                            }
//                        }
//                    }
//                    if (!isContain)
//                        listToDelete.add(device)
//                }
//            }
//            mArrayDevices.removeAll(listToDelete)
//
//            // check and add new
//            val listToAdd = getMoreObject(event.devices, mArrayDevices)
//            if (listToAdd != null && !listToAdd.isEmpty()) {
//                mArrayDevices.addAll(listToAdd)
//            }
//
//            layout_apartment.updataOfflineDevices(listToAdd, listToDelete)
//
//            deviceAdapter.notifyDataSetChanged()
//        }
//        StartService.startService(this, StartService.ACTION_LOAD_DEVICE_STATUS)
//    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true) //在ui线程执行
    fun onDataSynEvent(event: GetClientStatusResponse) {
//        var isChanged = false
//        if (event.devices != null)
//            for (i in mArrayDevices) loop@ {
//                for (j in event.devices) {
//                    if (i.mac.equals(j.mac)) {
//                        i.eat(j)
//                        deviceAdapter.notifyDataSetChanged()
//                        isChanged = true
//                        break
//                    }
//                }
//            }
        if (event.devices != null) {
            //clean client
            val listToDelete = ArrayList<WifiDevice>()
            mArrayDevices.forEach { device ->
                if (App.TYPE_DEVICE_CLIENT == device.type || App.TYPE_DEVICE_PHONE == device.type) {
                    var isContain = false
                    if (!event.devices.isEmpty()) {
                        for (device1 in event.devices) {
                            if (device1.equals(device)) {
                                isContain = true
                                device.eat(device1)
                                break
                            }
                        }
                    }
                    if (!isContain)
                        listToDelete.add(device)
                }
            }
            mArrayDevices.removeAll(listToDelete)

            // check and add new
            val listToAdd = getMoreObject(event.devices, mArrayDevices)
            if (listToAdd != null && !listToAdd.isEmpty()) {
                mArrayDevices.addAll(listToAdd)
            }

            layout_apartment.updataOfflineDevices(listToAdd, listToDelete)

            deviceAdapter.notifyDataSetChanged()
        }

        for (device in mArrayDevices) {
            Log.e(javaClass.simpleName, "==>>>mac = ${device.mac} rssi = ${device.rssi}")
        }
//        if (isChanged)
        layout_apartment.refresh()

        if (isFirst) {
            hideProgressDialog()
            isFirst = false;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true) //在ui线程执行
    fun onDataSynEvent(event: GetMasterResponse) {
        if (event.master != null) {
            val device = event.master
            for (d in mArrayDevices) {
                if (d.mac.equals(device.mac)) {
                    d.eat(device)
                    deviceAdapter.notifyDataSetChanged()
                    layout_apartment.refresh()
                    StartService.startService(this, StartService.ACTION_LOAD_DEVICE)
                    return
                }
            }

            mArrayDevices.add(device)
            layout_apartment.refresh()
            deviceAdapter.notifyDataSetChanged()
        }
//        StartService.startService(this, StartService.ACTION_LOAD_DEVICE)
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
            flashCountTimer = object : CountDownTimer(System.currentTimeMillis(), 20000) {
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
                        val response = GetClientStatusResponse(ByteArray(2), mArrayDevices)

                        EventBus.getDefault().postSticky(response)
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
                if (device.type == App.TYPE_DEVICE_CONNECT) {
                    showToast("中继器功能敬请期待……")
                    return
                }
                val intent = Intent(this@MainActivity, DeviceDetailActivity::class.java)
                intent.putExtra(Intent.EXTRA_DATA_REMOVED, device)
                startActivity(intent)
            }

            override fun onItemLongClick(view: View, device: WifiDevice, position: Int): Boolean {
                if (device.type == App.TYPE_DEVICE_CONNECT) {
                    showToast("中继器功能敬请期待……")
                    return false
                }
                if (deviceAdapter.isItemEnable(device))
                    return false;

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

            override fun onDrop(t: WifiDevice?, view: View?, x: Int, y: Int) {
                System.err.println("ondropd[${t.toString()}],($x,$y)")
                layout_apartment.setDraggedRectShow(false)
                layout_apartment.addDevice(t, x, y)
            }
        })
    }

    private fun askForSaveProgramme() {
        askForSaveProgramme(null)
    }

    private fun askForSaveProgramme(str: CharSequence?) {
        val data = layout_apartment.exportData()

        if (data == null || data.isEmpty()) {
            showToast(getString(R.string.no_data))
            return
        }

        run {
            data?.forEach { it ->
                when (it.type) {
                    App.TYPE_DEVICE_CLIENT, App.TYPE_DEVICE_PHONE -> return@run;
                }
            }
            showToast(getString(R.string.notice_at_least_one_client))
            return
        }

        val pDao = ProgrammeDao(this)
        val allrecodes = pDao.queryByUserName(App.sInstance.guestName)
        val recodesNums = allrecodes?.size ?: 0
        if (recodesNums >= App.MAX_NUM_PROGRAMME) {
            showToast(getString(R.string.notice_programme_max_limit))
            return
        }

        val dialog = DefaultInputDialog(this)
        dialog.setTitle(R.string.action_save_programme)
        if (str != null)
            dialog.setMessage(str)
        dialog.setTab(App.sInstance.guestName + ":")
        dialog.setOnCommitListener { dialogInstance, msg, var3 ->
            if (TextUtils.isEmpty(msg)) {
                return@setOnCommitListener
            }

            val recode = pDao.query(App.sInstance.guestName, msg)
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

            val programme = ProgrammeGroup(0, msg, groupId, avageRssi, App.sInstance.guestName)
            pDao.add(programme)
            dialogInstance.dismiss()
        }
        dialog.show()
    }

    private fun askForLoadProgramme() {
        val pDao = ProgrammeDao(this)
        val all = pDao.queryByUserName(App.sInstance.guestName)
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
                .setTitle("${getString(R.string.action_load_programme)} (${getString(R.string.username)}:${App.sInstance.guestName},${getString(R.string.count_of_programme)}:${items.size})")
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
        val recode = dDao.queryByProgrammeId(programmeGroup.group)
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

    fun askInputUser() {
        val dialog = UserControlDialog(this)
        dialog.setTitle("请输入当前用户名称")
        dialog.setOnCommitListener { dialoginterface, string, var3 ->
            if (TextUtils.isEmpty(string)) {
                showToast(getString(R.string.no_data))
                return@setOnCommitListener
            }

            App.sInstance.guestName = string
            dialoginterface.dismiss()
        }

        run {
            val pDao = ProgrammeDao(this)
            val num = pDao.rowNums
            val usernames = pDao.userNames
            dialog.setChoices(usernames)
        }

        dialog.setCancelable(false)
        dialog.show()
    }

    private fun <T> getMoreObject(list1: List<T>, list2: List<T>): List<T>? {
        if (list1 == null || list1.isEmpty())
            return list2

        if (list2 == null || list2.isEmpty())
            return null

        val result = ArrayList<T>()
        for (item2 in list1) {
            val isContain = list2.any { item2!!.equals(it) }
            if (!isContain)
                result.add(item2)
        }

        if (result.isEmpty())
            return null
        else
            return result
    }
}
