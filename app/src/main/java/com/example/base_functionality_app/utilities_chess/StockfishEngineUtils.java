package com.example.base_functionality_app.utilities_chess;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.example.base_functionality_app.utilities_general.AppUtilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StockfishEngineUtils {

    private Process stockfishProcess;
    private BufferedReader stockfishInput;
    private OutputStreamWriter stockfishOutput;

    private Context context;

    public StockfishEngineUtils(Context context) {
        this.context = context;
    }


    public void start_engine() {
        try {
            // Get the path for the native library (executable)
            ApplicationInfo appInfo = context.getApplicationInfo();
            String nativeLibDir = appInfo.nativeLibraryDir;
            String executablePath = nativeLibDir + "/stockfish.so";

            System.out.println(executablePath);
            // Create a File instance for the executable
            File stockfishFile = new File(executablePath);

            if (!stockfishFile.exists()) {
                // Handle if the executable file doesn't exist
                System.out.println("Stockfish executable file not found");
                return;
            }

            // Start Stockfish process using the native library
            stockfishProcess = new ProcessBuilder(stockfishFile.getAbsolutePath()).start();
            System.out.println("Process started");

            // Initialize input and output streams
            stockfishInput = new BufferedReader(new InputStreamReader(stockfishProcess.getInputStream()));
            stockfishOutput = new OutputStreamWriter(stockfishProcess.getOutputStream());
            System.out.println("I/O initialized");

            // Send 'uci' command to Stockfish and initialize options
            send_command("uci");
            send_command("setoption name EvalFile value " + AppUtilities.get_nnue_path());

            System.out.println("Stockfish was successful");
        } catch (IOException e) {
            System.out.println("Stockfish is broken");
            e.printStackTrace();
            // Handle exceptions (file not found, etc.) accordingly
        }
    }


    // Method to send commands to Stockfish
    public void send_command(String command) {
        try {
            stockfishOutput.write(command + "\n");
            stockfishOutput.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public List<String> get_legal_moves(String fen){
        List<String> move_list = new ArrayList<>();

        send_command("position fen " + fen);
        send_command("go perft 1");

        try {
            String line;
            Pattern pattern = Pattern.compile("[a-h]\\d[a-h]\\d");
            while ((line = stockfishInput.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()){
                    move_list.add(matcher.group(0));
                }
                if (line.contains("Nodes searched")){
                    break;
                }
            }
            return move_list;
        } catch (IOException e) {
            System.out.println("error displaying response");
            e.printStackTrace();
        }
        return move_list;
    }

    // Method to analyze a position. Type can be "depth" or "time"
    // value is in plies or milliseconds
    public void fixed_analysis(String fen, String type, int value) {
        List<String> move_list = get_legal_moves(fen);

        send_command("setoption name MultiPV value " + move_list.size());
        send_command("position fen " + fen);

        if (type.equals("depth")){
            send_command("go depth " + value);
        }else if(type.equals("time")){
            send_command("go movetime " + value);
        }

    }

    // Method to stop the Stockfish engine
    public void stop_engine() {
        send_command("quit");
        try {
            stockfishInput.close();
            stockfishOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void dump_response(){
        try {
            String response = "";
            String line;
            while ((line = stockfishInput.readLine()) != null) {
                System.out.println(line);
            }

        } catch (IOException e) {
            System.out.println("error displaying response");
            e.printStackTrace();
        }

    }

    // Method to receive and display Stockfish response as a toast
    public List<String> extract_analysis_results() {
        List<String> results = new ArrayList<>();;
        String multipv_number;

        try {
            String line;
            Pattern pattern = Pattern.compile("multipv \\d+");
            while ((line = stockfishInput.readLine()) != null) {
                if (line.contains("info") & line.contains("multipv")){
                    Matcher matcher = pattern.matcher(line);
                    matcher.find();
                    multipv_number = matcher.group(0);
                    multipv_number = multipv_number.substring(8, multipv_number.length());

                    if(results.size() < Integer.valueOf(multipv_number)){
                        results.add(line);
                    }else{
                        results.set(Integer.valueOf(multipv_number)-1, line);
                    }
                }
                if (line.contains("bestmove")){
                    break;
                }

            }
            return results;
        } catch (IOException e) {
            System.out.println("error displaying response");
            e.printStackTrace();
        }
        return results;
    }


}