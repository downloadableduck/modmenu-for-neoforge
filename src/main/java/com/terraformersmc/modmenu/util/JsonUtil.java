package com.terraformersmc.modmenu.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.Optional;

public class JsonUtil {
   private JsonUtil() {
   }

   public static Optional<String> getString(JsonObject parent, String field) {
      if (!parent.has(field)) {
         return Optional.empty();
      } else {
         JsonElement value = parent.get(field);
         return value.isJsonPrimitive() && ((JsonPrimitive)value).isString() ? Optional.of(value.getAsString()) : Optional.empty();
      }
   }

   public static Optional<Boolean> getBoolean(JsonObject parent, String field) {
      if (!parent.has(field)) {
         return Optional.empty();
      } else {
         JsonElement value = parent.get(field);
         return value.isJsonPrimitive() && ((JsonPrimitive)value).isBoolean() ? Optional.of(value.getAsBoolean()) : Optional.empty();
      }
   }
}
