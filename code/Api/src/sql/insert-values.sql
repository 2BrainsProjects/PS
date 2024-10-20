insert into dbo.User(name, password_hash, email) values ('Joana', '$2a$10$j1vtulla9rWy4r0PWRnCmONQQCS.wezKP523Zx0vHpnVujzetSh9a', 'joanachuco@gmail.com', 0);
insert into dbo.User(name, password_hash, email) values ('Diogo', '$3c$82$OIADHBoidsubinpounaui.DAHNS0d7h0a7h0h90asd9a/Jysa/sle', 'Diogo@gmail.com');

delete from dbo.Router;
delete from dbo.User where name = 'Constanca';


select * from dbo.Router;
select * from dbo.User;
