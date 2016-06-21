/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mc_a4;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author rafaelkperes
 */
public class PacketContent implements Serializable {

    public enum Type {
        ROUTE_REQUEST,
        ROUTE_REPLY,
        CONTENT
    }
    
    private List<String> route;

    private final Long id;
    private final String originalsender;
    private final String destination;
    private final byte[] content;
    private final Type type;
    private Integer leap;

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
        this.leap = 0;

        this.route = new ArrayList<>();
    }

    public PacketContent(Long id, String originalsender, String destination,
            Type type, List<String> route) {
        this.id = id;
        this.originalsender = originalsender;
        this.destination = destination;
        this.content = "".getBytes();
        this.type = type;
        this.leap = 0;

        this.route = route;
    }

    /* Task 2 */
    public void addToRoute(String ip) {
        this.route.add(ip);
    }

    public void setRoute(List<String> route) {
        this.route = route;
    }
    
    public String getNextOnRoute(String selfIp) {
        int pos = route.indexOf(selfIp);
        return route.get(pos + 1);
    }

    public String getPreviousOnRoute(String selfIp) {
        int pos = route.indexOf(selfIp);
        return route.get(pos - 1);
    }

    public List<String> getRoute() {
        return route;
    }
    
    public boolean hasRoute() {
        return !(route == null || route.isEmpty());
    }

    /* End of Task 2 */
    public void leap() {
        leap++;
    }

    public Long getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public Integer getLeap() {
        return leap;
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
        return "PacketContent{" + "id=" + id + ", originalsender=" + originalsender + ", destination=" + destination + ", content=" + new String(content) + ", type=" + type + ", leap=" + leap + ", route=" + route + '}';
    }

}
