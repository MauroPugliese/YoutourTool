package it.gangoffive.eculture.model;

public class RoomModel {
    private final String id;
    private final String name;
    private final String description;
    private final String structure_id;
    private final boolean isOpen;
    private final String createdBy;
    private final String visiting_time;

    public RoomModel(String id, String name, String description, String structure_id, boolean isOpen, String createdBy, String visiting_time) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.structure_id = structure_id;
        this.isOpen = isOpen;
        this.createdBy = createdBy;
        this.visiting_time = visiting_time;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getStructure_id() {
        return structure_id;
    }

    public boolean getIsOpen() {
        return isOpen;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getVisiting_time() {
        return visiting_time;
    }

}
