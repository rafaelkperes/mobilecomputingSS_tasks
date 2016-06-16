/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mc_a4;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rafaelkperes
 */
public class SenderThread implements Runnable {

    private static final Logger LOGGER = Logger.getLogger("MCA4");

    private final LinkedBlockingDeque<PacketContent> packetsToSend;

    public SenderThread(LinkedBlockingDeque<PacketContent> packetsToSend) {
        this.packetsToSend = packetsToSend;
    }

    @Override
    public void run() {
        LOGGER.log(Level.INFO, "Sender thread started!");

        while (true) {
            try {
                DatagramSocket sock = new DatagramSocket();
                sock.setBroadcast(true);

                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                PacketContent content = packetsToSend.take();

                LOGGER.log(Level.INFO, "trying to send: {0}", content);
                byte[] bcast_msg = MC_A4.serializeContent(content);
                DatagramPacket packet = new DatagramPacket(bcast_msg,
                        bcast_msg.length, InetAddress.getByName("192.168.24.255"),
                        5000 + 20
                );
                sock.send(packet);
            } catch (SocketException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } catch (UnknownHostException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } catch (IOException | InterruptedException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }

}
