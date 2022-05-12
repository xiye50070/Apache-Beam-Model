#### 读写mysql所有表语句 
``` sql
-- auto-generated definition
   create table TestBeam
   (
   id   varchar(50) not null
   primary key,
   name varchar(50) null
   )
   charset = utf8;
   
   
-- auto-generated definition
create table TestBeamCount
(
    name  varchar(50) not null
        primary key,
    count varchar(50) null
)
    charset = utf8;

```