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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rafaelkperes
 */
public class SenderThread implements Runnable {

    private static final Logger LOGGER = Logger.getLogger("MCA4");
    private static final int NO_OF_TRIES = 10;
    

    private final LinkedBlockingDeque<PacketContent> packetsToSend;
    private final HashSet<PacketContent> alreadySent;

    private final ConcurrentHashMap<String, List<String>> routeCache;
    private final ConcurrentHashMap<Long, PacketContent> waitingForRoute;
    
    private long millis;

    public SenderThread(LinkedBlockingDeque<PacketContent> packetsToSend,
            ConcurrentHashMap routeCache, ConcurrentHashMap waitingForRoute) {
        this.packetsToSend = packetsToSend;
        this.alreadySent = new HashSet<>();

        this.routeCache = routeCache;
        this.waitingForRoute = waitingForRoute;
    }

    @Override
    public void run() {
        LOGGER.log(Level.INFO, "Sender thread started!");

        try {
            DatagramSocket sock = new DatagramSocket();

            while (true) {
                PacketContent content = packetsToSend.take();

                if (!alreadySent.contains(content)) {
                    if (PacketContent.Type.CONTENT.equals(content.getType())) {
                        if (content.hasRoute()) {
                            LOGGER.log(Level.INFO, "sending: {0}", content);

                            String selfIp = MC_A4.getIPfromInterface("wlan0");
                            System.out.println("SENDING CONTENT: from: " + content.getOriginalsender() + " to: " + content.getDestination()
                                    + " via: " + content.getNextOnRoute(selfIp));

                            content.leap();

                            byte[] bcast_msg = MC_A4.serializeContent(content);

                            sock.setBroadcast(false);
                            DatagramPacket packet = new DatagramPacket(bcast_msg,
                                    bcast_msg.length,
                                    InetAddress.getByName(content.getNextOnRoute(selfIp)),
                                    5000 + 20
                            );

                            System.out.println("Route disvocery time: " + (System.currentTimeMillis() - millis));
                            
                            for (int i = 0; i < NO_OF_TRIES; i++) {
                                sock.send(packet);
                                Thread.sleep(10);
                            }
                            
                            alreadySent.add(content);

                        } else if (routeCache.get(content.getId()) != null) {
                            // ROUTE CACHE NOT USED YET
                        } else {
                            // NO ROUTE YET
                            System.out.println("CREATED RREQ");
                            
                            waitingForRoute.put(content.getId(), content);
                            PacketContent rREQ = new PacketContent(content.getId(),
                                    content.getOriginalsender(),
                                    content.getDestination(),
                                    PacketContent.Type.ROUTE_REQUEST,
                                    content.getRoute());

                            rREQ.leap();
                            rREQ.addToRoute(MC_A4.getIPfromInterface("wlan0"));

                            LOGGER.log(Level.INFO, "sending: {0}", rREQ);
                            System.out.println(rREQ.getRoute());

                            sock.setBroadcast(true);
                            byte[] bcast_msg = MC_A4.serializeContent(rREQ);
                            DatagramPacket packet = new DatagramPacket(bcast_msg,
                                    bcast_msg.length, InetAddress.getByName("192.168.24.255"),
                                    5000 + 20
                            );

                            //System.out.println("TAMANHO: " + bcast_msg.length);
                            millis = System.currentTimeMillis();
                            
                            for (int i = 0; i < NO_OF_TRIES; i++) {
                                sock.send(packet);
                                Thread.sleep(10);
                            }

                            alreadySent.add(rREQ);
                        }
                    } else if (PacketContent.Type.ROUTE_REQUEST.equals(content.getType())) {
                        PacketContent rREQ = content;

                        LOGGER.log(Level.INFO, "sending: {0}", rREQ);

                        //System.out.println("RREQ: from: " + content.getOriginalsender() + " to: " + content.getDestination());

                        rREQ.leap();
                        // RECV SHOULD HAVE ALREADY ADDED ITSELF TO THE ROUTE

                        byte[] bcast_msg = MC_A4.serializeContent(rREQ);

                        sock.setBroadcast(true);
                        DatagramPacket packet = new DatagramPacket(bcast_msg,
                                bcast_msg.length, InetAddress.getByName("192.168.24.255"),
                                5000 + 20
                        );

                        for (int i = 0; i < NO_OF_TRIES; i++) {
                            sock.send(packet);
                            Thread.sleep(10);
                        }

                        alreadySent.add(content);
                    } else { // ROUTE REPLY
                        PacketContent rREP = content;

                        LOGGER.log(Level.INFO, "sending: {0}", rREP);

                        //System.out.println("RREP: from: " + content.getOriginalsender() + " to: " + content.getDestination());
                        //System.out.println(content.getRoute());
                        
                        rREP.leap();

                        byte[] bcast_msg = MC_A4.serializeContent(rREP);
                        
                        //System.out.println("TAMANHO: " + bcast_msg.length);

                        sock.setBroadcast(false);
                        
                        String selfIp = MC_A4.getIPfromInterface("wlan0");
                        try {
                            DatagramPacket packet = new DatagramPacket(bcast_msg,
                                    bcast_msg.length,
                                    InetAddress.getByName(rREP.getPreviousOnRoute(selfIp)),
                                    5000 + 20
                            );
                            for (int i = 0; i < NO_OF_TRIES; i++) {
                                sock.send(packet);
                                Thread.sleep(10);
                            }
                        } catch (IndexOutOfBoundsException e) {
                            System.out.println("NO PREVIOUS ON ROUTE - SEND TO ITSELF!");
                            DatagramPacket packet = new DatagramPacket(bcast_msg,
                                    bcast_msg.length,
                                    InetAddress.getByName(selfIp),
                                    5000 + 20
                            );
                            for (int i = 0; i < NO_OF_TRIES; i++) {
                                sock.send(packet);
                                Thread.sleep(10);
                            }
                        }

                        alreadySent.add(content);
                    }
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
