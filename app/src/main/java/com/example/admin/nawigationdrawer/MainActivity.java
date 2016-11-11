package com.example.admin.nawigationdrawer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import static com.example.admin.nawigationdrawer.R.id.nav_manage1;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView mPostList;
    private DatabaseReference mDatabase;
    private DatabaseReference nDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Query mQueryCurrentRequest;
    private FirebaseRecyclerAdapter<Post, PostViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        nDatabase = FirebaseDatabase.getInstance().getReference().child("Post");

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener(){

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if(firebaseAuth.getCurrentUser() == null){
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Post");

        //
        mQueryCurrentRequest = mDatabase;

        mPostList = (RecyclerView)findViewById(R.id.post_list);
        mPostList.setHasFixedSize(true);
        //mPostList.setLayoutManager(new LinearLayoutManager(this);

        //изменение порядка отображения ленты постоы чтобы последние посты
        //публиковались вверху списка
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mPostList.setLayoutManager(layoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(

                Post.class,
                R.layout.post_row,
                PostViewHolder.class,
                mQueryCurrentRequest
               // mDatabase

        ) {
            @Override
            protected void populateViewHolder(PostViewHolder viewHolder, Post model, int position) {

                //получение имени пользователя и ид чата для отправки в следующую активити
                final String newString = model.getChatId();
                final String user_name = model.getUsername();
                final String chat_title = model.getTitle();

                viewHolder.setChannel(model.getChannel());
                viewHolder.setTitle(model.getTitle());
                viewHolder.setUsername(model.getUsername());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent chatRoomIntent = new Intent(MainActivity.this, ChatRoom.class);
                        chatRoomIntent.putExtra("chat_name", newString);
                        chatRoomIntent.putExtra("user_name", user_name);
                        chatRoomIntent.putExtra("chat_title", chat_title);
                        startActivity(chatRoomIntent);

                    }
                });
            }
        };

        mPostList.setAdapter(firebaseRecyclerAdapter);

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        firebaseRecyclerAdapter.cleanup();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public PostViewHolder(View itemView) {

            super(itemView);
            mView = itemView;
        }

        public void setChannel(String channel){

            TextView post_channel = (TextView)mView.findViewById(R.id.channel_title);
            post_channel.setText(channel);
        }

        public void setTitle(String title){

            TextView post_title = (TextView)mView.findViewById(R.id.post_title);
            post_title.setText(title);

        }

        public void setUsername(String username){

            TextView user_name = (TextView)mView.findViewById(R.id.textView4);
            user_name.setText(username);

        }

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_manu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if(item.getItemId() == R.id.action_add){

            startActivity(new Intent(MainActivity.this, PostActivity.class ));
        }

        if(item.getItemId() == R.id.action_logout){

            logout();

        }

        if(item.getItemId() == R.id.action_setup){

            startActivity(new Intent(MainActivity.this, SetupActivity.class ));
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout(){

        mAuth.signOut();

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.ort) {

            String currentCahnnel = "ОРТ";

            // clean adapter
            firebaseRecyclerAdapter.cleanup();
            mQueryCurrentRequest = mDatabase.orderByChild("channel").equalTo(currentCahnnel);
            onStart();

        }
        else if (id == R.id.all) {

            String currentCahnnel = "Все каналы";
            // clean adapter
            firebaseRecyclerAdapter.cleanup();
            mQueryCurrentRequest = mDatabase;
            onStart();

        }
        else if (id == R.id.nav_gallery) {

            String currentCahnnel = "Россия1";
            // clean adapter
            firebaseRecyclerAdapter.cleanup();
            mQueryCurrentRequest = mDatabase.orderByChild("channel").equalTo(currentCahnnel);
            onStart();

        } else if (id == R.id.nav_slideshow) {

            String currentCahnnel = "ТНТ";
            // clean adapter
            firebaseRecyclerAdapter.cleanup();
            mQueryCurrentRequest = mDatabase.orderByChild("channel").equalTo(currentCahnnel);
            onStart();

        } else if (id == R.id.nav_manage) {

            String currentCahnnel = "НТВ";
            // clean adapter
            firebaseRecyclerAdapter.cleanup();
            mQueryCurrentRequest = mDatabase.orderByChild("channel").equalTo(currentCahnnel);
            onStart();

        } else if (id == nav_manage1) {

            String currentCahnnel = "СТС";
            // clean adapter
            firebaseRecyclerAdapter.cleanup();
            mQueryCurrentRequest = mDatabase.orderByChild("channel").equalTo(currentCahnnel);
            onStart();

        } else if (id == R.id.nav_manage2) {

            String currentCahnnel = "ТВЦ";
            // clean adapter
            firebaseRecyclerAdapter.cleanup();
            mQueryCurrentRequest = mDatabase.orderByChild("channel").equalTo(currentCahnnel);
            onStart();

        }

        else if (id == R.id.nav_manage3) {

            String currentCahnnel = "Рентв";
            // clean adapter
            firebaseRecyclerAdapter.cleanup();
            mQueryCurrentRequest = mDatabase.orderByChild("channel").equalTo(currentCahnnel);
            onStart();

        }else if (id == R.id.nav_manage4) {

            String currentCahnnel = "5 канал";
            // clean adapter
            firebaseRecyclerAdapter.cleanup();
            mQueryCurrentRequest = mDatabase.orderByChild("channel").equalTo(currentCahnnel);
            onStart();

        }else if (id == R.id.nav_manage5) {

            String currentCahnnel = "Россия 24";
            // clean adapter
            firebaseRecyclerAdapter.cleanup();
            mQueryCurrentRequest = mDatabase.orderByChild("channel").equalTo(currentCahnnel);
            onStart();

        }else if (id == R.id.nav_manage6) {

            String currentCahnnel = "ТВ3";
            // clean adapter
            firebaseRecyclerAdapter.cleanup();
            mQueryCurrentRequest = mDatabase.orderByChild("channel").equalTo(currentCahnnel);
            onStart();

        }else if (id == R.id.nav_manage7) {

            String currentCahnnel = "Домашний";
            // clean adapter
            firebaseRecyclerAdapter.cleanup();
            mQueryCurrentRequest = mDatabase.orderByChild("channel").equalTo(currentCahnnel);
            onStart();

        }else if (id == R.id.nav_manage8) {

            String currentCahnnel = "2x2";
            // clean adapter
            firebaseRecyclerAdapter.cleanup();
            mQueryCurrentRequest = mDatabase.orderByChild("channel").equalTo(currentCahnnel);
            onStart();

        }else if (id == R.id.nav_manage9) {

            String currentCahnnel = "Пятница";
            // clean adapter
            firebaseRecyclerAdapter.cleanup();
            mQueryCurrentRequest = mDatabase.orderByChild("channel").equalTo(currentCahnnel);
            onStart();

        }else if (id == R.id.nav_manage10) {

            String currentCahnnel = "Звезда";
            // clean adapter
            firebaseRecyclerAdapter.cleanup();
            mQueryCurrentRequest = mDatabase.orderByChild("channel").equalTo(currentCahnnel);
            onStart();

        }else if (id == R.id.nav_manage11) {

            String currentCahnnel = "Дисней";
            // clean adapter
            firebaseRecyclerAdapter.cleanup();
            mQueryCurrentRequest = mDatabase.orderByChild("channel").equalTo(currentCahnnel);
            onStart();

        }else if (id == R.id.nav_manage12) {

            String currentCahnnel = "Че";
            // clean adapter
            firebaseRecyclerAdapter.cleanup();
            mQueryCurrentRequest = mDatabase.orderByChild("channel").equalTo(currentCahnnel);
            onStart();

        }else if (id == R.id.nav_manage13) {

            String currentCahnnel = "Ю";
            // clean adapter
            firebaseRecyclerAdapter.cleanup();
            mQueryCurrentRequest = mDatabase.orderByChild("channel").equalTo(currentCahnnel);
            onStart();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
