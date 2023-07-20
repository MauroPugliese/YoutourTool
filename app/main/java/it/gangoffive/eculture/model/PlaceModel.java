package it.gangoffive.eculture.model;

public class PlaceModel {

    private final String id;
    private final String title;
    private final String description;
    private final String roomID;
    private final String author;
    private final String period;
    private final String createdBy;
    private final boolean isOpen;
    private final String minigame;

    public PlaceModel(String placeID, String title, String description, String roomID, String author, String period, String createdBy, boolean isOpen, String minigame) {
        this.id = placeID;
        this.title = title;
        this.description = description;
        this.roomID = roomID;
        this.author = author;
        this.period = period;
        this.createdBy = createdBy;
        this.isOpen = isOpen;
        this.minigame = minigame;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public String getAuthor() {
        return author;
    }

    public String getPeriod() {
        return period;
    }

    public String getRoomID() {
        return roomID;
    }

    public boolean getIsOpen() {
        return isOpen;
    }

    public String getMinigame() {
        return minigame;
    }
}
