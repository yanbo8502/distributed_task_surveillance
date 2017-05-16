#!/bin/bash
#这是程序自动部署的模板
server_host=$1
deploy_root_dir=/data/dist/task_surv_dir
service_name='task_surv_service'
binFile="task_stats-0.0.1-SNAPSHOT.jar"

####更新子部署环境上的服务####
ssh root@$server_host 'ldconfig'

#在部署环境中创建部署所用的文件夹路径
ssh root@$server_host "if [ ! -d /data ];then mkdir /data; fi"
ssh root@$server_host "if [ ! -d /data/dist ];then mkdir /data/dist; fi"
ssh root@$server_host "if [ ! -d $deploy_root_dir ];then mkdir $deploy_root_dir; fi"

#把执行服务需要的文件部署到目标路径
scp check.sh root@$server_host:$deploy_root_dir

#更新rest服务脚本，然后重新启动服务
scp $service_name root@$server_host:/etc/init.d
scp $service_name root@$server_host:$deploy_root_dir
ssh root@$server_host "chmod a+x /etc/init.d/${service_name}"

#先停掉服务
ssh root@$server_host "echo true > ${deploy_root_dir}/deploying"
ssh root@$server_host "service ${service_name} stop"

#更新可执行文件
sleep 3
scp target/$binFile root@$server_host:$deploy_root_dir

#重启服务
ssh root@$server_host "sh $deploy_root_dir/${service_name} restart" &
sleep 3
ssh root@$server_host "rm -f $deploy_root_dir/deploying"