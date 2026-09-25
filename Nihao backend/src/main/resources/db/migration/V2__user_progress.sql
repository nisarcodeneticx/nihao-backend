-- Per-user learning progress tables

CREATE TABLE IF NOT EXISTS user_profiles (
    user_id CHAR(36) PRIMARY KEY,
    total_xp INT NOT NULL DEFAULT 0,
    level INT NOT NULL DEFAULT 1,
    streak_days INT NOT NULL DEFAULT 0,
    last_activity_date DATE,
    total_crowns INT NOT NULL DEFAULT 0,
    words_learned INT NOT NULL DEFAULT 0,
    completed_lessons INT NOT NULL DEFAULT 0,
    current_course_id CHAR(36),
    current_unit_number INT,
    current_lesson_number INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_course_progress (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    course_id CHAR(36) NOT NULL,
    is_unlocked BOOLEAN NOT NULL DEFAULT TRUE,
    progress_percent FLOAT NOT NULL DEFAULT 0,
    completed_units INT NOT NULL DEFAULT 0,
    completed_words INT NOT NULL DEFAULT 0,
    total_crowns INT NOT NULL DEFAULT 0,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_course (user_id, course_id)
);

CREATE TABLE IF NOT EXISTS user_unit_progress (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    unit_id CHAR(36) NOT NULL,
    state VARCHAR(20) NOT NULL DEFAULT 'locked',
    is_unlocked BOOLEAN NOT NULL DEFAULT FALSE,
    completed_lessons INT NOT NULL DEFAULT 0,
    completed_items INT NOT NULL DEFAULT 0,
    crowns INT NOT NULL DEFAULT 0,
    checkpoint_completed BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (unit_id) REFERENCES units(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_unit (user_id, unit_id)
);

CREATE TABLE IF NOT EXISTS user_lesson_progress (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    lesson_id CHAR(36) NOT NULL,
    state VARCHAR(20) NOT NULL DEFAULT 'locked',
    is_complete BOOLEAN NOT NULL DEFAULT FALSE,
    earned_crowns INT NOT NULL DEFAULT 0,
    completed_items INT NOT NULL DEFAULT 0,
    xp_earned INT NOT NULL DEFAULT 0,
    completed_at TIMESTAMP NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_lesson (user_id, lesson_id)
);

CREATE INDEX idx_user_course_progress_user ON user_course_progress(user_id);
CREATE INDEX idx_user_unit_progress_user ON user_unit_progress(user_id);
CREATE INDEX idx_user_lesson_progress_user ON user_lesson_progress(user_id);
