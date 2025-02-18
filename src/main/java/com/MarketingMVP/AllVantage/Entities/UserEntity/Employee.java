package com.MarketingMVP.AllVantage.Entities.UserEntity;

import com.MarketingMVP.AllVantage.Entities.Suit.Suit;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@DiscriminatorValue("Employee")
public class Employee extends UserEntity{

    @ManyToMany
    @JoinTable(
            name = "employee_suits",
            joinColumns = @JoinColumn(name = "employee_id"),
            inverseJoinColumns = @JoinColumn(name = "suit_id")
    )
    private List<Suit> suits;

}
