package com.example.ifty.myproducts;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SetupActivity extends AppCompatActivity {

    private static final int IMAGE_CHOOSE_CODE = 1;
    private Uri companyImageUri =null;
    private CircleImageView companyLogo;
    private EditText companyName;
    private EditText mobileNumber;
    private EditText email;
    private EditText companyAddress;
    private Button accSaveBtn;
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private ProgressBar setupProgressBar;
    private Uri companyDefaultUri;
    private String user_id;
    private Boolean isChanged;
    private Uri companyNotChangedUri;
    private Bitmap companyCompressedImageFile;
    private Uri companyDownloadUri;
    private Uri companyNotChangedThumbUri;
    private Uri companyThumbDlUri;
    private String company_name;
    private String mobile_number;
    private String company_email;
    private String company_address;
    private String intentChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        intentChecker=getIntent().getStringExtra("intentChecker");
        if (intentChecker.equals("first_time")){
            isChanged=true;
        }
        else if (intentChecker.equals("not_first_time")){
            isChanged=false;
        }


        companyLogo=findViewById(R.id.setup_company_logo);
        companyName=findViewById(R.id.setup_company_name);
        mobileNumber=findViewById(R.id.setup_mobile_number);
        email=findViewById(R.id.setup_email);
        companyAddress=findViewById(R.id.setup_company_address);
        accSaveBtn=findViewById(R.id.setup_btn);
        setupProgressBar=findViewById(R.id.setupProgressBar);

        setupProgressBar.setVisibility(View.VISIBLE);
        accSaveBtn.setEnabled(false);

        companyDefaultUri = Uri.parse("android.resource://com.example.ifty.myproducts/drawable/company_logo");
        try {
            InputStream stream = getContentResolver().openInputStream(companyDefaultUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        mAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        user_id=mAuth.getCurrentUser().getUid();

        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().exists()){
                        Toast.makeText(SetupActivity.this, "exist", Toast.LENGTH_SHORT).show();

                        String company_name = task.getResult().getString("companyName");
                        String mobile_number = task.getResult().getString("mobileNumber");
                        String company_address = task.getResult().getString("companyAddress");
                        String company_email = task.getResult().getString("email");
                        String company_logo = task.getResult().getString("companyLogo");
                        String company_thumbnail = task.getResult().getString("logoThumbnail");

                        companyNotChangedUri =Uri.parse(company_logo);
                        companyNotChangedThumbUri =Uri.parse(company_thumbnail);

                        companyName.setText(company_name);
                        mobileNumber.setText(mobile_number);
                        email.setText(company_email);
                        companyAddress.setText(company_address);

                        RequestOptions placeHolderRequest = new RequestOptions();
                        placeHolderRequest.placeholder(R.drawable.company_logo);

                        Glide.with(SetupActivity.this).applyDefaultRequestOptions(placeHolderRequest).load(company_logo).into(companyLogo);
                    }
                    else {
                        Toast.makeText(SetupActivity.this, "don't exist", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    String error = task.getException().toString();
                    Toast.makeText(SetupActivity.this, "RETRIEVE ERROR : "+error, Toast.LENGTH_SHORT).show();
                }
                setupProgressBar.setVisibility(View.INVISIBLE);
                accSaveBtn.setEnabled(true);
            }
        });
    }
                    //..........................End of onCreate.............................//

    public void deployImsge(View view) {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if (ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                bringImage();
            }
            else {
                requestForPermission(Manifest.permission.READ_EXTERNAL_STORAGE,"Storage",IMAGE_CHOOSE_CODE);
            }
        }
        else {
            bringImage();
        }
    }


    @Override
    public void onBackPressed() {
        startActivity(new Intent(this,MainActivity.class));
        finish();
        super.onBackPressed();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==IMAGE_CHOOSE_CODE){
            if (grantResults.length>0 && grantResults[0] ==PackageManager.PERMISSION_GRANTED){
                bringImage();
            }
            else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestForPermission(final String permissionType, String permissionText, final int REQUEST_CODE) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,permissionType)){
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("To make a "+permissionText+" this permission is needed.")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(SetupActivity.this,new String[] {permissionType},REQUEST_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        }
        else {
            ActivityCompat.requestPermissions(this,new String[] {permissionType},REQUEST_CODE);
        }
    }

    private void bringImage() {
        companyLogo.getScaleType();
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(SetupActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                companyImageUri = result.getUri();
                isChanged=true;
                companyLogo.setImageURI(companyImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

                  //..........................End of Image Upload.............................//

    public void saveAccSettings(View view) {
        company_name = companyName.getText().toString();
        mobile_number = mobileNumber.getText().toString();
        company_email = email.getText().toString();
        company_address = companyAddress.getText().toString();
        if (!TextUtils.isEmpty(company_name) && !TextUtils.isEmpty(mobile_number) && !TextUtils.isEmpty(company_address)){
            if (TextUtils.isEmpty(company_email)){
                company_email="";
            }
            if (isChanged){
                setupProgressBar.setVisibility(View.VISIBLE);
                final StorageReference photo_path= mStorageRef.child("company_logos").child(user_id+".jpg");
                if (companyImageUri ==null){
                    companyImageUri = companyDefaultUri;
                }
                photo_path.putFile(companyImageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        if (taskSnapshot.getTask().isSuccessful()){
                            //When the image has successfully uploaded, get its download URL
                            photo_path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    companyDownloadUri = uri;


                                    if (companyImageUri==companyDefaultUri){
                                        try {
                                            companyCompressedImageFile = MediaStore.Images.Media.getBitmap(SetupActivity.this.getContentResolver(),companyImageUri);
                                        } catch (IOException e) {
                                            e.printStackTrace();

                                            Toast.makeText(SetupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    else {
                                        File photoFile = new File(companyImageUri.getPath());
                                        try {
                                            companyCompressedImageFile = new Compressor(SetupActivity.this)
                                                    .setMaxHeight(50)
                                                    .setMaxWidth(50)
                                                    .setQuality(1)
                                                    .compressToBitmap(photoFile);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            Toast.makeText(SetupActivity.this, "3"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    if (companyCompressedImageFile !=null){

                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        companyCompressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                        byte[] thumbData = baos.toByteArray();

                                        final StorageReference uploadTask = mStorageRef.child("company_logos/thumbnail").child(user_id+".jpg");
                                        uploadTask.putBytes(thumbData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                if (taskSnapshot.getTask().isSuccessful()){

                                                    uploadTask.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri uri) {
                                                            companyThumbDlUri= uri;
                                                            storeToFirestore(companyDownloadUri, companyThumbDlUri,company_name,mobile_number,company_email,company_address,"seller");
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }


                                }
                            });
                        }
                        else {
                            String error = taskSnapshot.getTask().getException().toString();
                            Toast.makeText(SetupActivity.this, "IMAGE ERROR : "+error, Toast.LENGTH_SHORT).show();
                            setupProgressBar.setVisibility(View.INVISIBLE);
                        }

                    }
                });
            }
            else {
                storeToFirestore(companyNotChangedUri, companyNotChangedThumbUri,company_name,mobile_number,company_email,company_address,"seller");
            }

        }
        else {
            Toast.makeText(this, "*Company name\n*Owner Name\n*Mobile number\n*Address\n are required.", Toast.LENGTH_SHORT).show();
        }
    }

    private void storeToFirestore(Uri companyDownloadUri, Uri companyThumbDlUri,String company_name,String mobile_number,String company_email, String address, String seller) {
        Map<String, String> userMap = new HashMap<>();
        userMap.put("companyName",company_name);
        userMap.put("mobileNumber",mobile_number);
        userMap.put("email",company_email);
        userMap.put("companyAddress",address);
        userMap.put("companyLogo",companyDownloadUri.toString());
        userMap.put("logoThumbnail",companyThumbDlUri.toString());

        final Map<String, String> customerMap = new HashMap<>();
        customerMap.put("userId",user_id);
        customerMap.put("customerOrSeller",seller);
        customerMap.put("userName",company_name);
        customerMap.put("mobileNumber",mobile_number);
        customerMap.put("address",address);
        customerMap.put("customerImage", companyDownloadUri.toString());
        customerMap.put("customerThumbnail", companyThumbDlUri.toString());

        firebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                if (task.isSuccessful()){
                    firebaseFirestore.collection("Customers").document(user_id).set(customerMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(SetupActivity.this, "Settings Updated Successfully!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SetupActivity.this,MainActivity.class));
                                finish();
                            }
                            else {
                                String error = task.getException().toString();
                                Toast.makeText(SetupActivity.this, "FireStore ERROR : "+error, Toast.LENGTH_SHORT).show();
                                setupProgressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }
                else {
                    String error = task.getException().toString();
                    Toast.makeText(SetupActivity.this, "FireStore ERROR : "+error, Toast.LENGTH_SHORT).show();
                    setupProgressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

}
