package com.example.base_functionality_app.utilities_general;

import android.app.Application;


public class AppUtilities extends Application {
    private static String nnue_path;

    public static void set_nnue_path(String path){
        nnue_path = path;
    }

    public static String get_nnue_path(){
        return nnue_path;
    }

}

