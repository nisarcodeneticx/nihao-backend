# -*- coding: utf-8 -*-
"""Expand HSK 1 unit 1 so the app path has 17 lessons + checkpoint (~18 badges)."""
from __future__ import annotations

import json
import subprocess
import uuid
from pathlib import Path

MYSQL = r"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
UNIT_ID = "f84c3ca8-43db-428b-bd33-260bf9928f69"
CHECKPOINT_ID = "f19c4afe-1a81-464a-b7ec-a3c23ae4f5b8"
ADMIN_ID = "df25b914-81c2-4bca-a5a3-bfb4424543af"


def uid() -> str:
    return str(uuid.uuid4())


def sql_str(value: str) -> str:
    return "'" + value.replace("\\", "\\\\").replace("'", "\\'") + "'"


def sql_json(value: dict) -> str:
    return "CAST(" + sql_str(json.dumps(value, ensure_ascii=False)) + " AS JSON)"


def teach(word: str, pinyin: str, urdu: str) -> dict:
    return {
        "exerciseType": "TEACH_FRAME",
        "exerciseData": {
            "promptUr": "اس لفظ کو سیکھیں",
            "promptEn": "Learn this word",
            "word": word,
            "items": [word],
            "answer": word,
            "pinyin": pinyin,
            "urdu": urdu,
            "gloss": [{"ur": urdu, "en": word}],
        },
    }


def choose(options: list[str], answer: str, pinyin: str, urdu: str, prompt: str, kind: str = "PICTURE_MATCH") -> dict:
    return {
        "exerciseType": kind,
        "exerciseData": {
            "promptUr": prompt,
            "promptEn": "Choose the correct word",
            "options": options,
            "items": options,
            "answer": answer,
            "word": answer,
            "pinyin": pinyin,
            "urdu": urdu,
            "gloss": [{"ur": urdu, "en": answer}],
        },
    }


def tone(word: str, pinyin: str, urdu: str, tone_num: int) -> dict:
    return {
        "exerciseType": "TONE_DRILL",
        "exerciseData": {
            "promptUr": "صحیح ٹون چنیں",
            "promptEn": "Choose the correct tone",
            "word": word,
            "items": [word],
            "answer": f"{word}{tone_num}",
            "pinyin": pinyin,
            "urdu": urdu,
            "gloss": [{"ur": urdu, "en": word}],
        },
    }


def build(items: list[str], answer_parts: list[str], urdu: str) -> dict:
    return {
        "exerciseType": "TAP_TO_BUILD",
        "exerciseData": {
            "promptUr": "صحیح ترتیب میں ٹیپ کریں",
            "promptEn": "Tap in the correct order",
            "items": items,
            "options": items,
            "answer": " ".join(answer_parts),
            "urdu": urdu,
            "gloss": [{"ur": urdu, "en": "".join(answer_parts)}],
        },
    }


LESSONS = [
    (
        3,
        "NORMAL",
        "Easy",
        "آپ کیسے ہیں؟",
        "حال احوال پوچھنا سیکھیں: 你好吗؟",
        4,
        [
            teach("你好吗", "nǐ hǎo ma", "آپ کیسے ہیں؟"),
            teach("我很好", "wǒ hěn hǎo", "میں ٹھیک ہوں"),
            teach("很", "hěn", "بہت"),
            teach("吗", "ma", "کیا؟ (سوال)"),
            choose(["你好吗", "谢谢", "再见", "对不起"], "你好吗", "nǐ hǎo ma", "آپ کیسے ہیں؟", "حال پوچھنے والا جملہ چنیں"),
            tone("很", "hěn", "بہت", 3),
        ],
    ),
    (
        4,
        "REVIEW",
        "Easy",
        "حال احوال کی مشق",
        "你好吗 اور 我很好 کی مشق کریں",
        4,
        [
            choose(["我很好", "谢谢", "再见", "对不起"], "我很好", "wǒ hěn hǎo", "میں ٹھیک ہوں", "جواب والا جملہ چنیں"),
            choose(["你好吗", "我很好", "谢谢", "再见"], "你好吗", "nǐ hǎo ma", "آپ کیسے ہیں؟", "سن کر سوال چنیں", "LISTENING_CHOICE"),
            build(["你", "好", "吗", "谢"], ["你", "好", "吗"], "آپ کیسے ہیں؟"),
            choose(["我很好", "你好", "谢谢", "再见"], "我很好", "wǒ hěn hǎo", "میں ٹھیک ہوں", "خالی جگہ بھریں", "FILL_IN_THE_BLANK"),
            tone("我", "wǒ", "میں", 3),
        ],
    ),
    (
        5,
        "NORMAL",
        "Easy",
        "جواب اور معافی",
        "不客气، 没关系 اور 对不起 سیکھیں",
        4,
        [
            teach("不客气", "bú kèqi", "کوئی بات نہیں / خوش آمدید"),
            teach("没关系", "méi guānxi", "کوئی مسئلہ نہیں"),
            teach("对不起", "duìbuqǐ", "معاف کیجیے"),
            choose(["不客气", "你好", "再见", "吃饭"], "不客气", "bú kèqi", "خوش آمدید", "شکریہ کا جواب چنیں"),
            build(["不", "客气", "谢谢", "你好"], ["不", "客气"], "خوش آمدید"),
        ],
    ),
    (
        6,
        "REVIEW",
        "Easy",
        "ادب کی مشق",
        "شکریہ، معافی اور جواب کی مشق",
        4,
        [
            choose(["没关系", "你好", "吃饭", "学校"], "没关系", "méi guānxi", "کوئی مسئلہ نہیں", "معافی کا جواب چنیں"),
            choose(["对不起", "谢谢", "再见", "你好"], "对不起", "duìbuqǐ", "معاف کیجیے", "سن کر معافی چنیں", "LISTENING_CHOICE"),
            choose(["不客气", "对不起", "你好吗", "再见"], "不客气", "bú kèqi", "خوش آمدید", "خالی جگہ بھریں", "FILL_IN_THE_BLANK"),
            build(["没", "关系", "谢谢", "你好"], ["没", "关系"], "کوئی مسئلہ نہیں"),
            tone("对", "duì", "درست / معاف", 4),
        ],
    ),
    (
        7,
        "NORMAL",
        "Easy",
        "نام پوچھیں",
        "你叫什么名字؟ اور 我叫 سیکھیں",
        5,
        [
            teach("你叫什么名字", "nǐ jiào shénme míngzi", "آپ کا نام کیا ہے؟"),
            teach("我叫", "wǒ jiào", "میرا نام ہے"),
            teach("名字", "míngzi", "نام"),
            teach("什么", "shénme", "کیا"),
            choose(["你叫什么名字", "谢谢", "再见", "吃饭"], "你叫什么名字", "nǐ jiào shénme míngzi", "آپ کا نام کیا ہے؟", "نام پوچھنے والا سوال چنیں"),
            build(["你", "叫", "什么", "名字"], ["你", "叫", "什么", "名字"], "آپ کا نام کیا ہے؟"),
        ],
    ),
    (
        8,
        "REVIEW",
        "Easy",
        "نام کی مشق",
        "نام والے جملوں کی مشق کریں",
        5,
        [
            choose(["我叫", "谢谢", "再见", "吃饭"], "我叫", "wǒ jiào", "میرا نام ہے", "اپنا نام بتانے والا جملہ چنیں"),
            choose(["名字", "学校", "米饭", "水"], "名字", "míngzi", "نام", "سن کر نام والا لفظ چنیں", "LISTENING_CHOICE"),
            choose(["什么", "很好", "再见", "吃饭"], "什么", "shénme", "کیا", "خالی جگہ بھریں", "FILL_IN_THE_BLANK"),
            build(["我", "叫", "什么", "名字"], ["我", "叫"], "میرا نام ہے"),
            tone("叫", "jiào", "کہلاتا ہوں", 4),
        ],
    ),
    (
        9,
        "NORMAL",
        "Easy",
        "اعداد 1 سے 5",
        "一二三四五 سیکھیں",
        5,
        [
            teach("一", "yī", "ایک"),
            teach("二", "èr", "دو"),
            teach("三", "sān", "تین"),
            teach("四", "sì", "چار"),
            teach("五", "wǔ", "پانچ"),
            choose(["三", "八", "十", "九"], "三", "sān", "تین", "تین چنیں"),
        ],
    ),
    (
        10,
        "NORMAL",
        "Easy",
        "اعداد 6 سے 10",
        "六七八九十 سیکھیں",
        5,
        [
            teach("六", "liù", "چھ"),
            teach("七", "qī", "سات"),
            teach("八", "bā", "آٹھ"),
            teach("九", "jiǔ", "نو"),
            teach("十", "shí", "دس"),
            choose(["十", "一", "二", "三"], "十", "shí", "دس", "دس چنیں"),
        ],
    ),
    (
        11,
        "REVIEW",
        "Easy",
        "گنتی کی مشق",
        "1 سے 10 تک اعداد کی مشق",
        5,
        [
            choose(["五", "八", "十", "七"], "五", "wǔ", "پانچ", "پانچ چنیں"),
            choose(["七", "一", "二", "三"], "七", "qī", "سات", "سن کر سات چنیں", "LISTENING_CHOICE"),
            choose(["九", "一", "二", "四"], "九", "jiǔ", "نو", "خالی جگہ بھریں", "FILL_IN_THE_BLANK"),
            build(["一", "二", "三", "十"], ["一", "二", "三"], "ایک دو تین"),
            tone("五", "wǔ", "پانچ", 3),
        ],
    ),
    (
        12,
        "NORMAL",
        "Easy",
        "خاندان کے الفاظ",
        "爸爸妈妈哥哥姐姐 سیکھیں",
        4,
        [
            teach("爸爸", "bàba", "والد"),
            teach("妈妈", "māma", "والدہ"),
            teach("哥哥", "gēge", "بڑا بھائی"),
            teach("姐姐", "jiějie", "بڑی بہن"),
            choose(["妈妈", "学校", "米饭", "水"], "妈妈", "māma", "والدہ", "والدہ چنیں"),
            tone("妈", "mā", "ماں", 1),
        ],
    ),
    (
        13,
        "REVIEW",
        "Easy",
        "خاندان کی مشق",
        "خاندان کے الفاظ کی مشق کریں",
        4,
        [
            choose(["爸爸", "米饭", "水", "学校"], "爸爸", "bàba", "والد", "والد چنیں"),
            choose(["哥哥", "谢谢", "再见", "吃饭"], "哥哥", "gēge", "بڑا بھائی", "سن کر بھائی چنیں", "LISTENING_CHOICE"),
            choose(["姐姐", "一", "二", "三"], "姐姐", "jiějie", "بڑی بہن", "خالی جگہ بھریں", "FILL_IN_THE_BLANK"),
            build(["爸", "爸", "妈", "妈"], ["爸", "爸"], "والد"),
            tone("爸", "bà", "باپ", 4),
        ],
    ),
    (
        14,
        "NORMAL",
        "Medium",
        "کھانا اور پانی",
        "吃饭، 喝水، 茶، 米饭 سیکھیں",
        4,
        [
            teach("吃饭", "chī fàn", "کھانا کھانا"),
            teach("喝水", "hē shuǐ", "پانی پینا"),
            teach("茶", "chá", "چائے"),
            teach("米饭", "mǐfàn", "چاول"),
            choose(["吃饭", "学校", "爸爸", "名字"], "吃饭", "chī fàn", "کھانا کھانا", "کھانا والا فعل چنیں"),
            build(["吃", "饭", "水", "茶"], ["吃", "饭"], "کھانا کھانا"),
        ],
    ),
    (
        15,
        "REVIEW",
        "Medium",
        "کھانے کی مشق",
        "کھانے پینے کے الفاظ کی مشق",
        4,
        [
            choose(["喝水", "爸爸", "学校", "名字"], "喝水", "hē shuǐ", "پانی پینا", "پانی پینا چنیں"),
            choose(["茶", "一", "二", "三"], "茶", "chá", "چائے", "سن کر چائے چنیں", "LISTENING_CHOICE"),
            choose(["米饭", "谢谢", "再见", "名字"], "米饭", "mǐfàn", "چاول", "خالی جگہ بھریں", "FILL_IN_THE_BLANK"),
            build(["喝", "水", "吃", "饭"], ["喝", "水"], "پانی پینا"),
            tone("吃", "chī", "کھانا", 1),
        ],
    ),
    (
        16,
        "NORMAL",
        "Medium",
        "گھر اور اسکول",
        "家، 学校، 在哪儿 سیکھیں",
        4,
        [
            teach("家", "jiā", "گھر"),
            teach("学校", "xuéxiào", "اسکول"),
            teach("在哪儿", "zài nǎr", "کہاں ہے؟"),
            teach("学生", "xuésheng", "طالب علم"),
            choose(["学校", "米饭", "茶", "五"], "学校", "xuéxiào", "اسکول", "اسکول چنیں"),
            build(["在", "哪儿", "家", "茶"], ["在", "哪儿"], "کہاں ہے؟"),
        ],
    ),
    (
        17,
        "REVIEW",
        "Medium",
        "ملا جلا جائزہ",
        "سلام، اعداد، خاندان اور جگہ ملا کر مشق کریں",
        6,
        [
            choose(["在哪儿", "谢谢", "米饭", "五"], "在哪儿", "zài nǎr", "کہاں ہے؟", "جگہ پوچھنے والا سوال چنیں"),
            choose(["学生", "米饭", "茶", "八"], "学生", "xuésheng", "طالب علم", "سن کر طالب علم چنیں", "LISTENING_CHOICE"),
            choose(["家", "七", "八", "九"], "家", "jiā", "گھر", "خالی جگہ بھریں", "FILL_IN_THE_BLANK"),
            build(["学", "校", "吃", "饭"], ["学", "校"], "اسکول"),
            tone("家", "jiā", "گھر", 1),
            choose(["你好吗", "吃饭", "十", "茶"], "你好吗", "nǐ hǎo ma", "آپ کیسے ہیں؟", "سب سے آسان سلام والا سوال چنیں"),
        ],
    ),
]


VOCAB = [
    ("吗", "ma", 0, "کیا؟ (سوال کا جزء)", "kya", "particle"),
    ("很", "hěn", 3, "بہت", "bohat", "adverb"),
    ("不客气", "bú kèqi", 4, "خوش آمدید / کوئی بات نہیں", "koi baat nahi", "phrase"),
    ("一", "yī", 1, "ایک", "aik", "numeral"),
    ("二", "èr", 4, "دو", "do", "numeral"),
    ("三", "sān", 1, "تین", "teen", "numeral"),
    ("四", "sì", 4, "چار", "chaar", "numeral"),
    ("五", "wǔ", 3, "پانچ", "paanch", "numeral"),
    ("六", "liù", 4, "چھ", "chhe", "numeral"),
    ("七", "qī", 1, "سات", "saat", "numeral"),
    ("八", "bā", 1, "آٹھ", "aath", "numeral"),
    ("九", "jiǔ", 3, "نو", "nau", "numeral"),
    ("十", "shí", 2, "دس", "das", "numeral"),
    ("哥哥", "gēge", 1, "بڑا بھائی", "bara bhai", "noun"),
    ("吃饭", "chī fàn", 1, "کھانا کھانا", "khana khana", "verb"),
    ("喝水", "hē shuǐ", 1, "پانی پینا", "pani peena", "verb"),
    ("茶", "chá", 2, "چائے", "chai", "noun"),
    ("家", "jiā", 1, "گھر", "ghar", "noun"),
    ("学校", "xuéxiào", 2, "اسکول", "school", "noun"),
]


def build_sql() -> str:
    lines = [
        "SET NAMES utf8mb4;",
        "START TRANSACTION;",
        f"UPDATE lessons SET lesson_number = 18, urdu_title = {sql_str('چیک پوائنٹ')}, instruction_text = {sql_str('پورے یونٹ کا جائزہ')}, difficulty = 'Medium', updated_at = NOW(6) WHERE id = {sql_str(CHECKPOINT_ID)};",
        f"DELETE FROM lessons WHERE unit_id = {sql_str(UNIT_ID)} AND lesson_number BETWEEN 3 AND 17;",
        f"UPDATE units SET estimated_time = 90, difficulty = 'Easy', grammar_point = {sql_str('HSK 1: سلام، نام، اعداد، خاندان، کھانا اور جگہ')}, updated_at = NOW(6) WHERE id = {sql_str(UNIT_ID)};",
    ]

    for number, lesson_type, difficulty, title, instruction, words, exercises in LESSONS:
        lesson_id = uid()
        lines.append(
            "INSERT INTO lessons (id, unit_id, lesson_number, lesson_type, urdu_title, instruction_text, difficulty, crowns, status, exercises_count, words_count, created_by, created_at, updated_at) VALUES ("
            + ", ".join(
                [
                    sql_str(lesson_id),
                    sql_str(UNIT_ID),
                    str(number),
                    sql_str(lesson_type),
                    sql_str(title),
                    sql_str(instruction),
                    sql_str(difficulty),
                    "3",
                    sql_str("PUBLISHED"),
                    str(len(exercises)),
                    str(words),
                    sql_str(ADMIN_ID),
                    "NOW(6)",
                    "NOW(6)",
                ]
            )
            + ");"
        )
        for order, exercise in enumerate(exercises, start=1):
            lines.append(
                "INSERT INTO exercises (id, lesson_id, exercise_type, exercise_data, exercise_order, created_by, created_at, updated_at) VALUES ("
                + ", ".join(
                    [
                        sql_str(uid()),
                        sql_str(lesson_id),
                        sql_str(exercise["exerciseType"]),
                        sql_json(exercise["exerciseData"]),
                        str(order),
                        sql_str(ADMIN_ID),
                        "NOW(6)",
                        "NOW(6)",
                    ]
                )
                + ");"
            )

    for hanzi, pinyin, tone_num, urdu, roman, pos in VOCAB:
        lines.append(
            "INSERT INTO vocabulary (id, hanzi, pinyin, tone, urdu_translation, roman_urdu, part_of_speech, hsk_level, frequency, topics, created_by, created_at, updated_at) "
            f"SELECT {sql_str(uid())}, {sql_str(hanzi)}, {sql_str(pinyin)}, {tone_num}, {sql_str(urdu)}, {sql_str(roman)}, {sql_str(pos)}, 1, 5, CAST({sql_str('["Daily"]')} AS JSON), {sql_str(ADMIN_ID)}, NOW(6), NOW(6) "
            f"FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM vocabulary WHERE hanzi = {sql_str(hanzi)} LIMIT 1);"
        )

    lines.append("COMMIT;")
    return "\n".join(lines) + "\n"


def main() -> None:
    sql = build_sql()
    sql_path = Path(__file__).with_name("seed_expand_unit1_path.sql")
    sql_path.write_text(sql, encoding="utf-8")
    result = subprocess.run(
        [
            MYSQL,
            "-uroot",
            "-p123456",
            "--default-character-set=utf8mb4",
            "nihao_urdu",
        ],
        input=sql,
        check=False,
        capture_output=True,
        text=True,
        encoding="utf-8",
    )
    print(result.stdout)
    print(result.stderr)
    if result.returncode != 0:
        raise SystemExit(result.returncode)
    verify = subprocess.run(
        [
            MYSQL,
            "-uroot",
            "-p123456",
            "--default-character-set=utf8mb4",
            "nihao_urdu",
            "-e",
            f"SELECT lesson_number, lesson_type, urdu_title, difficulty, exercises_count FROM lessons WHERE unit_id='{UNIT_ID}' ORDER BY lesson_number;",
        ],
        check=True,
        capture_output=True,
        text=True,
        encoding="utf-8",
    )
    print(verify.stdout)


if __name__ == "__main__":
    main()
