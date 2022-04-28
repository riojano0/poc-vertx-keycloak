package io.github.riojano0.pocvertxkeycloak;

import io.github.riojano0.pocvertxkeycloak.commands.RunCommandYaml;
import io.vertx.core.Launcher;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppLauncher extends Launcher {

   @Override
   protected void load() {
      super.load();
      this.commandByName.put("run", new CommandRegistration(new RunCommandYaml.RunCommandYamlFactory()));
   }

   public static void main(String[] args) {
      new AppLauncher().dispatch(args);
   }

}
