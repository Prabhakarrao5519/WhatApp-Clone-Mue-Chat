package com.csjmdevelopers.muechat.Models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Toast;

import com.csjmdevelopers.muechat.MainActivity;
import com.csjmdevelopers.muechat.R;
import com.csjmdevelopers.muechat.databinding.ActivitySettingBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class SettingActivity extends AppCompatActivity {
    ActivitySettingBinding binding;
    FirebaseStorage storage;
    FirebaseAuth auth;
    FirebaseDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storage=FirebaseStorage.getInstance();
        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();

        getSupportActionBar().hide();

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i= new Intent(SettingActivity.this, MainActivity.class);
                startActivity(i);
            }
        });
         binding.btnSave.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 String status=binding.etStatus.getText().toString();
                 String username=binding.etUserName.getText().toString();

                 HashMap<String ,Object>obj=new HashMap<>();
                 obj.put("userName" , username);
                 obj.put("status" , status);

                 database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                         .updateChildren(obj);

                 Toast.makeText(SettingActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();

             }
         });

        binding.plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,33);

            }
        });
        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Users users =snapshot.getValue(Users.class);
                        Picasso.get()
                                .load(users.getProfilePic())
                                .placeholder(R.drawable.profile)
                                .into(binding.profileImage);
                        binding.etStatus.setText(users.getStatus());
                        binding.etUserName.setText(users.getUserName());

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data.getData() !=null){
            Uri sFile=data.getData();
            binding.profileImage.setImageURI(sFile);
            final StorageReference reference=storage.getReference().child("Profile Image")
                    .child(FirebaseAuth.getInstance().getUid());
            reference.putFile(sFile).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                           database.getReference().child("Users")
                                   .child(FirebaseAuth.getInstance().getUid())
                                   .child("profilePic").setValue(uri.toString());
                            Toast.makeText(SettingActivity.this, "Profile Pic Updated...", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }
}