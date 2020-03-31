package com.example.artisphere;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.security.Key;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    //firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    //Storage
    StorageReference storageReference;
    //path where the profile photo will be stored
    String storagePath = "Users_Profile_Pictures/";

    //views from xml
    ImageView avatar;
    TextView usernameTv,nameTv,emailTv,dobTv,nationalityTv,phoneTv;
    FloatingActionButton floatingActionButton;

    //progress bar
    ProgressDialog pd;

    //permissions constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    //arrays of permission to be requested
    String cameraPermissions[];
    String storagePermissions[];

    //uri of selected image
    Uri image_uri;


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = getInstance().getReference();  // firebase storage reference

        //init arrays of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        //init views
        avatar = view.findViewById(R.id.avatar);
        usernameTv = view.findViewById(R.id.usernameTv);
        nameTv = view.findViewById(R.id.nameTv);
        emailTv = view.findViewById(R.id.emailTv);
        dobTv = view.findViewById(R.id.dobTv);
        nationalityTv = view.findViewById(R.id.nationalityTv);
        phoneTv = view.findViewById(R.id.phoneTv);
        floatingActionButton = view.findViewById(R.id.floating_action_button);

        //init progress bar
        pd = new ProgressDialog(getActivity());


        //getting info of the currently signed in user using email id
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //check until required data found
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    //get data
                    String image = ""+ds.child("dp").getValue();
                    String username = ""+ds.child("username").getValue();
                    String name = ""+ds.child("name").getValue();
                    String phone = ""+ds.child("phone").getValue();
                    String nationality = ""+ds.child("nationality").getValue();
                    String email = ""+ds.child("email").getValue();




                    //set data
                    usernameTv.setText(username);
                    nameTv.setText(name);
                    nationalityTv.setText(nationality);
                    emailTv.setText(email);
                    phoneTv.setText(phone);
                    try{
                        //if image is received successfully
                        Picasso.get().load(image).into(avatar);
                    }
                    catch (Exception e){
                        //if there comes some exception in receiving the image then set default
                        Picasso.get().load(R.drawable.ic_default_img_white).into(avatar);
                    }

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //setting the floating action button (edit button) in the profile fragment
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });



        return view;
    }

    private boolean checkStoragePermission(){
        //check if storage permission is enabled or not
        //if enabled, return true
        //if not, return false
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;

    }
    private void requestStoragePermission(){
        //request runtime storage permission
        requestPermissions(storagePermissions,STORAGE_REQUEST_CODE);

    }

    private boolean checkCameraPermission(){
        //check if camera permission is enabled or not
        //if enabled, return true
        //if not, return false
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;

    }
    private void requestCameraPermission(){
        //request runtime storage permission
        requestPermissions(cameraPermissions,CAMERA_REQUEST_CODE);

    }



    private void showEditProfileDialog() {

        /*Show dialog containing options
        1. Edit Profile Picture
        2. Edit Name
        3. Edit Phonenumber
        4. Edit username/identification
        5.Date of birth
        6.Nationality
        */

        //options to show in dialog
        String[] options = {"Profile Picture","Identification(Username)","Name","Phone","Date Of Birth","Nationality"};
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //set title
        builder.setTitle("Choose Action");
        //set items to the dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle dialog item clicks
                if (which==0){
                    //edit Identification(username)
                    pd.setMessage("Updating Profile");
                    showImageSelectionDialog();

                }
                else if (which==1){
                    //Edit profile picture
                    pd.setMessage("Updating Profile");

                }
                else if (which==2){
                    //edit Name
                    pd.setMessage("Updating Profile");
                    //calling method and pass key "name" as parameter to update name in profile
                    showNamePhoneUpdateDialog("name");

                }
                else if (which==3){
                    //edit Phone
                    pd.setMessage("Updating Profile");
                    showNamePhoneUpdateDialog("phone");

                }
                else if (which==4){
                    //edit Date  Of Birth
                    pd.setMessage("Updating Profile");

                }
                else if (which==5){
                    //edit Nationality
                    pd.setMessage("Updating Profile");

                }

            }
        });
        //create and show dialog
        builder.create().show();

    }

    private void showNamePhoneUpdateDialog(final String key) {
        /*parameter key will contain values - name or phone
        name - to update name
        phone - to update phone
         */

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update "+key); // update name or update phone
        //set dialog layout
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        //add edit text
        final EditText editText = new EditText(getActivity());
        editText.setHint("Enter "+key); //update name or update phone
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        //add buttons in dialog to update
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input text from edit text
                String value = editText.getText().toString().trim();

                if (!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String,Object> result = new HashMap<>();
                    result.put(key,value);

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // updated, dismiss progress
                                    pd.dismiss();
                                    Toast.makeText(getActivity(),"Profile Updated",Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed, dismiss error message, get and show the error
                            pd.dismiss();
                            Toast.makeText(getActivity(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });




                }
                else {
                    Toast.makeText(getActivity(),"Please Enter "+key,Toast.LENGTH_SHORT).show();
                }
            }
        });
        //add buttons in dialog to cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
                //create and show dialog
        builder.create().show();
    }

    private void showImageSelectionDialog() {
        //Show dialog containing the option of camera and gallery to select the image
        String[] options = {"Camera","Gallery"};
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //set title
        builder.setTitle("Take Image From");
        //set items to the dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle dialog item clicks
                if (which==0){
                    //Camera Clicked
                    if (!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else pickFromCamera();


                }
                else if (which==1){
                    //Gallery Clicked
                    if (!checkStoragePermission()){
                        requestStoragePermission();
                    }
                    else pickFromGallery();

                }


            }
        });
        //create and show dialog
        builder.create().show();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        /*This method is called when the user press allow or deny in the dialog box
            Here we will handle permission cases (alllowed & denied)
         */

        switch (requestCode){

            case CAMERA_REQUEST_CODE:{
                //picking from CAMERA, first check if camera and storage permissions are allowed
                if (grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted){
                        //permissions enabled
                        pickFromCamera();

                    }
                    else {
                        Toast.makeText(getActivity(),"Please enable camera and storage permissions",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                //picking from GALLERY, first check if storage permissions are allowed
                if (grantResults.length>0){
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted){
                        //permissions enabled
                        pickFromGallery();

                    }
                    else {
                        Toast.makeText(getActivity(),"Please enable storage permissions",Toast.LENGTH_SHORT).show();
                    }
                }


            }
            break;
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //This method will be picked after picking image from camera/gallery
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                //image is picked from gallery, get uri of image
                image_uri = data.getData();
                uploadProfilePhoto(image_uri);

            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE){
                //image is picked from camera, get uri of image
                uploadProfilePhoto(image_uri);

            }
        }



        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfilePhoto(Uri image_uri) {
        //show progress
        pd.show();
        /* "image_uri" contains the uri of the image
        we will use the cuurerunt user's uid to name the image
         */
        //path and name of the image to be stored in firebase storage
        String filePath_andName = storagePath+""+"dp_"+user.getUid();

        StorageReference storageReference2 = storageReference.child(filePath_andName);
        storageReference2.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //image is uploaded in the storage, now get its url and store it in the user's database
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                Uri downloadUri = uriTask.getResult();

                //check if the image is uploaded or not and url is received
                if (uriTask.isSuccessful()){
                    //image uploaded successfully
                    //  add/update url in users database
                    HashMap<String,Object> results = new HashMap<>();
                    results.put("dp",downloadUri.toString());

                    databaseReference.child(user.getUid()).updateChildren(results)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //url in database of user is added successfully
                                    //dismiss progress bar
                                    pd.dismiss();
                                    Toast.makeText(getActivity(),"Image Updated",Toast.LENGTH_SHORT).show();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //error adding url in database of user
                            //dismiss progress bar
                            pd.dismiss();
                            Toast.makeText(getActivity(),"Error Updating Image",Toast.LENGTH_SHORT).show();

                        }
                    });
                }
                else {
                    //error
                    pd.dismiss();
                    Toast.makeText(getActivity(),"Some error occured",Toast.LENGTH_SHORT).show();
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ///there were some errors, get and show the error message, dismiss progress dialog
                        pd.dismiss();
                        Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void pickFromGallery() {
        //pick from GAllery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_CODE);
    }
    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp Desc");
        //put image uri
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        //intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);

    }
}
