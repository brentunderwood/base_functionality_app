package com.example.base_functionality_app.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.base_functionality_app.utilities_chess.StockfishEngineUtils;
import com.example.base_functionality_app.utilities_general.AppUtilities;
import com.example.base_functionality_app.R;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class Main extends Activity implements View.OnClickListener{
    public static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    StockfishEngineUtils sf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //needed for stockfish
        set_nnue_path();

        //start engine
        sf = new StockfishEngineUtils(this);
        sf.start_engine();

        Button button = findViewById(R.id.main_button);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // Handle button click events here
        if (v.getId() == R.id.main_button) {
            List<String> legal_moves = sf.get_legal_moves("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
            System.out.println(legal_moves);
        }
    }

    private void set_nnue_path(){
        AssetManager assetManager = getAssets();
        String nnueFileName = "nn-5af11540bbfe.nnue";
        String internalStoragePath = getFilesDir().getAbsolutePath() + File.separator + nnueFileName;

        try {
            InputStream inputStream = assetManager.open(nnueFileName);
            OutputStream outputStream = new FileOutputStream(internalStoragePath);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        AppUtilities.set_nnue_path(internalStoragePath);

    }
}