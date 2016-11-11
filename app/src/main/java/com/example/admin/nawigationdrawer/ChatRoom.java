package com.example.admin.nawigationdrawer;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class ChatRoom extends AppCompatActivity {

    private Button btn_send_msg;
    private EditText input_msg;
    private String temp_key;
    private FirebaseAuth mAuth;
    private RecyclerView mRecyclerView;
    private DatabaseReference root;
    private DatabaseReference mDatabaseUsers;
    private LinearLayoutManager mLinearLayoutManager;
    private FirebaseRecyclerAdapter<Massege, MassegeViewHolder> mFirebaseAdapter;
    private String userNewImage, userNewName, chatTitle;

    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        mAuth = FirebaseAuth.getInstance();
        final String newUser = mAuth.getCurrentUser().getUid();

        btn_send_msg = (Button)findViewById(R.id.SendChatMsg);
        input_msg = (EditText)findViewById(R.id.editTextMsg);

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(newUser);

        mRecyclerView = (RecyclerView)findViewById(R.id.rv);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Storage reference
        mStorageRef = FirebaseStorage.getInstance().getReference();

        final String user_name = getIntent().getExtras().getString("user_name");
        final String chat_name = getIntent().getExtras().getString("chat_name");
        final String chat_title = getIntent().getExtras().getString("chat_title");


        //инициализация linetLayoutManeger
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);

        setTitle(chat_title);

        root = FirebaseDatabase.getInstance().getReference().child("Chat").child(chat_name);

        // получаем картинку пользователя
        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userNewImage = dataSnapshot.child("image").getValue().toString();
                userNewName = dataSnapshot.child("name").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mFirebaseAdapter = new FirebaseRecyclerAdapter<Massege, MassegeViewHolder>(
                Massege.class,
                R.layout.massege_row,
                MassegeViewHolder.class,
                root
        ) {
            @Override
            protected void populateViewHolder(MassegeViewHolder viewHolder, Massege model, int position) {

                viewHolder.setName(model.getName());
                viewHolder.setMsg(model.getMsg());
                viewHolder.setImage(getApplicationContext(), model.getImage());
            }
        };

        //перемещение на последнюю позицию
        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();

                if(lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))){
                    mRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mFirebaseAdapter);

        //отправка сообщения
        btn_send_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Map<String,Object> map = new HashMap<String, Object>();
                temp_key = root.push().getKey();
                root.updateChildren(map);

                DatabaseReference message_root = root.child(temp_key);
                Map<String, Object> map2 = new HashMap<String, Object>();
                map2.put("name",userNewName);
                map2.put("msg",input_msg.getText().toString());
                map2.put("image", userNewImage);

                message_root.updateChildren(map2);

                input_msg.setText("");

                //помещаем добавленное сообщение на экран
                int s = mFirebaseAdapter.getItemCount();
                int d = mLinearLayoutManager.findFirstCompletelyVisibleItemPosition();
                if(s != d){
                    mRecyclerView.scrollToPosition(s);
                }
            }
        });

    }

    public static class MassegeViewHolder extends RecyclerView.ViewHolder{

        View nView;

        public  MassegeViewHolder(View itemView){
            super(itemView);
            nView = itemView;
        }

        public void setName(String name){
            TextView massege_name = (TextView)nView.findViewById(R.id.massege_name);
            massege_name.setText(name);
        }

        public void setMsg(String msg){
            TextView massege_msg = (TextView)nView.findViewById(R.id.massege_text);
            massege_msg.setText(msg);
        }

        public void setImage(Context ctx, String image){
            ImageView post_image = (ImageView)nView.findViewById(R.id.userImage);
            Picasso.with(ctx).load(image).into(post_image);
        }
    }
}
