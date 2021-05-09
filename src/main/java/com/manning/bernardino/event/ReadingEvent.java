package com.manning.bernardino.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadingEvent {

  @JsonProperty("timestamp")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
  private LocalDateTime timestamp;

  @JsonProperty("tsunami_event_validity")
  private short validity;

  @JsonProperty("tsunami_cause_code")
  private short cause;

  @JsonProperty("earthquake_magnitude")
  private short earthquakeMagnitude;

  @JsonProperty("latitude")
  private short latitude;

  @JsonProperty("longitude")
  private short longitude;

  @JsonProperty("maximum_water_height")
  private double waterHeight;
}
