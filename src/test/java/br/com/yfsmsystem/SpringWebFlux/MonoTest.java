package br.com.yfsmsystem.SpringWebFlux;


import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
public class MonoTest {


    @Test
    public void testMonoSubscribe() {
        String name = "test";
        Mono<String> mono = Mono.just(name).log();
        mono.subscribe();
        log.info("------------------------------------");
        StepVerifier.create(mono).expectNext(name).verifyComplete();// Unit Test Subscriber

    }


    @Test
    public void testMonoSubscribeConsumer() {
        String name = "test";
        Mono<String> mono = Mono.just(name).log();
        mono.subscribe(s -> log.info("Value: {}", s));
        log.info("------------------------------------");
        StepVerifier.create(mono).expectNext(name).verifyComplete();// Unit Test Subscriber
    }

    @Test
    public void testMonoSubscribeConsumerError() {
        String name = "test";
        Mono<String> mono = Mono.just(name).map(s -> {
            throw new RuntimeException();
        });
        mono.subscribe(s -> log.info("Value: {}", s), s -> log.info("Error"));
        log.info("------------------------------------");
        StepVerifier.create(mono).expectError(RuntimeException.class).verify();// Unit Test Subscriber
    }

    @Test
    public void testMonoSubscribeConsumerErrorWithStackTracer() {
        String name = "test";
        Mono<String> mono = Mono.just(name).map(s -> {
            throw new RuntimeException();
        });
        mono.subscribe(s -> log.info("Value: {}", s), Throwable::printStackTrace);
        log.info("------------------------------------");
        StepVerifier.create(mono).expectError(RuntimeException.class).verify();// Unit Test Subscriber
    }


    @Test
    public void testMonoSubscribeConsumerComplete() {
        String name = "test";
        Mono<String> mono = Mono.just(name).map(String::toUpperCase);
        mono.subscribe(s -> log.info("Value: {}", s), Throwable::printStackTrace, () -> log.info("Completed"));
        log.info("------------------------------------");
        StepVerifier.create(mono).expectNext(name.toUpperCase()).verifyComplete();
    }

    @Test
    public void testMonoSubscribeConsumerSubscription() {
        String name = "test";
        Mono<String> mono = Mono.just(name).map(String::toUpperCase);
        mono.subscribe(s -> log.info("Value: {}", s),
                Throwable::printStackTrace,
                () -> log.info("Completed"),
                Subscription::cancel);
    }


    @Test
    public void testMonoSubscribeConsumerBackPressure() {
        String name = "test";
        Mono<String> mono = Mono.just(name).map(String::toUpperCase);
        mono.subscribe(s -> log.info("Value: {}", s),
                Throwable::printStackTrace,
                () -> log.info("Completed"),
                subscription -> subscription.request(2));//define backpressure
    }

    @Test
    public void testMonoDoOnMethods() {
        String name = "Yallison Melo";
        Mono<Object> mono = Mono.just(name).log()
                .map(String::toUpperCase)
                .doOnSubscribe(subscription -> log.info("Subscribed"))
                .doOnRequest(longNumber -> log.info("Request Received, starting doing something..."))
                .doOnNext(s -> log.info("Value is here, Executing do doOnNext {}...",s))
                .flatMap(s-> Mono.empty())
                .doOnNext(s -> log.info("Value is here, Executing do doOnNext {}...",s))
                .doOnSuccess(s -> log.info("doOnSuccess executed!"));

        mono.subscribe(s -> log.info("Value: {}", s),
                Throwable::printStackTrace,
                () -> log.info("Completed"));
    }

    @Test
    public void testMonoDoOnError() {
      Mono<Object> error =   Mono.error(new IllegalArgumentException("Illegal argument exception"))
                .doOnError(e -> log.error("Error Message : {}", e.getMessage()))
                .log();

        log.info("------------------------------------");

        StepVerifier.create(error)
                .expectError(IllegalArgumentException.class)
                .verify(); //Unit Test Subscriber (verifica se o erro é do tipo IllegalArgumentException e se está completo (terminou)
    }


    @Test
    public void testMonoDoOnErrorResume() {
        String newCustomerName = "Felipe";
        Mono<Object> error =   Mono.error(new IllegalArgumentException("Illegal argument exception"))
                .doOnError(e -> log.error("Error Message : {}", e.getMessage()))
                .onErrorResume(s->{
                    log.info("Inside On Error Resume");// using to fallback actions
                    return Mono.just(newCustomerName);
                })
                .log();

        log.info("------------------------------------");

        StepVerifier.create(error)
                .expectNext(newCustomerName)
                .verifyComplete();
    }


    @Test
    public void testMonoDoOnErrorReturn() {
        String newCustomerName = "Felipe";
        String valueError = "Error Customer";
        Mono<Object> error =   Mono.error(new IllegalArgumentException("Illegal argument exception"))
                .doOnError(e -> log.error("Error Message : {}", e.getMessage()))
                .onErrorReturn(valueError)
                .onErrorResume(s->{
                    log.info("Inside On Error Resume");// using to fallback actions
                    return Mono.just(newCustomerName);
                })

                .log();

        log.info("------------------------------------");

        StepVerifier.create(error)
                .expectNext(valueError)
                .verifyComplete();
    }
}
