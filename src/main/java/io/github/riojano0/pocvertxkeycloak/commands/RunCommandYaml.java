package io.github.riojano0.pocvertxkeycloak.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import io.vertx.core.cli.annotations.Name;
import io.vertx.core.cli.annotations.Summary;
import io.vertx.core.impl.launcher.commands.RunCommand;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.launcher.DefaultCommandFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

@Name("run")
@Summary("Extends RunCommand and give support to YAML files")
public class RunCommandYaml extends RunCommand {

   private static final ObjectMapper YAML_MAPPER = new YAMLMapper();

   @Override
   protected JsonObject getJsonFromFileOrString(String jsonFileOrString, String argName) {
      JsonObject conf;
      if (jsonFileOrString != null) {
         File source = new File(jsonFileOrString);
         try (Scanner scanner = new Scanner(source, "UTF-8").useDelimiter("\\A")) {

            String sconf = scanner.next();
            String extension = getExtension(source);

            if ("yaml".equals(extension)) {
               try {
                  JsonNode jsonNode = YAML_MAPPER.readTree(sconf);
                  conf = new JsonObject(jsonNode.toString());
               } catch (JsonProcessingException e) {
                  log.error("Configuration file " + sconf + " does not contain a valid YAML object");
                  return null;
               }
            } else {
               try {
                  conf = new JsonObject(sconf);
               } catch (DecodeException e) {
                  log.error("Configuration file " + sconf + " does not contain a valid JSON object");
                  return null;
               }
            }
         } catch (FileNotFoundException e) {
            try {
               conf = new JsonObject(jsonFileOrString);
            } catch (DecodeException e2) {
               // The configuration is not printed for security purpose, it can contain sensitive data.
               log.error("The -" + argName + " argument does not point to an existing file or is not a valid JSON object", e2);
               return null;
            }
         }
      } else {
         conf = null;
      }
      return conf;
   }

   private static String getExtension(File source) {
      String sourceName = source.getName();
      int index = sourceName.lastIndexOf(".");
      String substring = sourceName.substring(index + 1);

      if ("yml".equals(substring) || "yaml".equals(substring)) {
         return "yaml";
      } else {
         return substring.toLowerCase();
      }
   }

   public static class RunCommandYamlFactory extends DefaultCommandFactory<RunCommandYaml> {

      public RunCommandYamlFactory() {
         super(RunCommandYaml.class, RunCommandYaml::new);
      }
   }
}
