
presto-cli/target/presto-cli-0.279-SNAPSHOT-executable.jar

SHOW CATALOGS;
SHOW SCHEMAS from bi_mysql_bi;
SHOW TABLES from bi_mysql_bi.yiibaidb;

DESCRIBE bi_mysql_bi.yiibaidb.orders;
SELECT * FROM bi_mysql_bi.yiibaidb.orders;
SELECT count(*) AS count,MAX(ordernumber) AS max FROM bi_mysql_bi.yiibaidb.orders GROUP BY status;