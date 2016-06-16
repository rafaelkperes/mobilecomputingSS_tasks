/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mc_a4;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rafaelkperes
 */
public class ReceiverThread implements Runnable {

    private final Logger logger;

    public ReceiverThread(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            logger.info("Receiver thread started!");

            byte[] buf = new byte[1500];

            DatagramSocket sock = new DatagramSocket(5000 + 20);
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            while (true) {
                logger.info("received: " + content.toString());
                sock.receive(packet);
                byte[] b = packet.getData();
                ByteArrayInputStream bis = new ByteArrayInputStream(b);
                ObjectInput in = new ObjectInputStream(bis);
                PacketContent content = (PacketContent) in.readObject();
                logger.info("received: " + content.toString());
            }
        } catch (SocketException ex) {
            Logger.getLogger(ReceiverThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(ReceiverThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
