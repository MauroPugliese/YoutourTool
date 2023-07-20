package it.gangoffive.eculture.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TourModel {

    private String id;
    private String title;
    private String subtitle;
    private String description;
    private String structure;
    private ArrayList<String> rooms;
    private List<HashMap<String, ArrayList<String>>> places;
    private String createdBy;

    public TourModel(
            String id, String title, String subtitle, String description, String structure, ArrayList<String> rooms, List<HashMap<String, ArrayList<String>>> places, String createdBy) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.description = description;
        this.structure = structure;
        this.rooms = rooms;
        this.places = places;
        this.createdBy = createdBy;
    }

    public TourModel(){
        this.id = null;
        this.title = null;
        this.subtitle = null;
        this.description = null;
        this.structure = null;
        this.places = null;
        this.rooms = null;
    }

    public String getId(){
        return this.id;
    }
    public String getTitle(){
        return this.title;
    }
    public String getSubtitle(){
        return this.subtitle;
    }
    public String getDescription(){
        return this.description;
    }
    public String getStructure() {
        return this.structure;
    }
    public ArrayList<String> getRooms() {
        return this.rooms;
    }
    public ArrayList<HashMap<String, ArrayList<String>>> getPlaces() {
        return (ArrayList<HashMap<String, ArrayList<String>>>) this.places;
    }
    public String getCreatedBy() {
        return createdBy;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStructure(String structure) {
        this.structure = structure;
    }

    public void setRooms(ArrayList<String> rooms) {
        this.rooms = rooms;
    }


    /**
     *
     * Aggiunge un Punto di Interesse all'Itinerario prendendo in input
     * l'ordine di visita, la stanza associata e il nome del Punto di Interesse
     *
     * @param position int
     * @param roomID String
     * @param places String
     */
    public void setPlaces(int position, String roomID, String places){
        position = position - 1;
        if (this.places.size() == 0){
            this.places = new ArrayList<>();
            for (int i = 0; i < this.rooms.size(); i++){
                ArrayList<String> arrayListId = new ArrayList<>();
                arrayListId.add(this.rooms.get(i));
                HashMap<String, ArrayList<String>> hashMap = new HashMap<>();
                hashMap.put("roomid", arrayListId);
                hashMap.put("places", new ArrayList<>());
                this.places.add(i, hashMap);
            }
        }
        if (this.places.get(position).get("roomid").contains(roomID)){
            ArrayList<String> arrayList = this.places.get(position).get("places");
            arrayList.add(places);
            HashMap<String, ArrayList<String>> hashMap = this.places.get(position);
            hashMap.put("places", arrayList);
            this.places.set(position, hashMap);
        } else {
            ArrayList<String> arrayListId = new ArrayList<>();
            ArrayList<String> arrayListPlace = new ArrayList<>();
            arrayListId.add(roomID);
            arrayListPlace.add(places);
            HashMap<String, ArrayList<String>> hashMap = new HashMap<>();
            hashMap.put("roomid", arrayListId);
            hashMap.put("places", arrayListPlace);
            this.places.set(position, hashMap);
        }

    }

    /**
     *
     * Azzera le stanza associate all'itinerario
     *
     */
    public void resetPlaces(){
        this.places = new ArrayList<>();
    }

    public void setCreatedBy(String createdBy){
        this.createdBy = createdBy;
    }

}
