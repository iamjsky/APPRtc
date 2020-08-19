package com.soundleader.apprtctest.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageLoader extends AsyncTask< Object, Void, Bitmap > {
    ImageView ivPreview = null;

    @Override
    protected Bitmap doInBackground(Object... params ) {
        this.ivPreview = (ImageView) params[0];
        String url = (String) params[1];
        System.out.println(url);
        return loadBitmap( url );
    }

    @Override
    protected void onPostExecute( Bitmap result ) {
        super.onPostExecute( result );
        ivPreview.setImageBitmap( result );
    }

    public Bitmap loadBitmap( String url ) {
        URL newurl = null;
        Bitmap bitmap = null;
        try {
            newurl = new URL( url );
            bitmap = BitmapFactory.decodeStream( newurl.openConnection( ).getInputStream( ) );
        } catch ( MalformedURLException e ) {
            e.printStackTrace( );
        } catch ( IOException e ) {
            e.printStackTrace( );
        }
        return bitmap;
    }

}

