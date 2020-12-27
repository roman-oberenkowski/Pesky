package pl.grupaPelnaBledow;

import javax.sound.sampled.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class NetworkTest {
    public static void main(String args[]) throws InterruptedException, IOException {
        NetworkTest xd = new NetworkTest();
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

    public NetworkTest(){
        new PlayThread().start();
        new CaptureThread().start();
    }

    class CaptureThread extends Thread {
        public void run() {
            try {
                Socket clientSocket = new Socket("localhost", 1234);
                OutputStream os = clientSocket.getOutputStream();
//                recording the sound from microphone
//                AudioFormat audioFormat = getAudioFormat();
//                DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
//                TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
//                targetDataLine.open(audioFormat);
//                targetDataLine.start();
                String filePath="clip2.wav";
                File file=new File(filePath);
                System.out.println(file.getAbsolutePath());
                System.out.println("can i read this file: "+file.canRead());
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file.getAbsoluteFile());

                byte tempBuffer[] = new byte[1000];

                while (true) {

                    //int cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
                    int cnt = audioInputStream.read(tempBuffer, 0, tempBuffer.length);
                    System.out.println(cnt);
                    if (cnt > 0) {
                        os.write(tempBuffer, 0, cnt);
                    }
                }
            } catch (IOException | UnsupportedAudioFileException e) {
                e.printStackTrace();
            }


        }
    }

    class PlayThread extends Thread {
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(1234);
                Socket connectionSocket = serverSocket.accept();
                InputStream is = connectionSocket.getInputStream();
                //audio

                byte tempBuffer[] = new byte[10000];
                AudioFormat audioFormat =
                        getAudioFormat();

                DataLine.Info dataLineInfo =
                        new DataLine.Info(
                                SourceDataLine.class,
                                audioFormat);
                SourceDataLine sourceDataLine = (SourceDataLine)
                        AudioSystem.getLine(
                                dataLineInfo);
                sourceDataLine.open(audioFormat);
                sourceDataLine.start();




                int cnt;
                while ((cnt = is.read(tempBuffer, 0, 1000)) != -1) {
                    if (cnt > 0) {
                        sourceDataLine.write(
                                tempBuffer, 0, cnt);
                    }
                }
                //connectionSocket.close();
                //serverSocket.close();
            } catch (Exception e) {
                System.out.println("Exception");
            }
        }
    }
}