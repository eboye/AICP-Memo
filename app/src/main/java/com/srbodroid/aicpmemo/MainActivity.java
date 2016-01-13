package com.srbodroid.aicpmemo;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;

import java.util.UUID;

public class MainActivity extends AppCompatActivity implements OnClickListener{

    private DrawingView drawView;
    private int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE;
    private int selectedColorR = 38;
    private int selectedColorG = 50;
    private int selectedColorB = 56;
    private int selectedColorRGB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FullScreencall();
        setContentView(R.layout.activity_main);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendImage(intent); // Handle single image being sent
            }
        } else {
            // Handle other intents, such as being started from the home screen
//            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//            fab.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
//                }
//            });

            drawView = (DrawingView)findViewById(R.id.drawing);

            drawView.setBrushSize(10);

            FloatingActionButton brush_size = (FloatingActionButton) findViewById(R.id.draw_btn);
            brush_size.setOnClickListener(this);

            FloatingActionButton erase_btn = (FloatingActionButton) findViewById(R.id.erase_btn);
            erase_btn.setOnClickListener(this);

            FloatingActionButton color_fill = (FloatingActionButton) findViewById(R.id.color_fill);
            color_fill.setOnClickListener(this);

            FloatingActionButton color_pick = (FloatingActionButton) findViewById(R.id.color_pick);
            color_pick.setOnClickListener(this);

            FloatingActionButton save_btn = (FloatingActionButton) findViewById(R.id.save_btn);
            save_btn.setOnClickListener(this);

//            FloatingActionButton screenShotButton = new FloatingActionButton(this);
//
//            if (RootUtil.isDeviceRooted()){
//                final FloatingActionMenu fabMenu = (FloatingActionMenu) findViewById(R.id.menu);
//                fabMenu.addMenuButton(screenShotButton);
//            }

        }

    }

    private void handleSendImage(Intent intent) {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // We will need to request the permission
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Explain to the user why we need to read the storage
                    Toast whyWeNeedPermission = Toast.makeText(getApplicationContext(),
                            "We need permission to access that image.", Toast.LENGTH_SHORT);
                    whyWeNeedPermission.show();
                }
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);


            } else {
                // The permission is granted, we can perform the action
                drawView = (DrawingView)findViewById(R.id.drawing);
                drawView.placeImage(imageUri);
            }
        }
    }

    @Override
    public void onClick(View view){
    //respond to clicks
        int defaultColorG;
        int defaultColorB;
        int defaultColorR;
        if(view.getId() == R.id.color_fill){
            defaultColorR = selectedColorR;
            defaultColorG = selectedColorG;
            defaultColorB = selectedColorB;
            final ColorPicker cp = new ColorPicker(MainActivity.this, defaultColorR, defaultColorG, defaultColorB);
            cp.show();
            /* On Click listener for the dialog, when the user select the color */
            Button okColor = (Button)cp.findViewById(R.id.okColorButton);
            okColor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                /* You can get single channel (value 0-255) */
                    selectedColorR = cp.getRed();
                    selectedColorG = cp.getGreen();
                    selectedColorB = cp.getBlue();

                /* Or the android RGB Color (see the android Color class reference) */
                    selectedColorRGB = cp.getColor();
                    drawView.startNew(selectedColorRGB);

                    cp.dismiss();
                    closeFab();
                }
            });
        } else if(view.getId() == R.id.color_pick){
            defaultColorR = selectedColorR;
            defaultColorG = selectedColorG;
            defaultColorB = selectedColorB;
            final ColorPicker cp = new ColorPicker(MainActivity.this, defaultColorR, defaultColorG, defaultColorB);
            cp.show();
            /* On Click listener for the dialog, when the user select the color */
            Button okColor = (Button)cp.findViewById(R.id.okColorButton);
            okColor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                /* You can get single channel (value 0-255) */
                    selectedColorR = cp.getRed();
                    selectedColorG = cp.getGreen();
                    selectedColorB = cp.getBlue();

                /* Or the android RGB Color (see the android Color class reference) */
                    selectedColorRGB = cp.getColor();
                    String hexColor = String.format("#%06X",(0xFFFFFF & selectedColorRGB));
                    drawView.setColor(hexColor);

                    cp.dismiss();
                    closeFab();
                }
            });
        } else if(view.getId()==R.id.draw_btn){
            //draw button clicked
            final Dialog brushDialog = new Dialog(this);
            brushDialog.setTitle("Brush size:");
            brushDialog.setContentView(R.layout.brush_size);

            final SeekBar seekBar = (SeekBar)brushDialog.findViewById(R.id.seekBar);
            int size = (Math.round(drawView.getLastBrushSize()) + 5) * 10;
            final ImageView brushPreview = (ImageView)brushDialog.findViewById(R.id.brush_preview);
            seekBar.setProgress(size);
            LinearLayout.LayoutParams sizeParams = new LinearLayout.LayoutParams(size,size);
            brushPreview.setLayoutParams(sizeParams);

            SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    final int width = (progress + 5) * 10;
                    final int height = (progress + 5) * 10;
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,height);
                    brushPreview.setLayoutParams(params);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            };

            seekBar.setOnSeekBarChangeListener(seekBarChangeListener);

            Button setSize = (Button)brushDialog.findViewById(R.id.get_size);
            setSize.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int finalSize = seekBar.getProgress();
                    drawView.setBrushSize(finalSize);
                    drawView.setLastBrushSize(finalSize);
                    drawView.setErase(false);
                    brushDialog.hide();
                    closeFab();
                }
            });

            brushDialog.show();
        } else if(view.getId()==R.id.erase_btn){
            //switch to erase - choose size
            final Dialog brushDialog = new Dialog(this);
            brushDialog.setTitle("Eraser size:");
            brushDialog.setContentView(R.layout.brush_size);

            final SeekBar seekBar = (SeekBar)brushDialog.findViewById(R.id.seekBar);
            int size = (Math.round(drawView.getLastBrushSize()) + 5) * 10;
            final ImageView brushPreview = (ImageView)brushDialog.findViewById(R.id.brush_preview);
            seekBar.setProgress(size);
            LinearLayout.LayoutParams sizeParams = new LinearLayout.LayoutParams(size,size);
            brushPreview.setLayoutParams(sizeParams);

            SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    final int width = (progress + 5) * 10;
                    final int height = (progress + 5) * 10;
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,height);
                    brushPreview.setLayoutParams(params);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            };

            seekBar.setOnSeekBarChangeListener(seekBarChangeListener);

            Button setSize = (Button)brushDialog.findViewById(R.id.get_size);
            setSize.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int finalSize = seekBar.getProgress();
                    drawView.setBrushSize(finalSize);
                    drawView.setLastBrushSize(finalSize);
                    drawView.setErase(true);
                    brushDialog.hide();
                    closeFab();
                }
            });

            brushDialog.show();

        } else if(view.getId()==R.id.save_btn){
            //save drawing
            AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
            saveDialog.setTitle("Save drawing");
            saveDialog.setMessage("Save drawing to device Gallery?");
            saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    //save drawing
                    drawView.setDrawingCacheEnabled(true);
                    String imgSaved = MediaStore.Images.Media.insertImage(
                            getContentResolver(), drawView.getDrawingCache(),
                            UUID.randomUUID().toString()+".png", "drawing");
                    if(imgSaved!=null){
                        Toast savedToast = Toast.makeText(getApplicationContext(),
                                "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
                        savedToast.show();
                    }
                    else{
                        Toast unsavedToast = Toast.makeText(getApplicationContext(),
                                "Oops! Image could not be saved.", Toast.LENGTH_SHORT);
                        unsavedToast.show();
                    }
                    drawView.destroyDrawingCache();
                }
            });
            saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.dismiss();
                }
            });
            saveDialog.show();
        }
    }

    public void FullScreencall() {
        if(Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if(Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    public void closeFab(){
        final FloatingActionMenu fabMenu = (FloatingActionMenu) findViewById(R.id.menu);
        fabMenu.close(true);
    }
}
