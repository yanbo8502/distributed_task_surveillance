
#!/bin/bash
# File: check.sh
# PATH: /data/dist/task_surv_dir
source /etc/profile
host_raw=`ifconfig -a|grep inet|grep -v 127.0.0.1|grep -v inet6|awk '{print $2}'|tr -d "addr:"|tr -d "地址:"`
host=`echo ${host_raw}`
host=${host// /@}
dir="/data/dist/task_surv_dir"
binFile="task_stats-0.0.1-SNAPSHOT.jar"
port=0
name="audience related task-flow status surveillance"
short_name="task_surv_service"
mail_list=yanbo@le.com
checkTime=`date "+%Y-%m-%d %H:%M:%S"`
checkTimemail=`date "+%Y-%m-%d_%H:%M:%S"`
service_name='task_surv_service'


if [ -f $dir/deploying ];then
	echo $checkTime "${short_name} is under deploying status, so the process checking is ingored"
	subject="${short_name} is in deploying-status on server ${host}"
	content="${short_name} is in deploying status, thus process checking is ignored on server ${host} at ${checkTimemail}"
	python /data/dist/scripts/send_mail.py "${mail_list}" "${subject}" "${content}"	exit 0
fi
#numProcess=`lsof -i:$port | grep 'LISTEN' | wc -l`
numProcess=`ps aux | grep ${binFile} | grep -v grep | wc -l`
echo $numProcess

cd $dir
if [[ $numProcess -eq 1 ]];then
	echo $checkTime "${name} status OK"
else
	echo $checkTime "${name} status Error"
	echo $checkTime "${name} status Error" >> check.log
	cd $dir 
	sh ${dir}/${service_name} restart
	subject="${short_name} process alert on ${host} at ${checkTimemail}"
	content="${short_name} was stopped and has restarted on server ${host} at ${checkTimemail}"
	python /data/dist/scripts/send_mail.py "${mail_list}" "${subject}" "${content}"
fi


