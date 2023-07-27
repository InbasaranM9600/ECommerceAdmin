package com.example.ecommerceadmin;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.ecommerceadmin.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private String id, title, description, price;
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.AddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title = binding.title.getText().toString();
                description = binding.description.getText().toString();
                price = binding.price.getText().toString();
                addProduct();
            }
        });

        // Use the "image" ImageView to select an image from the device's storage
        binding.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        // Use the "ChooseImage" button to trigger the actual image upload
        binding.ChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }

    private void chooseImage() {
        if (uri != null) {
            id = UUID.randomUUID().toString();
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("products/" + id + ".png");
            storageReference.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            storageReference.getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            FirebaseFirestore.getInstance()
                                                    .collection("products")
                                                    .document(id)
                                                    .update("image", uri.toString())
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            // Image uploaded successfully
                                                            Toast.makeText(MainActivity.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    });
                        }
                    });
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void addProduct() {
        id = UUID.randomUUID().toString();
        ProductModel productModel = new ProductModel(id, title, description, null, true);
        FirebaseFirestore.getInstance()
                .collection("products")
                .document(id)
                .set(productModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Product added successfully
                        Toast.makeText(MainActivity.this, "Product Added", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uri = data.getData();
            binding.image.setImageURI(uri);
        }
    }
}
