## To check Test reflection + Info:
# macOS
brew install grpcurl

# Linux (with Go)
go install github.com/fullstorydev/grpcurl/cmd/grpcurl@latest   


# List available services (proves reflection works)
grpcurl -plaintext localhost:9003 list

# Expected output:
# com.hussain.data_api.grpc.proto.DataApiService
# grpc.health.v1.Health
# grpc.reflection.v1alpha.ServerReflection

# Describe our service
grpcurl -plaintext localhost:9003 describe com.hussain.data_api.grpc.proto.DataApiService

# Expected: rpc Execute, rpc BatchExecute, rpc Info

# Call Info (no proto file needed — reflection provides the schema)
grpcurl -plaintext -d '{}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Info

# Expected output:
# {
#   "service": "data-api",
#   "version": "1.0.0",
#   "status": "UP",
#   "uptimeSeconds": "12"
# }

# Check the standard gRPC health service
grpcurl -plaintext -d '{"service": ""}' localhost:9003 grpc.health.v1.Health/Check

# Expected:
# {
#   "status": "SERVING"
# }


## sample grpc api call
grpcurl -plaintext -d '{
"operation": "READ",
"entity": "dn_pricing",
"columns": ["uuid","part_number","price"]
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute

grpcurl -plaintext -d '{
"operation": "BULK_UPDATE",
"entity": "dn_pricing",
"bulkKeyColumn": "uuid",
"data": {
"items": { "listValue": { "values": [
{ "mapValue": { "fields": {
"uuid":  {"stringValue": "11111111-1111-1111-1111-111111111111"},
"price": {"doubleValue": 111}
}}},
{ "mapValue": { "fields": {
"uuid":  {"stringValue": "22222222-2222-2222-2222-222222222222"},
"price": {"doubleValue": 222}
}}}
]}}
}
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute

1. CREATE — Insert a part
   grpcurl -plaintext -d '{
   "operation": "CREATE",
   "entity": "part",
   "data": {
   "short_part_number": {"stringValue": "SP-BULK-A"},
   "ecc": {"stringValue": "ECC-A"},
   "name": {"stringValue": "Test Part A"},
   "brand_minor": {"stringValue": "Bosch"},
   "uuid": {"stringValue": "aaaaaaaa-0000-0000-0000-000000000001"},
   "code": {"stringValue": "BULK-A-1"},
   "saleable": {"stringValue": "Y"},
   "segment": {"stringValue": "Braking"}
   }
   }' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute


2. READ — Fetch parts by IDs using IN
   grpcurl -plaintext -d '{
   "operation": "READ",
   "entity": "part",
   "columns": ["id", "name"],
   "filters": [
   {
   "column": "id",
   "operator": "IN",
   "value": {
   "listValue": {
   "values": [
   {"intValue": "1"},
   {"intValue": "2"}
   ]
   }
   }
   }
   ]
   }' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute


3. READ — Fetch one part by ID
   grpcurl -plaintext -d '{
   "operation": "READ",
   "entity": "part",
   "columns": ["id", "name"],
   "filters": [
   {
   "column": "id",
   "operator": "EQ",
   "value": {"intValue": "1"}
   }
   ]
   }' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute


4. READ — Fetch pricing by UUID
   grpcurl -plaintext -d '{
   "operation": "READ",
   "entity": "dn_pricing",
   "columns": ["uuid", "price"],
   "filters": [
   {
   "column": "uuid",
   "operator": "EQ",
   "value": {
   "stringValue": "11111111-1111-1111-1111-111111111111"
   }
   }
   ]
   }' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute


5. BULK_UPDATE — Update two parts
   grpcurl -plaintext -d '{
   "operation": "BULK_UPDATE",
   "entity": "part",
   "data": {
   "items": {
   "listValue": {
   "values": [
   {
   "mapValue": {
   "fields": {
   "id": {"intValue": "1"},
   "name": {"stringValue": "Brake Pad Front (BC update #1)"}
   }
   }
   },
   {
   "mapValue": {
   "fields": {
   "id": {"intValue": "2"},
   "name": {"stringValue": "Part 2 (BC update #1)"}
   }
   }
   }
   ]
   }
   }
   }
   }' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute


6. BULK_UPDATE — Update one part by ID
   grpcurl -plaintext -d '{
   "operation": "BULK_UPDATE",
   "entity": "part",
   "data": {
   "items": {
   "listValue": {
   "values": [
   {
   "mapValue": {
   "fields": {
   "id": {"intValue": "1"},
   "name": {"stringValue": "Brake Pad Front (BC-#1)"}
   }
   }
   }
   ]
   }
   }
   }
   }' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute

7. BULK_UPDATE — Update a part without an ID
   This is the test from history entry 974. It omits the ID, making it useful for checking your API's validation behavior.
   grpcurl -plaintext -d '{
   "operation": "BULK_UPDATE",
   "entity": "part",
   "data": {
   "items": {
   "listValue": {
   "values": [
   {
   "mapValue": {
   "fields": {
   "name": {"stringValue": "No ID"}
   }
   }
   }
   ]
   }
   }
   }
   }' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute


8. BULK_UPDATE — Update pricing without bulkKeyColumn
   This request appears four times in your history (966–969) and again at 975.
   grpcurl -plaintext -d '{
   "operation": "BULK_UPDATE",
   "entity": "dn_pricing",
   "data": {
   "items": {
   "listValue": {
   "values": [
   {
   "mapValue": {
   "fields": {
   "uuid": {"stringValue": "11111111-1111-1111-1111-111111111111"},
   "price": {"doubleValue": 999}
   }
   }
   }
   ]
   }
   }
   }
   }' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute


9. BULK_UPDATE — Update pricing with bulkKeyColumn
   This is the version from history entry 976, explicitly specifying uuid as the bulk matching key.
   grpcurl -plaintext -d '{
   "operation": "BULK_UPDATE",
   "entity": "dn_pricing",
   "bulkKeyColumn": "uuid",
   "data": {
   "items": {
   "listValue": {
   "values": [
   {
   "mapValue": {
   "fields": {
   "uuid": {"stringValue": "11111111-1111-1111-1111-111111111111"},
   "price": {"doubleValue": 999}
   }
   }
   }
   ]
   }
   }
   }
   }' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute