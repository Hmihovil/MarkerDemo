package com.example.tbrams.markerdemo.components;


import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.UUID;

public class AreaPolygon {
    private String id;
    private String areaId;
    List<LatLng> vertexList;

    public AreaPolygon(String areaId, List<LatLng> vertexList) {
        this.id = UUID.randomUUID().toString();
        this.areaId = areaId;
        this.vertexList = vertexList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public List<LatLng> getVertexList() {
        return vertexList;
    }

    public void setVertexList(List<LatLng> vertexList) {
        this.vertexList = vertexList;
    }
}
