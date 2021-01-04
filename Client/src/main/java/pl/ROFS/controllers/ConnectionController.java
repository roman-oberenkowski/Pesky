package pl.ROFS.controllers;

import pl.ROFS.threads.ConnectThread;
import pl.ROFS.threads.ReceiveFromServerThread;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class ConnectionController {

    private final int port = 4201;

    private PrintWriter writer;
    private BufferedReader reader;

    ClientController clientController;

    ReceiveFromServerThread receiveThread;

    public ConnectionController (ClientController controller) {
        this.clientController = controller;
        receiveThread = new ReceiveFromServerThread(this);
    }

    public int getPort() { return port; }
    public PrintWriter getWriter() { return this.writer; }
    public BufferedReader getReader() { return this.reader; }
    public ClientController getClientController() { return clientController; }

    public void setWriter(PrintWriter writer) {this.writer = writer;}
    public void setReader(BufferedReader reader) {this.reader = reader;}


    public void send(String type,String content) {
        String msg;
        if(content.length()>0)
            msg="type:"+type+";content:"+content;
        else{
            msg="type:"+type+";";
        }
        synchronized (writer) {
            try{
                if(writer.checkError()){
                    System.out.println("writer error");
                }
                if(msg.length()>200000){
                    System.out.println("msg.len: "+msg.length());
                }
                writer.println(msg);
                if(writer.checkError()){
                    System.out.println("writer error");
                }

            } catch(Exception e){
                System.out.println("writing got problem");
                e.printStackTrace();
                clientController.global_exit();
            }
        }
    }

    public void startServerReceivingThread() {
        receiveThread.start();
    }

    public void startConnectThread() {
        new ConnectThread(this).start();
    }
}
