package pl.ROFS.threads;

import pl.ROFS.controllers.AudioController;
import pl.ROFS.controllers.ConnectionController;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;

public class ReceiveFromServerThread extends Thread {
    private final ConnectionController connectionController;
    private final AudioController audioController;

    public ReceiveFromServerThread(ConnectionController connectionController) {
        this.connectionController = connectionController;
        System.out.println(connectionController);
        this.audioController = connectionController.getClientController().getAudioController();
    }

    public String getServerMessage()
    {
        try {
            return connectionController.getReader().readLine();
        } catch (SocketException | SocketTimeoutException e) {
            connectionController.getClientController().disconnectedHandler();
            return "";
        } catch (IOException e) {
            System.out.println("receive from server got problems");
            e.printStackTrace();
            connectionController.getClientController().global_exit();
            return "";
        }
    }

    public HashMap<String, String> processServerMessage(String serverMessage)
    {
        String content;
        String type;
        String[] cut_message;
        String right;

        HashMap<String, String> message = new HashMap<>(2);
        try {
            if (serverMessage.equals("")) {
                message.put("type", "undefined");
                return message;
            }
            cut_message = serverMessage.split(";");
            message.put("type", cut_message[0].split(":")[1]);
            right = cut_message[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Message format error! omitting -> " + serverMessage + "\n");
            message.put("type", "undefined");
            return message;
        }
        try {
            message.put("content", right.split(":")[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            message.put("content", "");
        }
        return message;
    }

    public void run() {
        try {
            audioController.setupAudioOutput();

            byte[] decodedContent;
            String serverMessage;
            HashMap<String, String> processedMessage;
            while (true) {
                serverMessage = getServerMessage();
                processedMessage = processServerMessage(serverMessage);
                switch (processedMessage.get("type")) {
                    case "joined":
                        connectionController.getClientController().showCallPane();
                        connectionController.getClientController().setCallerName(processedMessage.get("content"));
                        break;
                    case "audio":
                        if (!audioController.audioOutputOK) break; //don't process received audio when audio output setup failed
                        try {
                            decodedContent = DatatypeConverter.parseBase64Binary(processedMessage.get("content"));
                        } catch (ArrayIndexOutOfBoundsException e) {
                            e.printStackTrace();
                            continue;
                        }

                        if (audioController.speakerAvailable() < AudioController.SOUND_BUFFER_SIZE) {
                            audioController.flushSpeakers();
                        } else {
                            audioController.writeSpeakers(decodedContent);
                        }
                        break;

                    case "video":
                        try {
                            decodedContent = DatatypeConverter.parseBase64Binary(processedMessage.get("content"));
                        } catch (ArrayIndexOutOfBoundsException e) {
                            e.printStackTrace();
                            continue;
                        }
                        InputStream is = new ByteArrayInputStream(decodedContent);
                        BufferedImage image = ImageIO.read(is);
                        connectionController.getClientController().setCallerViewImage(image);
                        break;

                    case "disconnect":
                        System.out.println("Exiting because server said so");
                        connectionController.getClientController().global_exit();
                        return;
                    case "error":
                        System.out.println(serverMessage);
                        connectionController.getClientController().errorMessageHandler(processedMessage.get("content"));
                        break;
                    case "confirm":
                        if (processedMessage.get("content").equals("Successfully changed username")) {
                            connectionController.getClientController().showUsernameSetPane();
                        }
                        if (processedMessage.get("content").equals("Successfully called user")) {
                            connectionController.getClientController().showCallPane();
                        }
                        break;
                    case "undefined":
                        break;
                    default:
                        System.out.println("Type error in received message! -> " + processedMessage.get("type") + "\n");
                }
            }

        } catch (IOException e) {
            System.out.println("receive from server got problems");
            e.printStackTrace();
            connectionController.getClientController().global_exit();
        }
    }
}
