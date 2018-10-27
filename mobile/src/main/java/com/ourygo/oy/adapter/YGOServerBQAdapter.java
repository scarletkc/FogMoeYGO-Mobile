package com.ourygo.oy.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import cn.garymb.ygomobile.bean.ServerInfo;
import cn.garymb.ygomobile.lite.R;

public class YGOServerBQAdapter extends BaseQuickAdapter<ServerInfo,BaseViewHolder> {

    public YGOServerBQAdapter(@Nullable List<ServerInfo> data) {
        super(R.layout.ygo_server_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, ServerInfo item) {
        helper.setText(R.id.tv_name,item.getName());

    }
}