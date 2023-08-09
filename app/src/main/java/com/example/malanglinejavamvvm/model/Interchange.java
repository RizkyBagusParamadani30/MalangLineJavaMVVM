package com.example.malanglinejavamvvm.model;

import java.util.HashSet;
import java.util.Set;

public class Interchange {

    public String idInterchange;
    public String name;
    public Set<String> pointIds;
    public Set<PointTransport> points;


    public Interchange() {
        this.pointIds = new HashSet<>();
        this.points = new HashSet<>();
    }


}