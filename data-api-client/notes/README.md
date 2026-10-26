# data-api-client

Thin gRPC client for **data-api**. Add the dependency, configure 4 properties, inject
`DataApiClient` — you get REST-equivalent `List<Row>` results with no protobuf noise.

---

## 1. Add the dependency

```xml
<dependency>
    <groupId>com.hussain</groupId>
    <artifactId>data-api-client</artifactId>
    <version>1.0.0</version>
</dependency>
```
---
The client pulls its proto contract from com.hussain:data-api-proto transitively
— you do not need to add it separately.

2. Configure (application.properties)
```
   data-api.grpc.host=localhost
   data-api.grpc.port=9003
   data-api.grpc.plaintext=true
   data-api.grpc.deadline-seconds=30
```
---
Optional properties (defaults shown):

```
properties
data-api.grpc.max-inbound-message-size=8388608
data-api.grpc.enabled=true
data-api.grpc.logging-enabled=true
```
---
```
3. Inject and use
java
@Autowired
private DataApiClient dataApiClient;
That's it — DataApiClient is auto-registered. No @ComponentScan, no config class.

4. Quick examples
Read — simple
java
List<Row> rows = dataApiClient.read(
        DataApiRequest.read("dn_pricing", "uuid", "part_number", "price"));
Row is a LinkedHashMap<String, Object> — column order preserved.

Read — with filter
java
List<Row> rows = dataApiClient.read(DataApiRequest.builder()
        .operation(Operation.READ)
        .entity("dn_pricing")
        .column("uuid").column("part_number").column("price")
        .filter(DataApiRequest.Filter.builder()
                .column("part_number")
                .operator(FilterOp.EQ)
                .value("PART-10001")
                .build())
        .limit(10)
        .build());
Read — filterGroup (AND/OR)
java
List<Row> rows = dataApiClient.read(DataApiRequest.builder()
        .operation(Operation.READ)
        .entity("dn_pricing")
        .column("part_number").column("price")
        .filterGroup(DataApiRequest.FilterGroup.builder()
                .logicalOperator(LogicOp.OR)
                .filter(DataApiRequest.Filter.builder()
                        .column("price").operator(FilterOp.LT).value(1000.0).build())
                .filter(DataApiRequest.Filter.builder()
                        .column("part_number").operator(FilterOp.EQ).value("PART-10013").build())
                .build())
        .build());
Read — GROUP BY + HAVING
java
List<Row> rows = dataApiClient.read(DataApiRequest.builder()
        .operation(Operation.READ)
        .entity("dn_pricing")
        .column("part_number")
        .column("COUNT(*) as cnt")
        .column("SUM(price) as total")
        .groupBy(DataApiRequest.GroupBy.builder().column("part_number").build())
        .having(DataApiRequest.Having.builder()
                .column("price")
                .aggregate(Aggregate.SUM)
                .operator(FilterOp.GT)
                .value(5000.0)
                .build())
        .orderBy(DataApiRequest.OrderBy.builder()
                .column("total").direction(Direction.DESC).build())
        .limit(5)
        .build());
Read — JOIN
java
List<Row> rows = dataApiClient.read(DataApiRequest.builder()
        .operation(Operation.READ)
        .from(DataApiRequest.From.builder().entity("parts").alias("p").build())
        .join(DataApiRequest.Join.builder()
                .type(JoinType.LEFT)
                .entity("suppliers")
                .alias("s")
                .on(DataApiRequest.On.builder()
                        .left("s.part_name").operator(FilterOp.EQ).right("p.name")
                        .build())
                .build())
        .column("p.name")
        .filter(DataApiRequest.Filter.builder()
                .column("s.part_name").operator(FilterOp.IS_NULL).build())
        .build());
Read — Subquery
java
List<Row> rows = dataApiClient.read(DataApiRequest.builder()
        .operation(Operation.READ)
        .from(DataApiRequest.From.builder().entity("parts").alias("p").build())
        .column("p.name")
        .filter(DataApiRequest.Filter.builder()
                .column("p.name")
                .operator(FilterOp.IN)
                .subquery(DataApiRequest.Subquery.builder()
                        .query(DataApiRequest.builder()
                                .operation(Operation.READ)
                                .from(DataApiRequest.From.builder()
                                        .entity("suppliers").alias("s").build())
                                .column("s.part_name")
                                .build())
                        .build())
                .build())
        .build());
Create
java
int affected = dataApiClient.create("dn_pricing", Map.of(
        "part_number", "PART-GRPC-1",
        "price", 999.99
));
Update
java
int affected = dataApiClient.update("dn_pricing",
        Map.of("price", 199.99),
        DataApiRequest.Filter.builder()
                .column("part_number").operator(FilterOp.EQ).value("PART-GRPC-1").build());
Delete
java
int affected = dataApiClient.delete("dn_pricing",
        DataApiRequest.Filter.builder()
                .column("part_number").operator(FilterOp.EQ).value("PART-GRPC-1").build());
Bulk create
java
DataApiRequest bulk = DataApiRequest.builder()
        .operation(Operation.BULK_CREATE)
        .entity("dn_pricing")
        .data(Map.of("items", List.of(
                Map.of("part_number", "PART-B1", "price", 100),
                Map.of("part_number", "PART-B2", "price", 200),
                Map.of("part_number", "PART-B3", "price", 300)
        )))
        .build();
DataApiResponse resp = dataApiClient.execute(bulk);
Bulk update — specify the key column
java
DataApiRequest bulk = DataApiRequest.builder()
        .operation(Operation.BULK_UPDATE)
        .entity("dn_pricing")
        .bulkKeyColumn("uuid")     // required for tables without "id"
        .data(Map.of("items", List.of(
                Map.of("uuid", "11111111-1111-1111-1111-111111111111", "price", 111),
                Map.of("uuid", "22222222-2222-2222-2222-222222222222", "price", 222)
        )))
        .build();
DataApiResponse resp = dataApiClient.execute(bulk);
Batch (multiple operations in one round trip)
java
BatchResult result = dataApiClient.batch(List.of(
        DataApiRequest.read("dn_pricing", "uuid", "part_number"),
        DataApiRequest.create("dn_pricing",
                Map.of("part_number", "PART-BATCH", "price", 999)),
        DataApiRequest.read("dn_pricing", "uuid", "part_number")
));
result.getResults().forEach(r -> System.out.println(r.getRows()));
Info
java
InfoResponse info = dataApiClient.info();
System.out.println(info.getService() + " v" + info.getVersion() + " — " + info.getStatus());
5. Response shape
java
public class DataApiResponse {
    private boolean success;
    private String message;
    private List<Map<String, Object>> rows;   // READ
    private Object data;                       // CREATE / UPDATE / DELETE (e.g. Map.of("affectedRows", N))
    private Pagination pagination;
    private List<ValidationError> errors;
}
When the operation is READ, check getRows(). Otherwise check getData().

6. Value types in a Row
SQL column type	Java type in Row
integer	Long
bigint	Long
numeric / decimal	String (preserves precision)
varchar / text	String
boolean	Boolean
timestamp	String
uuid	String
json / jsonb	String
bytea	ByteString
SQL NULL	null
Numeric note: numeric and decimal columns arrive as String to preserve precision. Parse with new BigDecimal((String) row.get("price")).

7. Error handling
All failures are wrapped in DataApiClientException with a Status.Code:

java
try {
    dataApiClient.read(DataApiRequest.read("dn_pricing", "uuid"));
} catch (DataApiClientException e) {
    switch (e.getStatusCode()) {
        case INVALID_ARGUMENT -> // validation error (bad request)
                log.warn("Bad request: {}", e.getMessage());
        case INTERNAL -> // server-side error (SQL failure, unexpected)
                log.error("Server error: {}", e.getMessage(), e);
        case UNAVAILABLE -> // network issue / server down
                log.error("Cannot reach data-api", e);
        case DEADLINE_EXCEEDED -> // timeout
                log.warn("Timed out", e);
        default -> log.error("Unknown gRPC error: {}", e.getStatusCode(), e);
    }
}
Which status codes you'll see
Code	Meaning	Typical cause
INVALID_ARGUMENT	Request failed validation	Missing entity, missing data, bad operator, missing filter value
INTERNAL	Server-side exception	Unknown table, SQL syntax error, DB connection failure
UNAVAILABLE	Transport failure	Server down, DNS failure, network cut
DEADLINE_EXCEEDED	Call exceeded deadline-seconds	Slow query
8. Configuration reference
Property	Default	Description
data-api.grpc.host	localhost	Server hostname
data-api.grpc.port	9003	Server gRPC port
data-api.grpc.plaintext	true	true = no TLS, false = TLS (internal mesh / mTLS)
data-api.grpc.deadline-seconds	30	Per-call timeout
data-api.grpc.max-inbound-message-size	8388608 (8 MB)	Max response size
data-api.grpc.enabled	true	Master switch — set false in tests to skip auto-config
data-api.grpc.logging-enabled	true	Per-call [gRPC-OUT] logs
9. Debugging
Nothing returned / call hangs

Verify the server is reachable: grpcurl -plaintext <host>:<port> list

If reflection works, the wire is up

DataApiClientException: INVALID_ARGUMENT

Your request failed server-side validation

Inspect e.getMessage() — it names the exact field or operator

DataApiClientException: UNAVAILABLE

Server down or wrong host/port

Check data-api.grpc.host and data-api.grpc.port in the consumer's properties

DataApiClientException: DEADLINE_EXCEEDED

Query took longer than deadline-seconds

Either raise the timeout or add filters / limit to make the query cheaper

No qualifying bean of type 'DataApiClient'

The auto-config didn't run

Verify data-api.grpc.enabled=true (or unset — the default is true)

Verify the client jar is on the consumer's classpath

Verify META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
exists inside the jar (it should; sanity check with unzip -l ... | grep AutoConfiguration.imports)

10. Versioning
data-api-client follows the version of data-api-proto:

data-api-proto change	data-api-client version	Example
New field / new message (additive)	patch or minor	1.0.0 → 1.0.1 or 1.1.0
Field rename / removal (breaking wire)	major	1.0.0 → 2.0.0
Only bug fixes in client code	patch	1.0.0 → 1.0.1
Consumers pin the client version, so upgrade at your own pace.

text

---

## File 2 — `data-api-proto/README.md`

```markdown
# data-api-proto

Single source of truth for `data_api.proto` — the gRPC contract consumed by both
`data-api` (server) and `data-api-client` (consumer library).

**Do not add Java code to this module.** It only ships the `.proto` file as a
resource inside `META-INF/proto/`. Compilation happens downstream in each
consumer's build.

---

## Layout
data-api-proto/
├── pom.xml
└── src/main/proto/data_api.proto ← the ONE physical proto

text

Packaged JAR contains only:
META-INF/proto/data_api.proto

text

No `.class` files, no Spring, no transitive dependencies.

---

## How the contract is consumed

### `data-api` (server)

- Depends on `data-api-proto`
- Unpacks `META-INF/proto/data_api.proto` at build time via `maven-dependency-plugin`
- Compiles it via `protobuf-maven-plugin`
- Generated Java classes land in **`com.hussain.data_api.grpc.proto`**
- Wire package: `com.hussain.data_api.grpc.proto`

### `data-api-client` (consumer library)

- Depends on `data-api-proto`
- Unpacks the same proto
- **Rewrites** `option java_package` from `com.hussain.data_api.grpc.proto` →
  `com.hussain.data_api_client.proto` (via `maven-replacer-plugin`, on the
  unpacked copy only — the artifact itself is untouched)
- Compiles it
- Generated Java classes land in **`com.hussain.data_api_client.proto`**

**Wire stays identical.** Both sides send/receive the same `package com.hussain.data_api.grpc.proto;`
wire namespace. Only the generated Java classes' package differs, so a consumer
microservice that depends on both `data-api` (as a dependency) and `data-api-client`
won't have classpath collisions.

---

## Changing the contract

When you need to add a field, message, RPC, etc.:

### 1. Edit

```bash
vim data-api-proto/src/main/proto/data_api.proto
2. Bump version
bash
# SemVer rules:
#   patch (1.0.0 → 1.0.1)  — internal cleanup, comments, doc changes
#   minor (1.0.0 → 1.1.0)  — new fields, new messages (wire-compatible)
#   major (1.0.0 → 2.0.0)  — field renames, removals, type changes
vim data-api-proto/pom.xml
3. Publish locally (or to your internal Nexus)
bash
cd data-api-proto
mvn clean install
4. Update consumers
In data-api/pom.xml and data-api-client/pom.xml:

xml
<dependency>
    <groupId>com.hussain</groupId>
    <artifactId>data-api-proto</artifactId>
    <version>1.1.0</version>   <!-- was 1.0.0 -->
</dependency>
Also update the data-api-proto.version property in data-api-client/pom.xml
(used by maven-dependency-plugin and replacer).

5. Rebuild both consumers
bash
cd data-api && mvn clean install
cd ../data-api-client && mvn clean install
6. Bump consumer versions
data-api version bump (patch or minor)

data-api-client version bump (patch or minor)

7. Announce
Message your teams: "data-api-client X.Y.Z available; it now supports <new field>"

Consumers upgrade at their own pace — they pin the version

Why this module exists
Before data-api-proto, the proto existed as:

A physical file in data-api/src/main/proto/

A copy in data-api-client/src/main/proto/

Two files → guaranteed drift. One day a consumer would report "bulkKeyColumn doesn't work" — because they were on the client's stale copy.

data-api-proto fixes that by making the proto an artifact, versioned in Maven, consumed by both sides:

One physical file on disk

One version number

One build pipeline

Zero drift

Checking what's in the artifact
bash
unzip -l ~/.m2/repository/com/hussain/data-api-proto/<version>/data-api-proto-<version>.jar
Expected — exactly two files:

text
META-INF/MANIFEST.MF
META-INF/proto/data_api.proto
If you see .class files or BOOT-INF/, something is wrong — the module must
stay pure (no src/main/java/, no spring-boot-maven-plugin).

text

---

## File 3 — `data-api-client/src/main/resources/application-example.properties`

A template that consumer services copy-paste into their own `application.properties`:

```properties
# ==============================================
# data-api gRPC client
# ==============================================
# Copy these into your own service's application.properties.

# Server location
data-api.grpc.host=localhost
data-api.grpc.port=9003

# Transport — set to false if the server runs behind TLS/mTLS
data-api.grpc.plaintext=true

# Per-call timeout (seconds). Bump for heavy aggregation queries.
data-api.grpc.deadline-seconds=30

# Max response size — raise if your queries return large payloads
data-api.grpc.max-inbound-message-size=8388608

# Master switch — set to false in tests to skip the auto-configured beans
data-api.grpc.enabled=true

# Per-call log lines: [gRPC-OUT] <method> OK in Xms
data-api.grpc.logging-enabled=true
Bonus — small Consumer-onboarding.md at the repo root
Optional but nice — puts the "how do I start using this?" answer right at the entrance.

File: meta-platform/CONSUMER-ONBOARDING.md

markdown
# Using data-api from a new microservice

## 1. Add the dependency

```xml
<dependency>
    <groupId>com.hussain</groupId>
    <artifactId>data-api-client</artifactId>
    <version>1.0.0</version>
</dependency>
2. Add the properties (4 lines)
properties
data-api.grpc.host=localhost
data-api.grpc.port=9003
data-api.grpc.plaintext=true
data-api.grpc.deadline-seconds=30
3. Inject and call
java
@Autowired
private DataApiClient dataApiClient;

List<Row> rows = dataApiClient.read(
        DataApiRequest.read("dn_pricing", "uuid", "part_number", "price"));
Full guide
See data-api-client/README.md — operations,
filters, joins, subqueries, bulk ops, error handling, value types.

text

---

## Verification checklist

After adding all four files:

### 1. Client README is on disk

```bash
ls -la data-api-client/README.md
ls -la data-api-proto/README.md
ls -la data-api-client/src/main/resources/application-example.properties
ls -la CONSUMER-ONBOARDING.md
All four should exist and be non-empty.

2. Client still builds and installs
bash
cd data-api-client
mvn clean install
Should be green. The application-example.properties gets copied into target/classes — harmless.

3. Verify the example properties is in the JAR
bash
unzip -l target/data-api-client-1.0.0.jar | grep "application-example"
# → target-classes/application-example.properties
This is fine — it's an example, not active config. Nothing overrides a consumer's application.properties unless they explicitly copy it in.

4. Smoke test still passes
bash
mvn test -Dtest=ClientSmokeTest
```