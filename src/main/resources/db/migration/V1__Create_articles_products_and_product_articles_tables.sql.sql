CREATE TABLE articles
(
    id    BIGINT PRIMARY KEY NOT NULL ,
    name  VARCHAR(255) NOT NULL
);

CREATE TABLE articles_inventory
(
    article_id BIGINT NOT NULL ,
    warehouse_id BIGINT NOT NULL,
    quantity      INT NOT NULL,
    version       BIGINT NOT NULL,
    PRIMARY KEY (warehouse_id, article_id),
    FOREIGN KEY (article_id) REFERENCES articles (id) ON DELETE CASCADE
);

CREATE TABLE products
(
    id    BIGSERIAL PRIMARY KEY NOT NULL,
    name  VARCHAR(255) NOT NULL
);

CREATE TABLE product_articles
(
    product_id BIGINT NOT NULL,
    article_id BIGINT NOT NULL,
    quantity   INT NOT NULL,
    PRIMARY KEY (product_id, article_id),
    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    FOREIGN KEY (article_id) REFERENCES articles (id)
);