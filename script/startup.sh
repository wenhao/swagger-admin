#!/bin/sh

# 执行文件名称
app_name="swagger-admin"

# 先关闭服务
sh shutdown.sh
# --server.port：启动端口
nohup java -jar -Xms128m -Xmx128m $app_name.jar --server.port=6970 &
