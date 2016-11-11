package com.example.admin.nawigationdrawer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class SetupActivity extends AppCompatActivity {

    private ImageButton mSetupImageBtn;
    private EditText mNameField;
    private Button mSubmitBtn;
    private static final int GALLARY_REQUEST = 1;
    private Uri mImageUri = null;
    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth mAuth;
    private StorageReference mStorageImage;
    private ProgressDialog mProgress;
    private String cUser;
    private DatabaseReference mCurentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        mProgress = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        mSetupImageBtn = (ImageButton)findViewById(R.id.imageBtn);
        mNameField = (EditText)findViewById(R.id.editT);
        mSubmitBtn = (Button)findViewById(R.id.button2);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mStorageImage = FirebaseStorage.getInstance().getReference().child("Profile_image");

        cUser = mAuth.getCurrentUser().getUid();
        mCurentUser = FirebaseDatabase.getInstance().getReference().child("Users").child(cUser);


        //получение данных пользователя для вставки их форму
        mCurentUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String userName = dataSnapshot.child("name").getValue().toString();
                final String userImage = dataSnapshot.child("image").getValue().toString();
                mNameField.setText(userName);

                Picasso.with(getApplicationContext())
                        .load(userImage)
                        .placeholder(R.drawable.com_facebook_profile_picture_blank_square)
                        .into(mSetupImageBtn);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSetupAccount();
            }
        });

        mSetupImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLARY_REQUEST);
            }
        });

    }

    private void startSetupAccount() {

        final String name = mNameField.getText().toString().trim();

        final String user_id = mAuth.getCurrentUser().getUid();

        if(!TextUtils.isEmpty(name)&& mImageUri != null){

            mProgress.setMessage("Finishing setup ...");
            mProgress.show();

            StorageReference filepath = mStorageImage.child(mImageUri.getLastPathSegment());

            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    String downloadUrl = taskSnapshot.getDownloadUrl().toString();

                    mDatabaseUsers.child(user_id).child("name").setValue(name);
                    mDatabaseUsers.child(user_id).child("image").setValue(downloadUrl);

                    mProgress.dismiss();

                    Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLARY_REQUEST && resultCode == RESULT_OK){

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageUri = result.getUri();
                mSetupImageBtn.setImageURI(mImageUri);


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }
}
