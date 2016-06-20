/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mc_a4;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author rafaelkperes
 */
public class PacketContent implements Serializable {
    
    public enum Type {
        CONTENT,
        ACK
    }
    
    private final Long id;
    private final String originalsender;
    private final String destination;
    private final byte[] content;
    private final Type type;

    /**
     *
     * @param originalsender
     * @param destination
     * @param content
     * @param type
     */
    public PacketContent(String originalsender, String destination,
            byte[] content, Type type) {
        this.id = System.currentTimeMillis();
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
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.id);
        hash = 41 * hash + Objects.hashCode(this.originalsender);
        hash = 41 * hash + Objects.hashCode(this.destination);
        hash = 41 * hash + Arrays.hashCode(this.content);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PacketContent other = (PacketContent) obj;
        if (!Objects.equals(this.originalsender, other.originalsender)) {
            return false;
        }
        if (!Objects.equals(this.destination, other.destination)) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PacketContent{" 
                + "id=" + id 
                + ", originalsender=" + originalsender 
                + ", destination=" + destination 
                + ", content=" + new String(content) 
                + ", type=" + type + '}';
    }
    
}
