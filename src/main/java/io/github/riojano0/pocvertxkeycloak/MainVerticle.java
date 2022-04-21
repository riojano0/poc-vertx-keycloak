package io.github.riojano0.pocvertxkeycloak;

import io.github.riojano0.pocvertxkeycloak.verticle.ApiServerVerticle;
import io.github.riojano0.pocvertxkeycloak.verticle.RepositoryVerticle;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainVerticle extends AbstractVerticle {

   public static void main(String[] args) {
      Vertx.vertx().deployVerticle(new MainVerticle());
   }

   @Override
   public void start(Promise<Void> startPromise) throws Exception {
      getProperties()
            .onComplete(confHandler -> {
               if (confHandler.succeeded()) {
                  DeploymentOptions deploymentOptions = new DeploymentOptions()
                        .setConfig(confHandler.result());

                  Future<String> apiVerticle = vertx.deployVerticle(new ApiServerVerticle(), deploymentOptions);
                  Future<String> repositoryVerticle = vertx.deployVerticle(new RepositoryVerticle(), deploymentOptions);

                  CompositeFuture.all(apiVerticle, repositoryVerticle)
                                 .onSuccess(h -> {
                                    log.info("Deployed all verticles");
                                    startPromise.complete();
                                 });
               } else {
                  log.error("Fail to deploy: {}", confHandler.cause().getMessage());
                  startPromise.fail(confHandler.cause());
               }
            });
   }

   private Future<JsonObject> getProperties() {
      ConfigStoreOptions systemPropertiesStore = new ConfigStoreOptions()
            .setType("sys");
      ConfigStoreOptions envPropertiesStore = new ConfigStoreOptions()
            .setType("env");
      ConfigStoreOptions fileStoreOptions = new ConfigStoreOptions()
            .setType("file")
            .setFormat("yaml")
            .setConfig(new JsonObject().put("path", "config.yaml"));

      ConfigRetrieverOptions configRetrieverOptions = new ConfigRetrieverOptions()
            .setIncludeDefaultStores(true)
            .addStore(fileStoreOptions)
            .addStore(systemPropertiesStore)
            .addStore(envPropertiesStore);

      ConfigRetriever retriever = ConfigRetriever.create(vertx, configRetrieverOptions);

      JsonObject bareConfig = this.config();
      Future<JsonObject> config = retriever.getConfig();
      return config.compose(conf -> {
         if (bareConfig != null) {
            return Future.succeededFuture(conf.mergeIn(bareConfig));
         } else {
            return Future.succeededFuture(conf);
         }
      });
   }

}
