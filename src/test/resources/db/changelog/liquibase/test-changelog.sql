--liquibase formatted sql

--changeset dev:create-orders
CREATE TABLE orders (id INT PRIMARY KEY, product VARCHAR(100), quantity INT);

--rollback DROP TABLE orders;

--changeset dev:insert-order
INSERT INTO orders (id, product, quantity) VALUES (1, 'Widget', 10);

--rollback DELETE FROM orders WHERE id = 1;
