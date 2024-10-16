package com.project.shopapp.models;

import jakarta.persistence.*;
import lombok.*;

@Table(name="social_accounts")
@Entity
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SocialAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider",nullable = false,length = 100)
    private String provider;

    @Column(name = "provider_id",nullable = false,length = 100)
    private String providerId;

    @Column(name = "name",length = 100)
    private String name;

    @Column(name = "email",length = 100)
    private String email;
}
