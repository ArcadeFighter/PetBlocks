CREATE TABLE IF NOT EXISTS SHY_PLAYER
(
  id INTEGER PRIMARY KEY AUTO_INCREMENT,
  uuid CHAR (36
) UNIQUE NOT NULL,
name VARCHAR (16
) NOT NULL,
CONSTRAINT unique_uuid_cs UNIQUE (uuid
)
);

CREATE TABLE IF NOT EXISTS SHY_SKIN
(
  id INTEGER PRIMARY KEY AUTO_INCREMENT,
  typename VARCHAR (32
) NOT NULL,
  owner TEXT,
  datavalue INTEGER,
  unbreakable INTEGER
);

CREATE TABLE IF NOT EXISTS SHY_PET
(
  id INTEGER PRIMARY KEY AUTO_INCREMENT,
  shy_player_id INTEGER,
  shy_skin_id INTEGER,
  enabled INTEGER,
  displayname TEXT NOT NULL,
  soundenabled INTEGER,
  particleenabled INTEGER,
CONSTRAINT foreignkey_player_id_cs FOREIGN KEY (shy_player_id
) REFERENCES SHY_PLAYER (id
),
CONSTRAINT foreignkey_skin_id_cs FOREIGN KEY (shy_skin_id
) REFERENCES SHY_SKIN (id
)
);

CREATE TABLE IF NOT EXISTS SHY_PET_AI
(
  id INTEGER PRIMARY KEY AUTO_INCREMENT,
  shy_pet_id INTEGER,
  typename VARCHAR (32
) NOT NULL,
  content TEXT,
CONSTRAINT foreignkey_pet_ai_id_cs FOREIGN KEY (shy_pet_id
) REFERENCES SHY_PET (id
)
)

