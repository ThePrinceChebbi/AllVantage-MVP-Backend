package com.MarketingMVP.AllVantage.Entities.UserEntity;

import com.MarketingMVP.AllVantage.Entities.Suit.Suit;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@DiscriminatorValue("Client")
@Data
public class Client extends UserEntity{

    @OneToMany(mappedBy = "client")
    private List<Suit> suits;
}
