package com.manning.bernardino;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.manning.bernardino.event.ReadingEvent;
import lombok.SneakyThrows;

public class JacksonSerializer {

  private final ObjectMapper objectMapper;

  public JacksonSerializer() {
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  }

  @SneakyThrows
  public String write(ReadingEvent event) {
    return objectMapper.writeValueAsString(event);
  }
}
