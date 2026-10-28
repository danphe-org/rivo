package com.danphe.rivo.common.helper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Jsons {

  private static final Gson gson = new GsonBuilder()
      .disableHtmlEscaping()
      .serializeNulls()
      .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
      .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
      .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
      .create();

  private Jsons() {}

  private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(formatter.format(src));
    }

    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      return LocalDateTime.parse(json.getAsString(), formatter);
    }
  }

  private static class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(formatter.format(src));
    }

    @Override
    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      return LocalDate.parse(json.getAsString(), formatter);
    }
  }

  private static class LocalTimeAdapter implements JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_TIME;

    @Override
    public JsonElement serialize(LocalTime src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(formatter.format(src));
    }

    @Override
    public LocalTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      return LocalTime.parse(json.getAsString(), formatter);
    }
  }

  public static boolean isValidJsonString(String json) {
    if (json == null || json.isBlank()) {
      return false;
    }
    try {
      JsonParser.parseString(json);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public static <T> String toJsonObj(T obj) {
    return gson.toJson(obj);
  }

  public static <T> String toJsonList(List<T> objCol) {
    return gson.toJson(objCol);
  }

  public static <T> T fromJsonToObj(String jsonString, Class<T> obj) {
    if (!isValidJsonString(jsonString)) {
      throw new IllegalArgumentException("Invalid Json data.");
    }
    return gson.fromJson(jsonString, obj);
  }

  public static <T> T fromJsonToObj(String jsonString, Type type) {
    if (!isValidJsonString(jsonString)) {
      throw new IllegalArgumentException("Invalid Json data.");
    }
    return gson.fromJson(jsonString, type);
  }

  public static <T> List<T> fromJsonToList(String jsonString, Type t) {
    if (!isValidJsonString(jsonString)) {
      return Collections.emptyList();
    }
    try {
      return Arrays.asList(gson.fromJson(jsonString, t));
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }

  public static String toJsonTree(Object src) {
    return gson.toJsonTree(src).toString();
  }

  public static JsonObject toJsonObject(String jsonData) {
    return JsonParser.parseString(jsonData).getAsJsonObject();
  }
}
