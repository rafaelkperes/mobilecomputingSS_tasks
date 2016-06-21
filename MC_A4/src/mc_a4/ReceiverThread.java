/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mc_a4;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rafaelkperes
 */
public class ReceiverThread implements Runnable {

    private static final Logger LOGGER = Logger.getLogger("MCA4");

    private final LinkedBlockingDeque<PacketContent> packetsToSend;
    private final HashSet<PacketContent> alreadyReceived;

    private final ConcurrentHashMap<String, List<String>> routeCache;
    private final ConcurrentHashMap<Long, PacketContent> waitingForRoute;

    public ReceiverThread(LinkedBlockingDeque<PacketContent> packetsToSend,
            ConcurrentHashMap routeCache, ConcurrentHashMap waitingForRoute) {
        this.packetsToSend = packetsToSend;
        this.alreadyReceived = new HashSet<>();

        this.routeCache = routeCache;
        this.waitingForRoute = waitingForRoute;
    }

    @Override
    public void run() {
        LOGGER.log(Level.INFO, "Receiver thread started!");
        try {
            byte[] buf = new byte[1500];
            DatagramSocket sock = new DatagramSocket(5000 + 20);
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            while (true) {
                //LOGGER.log(Level.INFO, "waiting for packet...");
                sock.receive(packet);
                byte[] b = packet.getData();

                PacketContent content = MC_A4.deserializeContent(b);
                //System.out.println(content.getRoute());
                //ByteArrayInputStream bis = new ByteArrayInputStream(b);
                //ObjectInput in = new ObjectInputStream(bis);
                //PacketContent content = (PacketContent) in.readObject();
                if (!alreadyReceived.contains(content)) {
                    alreadyReceived.add(content);
                    LOGGER.log(Level.INFO, "received: {0}", content.toString());
                    System.out.println("received " + content.getType() + " from: " + content.getOriginalsender());

                    if (PacketContent.Type.ROUTE_REQUEST.equals(content.getType())) {
                        content.addToRoute(MC_A4.getIPfromInterface("wlan0"));
                        LOGGER.log(Level.INFO, "RREQ received: ", content.getRoute());
                    }

                    if (MC_A4.isMyIP(content.getDestination())) {
                        if (null != content.getType()) {
                            switch (content.getType()) {
                                case ROUTE_REQUEST:
                                    LOGGER.log(Level.INFO, "destination RREQ received: ", content.getRoute());
                                    System.out.println("CREATED RREP");
                                    PacketContent msg = new PacketContent(content.getId(),
                                            content.getDestination(), content.getOriginalsender(),
                                            PacketContent.Type.ROUTE_REPLY,
                                            content.getRoute());
                                    packetsToSend.put(msg);
                                    break;
                                case ROUTE_REPLY:
                                    LOGGER.log(Level.INFO, "destination RREP received: ", content.getRoute());
                                    System.out.println("RREP route: " + content.getRoute());
                                    msg = waitingForRoute.get(content.getId());
                                    msg.setRoute(content.getRoute());
                                    packetsToSend.put(msg);
                                    break;
                                default:
                                    LOGGER.log(Level.INFO, "PACKET REACHED DESTINATION: {0}", content.toString());
                                    System.out.println("I'm the destination: " + new String(content.getContent()));
                                    break;
                            }
                        }
                    } else {
                        /* forward packet! */
                        packetsToSend.put(content);
                        LOGGER.log(Level.INFO, "added to sender queue: {0}", content.toString());
                    }
                }
            }
        } catch (SocketException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (IOException | ClassNotFoundException | InterruptedException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

}
