-- Core tables for a brand-new database. Existing databases already have these,
-- so CREATE TABLE IF NOT EXISTS does nothing there.
-- google_id and auth_provider are added later by V4__user_auth_providers.sql.
-- Progress tables are created by V2__user_progress.sql.

CREATE TABLE IF NOT EXISTS users (
    id CHAR(36) PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    avatar_url VARCHAR(255),
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    last_login TIMESTAMP NULL,
    UNIQUE KEY uk_users_username (username),
    UNIQUE KEY uk_users_email (email)
);

CREATE TABLE IF NOT EXISTS courses (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    hsk_level VARCHAR(20) NOT NULL,
    description TEXT,
    difficulty VARCHAR(20),
    cover_image_url VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    created_by CHAR(36),
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    published_at TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS units (
    id CHAR(36) PRIMARY KEY,
    course_id CHAR(36) NOT NULL,
    unit_number INT NOT NULL,
    urdu_title VARCHAR(100) NOT NULL,
    hanzi_title VARCHAR(100) NOT NULL,
    grammar_point TEXT,
    hsk_level VARCHAR(20),
    estimated_time INT,
    difficulty VARCHAR(20),
    status VARCHAR(20) NOT NULL,
    version INT,
    published_at TIMESTAMP NULL,
    created_by CHAR(36),
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS lessons (
    id CHAR(36) PRIMARY KEY,
    unit_id CHAR(36) NOT NULL,
    lesson_number INT NOT NULL,
    lesson_type VARCHAR(20) NOT NULL,
    urdu_title VARCHAR(100) NOT NULL,
    instruction_text TEXT,
    difficulty VARCHAR(20),
    crowns INT,
    status VARCHAR(20) NOT NULL,
    exercises_count INT,
    words_count INT,
    created_by CHAR(36),
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS exercises (
    id CHAR(36) PRIMARY KEY,
    lesson_id CHAR(36) NOT NULL,
    exercise_type VARCHAR(50) NOT NULL,
    exercise_data JSON NOT NULL,
    exercise_order INT NOT NULL,
    created_by CHAR(36),
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS vocabulary (
    id CHAR(36) PRIMARY KEY,
    hanzi VARCHAR(50) NOT NULL,
    pinyin VARCHAR(100) NOT NULL,
    tone INT,
    urdu_translation TEXT NOT NULL,
    roman_urdu VARCHAR(100),
    literal_gloss TEXT,
    part_of_speech VARCHAR(50),
    hsk_level INT,
    frequency INT,
    stroke_count INT,
    radical VARCHAR(50),
    audio_male_url VARCHAR(255),
    audio_female_url VARCHAR(255),
    illustration_url VARCHAR(255),
    created_by CHAR(36),
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS example_sentences (
    id CHAR(36) PRIMARY KEY,
    vocabulary_id CHAR(36) NOT NULL,
    hanzi TEXT NOT NULL,
    pinyin TEXT NOT NULL,
    urdu TEXT NOT NULL,
    created_at TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS unit_topics (
    unit_id CHAR(36) NOT NULL,
    topic VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS unit_objectives (
    unit_id CHAR(36) NOT NULL,
    objective TEXT
);

CREATE TABLE IF NOT EXISTS vocabulary_topics (
    vocabulary_id CHAR(36) NOT NULL,
    topic VARCHAR(255)
);
