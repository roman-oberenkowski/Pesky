package pl.ROFS.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.awt.image.BufferedImage;


public class ClientController {
    public AnchorPane connectionPane;
    public AnchorPane signInPane;
    public AnchorPane callToPane;
    public AnchorPane callPane;
    public JFXTextField addressField;
    public JFXTextField usernameField;
    public JFXTextField callToField;
    public JFXToggleButton cameraToggle;
    public JFXToggleButton microphoneToggle;
    public JFXButton setUsernameButton;
    public JFXButton callButton;
    public JFXButton connectButton;
    public JFXButton disconnectButton;
    public Label connectionError;
    public Label signInError;
    public Label callToError;
    public Label callerNameText;
    public Label inCallError;
    public ImageView callerView;
    public ImageView myView;

    private AudioController audioController;
    private VideoController videoController;
    private ConnectionController connectionController;

    public ClientController () {
        audioController = new AudioController(this);
        videoController = new VideoController(this);
        connectionController= new ConnectionController(this);
    }

    public AudioController getAudioController() { return audioController; }
    public VideoController getVideoController() { return videoController; }
    public ConnectionController getConnectionController() { return connectionController; }

    public void initialize() {
        callToPane.setVisible(false);
        callPane.setVisible(false);
        connectionPane.setVisible(true);
        signInPane.setVisible(false);
        myView.setScaleX(-1);
    }

    public void showUsernameSetPane(){
        signInPane.setVisible(false);
        callToPane.setVisible(true);
    }

    public void showConnectedPane(){
        signInPane.setVisible(true);
        connectionPane.setVisible(false);
        connectionController.startServerReceivingThread();
    }

    public void showCallPane() {
        callToPane.setVisible(false);
        callPane.setVisible(true);
        Platform.runLater(
                () -> {
                    callerNameText.setText(callToField.getText());
                }
        );
    }

    public void connectButtonHandler() {
        connectButton.setDisable(true);
        connectButton.setText("Connecting...");
        connectionError.setText("");
        connectionController.startConnectThread();
    }

    public void printConnectionErrorMessage(String text){
        Platform.runLater(
                () -> {
                    connectionError.setText(text);
                    connectButton.setText("Connect");
                    connectButton.setDisable(false);
                }
        );
    }

    public void setUsernameHandler(){
        connectionController.send("set_username",usernameField.getText());
        setUsernameButton.setText("Checking...");
        signInError.setText("");
    }

    public void microphoneToggleHandler(){ audioController.microphoneHandler();}

    public void handleMicrophoneError() {
        Platform.runLater(
                () -> {
                    inCallError.setText("Cannot access your microphone");
                    microphoneToggle.setSelected(false);
                }
        );
    }

    public void cameraToggleHandler(ActionEvent actionEvent) {
        videoController.cameraHandler();
    }

    public void joinCallHandler(){
        connectionController.send("call_to", callToField.getText());
    }

    public void disconnectedHandler(){
        global_exit();
    }

    public void disconnectHandler() {
        global_exit();
    }

    public void errorMessageHandler(String content) {
        if (connectionController.getClientController().signInPane.isVisible()) {
            Platform.runLater(
                    () -> {
                        connectionController.getClientController().signInError.setText(content);
                        connectionController.getClientController().setUsernameButton.setDisable(false);
                        connectionController.getClientController().setUsernameButton.setText("Sign in");
                    }
            );
        }
        if (connectionController.getClientController().callToPane.isVisible()) {
            Platform.runLater(
                    () -> {
                        connectionController.getClientController().callToError.setText(content);
                    }
            );
        }
    }

    public void setCallerViewImage(BufferedImage image) {
        callerView.setImage(SwingFXUtils.toFXImage(image, null));
    }

    public void setUserViewImage(BufferedImage image) {myView.setImage(SwingFXUtils.toFXImage(image, null));}

    public void handleCameraError(String content) {
        Platform.runLater(
                () -> {
                    inCallError.setText(content);
                    cameraToggle.setSelected(false);
                }
        );
    }

    public void setCallerName(String name) {
        Platform.runLater(
                () -> callerNameText.setText(name)
        );
    }

    public void global_exit(){
        Platform.exit();
        System.exit(0);
    }

}
