# swagger-admin

一个Swagger文档管理后台，可统一管理多个项目的Swagger文档

- 使用简单，只需要一个Java8环境，下载后即可运行使用
- 支持文档搜索，方便过滤文档
- 支持树形表格展示复杂参数
- 接口调试，支持文件上传、下载

> 用到的技术：SpringBoot + Mybatis + Vue

## 使用步骤

- 前往[发行版页面](https://gitee.com/durcframework/swagger-admin/releases)，下载最新版本zip文件
- 解压zip，如果是Mac/Linux操作系统，运行`startup.sh`文件启动，Windows操作系统运行cmd输入`java -jar swagger-admin.jar`启动
- 浏览器访问`http://localhost:6970/`


默认端口是6970，更改端口号按如下方式：

- Mac/Linux操作系统：打开`startup.sh`文件，修改`--server.port`参数值
- Windows操作系统：可执行：`java -jar swagger-admin.jar --server.port=端口号`

## 工程说明

- front：前端vue
- admin：后端服务
- db：数据库初始化文件
- script：辅助脚本

## 自主构建

> 需要安装Maven3，Java8

- 自动构建[推荐]：

Mac/Linux系统可直接执行`build.sh`进行构建，构建结果在`dist`文件夹下。

- 手动构建：
    
    `cd front`
    
    - 执行`npm run build:prod`进行打包，结果在dist下
    - 把dist中的所有文件，放到`admin/src/main/resources/public`下
    
    `cd ..`
    
    - 执行`mvn clean package`，在`gen/target`下会生成一个`swagger-admin-xx-SNAPSHOT.jar`（xx表示本号）
    - 执行`java -jar swagger-admin-xx-SNAPSHOT.jar`
    - 浏览器访问`http://localhost:6970/`

## 效果图

![预览图](https://images.gitee.com/uploads/images/2020/0908/143445_bd590c74_332975.png "preview1.png")


![预览图](https://images.gitee.com/uploads/images/2020/0908/143505_c33b6e40_332975.png "preview2.png")