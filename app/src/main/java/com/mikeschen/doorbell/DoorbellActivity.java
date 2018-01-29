package com.mikeschen.doorbell;

import android.app.Activity;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.KeyEvent;

import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.contrib.driver.button.ButtonInputDriver;

import java.io.IOException;
import java.nio.ByteBuffer;

public class DoorbellActivity extends Activity {

    private DoorbellCamera mCamera;
    private ButtonInputDriver mButtonInputDriver;
    private static final String TAG = "Mike";

//    private final String BUTTON_GPIO_PIN = "GPIO6_IO14";

    private Handler mCameraHandler;
    private HandlerThread mCameraThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCameraThread = new HandlerThread("CameraBackground");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());
        Log.d(TAG, "Doorbell Activity created.");

        initPIO();
        mCamera = DoorbellCamera.getInstance();
        mCamera.initializeCamera(this, mCameraHandler, mOnImageAvailableListener);
    }

    private void initPIO() {
        try {
            mButtonInputDriver = new ButtonInputDriver(
                    BoardDefaults.getGPIOForButton(),
                    Button.LogicState.PRESSED_WHEN_LOW,
                    KeyEvent.KEYCODE_ENTER);
            mButtonInputDriver.register();
        } catch (IOException e) {
            mButtonInputDriver = null;
            Log.w(TAG, "Could not open GPIO pins", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mButtonInputDriver.close();
        } catch (IOException e) {
            Log.e(TAG, "button driver error", e);
        }
        mCameraThread.quitSafely();
        mCamera.shutDown();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d(TAG, "button pressed!!!!!!!");
        Log.d(TAG, "event" + KeyEvent.KEYCODE_ENTER);
        Log.d(TAG, "keyCode" + keyCode);
//        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            // Doorbell rang!
            Log.d(TAG, "SNAP!!!!!!!");
            mCamera.takePicture();
            return true;
//        }
//        return super.onKeyUp(keyCode, event);
    }

    // Callback to receive captured camera image data
    private ImageReader.OnImageAvailableListener mOnImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    // Get the raw image bytes
                    Image image = reader.acquireLatestImage();
                    ByteBuffer imageBuf = image.getPlanes()[0].getBuffer();
                    final byte[] imageBytes = new byte[imageBuf.remaining()];
                    imageBuf.get(imageBytes);
                    image.close();

                    onPictureTaken(imageBytes);
                }
            };

    private void onPictureTaken(final byte[] imageBytes) {
        if (imageBytes != null) {
            // ...process the captured image...
        }
    }
}

