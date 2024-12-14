package com.project.shopapp.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.project.shopapp.untils.GrantedAuthorityDeserializer;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Table(name="users")
@Entity

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class User extends BaseEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fullname",length = 100)
    private String fullName;

    @Column(name = "phone_number",length = 10,nullable = false)
    private String phoneNumber;

    @Column(name = "email", length = 255, nullable = false)
    private String email;

    @Column(name = "address",length = 200)
    private String address;

    @Column(name = "password",length = 200, nullable = false)
    private String password;

    @Column(name="is_active")
    private boolean active;

    @Column(name="date_of_birth")
    private Date dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name="auth_provider")
    private Provider provider;

    @ManyToOne
    @JoinColumn(name="role_id")
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Rating> ratings = new ArrayList<>();

    private boolean enabled;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonBackReference("user-tokens")
    private List<Token> tokens = new ArrayList<>();


    @Override
    @JsonDeserialize(contentUsing = GrantedAuthorityDeserializer.class)
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorityList = new ArrayList<>();
        authorityList.add(new SimpleGrantedAuthority("ROLE_"+getRole().getName().toUpperCase()));
        return authorityList;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }


}
