package com.example.ifty.myproducts;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

import javax.annotation.Nullable;

public class HomeFragment extends Fragment {
    private RecyclerView product_recycler_list;
    private ArrayList<Product> productList;
    private FirebaseFirestore firebaseFirestore;
    private ProductAdapter productAdapter = null;
    private FirebaseAuth mAuth;
    private DocumentSnapshot lastVisible;
    private Boolean isNewPostFirstLoad = true;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        product_recycler_list=view.findViewById(R.id.products_recycler_list);
        productList=new ArrayList<>();

        productAdapter = new ProductAdapter(container.getContext(),productList);
        product_recycler_list.setLayoutManager(new GridLayoutManager(getActivity(),2));
        product_recycler_list.setAdapter(productAdapter);

        mAuth=FirebaseAuth.getInstance();

            product_recycler_list.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);
                    if (reachedBottom){
                        Toast.makeText(container.getContext(), "Reached"+lastVisible.getString("desc"), Toast.LENGTH_SHORT).show();
                        loadMorePost();
                    }
                }
            });

            firebaseFirestore=FirebaseFirestore.getInstance();
            Query firstQuery = firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(3);
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
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
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

}
