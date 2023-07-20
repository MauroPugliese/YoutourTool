package it.gangoffive.eculture.model;

public class StructureModel {
    private final String id;
    private final String name;
    private final String address;
    private final String region;
    private final String province;
    private final String city;
    private final String schedule;
    private final String createdBy;

    public StructureModel(String id, String name, String address, String region, String province, String city, String schedule, String createdBy) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.region = region;
        this.province = province;
        this.city = city;
        this.schedule = schedule;
        this.createdBy = createdBy;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getRegion() {
        return region;
    }

    public String getProvince() {
        return province;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getName() {
        return name;
    }

    public String getSchedule() {
        return schedule;
    }

    public String getId() {
        return id;
    }
}
