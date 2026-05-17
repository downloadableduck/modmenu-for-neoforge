package com.terraformersmc.modmenu.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public final class EnumToLowerCaseJsonConverter implements JsonSerializer<Enum<?>>, JsonDeserializer<Enum<?>> {
   private static final Map<String, Class<? extends Enum<?>>> TYPE_CACHE = new HashMap();

   public JsonElement serialize(Enum<?> src, Type typeOfSrc, JsonSerializationContext context) {
      return src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.name().toLowerCase());
   }

   public Enum<?> deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
      if (json != null && !json.isJsonNull()) {
         if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
            try {
               String enumClassName = type.getTypeName();
               Class<? extends Enum<?>> enumClass = (Class)TYPE_CACHE.get(enumClassName);
               if (enumClass == null) {
                  enumClass = (Class<? extends Enum<?>>) Class.forName(enumClassName);
                  TYPE_CACHE.put(enumClassName, enumClass);
               }

               return Enum.valueOf((Class) enumClass, json.getAsString().toUpperCase());
            } catch (ClassNotFoundException var6) {
               throw new JsonParseException(var6);
            }
         } else {
            throw new JsonParseException("Expecting a String JsonPrimitive, getting " + String.valueOf(json));
         }
      } else {
         return null;
      }
   }
}
