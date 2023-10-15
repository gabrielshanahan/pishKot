CREATE TABLE posts (
    name VARCHAR(255) PRIMARY KEY,
    contents TEXT NOT NULL,
    url TEXT
);

CREATE TABLE post_dependencies (
    post_name VARCHAR(255),
    dependency_name VARCHAR(255),
    FOREIGN KEY (post_name) REFERENCES posts(name),
    FOREIGN KEY (dependency_name) REFERENCES posts(name)
);