package com.threego.algo.career.command.domain.repository;

import com.threego.algo.career.command.domain.aggregate.CareerInfoPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareerPostRepository extends JpaRepository<CareerInfoPost, Integer> {
}
