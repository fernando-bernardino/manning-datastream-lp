package com.manning.bernardino.event;

import lombok.Getter;

public enum CauseCode {
  UNKNOWN(0),
  EARTHQUAKE(1),
  QUESTIONABLE_EARTHQUAKE(2),
  EARTHQUAKE_LANDSLIDE(3),
  VOLCANO_EARTHQUAKE(4),
  VOLCANO_EARTHQUAKE_LANDSLIDE(5),
  VOLCANO(6),
  VOLCANO_LANDSLIDE(7),
  LANDSLIDE(8),
  METEOROLOGICAL(9),
  EXPLOSION(10),
  ASTRONOMICAL_TIDE(11);

  @Getter
  private final short code;

  CauseCode(int code) {
    this.code = (short) code;
  }
}
