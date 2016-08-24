package com.classroom.wnn.model;

public class BiZoneInfo {
    private Integer id;

    private Integer vFileid;

    private String zHdfsfile;

    private String zFile;

    private Integer zAvailable;

    private Integer zIsdel;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getvFileid() {
        return vFileid;
    }

    public void setvFileid(Integer vFileid) {
        this.vFileid = vFileid;
    }

    public String getzHdfsfile() {
        return zHdfsfile;
    }

    public void setzHdfsfile(String zHdfsfile) {
        this.zHdfsfile = zHdfsfile == null ? null : zHdfsfile.trim();
    }

    public String getzFile() {
        return zFile;
    }

    public void setzFile(String zFile) {
        this.zFile = zFile == null ? null : zFile.trim();
    }

    public Integer getzAvailable() {
        return zAvailable;
    }

    public void setzAvailable(Integer zAvailable) {
        this.zAvailable = zAvailable;
    }

    public Integer getzIsdel() {
        return zIsdel;
    }

    public void setzIsdel(Integer zIsdel) {
        this.zIsdel = zIsdel;
    }
}