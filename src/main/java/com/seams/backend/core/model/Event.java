package com.seams.backend.core.model;

import jakarta.persistence.*;

@Entity
@Table(indexes = {
    @Index(name = "idx_event_status_hub", columnList = "status")
})
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String eventDate;
    private String status; // PENDING, ACCEPTED
    private String aseadoProfile;
    
    private String localSyncId; // ID from the scanner node
    private boolean hasLogout;
    @Column(columnDefinition = "TEXT")
    private String filterJson;

    public Event() {}
    public Event(Integer id, String name, String eventDate, String status, String aseadoProfile) {
        this.id = id;
        this.name = name;
        this.eventDate = eventDate;
        this.status = status;
        this.aseadoProfile = aseadoProfile;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAseadoProfile() { return aseadoProfile; }
    public void setAseadoProfile(String aseadoProfile) { this.aseadoProfile = aseadoProfile; }

    public String getLocalSyncId() { return localSyncId; }
    public void setLocalSyncId(String localSyncId) { this.localSyncId = localSyncId; }
    public boolean isHasLogout() { return hasLogout; }
    public void setHasLogout(boolean hasLogout) { this.hasLogout = hasLogout; }
    public String getFilterJson() { return filterJson; }
    public void setFilterJson(String filterJson) { this.filterJson = filterJson; }
}
