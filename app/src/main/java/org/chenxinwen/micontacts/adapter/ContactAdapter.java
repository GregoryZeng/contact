package org.chenxinwen.micontacts.adapter;

/**
 * Created by Administrator on 2017/5/14.
 */


import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import org.chenxinwen.micontacts.R;
import org.chenxinwen.micontacts.bean.Contacts;
import org.kymjs.kjframe.KJBitmap;
import org.kymjs.kjframe.utils.KJLoger;
import org.kymjs.kjframe.widget.AdapterHolder;
import org.kymjs.kjframe.widget.KJAdapter;

import java.util.ArrayList;
import java.util.Collections;

/**
 * 列表适配器
 *
 * @author kymjs (http://www.kymjs.com/) on 9/16/15.
 */
public abstract class ContactAdapter extends KJAdapter<Contacts> implements SectionIndexer {
    private KJBitmap kjb = new KJBitmap();
    private ArrayList<Contacts> datas;
    public ContactAdapter(AbsListView view, ArrayList<Contacts> mDatas) {
        super(view, mDatas, R.layout.item_list_contact);
        datas = mDatas;
        if (datas == null) {
            datas = new ArrayList<>();
        }
        Collections.sort(datas);
    }

    @Override
    public void convert(AdapterHolder helper, Contacts item, boolean isScrolling) {
    }
    //@Override
    public void refresh(ArrayList<Contacts> datas)
    {

    }
    @Override
    public void convert(AdapterHolder holder, Contacts item, boolean isScrolling, int position) {

        holder.setText(R.id.contact_title, item.getName());
        ImageView headImg = holder.getView(R.id.contact_head);
        KJLoger.debug("itemUrl:",item.getUrl());
      /* if(item.getUrl()=="noImg")
        {
            kjb.displayCacheOrDefult(headImg, item.getUrl(), R.drawable.default_head_rect);
        }
        else
        {
            kjb.displayWithLoadBitmap(headImg, item.getUrl(), R.drawable.default_head_rect);
        }*/

        if (isScrolling) {
            //kjb.displayCacheOrDefult(headImg, "2", R.drawable.default_head_rect);
            kjb.displayWithLoadBitmap(headImg, item.getUrl(), R.drawable.default_head_rect);
        } else {
            kjb.displayWithLoadBitmap(headImg, item.getUrl(), R.drawable.default_head_rect);
        }

        TextView tvLetter = holder.getView(R.id.contact_catalog);
        TextView tvLine = holder.getView(R.id.contact_line);

        //如果是第0个那么一定显示#号
        if (position == 0) {
            if(item.getFirstChar()>'Z'||item.getFirstChar()<'A') {
                tvLetter.setVisibility(View.VISIBLE);
                tvLetter.setText("#");
                tvLine.setVisibility(View.VISIBLE);
            }
            else {
                tvLetter.setVisibility(View.VISIBLE);
                tvLetter.setText("" + item.getFirstChar());
                tvLine.setVisibility(View.VISIBLE);
            }
        }
        else{
            //如果和上一个item的首字母不同，则认为是新分类的开始
            Contacts prevData = datas.get(position - 1);
            if (item.getFirstChar() != prevData.getFirstChar()) {
                tvLetter.setVisibility(View.VISIBLE);
                tvLetter.setText("" + item.getFirstChar());
                tvLine.setVisibility(View.VISIBLE);
            } else {
                tvLetter.setVisibility(View.GONE);
                tvLine.setVisibility(View.GONE);
            }
        }
    }


    /**
     * 根据ListView的当前位置获取分类的首字母的Char ascii值
     */
    public int getSectionForPosition(int position) {
        Contacts item = datas.get(position);
        return item.getFirstChar();
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    public int getPositionForSection(int section) {
        for (int i = 0; i < getCount(); i++) {
            char firstChar = datas.get(i).getFirstChar();
            if (firstChar == section) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public Object[] getSections() {
        return null;
    }

}

