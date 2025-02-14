package com.MarketingMVP.AllVantage.Entities.Tokens.AccessToken;

import com.MarketingMVP.AllVantage.Entities.UserEntity.UserEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Getter
@Entity
@Table
public class Token {

    @SequenceGenerator(
            name = "token_seq",
            sequenceName = "token_seq",
            allocationSize = 1
    )
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "token_seq"
    )
    private long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    private TokenType tokenType =TokenType.BEARER;

    private boolean expired;

    private boolean revoked;

    @ManyToOne
    private UserEntity userEntity;

    public Token(String token, TokenType tokenType, boolean expired, boolean revoked, UserEntity userEntity) {
        this.token = token;
        this.tokenType = tokenType;
        this.expired = expired;
        this.revoked = revoked;
        this.userEntity = userEntity;
    }
}
