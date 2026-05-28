# build / run

Toolchain is pinned with [Hermit](https://cashapp.github.io/hermit/) (`bin/` =
corretto@21). Activate it first, or `./gradlew` runs under your ambient `java`
(Gradle 8.13 rejects Java 25):

```sh
. bin/activate-hermit
./gradlew run          # serves :1337; needs MySQL up (see below / kv3 docker-compose)
./gradlew installDist  # builds build/install/iotcj for the kv3 `full` compose profile
```

Java + Kotlin both target 21. DB defaults to `localhost:3307` (the kv3 docker MySQL);
`docker/traviscj-production.yaml` overrides that to the `mysql` service when run as a
container. The fastest local DB is `cd ~/kv3 && docker-compose up -d` — the manual
MySQL setup below is the from-scratch alternative.

# smoke test

Once `./gradlew run` is serving on `:1337`, hit the endpoints directly:

```sh
curl http://localhost:1337/io.tcj.KvService/List -d '{
    "ns": "MORTGAGES",
    "k_prefix": "",
    "limit": 20
}' -H 'Content-type: application/json' -H 'Accept: application/json' | jq .

curl http://localhost:1337/io.tcj.MortgageService/Calc -d '{
    "principal": 510400,
    "apr": 2.625,
    "term": 30
}' -H 'Content-type: application/json' -H 'Accept: application/json' | jq .
```

# mysql (manual / from scratch)

```
$ brew install mysql
$ brew services start mysql
$ mysql -u root
mysql> create database traviscj_localdev;
Query OK, 1 row affected (0.01 sec)
mysql> use traviscj_localdev ;
Database changed
mysql> CREATE TABLE `schema_version` (
    ->   `version` varchar(50) NOT NULL,
    ->   `installed_by` varchar(30) DEFAULT NULL,
    ->   PRIMARY KEY (`version`)
    -> ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
    -> ;
Query OK, 0 rows affected (0.01 sec)
```

```sql
create user root with superuser;
```


## qr-buttons
the QR code could just link to a simple form to create kv entries with a namespace, key, and value  or (ns, k, v) for short

nice-to-have feature 1: when namespace is unpopulated, show most recent N distinct namespace values

nice-to-have feature 2: when namespace is selected, show most recent N (k, v) pairs within that namespace.

- kv is feed published
- different feed consumers can listen for KV entries, perhaps listening for a specific namespace or something, and react appropriately
e.g. when `k.ns==“LAUNDRY::START”, enqueue a notification based on the k.v payload (perhaps {"minutes":64} or something)
