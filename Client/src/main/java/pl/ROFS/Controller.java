package pl.ROFS;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.embed.swing.SwingFXUtils;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;

import com.github.sarxos.webcam.Webcam;


public class Controller {
    private final int port = 4021;
    public Pane loginPane;
    public Pane callPane;
    public TextArea ipField;
    public TextArea usernameField;
    public Button connectButton;
    public Button hostButton;
    public ImageView myView;
    public ImageView callerView;
    private Webcam webcam;
    private InputStream is;
    private OutputStream os;
    private Thread playThread;
    private Thread reciveAudioThread;
    private Thread captureCameraThread;

    private SourceDataLine speakers;
    public void initialize() {
        callPane.setVisible(false);
        loginPane.setVisible(true);
        //temporary
        callPane.setVisible(true);


    }
    class CaptureCameraThread extends Thread{
        public void run(){
            while(true) {
                BufferedImage image = webcam.getImage();
                myView.setImage(SwingFXUtils.toFXImage(image, null));
                //send video frame
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void setupCamera(){
        //webcam setup
        webcam = Webcam.getDefault();
        webcam.open();
        myView.setScaleX(-1);
        captureCameraThread = new CaptureCameraThread();
        captureCameraThread.start();
    }

    class PlayAudioClipThread extends Thread{
        public void run(){
            try {
                byte tempBuffer[] = new byte[1000];
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(getClass().getResource("clip2.wav"));
                int cnt=1;
                while (cnt>=0) {
                    cnt = audioInputStream.read(tempBuffer, 0, tempBuffer.length);
                    if (cnt > 0) {
                        os.write(tempBuffer, 0, cnt);
                    }
                }
            } catch (IOException | UnsupportedAudioFileException e) {
                e.printStackTrace();
            }
        }
    }

    class ReciveAudioThread extends Thread{
        public void run() {
            try {
                byte tempBuffer[] = new byte[1000];

                int cnt;
                while ((cnt = is.read(tempBuffer, 0, tempBuffer.length)) != -1) {
                    if (cnt > 0) {
                        speakers.write(tempBuffer, 0, cnt);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void startPlayingAudioClip() {
        if(playThread == null) {
            playThread = new PlayAudioClipThread();
            playThread.start();
        }
    }

    public void startRecivingAudio(){
        try {
            if(reciveAudioThread==null) {
                AudioFormat audioFormat = getAudioFormat();
                DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
                speakers = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                speakers.open(audioFormat);
                speakers.start();
                reciveAudioThread = new ReciveAudioThread();
                reciveAudioThread.start();
            }
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

    }

    public void switchPanes() {
        if (callPane.isVisible()) {
            callPane.setVisible(false);
            loginPane.setVisible(true);
        } else {
            callPane.setVisible(true);
            loginPane.setVisible(false);
        }
    }

    public void connectButtonPressed(ActionEvent actionEvent) {
        System.out.println("client starting");
        try {
            Socket clientSocket = new Socket("localhost", port);
            os = clientSocket.getOutputStream();
            is = clientSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("connected_client!");
        switchPanes();
    }

    public void hostButtonPressed(ActionEvent actionEvent) {
        System.out.println("server starting");
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            Socket connectionSocket = serverSocket.accept();
            is = connectionSocket.getInputStream();
            os = connectionSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("connected_server!");
        switchPanes();


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
}
