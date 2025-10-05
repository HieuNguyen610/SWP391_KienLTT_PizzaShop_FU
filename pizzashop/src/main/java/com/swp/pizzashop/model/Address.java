package com.swp.pizzashop.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
public class Address extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Size(max = 255)
    @Column(name = "address_line", length = 255, nullable = false)
    private String addressLine; // composed from number + street

    @Size(max = 100)
    @Column(name = "city", length = 100)
    private String city; // maps from UI Province/City

    @Size(max = 100)
    @Column(name = "district", length = 100)
    private String district;

    @Size(max = 20)
    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "is_default")
    private boolean defaultAddress;

    @Transient
    public String getFullAddress() {
        String cityPart = (city == null || city.isBlank()) ? "" : ", " + city;
        String districtPart = (district == null || district.isBlank()) ? "" : ", " + district;
        return addressLine + districtPart + cityPart;
    }
}
