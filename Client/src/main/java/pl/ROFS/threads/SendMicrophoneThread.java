package pl.ROFS.threads;

import pl.ROFS.controllers.AudioController;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.xml.bind.DatatypeConverter;
import java.util.Arrays;

public class SendMicrophoneThread extends Thread {

    private final AudioController audioController;

    public SendMicrophoneThread(AudioController audioController) {
        this.audioController = audioController;
    }

    public void run() {
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioController.getAudioFormat());
        try {
            audioController.setMicrophone((TargetDataLine) AudioSystem.getLine(dataLineInfo));
            audioController.getMicrophone().open(audioController.getAudioFormat());
        } catch (LineUnavailableException e) {
            audioController.getClientController().handleMicrophoneError();
            return;
        }
        audioController.getMicrophone().start();
        byte[] tempBuffer = new byte[AudioController.SOUND_BUFFER_SIZE];
        int cnt = 1;
        while (cnt >= 0) {
            cnt = audioController.getMicrophone().read(tempBuffer, 0, tempBuffer.length);
            if (cnt > 0) {
                String audio_data = DatatypeConverter.printBase64Binary(Arrays.copyOfRange(tempBuffer, 0, cnt));
                audioController.getClientController().getConnectionController().send("audio", audio_data);
            }
        }
    }
}
