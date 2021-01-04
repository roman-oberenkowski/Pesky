package pl.ROFS.threads;

import pl.ROFS.controllers.ConnectionController;

public class DisconnectWatchdogThread extends Thread{
    ConnectionController connectionController;

    public DisconnectWatchdogThread(ConnectionController connectionController) {
        this.connectionController = connectionController;
    }
    public void run(){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }
        connectionController.getClientController().global_exit();

    }
}
