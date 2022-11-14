package com.tadeucruz.booking.model.db;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "services_locks")
public class ServiceLock {

  @Id
  @GeneratedValue
  private Long id;

  private String name;

  @Version
  private Long version;
}
