package com.project.shopapp.models;

import jakarta.persistence.*;
import lombok.*;

@Table(name="roles")
@Entity
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    public Role(String name) {
        this.name = name;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name",length = 100)
    private String name;

    public static String ADMIN = "ADMIN";
    public static String USER = "USER";


}
