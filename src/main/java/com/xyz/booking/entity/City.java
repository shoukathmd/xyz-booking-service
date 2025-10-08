package com.xyz.booking.entity;

import com.xyz.booking.entity.base.AuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class City extends AuditEntity {


    @Column(nullable = false, unique = true)
    private String name;

    // One city can have many theatres
    @OneToMany(mappedBy = "city", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Theatre> theatres;
}
