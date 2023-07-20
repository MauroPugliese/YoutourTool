package it.gangoffive.eculture.model;

public class CityModel {
    public String city_name;
    public String province;
    public String region;

    public CityModel(String region, String province, String city_name) {
        this.region = region;
        this.province = province;
        this.city_name = city_name;
    }

    public String getProvince() {
        return province;
    }

    public String getRegion() {
        return region;
    }


}
