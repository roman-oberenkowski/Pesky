package pl.ROFS.threads;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamLockException;
import javafx.application.Platform;
import pl.ROFS.controllers.VideoController;

import java.awt.*;
import java.awt.image.BufferedImage;

public class CameraCaptureThread extends Thread {
    private final VideoController videoController;

    public CameraCaptureThread(VideoController videoController) {
        this.videoController = videoController;
    }

    public void run() {
        videoController.setWebcam(Webcam.getDefault());
        if (videoController.getWebcam() != null) {
            try {
                videoController.getWebcam().setViewSize(new Dimension(320, 240));
                videoController.getWebcam().open();
            } catch (WebcamLockException e) {
                videoController.getClientController().handleCameraError("Camera already in use");
                videoController.getClientController().cameraToggle.setSelected(false);
                videoController.setWebcam(null);
                return;
            }
        } else {
            videoController.getClientController().handleCameraError("No camera found");
            return;
        }

        while (true) {
            try {
                videoController.getCameraSemaphore().acquire(2);
                videoController.getCameraSemaphore().release(2);
            } catch (InterruptedException e) {
                return;
            }
            BufferedImage image = videoController.getWebcam().getImage();
            if (image == null) return;
            videoController.sendImage(image);

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
