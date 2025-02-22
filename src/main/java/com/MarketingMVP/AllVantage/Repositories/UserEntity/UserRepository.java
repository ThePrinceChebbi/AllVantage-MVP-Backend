package com.MarketingMVP.AllVantage.Repositories.UserEntity;


import com.MarketingMVP.AllVantage.Entities.UserEntity.Admin;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Client;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Employee;
import com.MarketingMVP.AllVantage.Entities.UserEntity.UserEntity;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Transactional
@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    @Query(value = "SELECT U FROM UserEntity  U WHERE U.id = :id")
    Optional<UserEntity> fetchUserWithId(@Param("id") UUID id);

    @Query(value = "SELECT U FROM UserEntity U WHERE  U.email = :email ")
    Optional<UserEntity> fetchUserWithEmail(@Param("email") String email);

    @Query(value = "SELECT U FROM UserEntity U WHERE  U.username = :username ")
    Optional<UserEntity> findUserByUsername(@NotNull @Param("username") String username);

    @Query(value = "SELECT E FROM Employee E WHERE  E.email=:email ")
    Optional<Employee> fetchEmployeeWithEmail(@Param("email") String email);

    @Query(value = "SELECT U FROM UserEntity U where U.role.name != 'ADMIN' order by U.id ")
    List<UserEntity> fetchAllUsers(Pageable pageable);

    @Query(value = "SELECT EXISTS(SELECT U FROM UserEntity U WHERE  U.email = :email) AS RESULT")
    Boolean isEmailRegistered(@Param("email") String email);

    @Query(value = "SELECT EXISTS(SELECT U FROM UserEntity U WHERE  U.phoneNumber = :phoneNumber) AS RESULT")
    Boolean isPhoneNumberRegistered(@Param("phoneNumber") String phoneNumber);

    @Query(value = "SELECT COUNT(U) FROM UserEntity U where U.role.name != 'ADMIN'")
    int getTotalUserEntityCount();

    @Query(value = "select p from Employee p where p.id=:userId")
    Optional<Employee> getEmployeeById(@Param("userId") UUID userId);

    @Query(value = "select a from Client a where a.id=:userId")
    Optional<Client> getClientById(@Param("userId") UUID userId);

    @Query(value = "select a from Client a")
    List<Client> findAllAgencies();

    @Query(value = "select p from Employee p where p.role.name!='ADMIN'")
    List<Employee> findAllPeople();

    @Query(value = "SELECT EXISTS(SELECT U FROM UserEntity U WHERE  U.username = :username) AS RESULT")
    Boolean isUsernameRegistered(@Param("username") String username);

    @Query(value = "SELECT a FROM Admin a WHERE a.id = :userId")
    Optional<Admin> getAdminById(@Param("userId") UUID userId);

    @Query(value = "SELECT EXISTS(SELECT U FROM UserEntity U WHERE U.id = :clientId and U.role.name ='CLIENT' ) AS RESULT")
    boolean clientExists(@Param("clientId") UUID clientId);

    @Query(value = "SELECT E FROM Employee E WHERE E.username = :username")
    Optional<Employee> fetchEmployeeWithUsername(String username);
}
