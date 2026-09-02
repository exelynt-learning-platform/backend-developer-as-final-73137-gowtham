package com.exelynt.resource_booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.exelynt.resource_booking.entity.Resource;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

}