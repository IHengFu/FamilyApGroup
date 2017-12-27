package wifiairscout.changhong.com.wifiairscout.ui.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import java.util.ArrayList;
import java.util.List;

import wifiairscout.changhong.com.wifiairscout.App;
import wifiairscout.changhong.com.wifiairscout.R;
import wifiairscout.changhong.com.wifiairscout.model.HouseData;
import wifiairscout.changhong.com.wifiairscout.preferences.Preferences;
import wifiairscout.changhong.com.wifiairscout.utils.CommUtils;
import wifiairscout.changhong.com.wifiairscout.utils.FileUtils;

/**
 * Created by fuheng on 2017/12/15.
 */

public class HouseStyleChoiceActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {
    private Toolbar mToolBar;
    private ViewPager viewPager;
    private ArrayList<View> viewList;
    private List<String> titleList;
    private AlertDialog mDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_house_style_choice);
        mToolBar = findViewById(R.id.toolbar);
        mToolBar.setTitle(R.string.house_choice);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CommUtils.transparencyBar(this);

        initViewPager();
        setCurrentPage();
    }

    private void setCurrentPage() {
        int index = Preferences.getIntance().getHouseStyle();
        viewPager.setCurrentItem(index);
    }

    private void initViewPager() {
        viewPager = findViewById(R.id.vp_hourseChoice);
        initViewPagerContent();

        PagerAdapter pagerAdapter = new PagerAdapter() {

            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                //根据传来的key，找到view,判断与传来的参数View arg0是不是同一个视图
                return arg0 == viewList.get((int) Integer.parseInt(arg1.toString()));
            }

            @Override
            public int getCount() {
                return viewList.size();
            }

            @Override
            public void destroyItem(ViewGroup container, int position,
                                    Object object) {
                container.removeView(viewList.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(viewList.get(position));

                //把当前新增视图的位置（position）作为Key传过去
                return position;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return titleList.get(position);
            }
        };

        viewPager.setAdapter(pagerAdapter);

        viewPager.addOnPageChangeListener(this);
    }

    private void initViewPagerContent() {

        viewList = new ArrayList<View>();// 将要分页显示的View装入数组中
        titleList = new ArrayList<>();
        String[] pathOfHouseStyle = getResources().getStringArray(R.array.pathOfHouseStyle);

        for (String str : pathOfHouseStyle) {
            String content = FileUtils.getTextFromAssets(this, str, App.CHARSET);
            Log.e(getClass().getSimpleName(), content);

            HouseData housedata = new HouseData(content);

            AppCompatImageView imageView = new AppCompatImageView(this);
            imageView.setImageBitmap(FileUtils.getBitmapFromAssets(this, housedata.getBackground()));
            imageView.setCropToPadding(false);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            viewList.add(imageView);

            titleList.add(housedata.getName());
        }


//        for (int i : App.RES_ID_HOUME_PICTURE) {
//            AppCompatImageView imageView = new AppCompatImageView(this);
//            imageView.setImageResource(i);
//            imageView.setCropToPadding(false);
//            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//            viewList.add(imageView);
//        }


//        titleList = Arrays.asList(getResources().getStringArray(R.array.house_style_name));// 每个页面的Title数据
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_option, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (isChanged()) {
                    saveEdit();
                    setResult(RESULT_OK);
                }
                finish();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveEdit() {
        Preferences.getIntance().setHouseStyle(viewPager.getCurrentItem());
        setResult(RESULT_OK);
    }

    private boolean isChanged() {
        if (viewPager.getCurrentItem() == Preferences.getIntance().getHouseStyle())
            return false;
        return true;
    }

    @Override
    public void onBackPressed() {
        if (isChanged()) {
            showAlertDialog();
        } else
            super.onBackPressed();
    }

    private void showAlertDialog() {
        if (mDialog == null)
            mDialog = new AlertDialog.Builder(this).setMessage(R.string.ask_save_or_not)
                    .setPositiveButton(R.string.action_save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            saveEdit();
                            HouseStyleChoiceActivity.super.onBackPressed();
                        }
                    }).setNegativeButton(R.string.action_give_up, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            HouseStyleChoiceActivity.super.onBackPressed();
                        }
                    }).create();
        mDialog.show();
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        for (View view : viewList) {
            if (view.getScaleX() != 1) {
                view.setScaleX(1);
                view.setScaleY(1);
            }
            if (view.getScrollX() != 0 || view.getScrollY() != 0)
                view.scrollTo(0, 0);
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    }
}
