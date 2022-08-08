#!/usr/bin/env bash

#cd ..
#mvn clean package
#cd gvm-native
~/Downloads/graalvm-ce-java17-22.2.0/bin/native-image -jar \
  ../server/target/aos2-server-*-jar-with-dependencies.jar \
  --no-fallback -o aos2-server-native \
  -H:ReflectionConfigurationFiles=gvm-reflect-config.json \
  -H:IncludeResources=".*default-config.*yaml$" \
  -H:IncludeResources=".*redfort.*wld$"