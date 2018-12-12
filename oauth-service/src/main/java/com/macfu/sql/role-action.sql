drop database if exists macfussm;
create table macfussm character set utf8;
use macfussm;
create table member(
 mid varchar(64) not null,
 name varchar(32),
 password varchar(32),
 locked int,
 constraint pk_mid primary key(mid)
) engine='innodb';

create table role(
 rid varchar(64),
 title varchar(256),
 constraint pk_rid primary key(rid)
) engine='innodb';

create table action(
 actid varchar(64),
 title varchar(256),
 rid varchar(64),
 constraint pk_actid primary key(actid),
 constraint fk_rid1 foreign key(rid) references role(rid)
) engine='innodb';

create table member_role(
 mid varchar(64),
 rid varchar(64),
 constraint fk_mid2 foreign key(mid) references member(mid),
 constraint fk_rid2 foreign key(rid) references role(rid)
) engine='innodb';

-- 0表示活跃,1标识锁定
insert into member(mid,name,password,locked) vlaues ('admin','管理员','EAB62A7769F0313F8D69CEBA32F4347E',0);
insert into member(mid,name,password,locked) vlaues ('macfu','普通人','EAB62A7769F0313F8D69CEBA32F4347E',0);
insert into member(mid,name,password,locked) vlaues ('mermaid','美人','EAB62A7769F0313F8D69CEBA32F4347E',1);

-- 定义角色信息
insert into role(rid,title) values ('member','用户管理');
insert into role(rid,title) values ('dept','部分管理');
insert into role(rid,title) values ('news','新闻管理');

-- 定义权限信息
insert into action(actid, title, rid) values ('member:add','用户追加','member');
insert into action(actid, title, rid) values ('member:list','用户列表','member');
insert into action(actid, title, rid) values ('member:edit','用户编辑','member');
insert into action(actid, title, rid) values ('member:remove','用户删除','member');
insert into action(actid, title, rid) values ('dept:list','部分列表','dept');
insert into action(actid, title, rid) values ('dept:edit','部分编辑','dept');
insert into action(actid, title, rid) values ('news:add','新闻追加','news');
insert into action(actid, title, rid) values ('news:edit','新闻修改','news');
insert into action(actid, title, rid) values ('news:audit','新闻审核','news');
insert into action(actid, title, rid) values ('news:list','新闻列表','news');

-- 定义用户和角色的关系
insert into member_role(mid, rid) values ('admin','member');
insert into member_role(mid, rid) values ('admin','dept');
insert into member_role(mid, rid) values ('admin','news');
insert into member_role(mid, rid) values ('macfu','news');
insert into member_role(mid, rid) values ('mermaid','dept');