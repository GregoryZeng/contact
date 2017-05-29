package org.chenxinwen.micontacts;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import org.chenxinwen.micontacts.adapter.Call_for_one;
import org.chenxinwen.micontacts.bean.RecordEntity;

import java.util.ArrayList;
import java.util.List;


public class call_cluster extends AppCompatActivity {
    private List<RecordEntity> EntityList=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_cluster);
        Intent intent=getIntent();
        EntityList=(List<RecordEntity>)intent.getSerializableExtra("List");

        //getCallLog(phoneNumber);
        RecyclerView recyclerView=(RecyclerView) findViewById(R.id.recyclerView_for_one);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        Call_for_one adapter=new Call_for_one(EntityList);
        recyclerView.setAdapter(adapter);

    }

}
