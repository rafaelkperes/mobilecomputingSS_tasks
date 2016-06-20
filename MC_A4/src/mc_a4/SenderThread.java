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
import java.net.UnknownHostException;
import java.util.HashSet;
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
    private final HashSet<PacketContent> alreadySent;

    public SenderThread(LinkedBlockingDeque<PacketContent> packetsToSend) {
        this.packetsToSend = packetsToSend;
        this.alreadySent = new HashSet<>();
    }

    @Override
    public void run() {
        LOGGER.log(Level.INFO, "Sender thread started!");

        try {
            DatagramSocket sock = new DatagramSocket();
            sock.setBroadcast(true);

            while (true) {                
                PacketContent content = packetsToSend.take();

                if (!alreadySent.contains(content)) {
                    LOGGER.log(Level.INFO, "trying to send: {0}", content);
                    System.out.println("sending: " + new String(content.getContent()));
                    System.out.println("from: " + content.getOriginalsender()+ " to: " + content.getDestination());
                    content.leap();
                    byte[] bcast_msg = MC_A4.serializeContent(content);
                    DatagramPacket packet = new DatagramPacket(bcast_msg,
                            bcast_msg.length, InetAddress.getByName("192.168.24.255"),
                            5000 + 20
                    );
                    
                    for (int i = 0; i < 100; i++) {
                        sock.send(packet);
                        Thread.sleep(10);
                    }
                    
                    alreadySent.add(content);
                    LOGGER.log(Level.INFO, "packet sent");
                } else {
                    LOGGER.log(Level.INFO, "received duplicate: {0}", content);
                }
            }
        } catch (SocketException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (IOException | InterruptedException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

    }

}
