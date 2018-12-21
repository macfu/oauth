drop database if exists macfuoauthclient;
create database macfuoauthclient character set utf8;
use macfuoauthclient;
create table client(
 clid bigint auto_increment,
 client_id varchar(256),
 client_secret varchar(256),
 constraint pk_clid primary key(clid)
);


-- 测试数据
insert into client(client_id,client_secret) values ('c8901892-3f64-47f7-9056-4f964c648e2c','e44a029f-6074-3cc2-be50-fa76bcb193e2');