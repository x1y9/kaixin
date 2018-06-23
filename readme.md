Kaixin
全栈快速开发框架

# 运行
安装好java7+, Maven 3+, nodejs 8+, yarn

1. build migration #初始化数据库
1. build run       #运行后台
1. build dev       #运行前端admin界面（dev模式）

会自动弹出浏览器访问http://localhost:8000, 缺省登录密码是admin/admin。

# 调试
* 后端的调试直接在Eclipse或IntelliJ调试运行MainApplication.java即可。
* 前端的调试通过build dev运行后，在chrome里调试。

# 配置
配置文件是server.yml, 开发时缺省使用h2数据库，无需安装额外软件。可以通过server.yml修改http端口等设置等。

# 部署
build deploy 可以构建一个部署目录(target\deploy)出来，前端代码会构建好放在后端下，这个部署目录可以直接拷贝到别的机器运行。部署服务器可以是Windows或Linux，需要先安装好JRE和MySQL。

* 部署目录下的server.yml需要先修改下数据库配置，缺省使用本地的mysql，需要把配置里的数据库先创建出来。
* 部署目录下的startup.bat和startup.sh分别是Windows、Linux下的启动脚本，第一次运行前需要带参数执行以初始化数据库，这样做：
  
startup db migrate server.yml

* 以后再运行就直接运行startup即可

# 单文件发布
如果不想按目录部署，可以用build package把整个工程构建为一个jar，生成的jar在target目录下，将这个jar和配置文件server.yml放到任意目录下，直接运行jar即可运行服务器。
java -jar ****.jar

同样的，第一次运行前如果需要初始化数据库，可以带参数来处理，比如：

java -jar ****.jar db migrate server.yml

# 维护
开发时build有几个用于维护的命令

* build dump 可以把当前开发中的h2数据库导出为backup.sql文件，注意这个文件不能用于其他数据库
* build restore 可以恢复之前导出的sql文件
* build resetdb 重置h2数据库（删除所有数据）

服务器运行起来后，后台提供几个维护页面

* 通过http://localhost:8000/sys/dbmng, 访问内置数据管理工具.
* 通过http://localhost:8000/sys/property, 修改服务器端属性.
* 通过http://localhost:8000/sys/logger, 修改服务器端logger.
* 通过http://localhost:8000/sys/metric, 查看服务器端性能统计.

# TODO
 * 权限设计
 * grid列宽不能调整
 * File组件
 * Rich组件的图片
 * 首页的title可定制
 * 后端读写分离
 * 后端jdbc metric还是ns，需要重新封装jdbc
 * upload接口Edge上传文件名不对

# DONE 
 * Entity复制功能 
 * js debug 错行
 * list->item->list 保存位置

# LICENSE
MIT







