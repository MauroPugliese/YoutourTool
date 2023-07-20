package it.gangoffive.eculture.model;

public class QuizModel {

    private final String quest1;
    private final String quest2;
    private final String quest3;

    private final boolean answ1;
    private final boolean answ2;
    private final boolean answ3;

    private final boolean hasPuzzle;

    private final String place_id;


    public QuizModel(String quest1, String quest2, String quest3, boolean answ1, boolean answ2, boolean answ3, String place_id, boolean hasPuzzle) {

        this.quest1 = quest1;
        this.quest2 = quest2;
        this.quest3 = quest3;

        this.answ1 = answ1;
        this.answ2 = answ2;
        this.answ3 = answ3;

        this.place_id = place_id;
        this.hasPuzzle = hasPuzzle;
    }



    public String getQuest1(){ return this.quest1; }
    public String getQuest2(){ return this.quest2; }
    public String getQuest3(){ return this.quest3; }

    public boolean getAnsw1() { return this.answ1; }
    public boolean getAnsw2() { return this.answ2; }
    public boolean getAnsw3() { return this.answ3; }

    public String getPlace_id() {return this.place_id; }
    public boolean getHasPuzzle() {return this.hasPuzzle; }
}
