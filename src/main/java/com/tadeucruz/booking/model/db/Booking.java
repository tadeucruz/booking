package com.tadeucruz.booking.model.db;

import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Type;

@Data
@Entity
@Table(name = "bookings")
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Booking {

  @Id
  @Type(type = "uuid-char")
  private UUID id;

  @Column(name = "room_id")
  @Type(type = "uuid-char")
  private UUID roomId;

  @Column(name = "user_id")
  @Type(type = "uuid-char")
  private UUID userId;

  @Column(name = "start_date")
  private LocalDateTime startDate;

  @Column(name = "end_date")
  private LocalDateTime endDate;
}
