
package org.redstudios.objecthunt.tf;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Log;

import org.redstudios.objecthunt.eviroment.Logger;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.nnapi.NnApiDelegate;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeOp.ResizeMethod;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.image.ops.Rot90Op;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import static android.content.ContentValues.TAG;

/**
 * A classifier specialized to label images using TensorFlow Lite.
 */
public class Classifier {
    /**
     * The quantized model does not require normalization, thus set mean as 0.0f, and std as 1.0f to
     * bypass the normalization.
     */
    private Float target_perc = 0.0f;
    Queue<String> targetObjects;
    private static final float IMAGE_MEAN = 0.0f;

    private static final float IMAGE_STD = 1.0f;

    /** The model type used for classification. */
    public enum GameMode {
        OFFICE,
        OUTDOOR
    }

    /**
     * Quantized MobileNet requires additional dequantization to the output probability.
     */
    private static final float PROBABILITY_MEAN = 0.0f;

    private static final float PROBABILITY_STD = 255.0f;


    private static final Logger LOGGER = new Logger();

    protected final String MODEL_PATH = "mobilenet_v1_1.0_224_quant.tflite";

    protected final String LABEL_PATH = "labels.txt";

    protected final Integer NUM_THREADS = 1;

    /**
     * Number of results to show in the UI.
     */
    private static final int MAX_RESULTS = 3;

    /**
     * The loaded TensorFlow Lite model.
     */
    private MappedByteBuffer tfliteModel;

    /**
     * Image size along the x axis.
     */
    private final int imageSizeX;

    /**
     * Image size along the y axis.
     */
    private final int imageSizeY;

    /**
     * Optional GPU delegate for accleration.
     */
    private GpuDelegate gpuDelegate = null;

    /**
     * Optional NNAPI delegate for accleration.
     */
    private NnApiDelegate nnApiDelegate = null;

    /**
     * An instance of the driver class to run model inference with Tensorflow Lite.
     */
    protected Interpreter tflite;

    /**
     * Options for configuring the Interpreter.
     */
    private final Interpreter.Options tfliteOptions = new Interpreter.Options();

    /**
     * Labels corresponding to the output of the vision model.
     */
    private List<String> labels;

    /**
     * The list of labes that we are interested in detecting
     */
    private List<String> labelFilter;

    /**
     * Input image TensorBuffer.
     */
    private TensorImage inputImageBuffer;

    /**
     * Output probability TensorBuffer.
     */
    private final TensorBuffer outputProbabilityBuffer;

    /**
     * Processer to apply post processing of the output probability.
     */
    private final TensorProcessor probabilityProcessor;

    /**
     * An immutable result returned by a Classifier describing what was recognized.
     */
    public static class Recognition {
        /**
         * A unique identifier for what has been recognized. Specific to the class, not the instance of
         * the object.
         */
        private final String id;

        /**
         * Display name for the recognition.
         */
        private final String title;

        /**
         * A sortable score for how good the recognition is relative to others. Higher should be better.
         */
        private final Float confidence;

        /**
         * Optional location within the source image for the location of the recognized object.
         */
        private RectF location;

        public Recognition(
                final String id, final String title, final Float confidence, final RectF location) {
            this.id = id;
            this.title = title;
            this.confidence = confidence;
            this.location = location;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public Float getConfidence() {
            return confidence;
        }

        public RectF getLocation() {
            return new RectF(location);
        }

        public void setLocation(RectF location) {
            this.location = location;
        }

        @Override
        public String toString() {
            String resultString = "";
            if (id != null) {
                resultString += "[" + id + "] ";
            }

            if (title != null) {
                resultString += title + " ";
            }

            if (confidence != null) {
                resultString += String.format("(%.1f%%) ", confidence * 100.0f);
            }

            if (location != null) {
                resultString += location + " ";
            }

            return resultString.trim();
        }
    }

    public Classifier(Activity activity, GameMode gameMode) throws IOException {
        //Load model out from the model file.
        tfliteModel = FileUtil.loadMappedFile(activity, MODEL_PATH);
        tfliteOptions.setNumThreads(NUM_THREADS);
        tflite = new Interpreter(tfliteModel, tfliteOptions);

        // Loads labels out from the label file.
        readLabelsAndLabelFilter(activity,gameMode);

        // Reads type and shape of input and output tensors, respectively.
        int imageTensorIndex = 0;
        int[] imageShape = tflite.getInputTensor(imageTensorIndex).shape(); // {1, height, width, 3}
        imageSizeY = imageShape[1];
        imageSizeX = imageShape[2];
        DataType imageDataType = tflite.getInputTensor(imageTensorIndex).dataType();
        int probabilityTensorIndex = 0;
        int[] probabilityShape =
                tflite.getOutputTensor(probabilityTensorIndex).shape(); // {1, NUM_CLASSES}
        DataType probabilityDataType = tflite.getOutputTensor(probabilityTensorIndex).dataType();

        // Creates the input tensor.
        inputImageBuffer = new TensorImage(imageDataType);

        // Creates the output tensor and its processor.
        outputProbabilityBuffer = TensorBuffer.createFixedSize(probabilityShape, probabilityDataType);

        // Creates the post processor for the output probability.
        probabilityProcessor = new TensorProcessor.Builder().add(getPostprocessNormalizeOp()).build();

        targetObjects = getSampleObjects(10);
        LOGGER.d("Created a Tensorflow Lite Image Classifier.");
    }

    /**
     * Runs inference and returns the classification results.
     */
    public List<Recognition> recognizeImage(final Bitmap bitmap, int sensorOrientation) {
        Log.d("ANDREI", "recognizeImage");
        // Logs this method so that it can be analyzed with systrace.
        Trace.beginSection("recognizeImage");

        Trace.beginSection("loadImage");
        long startTimeForLoadImage = SystemClock.uptimeMillis();
        inputImageBuffer = loadImage(bitmap, sensorOrientation);
        long endTimeForLoadImage = SystemClock.uptimeMillis();
        Trace.endSection();
        LOGGER.v("Timecost to load the image: " + (endTimeForLoadImage - startTimeForLoadImage));

        // Runs the inference call.
        Trace.beginSection("runInference");
        long startTimeForReference = SystemClock.uptimeMillis();
        tflite.run(inputImageBuffer.getBuffer(), outputProbabilityBuffer.getBuffer().rewind());
        long endTimeForReference = SystemClock.uptimeMillis();
        Trace.endSection();
        LOGGER.v("Timecost to run model inference: " + (endTimeForReference - startTimeForReference));

        Log.d(TAG, "recognizeImage: " + probabilityProcessor.process(outputProbabilityBuffer) + " " + probabilityProcessor.process(outputProbabilityBuffer).getShape()[1]);
        // Gets the map of label and probability.
        Map<String, Float> labeledProbability =
                new TensorLabel(labels, probabilityProcessor.process(outputProbabilityBuffer))
                        .getMapWithFloatValue();

        for (Map.Entry<String, Float> entry : labeledProbability.entrySet()) {
            String k = entry.getKey();
            Float v = entry.getValue();
            if (k.equals(targetObjects.peek())) {
                target_perc = 100 * v;
            }
        }
        Trace.endSection();

        // Gets top-k results.
        return getTopKProbability(labeledProbability);
    }

    /**
     * Closes the interpreter and model to release resources.
     */
    public void close() {
        if (tflite != null) {
            tflite.close();
            tflite = null;
        }
        if (gpuDelegate != null) {
            gpuDelegate.close();
            gpuDelegate = null;
        }
        if (nnApiDelegate != null) {
            nnApiDelegate.close();
            nnApiDelegate = null;
        }
        tfliteModel = null;
    }

    /**
     * Get the image size along the x axis.
     */
    public int getImageSizeX() {
        return imageSizeX;
    }

    /**
     * Get the image size along the y axis.
     */
    public int getImageSizeY() {
        return imageSizeY;
    }

    /**
     * Loads input image, and applies preprocessing.
     */
    private TensorImage loadImage(final Bitmap bitmap, int sensorOrientation) {
        // Loads bitmap into a TensorImage.
        inputImageBuffer.load(bitmap);

        // Creates processor for the TensorImage.
        int cropSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
        int numRoration = sensorOrientation / 90;
        // TODO(b/143564309): Fuse ops inside ImageProcessor.
        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        .add(new ResizeWithCropOrPadOp(cropSize, cropSize))
                        .add(new ResizeOp(imageSizeX, imageSizeY, ResizeMethod.BILINEAR))
                        .add(new Rot90Op(numRoration))
                        .add(getPreprocessNormalizeOp())
                        .build();
        return imageProcessor.process(inputImageBuffer);
    }

    /**
     * Gets the top-k results.
     */
    private static List<Recognition> getTopKProbability(Map<String, Float> labelProb) {

        ArrayList<String> filter = new ArrayList<>();
        filter.add("mouse");
        filter.add("monitor");
        filter.add("smartphone");
        filter.add("computer keyboard");
        // Find the best classifications.
        PriorityQueue<Recognition> pq =
                new PriorityQueue<>(
                        MAX_RESULTS,
                        new Comparator<Recognition>() {
                            @Override
                            public int compare(Recognition lhs, Recognition rhs) {
                                // Intentionally reversed to put high confidence at the head of the queue.
                                return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                            }
                        });

        for (Map.Entry<String, Float> entry : labelProb.entrySet()) {
            if (filter.contains(entry.getKey()))
                pq.add(new Recognition("" + entry.getKey(), entry.getKey(), entry.getValue(), null));
        }

        final ArrayList<Recognition> recognitions = new ArrayList<>();
        int recognitionsSize = Math.min(pq.size(), MAX_RESULTS);
        for (int i = 0; i < recognitionsSize; ++i) {
            recognitions.add(pq.poll());
        }
        return recognitions;
    }

    /**
     * Gets the name of the model file stored in Assets.
     */
    protected final String getModelPath() {
        return MODEL_PATH;
    }

    /**
     * Gets the name of the label file stored in Assets.
     */
    protected final String getLabelPath() {
        return LABEL_PATH;
    }

    /**
     * Gets the TensorOperator to nomalize the input image in preprocessing.
     */
    protected TensorOperator getPreprocessNormalizeOp() {
        return new NormalizeOp(IMAGE_MEAN, IMAGE_STD);
    }

    /**
     * Gets the TensorOperator to dequantize the output probability in post processing.
     * <p>
     * <p>For quantized model, we need de-quantize the prediction with NormalizeOp (as they are all
     * essentially linear transformation). For float model, de-quantize is not required. But to
     * uniform the API, de-quantize is added to float model too. Mean and std are set to 0.0f and
     * 1.0f, respectively.
     */
    protected TensorOperator getPostprocessNormalizeOp() {
        return new NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD);
    }

    private void readLabelsAndLabelFilter(Activity activity, GameMode gameMode)
            throws IOException{

        if (gameMode == GameMode.OFFICE)
            ;//TODO read from office file
        else if (gameMode == GameMode.OUTDOOR)
            ;//TODO read from outdoor file
        labels = FileUtil.loadLabels(activity, getLabelPath());

    }

    //Raul
    public Float getTargetObjPercentage() {
        return target_perc;
    }

    public String popPeekObject() {
        return targetObjects.remove();
    }

    public String getPeekObject() {
        return targetObjects.peek();
    }

    public Boolean checkEmptyQueue() {
        return targetObjects.isEmpty();
    }


    public Queue<String> getSampleObjects(int numberOfObjects) {
        Queue<String> sample = new PriorityQueue<>();
        sample.add("mouse");
        sample.add("monitor");
        sample.add("computer keyboard");
        return sample;

//        for(int i=0; i<=numberOfObjects; i++) {
//            String obj = labels.get(new Random().nextInt(labels.size()));
//            if(sample.contains(obj)){
//                i--;
//                continue;
//            }
//            sample.add(obj);
//        }
//        return sample;
    }
}
