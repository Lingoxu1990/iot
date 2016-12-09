#! /bin/bash  
#chmod +x ./ok.sh
source ~/.bashrc
#获取当前脚本所在目录路径
MPATH=`dirname $0`
#获取输入的第一个参数
ARG=$1
#如果没输入参数，提示数据参数test或prod,test代表测试环境，prod代表正式环境
if [ $# != 1 ];then
    echo “input parameter:test or prod”
    exit 1;
fi
#初始化路径参数
TOMCAT=$(cd `dirname $0`;cd ..;cd ..;pwd)
FILEPATH=$TOMCAT/webapps/demo
CLASSES=$FILEPATH/WEB-INF/classes
#maven编译
mvn package
#编译后的目录复制到tomcat下
\cp -rf $MPATH/target/demo/* $FILEPATH/
#根据参数更换正确的配置文件
mv $CLASSES/config.$ARG.properties $CLASSES/config.properties
mv $CLASSES/jdbc.$ARG.properties $CLASSES/jdbc.properties
imv $CLASSES/log4j.$ARG.properties $CLASSES/log4j.properties
#启动tomcat#
sh /home/tomcat/bin/catalina.sh run
#bash
