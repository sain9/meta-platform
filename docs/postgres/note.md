## create network
> docker network create meta-platform-net

## data-api or definition api is not working
> docker network inspect meta-platform-net

[
{
"Name": "meta-platform-net",
"Id": "3ff159376035bd94da9ee8274d43cf13d37e68b60308ec57524d0c2697e476b4",
"Created": "2026-10-24T01:08:00.304865919+05:30",
"Scope": "local",
"Driver": "bridge",
"EnableIPv6": false,
"IPAM": {
"Driver": "default",
"Options": {},
"Config": [
{
"Subnet": "172.24.0.0/16",
"Gateway": "172.24.0.1"
}
]
},
"Internal": false,
"Attachable": false,
"Ingress": false,
"ConfigFrom": {
"Network": ""
},
"ConfigOnly": false,
"Containers": {
"2737df53748be21552bd53dc587f531e588a4564bea45003a43e8ff98c52853f": {
"Name": "user-access-api",
"EndpointID": "292c7a207556efa1c8d6ec59d0c2a29a1721d689c318117422e9762ffad38b74",
"MacAddress": "02:42:ac:18:00:05",
"IPv4Address": "172.24.0.5/16",
"IPv6Address": ""
},
"2b127f3b44ac9a6aeaae85d277fbb0e05da96b4e408570d47cc147f84ba76da8": {
"Name": "keycloak",
"EndpointID": "8231062925b5504b64cd9ad8d9ab61e3bb6d44cfd10d0fc88d1867ed7bc888cd",
"MacAddress": "02:42:ac:18:00:02",
"IPv4Address": "172.24.0.2/16",
"IPv6Address": ""
},
"7996e6efe388c3f4d8e5c991b8c6305a1dd2b6e8d0efebcaa620e1fcdb1610c9": {
"Name": "gateway-api",
"EndpointID": "e6fda158a4c2b30b95432e99349e0eb63f3d5085b75527c1dead90dc8fb86458",
"MacAddress": "02:42:ac:18:00:06",
"IPv4Address": "172.24.0.6/16",
"IPv6Address": ""
},
"aa33fca05c59e1113b75807a796b4af4bb9c9a0420209c93237c1e998c49cf2c": {
"Name": "definition-api",
"EndpointID": "79220b2628823dab73a740d4cdafb67c7eadde1ae95624901b2bf947e81ec667",
"MacAddress": "02:42:ac:18:00:08",
"IPv4Address": "172.24.0.8/16",
"IPv6Address": ""
},
"b9f33da29455ce2c9e207a449bfaeefde55cd0b4cc14c497664f1f6a28ee02ab": {
"Name": "postgres",
"EndpointID": "102f2d87d37dbd331e314ea56157bea5ba93db7beff9d2ed8d12750f8f1a9e8f",
"MacAddress": "02:42:ac:18:00:03",
"IPv4Address": "172.24.0.3/16",
"IPv6Address": ""
},
"ea31db1785019da0a23ea5185bb7962756a45697f18af1b78d5a5e89f2669b66": {
"Name": "data-api",
"EndpointID": "65aa3497713292d81623282472913888355e7103d8f2a85df600eb9328e1ac06",
"MacAddress": "02:42:ac:18:00:04",
"IPv4Address": "172.24.0.4/16",
"IPv6Address": ""
},
"ffc959b23bb463fd0c51d281008e1d7b228f07c7314140002ef2ea680d54b0d2": {
"Name": "pgadmin",
"EndpointID": "8eb6bcb5033c358c72467756123ddd67ab9d2656827f253fd8565c7de907d308",
"MacAddress": "02:42:ac:18:00:07",
"IPv4Address": "172.24.0.7/16",
"IPv6Address": ""
}
},
"Options": {},
"Labels": {}
}
]

# if anything is missing say postgres or definition api etc.
# then include it as following

> docker network connect meta-platform-net definition-api
# this will include the missing microservice or container
# in our custom network [meta-platform-net]


