package rso.football.postavke.lib;

public class PlaceMetadata {

    private Integer uporabnikID;
    private double salary;

    public PlaceMetadata(Integer uporabnikID, double salary) {
        this.uporabnikID = uporabnikID;
        this.salary = salary;
    }

    public Integer getUporabnikID() {
        return uporabnikID;
    }

    public void setUporabnikID(Integer uporabnikID) {
        this.uporabnikID = uporabnikID;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(float salary) {
        this.salary = salary;
    }
}
