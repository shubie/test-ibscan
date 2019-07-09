package sample;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


import com.integratedbiometrics.ibscanultimate.IBScan;
import com.integratedbiometrics.ibscanultimate.IBScanDevice;
import com.integratedbiometrics.ibscanultimate.IBScanDeviceListener;
import com.integratedbiometrics.ibscanultimate.IBScanException;
import com.integratedbiometrics.ibscanultimate.IBScanListener;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.FingerCountState;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.FingerQualityState;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.ImageData;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.ImageType;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.PlatenState;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.SegmentPosition;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class Main extends Application  implements IBScanListener, IBScanDeviceListener{

    protected static final int DEVICE_INDEX_INVALID = -12345678;
    protected static final long LEDS_INVALID = -12345678;
    protected static final int CONTRAST_INVALID = -12345678;

    // The IB scan through which the bus is accessed.
    protected IBScan        ibScan            = null;
    protected IBScanDevice  ibScanDevice      = null;
    protected BufferedImage lastScanImage     = null;
    protected ImageData     lastScanImageData = null;

    // Get IBScan.
    protected IBScan getIBScan()
    {
        return (this.ibScan);
    }

    // Get opened or null IBScanDevice.
    protected IBScanDevice getIBScanDevice()
    {
        return (this.ibScanDevice);
    }

    TextField deviceIndexField = new TextField();

    ImageView imagePreview = new ImageView();


    // Set IBScanDevice.
    protected void setIBScanDevice(IBScanDevice ibScanDevice)
    {
        this.ibScanDevice = ibScanDevice;
        if (ibScanDevice != null)
        {
            ibScanDevice.setScanDeviceListener(this);
        }
    }

    protected int getDeviceIndex()
    {
        String deviceIndexString = deviceIndexField.getText();
        int deviceIndex;

        try
        {
            deviceIndex = Integer.parseInt(deviceIndexString);
        }
        catch (NumberFormatException nfe)
        {
            deviceIndex = DEVICE_INDEX_INVALID;
        }

        return (deviceIndex);
    }

    // Get image type.
    protected IBScanDevice.ImageType getImageType()
    {
        IBScanDevice.ImageType imageType =
                (IBScanDevice.ImageType) ImageType.FLAT_FOUR_FINGERS;

        return (imageType);
    }
    protected IBScanDevice.ImageResolution getImageResolution()
    {
        // Currently only 500ppi is supported.
        IBScanDevice.ImageResolution imageResolution = IBScanDevice.ImageResolution.RESOLUTION_500;

        return (imageResolution);
    }

    // Get capture options.
    protected int getCaptureOptions()
    {
        int options = 0;

//        if (this.chckbxAutoContrastOptimization.isSelected())
//        {
//            options |= IBScanDevice.OPTION_AUTO_CONTRAST;
//        }
//        if (this.chckbxAutoCaptureFingerprints.isSelected())
//        {
//            options |= IBScanDevice.OPTION_AUTO_CAPTURE;
//        }
//        if (this.chckbxTriggerInvalidFingerCount.isSelected())
//        {
//            options |= IBScanDevice.OPTION_IGNORE_FINGER_COUNT;
//        }

        return (options);
    }

    public void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.show();

    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));


        Button openButton = new Button("Open Device");
        openButton.setOnAction(ae -> {

            showAlert("OpenDevice button pressed; deviceIndex = " + getDeviceIndex() + ".");

            int deviceIndex = getDeviceIndex();

            if (deviceIndex == DEVICE_INDEX_INVALID)
            {
                // Alert user that device index is invalid.
                showAlert("Invalid device index");

            }
            else
            {
                if (getIBScanDevice() != null)
                {
                    // Close old device.
                    try
                    {
                        if (getIBScanDevice().isOpened())
                        {
                            getIBScanDevice().close();
                        }
                    }
                    catch (IBScanException ibse)
                    {
                        showAlert("Exception closing old device: " + ibse.getType().toString() + ".");

                    }
                    setIBScanDevice(null);
                }

                // Local runnable class to perform action on background thread.
                class OpenDeviceThread extends Thread
                {
                    private int deviceIndexTemp;

                    OpenDeviceThread(int deviceIndexTemp)
                    {
                        this.deviceIndexTemp = deviceIndexTemp;
                    }

                    @Override
                    public void run()
                    {
                        try
                        {
                            // Open device for the device index specified in

                            IBScanDevice ibScanDeviceNew = getIBScan().openDevice(this.deviceIndexTemp);
                            setIBScanDevice(ibScanDeviceNew);
//                            showAlert("IBScan.openDevice() successful");
                            System.out.println("Device opened");
                            System.out.println(ibScanDeviceNew);

                        }
                        catch (IBScanException ibse)
                        {
                            showAlert("IBScan.openDevice() returned exception " + ibse.getType().toString() );
                        }
                    }
                }

                // Create thread to open device.
                OpenDeviceThread openDeviceThread = new OpenDeviceThread(deviceIndex);
                openDeviceThread.start();
            }
        });

        // GET DEVICE DESCRIPTION

        Button getDeviceDescription = new Button("Get Device Description");
        getDeviceDescription.setOnAction(e -> {

            showAlert("GetDeviceDescription button pressed; deviceIndex = " + getDeviceIndex()
                    + ".");

            int deviceIndex = getDeviceIndex();

            if (deviceIndex == DEVICE_INDEX_INVALID)
            {
                // Alert user that device index is invalid.
                showAlert("Invalid device index.");

            }
            else
            {
                try
                {
                    // Get device description for the device index specified in
                    // "Device Index" field.
                    IBScan.DeviceDesc deviceDesc = getIBScan().getDeviceDescription(deviceIndex);
                    showAlert("IBScan.getDeviceDescription() successful");
                    System.out.println(deviceDesc);

                }
                catch (IBScanException ibse)
                {
                    showAlert("IBScan.getDeviceDescription() returned exception "
                            + ibse.getType().toString() + ".");
                }
            }
        });

        Button btnCloseDevice = new Button("Close Device");
        btnCloseDevice.setOnAction(e -> {

        });

        Button btnIsDeviceOpen = new Button("Is Device Open");
        btnIsDeviceOpen.setOnAction(e -> {


            System.out.println("IsDeviceOpened button pressed.");

            if (getIBScanDevice() == null)
            {
                // Alert user no device has been opened.
                showAlert("No device has been opened.");

            }
            else
            {
                // Test whether device is open.
                boolean open = getIBScanDevice().isOpened();
//                setFunctionResult("");
                if (open)
                    System.out.println("Device is open.");
                else
                    System.out.println("Device is not open.");
            }

        });


        Button btnBeginCapture = new Button("Begin Image Capture");
        btnBeginCapture.setOnAction(e -> {

            System.out.println("BeginCaptureImage button pressed; imageType = " + getImageType()
                    + "; imageResolution = " + getImageResolution() + "; captureOptions = "
                    + getCaptureOptions() + ".");

            if (getIBScanDevice() == null)
            {
                // Alert user no device has been opened.
                showAlert("No device has been opened.");
//                setFunctionResult("");setFunctionResult
//                setAdditionalInformation("");
            }
            else
            {
                // Begin capturing image for active device.
                int captureOptions = getCaptureOptions();
                IBScanDevice.ImageType imageType = getImageType();
                IBScanDevice.ImageResolution imageResolution = getImageResolution();

                try
                {
                    // Begin capturing image for active device.
                    this.getIBScanDevice().beginCaptureImage(imageType,
                            imageResolution,captureOptions);
                    SwingUtilities.invokeLater(new ReportResultsRunnable(
                            "IBScanDevice.beginCaptureImage() successful", ""));
                }
                catch (IBScanException ibse)
                {
                    SwingUtilities.invokeLater(new ReportResultsRunnable(
                            "IBScanDevice.beginCaptureImage() returned exception "
                                    + ibse.getType().toString() + ".", ""));
                }
            }

        });


        Button btnCancelCapture = new Button("Cancel Image Capture");
        btnCancelCapture.setOnAction(e -> {

        });


        Button btnCaptureImageManually = new Button("Capture Image Manually");
        btnCaptureImageManually.setOnAction(e -> {

            System.out.println("TakeResultImageManually button pressed.");

            if (getIBScanDevice() == null)
            {
                // Alert user no device has been opened.
                System.out.println("No device has been opened.");
//                setFunctionResult("");
//                setAdditionalInformation("");
            }
            else
            {
                try
                {
                    // Capture image manually for active device
                    getIBScanDevice().captureImageManually();
                    System.out.println("IBScanDevice.takeResultImageManually() successful");
//                    setAdditionalInformation("");
                }
                catch (IBScanException ibse)
                {
                    System.out.println("IBScanDevice.takeResultImageManually() returned exception "
                            + ibse.getType().toString() + ".");
//                    setAdditionalInformation("");
                }
            }

        });



        FlowPane pane1 = new FlowPane();
//
//        TextField deviceIndexField = new TextField();
        deviceIndexField.setText("0");
        pane1.getChildren().addAll(new Label("Device index"), deviceIndexField);





        FlowPane pane2 = new FlowPane();
        pane2.setHgap(10);
        pane2.getChildren().addAll(getDeviceDescription, openButton, btnCloseDevice, btnIsDeviceOpen, btnBeginCapture, btnCancelCapture, btnCaptureImageManually);

        Label preview = new Label("Image Preview");



        VBox vBox = new VBox(10);
        vBox.getChildren().addAll(pane1,pane2, preview, imagePreview);



        this.ibScan = IBScan.getInstance();
        this.ibScan.setScanListener(this);

        primaryStage.setTitle("Testing Integrated Biometric Scanner");
        primaryStage.setScene(new Scene(vBox, 900, 275));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }


    // //////////////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE INTERFACE
    // //////////////////////////////////////////////////////////////////////////////////////////////

    private static final int CLICK_TIME_MS = 5;//50;
    private static final int IMAGE_WIDTH = 280;//279;
    private static final int IMAGE_HEIGHT = 200;//201;

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC INTERFACE: IBScanListener METHODS
    // //////////////////////////////////////////////////////////////////////////////////////////////



    @Override
    public void deviceCommunicationBroken(IBScanDevice ibScanDevice) {

    }

    @Override
    public void deviceImagePreviewAvailable(IBScanDevice ibScanDevice, ImageData image) throws IBScanException {
        System.out.print("Callback \"scanPreviewImageAvailable\" received with (image = "
                + image.width + "x" + image.height + " " + image.bitsPerPixel + "-bpp "
                + image.format.toString() + " pitch=" + image.pitch + " res=" + image.resolutionX
                + "x" + image.resolutionY + ")");



        class DisplayImagePreviewRunnable implements Runnable
        {
            javafx.scene.image.Image imageIconTemp;

            DisplayImagePreviewRunnable(javafx.scene.image.Image imageIconTemp)
            {
                this.imageIconTemp = imageIconTemp;
            }

            @Override
            public void run()
            {
                // Set image in image preview and flash button.
                imagePreview.setImage(this.imageIconTemp);
//                FunctionTester.this.lblImagePreview.setIcon(this.imageIconTemp);
//                lightCallbackButton(FunctionTester.this.btnDeviceImagePreviewAvailable, 5);
            }
        }



        // UI updates must occur on UI thread.
        int destWidth = IMAGE_WIDTH;
        int destHeight = IMAGE_HEIGHT;
        int outImageSize = destWidth * destHeight;

        byte[] outImage = new byte[outImageSize];
        Arrays.fill(outImage, (byte) 255);

        try {
            int nRc = ibScanDevice.generateZoomOutImageEx(image.buffer,
                    image.width, image.height, outImage, destWidth, destHeight,
                    (byte) 255);
        } catch (IBScanException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        Image imageJ = image.toImage(outImage, IMAGE_WIDTH, IMAGE_HEIGHT);
//        ImageIcon imageIcon = new ImageIcon(imageJ);
        javafx.scene.image.Image imageFX = SwingFXUtils.toFXImage(image.toImage(outImage, IMAGE_WIDTH, IMAGE_HEIGHT), null);
        SwingUtilities.invokeLater(new DisplayImagePreviewRunnable(imageFX));

    }

    @Override
    public void deviceFingerCountChanged(IBScanDevice ibScanDevice, FingerCountState fingerCountState) {

    }

    @Override
    public void deviceFingerQualityChanged(IBScanDevice ibScanDevice, FingerQualityState[] fingerQualityStates) {

    }

    @Override
    public void deviceAcquisitionBegun(IBScanDevice ibScanDevice, ImageType imageType) {

    }

    @Override
    public void deviceAcquisitionCompleted(IBScanDevice ibScanDevice, ImageType imageType) {

    }

    @Override
    public void deviceImageResultAvailable(IBScanDevice ibScanDevice, ImageData imageData, ImageType imageType, ImageData[] imageData1) {
        System.out.print("Callback \"imageResultAvailable\" received (image = " + imageData.width + "x"
                + imageData.height + " " + imageData.bitsPerPixel + "-bpp " + imageData.format.toString()
                + " pitch=" + imageData.pitch + " res=" + imageData.resolutionX + "x" + imageData.resolutionY
                + ")");

        System.out.println(imageData1);

    }

    @Override
    public void deviceImageResultExtendedAvailable(IBScanDevice device, IBScanException imageStatus,
                                                   ImageData image, ImageType imageType, int detectedFingerCount, ImageData[] segmentImageArray,
                                                   SegmentPosition[] segmentPositionArray)
    {
        System.out.println("Callback \"imageResultExtendedAvailable\" received (image = " + image.width + "x"
                + image.height + " " + image.bitsPerPixel + "-bpp " + image.format.toString()
                + " pitch=" + image.pitch + " res=" + image.resolutionX + "x" + image.resolutionY
                + ")");

        BufferedImage saveImage = image.toSaveImage();
        try {
            ImageIO.write(saveImage, "jpg", new File("savedImaged.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("-------- ------ ------ ---------");
        System.out.println(segmentImageArray.length);
        System.out.println("-------- ------ ------ ---------");

        for (ImageData imageData : segmentImageArray) {
            try {
                ImageIO.write(imageData.toSaveImage(), "jpg", new File(imageData + "Imaged.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        class DisplayImageResultExtendedRunnable implements Runnable
        {
            BufferedImage imageTemp;
            ImageData     imageDataTemp;

            DisplayImageResultExtendedRunnable(BufferedImage imageTemp, ImageData imageDataTemp)
            {
                this.imageTemp     = imageTemp;
                this.imageDataTemp = imageDataTemp;
            }

            @Override
            public void run()
            {
                // Set image in image preview, preserve image for saving, and
                // flash button.
//                lightCallbackButton(FunctionTester.this.btnDeviceImageResultExtendedAvailable, 50);
                int destWidth = IMAGE_WIDTH;
                int destHeight = IMAGE_HEIGHT;
                int outImageSize = destWidth * destHeight;

                byte[] outImage = new byte[outImageSize];
                Arrays.fill(outImage, (byte) 255);

                try {
                    int nRc = ibScanDevice.generateZoomOutImageEx(
                            imageDataTemp.buffer,
                            imageDataTemp.width,
                            imageDataTemp.height, outImage, destWidth,
                            destHeight, (byte) 255);
                } catch (IBScanException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                Image imageJ = imageDataTemp.toImage(outImage,
                        IMAGE_WIDTH, IMAGE_HEIGHT);
                ImageIcon imageIcon = new ImageIcon(imageJ);
//                FunctionTester.this.lblImagePreview.setIcon(imageIcon);
//                FunctionTester.this.lastScanImage = this.imageTemp;
//                FunctionTester.this.lastScanImageData = this.imageDataTemp;
            }
        }

        // UI updates must occur on UI thread.
        BufferedImage imageJ = image.toImage();
        SwingUtilities.invokeLater(new DisplayImageResultExtendedRunnable(imageJ, image));
    }

    @Override
    public void devicePlatenStateChanged(IBScanDevice ibScanDevice, PlatenState platenState) {

    }

    @Override
    public void deviceWarningReceived(IBScanDevice ibScanDevice, IBScanException e) {

    }

    @Override
    public void devicePressedKeyButtons(IBScanDevice ibScanDevice, int i) {

    }

    @Override
    public void scanDeviceCountChanged(int i) {

    }

    @Override
    public void scanDeviceInitProgress(int i, int i1) {

    }

    @Override
    public void scanDeviceOpenComplete(int i, IBScanDevice ibScanDevice, IBScanException e) {

    }

    // Utility class to report results back to display thread.
    private class ReportResultsRunnable implements Runnable
    {
        private String functionResult;
        private String additionalInformation;

        ReportResultsRunnable(String functionResult, String additionalInformation)
        {
            this.functionResult = functionResult;
            this.additionalInformation = additionalInformation;
        }

        @Override
        public void run()
        {
            System.out.println(this.functionResult);
//            FunctionTester.this.setFunctionResult(this.functionResult);
//            FunctionTester.this.setAdditionalInformation(this.additionalInformation);
        }
    }
}


