/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mc_a4;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author rafaelkperes
 */
public class MC_A4 {

    public static void send() throws SocketException, IOException {
        DatagramSocket sock = new DatagramSocket();
        sock.setBroadcast(true);
        byte[] bcast_msg = null;
        DatagramPacket packet = new DatagramPacket(
                bcast_msg, bcast_msg.length,
                InetAddress.getByName("192.168.24.255"),
                5000 + 20
        );
        sock.send(packet);
    }

    public static void recv() throws SocketException, IOException {
        byte[] buf = null;

        DatagramSocket sock = new DatagramSocket(5000 + 20);
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        while (true) {
            sock.receive(packet);
            packet.getData();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("As4Log");
        FileHandler fh;

        try {

            // This block configure the logger with handler and formatter  
            fh = new FileHandler("as4.log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            // the following statement is used to log any messages  
            logger.info("My first log");
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
        
        Thread recv = new Thread(new ReceiverThread(logger));
        recv.start();
        
    }
}
