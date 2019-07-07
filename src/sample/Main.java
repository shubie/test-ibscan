package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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

import java.awt.image.BufferedImage;

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

    public void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
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
                            showAlert("IBScan.openDevice() successful");

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

        });


        Button btnBeginCapture = new Button("Begin Image Capture");
        btnBeginCapture.setOnAction(e -> {

        });


        Button btnCancelCapture = new Button("Cancel Image Capture");
        btnCancelCapture.setOnAction(e -> {

        });


        Button btnCaptureImageManually = new Button("Capture Image Manually");
        btnCaptureImageManually.setOnAction(e -> {

        });



        FlowPane pane1 = new FlowPane();
//
//        TextField deviceIndexField = new TextField();
        deviceIndexField.setText("0");
        pane1.getChildren().addAll(new Label("Device index"), deviceIndexField);





        FlowPane pane2 = new FlowPane();
        pane2.setHgap(10);
        pane2.getChildren().addAll(getDeviceDescription, openButton, btnCloseDevice, btnIsDeviceOpen, btnBeginCapture, btnCancelCapture, btnCaptureImageManually);

        VBox vBox = new VBox(10);
        vBox.getChildren().addAll(pane1,pane2);

        primaryStage.setTitle("Testing Integrated Biometric Scanner");
        primaryStage.setScene(new Scene(vBox, 900, 275));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }





    @Override
    public void deviceCommunicationBroken(IBScanDevice ibScanDevice) {

    }

    @Override
    public void deviceImagePreviewAvailable(IBScanDevice ibScanDevice, ImageData imageData) throws IBScanException {

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

    }

    @Override
    public void deviceImageResultExtendedAvailable(IBScanDevice ibScanDevice, IBScanException e, ImageData imageData, ImageType imageType, int i, ImageData[] imageData1, SegmentPosition[] segmentPositions) {

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
}
