package com.example.message;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.message.network.NetworkStatus;

public class MainActivity extends AppCompatActivity {

    TextView name_Tv, content_Tv;
    Button btn1, btn2, btn3;

    Context context = this;

    //네트워크 상태 체크
    int networkStatus;
    //유저 이름
    String userName;

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        networkStatus = NetworkStatus.getConnectivityStatus(getApplicationContext());

        /**
         * APP 소개글 및 유저 이름 등록
         * **/
        content_Tv = findViewById(R.id.content);
        content_Tv.setText("APP 제목 : 1:1 & 1:N 채팅 어플 \n\n" +
                "APP 설명서 \n\n " +
                "1. 방 입장 - 원하는 방을 클릭해 방에 입장\n\n" +
                "2. 메시지 전송 - 원하는 방에 입장한 후 메시지를 입력해 전송");

        name_Tv = findViewById(R.id.userName);
        name_Tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addUserName();
            }
        });

        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);

        //1번방 입장
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (networkStatus == NetworkStatus.TYPE_NOT_CONNECTED){

                    Toast.makeText(context, "인터넷 연결 상태를 확인해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (userName == null){

                    Toast.makeText(context, "유저 이름을 생성해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                intent = new Intent(MainActivity.this, MessageActivity.class);
                intent.putExtra("roomName", "1번방");
                intent.putExtra("userName" , userName);
                startActivity(intent);

            }
        });

        //2번방 입장
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (networkStatus == NetworkStatus.TYPE_NOT_CONNECTED){

                    Toast.makeText(context, "인터넷 연결 상태를 확인해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (userName == null){

                    Toast.makeText(context, "유저 이름을 생성해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                intent = new Intent(MainActivity.this, MessageActivity.class);
                intent.putExtra("roomName", "2번방");
                intent.putExtra("userName" , userName);
                startActivity(intent);

            }
        });

        //3번방 입장
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (networkStatus == NetworkStatus.TYPE_NOT_CONNECTED){
                    Toast.makeText(context, "인터넷 연결 상태를 확인해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (userName == null){

                    Toast.makeText(context, "유저 이름을 생성해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                intent = new Intent(MainActivity.this, MessageActivity.class);
                intent.putExtra("roomName", "3번방");
                intent.putExtra("userName" , userName);
                startActivity(intent);

            }
        });

    }

    EditText userName_Edit;
    private void addUserName(){
        AlertDialog.Builder addUserDialog = new AlertDialog.Builder(this);
        addUserDialog.setTitle("유저 이름을 입력해주세요.");

        userName_Edit = new EditText(this);
        userName_Edit.setHint("유저 이름");

        addUserDialog.setView(userName_Edit);
        addUserDialog.setPositiveButton("취소", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                name_Tv.setText("클릭 후 유저 이름을 생성하세요.");
            }
        });

        addUserDialog.setNegativeButton("확인",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                userName = userName_Edit.getText().toString();

                if (userName.equals("")) {
                    Toast.makeText(context, "유저 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                name_Tv.setText(userName);
            }
        });
        addUserDialog.show();
    }
}
