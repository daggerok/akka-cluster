package daggerok;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.event.LoggingAdapter;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import io.vavr.control.Try;

import java.util.concurrent.CompletionStage;

/**
 * TODO: Transform that simple example to akka actors clustered communication...
 */
public class Main extends AllDirectives {

  public static void main(String[] args) {

    ActorSystem actorSystem = ActorSystem.create("lets-play");
    LoggingAdapter log = actorSystem.log();
    Main app = new Main();

    Route route = app.routes(log, actorSystem);
    // Route route = app.pathPrefix("api", () -> app.concat(
    //     app.path("hello",
    //              () -> app.get(() -> {
    //                log.info("Handling: GET /hello");
    //                return app.complete("Hello!");
    //              })),
    //     app.path("bye",
    //              () -> app.post(() -> {
    //                log.info("Handling: POST /bye");
    //                new Thread(() -> {
    //                  Try.run(() -> Thread.sleep(1000));
    //                  actorSystem.terminate();
    //                  // System.exit(0);
    //                }).start();
    //                return complete(StatusCodes.ACCEPTED, "Bye...");
    //              }))
    // ));

    Http http = Http.get(actorSystem);
    Materializer materializer = Materializer.matFromSystem(actorSystem);
    Flow<HttpRequest, HttpResponse, NotUsed> handler = route.flow(actorSystem, materializer);
    ConnectHttp connect = ConnectHttp.toHost("0.0.0.0", 8080);
    CompletionStage<ServerBinding> binding = http.bindAndHandle(handler, connect, materializer);

    log.info("Akka HTTP server started: {}", binding);
    log.info("To shutdown, press enter or send 'POST /shutdown' http request.");

    Try.run(System.in::read)
       .onSuccess(aVoid -> log.info("Do you want some?"))
       .onFailure(e -> log.error(e.getLocalizedMessage(), e));

    if (!System.getenv().containsKey("CI"))
      binding.thenCompose(ServerBinding::unbind)
             .thenAccept(done -> actorSystem.terminate());
  }

  private Route routes(LoggingAdapter log, ActorSystem actorSystem) {
    return pathPrefix("api", () ->
        concat(
            path("hello", () ->
                get(() -> {
                  log.info("Handling: GET /hello");
                  return complete("Hello!");
                })),
            path("bye", () ->
                post(() -> {
                  log.info("Handling: POST /bye");
                  new Thread(() -> {
                    Try.run(() -> Thread.sleep(1000));
                    actorSystem.terminate();
                    // System.exit(0);
                  }).start();
                  return complete(StatusCodes.ACCEPTED, "Bye...");
                })
            )
        )
    );
  }
}
