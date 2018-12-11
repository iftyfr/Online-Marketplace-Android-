package com.example.ifty.myproducts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProductAddActivity extends AppCompatActivity {

    private static final int IMAGE_CHOOSE_CODE = 1;

    private TextView productType, deviceTpTV, transmissionTV, landTypeTV, landUnitsTV, houseUnitsTV;

    private TextInputEditText locationET, brandET, modelET, engineET,modelYrET, titleET, descriptionET, priceET, bedsET, bathsET, landsizeET, sizeET, housesizeET, productNameET;

    private Button addPhotoBtn, addPostBtn;

    private TextInputLayout brandEtLayout, modelEtLayout, engineEtLayout,  modelYrEtLayout, titleEtLayout, bedsEtLayout, bathsEtLayout, landsizeEtLayout, sizeEtLayout, housesizeEtLayout, productNameEtLayout;

    private RadioGroup  deviceTpRG, transmissionRG;
    private RadioButton radioButton;

    private ImageView post_image;
    private Spinner landUitsSP, landTypeSP, houseUitsSP;
    private CheckBox negotiableCB;
    private String checkItem;
    private String selectedCategory;
    private ProgressBar mProgressBar;
    private String deviceType=null;
    private String transmissionType=null;
    private String landSizeUnit=null;
    private String houseSizeUnit=null;
    private String landType=null;
    private String description = null;

    private Uri postImageUri = null;


    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference mStorageReference;
    private String user_id;
    private Toolbar toolbar;
    private Uri postDownlUri=null;
    private Bitmap compressedImageFile;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_add);



        mProgressBar = findViewById(R.id.progressBar);
        addPhotoBtn = findViewById(R.id.addPhotoBtn);
        addPostBtn = findViewById(R.id.addPostBtn);
        productType = findViewById(R.id.productType);
        deviceTpTV = findViewById(R.id.deviceTpTV);
        transmissionTV = findViewById(R.id.transmissionTV);
        landTypeTV = findViewById(R.id.landTypeTV);
        landUnitsTV = findViewById(R.id.landUnitsTV);
        houseUnitsTV = findViewById(R.id.houseUnitsTV);
        productNameET = findViewById(R.id.productNameET);
        locationET = findViewById(R.id.locationET);
        brandET = findViewById(R.id.brandET);
        modelET = findViewById(R.id.modelET);
        engineET = findViewById(R.id.engineET);
        modelYrET = findViewById(R.id.modelYrET);
        titleET = findViewById(R.id.titleET);
        descriptionET = findViewById(R.id.descriptionET);
        priceET = findViewById(R.id.priceET);
        bedsET = findViewById(R.id.bedsET);
        bathsET = findViewById(R.id.bathsET);
        landsizeET = findViewById(R.id.landsizeET);
        sizeET = findViewById(R.id.sizeET);
        housesizeET = findViewById(R.id.housesizeET);
        post_image = findViewById(R.id.image_post);
        deviceTpRG = findViewById(R.id.deviceTpRG);
        transmissionRG = findViewById(R.id.transmissionRG);
        negotiableCB = findViewById(R.id.negotiableCB);
        landUitsSP = findViewById(R.id.landUitsSP);
        landTypeSP = findViewById(R.id.landTypeSP);
        houseUitsSP = findViewById(R.id.houseUitsSP);

        brandEtLayout = findViewById(R.id.brandEtLayout);
        modelEtLayout = findViewById(R.id.modelEtLayout);
        engineEtLayout = findViewById(R.id.engineEtLayout);
        modelYrEtLayout = findViewById(R.id.modelYrEtLayout);
        titleEtLayout = findViewById(R.id.titleEtLayout);
        bedsEtLayout = findViewById(R.id.bedsEtLayout);
        bathsEtLayout = findViewById(R.id.bathsEtLayout);
        landsizeEtLayout = findViewById(R.id.landsizeEtLayout);
        sizeEtLayout = findViewById(R.id.sizeEtLayout);
        housesizeEtLayout = findViewById(R.id.housesizeEtLayout);


        mAuth=FirebaseAuth.getInstance();
        mStorageReference=FirebaseStorage.getInstance().getReference();
        firebaseFirestore=FirebaseFirestore.getInstance();
        user_id = mAuth.getCurrentUser().getUid();


        checkItem = getIntent().getStringExtra("item");
        productType.setText(checkItem);

        switch (checkItem) {
            case "Mobile Phones":
                brandEtLayout.setVisibility(View.VISIBLE);
                modelEtLayout.setVisibility(View.VISIBLE);
                break;
            case "Mobile Phone Accessories":
                titleEtLayout.setVisibility(View.VISIBLE);
                break;
            case "Computers & Tablets":
                deviceTpTV.setVisibility(View.VISIBLE);
                deviceTpRG.setVisibility(View.VISIBLE);
                brandEtLayout.setVisibility(View.VISIBLE);
                modelEtLayout.setVisibility(View.VISIBLE);
                titleEtLayout.setVisibility(View.VISIBLE);
                break;
            case "TVs":
                brandEtLayout.setVisibility(View.VISIBLE);
                modelEtLayout.setVisibility(View.VISIBLE);
                titleEtLayout.setVisibility(View.VISIBLE);
                break;
            case "Motorbikes & Scooters":
                brandEtLayout.setVisibility(View.VISIBLE);
                modelEtLayout.setVisibility(View.VISIBLE);
                modelYrEtLayout.setVisibility(View.VISIBLE);
                engineEtLayout.setVisibility(View.VISIBLE);
                break;
            case "Cars":
                brandEtLayout.setVisibility(View.VISIBLE);
                modelEtLayout.setVisibility(View.VISIBLE);
                modelYrEtLayout.setVisibility(View.VISIBLE);
                transmissionTV.setVisibility(View.VISIBLE);
                transmissionRG.setVisibility(View.VISIBLE);
                engineEtLayout.setVisibility(View.VISIBLE);
                break;
            case "Trucks":
                brandEtLayout.setVisibility(View.VISIBLE);
                modelEtLayout.setVisibility(View.VISIBLE);
                modelYrEtLayout.setVisibility(View.VISIBLE);
                break;
            case "Vans & Buses":
                brandEtLayout.setVisibility(View.VISIBLE);
                modelEtLayout.setVisibility(View.VISIBLE);
                modelYrEtLayout.setVisibility(View.VISIBLE);
                break;
            case "Apartment & Flats":
                bedsEtLayout.setVisibility(View.VISIBLE);
                bathsEtLayout.setVisibility(View.VISIBLE);
                sizeEtLayout.setVisibility(View.VISIBLE);
                titleEtLayout.setVisibility(View.VISIBLE);
                break;
            case "Houses":
                bedsEtLayout.setVisibility(View.VISIBLE);
                bathsEtLayout.setVisibility(View.VISIBLE);
                landsizeEtLayout.setVisibility(View.VISIBLE);
                landUnitsTV.setVisibility(View.VISIBLE);
                landUitsSP.setVisibility(View.VISIBLE);
                housesizeEtLayout.setVisibility(View.VISIBLE);
                houseUnitsTV.setVisibility(View.VISIBLE);
                houseUitsSP.setVisibility(View.VISIBLE);
                titleEtLayout.setVisibility(View.VISIBLE);
                break;
            case "Plots & Land":
                landTypeTV.setVisibility(View.VISIBLE);
                landTypeSP.setVisibility(View.VISIBLE);
                landsizeEtLayout.setVisibility(View.VISIBLE);
                landUnitsTV.setVisibility(View.VISIBLE);
                landUitsSP.setVisibility(View.VISIBLE);
                titleEtLayout.setVisibility(View.VISIBLE);
                break;
        }


        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this, R.array.land_type, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        landTypeSP.setAdapter(typeAdapter);
        landTypeSP.setSelection(2);
        landTypeSP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                landType=landTypeSP.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter<CharSequence> HouseUnitsAdapter = ArrayAdapter.createFromResource(this, R.array.units, android.R.layout.simple_spinner_item);
        HouseUnitsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        houseUitsSP.setAdapter(HouseUnitsAdapter);
        houseUitsSP.setSelection(2);
        houseUitsSP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                houseSizeUnit=houseUitsSP.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter<CharSequence> LandUnitsAdapter = ArrayAdapter.createFromResource(this, R.array.units, android.R.layout.simple_spinner_item);
        LandUnitsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        landUitsSP.setAdapter(LandUnitsAdapter);
        landUitsSP.setSelection(2);
        landUitsSP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                landSizeUnit=landUitsSP.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }
                         //................. End of onCreate ...............//


                         //................. Image Upload ...............//
    public void deployPostImage(View view) {
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
                            ActivityCompat.requestPermissions(ProductAddActivity.this,new String[] {permissionType},REQUEST_CODE);
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
        post_image.getScaleType();
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAutoZoomEnabled(false)
                .setAspectRatio(10, 8)
                .start(ProductAddActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                postImageUri = result.getUri();
                post_image.setImageURI(postImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
                        //.................End of Image Upload ...............//

    public void getDeviceType(View view) {
        boolean status = ((RadioButton)view).isChecked();
        switch (view.getId()){
            case R.id.desktop:
                if (status){
                    deviceType="Desktop computer";
                }
                break;
            case R.id.laptop:
                if (status){
                    deviceType="Laptop/Netbook";
                }
                break;
            case R.id.tablet:
                if (status){
                    deviceType="Tablet";
                }
                break;
        }
    }


    public void transmissionType(View view) {
        boolean status = ((RadioButton)view).isChecked();
        switch (view.getId()){
            case R.id.manual:
                if (status){
                    transmissionType="Manual";
                }
                break;
            case R.id.Automatic:
                if (status){
                    transmissionType="Automatic";
                }
                break;
            case R.id.Other:
                if (status){
                    transmissionType="Other Transmission";
                }
                break;
        }
    }

    public void postAd(View view) {
        final String location=Objects.requireNonNull(locationET.getText()).toString();
        final String productName= Objects.requireNonNull(productNameET.getText()).toString();
        final String price;

        if (negotiableCB.isChecked()) {
            price = "\u09F3 " + Objects.requireNonNull(priceET.getText()).toString() + " (" + negotiableCB.getText().toString() + ")";
        } else {
            price = "\u09F3 " + Objects.requireNonNull(priceET.getText()).toString();
        }

        switch (checkItem) {
            case "Mobile Phones":
                description = "> Brand : "+Objects.requireNonNull(brandET.getText()).toString()+"\n"+"> Model : "+Objects.requireNonNull(modelET.getText()).toString()+"\n"+"> Description : "+Objects.requireNonNull(descriptionET.getText()).toString();
                Toast.makeText(this, ""+description, Toast.LENGTH_SHORT).show();
                break;
            case "Mobile Phone Accessories":
                description="> Title : "+Objects.requireNonNull(titleET.getText()).toString()+"\n"+"> Description : "+Objects.requireNonNull(descriptionET.getText());
                Toast.makeText(this, ""+description, Toast.LENGTH_SHORT).show();
                break;
            case "Computers & Tablets":
                description = "> Device Type : "+deviceType+"\n"+"> Brand : "+Objects.requireNonNull(brandET.getText()).toString()+"\n"+"> Model : "+Objects.requireNonNull(modelET.getText()).toString()+"\n"+"> Title : "+Objects.requireNonNull(titleET.getText()).toString()+"\n"+"> Description : "+Objects.requireNonNull(descriptionET.getText());
                Toast.makeText(this, ""+description, Toast.LENGTH_SHORT).show();
                break;
            case "TVs":
                description="> Brand : "+Objects.requireNonNull(brandET.getText()).toString()+"\n"+"> Model : "+Objects.requireNonNull(modelET.getText()).toString()+"\n"+"> Title : "+Objects.requireNonNull(titleET.getText()).toString()+"\n"+"> Description : "+Objects.requireNonNull(descriptionET.getText());
                break;
            case "Motorbikes & Scooters":
                description="> Brand : "+Objects.requireNonNull(brandET.getText()).toString()+"\n"+"> Model : "+Objects.requireNonNull(modelET.getText()).toString()+"\n"+"> Model Year : "+Objects.requireNonNull(modelYrET.getText()).toString()+"\n"+"> Engine Capacity (cc) : "+Objects.requireNonNull(engineET.getText()).toString()+"\n"+"> Description : "+Objects.requireNonNull(descriptionET.getText());
                break;
            case "Cars":
                description="> Brand : "+Objects.requireNonNull(brandET.getText()).toString()+"\n"+"> Model : "+Objects.requireNonNull(modelET.getText()).toString()+"\n"+"> Model Year : "+Objects.requireNonNull(modelYrET.getText()).toString()+"\n"+"> Transmission Type : "+transmissionType+"\n"+"> Engine Capacity (cc) : "+Objects.requireNonNull(engineET.getText()).toString()+"\n"+"> Description : "+Objects.requireNonNull(descriptionET.getText());
                break;
            case "Trucks":
                description="> Brand : "+Objects.requireNonNull(brandET.getText()).toString()+"\n"+"> Model : "+Objects.requireNonNull(modelET.getText()).toString()+"\n"+"> Model Year : "+Objects.requireNonNull(modelYrET.getText()).toString()+"\n"+"> Description : "+Objects.requireNonNull(descriptionET.getText());
                break;
            case "Vans & Buses":
                description="> Brand : "+Objects.requireNonNull(brandET.getText()).toString()+"\n"+"> Model : "+Objects.requireNonNull(modelET.getText()).toString()+"\n"+"> Model Year : "+Objects.requireNonNull(modelYrET.getText()).toString()+"\n"+"> Description : "+Objects.requireNonNull(descriptionET.getText());
                break;
            case "Apartment & Flats":
                description="> Beds : "+Objects.requireNonNull(bedsET.getText()).toString()+"\n"+"> Baths : "+Objects.requireNonNull(bathsET.getText()).toString()+"\n"+"> Size (sqft) : "+Objects.requireNonNull(sizeET.getText()).toString()+"\n"+"> Title : "+Objects.requireNonNull(titleET.getText()).toString()+"\n"+"> Description : "+Objects.requireNonNull(descriptionET.getText());
                break;
            case "Houses":
                description="> Beds : "+Objects.requireNonNull(bedsET.getText()).toString()+"\n"+"> Baths : "+Objects.requireNonNull(bathsET.getText()).toString()+"\n"+"> Land Size : "+Objects.requireNonNull(landsizeET.getText()).toString()+" "+landSizeUnit+"\n"+"> House Size : "+Objects.requireNonNull(housesizeET.getText()).toString()+" "+houseSizeUnit+"\n"+"> Title : "+Objects.requireNonNull(titleET.getText()).toString()+"\n"+"> Description : "+Objects.requireNonNull(descriptionET.getText());
                break;
            case "Plots & Land":
                description="> Land Type : "+landType+"\n"+"> Land Size : "+Objects.requireNonNull(landsizeET.getText()).toString()+" "+landSizeUnit+"\n"+"> Title : "+Objects.requireNonNull(titleET.getText()).toString()+"\n"+"> Description : "+Objects.requireNonNull(descriptionET.getText());
                break;
        }


                        //.................Upload to Firebase ...............//

        if (!TextUtils.isEmpty(location) && !TextUtils.isEmpty(productName) && !TextUtils.isEmpty(description) && !TextUtils.isEmpty(price) && postImageUri !=  null){

            mProgressBar.setVisibility(View.VISIBLE);
            final String randomeName = String.valueOf(System.currentTimeMillis());

            final StorageReference post_image_path = mStorageReference.child("post_images").child(randomeName+".jpg");
            post_image_path.putFile(postImageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    if (taskSnapshot.getTask().isSuccessful()){
                        post_image_path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                            @Override
                            public void onSuccess(Uri uri) {
                                postDownlUri= uri;
                            }
                        });

                        File photoFile = new File(postImageUri.getPath());
                        try {
                            compressedImageFile = new Compressor(ProductAddActivity.this)
                                    .setMaxHeight(100)
                                    .setMaxWidth(100)
                                    .setQuality(1)
                                    .compressToBitmap(photoFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] thumbData = baos.toByteArray();

                        final StorageReference uploadTask = mStorageReference.child("post_images/thumbs").child(randomeName+".jpg");
                        uploadTask.putBytes(thumbData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                if (taskSnapshot.getTask().isSuccessful()){
                                    uploadTask.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Uri thumbDownloadUri= uri;

                                            storeToFirestore(postDownlUri,thumbDownloadUri,productName,price,location,description,checkItem);
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
            Toast.makeText(this, "Image and Description both are required!", Toast.LENGTH_LONG).show();
            mProgressBar.setVisibility(View.INVISIBLE);
        }


    }

    private void storeToFirestore(Uri postDownlUri, Uri thumbDownloadUri, String productName, String price, String location, String desc, String checkItem) {
        final Map<String,Object> newPostMap = new HashMap<>();
        newPostMap.put("productName",productName);
        newPostMap.put("price",price);
        newPostMap.put("location",location);
        newPostMap.put("description",desc);
        newPostMap.put("checkItem",checkItem);
        newPostMap.put("postImage",postDownlUri.toString());
        newPostMap.put("thumbImage",thumbDownloadUri.toString());
        newPostMap.put("timestamp",FieldValue.serverTimestamp());
        newPostMap.put("userId",user_id);

        firebaseFirestore.collection("Posts").add(newPostMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(Task<DocumentReference> task) {
                if (task.isSuccessful()){
                    String currentId= Objects.requireNonNull(task.getResult()).getId();
                    newPostMap.put("postId",currentId);
                    firebaseFirestore.collection("Users").document(user_id).collection("Posts").document(currentId).set(newPostMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(ProductAddActivity.this, "Post have been published successfully!", Toast.LENGTH_SHORT).show();
                                Intent intent =new Intent(ProductAddActivity.this,MainActivity.class);
                                finish();
                                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                            else {
                                Toast.makeText(ProductAddActivity.this, "ERROR : "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
                else {
                    Toast.makeText(ProductAddActivity.this, "ERROR : "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

}
