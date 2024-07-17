package com.iapex.repositories.institution;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iapex.models.institution.Direction;

@Repository
public interface DirectionRepository extends JpaRepository<Direction, Long> {
}