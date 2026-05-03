package com.terraformersmc.modmenu.util.mod.neoforge;

import com.mojang.blaze3d.platform.NativeImage;
import java.awt.Dimension;
import java.io.Closeable;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.Tuple;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforgespi.locating.IModFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NeoforgeIconHandler implements Closeable {
   private static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu | NeoforgeIconHandler");
   private final Map<String, Tuple<DynamicTexture, Dimension>> modIconCache = new HashMap();
   public static final Map<String, Tuple<DynamicTexture, Dimension>> modResourceIconCache = new HashMap();

   public Tuple<DynamicTexture, Dimension> createIcon(ModContainer iconSource, String iconPath) {
      String var10000 = iconSource.getModId();
      String cacheKey = var10000 + "|" + iconPath;
      Tuple<DynamicTexture, Dimension> cached = (Tuple)this.modIconCache.get(cacheKey);
      if (cached != null) {
         return cached;
      } else {
         try {
            Path path = this.findIconPath(iconSource, iconPath);
            if (path != null && Files.exists(path, new LinkOption[0])) {
               InputStream inputStream = Files.newInputStream(path);

               Tuple var12;
               try {
                  NativeImage image = NativeImage.read((InputStream)Objects.requireNonNull(inputStream));
                  String pathStr = path.toString();
                  DynamicTexture tex = new DynamicTexture(() -> {
                     return pathStr;
                  }, image);
                  Dimension dim = new Dimension(image.getWidth(), image.getHeight());
                  Tuple<DynamicTexture, Dimension> result = new Tuple(tex, dim);
                  this.modIconCache.put(cacheKey, result);
                  var12 = result;
               } catch (Throwable var14) {
                  if (inputStream != null) {
                     try {
                        inputStream.close();
                     } catch (Throwable var13) {
                        var14.addSuppressed(var13);
                     }
                  }

                  throw var14;
               }

               if (inputStream != null) {
                  inputStream.close();
               }

               return var12;
            } else {
               return null;
            }
         } catch (Throwable var15) {
            LOGGER.warn("Failed to load icon for mod {}: {}", iconSource.getModId(), var15.getMessage());
            return null;
         }
      }
   }

   private Path findIconPath(ModContainer iconSource, String iconPath) {
      try {
         IModFile modFile = iconSource.getModInfo().getOwningFile().getFile();

         Method getSecureJar;
         Object secureJar;
         Path iconInDir;
         Path resourcesDir;
         try {
            getSecureJar = modFile.getClass().getMethod("getFilePath");
            secureJar = getSecureJar.invoke(modFile);
            if (secureJar instanceof Path) {
               Path jarPath = (Path)secureJar;
               if (Files.isRegularFile(jarPath, new LinkOption[0]) && jarPath.toString().endsWith(".jar")) {
                  try {
                     URI jarUri = URI.create("jar:" + String.valueOf(jarPath.toUri()));

                     FileSystem fs;
                     try {
                        fs = FileSystems.getFileSystem(jarUri);
                     } catch (FileSystemNotFoundException var17) {
                        fs = FileSystems.newFileSystem(jarUri, Collections.emptyMap());
                     }

                     resourcesDir = fs.getPath(iconPath);
                     if (Files.exists(resourcesDir, new LinkOption[0])) {
                        return resourcesDir;
                     }
                  } catch (Throwable var18) {
                     LOGGER.warn("findIconPath: JAR filesystem failed: {}", var18.getMessage());
                  }
               } else if (Files.isDirectory(jarPath, new LinkOption[0])) {
                  iconInDir = jarPath.resolve(iconPath);
                  if (Files.exists(iconInDir, new LinkOption[0])) {
                     return iconInDir;
                  }

                  Path parent = jarPath.getParent();
                  if (parent != null && parent.getFileName() != null && parent.getFileName().toString().equals("java")) {
                     resourcesDir = parent.getParent().resolve("resources").resolve("main");
                     if (Files.isDirectory(resourcesDir, new LinkOption[0])) {
                        Path iconInResources = resourcesDir.resolve(iconPath);
                        if (Files.exists(iconInResources, new LinkOption[0])) {
                           return iconInResources;
                        }
                     }
                  }

                  String pathStr = jarPath.toString().replace("\\", "/");
                  if (pathStr.contains("/build/classes/java/main")) {
                     String resourcePath = pathStr.replace("/build/classes/java/main", "/build/resources/main");
                     resourcesDir = Path.of(resourcePath);
                     if (Files.isDirectory(resourcesDir, new LinkOption[0])) {
                        Path iconInResources = resourcesDir.resolve(iconPath);
                        if (Files.exists(iconInResources, new LinkOption[0])) {
                           return iconInResources;
                        }
                     }
                  }
               }
            }
         } catch (NoSuchMethodException var19) {
         } catch (Throwable var20) {
            LOGGER.warn("findIconPath: getFilePath approach failed: {}", var20.getMessage());
         }

         try {
            getSecureJar = modFile.getClass().getMethod("findResource", String[].class);
            String[] parts = iconPath.split("/");
            Object result = getSecureJar.invoke(modFile, parts);
            if (result instanceof Path) {
               iconInDir = (Path)result;
               if (Files.exists(iconInDir, new LinkOption[0])) {
                  return iconInDir;
               }
            }
         } catch (NoSuchMethodException var15) {
         } catch (Throwable var16) {
            LOGGER.warn("findIconPath: findResource(String[]) failed: {}", var16.getMessage());
         }

         try {
            getSecureJar = modFile.getClass().getMethod("getSecureJar");
            secureJar = getSecureJar.invoke(modFile);
            if (secureJar != null) {
               Method getPath = secureJar.getClass().getMethod("getPath", String.class, String[].class);
               String[] parts = iconPath.split("/");
               String first = parts.length > 0 ? parts[0] : "";
               String[] rest = parts.length > 1 ? (String[])Arrays.copyOfRange(parts, 1, parts.length) : new String[0];
               Object result = getPath.invoke(secureJar, first, rest);
               if (result instanceof Path) {
                  resourcesDir = (Path)result;
                  if (Files.exists(resourcesDir, new LinkOption[0])) {
                     return resourcesDir;
                  }
               }
            }
         } catch (NoSuchMethodException var13) {
         } catch (Throwable var14) {
            LOGGER.warn("findIconPath: getSecureJar approach failed: {}", var14.getMessage());
         }
      } catch (Throwable var21) {
         LOGGER.warn("findIconPath: Error finding icon path for mod {}: {}", iconSource.getModId(), var21.getMessage());
      }

      return null;
   }

   public void close() {
      Iterator var1 = this.modIconCache.values().iterator();

      while(var1.hasNext()) {
         Tuple tex = (Tuple)var1.next();

         try {
            ((DynamicTexture)tex.getA()).close();
         } catch (Throwable var4) {
         }
      }

      this.modIconCache.clear();
   }

   public Tuple<DynamicTexture, Dimension> getCachedModIcon(String key) {
      return (Tuple)this.modIconCache.get(key);
   }
}
