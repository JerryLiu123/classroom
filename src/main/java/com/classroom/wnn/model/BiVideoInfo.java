package com.classroom.wnn.model;

public class BiVideoInfo {
    private Integer id;

    private String vName;

    private Integer vAvailable;

    private Integer vIsdel;

    private Integer cClassid;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getvName() {
        return vName;
    }

    public void setvName(String vName) {
        this.vName = vName == null ? null : vName.trim();
    }

    public Integer getvAvailable() {
        return vAvailable;
    }

    public void setvAvailable(Integer vAvailable) {
        this.vAvailable = vAvailable;
    }

    public Integer getvIsdel() {
        return vIsdel;
    }

    public void setvIsdel(Integer vIsdel) {
        this.vIsdel = vIsdel;
    }

    public Integer getcClassid() {
        return cClassid;
    }

    public void setcClassid(Integer cClassid) {
        this.cClassid = cClassid;
    }
}