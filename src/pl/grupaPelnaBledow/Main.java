package pl.grupaPelnaBledow;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

public class Main {
    public static void main(String[] args) {

        Mixer.Info[] audio_list = AudioSystem.getMixerInfo();
        System.out.println("Devices detected: "+audio_list.length);
        System.out.println("Available sound devicess: ");
        int index=1;
        for (Mixer.Info deviceInfo: audio_list) {
            System.out.println(index+"\t"+deviceInfo);
            index++;
        }
    }
}
