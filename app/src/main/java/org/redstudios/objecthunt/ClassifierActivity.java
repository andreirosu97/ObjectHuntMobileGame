package org.redstudios.objecthunt;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Size;
import android.util.TypedValue;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.redstudios.objecthunt.eviroment.BorderedText;
import org.redstudios.objecthunt.eviroment.Logger;
import org.redstudios.objecthunt.model.GameMode;
import org.redstudios.objecthunt.model.ImageFoundDialog;
import org.redstudios.objecthunt.tf.Classifier;

import java.io.IOException;
import java.util.List;

public class ClassifierActivity extends CameraActivity implements OnImageAvailableListener {

    private static final int NEXT_IMAGE_REQUEST_CODE = 421;
    private static final Logger LOGGER = new Logger();
    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
    private static final float TEXT_SIZE_DIP = 10;
    private Bitmap rgbFrameBitmap = null;
    private Integer sensorOrientation;
    private Classifier classifier;
    private BorderedText borderedText;
    private Handler mHandler = new Handler();
    private Boolean isPostedEndGame = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameMode = (GameMode) getIntent().getExtras().getSerializable("GameMode");
    }

    @Override
    protected int getLayoutId() {
        return R.layout.camera_connection_fragment;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        LOGGER.d("onPreviewSizeChosen");
        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        createClassifier();
        if (classifier == null) {
            LOGGER.e("No classifier on preview!");
            return;
        }

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        sensorOrientation = rotation - getScreenOrientation();
        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
    }

    @Override
    protected void processImage() {
        LOGGER.d("processImage");
        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
        final int imageSizeX = classifier.getImageSizeX();
        final int imageSizeY = classifier.getImageSizeY();
        final int cropSize = Math.min(previewWidth, previewHeight);
        int targetObjPercentage = (int) (float) classifier.getTargetObjPercentage();

        if (targetObjPercentage > thresholdAccuracy) {
            totalCurrentPoints = getCurrentPoints() + 100 + foundObjects.size() * 25;
            addFoundObject(classifier.popPeekObject());
            isObjectFound = true;
            addTime(20 - foundObjects.size());
            //TODO Play sound or sth +- vibrate flash the screen in one color
        }

        runInBackground(
                () -> {
                    if (classifier != null) {
                        final long startTime = SystemClock.uptimeMillis();
                        final List<Classifier.Recognition> results =
                                classifier.recognizeImage(rgbFrameBitmap, sensorOrientation);
                        LOGGER.v("Detect: %s", results);

                        runOnUiThread(
                                () -> {
                                    showResultsInBottomSheet(results);
                                    updateProgressBar(targetObjPercentage);
                                    if (targetObjPercentage > thresholdAccuracy) {
                                        updateTotalPoints();
                                    }
                                });
                    }
                    if (isObjectFound) {
                        Intent dialogIntent = new Intent(this, ImageFoundDialog.class);
                        dialogIntent.putExtra("ObjectName", foundObjects.get(foundObjects.size() - 1));
                        startActivityForResult(dialogIntent, NEXT_IMAGE_REQUEST_CODE);
                    } else {
                        readyForNextImage();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NEXT_IMAGE_REQUEST_CODE) {
            if (classifier.isEmptyQueue()) {
                //TODO You won the game wow
                openGameOverScreen();
            } else {
                LOGGER.d("Ready for next image");
                isObjectFound = false;
                updateTextViewTargetObject(classifier.getPeekObject());
                readyForNextImage();
            }

        }
    }

    private Runnable endGame = () -> openGameOverScreen();

    @Override
    protected void onInferenceConfigurationChanged() {
        if (rgbFrameBitmap == null) {
            // Defer creation until we're getting camera frames.
            return;
        }
        runInBackground(() -> createClassifier());
    }

    private void createClassifier() {
        if (classifier != null) {
            return;
        }
        try {
            classifier = new Classifier(this, gameMode);
            updateProgressBar(0);
            updateTextViewTargetObject(classifier.getPeekObject());
        } catch (IOException e) {
            LOGGER.e(e, "Failed to create classifier.");
        }
    }

    private void addFoundObject(String foundObject) {
        foundObjects.add(foundObject);
    }

    private void doExit() {
        new MaterialAlertDialogBuilder(this, R.style.AlertDialogOnExit)
                .setIcon(R.drawable.warning_icon)
                .setTitle("Giving up ?")
                .setMessage("What do you want to do ?\nContinue the game\nEnd the game, go to the end screen\nDrop the game, all your progress will be lost")
                .setPositiveButton("Continue", null)
                .setNeutralButton("End game", (DialogInterface dialog, int which) -> openGameOverScreen())
                .setNegativeButton("Drop game", (DialogInterface dialog, int which) -> finish())
                .show();
    }

    @Override
    public void onBackPressed() {
        doExit();
    }
}
