package org.chenxinwen.micontacts;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
        String phone= curr_intent.getStringExtra("phone");


        listView= (ListView) findViewById(R.id.listView);
        list=new ArrayList<String>();
        list.add("1111");
        aa=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list);
        listView.setAdapter(aa);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(ComfortMsgActivity.this,""+position,Toast.LENGTH_SHORT).show();
                String number="18819251578";
                Uri uri = Uri.parse("smsto:"+number);
                Intent it = new Intent(Intent.ACTION_SENDTO, uri);
                it.putExtra("sms_body", list.get(position).toString());
                startActivity(it);
            }
        });
    }


}
