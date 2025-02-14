package com.MarketingMVP.AllVantage.Entities.Tokens.RefreshToken;

import com.MarketingMVP.AllVantage.Entities.UserEntity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Table
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {
    @SequenceGenerator(
            name = "refresh_seq",
            sequenceName = "refresh_seq",
            allocationSize = 1
    )
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "refresh_seq"
    )
    private long id;
    @Column(nullable = false, unique = false)
    private String refreshToken;
    @Column(nullable = false)
    private Date creationDate;
    @Column(nullable = false)
    private Date expirationDate;
    private boolean expired;
    private boolean revoked;
    @ManyToOne
    private UserEntity userEntity;

}
