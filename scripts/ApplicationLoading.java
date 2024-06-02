import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Main {
    private static JLabel label;
    private static Timer timer;
    private static int dotCount = 0;

    public static void main(String[] args) {
        JFrame frame = createView();
        timer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateLabel();
            }
        });
        timer.start();

        try(ServerSocket serverSocket = new ServerSocket(65444)) {
            serverSocket.setSoTimeout(60000);

            try{
                Socket socket = serverSocket.accept();
                System.out.println("FrameHopper is running correctly");
                socket.close();
                throw new Exception("Socket closed");
            }catch (SocketTimeoutException e){
                System.out.println(e.getMessage());;
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        frame.dispose();
        try {
            Thread.sleep(750);
        } catch (InterruptedException ignored){}
        timer.stop();
    }

    public static JFrame createView(){
        JFrame frame = new JFrame();
        frame.setSize(300,100);
        frame.setUndecorated(true);

        label = new JLabel("LOADING", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 32));

        frame.getContentPane().add(label,BorderLayout.CENTER);

        frame.setLocationRelativeTo(null);
        frame.setAlwaysOnTop(true);
        frame.setVisible(true);

        return frame;
    }

    private static void updateLabel() {
        dotCount = (dotCount + 1) % 4;
        StringBuilder text = new StringBuilder("LOADING");
        for (int i = 0; i < dotCount; i++) {
            text.append(".");
        }
        label.setText(text.toString());
    }
}