DROP TABLE IF EXISTS category_closure;
DROP TABLE IF EXISTS category;

CREATE TABLE category
(
    id   BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE category_closure
(
    ancestor   BIGINT NOT NULL,
    descendant BIGINT NOT NULL,
    depth      INT    NOT NULL,
    PRIMARY KEY (ancestor, descendant),
    CONSTRAINT fk_closure_ancestor FOREIGN KEY (ancestor) REFERENCES category (id),
    CONSTRAINT fk_closure_descendant FOREIGN KEY (descendant) REFERENCES category (id)
);