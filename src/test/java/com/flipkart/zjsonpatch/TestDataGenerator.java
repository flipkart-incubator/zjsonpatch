/*
 * Copyright 2016 flipkart.com zjsonpatch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * User: gopi.vishwakarma
 * Date: 05/08/14
 */
public class TestDataGenerator {
    private static Random random = new Random();
    private static List<String> name = Arrays.asList("summers", "winters", "autumn", "spring", "rainy");
    private static List<Integer> age = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    private static List<String> gender = Arrays.asList("male", "female");
    private static List<String> country = Arrays.asList("india", "aus", "nz", "sl", "rsa", "wi", "eng", "bang", "pak");
    private static List<String> friends = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j");

    public static JsonNode generate(int count) {
        ArrayNode jsonNode = JsonNodeFactory.instance.arrayNode();
        for (int i = 0; i < count; i++) {
            ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
            objectNode.put("name", name.get(random.nextInt(name.size())));
            objectNode.put("age", age.get(random.nextInt(age.size())));
            objectNode.put("gender", gender.get(random.nextInt(gender.size())));
            ArrayNode countryNode = getArrayNode(country.subList(random.nextInt(country.size() / 2), (country.size() / 2) + random.nextInt(country.size() / 2)));
            objectNode.set("country", countryNode);
            ArrayNode friendNode = getArrayNode(friends.subList(random.nextInt(friends.size() / 2), (friends.size() / 2) + random.nextInt(friends.size() / 2)));
            objectNode.set("friends", friendNode);
            jsonNode.add(objectNode);
        }
        return jsonNode;
    }

    private static ArrayNode getArrayNode(List<String> args) {
        ArrayNode countryNode = JsonNodeFactory.instance.arrayNode();
        for(String arg : args){
            countryNode.add(arg);
        }
        return countryNode;
    }
}
