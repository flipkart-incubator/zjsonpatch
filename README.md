[![CircleCI](https://circleci.com/gh/flipkart-incubator/zjsonpatch/tree/master.svg?style=svg)](https://circleci.com/gh/flipkart-incubator/zjsonpatch/tree/master) [![Join the chat at https://gitter.im/zjsonpatch/community](https://badges.gitter.im/zjsonpatch/community.svg)](https://gitter.im/zjsonpatch/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) 

# This is an implementation of [RFC 6902 JSON Patch](https://datatracker.ietf.org/doc/html/rfc6902) written in Java with extended JSON pointer.

## Description & Use-Cases
- Java Library to find / apply JSON Patches according to [RFC 6902](https://datatracker.ietf.org/doc/html/rfc6902).
- JSON Patch defines a JSON document structure for representing changes to a JSON document.
- It can be used to avoid sending a whole document when only a part has changed, thus reducing network bandwidth requirements if data (in JSON format) is required to send across multiple systems over network or in case of multi DC transfer.
- When used in combination with the HTTP PATCH method as per [RFC 5789 HTTP PATCH](https://datatracker.ietf.org/doc/html/rfc5789), it will do partial updates for HTTP APIs in a standard way.
- Extended JSON pointer functionality (i.e. reference array elements via a key): `/array/id=123/data`
  - The user has to ensure that a unique field is used as a reference key. Should there be more than one array 
    element matching the given key-value pair, the first element will be selected.
  - Key based referencing may be slow for large arrays. Hence, standard index based array pointers should be used for large arrays. 


### Compatible with : Java 17+ versions

### Jackson Version Support
- **Jackson 2.x**: JSON processing using `com.fasterxml.jackson.*` APIs
- **Jackson 3.x**: Full compatibility using `tools.jackson.*` APIs
- Both Jackson versions can coexist as optional dependencies

## ⚠️ Breaking Changes in 0.6.0

### Jackson Dependencies are now Optional

Starting with version 0.6.0, all Jackson dependencies are marked as `<optional>true</optional>` to allow consumers to choose between Jackson 2.x and Jackson 3.x versions.

### Migration Required for Existing Users:

If you were relying on zjsonpatch to transitively provide Jackson dependencies, you must now explicitly add Jackson to your project:

```xml
<!-- Add this to your pom.xml dependencies -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.18.2</version>
</dependency>
```

**Why this change?**
- Prevents version conflicts when projects use different Jackson versions
- Allows explicit choice between Jackson 2.x and Jackson 3.x
- Provides cleaner dependency management for consumers
- Enables Jackson 3.x support without forcing version upgrades

**Impact:** Projects that don't explicitly declare Jackson dependencies will get compilation errors until Jackson is added to their dependencies.

## Code Coverage
Package      |	Class, % 	 |  Method, % 	   |  Line, %           |
-------------|---------------|-----------------|--------------------|
all classes  |	100% (6/ 6)  |	93.6% (44/ 47) |  96.2% (332/ 345)  |

## Complexity
- To find JsonPatch : Ω(N+M), N and M represents number of keys in first and second json respectively / O(summation of la*lb) where la , lb represents JSON array of length la / lb of against same key in first and second JSON ,since LCS is used to find difference between 2 JSON arrays there of order of quadratic.
- To Optimize Diffs ( compact move and remove into Move ) : Ω(D) / O(D*D) where D represents number of diffs obtained before compaction into Move operation.
- To Apply Diff : O(D) where D represents number of diffs

### How to use:

### Current Version : 0.5.0

Add following to `<dependencies/>` section of your pom.xml:

#### For Jackson 2.x support only
```xml
<dependency>
    <groupId>io.github.vishwakarma</groupId>
    <artifactId>zjsonpatch</artifactId>
    <version>{version}</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.18.2</version>
</dependency>
```

#### For Jackson 3.x support only  
```xml
<dependency>
    <groupId>io.github.vishwakarma</groupId>
    <artifactId>zjsonpatch</artifactId>
    <version>{version}</version>
</dependency>
<dependency>
    <groupId>tools.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>3.0.0</version>
</dependency>
```

#### For both Jackson 2.x and 3.x support
```xml
<dependency>
    <groupId>io.github.vishwakarma</groupId>
    <artifactId>zjsonpatch</artifactId>
    <version>{version}</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.18.2</version>
</dependency>
<dependency>
    <groupId>tools.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>3.0.0</version>
</dependency>
```

- 0.4.16 and below can be found at [https://central.sonatype.com/artifact/com.flipkart.zjsonpatch/zjsonpatch/overview](https://central.sonatype.com/artifact/com.flipkart.zjsonpatch/zjsonpatch/overview)
- 0.5.0 and above can be found at [https://central.sonatype.com/artifact/io.github.vishwakarma/zjsonpatch/overview](https://central.sonatype.com/artifact/io.github.vishwakarma/zjsonpatch/overview)

## API Usage

### Jackson Version Selection
You must choose the correct public API classes based on your Jackson version:

- Use `JsonDiff`/`JsonPatch` for Jackson 2.x
- Use `Jackson3JsonDiff`/`Jackson3JsonPatch` for Jackson 3.x

**Why separate APIs?** The original public API is based on Jackson 2.x types and cannot be changed without breaking compatibility. A unified API containing both versions is not possible since only one Jackson version may be available in the classpath, and the versions use incompatible package structures.

Each API automatically uses its corresponding Jackson version internally. The library detects the actual Jackson version of nodes by checking if they are instances of specific JsonNode types, properly handling inheritance and ensuring correct operation even in mixed-version scenarios.

### Obtaining JSON Diff as patch

#### Jackson 2.x API
```java
JsonNode patch = JsonDiff.asJson(JsonNode source, JsonNode target)
```

#### Jackson 3.x API
```java
JsonNode patch = Jackson3JsonDiff.asJson(JsonNode source, JsonNode target)
```
Computes and returns a JSON `patch` from `source`  to `target`,
Both `source` and `target` must be either valid JSON objects or arrays or values. 
Further, if resultant `patch` is applied to `source`, it will yield `target`.

The algorithm which computes this JsonPatch currently generates following operations as per [RFC 6902](https://datatracker.ietf.org/doc/html/rfc6902#section-4) - 
 - `add`
 - `remove`
 - `replace`
 - `move`
 - `copy`

### Apply Json Patch

#### Jackson 2.x API
```java
JsonNode target = JsonPatch.apply(JsonNode patch, JsonNode source);
```

#### Jackson 3.x API
```java
JsonNode target = Jackson3JsonPatch.apply(JsonNode patch, JsonNode source);
```
Given a `patch`, it apply it to `source` JSON and return a `target` JSON which can be ( JSON object or array or value ). This operation  performed on a clone of `source` JSON ( thus, the `source` JSON is unmodified and can be used further). 

## To turn off MOVE & COPY Operations

### Jackson 2.x API
```java
EnumSet<DiffFlags> flags = DiffFlags.dontNormalizeOpIntoMoveAndCopy().clone();
JsonNode patch = JsonDiff.asJson(JsonNode source, JsonNode target, flags);
```

### Jackson 3.x API
```java
EnumSet<DiffFlags> flags = DiffFlags.dontNormalizeOpIntoMoveAndCopy().clone();
JsonNode patch = Jackson3JsonDiff.asJson(JsonNode source, JsonNode target, flags);
```

### Example
First Json
```json
{"a": 0,"b": [1,2]}
```

Second json ( the json to obtain )
```json
 {"b": [1,2,0]}
```
Following patch will be returned:
```json
[{"op":"move","from":"/a","path":"/b/2"}]
```
here `"op"` specifies the operation (`"move"`), `"from"` specifies the path from where the value should be moved, and  `"path"` specifies where value should be moved. The value that is moved is taken as the content at the `"from"` path.

### Extended JSON Pointer Example
JSON
```json
{
  "a": [
    {
      "id": 1,
      "data": "abc"
    },
    {
      "id": 2,
      "data": "def"
    }
  ]
}
```

JSON path
```jsonpath
/a/id=2/data
```

Following JSON would be returned
```json
"def"
```

### Apply Json Patch In-Place

#### Jackson 2.x API
```java
JsonPatch.applyInPlace(JsonNode patch, JsonNode source);
```

#### Jackson 3.x API
```java
Jackson3JsonPatch.applyInPlace(JsonNode patch, JsonNode source);
```
Given a `patch`, it will apply it to the `source` JSON mutating the instance, opposed to `JsonPatch.apply` which returns 
a new instance with the patch applied, leaving the `source` unchanged.

This is an extension to the RFC, and has some additional limitations. Specifically, the source document cannot be fully change in place (the Jackson APIs do not support that level of mutability). This means the following operations are not supported:
* `remove` with an empty or root path;
* `replace` with an empty or root path;
* `move`, `add` or `copy` targeting an empty or root path. 

### Tests:
1. 100+ selective hardcoded different input JSONs , with their driver test classes present under /test directory.
2. Apart from selective input, a deterministic random JSON generator is present under ( TestDataGenerator.java ),  and its driver test class method is JsonDiffTest.testGeneratedJsonDiff().
3. Full Jackson 3.x compatibility test suite with Jackson3* test classes that reflect all Jackson 2.x functionality.


#### *** Tests can only show presence of bugs and not their absence ***
