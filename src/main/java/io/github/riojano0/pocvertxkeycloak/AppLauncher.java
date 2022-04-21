package io.github.riojano0.pocvertxkeycloak;

import io.vertx.core.Launcher;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppLauncher extends Launcher {

   public static void main(String[] args) {
      new AppLauncher().dispatch(args);
   }

}
