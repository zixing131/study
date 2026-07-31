CREATE TABLE IF NOT EXISTS chinese_character (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    char_text     TEXT    NOT NULL,
    pinyin        TEXT    NOT NULL,
    stroke_order  TEXT    NOT NULL,
    words         TEXT    NOT NULL,
    sentence      TEXT    NOT NULL,
    sort_order    INTEGER NOT NULL DEFAULT 0,
    created_at    TEXT    DEFAULT (datetime('now','localtime'))
);

CREATE TABLE IF NOT EXISTS poem (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    title         TEXT    NOT NULL,
    author        TEXT    NOT NULL,
    dynasty       TEXT    DEFAULT '',
    lines_json    TEXT    NOT NULL,
    sort_order    INTEGER NOT NULL DEFAULT 0,
    created_at    TEXT    DEFAULT (datetime('now','localtime'))
);

CREATE TABLE IF NOT EXISTS english_word (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    word          TEXT    NOT NULL,
    phonetic      TEXT    DEFAULT '',
    meaning       TEXT    NOT NULL,
    category      TEXT    NOT NULL,
    example       TEXT    DEFAULT '',
    emoji         TEXT    DEFAULT '',
    sort_order    INTEGER NOT NULL DEFAULT 0,
    created_at    TEXT    DEFAULT (datetime('now','localtime'))
);
