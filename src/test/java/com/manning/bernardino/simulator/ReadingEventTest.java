package com.manning.bernardino.simulator;

import com.manning.bernardino.JacksonSerializer;
import com.manning.bernardino.event.ReadingEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReadingEventTest {

  private JacksonSerializer serializer = new JacksonSerializer();

  @Test
  public void dateFormat() {
    ReadingEvent event = ReadingEvent.builder()
      .timestamp(LocalDateTime.of(2019, 1, 23, 13, 45, 34)).build();

    String eventAsString = serializer.write(event);

    assertTrue(eventAsString.contains("\"timestamp\":\"23-01-2019 13:45:34\""));
  }
}