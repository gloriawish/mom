#!/bin/sh

mvn clean
mvn assembly:assembly
mv ./target/*.jar ./target/lib/
