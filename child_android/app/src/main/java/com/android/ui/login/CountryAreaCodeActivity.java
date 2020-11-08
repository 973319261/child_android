package com.android.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.android.widgets.SideBar;
import com.koi.chat.R;
import com.android.ui.BaseActivity;
import com.android.adapter.CountryAreaCodeAdapter;
import com.android.core.AppService;
import com.android.model.CountryAreaCode;
import com.android.model.MessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.List;

/**
 * 区域代码页面
 */
@ContentView(R.layout.activity_country_area_code)
public class CountryAreaCodeActivity extends BaseActivity implements CountryAreaCodeAdapter.CountryAreaCodeListener {
    @ViewInject(R.id.listview)
    private ListView listView;
    @ViewInject(R.id.sideBar)
    private SideBar sideBar;
    private CountryAreaCodeAdapter countryAreaCodeAdapter;

    @Override
    protected void initNaviHeadView() {
        super.initNaviHeadView();
        addTextViewByIdAndStr(R.id.navi_title_txt, getString(R.string.text_area_code_title));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        initNaviHeadView();

        countryAreaCodeAdapter = new CountryAreaCodeAdapter(this);
        countryAreaCodeAdapter.setCountryAreaCodeListener(this);
        listView.setAdapter(countryAreaCodeAdapter);
        sideBar.setTypeEnum(SideBar.TypeEnum.AreaCode);
        sideBar.setListView(listView);
        showDialog();
        x.task().run(new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault().post(new MessageEvent(-111, AppService.getCountryAreaCodeDatas()));
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void initDatas(MessageEvent messageEvent) {
        if (messageEvent.getMsgType() == -111) {
            closeDialog();
            countryAreaCodeAdapter.setup((List<CountryAreaCode>) messageEvent.getPayload());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onPicked(CountryAreaCode countryAreaCode) {
        Intent intent = new Intent();
        intent.putExtra("areaCode", countryAreaCode.areaCode);
        setResult(RESULT_OK, intent);
        goBack();
    }
}
