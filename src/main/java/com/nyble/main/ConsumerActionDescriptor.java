package com.nyble.main;

import java.util.Objects;

public class ConsumerActionDescriptor {
    private int id;
    private String systemId;

    public ConsumerActionDescriptor(int id, String systemId) {
        this.id = id;
        this.systemId = systemId;
    }


    public int getId() {
        return id;
    }

    public String getSystemId() {
        return systemId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConsumerActionDescriptor)) return false;
        ConsumerActionDescriptor that = (ConsumerActionDescriptor) o;
        return id == that.id &&
                systemId.equals(that.systemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, systemId);
    }
}
