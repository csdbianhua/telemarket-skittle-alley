CREATE TABLE PUBLIC.draw_word
(
  id       INTEGER PRIMARY KEY AUTO_INCREMENT,
  word     varchar(100) NOT NULL UNIQUE,
  word_tip varchar(100) NOT NULL
);
