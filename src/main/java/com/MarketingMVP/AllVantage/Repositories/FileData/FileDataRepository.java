package com.MarketingMVP.AllVantage.Repositories.FileData;

import com.MarketingMVP.AllVantage.Entities.FileData.FileData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Transactional
@Repository
public interface FileDataRepository extends JpaRepository<FileData,Long> {

    @Query(value = "select fd from FileData fd where fd.id = :id")
    Optional<FileData> fetchFileDataById(long id);

    @Transactional
    @Modifying
    @Query(value = "delete from FileData f where f.id = :id")
    void deleteFileDataById(final long id);

    @Transactional
    @Modifying
    @Query(value = "delete from FileData f where f in :files")
    void deleteAllFiles(@Param("files") List<FileData> files);
}
