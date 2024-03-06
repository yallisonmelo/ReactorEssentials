package br.com.yfsmsystem.SpringWebFlux;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.util.annotation.NonNullApi;

import java.time.Duration;
import java.util.List;


@Slf4j
public class FluxTest {


    @Test
    public void fluxSubscribeName() {
        Flux<String> fluxString = Flux.just("Yallison", "Felipe", "Melo").log();

        StepVerifier.create(fluxString)
                .expectNext("Yallison", "Felipe", "Melo")
                .verifyComplete();
    }

    @Test
    public void fluxSubscribeNumbers() {
        Flux<Integer> fluxNumbers = Flux.range(1, 10).log();
        fluxNumbers.subscribe(i -> log.info("Number: {}", i));
        log.info("----------------------------------");
        StepVerifier.create(fluxNumbers)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .verifyComplete();
    }


    @Test
    public void fluxSubscribeFromList() {
        Flux<Integer> fluxNumbers = Flux.fromIterable(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)).log();
        fluxNumbers.subscribe(i -> log.info("Number: {}", i));
        log.info("----------------------------------");
        StepVerifier.create(fluxNumbers)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .verifyComplete();
    }

    @Test
    public void fluxSubscribeNumersErrors() {
        Flux<Integer> fluxNumbers = Flux.range(1, 10).log().map(i -> {
            if (i == 5) {
                throw new RuntimeException();
            }
            return i;
        });
        fluxNumbers.subscribe(i -> log.info("Number: {}", i), Throwable::printStackTrace, () -> log.info("Completed"),
                subscription -> subscription.request(2));
        log.info("----------------------------------");
        StepVerifier.create(fluxNumbers)
                .expectNext(1, 2, 3, 4)
                .expectError(RuntimeException.class)
                .verify();
    }


    @Test
    public void fluxSubscribeUglyBackPressure() {
        Flux<Integer> fluxNumbers = Flux.range(1, 10).log();
        fluxNumbers.subscribe(new Subscriber<>() {
                                  private Subscription subscription;
                                  private int count = 0;
                                  private final int requestCount = 2;

                                  @Override
                                  public void onSubscribe(Subscription subscription) {
                                      this.subscription = subscription;
                                      subscription.request(requestCount);
                                  }

                                  @Override
                                  public void onNext(Integer integer) {
                                      count++;
                                      if (count >= requestCount) {
                                          count = 0;
                                          subscription.request(requestCount);
                                      }
                                  }

                                  @Override
                                  public void onError(Throwable throwable) {

                                  }

                                  @Override
                                  public void onComplete() {

                                  }
                              }
        );
        log.info("----------------------------------");
        StepVerifier.create(fluxNumbers)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .verifyComplete();
    }

    @Test
    public void fluxSubscribeBaseUglyBackPressure() {
        Flux<Integer> fluxNumbers = Flux.range(1, 10).log();
        fluxNumbers.subscribe(new BaseSubscriber<>() {
            private int count = 0;
            private final int requestCount = 2;

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                request(requestCount);
            }

            @Override
            protected void hookOnNext(Integer  value) {
                count++;
                if (count >= requestCount) {
                    count = 0;
                    request(requestCount);
                }
            }
        });
        log.info("----------------------------------");
        StepVerifier.create(fluxNumbers)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberInterval() throws Exception {
        Flux<Long> interval = Flux.interval(Duration.ofMillis(100)).take(10).log();
        interval.subscribe(i -> log.info("Number: {}", i));
        log.info("----------------------------------");
    }

    @Test
    public void fluxSubscriberPrettyBackPressure()  {
        Flux<Integer> flux = Flux.range(1, 10)
                .log()
                .limitRate(3);

        flux.subscribe(i -> log.info("Number: {}", i));

        log.info("----------------------------------");

        StepVerifier.create(flux)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .verifyComplete();
    }

    @Test
    public void connectableFlux() {
        Flux<Integer> flux = Flux.range(1, 10).log();
        ConnectableFlux<Integer> connectableFlux = flux.publish();
        connectableFlux.delayElements(Duration.ofMillis(100));
        connectableFlux.connect();
        connectableFlux.subscribe(i -> log.info("Number: {}", i));
        log.info("----------------------------------");
        StepVerifier.create(connectableFlux)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .verifyComplete();
    }
}
