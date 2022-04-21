package io.github.riojano0.pocvertxkeycloak.repository;


import java.util.List;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

@ProxyGen
public interface Repository {

   void findById(Long id, Handler<AsyncResult<JsonObject>> resultHandler);

   void findAll(Handler<AsyncResult<List<JsonObject>>> resultHandler);

   void save(JsonObject jsonObject, Handler<AsyncResult<JsonObject>> resultHandler);

}
