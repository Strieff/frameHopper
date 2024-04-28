package com.example.engineer.FrameProcessor;

import org.springframework.stereotype.Component;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class FrameProcessorClient extends Thread{
    private static String HOST = "localhost";
    private static int PORT = 65432;

    private BlockingQueue<String> commandQueue = new LinkedBlockingQueue<>();

    private BlockingQueue<String> responseQueue = new LinkedBlockingQueue<>();
    private PrintWriter out;
    private BufferedReader in;
    Socket socket;

    public void connect(){
        try{
            socket = new Socket(HOST,PORT);
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8),true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            Thread clientThread = new Thread(new ClientRunnable());
            clientThread.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String send(String request,boolean wait){
        commandQueue.offer(request);

        if(wait)
            return sendAndWait(request);
        else
            send(request);

        return "OK";
    }

    private void send(String request){
        commandQueue.offer(request);

        new Thread(()->{
            String s = null;

            while (s == null)
                s = responseQueue.poll();
        }).start();
    }

    private String sendAndWait(String request){
        commandQueue.offer(request);

        String s = null;
        while (s == null)
            s = responseQueue.poll();

        return s;
    }

    private class ClientRunnable implements Runnable{
        @Override
        public void run() {
            try{
                while(!Thread.currentThread().isInterrupted()){
                    String command = commandQueue.poll();

                    if(command!=null && command.split(";")[0].equals("-1")) {
                        out.println(command);
                        out.flush();
                        out.close();
                        in.close();
                        socket.close();
                        throw new InterruptedException();
                    }

                    if(command!=null){
                        out.println(command);
                        out.flush();

                        responseQueue.offer(in.readLine());
                    }

                    //Thread.sleep(commandQueue.size()>2?0:50);
                }
            }catch (InterruptedException e){
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
