package com.example.roseanna.massachusettsinfo;

/**
 * Created by roseanna on 4/1/16.
 */
public class Town {
    public String name, pop, pop2;
    public int population, population2;


    public Town(String name, int pop, int pop2){
        this.name           = name;
        this.population     = pop;
        this.population2    = pop2;
    }

    public String getChange(){
        float popchange = ((float)population2-population)/population * 100;
        return String.format("%.2f", popchange);
    }

}
