package com.example.ifty.myproducts;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MyPreferences {

    private static MyPreferences myPreferences;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    ArrayList<String> postIdList = null;

    private MyPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences("my_shared_preferences", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.apply();
    }

    public static MyPreferences getPreferences(Context context) {
        if (myPreferences == null) myPreferences = new MyPreferences(context);
        return myPreferences;
    }

    public void setPostIdList(ArrayList<String> postIdSet){
        Set<String> set = new HashSet<String>();
        set.addAll(postIdSet);
        editor.putStringSet("key", set);
        editor.commit();
    }

    public ArrayList<String> getPostIdList(){
        //if no data is available for Config.USER_NAME then this getString() method returns
        //a default value that is mentioned in second parameter
        Set<String> set = sharedPreferences.getStringSet("key", null);
        if (set!=null){
            postIdList = new ArrayList<>(set);
        }
        return postIdList;
    }
}
