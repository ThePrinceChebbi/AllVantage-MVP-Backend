package com.MarketingMVP.AllVantage.Entities.FileData;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
public class FileData {

    @Id
    @SequenceGenerator(
        name = "file-sequence",
        sequenceName = "file-sequence",
            allocationSize = 1

    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
        generator = "file-sequence"
    )
    private Long id;

    @NotNull
    private String path;

    @NotNull
    private String type;

    @NotNull
    private String prefix;

    public FileData(String path, String type, String prefix) {
        this.path = path;
        this.type = type;
        this.prefix = prefix;
    }
}
