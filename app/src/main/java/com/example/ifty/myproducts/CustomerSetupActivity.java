package com.example.ifty.myproducts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import id.zelory.compressor.Compressor;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CustomerSetupActivity extends AppCompatActivity {

    private static final int IMAGE_CHOOSE_CODE = 1;
    private Uri mainImageUri = null;
    private EditText mobileNumber;
    private ImageView customerImage;
    private EditText userName;
    private EditText address;
    private Button accSaveBtn;
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private ProgressBar customerProgressBar;
    private Uri defaultUri;
    private String user_id;
    private Boolean isChanged;
    private Uri notChangedUri;
    private Bitmap compressedImageFile;
    private Uri mainDownloadUri;
    private Uri notChangedThumbUri;
    private Uri customerImageUri = null;
    private String ifOrder;
    private String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_setup);

        ifOrder=getIntent().getStringExtra("ifOrder");
        postId=getIntent().getStringExtra("postId");

        String intentChecker = getIntent().getStringExtra("intentChecker");
        if (intentChecker.equals("first_time")) {
            isChanged = true;
        } else if (intentChecker.equals("not_first_time")) {
            isChanged = false;
        }

        mobileNumber = findViewById(R.id.mobile_customer);
        customerImage = findViewById(R.id.user_image);
        userName = findViewById(R.id.customer_name);
        address = findViewById(R.id.customer_address);
        accSaveBtn = findViewById(R.id.customer_setup_btn);
        customerProgressBar = findViewById(R.id.customerProgressBar);

        customerProgressBar.setVisibility(View.VISIBLE);
        accSaveBtn.setEnabled(false);

        defaultUri = Uri.parse("android.resource://com.example.ifty.myproducts/drawable/default_user_image");
        try {
            InputStream stream = getContentResolver().openInputStream(defaultUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        user_id = mAuth.getCurrentUser().getUid();

        firebaseFirestore.collection("Customers").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        Toast.makeText(CustomerSetupActivity.this, "exist", Toast.LENGTH_SHORT).show();

                        String customer_or_seller = task.getResult().getString("customerOrSeller");
                        String user_name = task.getResult().getString("userName");
                        String mobile_number = task.getResult().getString("mobileNumber");
                        String customer_address = task.getResult().getString("address");
                        String customer_image = task.getResult().getString("customerImage");
                        String customer_Thumbnail = task.getResult().getString("customerThumbnail");
                        notChangedUri = Uri.parse(customer_image);
                        notChangedThumbUri = Uri.parse(customer_Thumbnail);

                        userName.setText(user_name);
                        mobileNumber.setText(mobile_number);
                        address.setText(customer_address);

                        RequestOptions placeHolderRequest = new RequestOptions();
                        placeHolderRequest.placeholder(R.drawable.default_user_image);

                        Glide.with(CustomerSetupActivity.this).applyDefaultRequestOptions(placeHolderRequest).load(customer_image).into(customerImage);
                    } else {
                        Toast.makeText(CustomerSetupActivity.this, "don't exist", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String error = task.getException().toString();
                    Toast.makeText(CustomerSetupActivity.this, "RETRIEVE ERROR : " + error, Toast.LENGTH_SHORT).show();
                }
                customerProgressBar.setVisibility(View.INVISIBLE);
                accSaveBtn.setEnabled(true);
            }
        });
    }


    public void deployImsge(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                bringImage();
            } else {
                requestForPermission(Manifest.permission.READ_EXTERNAL_STORAGE, "Storage", IMAGE_CHOOSE_CODE);
            }
        } else {
            bringImage();
        }
    }


    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
        super.onBackPressed();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == IMAGE_CHOOSE_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                bringImage();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestForPermission(final String permissionType, String permissionText, final int REQUEST_CODE) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissionType)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("To make a " + permissionText + " this permission is needed.")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(CustomerSetupActivity.this, new String[]{permissionType}, REQUEST_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permissionType}, REQUEST_CODE);
        }
    }

    private void bringImage() {
        customerImage.getScaleType();
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(CustomerSetupActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageUri = result.getUri();
                isChanged = true;
                customerImage.setImageURI(mainImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void saveAccSettings(View view) {
        final String mobile_number = mobileNumber.getText().toString();
        final String customer_address = address.getText().toString();
        final String user_name = userName.getText().toString();
        if (!TextUtils.isEmpty(user_name) && !TextUtils.isEmpty(mobile_number) && !TextUtils.isEmpty(customer_address)) {
            if (isChanged) {
                customerProgressBar.setVisibility(View.VISIBLE);
                final StorageReference photo_path = mStorageRef.child("customer_images").child(user_id + ".jpg");
                if (mainImageUri == null) {
                    mainImageUri = defaultUri;
                }

                photo_path.putFile(mainImageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        if (taskSnapshot.getTask().isSuccessful()) {
                            //When the image has successfully uploaded, get its download URL
                            photo_path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    mainDownloadUri = uri;


                                    File photoFile = new File(Objects.requireNonNull(mainImageUri.getPath()));
                                    try {
                                        compressedImageFile = new Compressor(CustomerSetupActivity.this)
                                                .setMaxHeight(50)
                                                .setMaxWidth(50)
                                                .setQuality(1)
                                                .compressToBitmap(photoFile);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    if (compressedImageFile != null) {
                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                        byte[] thumbData = baos.toByteArray();

                                        final StorageReference uploadTask = mStorageRef.child("customer_images/thumbnail").child(user_id + ".jpg");
                                        uploadTask.putBytes(thumbData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                if (taskSnapshot.getTask().isSuccessful()) {
                                                    uploadTask.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri uri) {
                                                            Uri thumbDownloadUri = uri;

                                                            storeToFirestore(user_id,mainDownloadUri, thumbDownloadUri, mobile_number, customer_address, user_name,"customer");
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }


                                }
                            });
                        } else {
                            String error = taskSnapshot.getTask().getException().toString();
                            Toast.makeText(CustomerSetupActivity.this, "IMAGE ERROR : " + error, Toast.LENGTH_SHORT).show();
                            customerProgressBar.setVisibility(View.INVISIBLE);
                        }

                    }
                });
            } else {
                storeToFirestore(user_id,notChangedUri, notChangedThumbUri, mobile_number, customer_address, user_name,"customer");
            }

        } else {
            Toast.makeText(this, "*Company name\n*Mobile number\n*Address\n are required.", Toast.LENGTH_SHORT).show();
        }
    }


    private void storeToFirestore(String user_id, Uri dlUri, Uri thumbDownloadUri, String mobile_number, String address, String user_name,String customer) {

        Map<String, String> customerMap = new HashMap<>();
        customerMap.put("userId",user_id);
        customerMap.put("customerOrSeller",customer);
        customerMap.put("userName",user_name);
        customerMap.put("mobileNumber",mobile_number);
        customerMap.put("address",address);
        customerMap.put("customerImage", dlUri.toString());
        customerMap.put("customerThumbnail", thumbDownloadUri.toString());

        firebaseFirestore.collection("Customers").document(user_id).set(customerMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(CustomerSetupActivity.this, "Settings Updated Successfully!", Toast.LENGTH_SHORT).show();
                    if (ifOrder.equals("fromOrder")){
                        Intent intent = new Intent(CustomerSetupActivity.this,ProductOrderActivity.class);
                        intent.putExtra("postId",postId);
                        finish();
                        startActivity(intent);
                    }
                    else {
                        startActivity(new Intent(CustomerSetupActivity.this,MainActivity.class));
                        finish();
                    }
                }
                else {
                    String error = task.getException().toString();
                    Toast.makeText(CustomerSetupActivity.this, "FireStore ERROR : "+error, Toast.LENGTH_SHORT).show();
                    customerProgressBar.setVisibility(View.INVISIBLE);
                }
            }
        });

    }
}