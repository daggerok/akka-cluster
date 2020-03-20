# play [![CI](https://github.com/daggerok/akka-cluster/workflows/%20CI/badge.svg)](https://github.com/daggerok/akka-cluster/actions?query=workflow%3A%22+CI%22)

## develop

```bash
./sbt run
./sbtr test
```

## production

```bash
./sbt clean dist
unzip -d ./target/app ./target/universal/play-java-sbt-example-*.zip
./target/app/play-java-sbt-example-*/bin/play-java-sbt-example -Dplay.http.secret.key=ad31779d4ee49d5ad5162bf1429c32e2e9933f3b
```

See:

* https://www.playframework.com/documentation/2.8.x/ApplicationSecret
* https://www.playframework.com/documentation/2.8.x/Deploying
