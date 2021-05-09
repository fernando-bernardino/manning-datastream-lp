package com.manning.bernardino.event;

import lombok.Getter;

public enum EventValidity {
  MINOR(0),
  VERY_DOUBTFUL(1),
  QUESTIONABLE(2),
  PROBABLE(3),
  DEFINITE(4);

  @Getter
  private final short code;

  EventValidity(int code) {
    this.code = (short) code;
  }
}
