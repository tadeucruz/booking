package com.tadeucruz.booking.repository;

import static javax.persistence.LockModeType.PESSIMISTIC_WRITE;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.tadeucruz.booking.model.db.ServiceLock;

public interface ServiceLockRepository extends JpaRepository<ServiceLock, Long> {

    @Lock(PESSIMISTIC_WRITE)
    List<ServiceLock> findByName(String name);

}
