package pl.ROFS.controllers;

import javafx.application.Platform;
import pl.ROFS.threads.SendMicrophoneThread;

import javax.sound.sampled.*;

public class AudioController {
    public static final int SOUND_BUFFER_SIZE =1000;

    private SourceDataLine speakers;
    private TargetDataLine microphone;

    private ClientController clientController;

    public boolean audioOutputOK=false;
    SendMicrophoneThread sendMicrophoneThread;

    public AudioController (ClientController controller) {
        this.clientController = controller;
    }

    public ClientController getClientController() {
        return clientController;
    }
    public TargetDataLine getMicrophone() { return microphone; }
    public void setMicrophone(TargetDataLine microphone) { this.microphone = microphone; }


    public int speakerAvailable() {return speakers.available();}
    public void writeSpeakers(byte[] content) {
        speakers.write(content, 0, content.length);
    }
    public void flushSpeakers() { speakers.flush(); }

    public void startSendMicrophoneThread() {
        sendMicrophoneThread = new SendMicrophoneThread(this);
        sendMicrophoneThread.start();
    }
    public void microphoneHandler() {
        if(sendMicrophoneThread == null) {
            startSendMicrophoneThread();
        }
        else {
            if(microphone == null){
                clientController.microphoneToggle.setSelected(false);
                return;
            }
            if(!clientController.microphoneToggle.isSelected())
                microphone.start();
            else
                microphone.stop();
        }
    }

    public AudioFormat getAudioFormat() {
        float sampleRate = 8000.0F;
        //8000,11025,16000,22050,44100
        int sampleSizeInBits = 8;
        //8,16
        int channels = 1;
        //1,2
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    public void setupAudioOutput(){
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
                        clientController.inCallError.setText("Audio output failure");
                    }
            );
        }
    }

}
