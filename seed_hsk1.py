# -*- coding: utf-8 -*-
"""One-shot seed: HSK 1 units, lessons, exercises, vocabulary."""
import json
import urllib.error
import urllib.request

BASE = "http://localhost:8089/api"
COURSE_ID = "0a2552cb-962e-40ef-9cfe-85a6e5830387"


def req(method, path, token=None, body=None):
    headers = {"Content-Type": "application/json; charset=utf-8"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    data = json.dumps(body, ensure_ascii=False).encode("utf-8") if body is not None else None
    request = urllib.request.Request(BASE + path, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(request) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as err:
        detail = err.read().decode("utf-8", errors="replace")
        raise SystemExit(f"{method} {path} -> {err.code}\n{detail}") from err


def teach(word, pinyin, urdu, prompt_ur="اس لفظ کو سیکھیں"):
    return {
        "exerciseType": "TEACH_FRAME",
        "exerciseData": {
            "promptUr": prompt_ur,
            "promptEn": "Learn this word",
            "word": word,
            "items": [word],
            "answer": word,
            "pinyin": pinyin,
            "urdu": urdu,
            "gloss": [{"ur": urdu, "en": word}],
        },
    }


def choose(options, answer, pinyin, urdu, prompt_ur, kind="PICTURE_MATCH"):
    return {
        "exerciseType": kind,
        "exerciseData": {
            "promptUr": prompt_ur,
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


def tone(word, pinyin, urdu, tone_num):
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


def build(items, answer_parts, urdu):
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


def add_exercises(token, lesson_id, exercises):
    for i, exercise in enumerate(exercises, start=1):
        payload = {**exercise, "exerciseOrder": i}
        req("POST", f"/exercises/lesson/{lesson_id}", token, payload)


def add_lesson(token, unit_id, number, title, instruction, lesson_type, exercises):
    lesson = req(
        "POST",
        f"/lessons/unit/{unit_id}",
        token,
        {
            "lessonNumber": number,
            "lessonType": lesson_type,
            "urduTitle": title,
            "instructionText": instruction,
            "difficulty": "Easy",
            "status": "PUBLISHED",
            "crowns": 3,
        },
    )["data"]
    add_exercises(token, lesson["id"], exercises)
    return lesson["id"]


def main():
    token = req(
        "POST",
        "/auth/login",
        body={"email": "admin@nihao-urdu.com", "password": "admin123"},
    )["data"]["token"]

    req(
        "PUT",
        f"/courses/{COURSE_ID}",
        token,
        {
            "name": "HSK 1 Greetings",
            "hskLevel": "HSK 1",
            "description": "Beginner Chinese greetings and introductions for Urdu speakers.",
            "difficulty": "Beginner",
            "status": "PUBLISHED",
        },
    )

    existing_units = req("GET", f"/units/course/{COURSE_ID}", token).get("data") or []
    if existing_units:
        print(f"Course already has {len(existing_units)} unit(s). Skipping seed.")
        return

    unit1 = req(
        "POST",
        f"/units/course/{COURSE_ID}",
        token,
        {
            "unitNumber": 1,
            "urduTitle": "سلام اور تعارف",
            "hanziTitle": "问候",
            "grammarPoint": "你好 is a greeting. 吗 turns a sentence into a yes/no question.",
            "hskLevel": "HSK 1",
            "topics": ["Daily", "Culture"],
            "estimatedTime": 20,
            "difficulty": "Easy",
            "learningObjectives": ["Say hello", "Say thank you", "Say goodbye"],
            "status": "PUBLISHED",
            "version": 1,
        },
    )["data"]

    unit2 = req(
        "POST",
        f"/units/course/{COURSE_ID}",
        token,
        {
            "unitNumber": 2,
            "urduTitle": "نام پوچھنا",
            "hanziTitle": "名字",
            "grammarPoint": "你叫什么名字？ asks someone's name. 我叫… answers it.",
            "hskLevel": "HSK 1",
            "topics": ["Daily", "Education"],
            "estimatedTime": 20,
            "difficulty": "Easy",
            "learningObjectives": ["Ask a name", "Say your name"],
            "status": "PUBLISHED",
            "version": 1,
        },
    )["data"]

    add_lesson(
        token,
        unit1["id"],
        1,
        "ہیلو",
        "اس سبق میں سلام سیکھیں",
        "NORMAL",
        [
            teach("你好", "nǐ hǎo", "ہیلو"),
            choose(["你好", "谢谢", "再见", "对不起"], "你好", "nǐ hǎo", "ہیلو", "ان میں سے ہیلو منتخب کریں"),
            choose(
                ["你好", "谢谢", "再见", "对不起"],
                "你好",
                "nǐ hǎo",
                "ہیلو",
                "سن کر صحیح لفظ چنیں",
                "LISTENING_CHOICE",
            ),
            tone("你", "nǐ", "آپ", 3),
            build(["你", "好", "吗", "谢"], ["你", "好"], "ہیلو"),
        ],
    )

    add_lesson(
        token,
        unit1["id"],
        2,
        "شکریہ",
        "شکریہ کہنا سیکھیں",
        "NORMAL",
        [
            teach("谢谢", "xièxie", "شکریہ"),
            choose(["谢谢", "你好", "再见", "对不起"], "谢谢", "xièxie", "شکریہ", "شکریہ منتخب کریں"),
            build(["谢", "谢", "你", "好"], ["谢", "谢"], "شکریہ"),
            choose(
                ["谢谢", "你好", "再见", "对不起"],
                "谢谢",
                "xièxie",
                "شکریہ",
                "خالی جگہ بھریں",
                "FILL_IN_THE_BLANK",
            ),
        ],
    )

    add_lesson(
        token,
        unit1["id"],
        3,
        "الوداع — کوئز",
        "الوداع اور معافی کا جائزہ",
        "REVIEW",
        [
            teach("再见", "zàijiàn", "الوداع"),
            choose(["再见", "你好", "谢谢", "对不起"], "再见", "zàijiàn", "الوداع", "الوداع منتخب کریں"),
            choose(
                ["对不起", "你好", "谢谢", "再见"],
                "对不起",
                "duìbuqǐ",
                "معاف کیجیے",
                "معافی والا لفظ چنیں",
            ),
            build(["再", "见", "你", "好"], ["再", "见"], "الوداع"),
        ],
    )

    add_lesson(
        token,
        unit2["id"],
        1,
        "آپ کا نام کیا ہے؟",
        "نام پوچھنا سیکھیں",
        "NORMAL",
        [
            teach("你叫什么名字？", "Nǐ jiào shénme míngzi?", "آپ کا نام کیا ہے؟"),
            choose(
                ["你叫什么名字？", "你好", "谢谢", "再见"],
                "你叫什么名字？",
                "Nǐ jiào shénme míngzi?",
                "آپ کا نام کیا ہے؟",
                "صحیح سوال چنیں",
            ),
            build(["你", "叫", "什么", "名字"], ["你", "叫", "什么", "名字"], "آپ کا نام کیا ہے؟"),
        ],
    )

    add_lesson(
        token,
        unit2["id"],
        2,
        "میرا نام",
        "اپنا نام بتانا سیکھیں",
        "NORMAL",
        [
            teach("我叫", "wǒ jiào", "میرا نام ہے"),
            choose(["我叫", "你好", "谢谢", "再见"], "我叫", "wǒ jiào", "میرا نام ہے", "میرا نام والا جملہ چنیں"),
            choose(["我", "你", "他", "她"], "我", "wǒ", "میں", "خالی جگہ بھریں", "FILL_IN_THE_BLANK"),
            tone("我", "wǒ", "میں", 3),
        ],
    )

    add_lesson(
        token,
        unit2["id"],
        3,
        "نام کا کوئز",
        "نام والے الفاظ کا جائزہ",
        "CHECKPOINT",
        [
            choose(
                ["你叫什么名字？", "谢谢", "再见", "对不起"],
                "你叫什么名字？",
                "Nǐ jiào shénme míngzi?",
                "آپ کا نام کیا ہے؟",
                "نام پوچھنے والا سوال چنیں",
            ),
            choose(["我叫", "你好", "谢谢", "再见"], "我叫", "wǒ jiào", "میرا نام ہے", "جواب والا جملہ چنیں"),
            build(["我", "叫", "你", "好"], ["我", "叫"], "میرا نام ہے"),
        ],
    )

    words = [
        ("你好", "nǐ hǎo", 3, "ہیلو", "hello", "phrase"),
        ("谢谢", "xièxie", 4, "شکریہ", "shukriya", "verb"),
        ("再见", "zàijiàn", 4, "الوداع", "alwida", "verb"),
        ("对不起", "duìbuqǐ", 4, "معاف کیجیے", "maaf kijiye", "phrase"),
        ("你", "nǐ", 3, "آپ", "aap", "pronoun"),
        ("好", "hǎo", 3, "اچھا", "acha", "adjective"),
        ("我", "wǒ", 3, "میں", "main", "pronoun"),
        ("叫", "jiào", 4, "کہلاتا ہوں", "kehlata hon", "verb"),
        ("什么", "shénme", 2, "کیا", "kya", "pronoun"),
        ("名字", "míngzi", 2, "نام", "naam", "noun"),
    ]
    for hanzi, pinyin, tone_num, urdu, roman, pos in words:
        req(
            "POST",
            "/vocabulary",
            token,
            {
                "hanzi": hanzi,
                "pinyin": pinyin,
                "tone": tone_num,
                "urduTranslation": urdu,
                "romanUrdu": roman,
                "partOfSpeech": pos,
                "hskLevel": 1,
                "topics": ["Daily"],
            },
        )

    print("Seeded HSK 1 Greetings: 2 units, 6 lessons, mixed exercises, 10 words.")


if __name__ == "__main__":
    main()
