package pl.ROFS.controllers;

import com.github.sarxos.webcam.Webcam;
import pl.ROFS.threads.CameraCaptureThread;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Semaphore;

public class VideoController {

    private Webcam webcam;
    private final Semaphore cameraSemaphore = new Semaphore(2, false);
    CameraCaptureThread captureCameraThread;

    ClientController clientController;

    public VideoController (ClientController controller) {
        this.clientController = controller;
    }

    public void setWebcam(Webcam webcam) { this.webcam = webcam; }
    public Semaphore getCameraSemaphore() { return cameraSemaphore; }
    public Webcam getWebcam() { return webcam; };

    public ClientController getClientController() {return clientController;}

    public void sendImage(BufferedImage image){
        clientController.setUserViewImage(image);

        ByteArrayOutputStream img_stream=new ByteArrayOutputStream();
        try {
            ImageIO.write(image,"jpg",img_stream);
        } catch (IOException e) {
            return;
        }
        byte [] img_byte_array=img_stream.toByteArray();
        String video_data= DatatypeConverter.printBase64Binary(img_byte_array);
        clientController.getConnectionController().send("video",video_data);
    }

    public void cameraHandler(){
        if(captureCameraThread==null){
            captureCameraThread = new CameraCaptureThread(this);
            captureCameraThread.start();
        }
        else{
            if(webcam==null){
                clientController.cameraToggle.setSelected(false);
                return;
            }
            if(!clientController.cameraToggle.isSelected()){
                try {
                    cameraSemaphore.acquire();
                } catch (InterruptedException e) {
                    clientController.cameraToggle.setSelected(true);
                    return;
                }
                BufferedImage image = null;
                try {
                    image = ImageIO.read(getClientController().getClass().getResource("camera.jpg"));

                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (image!=null){
                    sendImage(image);
                }else{
                    System.out.println("img error");
                }

            }
            else{
                cameraSemaphore.release();
            }
        }
    }


}
