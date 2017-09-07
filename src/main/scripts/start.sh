#!/bin/bash
set SUBTITLE_ASSISTANT_HOME=`pwd`
if [ -n "`lsof -i:9607`" ]; then
    echo 已有服务占用9607端口,服务可能已启动
    exit
fi
jar_name=skittle-alley.jar
if [ -z "${jar_name}" ]; then
    echo 不存在服务主文件,请检查文件完整性
    exit
fi
java -jar ${jar_name}