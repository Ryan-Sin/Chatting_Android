package com.example.message;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MessageActivity extends AppCompatActivity {

    private String HOST = "http://192.168.25.12:3000"; // IP & PORT
    private Socket nodeSocket; //Socket IO


    private EditText messageEdit; // 메시지 입력 EditText
    private Button messageBtn; // 메시지 전송 버

    private RecyclerView messageRecyclerView; //메시지 리사이클러뷰
    private MessageRecyclerAdapter messageRecyclerAdapter; //메시지 어뎁터

    private ArrayList<MessageItem> megItem; // 메시지 아이템을 담을 ArrayList

    private String roomName; // 방이름
    private String userName; // 로그인한 유저 아이디

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        /**
         * MainActivity 에서 방 이름과 유저 이름을 받아온다.
         */
        Intent intent = getIntent();
        roomName = intent.getStringExtra("roomName");
        userName = intent.getStringExtra("userName");


        try {
            /**서버와 연결**/
            nodeSocket = IO.socket(HOST);

            /** connect 메소드를 타고 들어가면 open 메소드가 불러진다. 소켓을 만드는 것이다.**/
            nodeSocket.connect();

            /**서버에 메세지를 보낸다. onConnect 메소드에 메세지 를 보내는 함수가 있다.**/
            nodeSocket.on(Socket.EVENT_CONNECT, onConnect);

            /**서버에 메세지를 받는 메소드**/
            nodeSocket.on("serverMessage", onMessageReceived);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        megItem = new ArrayList<MessageItem>();

        //뷰 생성
        messageRecyclerView = findViewById(R.id.Message_RecyclerView);
        messageRecyclerAdapter = new MessageRecyclerAdapter(this, megItem);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        //설정 세팅
        messageRecyclerView.setLayoutManager(linearLayoutManager);
        messageRecyclerView.setAdapter(messageRecyclerAdapter);

        messageEdit = findViewById(R.id.Message_Message_edit);
        messageBtn = findViewById(R.id.Message_Send_btn);

        //전송 버튼 클릭 이벤트
        messageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = messageEdit.getText().toString();

                //메시지를 작성하지 않으면 서버에 메시지를 보내지 않는다.
                if (message.equals("")) return;

                //전송하려는 클라이언트 화면에 메시지를 보여준다.
                megItem.add(new MessageItem(message, userName));

                messageRecyclerAdapter.addItem(megItem);
                messageRecyclerAdapter.notifyDataSetChanged();

                //EditText 값 초기화
                messageEdit.setText("");

                //서버에 데이터를 전송한다.
                JSONObject data = new JSONObject();
                try {

                    /**
                     * room : 방이름
                     * user : 전송하는 유저 이름
                     * message : 메시지
                     */
                    data.put("room", roomName);
                    data.put("user", userName);
                    data.put("message", message);

                    // 메시지 전송
                    nodeSocket.emit("clientMessage", data);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    /*Node.js와 연관된 코드 아래*/

    /* Socket서버에 connect 되면 발생하는 이벤트*/
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            /**join 이벤트 값을 보내 room_id 값을 보낸다,**/
            nodeSocket.emit("join", roomName);

        }
    };

    String serverMessage = null;
    String serverUser = null;

    /*서버로부터 전달받은 'chat-message' Event 처리*/
    private Emitter.Listener onMessageReceived = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            /**채팅 서버에서 메세지 받기
             *
             * 메시지 타입 Json
             * {user : 유저 이름 , message : 메시지}
             * **/
            JSONObject receivedData = (JSONObject) args[0];

            try {

                serverMessage = (String) receivedData.get("message");
                serverUser  = (String) receivedData.get("user");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                /**
                                 * 상대방에게만 메시지가 보여지기 위한 조건문
                                 * - 서버에서 받은 유저이름과 로그인한 유저가 다르다면 실행되는 조건문 -
                                 */
                                if (serverUser.equals(userName) == false) {

                                    megItem.add(new MessageItem(serverMessage, serverUser));

                                    messageRecyclerAdapter.addItem(megItem);
                                    messageRecyclerAdapter.notifyDataSetChanged();
                                }
                            }
                        });

                    }
                }).start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    /**
     * Activity 가 Destroy 상태가 되면 서버와 연결을 끊는다.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        nodeSocket.emit("disconnect", roomName);
        nodeSocket.disconnect();
        nodeSocket.off("new message", onMessageReceived);

    }

    /**
     * Recycler Adapter Class
     */
    public class MessageRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


        //viewType 설정 : 상대방 xml 과 나에 xml 구분을 짓기 위해서 선언함.
        private static final int VIEW_ME = 0;
        private static final int VIEW_YOU = 1;

        Context context;
        ArrayList<MessageItem> message;

        //생성자
        public MessageRecyclerAdapter(Context context, ArrayList<MessageItem> message) {

            this.context = context;
            this.message = message;

        }

        /**
         * 뷰 홀더를 생성하고 뷰를 붙여주는 부분
         **/
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view;

            //viewType == 0 이라면 즉, 나라면 오른쪽에 메시지가 보여진다.
            if (viewType == VIEW_ME) {

                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_me, parent, false);
                return new ItemMe(view);

            }
            //viewType == 1 이라면 즉, 상대방이라면 왼쪽에 메시지가 보여진다.
            else if (viewType == VIEW_YOU) {

                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_you, parent, false);
                return new ItemYou(view);

            }

            return null;
        }


        /**
         * 재활용 되는 뷰가 호출하여 실행되는 메소드,
         * 뷰 홀더를 전달하고 어뎁터는 position 의 데이터를 결합시킵니다.
         **/

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            //나에게 메시지가 보여질 때
            if (megItem.get(position).userName.equals(userName) == true) {

                ((ItemMe) holder).message_me.setText(megItem.get(position).message);

            }
            // 상대방에게 메시지가 보여질 때
            else {

                ((ItemYou) holder).message_you.setText(megItem.get(position).message);

            }

        }

        /**
         * 데이터의 개수를 반환한다.
         **/
        @Override
        public int getItemCount() {
            return message.size();
        }

        /**
         * 상대방과 나에 채팅 xml 표시를 구분 하기 위해 사용됨.
         **/
        @Override
        public int getItemViewType(int position) {

            if (megItem.get(position).userName.equals(userName) == true) {
                return 0;
            } else {
                return 1;
            }
        }

        public void addItem(ArrayList<MessageItem> message) {

            this.message = message;
        }

        /**
         * 커스텀 뷰홀더
         * item layout 에 존재하는 위젯들을 바인딩합니다.
         **/
        public class ItemMe extends RecyclerView.ViewHolder {
            TextView message_me;

            public ItemMe(View itemView) {
                super(itemView);

                message_me = (TextView) itemView.findViewById(R.id.me);
            }
        }

        public class ItemYou extends RecyclerView.ViewHolder {

            TextView message_you;

            public ItemYou(@NonNull View itemView) {
                super(itemView);

                message_you = itemView.findViewById(R.id.you);


            }
        }

    }
}
