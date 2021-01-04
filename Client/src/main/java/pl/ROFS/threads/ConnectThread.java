package pl.ROFS.threads;

import javafx.application.Platform;
import pl.ROFS.controllers.ClientController;
import pl.ROFS.controllers.ConnectionController;

import java.io.*;
import java.net.*;

public class ConnectThread extends Thread {

    private final ConnectionController connectionController;

    public ConnectThread(ConnectionController connectionController) {
        this.connectionController = connectionController;
    }

    public void run() {
        try {
            Socket clientSocket = new Socket();
            clientSocket.connect(new InetSocketAddress(connectionController.getClientController().addressField.getText(), connectionController.getPort()), 1000);
            OutputStream os = clientSocket.getOutputStream();
            InputStream is = clientSocket.getInputStream();
            connectionController.setWriter(new PrintWriter(os, true));
            connectionController.setReader(new BufferedReader(new InputStreamReader(is)));
        } catch (ConnectException | SocketTimeoutException e) {
            connectionController.getClientController().printConnectionErrorMessage("Connection failed");
            return;
        } catch (UnknownHostException e) {
            connectionController.getClientController().printConnectionErrorMessage("Incorrect server address");
            return;
        } catch (IOException e) {
            connectionController.getClientController().printConnectionErrorMessage("Unexpected IO Error");
            return;
        }
        connectionController.getClientController().showConnectedPane();
    }
}
