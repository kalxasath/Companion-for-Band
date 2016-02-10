package com.pimp.companionforband;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.microsoft.band.tiles.TileEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;

    Camera.PictureCallback jpegCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        int permissionCheck = ContextCompat.checkSelfPermission(CameraActivity.this,
                Manifest.permission.CAMERA);

        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(CameraActivity.this, getString(R.string.camera_permission), Toast.LENGTH_LONG).show();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            return;
        }

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();

        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        jpegCallback = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                FileOutputStream outStream = null;
                try {
                    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "CompanionForBand" + File.separator + "Camera");
                    if (!file.exists() && !file.isDirectory())
                        file.mkdirs();

                    outStream = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath()
                            + File.separator + "CompanionForBand" + File.separator
                            + "Camera" + File.separator + System.currentTimeMillis() + ".jpg");

                    outStream.write(data);
                    outStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                }

                Toast.makeText(getApplicationContext(), getString(R.string.picture_saved) + " CompanionForBand/Camera",
                        Toast.LENGTH_LONG).show();
                refreshCamera();
            }
        };
    }

    public void captureImage() throws IOException {
        camera.takePicture(null, null, jpegCallback);
    }

    public void refreshCamera() {
        if (surfaceHolder.getSurface() == null) {
            return;
        }

        try {
            camera.stopPreview();
        } catch (Exception e) {
        }

        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera = Camera.open();
        } catch (RuntimeException e) {
            System.err.println(e);
            return;
        }

        Camera.Parameters params;
        params = camera.getParameters();

        List<Camera.Size> sizes = params.getSupportedPictureSizes();
        Camera.Size size = sizes.get(0);
        for (int i = 0; i < sizes.size(); i++) {
            if (sizes.get(i).width > size.width)
                size = sizes.get(i);
        }
        params.setPictureSize(size.width, size.height);
        params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);

        camera.setParameters(params);

        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        refreshCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera = null;
    }


    private void processIntent(Intent intent) {
        String extraString = intent.getStringExtra("new intent");

        if (extraString != null && extraString.equals("TileEventReceiver")) {
            if (intent.getAction() == TileEvent.ACTION_TILE_BUTTON_PRESSED) {
                try {
                    captureImage();
                } catch (Exception e) {
                    //
                }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        processIntent(intent);
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getIntent() != null && getIntent().getExtras() != null) {
            processIntent(getIntent());
        }
    }

    public void CameraClick(View view) {
        Camera.Parameters params;
        params = camera.getParameters();

        ImageButton cameraSwitch = (ImageButton) view.findViewById(R.id.camera_switch);
        ImageButton flashSwitch = (ImageButton) view.findViewById(R.id.flash_switch);
        switch (view.getId()) {
            case R.id.camera_switch:
                int cameraId = -1;
                switch (cameraSwitch.getContentDescription().toString()) {
                    case "to Rear":
                        cameraId = findFrontFacingCamera(true);
                        cameraSwitch.setImageResource(R.drawable.ic_camera_rear_white_48dp);
                        cameraSwitch.setContentDescription("to Front");
                        break;
                    case "to Front":
                        cameraId = findFrontFacingCamera(false);
                        cameraSwitch.setImageResource(R.drawable.ic_camera_front_white_48dp);
                        cameraSwitch.setContentDescription("to Rear");
                        break;
                }
                if (cameraId < 0) {
                    Toast.makeText(this, "Camera not found.",
                            Toast.LENGTH_LONG).show();
                    return;
                } else {
                    surfaceDestroyed(surfaceHolder);
                    camera = Camera.open(cameraId);
                }

                params = camera.getParameters();

                List<Camera.Size> sizes = params.getSupportedPictureSizes();
                Camera.Size size = sizes.get(0);
                for (int i = 0; i < sizes.size(); i++) {
                    if (sizes.get(i).width > size.width)
                        size = sizes.get(i);
                }
                params.setPictureSize(size.width, size.height);
                camera.setParameters(params);

                try {
                    camera.setPreviewDisplay(surfaceHolder);
                    camera.startPreview();
                    cameraSwitch.setImageResource(R.drawable.ic_camera_rear_white_48dp);
                } catch (Exception e) {
                    System.err.println(e);
                }
                break;
            case R.id.flash_switch:
                String string = flashSwitch.getContentDescription().toString();
                switch (string) {
                    case "Flash Auto":
                        params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                        flashSwitch.setContentDescription("Flash On");
                        flashSwitch.setImageResource(R.drawable.ic_flash_on_white_48dp);
                        break;
                    case "Flash On":
                        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        flashSwitch.setContentDescription("Flash Off");
                        flashSwitch.setImageResource(R.drawable.ic_flash_off_white_48dp);
                        break;
                    case "Flash Off":
                        params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                        flashSwitch.setContentDescription("Flash Auto");
                        flashSwitch.setImageResource(R.drawable.ic_flash_auto_white_48dp);
                        break;
                }
                camera.setParameters(params);
                break;
        }
    }

    private int findFrontFacingCamera(boolean b) {
        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (b) {
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    cameraId = i;
                    break;
                }
            } else {
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    cameraId = i;
                    break;
                }
            }
        }
        return cameraId;
    }
}