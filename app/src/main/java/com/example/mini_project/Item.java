package com.example.mini_project;

public class Item {

    private int id;
    private String name, desc, location, type, imagePath;
    private String posterName, posterContact, status, category;
    private long createdAt;

    public Item(int id, String name, String desc, String location, String type, String imagePath,
                String posterName, String posterContact, String status, long createdAt, String category) {
        this.id            = id;
        this.name          = name;
        this.desc          = desc;
        this.location      = location;
        this.type          = type;
        this.imagePath     = imagePath;
        this.posterName    = posterName;
        this.posterContact = posterContact;
        this.status        = status;
        this.createdAt     = createdAt;
        this.category      = category;
    }

    // ── Getters ──────────────────────────────────────────────────
    public int    getId()           { return id; }
    public String getName()         { return name; }
    public String getDesc()         { return desc; }
    public String getLocation()     { return location; }
    public String getType()         { return type; }
    public String getImagePath()    { return imagePath; }
    public String getPosterName()   { return posterName; }
    public String getPosterContact(){ return posterContact; }
    public String getStatus()       { return status; }
    public long   getCreatedAt()    { return createdAt; }
    public String getCategory()     { return category; }

    // ── Setters ──────────────────────────────────────────────────
    public void setPosterName(String posterName)       { this.posterName = posterName; }
    public void setPosterContact(String posterContact) { this.posterContact = posterContact; }
    public void setStatus(String status)               { this.status = status; }
    public void setCategory(String category)           { this.category = category; }
}
