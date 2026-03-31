package be.valuya.hibernate.param.detached.repro;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "trustee")
public class Trustee {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trustee_seq")
    @SequenceGenerator(name = "trustee_seq", sequenceName = "trustee_seq", allocationSize = 1)
    private Long id;

    @Version
    private Long version;

    private String name;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
