package org.chenxinwen.micontacts;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ComfortMsgActivity extends AppCompatActivity {

    public ListView listView;
    private List<String> list;
    private ArrayAdapter<String> aa;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comfort_msg);

        //Get Intent
        Intent curr_intent=getIntent();
        final String phone= curr_intent.getStringExtra("phone");
        Log.d("Greg's ComfortMsgAct",phone);


        listView= (ListView) findViewById(R.id.listView);
        list=new ArrayList<String>();
        list.add("青山绿水连海平，翠荫香浓步微风，牵挂思念几点浓，舞动祝福半天红。休闲自在听鸟语，逍遥前行追宁境，祝你好梦清浪远，涨落胜似湖水澄；朋友好心情哟！");
        list.add("不时常的交流并没有使我们的关系边的那么冷，因为我们的始终关心着。最主要的，记住了身体是自己的，健康，开心的，我就能感觉到你的快乐！");
        list.add("时光荏苒，我在您温馨甜美的笑容中一天天长大，您就是我的太阳，把一缕缕阳光洒在我的心上，从没有为您写过什么，也没对您说过什么感谢的话，但您对我的关心和照料，点点滴滴都在心头。我最亲爱最想感谢的妈妈，在这特别的日子里，送上我迟来的关心，“你辛苦了，我永远爱你！”");
        list.add("每一天的这个时候我就会想起你，美丽的身影，动人的微笑，甜蜜的言语，还有你那温暖的心，我好像在回到你的身边，爱抚你，亲你，关心你。");
        list.add("哥们，工作累了，歇歇脚，让压力藏的藏、跑的跑，鸡飞狗跳；心情烦了，微微笑，让烦恼躲的躲、逃的逃，鬼哭狼嚎；切记身体健康最为重要。");
        list.add("看着我们渐渐老去的爸妈，心里面有没有丝丝的辛酸，趁着他们都还健在，多一点电话，多一点关心，多一点问候，多一点陪伴，多一点回家。");
        list.add("一条短短的短信，表达我长长的思念；一句淡淡的问侯，寄托我暖暖的关心。祝：十一快乐、幸福。");
        list.add("不联系不代表忘记，不看你不代表漠视，不想你其实不容易，关心你才是必须，和你在一起的日子定是我最美好的回忆，朋友，保重身体！");
        list.add("总有一些事在心里，总有一些朋友关心你，朋友，一切安好，如果心里有事，不妨向我诉说，多多联系。愿天下间所有朋友快乐幸福，没有烦恼。");

        aa=new ArrayAdapter<String>(this,R.layout.list_item,list);
        listView.setAdapter(aa);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(ComfortMsgActivity.this,""+position,Toast.LENGTH_SHORT).show();
                String number=phone;
                Uri uri = Uri.parse("smsto:"+number);
                Intent it = new Intent(Intent.ACTION_SENDTO, uri);
                it.putExtra("sms_body", list.get(position).toString());
                startActivity(it);
            }
        });
    }


}
