package org.dyndns.warenix.hkg;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.View;

import com.example.android.bitmapfun.util.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

/**
 * Created by warenix on 9/10/14.
 */
public class URLImageParser implements Html.ImageGetter {

    Context c;
    View container;

    /**
     * Construct the URLImageParser which will execute AsyncTask and refresh the container
     */
    public URLImageParser(View t, Context c) {
        this.c = c;
        this.container = t;
    }

    public Drawable getDrawable(String source) {
        URLDrawable urlDrawable = new URLDrawable();

        // get the actual source
        ImageGetterAsyncTask asyncTask =
            new ImageGetterAsyncTask(urlDrawable);

        asyncTask.execute(source);

        // return reference to URLDrawable where I will change with actual image from
        // the src tag
        return urlDrawable;
    }

    public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable> {

        URLDrawable urlDrawable;

        public ImageGetterAsyncTask(URLDrawable d) {
            this.urlDrawable = d;
        }

        @Override
        protected Drawable doInBackground(String... params) {
            String source = params[0];
            return fetchDrawable(source);
        }

        @Override
        protected void onPostExecute(Drawable result) {
            if (result == null) {
                return;
            }
            // set the correct bound according to the result from HTTP call
            urlDrawable.setBounds(0, 0, 0 + result.getIntrinsicWidth(), 0
                                                                        + result
                .getIntrinsicHeight());

            // change the reference of the current drawable to the result
            // from the HTTP call
            if (urlDrawable.drawable != null && urlDrawable.drawable instanceof URLDrawable) {
                Bitmap bitmap = ((URLDrawable) urlDrawable.drawable).getBitmap();
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }

            }
            urlDrawable.drawable = result;

            // redraw the image by invalidating the container
            URLImageParser.this.container.invalidate();
        }

        /**
         * Get the Drawable from URL
         */
        public Drawable fetchDrawable(String urlString) {
            try {
                InputStream is = fetch(urlString);
                Drawable drawable = Drawable.createFromStream(is, "src");
                drawable.setBounds(0, 0, 0 + drawable.getIntrinsicWidth(), 0
                                                                           + drawable
                    .getIntrinsicHeight());
                return drawable;
            } catch (Exception e) {
                return null;
            }
        }

        private InputStream fetch(String urlString) throws MalformedURLException, IOException {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet request = new HttpGet(urlString);
            HttpResponse response = httpClient.execute(request);
            return response.getEntity().getContent();
        }
    }
}
