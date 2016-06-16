/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mc_a4;

import java.io.Serializable;

/**
 *
 * @author rafaelkperes
 */
public class PacketContent implements Serializable {
    
    enum Type {
        CONTENT,
        ACK
    }
    
    private final String originalsender;
    private final String destination;
    private final byte[] content;
    private final Type type;

    public PacketContent(String originalsender, String destination, byte[] content, Type type) {
        this.originalsender = originalsender;
        this.destination = destination;
        this.content = content;
        this.type = type;
    }

    public String getOriginalsender() {
        return originalsender;
    }

    public String getDestination() {
        return destination;
    }

    public byte[] getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "PacketContent{" + "originalsender=" + originalsender + ", destination=" + destination + ", content=" + content + '}';
    }
    
}
