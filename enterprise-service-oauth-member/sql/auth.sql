drop database if exists macfuoauthauthz;
create table macfuoauthauthz character set utf8;
use macfuoauthauthz;
create table member(
 mid varchar(64) not null,
 name varchar(32),
 password varchar(32),
 locked int,
 constraint pk_mid primary key(mid)
) engine='innodb';

-- 0表示活跃,1标识锁定
insert into member(mid,name,password,locked) vlaues ('admin','管理员','EAB62A7769F0313F8D69CEBA32F4347E',0);
insert into member(mid,name,password,locked) vlaues ('macfu','普通人','EAB62A7769F0313F8D69CEBA32F4347E',0);
insert into member(mid,name,password,locked) vlaues ('mermaid','美人','EAB62A7769F0313F8D69CEBA32F4347E',1);
