package pl.ROFS;

import com.github.sarxos.webcam.WebcamLockException;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.embed.swing.SwingFXUtils;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import javax.xml.bind.DatatypeConverter;
import com.github.sarxos.webcam.Webcam;


public class Controller {
    private final int port = 4201;
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

    PrintWriter writer;
    BufferedReader reader;
    private SourceDataLine speakers;
    private Webcam webcam;
    private final int soundBufferSize=1000;
    private boolean audioOutputOK=false;
    private final Semaphore cameraSemaphore = new Semaphore(2, false);
    Thread captureCameraThread;
    Thread captureMicrophoneThread;
    TargetDataLine microphone;

    public void initialize() {
        callToPane.setVisible(false);
        callPane.setVisible(false);
        connectionPane.setVisible(true);
        signInPane.setVisible(false);
        myView.setScaleX(-1);
    }

    public void connected(){
        signInPane.setVisible(true);
        connectionPane.setVisible(false);
        new ReceiveFromServerThread().start();
    }

    public void usernameSet(){
        signInPane.setVisible(false);
        callToPane.setVisible(true);
    }

    public void setUsernameHandler(){
        sendToServer("set_username",usernameField.getText());
        setUsernameButton.setText("Checking...");
        signInError.setText("");
    }

    public void joinCallHandler(){
        sendToServer("call_to", callToField.getText());
    }

    public void goToCallView() {
        callToPane.setVisible(false);
        callPane.setVisible(true);
    }

    public void global_exit(){
        Platform.exit();
        System.exit(0);
    }

    //CAMERA=============================
    public void cameraHandler(){
        if(captureCameraThread==null){
            captureCameraThread = new CaptureCameraThread();
            captureCameraThread.start();
        }
        else{
            if(webcam==null){
                cameraToggle.setSelected(false);
                return;
            }

            if(!cameraToggle.isSelected()){
                //disable cam cap thread
                try {
                    cameraSemaphore.acquire();
                } catch (InterruptedException e) {
                    //failed to block camera
                    cameraToggle.setSelected(true);
                    return;
                }
                BufferedImage image = null;
                try {
                    image = ImageIO.read(getClass().getResource("camera.jpg"));

                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (image!=null)
                    sendImage(image);
            }
            else{
                //enable cam cap thread
                cameraSemaphore.release();
            }
        }
    }

    public void sendImage(BufferedImage image){
        myView.setImage(SwingFXUtils.toFXImage(image, null));
        ByteArrayOutputStream img_stream=new ByteArrayOutputStream();
        try {
            ImageIO.write(image,"jpg",img_stream);
        } catch (IOException e) {
            return;
        }
        byte [] img_byte_array=img_stream.toByteArray();
        String video_data=DatatypeConverter.printBase64Binary(img_byte_array);
        sendToServer("video",video_data);
    }

    private class CaptureCameraThread extends Thread{
        public void run(){
            webcam = Webcam.getDefault();
            if (webcam!=null){
                try{
                    webcam.setViewSize(new Dimension(320, 240));
                    webcam.open();
                }catch(WebcamLockException e){
                    Platform.runLater(
                            ()->{
                                inCallError.setText("Camera already in use");
                            }
                    );
                    cameraToggle.setSelected(false);
                    webcam=null;
                    return;
                }
            }
            else{
                Platform.runLater(
                        ()->{
                             inCallError.setText("No camera found");
                             cameraToggle.setSelected(false);
                        }
                );
                return;
            }
            while(true) {
                try {
                    cameraSemaphore.acquire(2);
                    cameraSemaphore.release(2);
                } catch (InterruptedException e) {
                    return;
                }
                BufferedImage image = webcam.getImage();
                if(image==null)return;
                sendImage(image);

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //AUDIO==============================
    public void microphoneToggleHandler(){
        if(captureMicrophoneThread ==null){
            captureMicrophoneThread = new SendMicrophoneThread();
            captureMicrophoneThread.start();
        }
        else{
            if(microphone == null){
                microphoneToggle.setSelected(false);
                return;
            }

            if(!microphoneToggle.isSelected()){
                //disable mic
                microphone.stop();
            }
            else{
                //enable mic
                microphone.start();
            }
        }
    }

    private class SendMicrophoneThread extends Thread{
        public void run(){
            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, getAudioFormat());
            try {
                microphone = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
                microphone.open(getAudioFormat());
            } catch (LineUnavailableException e) {
                Platform.runLater(
                        ()->{
                            inCallError.setText("Cannot access your microphone");
                            microphoneToggle.setSelected(false);
                        }
                );
                return;
            }
            microphone.start();
            byte[] tempBuffer = new byte[soundBufferSize];
            int cnt=1;
            while (cnt>=0) {
                cnt = microphone.read(tempBuffer, 0, tempBuffer.length);
                if (cnt > 0) {
                    String audio_data=DatatypeConverter.printBase64Binary(Arrays.copyOfRange(tempBuffer,0,cnt));
                    sendToServer("audio",audio_data);
                }
            }
        }
    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 8000.0F;
        //8000,11025,16000,22050,44100
        int sampleSizeInBits = 8;
        //8,16
        int channels = 1;
        //1,2
        boolean signed = true;
        //true,false
        boolean bigEndian = false;
        //true,false
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    private void setupAudioOutput(){
        try {
            AudioFormat audioFormat = getAudioFormat();
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
            speakers = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            speakers.open(audioFormat);
            speakers.start();
            audioOutputOK=true;
        } catch (LineUnavailableException e) {
            audioOutputOK=false;
            Platform.runLater(
                    ()->{
                        inCallError.setText("Audio output failure");
                    }
            );
        }
    }

    //NETWORK
    public void connectButtonHandler() {
        connectButton.setDisable(true);
        connectButton.setText("Connecting...");
        connectionError.setText("");
        new connectThread().start();
    }

    class connectThread extends Thread{
        public void run(){
            try {
                Socket clientSocket = new Socket();
                clientSocket.connect(new InetSocketAddress(addressField.getText(), port),1000);
                OutputStream os = clientSocket.getOutputStream();
                InputStream is = clientSocket.getInputStream();
                writer = new PrintWriter(os, true);
                reader = new BufferedReader(new InputStreamReader(is));
            } catch (ConnectException |SocketTimeoutException e){
                connFailed("Connection failed");
                return;
            }catch(UnknownHostException e){
                connFailed("Incorrect server address");
                return;
            }
            catch (IOException e) {
                connFailed("Unexpected IO Error");
                return;
            }
            connected();
        }
        public void connFailed(String text){
            Platform.runLater(
                    ()-> {
                        connectionError.setText(text);
                        connectButton.setText("Connect");
                        connectButton.setDisable(false);
                    }
            );
        }
    }

    private void sendToServer(String type,String content){
        String msg;
        if(content.length()>0)
            msg="type:"+type+";content:"+content;
        else{
            msg="type:"+type+";";
        }
        synchronized (writer) {
            try{
                writer.println(msg);
            } catch(Exception e){
                System.out.println("writing got problem");
                e.printStackTrace();
                global_exit();
            }
        }
    }

    private void disconnectedHandler(){
        global_exit();
    }

    public void disconnectHandler() {
//        sendToServer("disconnect","");
//        try {
//            Thread.sleep(1500);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        global_exit();
    }

    class ReceiveFromServerThread extends Thread{
        public void run() {
            try {
                setupAudioOutput();

                byte[] decodedContent;
                String content;
                String type;
                String serverMessage="";
                String [] cut_message;
                String right;

                while(true){
                    try{
                        serverMessage = reader.readLine();
                    } catch(SocketException | SocketTimeoutException e){
                        disconnectedHandler();
                        return;
                    }
                    try {
                        if(serverMessage == null)return;
                        cut_message = serverMessage.split(";");
                        type = cut_message[0].split(":")[1];
                        right = cut_message[1];
                    }
                    catch(ArrayIndexOutOfBoundsException e){
                        System.out.println("Message format error! omitting -> "+serverMessage+"\n");
                        continue;
                    }
                    try {
                        content=right.split(":")[1];
                    }catch (ArrayIndexOutOfBoundsException e){
                        content="";
                    }

                    switch(type){
                        case "joined":
                            goToCallView();
                            String finalContent1 = content;
                            Platform.runLater(
                                    ()->callerNameText.setText(finalContent1)
                            );
                            break;

                        case "audio":
                            if (!audioOutputOK) break; //don't process received audio when audio output setup failed
                            decodedContent = DatatypeConverter.parseBase64Binary(content);
                            if(speakers.available()<soundBufferSize){
                                speakers.flush();
                            }else{
                                speakers.write(decodedContent, 0, decodedContent.length);
                            }
                            break;

                        case "video":
                            decodedContent = DatatypeConverter.parseBase64Binary(content);
                            InputStream is = new ByteArrayInputStream(decodedContent);
                            BufferedImage image = ImageIO.read(is);
                            callerView.setImage(SwingFXUtils.toFXImage(image, null));
                            break;

                        case "disconnect":
                            System.out.println("Exiting because server said so");
                            global_exit();
                            return;

                        case "error":
                            System.out.println(serverMessage);
                              if (signInPane.isVisible()){
                                  String finalContent = content;
                                  Platform.runLater(
                                          ()->{
                                              signInError.setText(finalContent);
                                              setUsernameButton.setDisable(false);
                                              setUsernameButton.setText("Sign in");
                                          }
                                  );
                              }
                              if(callToPane.isVisible()){
                                  String finalContent = content;
                                  Platform.runLater(
                                          ()->{
                                              callToError.setText(finalContent);
                                              //callButton.setDisable(false);
                                              //callButton.setText("Sign in");
                                          }
                                  );
                              }

                            break;

                        case "confirm":
                            if(content.equals("Successfully changed username")){
                                usernameSet();
                            }
                            if(content.equals("Successfully called user")){
                                goToCallView();
                                Platform.runLater(
                                        ()->{
                                            callerNameText.setText(callToField.getText());
                                        }
                                );
                            }
                            break;

                        default:
                            System.out.println("type error in received message! -> "+type+"\n");
                    }
                }

            } catch (IOException e) {
                System.out.println("receive from server got problems");
                e.printStackTrace();
                global_exit();
            }
        }
    }
}
