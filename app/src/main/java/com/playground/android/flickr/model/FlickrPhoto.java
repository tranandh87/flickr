package com.playground.android.flickr.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FlickrPhoto {

    @Expose
    private Integer isfamily;
    @Expose
    private Integer farm;
    @Expose
    private String id;
    @Expose
    private String title;
    @Expose
    private Integer ispublic;
    @SerializedName("url_s")
    @Expose
    private String urlS;
    @Expose
    private String owner;
    @Expose
    private String secret;
    @SerializedName("height_s")
    @Expose
    private String heightS;
    @Expose
    private String server;
    @SerializedName("width_s")
    @Expose
    private String widthS;
    @Expose
    private Integer isfriend;

    /**
     *
     * @return
     * The isfamily
     */
    public Integer getIsfamily() {
        return isfamily;
    }

    /**
     *
     * @param isfamily
     * The isfamily
     */
    public void setIsfamily(Integer isfamily) {
        this.isfamily = isfamily;
    }

    /**
     *
     * @return
     * The farm
     */
    public Integer getFarm() {
        return farm;
    }

    /**
     *
     * @param farm
     * The farm
     */
    public void setFarm(Integer farm) {
        this.farm = farm;
    }

    /**
     *
     * @return
     * The id
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The title
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * @param title
     * The title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *
     * @return
     * The ispublic
     */
    public Integer getIspublic() {
        return ispublic;
    }

    /**
     *
     * @param ispublic
     * The ispublic
     */
    public void setIspublic(Integer ispublic) {
        this.ispublic = ispublic;
    }

    /**
     *
     * @return
     * The urlS
     */
    public String getUrl_s() {
        return urlS;
    }

    /**
     *
     * @param urlS
     * The url_s
     */
    public void setUrl_s(String urlS) {
        this.urlS = urlS;
    }

    /**
     *
     * @return
     * The owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     *
     * @param owner
     * The owner
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     *
     * @return
     * The secret
     */
    public String getSecret() {
        return secret;
    }

    /**
     *
     * @param secret
     * The secret
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
     *
     * @return
     * The heightS
     */
    public String getHeightS() {
        return heightS;
    }

    /**
     *
     * @param heightS
     * The height_s
     */
    public void setHeightS(String heightS) {
        this.heightS = heightS;
    }

    /**
     *
     * @return
     * The server
     */
    public String getServer() {
        return server;
    }

    /**
     *
     * @param server
     * The server
     */
    public void setServer(String server) {
        this.server = server;
    }

    /**
     *
     * @return
     * The widthS
     */
    public String getWidthS() {
        return widthS;
    }

    /**
     *
     * @param widthS
     * The width_s
     */
    public void setWidthS(String widthS) {
        this.widthS = widthS;
    }

    /**
     *
     * @return
     * The isfriend
     */
    public Integer getIsfriend() {
        return isfriend;
    }

    /**
     *
     * @param isfriend
     * The isfriend
     */
    public void setIsfriend(Integer isfriend) {
        this.isfriend = isfriend;
    }

}
