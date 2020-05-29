package com.example.artisphere;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.artisphere.Model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static androidx.constraintlayout.widget.Constraints.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    ImageView image_profile;
    TextView username, name, email, phone, bio;

    ImageButton std_repo,shared_repo,marked_work_repo,right_button;
    FloatingActionButton floatingActionButton;

    FirebaseUser firebaseUser;
    String profileid;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        profileid = prefs.getString("profileid","none");

        image_profile = view.findViewById(R.id.image_profile);
        username = view.findViewById(R.id.username);
        name = view.findViewById(R.id.name);
        email = view.findViewById(R.id.email);
        phone = view.findViewById(R.id.phone);
        bio = view.findViewById(R.id.bio);
        std_repo = view.findViewById(R.id.std_repo);
        shared_repo = view.findViewById(R.id.shared_repo);
        marked_work_repo = view.findViewById(R.id.marked_work_repo);
        right_button = view.findViewById(R.id.right_button);

        userInfo();

        if (profileid.equals(firebaseUser.getUid())){
            right_button.setImageResource(R.drawable.ic_edit_profile);
            right_button.setTag("edit_profile");
        }
        else {
            checkFollow();
            marked_work_repo.setVisibility(View.GONE);
        }

        right_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tag = right_button.getTag().toString();

                if (tag.equals("edit_profile")){
                    //go to edit profile
                }
                else if (tag.equals("follow")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("Following").child(profileid).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileid)
                            .child("followers").child(firebaseUser.getUid()).setValue(true);
                }
                else if (tag.equals("unfollow")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("Following").child(profileid).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileid)
                            .child("followers").child(firebaseUser.getUid()).removeValue();
                }
            }
        });


        return view;
    }

    private void userInfo(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(profileid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (getContext() == null){
                    return;
                }
                User user = dataSnapshot.getValue(User.class);

                Glide.with(getContext()).load(user.getDp()).into(image_profile);
                username.setText(user.getUsername());
                name.setText(user.getName());
                email.setText(user.getEmail());
                phone.setText(user.getPhone());
                bio.setText(user.getBio());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void checkFollow(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(profileid).exists()) {
                    right_button.setImageResource(R.drawable.ic_unfollow);
                    right_button.setTag("unfollow");
                }
                else {
                    right_button.setImageResource(R.drawable.ic_follow);
                    right_button.setTag("follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}
