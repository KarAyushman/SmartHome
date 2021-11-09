package com.ethan.smarthome;

import android.content.Context;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;



public class database {

    FileReader fileReader = null;
    FileWriter fileWriter = null;
    BufferedReader bufferedReader = null;
    BufferedWriter bufferedWriter = null;
    File file;
    File myDir;
    String response = null;

    public void init(Context context, String filename){
        myDir = new File(context.getFilesDir(), "ETHAN");
        if(!myDir.exists()){
            myDir.mkdir();
        }
        file = new File(myDir, filename);

    }

    public String readFile(){
        if(!file.exists()){
            try{
                file.createNewFile();
                fileWriter = new FileWriter(file.getAbsoluteFile());
                bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write("{}");
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                StringBuilder output = new StringBuilder();
                fileReader = new FileReader(file.getAbsoluteFile());
                bufferedReader = new BufferedReader(fileReader);

                String line;

                while((line = bufferedReader.readLine())!=null){
                    output.append(line).append("\n");
                }

                response = output.toString();
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return (response);
    }

    public void writeFile(String data){
        try {
            fileWriter = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fileWriter);
            bw.write(data);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
