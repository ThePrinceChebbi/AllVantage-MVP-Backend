package com.MarketingMVP.AllVantage.Entities.Tokens.ConfirmationToken;

import com.MarketingMVP.AllVantage.Entities.UserEntity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Table
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmationToken {
    @SequenceGenerator(
            name = "confirmation_seq",
            sequenceName = "confirmation_seq",
            allocationSize = 1
    )
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "confirmation_seq"
    )
    private long id;
    @Column(nullable = false, unique = true)
    private String confirmationToken;
    private LocalDateTime creationDate;
    private LocalDateTime expirationDate;
    private LocalDateTime confirmationDate;
    @ManyToOne
    private UserEntity userEntity;
    public ConfirmationToken(String confirmationToken, LocalDateTime creationDate, LocalDateTime expirationDate, LocalDateTime confirmationDate, UserEntity userEntity) {
        this.confirmationToken = confirmationToken;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
        this.confirmationDate = confirmationDate;
        this.userEntity = userEntity;
    }

    public ConfirmationToken(String confirmationToken, LocalDateTime creationDate, LocalDateTime expirationDate, UserEntity userEntity) {
        this.confirmationToken = confirmationToken;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
        this.userEntity = userEntity;
    }
}
