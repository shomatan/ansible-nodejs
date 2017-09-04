# --- !Ups

CREATE TABLE settings (
    setting_name   VARCHAR NOT NULL UNIQUE
  , value          VARCHAR NOT NULL
  , value_type     INT     NOT NULL
  , PRIMARY KEY (setting_name)
);

INSERT INTO settings (setting_name, value, value_type) VALUES ('title',       'Ayumi CMS!',       1);
INSERT INTO settings (setting_name, value, value_type) VALUES ('url',         'http://localhost', 1);
INSERT INTO settings (setting_name, value, value_type) VALUES ('description', 'A CMS powered by Play framework', 1);

# --- !Downs
drop table settings;