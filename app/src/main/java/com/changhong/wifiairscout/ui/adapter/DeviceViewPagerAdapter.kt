package com.changhong.wifiairscout.ui.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import android.support.v4.view.ViewPager
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.AppCompatTextView
import android.text.Layout
import android.view.LayoutInflater
import com.changhong.wifiairscout.App
import com.changhong.wifiairscout.R
import com.changhong.wifiairscout.model.WifiDevice
import com.changhong.wifiairscout.utils.CommUtils


/**
 * Created by fuheng on 2017/12/19.
 */

class DeviceViewPagerAdapter(val context: Context, val mList: List<WifiDevice>) : PagerAdapter(), View.OnClickListener, View.OnLongClickListener {


    private val layoutInflater: LayoutInflater

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
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any? {
        val view = layoutInflater.inflate(R.layout.item_device1, null, false)
        val imageView = view.findViewById<AppCompatImageView>(R.id.icon)
//        if (mList.get(position).rssi < -84) {
//            val drawable = container.resources.getDrawable(App.RESID_WIFI_DEVICE[mList.get(position).type.toInt()])
//            imageView.setImageDrawable(CommUtils.tintDrawable(drawable, Color.RED))
//        } else
        imageView.setImageResource(App.RESID_WIFI_DEVICE[mList.get(position).type.toInt()])
//        view.findViewById<AppCompatTextView>(R.id.text).setText(mList.get(position).name)
        view.findViewById<AppCompatTextView>(R.id.text).setText(mList.get(position).mac.substring(12))
        container.addView(view)

        view.setTag(position)

        view.setOnClickListener(this)
        view.setOnLongClickListener(this)
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
}

