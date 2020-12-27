package pl.grupaPelnaBledow;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.sound.sampled.*;

public class Main {
    static AudioInputStream audioInputStream;
    static String filePath;
    TargetDataLine targetDataLine;

    public static void main(String[] args) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        new Main();
    }

    public Main() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        try {
            Mixer.Info[] mixerInfo =
                    AudioSystem.getMixerInfo();
            System.out.println("Available mixers:");
            for (int cnt = 0; cnt < mixerInfo.length;
                 cnt++) {
                System.out.println(mixerInfo[cnt].
                        getName());
            }//end for loop
        }catch (Exception e){

        }
            filePath="clip2.wav";
        File file=new File(filePath);
        System.out.println(file.getAbsolutePath());
        System.out.println("can i read this file: "+file.canRead());
        audioInputStream = AudioSystem.getAudioInputStream(file.getAbsoluteFile());
        Clip clip = AudioSystem.getClip();
        clip.open(audioInputStream);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        System.in.read();


        AudioFormat audioFormat = getAudioFormat();
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
        targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
        targetDataLine.open(audioFormat);
        targetDataLine.start();


    }
    private AudioFormat getAudioFormat() {
        float sampleRate = 8000.0F;
        //8000,11025,16000,22050,44100
        int sampleSizeInBits = 16;
        //8,16
        int channels = 1;
        //1,2
        boolean signed = true;
        //true,false
        boolean bigEndian = false;
        //true,false
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }
}
