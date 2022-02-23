package com.example.myscholarshipadmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {
    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;
    Button pick_image,post;
    ImageView imageview;
    Spinner spinner1,spinner2;
    EditText et_countryName,et_deadline,et_organization,et_link;
    String Location;

    FirebaseDatabase mDatabase;
    DatabaseReference mRef;
    FirebaseStorage mStorage;
    Uri imageurl;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner1 =  findViewById(R.id.continent);
        spinner2 =  findViewById(R.id.program);
        imageview = findViewById(R.id.image);
        pick_image = findViewById(R.id.pick_image);
        post =  findViewById(R.id.post);
        et_countryName = findViewById(R.id.country);
        et_deadline = findViewById(R.id.deadline);
        et_organization = findViewById(R.id.organization);
        et_link = findViewById(R.id.link);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.Continents, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.program, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter);
        spinner2.setAdapter(adapter2);

        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();
        progressDialog = new ProgressDialog(this);



        pick_image.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT < 23) {
                   pickImageFromGallary();
                } else if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 1001);
                } else {
                    pickImageFromGallary();
                }
            }
        });
    }
    public void pickImageFromGallary() {
        Intent intent = new Intent("android.intent.action.PICK");
        intent.setType("image/*");
        startActivityForResult(intent, 1000);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1001:
                if (grantResults.length <= 0 || grantResults[0] != 0) {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    pickImageFromGallary();
                    return;
                }
            default:
                return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1 && requestCode == 1000) {
            imageurl =data.getData();
            imageview.setImageURI(imageurl);
        }
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Location = spinner1.getSelectedItem().toString();
                switch(Location)
                {
                    case "USA":
                        mRef = mDatabase.getReference().child("Scholarships").child("USA");
                        break;
                    case "SAmerica":
                        mRef = mDatabase.getReference().child("Scholarships").child("SAmerica");
                        break;
                    case "Europe":
                        mRef = mDatabase.getReference().child("Scholarships").child("Europe");
                        break;
                    case "Asia":
                        mRef = mDatabase.getReference().child("Scholarships").child("Asia");
                        break;
                    case "Australia":
                        mRef = mDatabase.getReference().child("Scholarships").child("Australia");
                        break;
                    default:
                        mRef = mDatabase.getReference().child("Scholarships");
                }

                String Continent = spinner1.getSelectedItem().toString();
                String Program = spinner2.getSelectedItem().toString();
                String CountryName = et_countryName.getText().toString();
                String Deadline = et_deadline.getText().toString();
                String Organization = et_organization.getText().toString();
                String link = et_link.getText().toString();
                if(!(Continent.isEmpty() && Program.isEmpty() && CountryName.isEmpty() && Deadline.isEmpty() && Organization.isEmpty() ))
                {
                progressDialog.setTitle("Uploading Data");
                progressDialog.show();
                    StorageReference filepath = mStorage.getReference("Images").child(imageurl.getLastPathSegment());
                    filepath.putFile(imageurl).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> downloadUri = taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Uri> task) {
                                    String t = task.getResult().toString();
                                    DatabaseReference newPost=mRef.push();
                                    newPost.child("Continent").setValue(Continent);
                                    newPost.child("Program").setValue(Program);
                                    newPost.child("CountryName").setValue(CountryName);
                                    newPost.child("Deadline").setValue(Deadline);
                                    newPost.child("Organization").setValue(Organization);
                                    newPost.child("Link").setValue(link);
                                    newPost.child("Image").setValue(task.getResult().toString());
                                    progressDialog.dismiss();
                                }
                            });
                        }
                    });
                }

            }
        });
    }

}