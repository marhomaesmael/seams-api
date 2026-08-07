package com.seams.backend.core.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class ConnectedNode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name; // Aseado Profile Name
    private String deptCode;
    private String token;
    private Instant lastHeartbeat;
    private String status; // PENDING, ACTIVE, REJECTED, OFFLINE

    public ConnectedNode() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDeptCode() { return deptCode; }
    public void setDeptCode(String deptCode) { this.deptCode = deptCode; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Instant getLastHeartbeat() { return lastHeartbeat; }
    public void setLastHeartbeat(Instant lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
