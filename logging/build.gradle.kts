/*
 * Copyright 2022-2025 Creek Contributors (https://github.com/creek-service)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    `java-library`
}

val creekVersion : String by extra
val slf4jVersion : String by extra
val jacksonVersion : String by extra
val spotBugsVersion : String by extra

dependencies {
    implementation("org.creekservice:creek-base-annotation:$creekVersion")
    implementation("org.creekservice:creek-base-type:$creekVersion")

    implementation("org.slf4j:slf4j-api:$slf4jVersion")

    testImplementation("com.github.spotbugs:spotbugs-annotations:$spotBugsVersion")

    testImplementation("tools.jackson.core:jackson-databind:$jacksonVersion")

    // Required by Log4j when using JsonLayout (Log4j2 requires Jackson 2):
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.21.2")
    // Required by Log4j when using XmlLayout (Log4j2 requires Jackson 2):
    testRuntimeOnly("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.21.2")
}
