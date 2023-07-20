package it.gangoffive.eculture.model;

public class UserModel {

    private final String surname;
    private final String name;
    private final String type;

    public UserModel(
            String surname, String name, String type) {
        this.surname = surname;
        this.name = name;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getSurname() {
        return surname;
    }

    public String getName() {
        return name;
    }
}
