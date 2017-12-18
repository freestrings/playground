package wmp.tnc.product;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@EqualsAndHashCode
public class Product {

    private Integer id;

    private String name;

    private List<String> images;
}
