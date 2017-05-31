package org.chenxinwen.micontacts;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2017/5/30.
 */

public class AddTag extends Activity {

    private static final int RESULT_TAG_CODE = 3;
    private Button bt_add;
    private Button bt_cancel;
    private ArrayList<String> label_list = new ArrayList<>();
    private DBnew db = new DBnew(this);

    private FlowLayout flowLayout;//上面的flowLayout
    private TagFlowLayout allFlowLayout;//所有标签的TagFlowLayout

    private List<String> all_label_List = new ArrayList<>();//所有标签列表
    final List<TextView> labels = new ArrayList<>();//存放标签
    final List<Boolean> labelStates = new ArrayList<>();//存放标签状态
    final Set<Integer> set = new HashSet<>();//存放选中的
    private TagAdapter<String> tagAdapter;//标签适配器
    private LinearLayout.LayoutParams params;
    private EditText editText;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_tag);
        Intent testintent = getIntent();
        label_list= getIntent().getStringArrayListExtra("label_list");

        bt_cancel = (Button) findViewById(R.id.cancel);
        bt_add = (Button) findViewById(R.id.add);
        
        bt_cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                finish();
            }

        });

        bt_add.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent =new Intent();

                ArrayList<String> newList = new ArrayList<String>();
                for(TextView textView:labels)
                {
                    Log.d("system.out return:",textView.getText().toString());
                    newList.add(textView.getText().toString());
                }
                intent.putStringArrayListExtra("label_list", newList);
                setResult(RESULT_TAG_CODE,intent);
                finish();
            }

        });
        initView();
        initData();
        initEdittext();
        initAllLeblLayout();
    }
    /**
     * 初始化View
     */
    private void initView() {
        flowLayout = (FlowLayout) findViewById(R.id.id_flowlayout);
        allFlowLayout = (TagFlowLayout) findViewById(R.id.id_flowlayout_two);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(20, 20, 20, 20);
        flowLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String editTextContent = editText.getText().toString();
                if (TextUtils.isEmpty(editTextContent)) {
                    tagNormal();
                } else {
                    addLabel(editText);
                }
            }
        });
    }
    /**
     * 初始化数据
     */
    private void initData(){
/*        //初始化上面标签
        label_list.add("同事");
        label_list.add("亲人");
        label_list.add("同学");
        label_list.add("朋友");
        label_list.add("知己");
        //初始化下面标签列表
        all_label_List.addAll(label_list);
        all_label_List.add("异性朋友");
        all_label_List.add("高中同学");
        all_label_List.add("大学同学");
        all_label_List.add("社会朋友");*/
        ArrayList<String> tags = db.getAllTag();
        Set<String> set=new HashSet<String>();
        set.addAll(tags);
        set.addAll(label_list);
        for (String str : set) {
            System.out.println(str);
        }
        all_label_List.addAll(set);
        for (String str : all_label_List) {
            System.out.println("label:"+str);
        }
        for (int i = 0; i < label_list.size() ; i++) {
            editText = new EditText(getApplicationContext());//new 一个EditText
            editText.setText(label_list.get(i));
            addLabel(editText);//添加标签
        }

    }

    /**
     * 初始化默认的添加标签
     */
    private void initEdittext(){
        editText = new EditText(getApplicationContext());
        editText.setHint("添加标签");
        //设置固定宽度
        editText.setMinEms(4);
        editText.setTextSize(12);
        //设置shape
        editText.setBackgroundResource(R.drawable.label_add);
        editText.setHintTextColor(Color.parseColor("#b4b4b4"));
        editText.setTextColor(Color.parseColor("#000000"));
        editText.setLayoutParams(params);
        //添加到layout中
        flowLayout.addView(editText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tagNormal();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    /**
     * 初始化所有标签列表
     */
    private void initAllLeblLayout() {
        //初始化适配器
        tagAdapter = new TagAdapter<String>(all_label_List) {
            @Override
            public View getView(FlowLayout parent, int position, String s) {
                TextView tv = (TextView) getLayoutInflater().inflate(R.layout.flag_adapter,
                        allFlowLayout, false);
                tv.setText(s);
                return tv;
            }
        };

        allFlowLayout.setAdapter(tagAdapter);

        //根据上面标签来判断下面的标签是否含有上面的标签
        for (int i = 0; i < label_list.size(); i++) {
            for (int j = 0; j < all_label_List.size(); j++) {
                if (label_list.get(i).equals(
                        all_label_List.get(j))) {
                    tagAdapter.setSelectedList(i);//设为选中
                }
            }
        }
        tagAdapter.notifyDataChanged();


        //给下面的标签添加监听
        allFlowLayout.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
            @Override
            public boolean onTagClick(View view, int position, FlowLayout parent) {
                if (labels.size() == 0) {
                    editText.setText(all_label_List.get(position));
                    addLabel(editText);
                    return false;
                }
                tagNormal();
                List<String> list = new ArrayList<>();
                for (int i = 0; i < labels.size(); i++) {
                    list.add(labels.get(i).getText().toString());
                }
                //如果上面包含点击的标签就删除
                if (list.contains(all_label_List.get(position))) {
                    for (int i = 0; i < list.size(); i++) {
                        if (all_label_List.get(position).equals(list.get(i))) {
                            flowLayout.removeView(labels.get(i));
                            labels.remove(i);
                        }
                    }

                } else {
                    editText.setText(all_label_List.get(position));
                    addLabel(editText);
                }

                return false;
            }
        });

        //已经选中的监听
        allFlowLayout.setOnSelectListener(new TagFlowLayout.OnSelectListener() {

            @Override
            public void onSelected(Set<Integer> selectPosSet) {
                set.clear();
                set.addAll(selectPosSet);
            }
        });
    }

    /**
     * 添加标签
     * @param editText
     * @return
     */
    private boolean addLabel(EditText editText) {
        String editTextContent = editText.getText().toString();
        //判断输入是否为空
        if (editTextContent.equals(""))
            return true;
        //判断是否重复
        for (TextView tag : labels) {
            String tempStr = tag.getText().toString();
            if (tempStr.equals(editTextContent)) {
                editText.setText("");
                editText.requestFocus();
                return true;
            }
        }
        //添加标签
        final TextView temp = getTag(editText.getText().toString());
        labels.add(temp);
        labelStates.add(false);
        //添加点击事件，点击变成选中状态，选中状态下被点击则删除
        temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int curIndex = labels.indexOf(temp);
                if (!labelStates.get(curIndex)) {
                    //显示 ×号删除
                    temp.setText(temp.getText() + " ×");
                    temp.setBackgroundResource(R.drawable.label_del);
                    temp.setTextColor(Color.parseColor("#ffffff"));
                    //修改选中状态
                    labelStates.set(curIndex, true);
                } else {
                    delByTest(temp.getText().toString());
                    flowLayout.removeView(temp);
                    labels.remove(curIndex);
                    labelStates.remove(curIndex);
                    for (int i = 0; i < label_list.size(); i++) {
                        for (int j = 0; j < labels.size(); j++) {
                            if (label_list.get(i).equals(
                                    labels.get(j).getText())) {
                                tagAdapter.setSelectedList(i);
                            }
                        }
                    }
                    tagAdapter.notifyDataChanged();
                }
            }
        });
        flowLayout.addView(temp);
        //让输入框在最后一个位置上
        editText.bringToFront();
        //清空编辑框
        editText.setText("");
        editText.requestFocus();
        return true;

    }


    /**
     * 根据字符删除标签
     * @param text
     */
    private void delByTest(String text) {

        for (int i = 0; i < all_label_List.size(); i++) {
            String a = all_label_List.get(i) + " ×";
            if (a.equals(text)) {
                set.remove(i);
            }
        }
        tagAdapter.setSelectedList(set);//重置选中的标签

    }


    /**
     * 标签恢复到正常状态
     */
    private void tagNormal() {
        //输入文字时取消已经选中的标签
        for (int i = 0; i < labelStates.size(); i++) {
            if (labelStates.get(i)) {
                TextView tmp = labels.get(i);
                tmp.setText(tmp.getText().toString().replace(" ×", ""));
                labelStates.set(i, false);
                tmp.setBackgroundResource(R.drawable.label_normal);
                tmp.setTextColor(Color.parseColor("#00aa00"));
            }
        }
    }


    /**
     * 创建一个正常状态的标签
     * @param label
     * @return
     */
    private TextView getTag(String label) {
        TextView textView = new TextView(getApplicationContext());
        textView.setTextSize(12);
        textView.setBackgroundResource(R.drawable.label_normal);
        textView.setTextColor(Color.parseColor("#00aa00"));
        textView.setText(label);
        textView.setLayoutParams(params);
        return textView;
    }












}
