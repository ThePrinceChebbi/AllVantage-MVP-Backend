package com.MarketingMVP.AllVantage.Entities.Postable;

import com.MarketingMVP.AllVantage.Entities.UserEntity.Employee;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
public class Postable {
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "post_sequence"
    )
    @SequenceGenerator(
            name = "post_sequence",
            sequenceName = "post_sequence",
            allocationSize = 1
    )
    @Id
    private Long id;

    @NotNull
    private String title;

    @NotNull
    private String content;

    @NotNull
    private Date createdAt;

    @NotNull
    private Date scheduledToPostAt;

    @NotNull
    private Date lastEditedAt;

    @ManyToOne
    private Employee employee;

    public Postable(
            String title,
            String content,
            Date createdAt,
            Date scheduledToPostAt,
            Date lastEditedAt,
            Employee employee
    ) {
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.scheduledToPostAt = scheduledToPostAt;
        this.lastEditedAt = lastEditedAt;
        this.employee = employee;
    }
}
