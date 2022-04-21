package io.github.riojano0.pocvertxkeycloak.verticle;

import java.util.ArrayList;
import java.util.List;

import io.github.riojano0.pocvertxkeycloak.repository.Repository;
import io.github.riojano0.pocvertxkeycloak.repository.RepositoryVertxEBProxy;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.auth.oauth2.OAuth2Options;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.OAuth2AuthHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApiServerVerticle extends AbstractVerticle {

   private Repository repositoryProxy;

   @Override
   public void init(Vertx vertx, Context context) {
      super.init(vertx, context);
      repositoryProxy = new RepositoryVertxEBProxy(vertx, RepositoryVerticle.REPOSITORY_ADDRESS);
   }

   @Override
   public void start(Promise<Void> startPromise) throws Exception {
            getOauth2HandlerOptions()
            .compose(this::configureRouter)
            .compose(this::createHttpServer)
            .onComplete( h -> {
               if (h.succeeded()) {
                  log.info("Server Starting");
                  startPromise.complete();
               } else {
                  log.error("Failed to deploy: {}", h.cause().getMessage(), h.cause());
                  startPromise.fail(h.cause());
               }
            });
   }

   private Future<OAuth2AuthHandler> getOauth2HandlerOptions() {
      JsonObject jsonProperties = this.config();

      var oauth2Providers = jsonProperties.getJsonObject("oauth2");
      var propertyValues = oauth2Providers.getJsonObject("keycloak");
      OAuth2Options options = new OAuth2Options(propertyValues);
      options.setFlow(OAuth2FlowType.AUTH_CODE);
      String clientSecret = options.getClientSecret();
      if (clientSecret == null || clientSecret.isBlank()) {
         String sysClientSecret = jsonProperties.getString("keycloak.CLIENT_SECRET");
         options.setClientSecret(sysClientSecret);
      }

      String callbackPath = propertyValues.getString("callbackPath");
      String scopes = propertyValues.getString("scopes");

      List<String> scopeList = new ArrayList<>();
      if (scopes != null && !scopes.isBlank()) {
         scopeList = List.of(scopes.split(","));
      }

      OAuth2Auth oAuth2Auth = OAuth2Auth.create(vertx, options);

      OAuth2AuthHandler oAuth2AuthHandler = OAuth2AuthHandler
            .create(vertx, oAuth2Auth, callbackPath)
            .withScopes(scopeList);

      return Future.succeededFuture(oAuth2AuthHandler);
   }

   private Future<Router> configureRouter(OAuth2AuthHandler oAuth2AuthHandlers) {
      Router router = Router.router(vertx);
      router.route("/hello").handler(r -> r.response().end("Hello World"));
      router.route("/secure/*").handler(oAuth2AuthHandlers);
      router.route("/secure/hello").handler(r -> r.response().end("Hello from a secure world"));

      // Book Endpoints
      router.route("/book*").handler(BodyHandler.create());
      router.route(HttpMethod.GET, "/book/all").handler(this::getAllBooks);
      router.route(HttpMethod.GET, "/book/:id").handler(this::findById);
      router.route(HttpMethod.POST, "/book")
            .handler(oAuth2AuthHandlers)
            .handler(this::addOne);

      return Future.succeededFuture(router);
   }

   private void getAllBooks(RoutingContext routingContext) {
      repositoryProxy.findAll(repositoryHandler -> {
         if (repositoryHandler.succeeded()) {
            List<JsonObject> result = repositoryHandler.result();
            routingContext.response()
                          .putHeader("content-type", "application/json; charset=utf-8")
                          .setStatusCode(200)
                          .end(Json.encodePrettily(result));
         } else {
            handleFail(routingContext, "Not found books");
         }
      });
   }

   private void findById(RoutingContext routingContext) {
      String id = routingContext.request()
                                .getParam("id");
      Long idValue = Long.valueOf(id);

      repositoryProxy.findById(idValue, repositoryHandler -> {
         if (repositoryHandler.succeeded()) {
            routingContext.response()
                          .putHeader("content-type", "application/json; charset=utf-8")
                          .setStatusCode(200)
                          .end(Json.encodePrettily(repositoryHandler.result()));
         } else {
            handleFail(routingContext, "Not found book with id: " + idValue);
         }
      });
   }

   private void addOne(RoutingContext routingContext) {
      JsonObject bodyAsJson = routingContext.getBodyAsJson();
      if (bodyAsJson != null) {
         repositoryProxy.save(bodyAsJson, repositoryHandler -> {
            if (repositoryHandler.succeeded()) {
               JsonObject result = repositoryHandler.result();
               routingContext.response()
                             .putHeader("content-type", "application/json; charset=utf-8")
                             .setStatusCode(200)
                             .end(Json.encodePrettily(result));
            } else {
               handleFail(routingContext, "Unable to save");
            }
         });
      } else {
         handleFail(routingContext, "Not body found");
      }
   }

   private void handleFail(RoutingContext routingContext, String message) {
      routingContext
            .response()
            .setStatusCode(400)
            .end(message);
   }

   private Future<HttpServer> createHttpServer(Router router) {
      HttpServer httpServer = vertx.createHttpServer()
                                   .requestHandler(router);
      JsonObject jsonProperties = this.config();
      Integer port = jsonProperties.getJsonObject("api")
                                   .getInteger("port");
      log.info("Setup port: {}", port);
      return Future.future(promise -> httpServer.listen(port, promise));
   }
}
