package rso.football.postavke.models.entities;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "postavke_metadata")
@NamedQueries(value =
        {
                @NamedQuery(name = "PostavkeMetadataEntity.getAll",
                        query = "SELECT im FROM PostavkeMetadataEntity im"),
                @NamedQuery(name = "PostavkeMetadataEntity.getAllTrenerId",
                        query = "SELECT p FROM PostavkeMetadataEntity p WHERE p.uporabnikID = ?1")
        })
public class PostavkeMetadataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "uporabnikID")
    private Integer uporabnikID;

    @Column(name = "pay")
    private float pay;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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