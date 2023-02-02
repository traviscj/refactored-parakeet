
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
