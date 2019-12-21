package org.redstudios.objecthunt;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.SystemClock;
import android.util.Size;
import android.util.TypedValue;

import org.redstudios.objecthunt.eviroment.BorderedText;
import org.redstudios.objecthunt.eviroment.Logger;
import org.redstudios.objecthunt.tf.Classifier;
import org.redstudios.objecthunt.tf.Classifier.GameMode;

import java.io.IOException;
import java.util.List;

public class ClassifierActivity extends CameraActivity implements OnImageAvailableListener {
  private static final Logger LOGGER = new Logger();
  private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
  private static final float TEXT_SIZE_DIP = 10;
  private Bitmap rgbFrameBitmap = null;
  private Integer sensorOrientation;
  private Classifier classifier;
  private BorderedText borderedText;
    private int tresholdAccuracy = 60;
    private int foundObjs = 0;

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

    recreateClassifier();
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

      if (targetObjPercentage > tresholdAccuracy) {
          classifier.popPeekObject();
          if (classifier.checkEmptyQueue())
              openGameOverScreen();
          foundObjs++;
      }

    runInBackground(
        new Runnable() {
          @Override
          public void run() {
            if (classifier != null) {
              final long startTime = SystemClock.uptimeMillis();
              final List<Classifier.Recognition> results =
                  classifier.recognizeImage(rgbFrameBitmap, sensorOrientation);
              LOGGER.v("Detect: %s", results);

              runOnUiThread(
                  new Runnable() {
                    @Override
                    public void run() {
                      showResultsInBottomSheet(results);
                        updateProgressBar(targetObjPercentage);
                        if (targetObjPercentage > tresholdAccuracy) {
                            addPoints(getCurrentPoints() + 100 + foundObjs * 25);
                            updateTextViewTargetObject(classifier.getPeekObject());
                        }
                    }
                  });
            }
            readyForNextImage();
          }
        });
  }

  @Override
  protected void onInferenceConfigurationChanged() {
    if (rgbFrameBitmap == null) {
      // Defer creation until we're getting camera frames.
      return;
    }
    runInBackground(() -> recreateClassifier());
  }

  private void recreateClassifier() {
    if (classifier != null) {
      LOGGER.d("Closing classifier.");
      classifier.close();
      classifier = null;
    }
    try {
      LOGGER.d(
          "Creating classifier (numThreads=2)");
      classifier = new Classifier(this, GameMode.OFFICE);
        updateProgressBar(0);
        updateTextViewTargetObject(classifier.getPeekObject());
    } catch (IOException e) {
      LOGGER.e(e, "Failed to create classifier.");
    }
  }
}
