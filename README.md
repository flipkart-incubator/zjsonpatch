# This is an implementation of  [RFC 6902 JSON Patch](http://tools.ietf.org/html/rfc6902) written in Java.

##Description & Use-Cases
- Java Library to find / apply JSON Patches according to RFC 6902.
- JSON Patch defines a JSON document structure for representing changes to a JSON document.
- It can be used to avoid sending a whole document when only a part has changed, thus reducing network bandwidth requirements if data (in json format) is required to send across multiple systems over network or in case of multi DC transfer.
- When used in combination with the HTTP PATCH method as per [RFC 5789 HTTP PATCH](http://tools.ietf.org/html/rfc5789), it will do partial updates for HTTP APIs in a standard  way.


###Compatible with : Java 6 / 7 / 8

##Code Coverage
Package      |	Class, % 	 |  Method, % 	   |  Line, %           |
-------------|---------------|-----------------|--------------------|
all classes  |	100% (6/ 6)  |	93.6% (44/ 47) |  96.2% (332/ 345)  |

##Complexity
- To find JsonPatch : Ω(N+M) ,N and M represnets number of keys in first and second json respectively / O(summation of la*lb) where la , lb represents jsonArray of length la / lb of against same key in first and second json ,since LCS is used to find difference between 2 json arrays there of order of quadratic.
- To Optimize Diffs ( compact move and remove into Move ) : Ω(D) / O(D*D) where D represents number of diffs obtained before compaction into Move operation.
- To Apply Diff : O(D) where D represents number of diffs

### How to use:

###Current Version : 0.2.1

Add following to `<repositories/>` section of pom.xml -
```xml
<repository>
  <id>clojars</id>
  <name>Clojars repository</name>
  <url>https://clojars.org/repo</url>
</repository>
```
Add following to `<dependencies/>` section of your pom.xml -

```xml
<groupId>com.flipkart.zjsonpatch</groupId>
<artifactId>zjsonpatch</artifactId>
<version>{version}</version>
```

[![Clojars Project](http://clojars.org/com.flipkart.zjsonpatch/zjsonpatch/latest-version.svg)](http://clojars.org/com.flipkart.zjsonpatch/zjsonpatch)


## API Usage

### Obtaining Json Diff as patch
```xml
JsonNode patch = JsonDiff.asJson(JsonNode source, JsonNode target)
```
Computes and returns a JSON Patch from source  to target,
Both source and target must be either valid JSON objects or  arrays or values. 
Further, if resultant patch is applied to source, it will yield target.

The algorithm which computes this JsonPatch currently generates following operations as per rfc 6902 - 
 - ADD
 - REMOVE
 - REPLACE
 - MOVE
 

### Apply Json Patch
```xml
JsonPatch target = JsonPatch.apply(JsonNode patch, JsonNode source);
```
Given a Patch, it apply it to source Json and return a target json which can be ( json object or array or value ). This operation  performed on a clone of source json ( thus, source json is untouched and can be used further). 

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
[{"op":"MOVE","from":"/a","path":"/b/2","value":0}]
```
here o represents Operation, p represent fromPath from where value should be moved, tp represents toPath where value should be moved and v represents value to move.


### Tests:
1. 100+ selective hardcoded different input jsons , with their driver test classes present under /test directory.
2. Apart from selective input, a deterministic random json generator is present under ( TestDataGenerator.java ),  and its driver test class method is JsonDiffTest.testGeneratedJsonDiff().



