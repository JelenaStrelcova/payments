insert into account (ident_account, number) values (nextval('seq_account'), 'ACCOUNT_A');
insert into account (ident_account, number) values (nextval('seq_account'), 'ACCOUNT_B');
insert into account (ident_account, number) values (nextval('seq_account'), 'ACCOUNT_C');
insert into account (ident_account, number) values (nextval('seq_account'), 'ACCOUNT_D');

insert into currency (ident_currency, code) values (nextval('seq_currency'), 'EUR');
insert into currency (ident_currency, code) values (nextval('seq_currency'), 'USD');
insert into currency (ident_currency, code) values (nextval('seq_currency'), 'GBP');

insert into accountbalance (ident_accountbalance, ident_account, ident_currency, amount, current, validfrom) values (nextval('seq_accountbalance'), (select ident_account from account where number = 'ACCOUNT_A'), (select ident_currency from currency where code = 'EUR'), 1000, true, CURRENT_TIMESTAMP());
insert into accountbalance (ident_accountbalance, ident_account, ident_currency, amount, current, validfrom) values (nextval('seq_accountbalance'), (select ident_account from account where number = 'ACCOUNT_A'), (select ident_currency from currency where code = 'USD'), 1000, true, CURRENT_TIMESTAMP());
insert into accountbalance (ident_accountbalance, ident_account, ident_currency, amount, current, validfrom) values (nextval('seq_accountbalance'), (select ident_account from account where number = 'ACCOUNT_A'), (select ident_currency from currency where code = 'GBP'), 1000, true, CURRENT_TIMESTAMP());