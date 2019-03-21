CREATE TABLE IF NOT EXISTS PUBLIC.draw_word
(
  id       integer primary key auto_increment,
  word     varchar(100) NOT NULL UNIQUE,
  word_tip varchar(100) NOT NULL
);
