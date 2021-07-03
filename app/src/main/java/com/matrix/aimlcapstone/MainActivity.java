package com.matrix.aimlcapstone;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import org.tensorflow.lite.Interpreter;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private static final int USE_PHOTO = 1001;
    private static final int START_CAMERA = 1002;
    private String camera_image_path;
    private Uri mImageCaptureUri;
    private ImageView show_image;
    private TextView result_text;
    private String assets_path = "lite_images";
    private boolean load_result = false;
    private int[] ddims = {1, 256, 256, 3};//{1, 3, 256, 256};//{1, 3, 224, 224};
    private int model_index = 0;
    private List<String> resultLabel = new ArrayList<>();
    private Interpreter tflite = null;

    private static final String[] PADDLE_MODEL = {
            "Custom"//, "mobilenet_v2"
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);

        init_view();
        readCacheLabelFromLocalFile();
    }

    // initialize view
    private void init_view() {
        request_permissions();
        show_image = (ImageView) findViewById(R.id.show_image);
        result_text = (TextView) findViewById(R.id.result_text);
        result_text.setMovementMethod(ScrollingMovementMethod.getInstance());
        Button load_model = (Button) findViewById(R.id.load_model);
        Button use_photo = (Button) findViewById(R.id.use_photo);
        Button start_photo = (Button) findViewById(R.id.start_camera);

        load_model.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });

        // use photo click
        use_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!load_result) {
                    Toast.makeText(MainActivity.this, "never load model", Toast.LENGTH_SHORT).show();
                    return;
                }
                PhotoUtil.use_photo(MainActivity.this, USE_PHOTO);
            }
        });

        // start camera click
        start_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!load_result) {
                    Toast.makeText(MainActivity.this, "never load model", Toast.LENGTH_SHORT).show();
                    return;
                }
                //camera_image_path = PhotoUtil.start_camera(MainActivity.this, START_CAMERA);
                if (ActivityCompat.checkSelfPermission(
                        getApplicationContext(),
                        Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                            MainActivity.this, new String[] {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA
                    }, START_CAMERA);
                } else {
                    //isProgressShowed = false;
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, "MyPicture");
                    values.put(
                            MediaStore.Images.Media.DESCRIPTION,
                            "Photo taken on " + System.currentTimeMillis()
                    );
                    mImageCaptureUri = getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            values
                    );

                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    takePicture.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                    startActivityForResult(takePicture, START_CAMERA);
                }
            }
        });
    }

    /**
     * Memory-map the model file in Assets.
     */
    private MappedByteBuffer loadModelFile(String model) throws IOException {
        AssetFileDescriptor fileDescriptor = getApplicationContext().getAssets().openFd(model + ".tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }


    // load infer model
    private void load_model(String model) {
        try {
            tflite = new Interpreter(loadModelFile(model));
            Toast.makeText(MainActivity.this, model + " model load success", Toast.LENGTH_SHORT).show();
            Log.d(TAG, model + " model load success");
            tflite.setNumThreads(4);
            load_result = true;
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, model + " model load fail", Toast.LENGTH_SHORT).show();
            Log.d(TAG, model + " model load fail");
            load_result = false;
            e.printStackTrace();
        }
    }

    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        // set dialog title
        builder.setTitle("Please select model");

        // set dialog icon
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        // able click other will cancel
        builder.setCancelable(true);

        // cancel button
        builder.setNegativeButton("cancel", null);

        // set list
        builder.setSingleChoiceItems(PADDLE_MODEL, model_index, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                model_index = which;
                load_model("Custom_6");//PADDLE_MODEL[model_index]);
                dialog.dismiss();
            }
        });

        // show dialog
        builder.show();
    }


    private void readCacheLabelFromLocalFile() {
        try {
            AssetManager assetManager = getApplicationContext().getAssets();
            BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open("Label.txt")));
            String readLine = null;
            while ((readLine = reader.readLine()) != null) {
                System.out.println("Lines:::: "+readLine);
                resultLabel.add(readLine);
            }
            reader.close();
        } catch (Exception e) {
            Log.e("labelCache", "error " + e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String image_path;
        RequestOptions options = new RequestOptions().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case USE_PHOTO:
                    if (data == null) {
                        Log.w(TAG, "user photo data is null");
                        return;
                    }
                    Uri image_uri = data.getData();
                    System.out.println("URI::::: "+ image_uri);
                    Glide.with(MainActivity.this).load(image_uri).apply(options)
                    .into(show_image);
                    // get image path from uri
                    image_path = PhotoUtil.get_path_from_URI(MainActivity.this, image_uri);
                    // predict image
                    predict_image(image_path);
                    break;
                case START_CAMERA:
                    // show photo
                    System.out.println("URI::::: "+ camera_image_path);
                    Uri image_uri_2  = mImageCaptureUri;
                    Glide.with(MainActivity.this).load(image_uri_2).apply(options)//camera_image_path).apply(options)
                    .into(show_image);

                    image_path = PhotoUtil.get_path_from_URI(MainActivity.this, image_uri_2);
                    // predict image
                    predict_image(image_path);//camera_image_path);
                    break;
            }
        }
    }

    //  predict image
    private void predict_image(String image_path) {
        // picture to float array
        Bitmap bmp = PhotoUtil.getScaleBitmap(image_path);
        System.out.println("Bitmap:::::::::;;; "+ bmp);
        ByteBuffer inputData = PhotoUtil.getScaledMatrix(bmp, ddims);
        try {
            // Data format conversion takes too long
            // Log.d("inputData", Arrays.toString(inputData));

            //float[] output = Array(1) { FloatArray(labels.size) }
            //float[] labelProbArray = {1,1};

            float[][] labelProbArray = new float[1][2];//[1001];
            long start = System.currentTimeMillis();
            // get predict result
            tflite.run(inputData, labelProbArray);
            long end = System.currentTimeMillis();
            long time = end - start;
            System.out.println("Result:::::::::::::::::::::::::: "+ labelProbArray[0].length +" "+ labelProbArray[0][0]+" "+ labelProbArray[0][1]+ " "+ labelProbArray[0]);
            float[] results = new float[labelProbArray[0].length];
            //float[] results = new float[labelProbArray.length];
            System.arraycopy(labelProbArray[0], 0, results, 0, labelProbArray[0].length);
            //System.arraycopy(labelProbArray[0], 0, results, 0, labelProbArray.length);
            // show predict result and time
            int r = get_max_result(results);
            //String show_text = "result：" + r + "\nname：" + resultLabel.get(r) + "\nprobability：" + results[r]*100 + "\ntime：" + time + "ms";
            String show_text = "result：" + r + "\nname：" + resultLabel.get(r) + "\ntime：" + time + "ms";
            result_text.setText(show_text);
        } catch (Exception e) {
            System.out.println("Error Predicting result:"+e);
            e.printStackTrace();
        }
    }

        // get max probability label
        private int get_max_result(float[] result) {
            float probability = result[0];
            int r = 0;
            System.out.println("Result::::: 0: "+ result.length);

            for (int i = 0; i < result.length; i++) {
                System.out.println("Result::::: 1: "+ result[i]+" "+i);
                if (probability < result[i]) {
                    probability = result[i];
                    r = i;
                    System.out.println("Result::::: 2: "+ result[i]+" "+i);
                }
            }
            System.out.println("Result::::: 3: "+ result[r] +" "+ r);
            return r;
        }

        // request permissions
        private void request_permissions() {

            List<String> permissionList = new ArrayList<>();
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.CAMERA);
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }

            // if list is not empty will request permissions
            if (!permissionList.isEmpty()) {
                ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), 1);
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            switch (requestCode) {
                case 1:
                    if (grantResults.length > 0) {
                        for (int i = 0; i < grantResults.length; i++) {

                            int grantResult = grantResults[i];
                            if (grantResult == PackageManager.PERMISSION_DENIED) {
                                String s = permissions[i];
                                Toast.makeText(this, s + " permission was denied", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    break;
            }
        }
    }