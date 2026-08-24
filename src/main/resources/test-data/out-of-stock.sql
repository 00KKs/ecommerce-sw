INSERT INTO member (id, email, password_hash, name)
VALUES (1, 'test@example.com', 'hash', '홍길동');

INSERT INTO address (id, member_id, recipient_name, recipient_phone, address, delivery_request, is_default)
VALUES (1, 1, '홍길동', '010-1234-5678', '서울시 강남구 테헤란로', '문 앞에 놔주세요', true);

INSERT INTO product (id, category_id, name, description, sale_status) VALUES (10, 4, '한정판 후드', '한정 수량', 'SELLING');

INSERT INTO sku (id, product_id, option_name, price, status) VALUES (100, 10, '그레이 / M', 50000, 'SELLING');

INSERT INTO stock (sku_id, quantity) VALUES (100, 1);