package pl.ROFS;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.embed.swing.SwingFXUtils;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.Arrays;
import javax.xml.bind.DatatypeConverter;
import com.github.sarxos.webcam.Webcam;

import static java.lang.Math.min;


public class Controller {
    private final int port = 1235;
    public Pane loginPane;
    public Pane callPane;
    public TextArea ipField;
    public TextArea usernameField;
    public Button connectButton;
    public ImageView myView;
    public ImageView callerView;
    public TextArea logArea;
    public TextArea targetUserField;
    public Button setUsernameButton;
    public Button joinCallButton;
    public Button fastConnectButton;
    public TextArea callerNameText;
    PrintWriter writer;
    BufferedReader reader;
    private SourceDataLine speakers;
    private Webcam webcam;
    private Thread sendAudioThread;
    private final int soundBufferSize=10000;
    private boolean isUsernameSet=false;

    public void initialize() {
        callPane.setVisible(false);
        loginPane.setVisible(true);
        setUsernameButton.setDisable(true);
        joinCallButton.setDisable(true);
    }

    public void connected(){
        Platform.runLater(
                ()->{
                    connectButton.setText("Connected :)");
                }
        );
        ipField.setDisable(true);
        setUsernameButton.setDisable(false);
        new ReceiveFromServerThread().start();
    }

    public void usernameSet(){
        isUsernameSet=true;
        setUsernameButton.setDisable(true);
        usernameField.setDisable(true);
        joinCallButton.setDisable(false);
    }

    public void setUsernameHandler(){
        sendToServer("set_username",usernameField.getText());
    }
    public void joinCallHandler(){
        sendToServer("call_to",targetUserField.getText());
    }

    public void fastConnect(ActionEvent actionEvent) {
        ipField.setText("192.168.1.23");
        connectButtonHandler();
    }

    public void goToCallView() {
        callPane.setVisible(true);
        loginPane.setVisible(false);
    }

    public void exit(){
        Platform.exit();
        System.exit(0);
    }

    //CAMERA=============================
    public void startCamera(){
        webcam = Webcam.getDefault();
        webcam.setViewSize(new Dimension(320, 240));
        webcam.open();
        myView.setScaleX(-1);
        Thread captureCameraThread = new CaptureCameraThread();
        captureCameraThread.start();
    }

    private class CaptureCameraThread extends Thread{
        public void run(){
            while(true) {
                BufferedImage image = webcam.getImage();
                myView.setImage(SwingFXUtils.toFXImage(image, null));
                ByteArrayOutputStream img_stream=new ByteArrayOutputStream();
                try {
                    ImageIO.write(image,"jpg",img_stream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                byte [] img_byte_array=img_stream.toByteArray();
                String video_data=DatatypeConverter.printBase64Binary(img_byte_array);
                sendToServer("video",video_data);

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //AUDIO==============================
    public void startRecordingMic(){
        new SendMicrophoneThread().start();

    }

    private class SendMicrophoneThread extends Thread{
        public void run(){
            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, getAudioFormat());
            TargetDataLine targetDataLine = null;
            try {
                targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
                targetDataLine.open(getAudioFormat());
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
            targetDataLine.start();
            byte[] tempBuffer = new byte[soundBufferSize];
            int cnt=1;
            while (cnt>=0) {
                cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
                if (cnt > 0) {
                    String audio_data=DatatypeConverter.printBase64Binary(Arrays.copyOfRange(tempBuffer,0,cnt));
                    sendToServer("audio",audio_data);
                }
            }
        }
    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 44100.0F;
        //8000,11025,16000,22050,44100
        int sampleSizeInBits = 16;
        //8,16
        int channels = 2;
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
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    //NETWORK
    public void connectButtonHandler() {
        fastConnectButton.setDisable(true);
        connectButton.setDisable(true);
        connectButton.setText("Connecting...");
        new connectThread().start();
    }

    class connectThread extends Thread{
        public void run(){
            try {
                Socket clientSocket = new Socket(ipField.getText(), port);
                OutputStream os = clientSocket.getOutputStream();
                InputStream is = clientSocket.getInputStream();
                writer = new PrintWriter(os, true);
                reader = new BufferedReader(new InputStreamReader(is));
            } catch (ConnectException e){
                connFailed("connection failed");
                return;
            }catch(UnknownHostException e){
                connFailed("incorrect server ip");
                return;
            }
            catch (IOException e) {
                connFailed("unexpected IO Error");
                return;
            }
            connected();
        }
        public void connFailed(String text){
            logArea.appendText(text+"\n");
            Platform.runLater(
                    ()-> {
                        connectButton.setText("Connect");
                        connectButton.setDisable(false);
                    }
            );
        }
    }

    private void sendToServer(String type,String content){
        String msg="type:"+type+";content:"+content;
        synchronized (writer) {
            writer.println(msg);
        }
    }

    class ReceiveFromServerThread extends Thread{
        public void run() {
            try {
                setupAudioOutput();

                byte[] decodedContent;
                String content;
                String type;
                String serverMessage="";
                String [] splited;
                String right;

                while(true){
                    try{
                        serverMessage = reader.readLine();
                    } catch(SocketException | SocketTimeoutException e){
                        exit();
                    }
                    try {
                        splited = serverMessage.split(";");
                        type = splited[0].split(":")[1];
                        right = splited[1];
                    }
                    catch(ArrayIndexOutOfBoundsException e){
                        logArea.appendText("message format error! ommiting -> "+serverMessage+"\n");
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
                            callerNameText.setText("Talking with: "+content);
                            logArea.appendText("Joined "+content);
                            break;
                        case "audio":
                            decodedContent = DatatypeConverter.parseBase64Binary(content);
                            if(speakers.available()<soundBufferSize){
                                speakers.flush();
                                System.out.println("Reciving audio would block -> flushed!");
                            }else{
                                speakers.write(decodedContent, 0, min(decodedContent.length,speakers.available()));
                            }
                            break;
                        case "video":
                            decodedContent = DatatypeConverter.parseBase64Binary(content);
                            InputStream is = new ByteArrayInputStream(decodedContent);
                            BufferedImage image = ImageIO.read(is);
                            callerView.setImage(SwingFXUtils.toFXImage(image, null));
                            break;
                        case "disconnect":
                            System.out.println("Exiting because server said so ;)");
                            exit();
                            break;
                        case "error":
                            logArea.appendText(content+"\n");
                            break;
                        case "confirm":
                            logArea.appendText(content+"\n");
                            if(content.equals("Successfully changed username")){
                                usernameSet();
                            }
                            if(content.equals("Successfully called user")){
                                goToCallView();
                                callerNameText.setText("Talking with: "+targetUserField.getText());
                            }

                            break;
                        default:
                            logArea.appendText("type error in recived message! -> "+type+"\n");

                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //TEMP
    public void startPlayingAudioClip() {
        if(sendAudioThread == null) {
            sendAudioThread = new SendAudioClipThread();
            sendAudioThread.start();
        }
    }

    class SendAudioClipThread extends Thread{
        public void run(){
            try {
                byte[] tempBuffer = new byte[soundBufferSize];
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(getClass().getResource("clip2.wav"));
                int cnt=1;
                while (cnt>=0) {
                    cnt = audioInputStream.read(tempBuffer, 0, tempBuffer.length);
                    if (cnt > 0) {
                        String audio_data=DatatypeConverter.printBase64Binary(Arrays.copyOfRange(tempBuffer,0,cnt));
                        sendToServer("audio",audio_data);
                    }
                    Thread.sleep(56);
                }
            } catch (IOException | UnsupportedAudioFileException  e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }



}
