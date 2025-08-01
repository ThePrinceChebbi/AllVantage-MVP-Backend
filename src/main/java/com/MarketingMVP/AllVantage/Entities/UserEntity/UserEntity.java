package com.MarketingMVP.AllVantage.Entities.UserEntity;

import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import com.MarketingMVP.AllVantage.Entities.Role.Role;
import com.MarketingMVP.AllVantage.Entities.Tokens.ConfirmationToken.ConfirmationToken;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@DiscriminatorColumn(name = "type")
@Entity
@Table(name = "users")
public class  UserEntity implements UserDetails {

    @Id
    @GeneratedValue(
            strategy = GenerationType.UUID
    )
    private UUID id;

    @OneToOne
    private FileData image;

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    @NotNull
    @Column(unique = true)
    private String username;

    @NotNull
    @Column(unique = true)
    private String email;

    @NotNull
    private String password;

    @NotNull
    @Column(unique = true)
    private String phoneNumber;

    @ManyToOne
    private Role role;

    @NotNull
    private Date creationDate;

    @NotNull
    private String country;

    @NotNull
    private String state;

    @NotNull
    private String address;

    @NotNull
    private String postalCode;

    @NotNull
    private boolean enabled;

    @NotNull
    private boolean locked;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.getName()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonLocked(){
        return !this.locked;
    }

}
