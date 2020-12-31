package pl.ROFS;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
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


public class Controller {
    private final int port = 4201;
    public Pane loginPane;
    public Pane callPane;
    public TextArea ipField;
    public TextArea usernameField;
    public Button connectButton;
    public Button hostButton;
    public ImageView myView;
    public ImageView callerView;
    public TextArea logArea;
    PrintWriter writer;
    BufferedReader reader;
    private SourceDataLine speakers;
    private Webcam webcam;
    private Thread sendAudioThread;
    private Thread receiveFromServerThread;
    private Thread captureCameraThread;
    private boolean privateServer=false;
    private final int soundBufferSize=10000;
    public void initialize() {
        callPane.setVisible(false);
        loginPane.setVisible(true);
    }

    class CaptureCameraThread extends Thread{
        public void run(){
            while(true) {
                BufferedImage image = webcam.getImage();
                myView.setImage(SwingFXUtils.toFXImage(image, null));
                ByteArrayOutputStream img_stream=new ByteArrayOutputStream();
                try {
                    ImageIO.write(image,"png",img_stream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                byte [] img_byte_array=img_stream.toByteArray();
                String video_data=DatatypeConverter.printBase64Binary(img_byte_array);
                sendToServer("video",video_data);

//                try {
//                    Thread.sleep(50);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        }
    }

    public void startCamera(){
        //webcam setup
        webcam = Webcam.getDefault();
        //webcam.setViewSize(new Dimension(160, 120));
        webcam.open();
        myView.setScaleX(-1);
        captureCameraThread = new CaptureCameraThread();
        captureCameraThread.start();
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
                    Thread.sleep(10);
                }
            } catch (IOException | UnsupportedAudioFileException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void startPlayingAudioClip() {
        if(sendAudioThread == null) {
            sendAudioThread = new SendAudioClipThread();
            sendAudioThread.start();
        }
    }

    class ReciveFromServerThread extends Thread{
        public void run() {
            try {
                byte tempBuffer[] = new byte[soundBufferSize];
                String content;
                String type;
                String serverMessage="";
                String [] splited;
                String right;
                byte[] decodedContent;

                while(true){
                    try{
                    serverMessage = reader.readLine();
                    } catch(SocketException e){
                        exit();
                    }
                    try {
                        splited = serverMessage.split(";");
                        type = splited[0].split(":")[1];
                        right = splited[1];
                    }
                    catch(ArrayIndexOutOfBoundsException e){
                        System.out.println("message format error! ommiting -> "+serverMessage);
                        continue;
                    }
                    try {
                        content=right.split(":")[1];
                    }catch (ArrayIndexOutOfBoundsException e){
                        content="";
                    }
                    switch(type){
                        case "joined":
                            break;
                        case "audio":
                            decodedContent = DatatypeConverter.parseBase64Binary(content);
                            speakers.write(decodedContent, 0, decodedContent.length);
                            break;
                        case "video":
                            decodedContent = DatatypeConverter.parseBase64Binary(content);
                            InputStream is = new ByteArrayInputStream(decodedContent);
                            BufferedImage image = ImageIO.read(is);
                            callerView.setImage(SwingFXUtils.toFXImage(image, null));
                            break;
                        case "disconnect":
                            exit();
                            break;
                        case "error":
                            break;
                        case "confirm":
                            break;
                        default:
                            System.out.println("type error in recived message! -> "+type);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setupAudioOutput(){
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

    public void connectButtonPressed(ActionEvent actionEvent) {
        logArea.setText("trying to connect\n");
        try {
            Socket clientSocket = new Socket(ipField.getText(), port);
            OutputStream os = clientSocket.getOutputStream();
            InputStream is = clientSocket.getInputStream();
            writer = new PrintWriter(os, true);
            reader = new BufferedReader(new InputStreamReader(is));
        } catch (ConnectException e){
            logArea.appendText("connection failed\n");
            return;
        }catch(UnknownHostException e){
            logArea.appendText("incorrect server ip\n");
            return;
        }
        catch (IOException e) {
            e.printStackTrace();
            logArea.appendText("unexpected IO Error\n");
            return;
        }
        logArea.appendText("connected to server\n");
        sendToServer("set_username",usernameField.getText());
        setupAfterConnectionSuccessfull();
    }

    private void sendToServer(String type,String content){
        writer.println("type:"+type+";content:"+content+";");
    }

    public void hostButtonPressed(ActionEvent actionEvent) {
        logArea.setText("server starting\n");
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            Socket connectionSocket = serverSocket.accept();
            InputStream is = connectionSocket.getInputStream();
            OutputStream os = connectionSocket.getOutputStream();
            writer = new PrintWriter(os, true);
            reader = new BufferedReader(new InputStreamReader(is));
        } catch(ConnectException e){
            logArea.appendText("cannot connect\n");
            return;

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        logArea.appendText(" -> server started");
        privateServer=true;
        setupAfterConnectionSuccessfull();


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

    public void setupAfterConnectionSuccessfull() {
        callPane.setVisible(true);
        loginPane.setVisible(false);
        setupAudioOutput();

        receiveFromServerThread = new ReciveFromServerThread();
        receiveFromServerThread.start();
    }

    public void exit(){
        Platform.exit();
        System.exit(0);
    }

}
