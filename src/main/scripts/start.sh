#!/usr/bin/env bash
nohup java -XX:+UseG1GC -Xmx1g -Xms1g -XX:+HeapDumpOnOutOfMemoryError -jar skittle-alley.jar >/dev/null 2>&1 &