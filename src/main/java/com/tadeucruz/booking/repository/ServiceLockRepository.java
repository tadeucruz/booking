package com.tadeucruz.booking.repository;

import static javax.persistence.LockModeType.PESSIMISTIC_WRITE;

import com.tadeucruz.booking.model.db.ServiceLock;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface ServiceLockRepository extends JpaRepository<ServiceLock, Long> {

  @Lock(PESSIMISTIC_WRITE)
  List<ServiceLock> findByName(String name);

}
