package com.MarketingMVP.AllVantage.DTOs.Suit;

import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import org.springframework.lang.Nullable;

@Getter
public class SuitCreationDTO {

    @NotNull
    private String name;

    @Nullable
    private String description;

}
