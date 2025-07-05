package com.MarketingMVP.AllVantage.Repositories.Post;

import com.MarketingMVP.AllVantage.Entities.Postable.Postable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.UUID;

public interface PostableRepository extends JpaRepository<Postable, Long> {

    @Query("SELECT count(p) FROM Postable p WHERE TYPE(p) = Reel AND CAST(p.createdAt AS date) = CAST(:queryDate AS date)")
    int getAllReelCountPerDay(@Param("queryDate") Date queryDate);

    @Query("SELECT count(p) FROM Postable p WHERE TYPE(p) = Post AND CAST(p.createdAt AS date) = CAST(:queryDate AS date)")
    int getAllPostCountPerDay(@Param("queryDate") Date queryDate);

    @Query("SELECT count(p) FROM Postable p WHERE TYPE(p) = Reel AND p.employee.id =:employeeId AND CAST(p.createdAt AS date) = CAST(:queryDate AS date)")
    int getEmployeeReelCountPerDay(@Param("queryDate") Date queryDate, @Param("employeeId") UUID employeeId);

    @Query("SELECT count(p) FROM Postable p WHERE TYPE(p) = Post AND p.employee.id =:employeeId AND CAST(p.createdAt AS date) = CAST(:queryDate AS date)")
    int getEmployeePostCountPerDay(@Param("queryDate") Date queryDate, @Param("employeeId") UUID employeeId);
}
