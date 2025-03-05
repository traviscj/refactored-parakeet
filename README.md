
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
