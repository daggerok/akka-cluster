package daggerok;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * TODO: Transform that simple example to akka actors clustered communication...
 */
public class Main extends AbstractBehavior<Main.Command> {

  public interface Command { }

  public enum SayHello implements Command {
    INSTANCE
  }

  public static class SetGreeting implements Command {
    public final String greeting;
    public SetGreeting(String greeting) {
      this.greeting = greeting;
    }
  }

  private static final Function<String, Behavior<Command>> withState =
      greeting -> Behaviors.setup(context -> new Main(context, greeting));

  public static final Supplier<Behavior<Command>> behavior =
      () -> withState.apply("Hello!");

  private final String greeting;

  private Main(ActorContext<Command> context, String greeting) {
    super(context);
    this.greeting = greeting;
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder().onMessageEquals(SayHello.INSTANCE, this::handleSayHello)
                              .onMessage(SetGreeting.class, this::handleSetGreeting)
                              .build();
  }

  private Behavior<Command> handleSayHello() {
    getContext().getLog().info(greeting);
    return Behaviors.same();
  }

  private Behavior<Command> handleSetGreeting(SetGreeting command) {
    return withState.apply(command.greeting);
  }

  public static void main(String[] args) {

    ActorSystem<Command> actorSystem = ActorSystem.create(Main.behavior.get(), "lets-play");

    actorSystem.tell(SayHello.INSTANCE);
    actorSystem.tell(new SetGreeting("Привет!"));
    actorSystem.tell(SayHello.INSTANCE);

    actorSystem.terminate();
  }
}
