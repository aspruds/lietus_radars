package com.spruds.raincheck.model;

import java.util.Date;

public class WeatherImage {
    private Date dateFetched;
    private byte[] image;

    /**
     * @return the dateFetched
     */
    public Date getDateFetched() {
        return dateFetched;
    }

    /**
     * @param dateFetched the dateFetched to set
     */
    public void setDateFetched(Date dateFetched) {
        this.dateFetched = dateFetched;
    }

    /**
     * @return the image
     */
    public byte[] getImage() {
        return image;
    }

    /**
     * @param image the image to set
     */
    public void setImage(byte[] image) {
        this.image = image;
    }
}
