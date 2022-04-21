package io.github.riojano0.pocvertxkeycloak.repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.riojano0.pocvertxkeycloak.model.Book;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public class InMemoryRepository implements Repository {

   private long initialId = 1L;
   private final Map<Long, Book> store = Stream.of(new Object[][] {
         { 0L, new Book(0L, "The beginning", "The people") }
         }
   ).collect(Collectors.toMap(data -> (Long) data[0], data -> (Book) data[1]));


   @Override
   public void findById(Long id, Handler<AsyncResult<JsonObject>> resultHandler) {
      Future<JsonObject> future = store.containsKey(id)
            ? Future.succeededFuture(JsonObject.mapFrom(store.get(id)))
            : Future.failedFuture("Not found");

      resultHandler.handle(future);
   }

   @Override
   public void findAll(Handler<AsyncResult<List<JsonObject>>> resultHandler) {
      List<JsonObject> jsonObjects = store.values()
                                          .stream()
                                          .map(JsonObject::mapFrom)
                                          .collect(Collectors.toList());

      Future<List<JsonObject>> listFuture = Future.succeededFuture(jsonObjects);
      resultHandler.handle(listFuture);
   }

   @Override
   public void save(JsonObject jsonObject, Handler<AsyncResult<JsonObject>> resultHandler) {
      Book book = jsonObject.mapTo(Book.class);
      book.setId(initialId++);
      store.put(book.getId(), book);
      JsonObject jsonObjectSaved = JsonObject.mapFrom(book);
      resultHandler.handle(Future.succeededFuture(jsonObjectSaved));
   }

}
