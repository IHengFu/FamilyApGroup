package com.changhong.wifiairscout.ui.adapter

import android.content.Context
import android.graphics.Color
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.AppCompatTextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.changhong.wifiairscout.App
import com.changhong.wifiairscout.R
import com.changhong.wifiairscout.model.WifiDevice


/**
 * Created by fuheng on 2017/12/19.
 */

class DeviceViewPagerAdapter(val context: Context, val mList: List<WifiDevice>) : PagerAdapter(), View.OnClickListener, View.OnLongClickListener {


    private val layoutInflater: LayoutInflater

    private val mTempList = ArrayList<View>()

    val mHashShown = HashMap<String, Boolean>()

    init {
        layoutInflater = LayoutInflater.from(context)
    }

    override fun getCount(): Int {
        return mList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    /**
     * 页面宽度所占ViewPager测量宽度的权重比例，默认为1
     */
    override fun getPageWidth(position: Int): Float {
        return 1.toFloat()
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val view = `object` as View
        (container as ViewPager).removeView(view)

        mTempList.remove(view)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any? {
        val view = layoutInflater.inflate(R.layout.item_device1, null, false)
//        if (item.rssi < -84) {
//            val drawable = container.resources.getDrawable(App.RESID_WIFI_DEVICE[item.type.toInt()])
//            imageView.setImageDrawable(CommUtils.tintDrawable(drawable, Color.RED))
//        } else
        val item = mList.get(position)
        refreshView(view, item)
        container.addView(view)

        view.setTag(position)

        view.setOnClickListener(this)
        view.setOnLongClickListener(this)

        mTempList.add(view)
        return view
    }

    override fun onClick(p0: View?) {
        val position = p0?.getTag() as Int
        itemClickListener?.onItemClick(mList.get(position), position)
    }

    override fun onLongClick(p0: View?): Boolean {
        val position = p0?.getTag() as Int
        return itemClickListener?.onItemLongClick(p0, mList.get(position), position) ?: false
    }

    //点击事件接口
    interface OnItemClickListener {
        fun onItemClick(device: WifiDevice, position: Int)

        fun onItemLongClick(view: View, device: WifiDevice, position: Int): Boolean
    }

    private var itemClickListener: OnItemClickListener? = null

    //设置点击事件的方法
    fun setOnItemClickListener(itemClickListener: OnItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    fun isItemEnable(item: WifiDevice) =
            mHashShown.containsKey(item.mac) && mHashShown.get(item.mac) ?: false

    override fun notifyDataSetChanged() {
        if (mTempList != null && !mTempList.isEmpty())
            for (view in mTempList) {
                refreshView(view, mList.get(view.getTag() as Int))
            }

        super.notifyDataSetChanged()
    }

    fun refreshView(view: View, item: WifiDevice) {
        val imageView = view.findViewById<AppCompatImageView>(R.id.icon)
        imageView.setImageResource(App.RESID_WIFI_DEVICE[item.type.toInt()])
        if (isItemEnable(item)) {
            DrawableCompat.setTint(imageView.drawable, Color.YELLOW)
        } else {
            DrawableCompat.setTint(imageView.drawable, Color.WHITE)
        }

        if (item.type == App.TYPE_DEVICE_CONNECT)
            view.findViewById<AppCompatTextView>(R.id.text).setText(item.name)
        else
            view.findViewById<AppCompatTextView>(R.id.text).setText(item.mac.substring(12))
    }

}

