package com.example.ifty.myproducts;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 */
public class CategoryFilterFragment extends Fragment implements ProductAdapter.FavouriteCheck {

    private RecyclerView filterRecycler;
    private ArrayList<Product> productList;
    private FirebaseFirestore firebaseFirestore;
    private ProductAdapter productAdapter = null;
    private FirebaseAuth mAuth;
    private DocumentSnapshot lastVisible;
    private Boolean isNewPostFirstLoad = true;
    private String checkItem;
    private String userId;
    private boolean isPostId=false;

    public CategoryFilterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_category_filter, container, false);

        checkItem = getArguments().getString("checkItem");
        Toast.makeText(getContext(), "checkItem....."+checkItem, Toast.LENGTH_SHORT).show();


        filterRecycler=view.findViewById(R.id.filterRecycler);
        productList=new ArrayList<>();

        productAdapter = new ProductAdapter(container.getContext(),productList,this);
        filterRecycler.setLayoutManager(new GridLayoutManager(getActivity(),2));
        filterRecycler.setAdapter(productAdapter);

        mAuth=FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser()!=null){
            userId = mAuth.getCurrentUser().getUid();
        }

        filterRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean reachedBottom = !recyclerView.canScrollVertically(1);
                if (reachedBottom){

                    loadMorePost();
                }
            }
        });

        firebaseFirestore=FirebaseFirestore.getInstance();
        Query firstQuery = firebaseFirestore.collection("Posts").whereEqualTo("checkItem",checkItem).limit(3);
        firstQuery.addSnapshotListener(Objects.requireNonNull(getActivity()), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()){
                    if (isNewPostFirstLoad){
                        lastVisible = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() -1);
                    }
                    for (DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){
                        if (doc.getType()==DocumentChange.Type.ADDED){
                            String blogPostId = doc.getDocument().getId();
                            Product product = doc.getDocument().toObject(Product.class).withId(blogPostId);

                            if (isNewPostFirstLoad){
                                productList.add(product);
                            }
                            else {
                                productList.add(0,product);
                            }

                            productAdapter.notifyDataSetChanged();
                        }
                    }
                    isNewPostFirstLoad=false;
                }
            }
        });

        return view;
    }

    public void loadMorePost(){
        Query nextQuery = firebaseFirestore.collection("Posts")
                .whereEqualTo("checkItem",checkItem).startAfter(lastVisible)
                .limit(3);
        nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null){
                    if(!queryDocumentSnapshots.isEmpty()){
                        lastVisible = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() -1);
                        for (DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){
                            if (doc.getType()==DocumentChange.Type.ADDED){
                                String blogPostId = doc.getDocument().getId();
                                Product product = doc.getDocument().toObject(Product.class).withId(blogPostId);
                                productList.add(product);

                                productAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }

            }
        });
    }



   /* @Override
    public boolean favouriteCheck(final String post_id, int numb) {

        if (mAuth.getCurrentUser() != null) {
            firebaseFirestore.collection("Customers").document(userId).collection("Favourites").document(post_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            isPostId = true;
                            Toast.makeText(getContext(), "yes", Toast.LENGTH_SHORT).show();

                        } else {
                            //Log.d(TAG, "No such document");
                            isPostId = false;
                            Toast.makeText(getContext(), "no", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        //Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });

            return isPostId;
        } else {
            return false;
        }
    }*/

    @Override
    public boolean favouriteCheck(final String post_id, int numb) {

        if (mAuth.getCurrentUser() != null) {
            firebaseFirestore.collection("Customers").document(userId).collection("Favourites").document(post_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            isPostId = true;
                            //Toast.makeText(getContext(), "yes", Toast.LENGTH_SHORT).show();

                        } else {
                            //Log.d(TAG, "No such document");
                            isPostId = false;
                            //Toast.makeText(getContext(), "no", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        //Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });

            if (!isPostId) {

                if (numb == 1) {
                    firebaseFirestore.collection("Posts").document(post_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                if (Objects.requireNonNull(task.getResult()).exists()) {
                                    String product_name = task.getResult().getString("productName");
                                    String price = task.getResult().getString("price");
                                    String desc = task.getResult().getString("description");
                                    String check_item = task.getResult().getString("checkItem");
                                    String user_id = task.getResult().getString("userId");
                                    String product_image = task.getResult().getString("postImage");
                                    String thumb_image = task.getResult().getString("thumbImage");

                                    Map<String, String> favMap = new HashMap<>();
                                    favMap.put("productName", Objects.requireNonNull(product_name));
                                    favMap.put("price", Objects.requireNonNull(price));
                                    favMap.put("description", Objects.requireNonNull(desc));
                                    favMap.put("postId", post_id);
                                    favMap.put("checkItem", Objects.requireNonNull(check_item));
                                    favMap.put("userId", Objects.requireNonNull(user_id));
                                    favMap.put("postImage", Objects.requireNonNull(product_image));
                                    favMap.put("thumbImage", Objects.requireNonNull(thumb_image));

                                    firebaseFirestore.collection("Customers").document(userId).collection("Favourites").document(post_id).set(favMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                //Toast.makeText(getContext(), "Added to Favourite list!", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getContext(), "ERROR : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                                } else {
                                    Toast.makeText(getActivity(), "Error : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getContext(), "Error : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }

                return false;

            } else {
                if (numb == 1) {
                    firebaseFirestore.collection("Customers").document(userId).collection("Favourites").document(post_id).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //Toast.makeText(getContext(), "removed from favourite.", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }
                //isPostId=false;
                return true;
            }
        } else {
            return false;
        }
    }

   /* @Override
    public void myFave(boolean isFav, final String postId, final ImageView img) {


        if (!isFav) {
            //userId, craete
            firebaseFirestore.collection("Posts").document(postId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (Objects.requireNonNull(task.getResult()).exists()) {
                            String product_name = task.getResult().getString("productName");
                            String price = task.getResult().getString("price");
                            String desc = task.getResult().getString("description");
                            String check_item = task.getResult().getString("checkItem");
                            String user_id = task.getResult().getString("userId");
                            String product_image = task.getResult().getString("postImage");
                            String thumb_image = task.getResult().getString("thumbImage");

                            Map<String, String> favMap = new HashMap<>();
                            favMap.put("productName", Objects.requireNonNull(product_name));
                            favMap.put("price", Objects.requireNonNull(price));
                            favMap.put("description", Objects.requireNonNull(desc));
                            favMap.put("postId", postId);
                            favMap.put("checkItem", Objects.requireNonNull(check_item));
                            favMap.put("userId", Objects.requireNonNull(user_id));
                            favMap.put("postImage", Objects.requireNonNull(product_image));
                            favMap.put("thumbImage", Objects.requireNonNull(thumb_image));

                            firebaseFirestore.collection("Customers").document(userId).collection("Favourites").document(postId).set(favMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "Added to Favourite list!", Toast.LENGTH_SHORT).show();
                                        Glide.with(CategoryFilterFragment.this).load(R.drawable.favourite_red).into(img);
                                    } else {
                                        Toast.makeText(getContext(), "ERROR : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(getActivity(), "Error : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Error : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } else {
            //delete
            firebaseFirestore.collection("Customers").document(userId).collection("Favourites").document(postId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "removed from favourite.", Toast.LENGTH_SHORT).show();
                        Glide.with(CategoryFilterFragment.this).load(R.drawable.favourite_gray).into(img);
                    }
                }
            });

        }

    }*/
}
