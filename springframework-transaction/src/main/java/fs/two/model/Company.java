package fs.two.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class Company {

    @Id
    private Integer id;

    private String name;
}
