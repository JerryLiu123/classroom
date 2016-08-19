package com.classroom.wnn.model;

public class BiVideoInfo {
    private Integer id;

    private String vName;

    private String vHdfsfile;

    private String vFile;

    private Integer vAvailable;

    private Integer vIsdel;

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

    public String getvHdfsfile() {
        return vHdfsfile;
    }

    public void setvHdfsfile(String vHdfsfile) {
        this.vHdfsfile = vHdfsfile == null ? null : vHdfsfile.trim();
    }

    public String getvFile() {
        return vFile;
    }

    public void setvFile(String vFile) {
        this.vFile = vFile == null ? null : vFile.trim();
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
}