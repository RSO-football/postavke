package rso.football.postavke.lib;

import java.time.Instant;

public class PostavkeMetadata {

    private Integer postavkaId;
    private Integer uporabnikID;
    private float pay;


    public Integer getPostavkaId() {
        return postavkaId;
    }

    public void setPostavkaId(Integer postavkaId) {
        this.postavkaId = postavkaId;
    }

    public Integer getUporabnikID() {
        return uporabnikID;
    }

    public void setUporabnikID(Integer uporabnikID) {
        this.uporabnikID = uporabnikID;
    }

    public float getPay() {
        return pay;
    }

    public void setPay(float pay) {
        this.pay = pay;
    }
}
