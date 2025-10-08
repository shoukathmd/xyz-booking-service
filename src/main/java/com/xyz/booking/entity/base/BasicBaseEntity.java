package com.xyz.booking.entity.base;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;


import java.io.Serializable;


/**
 * @author Mohammed Shoukath Ali
 */

@MappedSuperclass
@Data
@EqualsAndHashCode(of = {"id"})
public class BasicBaseEntity implements Serializable{
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
