package com.example.d060753.scanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    static final String IMAGE_FOR_CROP = "image_for_crop";
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int SEND_IMAGE = 2;

    CropImageView mImageView;
    GridView gridView;
    ArrayList<Bitmap> imageCollection = new ArrayList<Bitmap>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button picBtn = (Button) findViewById(R.id.takePicBtn);
        setBtnListenerOrDisable(picBtn, mTakePicOnClickListener, MediaStore.ACTION_IMAGE_CAPTURE);
        mImageView = (CropImageView) findViewById(R.id.cropImageView);

        gridView = (GridView)findViewById(R.id.gridView);

    }

    public void sendForCrop(String picPath) {
        Intent intent = new Intent(MainActivity.this, DisplayMessageActivity.class);
        intent.putExtra(IMAGE_FOR_CROP, picPath);
        startActivityForResult(intent, SEND_IMAGE);
    }

    Button.OnClickListener mTakePicOnClickListener = new Button.OnClickListener(){

        @Override
        public void onClick(View v) {
            dispatchTakePictureIntent();
        }
    };
    File file;
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            //file = new File(Environment.getExternalStorageDirectory()+File.separator + "temp_image_scanner.jpg");
            try {
                file =createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (file != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //Bitmap imageBitmap = decodeSampledBitmapFromFile(file.getAbsolutePath(), 1000, 700);
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
            sendForCrop(file.getAbsolutePath());
        }

        if (requestCode == SEND_IMAGE && resultCode == RESULT_OK) {
            Bitmap image = decodeSampledBitmapFromFile(data.getStringExtra("IMAGE_PATH"), 1000, 700);
            imageCollection.add(image);

            ImageAdapter imageAdapter = new ImageAdapter(this, R.layout.document, imageCollection);
            gridView.setAdapter(imageAdapter);

        }
    }

    public class ImageAdapter extends BaseAdapter {
        private ArrayList<Bitmap> imageCollection;
        private Context context;
        private int layoutResourceId;
        public ImageAdapter(Context context,  int layoutResourceId,ArrayList<Bitmap> imageCollection) {
            this.imageCollection = imageCollection;
            this.context = context;
            this.layoutResourceId = layoutResourceId;
        }

        @Override
        public int getCount() {
            return imageCollection.size();
        }

        @Override
        public Bitmap getItem(int position) {
            return imageCollection.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            TextView textView;
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            View row = inflater.inflate(layoutResourceId, parent, false);


            imageView = (ImageView) row.findViewById(R.id.item_image);

            imageView.setLayoutParams(new GridView.LayoutParams(200, 200));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);

            textView = (TextView) row.findViewById(R.id.item_text);
            textView.setText("Document " + position);

            Bitmap imageBitmap = getItem(position);
            imageView.setImageBitmap(imageBitmap);

            return row;
        }
    }

    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight)
    { // BEST QUALITY MATCH

        //First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize, Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        int inSampleSize = 1;

        if (height > reqHeight)
        {
            inSampleSize = Math.round((float)height / (float)reqHeight);
        }
        int expectedWidth = width / inSampleSize;

        if (expectedWidth > reqWidth)
        {
            //if(Math.round((float)width / (float)reqWidth) > inSampleSize) // If bigger SampSize..
            inSampleSize = Math.round((float)width / (float)reqWidth);
        }

        options.inSampleSize = inSampleSize;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, options);
    }

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void setBtnListenerOrDisable(
            Button btn,
            Button.OnClickListener onClickListener,
            String intentName
    ) {
        if (isIntentAvailable(this, intentName)) {
            btn.setOnClickListener(onClickListener);
        } else {
            btn.setText("Not Available");
            btn.setClickable(false);
        }
    }

    public static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
}
