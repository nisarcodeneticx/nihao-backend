package com.codeneticx.nihaobackend.repository;

import com.codeneticx.nihaobackend.model.UserUnitProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserUnitProgressRepository extends JpaRepository<UserUnitProgress, String> {
    Optional<UserUnitProgress> findByUserIdAndUnitId(String userId, String unitId);

    List<UserUnitProgress> findByUserIdAndUnitIdIn(String userId, Collection<String> unitIds);

    void deleteByUnit_Id(String unitId);
}
