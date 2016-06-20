/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mc_a4;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author rafaelkperes
 */
public class MC_A4 {

    public static final Logger LOGGER = Logger.getLogger("MCA4");

    public static String getIPfromInterface(String interfaceName) throws SocketException, UnknownHostException {
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets)) {
            for (InetAddress inetAddress : Collections.list(netint.getInetAddresses())) {
                String inetAStringString = inetAddress.getHostAddress();
                if (inetAStringString.startsWith("192.168.")) {
                    return inetAStringString;
                }
            }
            /*if (interfaceName.equalsIgnoreCase(netint.getName())) {
                for (InetAddress inetAddress : Collections.list(netint.getInetAddresses())) {
                    if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }*/
        }

        return InetAddress.getLocalHost().getHostAddress();
    }

    public static Boolean isMyIP(String ipString) throws SocketException, UnknownHostException {
        InetAddress ip = InetAddress.getByName(ipString);

        Enumeration e = NetworkInterface.getNetworkInterfaces();
        while (e.hasMoreElements()) {
            NetworkInterface n = (NetworkInterface) e.nextElement();
            Enumeration ee = n.getInetAddresses();
            while (ee.hasMoreElements()) {
                InetAddress i = (InetAddress) ee.nextElement();
                if (i.equals(ip)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static byte[] serializeContent(PacketContent content) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(content);
            byte[] _bytes = bos.toByteArray();
            return _bytes;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
    }

    public static PacketContent deserializeContent(byte[] b) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(b);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            PacketContent content = (PacketContent) in.readObject();
            return content;
        } finally {
            try {
                bis.close();
            } catch (IOException ex) {
                // ignore close exception
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
    }

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        FileHandler fh;

        /* LOGGER config */
        try {

            // This block configure the LOGGER with handler and formatter  
            fh = new FileHandler("as4.log");
            LOGGER.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            // the following statement is used to log any messages  
            LOGGER.log(Level.INFO, "Logger started");
        } catch (SecurityException | IOException e) {
            System.out.println("couldn't start LOGGER");
            System.exit(2);
        }

        /* sender queue */
        LinkedBlockingDeque<PacketContent> senderQ = new LinkedBlockingDeque<>();

        /* starting receiver */
        Thread recv = new Thread(new ReceiverThread(senderQ));
        recv.start();

        /* starting sender */
        Thread send = new Thread(new SenderThread(senderQ));
        send.start();

        while (true) {
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Content:");
            String s = bufferRead.readLine();
            System.out.println("Destination:");
            String dest = bufferRead.readLine();
            String origin = getIPfromInterface("wlan0");

            PacketContent content = new PacketContent(origin, dest, s.getBytes(),
                    PacketContent.Type.CONTENT);
            LOGGER.log(Level.INFO, "request to send: {0}", content);
            senderQ.put(content);
        }

    }
}
