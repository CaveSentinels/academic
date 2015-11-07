use inventory;


DROP TABLE IF EXISTS `users`;
CREATE TABLE users
(
UserID int NOT NULL UNIQUE AUTO_INCREMENT,
FirstName varchar(255),
LastName varchar(255),
UserName varchar(30) NOT NULL UNIQUE,
Pwd CHAR(32) NOT NULL,
Inventory BOOLEAN NOT NULL default 0,
Orders BOOLEAN NOT NULL  default 0,
Shipping BOOLEAN NOT NULL default 0,
PRIMARY KEY (UserID)
);

INSERT INTO users(FirstName, LastName, UserName, Pwd, Inventory, Orders, Shipping)
VALUES('Sankalp', 'Anand', 'sankalpa1', '1dc231b18fd6761ebc49f2ed5114bb0f', 1, 1, 1);


INSERT INTO users(FirstName, LastName, UserName, Pwd, Inventory, Orders, Shipping)
VALUES('Sankalp', 'Anand', 'sankalpa2', '1dc231b18fd6761ebc49f2ed5114bb0f', 1, 0, 0);

INSERT INTO users(FirstName, LastName, UserName, Pwd, Inventory, Orders, Shipping)
VALUES('Sankalp', 'Anand', 'sankalpa3', '1dc231b18fd6761ebc49f2ed5114bb0f', 0, 1, 0);

INSERT INTO users(FirstName, LastName, UserName, Pwd, Inventory, Orders, Shipping)
VALUES('Sankalp', 'Anand', 'sankalpa4', '1dc231b18fd6761ebc49f2ed5114bb0f', 0, 0, 1);



CREATE TABLE IF NOT EXISTS logs
(
	UserName varchar(30) NOT NULL,
	Activity VARCHAR(30) NOT NULL,
	Time VARCHAR(50) NOT NULL
);




