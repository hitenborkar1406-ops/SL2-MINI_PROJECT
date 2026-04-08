package com.example.mini_project;

public class Item {
    private int id;
    private String name;
    private String desc;
    private String location;
    private String type;          // "Lost" or "Found"
    private String imagePath;
    private String posterName;
    private String posterContact;
    private String status;        // "active" or "resolved"
    private long createdAt;       // System.currentTimeMillis()

    public Item(int id, String name, String desc, String location, String type,
                String imagePath, String posterName, String posterContact,
                String status, long createdAt) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.location = location;
        this.type = type;
        this.imagePath = imagePath;
        this.posterName = posterName;
        this.posterContact = posterContact;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId()             { return id; }
    public String getName()        { return name; }
    public String getDesc()        { return desc; }
    public String getLocation()    { return location; }
    public String getType()        { return type; }
    public String getImagePath()   { return imagePath; }
    public String getPosterName()  { return posterName; }
    public String getPosterContact(){ return posterContact; }
    public String getStatus()      { return status; }
    public long getCreatedAt()     { return createdAt; }
}
