package com.MarketingMVP.AllVantage.Entities.UserEntity;

import com.MarketingMVP.AllVantage.Entities.Suit.Suit;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@DiscriminatorValue("Employee")
public class Employee extends UserEntity{

    @ManyToMany(mappedBy = "employees")
    private List<Suit> suits = new ArrayList<>();

}
