package com.soundleader.apprtctest.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Utils {

    public static String getRandomString(int strLength){
        char[] list = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890".toCharArray();
        String randomS = "";

        for (int i = 0 ; i < strLength ; i++) {
            Random ran= new Random();
            int ranPos = ran.nextInt(list.length);
            randomS += list[ranPos];
        }

        return randomS;
    }

    public static String Decrypt(String text, String key) throws Exception

    {

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        byte[] keyBytes= new byte[16];

        byte[] b= key.getBytes("UTF-8");

        int len= b.length;

        if (len > keyBytes.length) len = keyBytes.length;

        System.arraycopy(b, 0, keyBytes, 0, len);

        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

        IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);

        cipher.init(Cipher.DECRYPT_MODE,keySpec,ivSpec);





//               BASE64Decoder decoder = new BASE64Decoder();

//               Base64.decode(input, flags)

//               byte [] results = cipher.doFinal(decoder.decodeBuffer(text));

        // BASE64Decoder decoder = new BASE64Decoder();

        // Base64.decode(input, flags)

        byte [] results = cipher.doFinal(Base64.decode(text, 0));



        return new String(results,"UTF-8");

    }



    public static String Encrypt(String text, String key) throws Exception

    {

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        byte[] keyBytes= new byte[16];

        byte[] b= key.getBytes("UTF-8");

        int len= b.length;

        if (len > keyBytes.length) len = keyBytes.length;

        System.arraycopy(b, 0, keyBytes, 0, len);

        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

        IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);

        cipher.init(Cipher.ENCRYPT_MODE,keySpec,ivSpec);



        byte[] results = cipher.doFinal(text.getBytes("UTF-8"));

//               BASE64Encoder encoder = new BASE64Encoder();

//               return encoder.encode(results);



        return Base64.encodeToString(results, 0);

    }



    public static void doWrite(Context context, String fileTitle, String msg) throws Exception {
        if(!checkAvailable()){
            return;
        }

        if(!checkWritable()){
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());
        String fileName = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)+"/"+currentDateandTime+"_"+fileTitle+"+.txt";
        File file = new File(fileName);
//        File file = context.getFileStreamPath(currentDateandTime +".txt");
        Log.d("Utils_L",file.getAbsolutePath());
        if (!file.exists()) {
            file.createNewFile();
        }

        FileWriter fw = null ;
        BufferedWriter bufwr = null ;
        fw = new FileWriter(file, true);
        bufwr = new BufferedWriter(fw) ;

        bufwr.write(msg) ;
        bufwr.newLine() ;

        // write data to the file.
        bufwr.flush() ;
        if (bufwr != null) {
            bufwr.close();
        }

        if (fw != null) {
            fw.close();
        }
    }


    public static boolean checkAvailable() {

        // Retrieving the external storage state
        String state = Environment.getExternalStorageState();

        // Check if available
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static boolean checkWritable() {
        // Retrieving the external storage state
        String state = Environment.getExternalStorageState();

        // Check if writable
        if (Environment.MEDIA_MOUNTED.equals(state)){
            if(Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                return false;
            }else{
                return true;
            }
        }
        return false;
    }

    public static String getNowDateTime(){

        //msec
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String formatDate = sdfNow.format(date);

        return formatDate;
    }

}
