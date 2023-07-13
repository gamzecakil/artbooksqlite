package com.gamzeuysal.artbooksqlitecourserewriting;

public class Art {
   private String name;
   private int id;
    //Constructor
    public Art(int id,String name){
        this.id = id;
        this.name = name;
    }
    //Getter & Setter

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
