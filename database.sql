ALTER TABLE ebooks.books
    ADD COLUMN description TEXT;

ALTER TABLE books
    CHANGE COLUMN author updater VARCHAR(255); -- 或者使用相应的文本类型

TRUNCATE TABLE books_category;
TRUNCATE TABLE categories;

INSERT INTO categories (name, create_time, update_time, is_deleted)
VALUES
    ('Computer', NOW(), NOW(), 0),
    ('Finance', NOW(), NOW(), 0),
    ('Other', NOW(), NOW(), 0);
