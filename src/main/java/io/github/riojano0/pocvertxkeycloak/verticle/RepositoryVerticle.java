package io.github.riojano0.pocvertxkeycloak.verticle;

import io.github.riojano0.pocvertxkeycloak.repository.InMemoryRepository;
import io.github.riojano0.pocvertxkeycloak.repository.Repository;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;

public class RepositoryVerticle extends AbstractVerticle {

   public static final String REPOSITORY_ADDRESS = "repository-service";

   private Repository repository;
   private MessageConsumer<JsonObject> binder;

   @Override
   public void init(Vertx vertx, Context context) {
      super.init(vertx, context);
      repository = new InMemoryRepository();
   }

   @Override
   public void start(Promise<Void> startPromise) throws Exception {
      binder = new ServiceBinder(vertx)
            .setAddress(REPOSITORY_ADDRESS)
            .register(Repository.class, repository);

      binder.completionHandler(startPromise);
   }

   @Override
   public void stop(Promise<Void> stopPromise) throws Exception {
      binder.unregister(stopPromise);
   }

}
