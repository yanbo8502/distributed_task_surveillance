提供组件（java），以jar包的形式，可以引入工程内部调用其内部接口，也可以在命令行单独执行。
1.任务日志记录和计算处理组件（已完成）
记录和处理的一些基本逻辑和规范是一致的，因此开发了一个组件模块，打jar包后上传到nexus私服，可以在单独的监控程序中引入，也可以在其他java工程中依赖。
maven 引用：
<dependency>
<groupId>com.yanbo.task_surv</groupId>
<artifactId>task_stats_module</artifactId>
<version>0.0.1-SNAPSHOT</version>
</dependency>
要检查最新版本，可从大数据部nexus私服上搜索：

使用方法：
日志统计功能：
见下3.
 
日志记录功能：
下2是一种；
直接在java代码里引用也是一种，代码范例：
//TaskStatus task_status = new TaskStatus();//使用默认的表task_tracking_db.task_stage_status
TaskStatus task_status = new TaskStatus(db_name, collection_name);
task_status.setTaskStatus(task_id, stage, status, description)
 
2.日志记录可执行程序：（已完成）
每个阶段在开始/完成时，都要进行记录。
输入参数为每个阶段每次计算的id，任务状态（0失败，1成功，2正在进行），记录日志的表名（一般是针对这个阶段的表），日志描述。
然后组件会有默认数据库（也可以外部指定），把输入信息连同当前时间一起写入表。id具有唯一性，每次记录都是一个写入更新操作。

 
 
部署方式：在需要调用它的母服务所在的机器上，要有这个可执行jar包
这个使用时，应该统一编译成可执行jar包然后分发到需要执行的机器的固定位置上。
 
3：日志分析处理可执行程序（一次性/定时：（已完成）
id的规则首先要保证在本阶段的任务标识唯一性，一个流程的不同阶段所用的id可能不一样，只要各阶段的开发人员在id规则上有共识，id尽量带上一些特征信息，就能在不同阶段的日志表里把一次人群流程内容串联起来。
组件可以在一台机器上定时运行，增量扫描数据库，做即时统计以及每天/每周的日报工作。

 
部署方式：
这个在一台机器上统一部署即可。

