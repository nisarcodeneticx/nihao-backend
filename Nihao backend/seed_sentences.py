# -*- coding: utf-8 -*-
"""Upload Easy and Medium full-sentence courses to the Nihao admin API."""
import json
import urllib.error
import urllib.request

BASE = "http://localhost:8089/api"

EASY_NAME = "Easy Chinese Sentences"
MEDIUM_NAME = "Medium Chinese Sentences"


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


def teach(hanzi, pinyin, urdu):
    return {
        "exerciseType": "TEACH_FRAME",
        "exerciseData": {
            "promptUr": "اس جملے کو سیکھیں",
            "promptEn": "Learn this sentence",
            "word": hanzi,
            "items": [hanzi],
            "answer": hanzi,
            "pinyin": pinyin,
            "urdu": urdu,
            "gloss": [{"ur": urdu, "en": hanzi}],
        },
    }


def choose(options, answer, pinyin, urdu, prompt_ur, kind="PICTURE_MATCH"):
    return {
        "exerciseType": kind,
        "exerciseData": {
            "promptUr": prompt_ur,
            "promptEn": "Choose the correct sentence",
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


def build(parts, extra, urdu):
    items = list(parts) + list(extra)
    return {
        "exerciseType": "TAP_TO_BUILD",
        "exerciseData": {
            "promptUr": "جملہ صحیح ترتیب میں بنائیں",
            "promptEn": "Tap the words in the correct order",
            "items": items,
            "options": items,
            "answer": " ".join(parts),
            "urdu": urdu,
            "gloss": [{"ur": urdu, "en": "".join(parts)}],
        },
    }


def fill(options, answer, pinyin, urdu, prompt_ur):
    return choose(options, answer, pinyin, urdu, prompt_ur, "FILL_IN_THE_BLANK")


def listen(options, answer, pinyin, urdu, prompt_ur):
    return choose(options, answer, pinyin, urdu, prompt_ur, "LISTENING_CHOICE")


def add_exercises(token, lesson_id, exercises):
    for i, exercise in enumerate(exercises, start=1):
        payload = {**exercise, "exerciseOrder": i}
        req("POST", f"/exercises/lesson/{lesson_id}", token, payload)


def add_lesson(token, unit_id, number, title, instruction, lesson_type, difficulty, words_count, exercises):
    lesson = req(
        "POST",
        f"/lessons/unit/{unit_id}",
        token,
        {
            "lessonNumber": number,
            "lessonType": lesson_type,
            "urduTitle": title,
            "instructionText": instruction,
            "difficulty": difficulty,
            "status": "PUBLISHED",
            "crowns": 3,
            "wordsCount": words_count,
        },
    )["data"]
    add_exercises(token, lesson["id"], exercises)
    return lesson["id"]


def add_unit(token, course_id, number, urdu, hanzi, grammar, hsk, topics, minutes, difficulty, objectives):
    return req(
        "POST",
        f"/units/course/{course_id}",
        token,
        {
            "unitNumber": number,
            "urduTitle": urdu,
            "hanziTitle": hanzi,
            "grammarPoint": grammar,
            "hskLevel": hsk,
            "topics": topics,
            "estimatedTime": minutes,
            "difficulty": difficulty,
            "learningObjectives": objectives,
            "status": "PUBLISHED",
            "version": 1,
        },
    )["data"]


def add_course(token, name, hsk, description, difficulty):
    return req(
        "POST",
        "/courses",
        token,
        {
            "name": name,
            "hskLevel": hsk,
            "description": description,
            "difficulty": difficulty,
            "status": "PUBLISHED",
        },
    )["data"]


def existing_course_names(token):
    payload = req("GET", "/courses?page=0&size=200", token)
    data = payload.get("data") or {}
    content = data.get("content") if isinstance(data, dict) else data
    return {item.get("name"): item for item in (content or []) if item.get("name")}


def vocab_item(hanzi, pinyin, tone_num, urdu, roman, pos, hsk, topics, example):
    return {
        "hanzi": hanzi,
        "pinyin": pinyin,
        "tone": tone_num,
        "urduTranslation": urdu,
        "romanUrdu": roman,
        "partOfSpeech": pos,
        "hskLevel": hsk,
        "topics": topics,
        "frequency": 5,
        "examples": [
            {
                "hanzi": example[0],
                "pinyin": example[1],
                "urdu": example[2],
            }
        ],
    }


def sentence_pack(s, distractors):
    options = [s["hanzi"]] + distractors
    return [
        teach(s["hanzi"], s["pinyin"], s["urdu"]),
        choose(options, s["hanzi"], s["pinyin"], s["urdu"], "صحیح جملہ منتخب کریں"),
        listen(options, s["hanzi"], s["pinyin"], s["urdu"], "سن کر صحیح جملہ چنیں"),
        build(s["parts"], s["extra"], s["urdu"]),
        fill(s["fill_opts"], s["fill_answer"], s["pinyin"], s["urdu"], s["fill_prompt"]),
    ]


def review_pack(items):
    exercises = []
    for item in items:
        exercises.append(
            choose(
                item["options"],
                item["hanzi"],
                item["pinyin"],
                item["urdu"],
                item["prompt"],
                item.get("kind", "PICTURE_MATCH"),
            )
        )
    return exercises


def distractors_for(sentence, group):
    return [item["hanzi"] for item in group if item["hanzi"] != sentence["hanzi"]][:3]


def practice_quizzes(sentences, tones=None):
    quizzes = []
    for sentence in sentences:
        options = [sentence["hanzi"]] + distractors_for(sentence, sentences)
        quizzes.append(
            choose(
                options,
                sentence["hanzi"],
                sentence["pinyin"],
                sentence["urdu"],
                "صحیح جملہ منتخب کریں",
            )
        )
        quizzes.append(build(sentence["parts"], sentence["extra"], sentence["urdu"]))
        quizzes.append(
            fill(
                sentence["fill_opts"],
                sentence["fill_answer"],
                sentence["pinyin"],
                sentence["urdu"],
                sentence["fill_prompt"],
            )
        )
    if tones:
        quizzes.extend(tones)
    return quizzes


def checkpoint_exercises(sentences):
    items = []
    kinds = ["PICTURE_MATCH", "LISTENING_CHOICE", "PICTURE_MATCH", "LISTENING_CHOICE"]
    for index, sentence in enumerate(sentences):
        items.append(
            {
                "hanzi": sentence["hanzi"],
                "pinyin": sentence["pinyin"],
                "urdu": sentence["urdu"],
                "options": [sentence["hanzi"]] + distractors_for(sentence, sentences),
                "prompt": "صحیح جملہ چنیں",
                "kind": kinds[index % len(kinds)],
            }
        )
    exercises = review_pack(items)
    last = sentences[-1]
    exercises.append(build(last["parts"], last["extra"], last["urdu"]))
    exercises.append(
        fill(last["fill_opts"], last["fill_answer"], last["pinyin"], last["urdu"], last["fill_prompt"])
    )
    return exercises


def add_learn_then_quiz(
    token,
    unit_id,
    difficulty,
    words_count,
    sentences,
    learn_title,
    practice_title,
    checkpoint_title,
    tones=None,
):
    teaches = [teach(sentence["hanzi"], sentence["pinyin"], sentence["urdu"]) for sentence in sentences]
    add_lesson(
        token,
        unit_id,
        1,
        learn_title,
        "پہلے جملے سیکھیں، مشق بعد میں آئے گی",
        "NORMAL",
        difficulty,
        words_count,
        teaches,
    )
    add_lesson(
        token,
        unit_id,
        2,
        practice_title,
        "اب ان جملوں کی مشق کریں",
        "REVIEW",
        difficulty,
        words_count,
        practice_quizzes(sentences, tones),
    )
    add_lesson(
        token,
        unit_id,
        3,
        checkpoint_title,
        "جملوں کا جائزہ",
        "CHECKPOINT",
        difficulty,
        words_count,
        checkpoint_exercises(sentences),
    )


def seed_easy(token):
    course = add_course(
        token,
        EASY_NAME,
        "HSK 1",
        "Beginner Chinese full sentences for Urdu speakers: greetings, names, family, and food.",
        "Beginner",
    )

    s_how = {
        "hanzi": "你好！你怎么样？",
        "pinyin": "Nǐ hǎo! Nǐ zěnmeyàng?",
        "urdu": "ہیلو! آپ کیسے ہیں؟",
        "parts": ["你好", "你", "怎么样"],
        "extra": ["很好"],
        "fill_opts": ["怎么样", "谢谢", "再见"],
        "fill_answer": "怎么样",
        "fill_prompt": "خالی جگہ بھریں: 你好！你____？",
    }
    s_fine = {
        "hanzi": "我很好，谢谢！",
        "pinyin": "Wǒ hěn hǎo, xièxie!",
        "urdu": "میں بہت اچھا ہوں، شکریہ!",
        "parts": ["我", "很好", "谢谢"],
        "extra": ["你好"],
        "fill_opts": ["很好", "再见", "对不起"],
        "fill_answer": "很好",
        "fill_prompt": "خالی جگہ بھریں: 我____，谢谢！",
    }
    s_bye = {
        "hanzi": "再见！明天见！",
        "pinyin": "Zàijiàn! Míngtiān jiàn!",
        "urdu": "الوداع! کل ملتے ہیں!",
        "parts": ["再见", "明天见"],
        "extra": ["你好", "谢谢"],
        "fill_opts": ["明天见", "怎么样", "很好"],
        "fill_answer": "明天见",
        "fill_prompt": "خالی جگہ بھریں: 再见！____！",
    }
    s_sorry = {
        "hanzi": "对不起。没关系。",
        "pinyin": "Duìbuqǐ. Méi guānxi.",
        "urdu": "معاف کیجیے۔ کوئی بات نہیں۔",
        "parts": ["对不起", "没关系"],
        "extra": ["谢谢", "你好"],
        "fill_opts": ["没关系", "怎么样", "再见"],
        "fill_answer": "没关系",
        "fill_prompt": "خالی جگہ بھریں: 对不起。____。",
    }
    s_name_q = {
        "hanzi": "你叫什么名字？",
        "pinyin": "Nǐ jiào shénme míngzi?",
        "urdu": "آپ کا نام کیا ہے؟",
        "parts": ["你", "叫", "什么", "名字"],
        "extra": ["很好"],
        "fill_opts": ["名字", "谢谢", "再见"],
        "fill_answer": "名字",
        "fill_prompt": "خالی جگہ بھریں: 你叫什么____？",
    }
    s_name_a = {
        "hanzi": "我叫阿里。",
        "pinyin": "Wǒ jiào Ālǐ.",
        "urdu": "میرا نام علی ہے۔",
        "parts": ["我", "叫", "阿里"],
        "extra": ["你", "什么"],
        "fill_opts": ["叫", "是", "有"],
        "fill_answer": "叫",
        "fill_prompt": "خالی جگہ بھریں: 我____阿里。",
    }
    s_meet = {
        "hanzi": "很高兴认识你。",
        "pinyin": "Hěn gāoxìng rènshi nǐ.",
        "urdu": "آپ سے مل کر خوشی ہوئی۔",
        "parts": ["很高兴", "认识", "你"],
        "extra": ["谢谢"],
        "fill_opts": ["认识", "再见", "吃饭"],
        "fill_answer": "认识",
        "fill_prompt": "خالی جگہ بھریں: 很高兴____你。",
    }
    s_student = {
        "hanzi": "我是学生。你是老师吗？",
        "pinyin": "Wǒ shì xuésheng. Nǐ shì lǎoshī ma?",
        "urdu": "میں طالب علم ہوں۔ کیا آپ استاد ہیں؟",
        "parts": ["我", "是", "学生"],
        "extra": ["老师", "叫"],
        "fill_opts": ["是", "叫", "有"],
        "fill_answer": "是",
        "fill_prompt": "خالی جگہ بھریں: 我____学生。",
    }
    s_dad = {
        "hanzi": "这是我的爸爸。",
        "pinyin": "Zhè shì wǒ de bàba.",
        "urdu": "یہ میرے والد ہیں۔",
        "parts": ["这", "是", "我的", "爸爸"],
        "extra": ["妈妈"],
        "fill_opts": ["爸爸", "老师", "学生"],
        "fill_answer": "爸爸",
        "fill_prompt": "خالی جگہ بھریں: 这是我的____。",
    }
    s_mom = {
        "hanzi": "这是我的妈妈。",
        "pinyin": "Zhè shì wǒ de māma.",
        "urdu": "یہ میری والدہ ہیں۔",
        "parts": ["这", "是", "我的", "妈妈"],
        "extra": ["爸爸"],
        "fill_opts": ["妈妈", "学生", "老师"],
        "fill_answer": "妈妈",
        "fill_prompt": "خالی جگہ بھریں: 这是我的____。",
    }
    s_sister = {
        "hanzi": "我有一个姐姐。",
        "pinyin": "Wǒ yǒu yí ge jiějie.",
        "urdu": "میری ایک بڑی بہن ہے۔",
        "parts": ["我", "有", "一个", "姐姐"],
        "extra": ["爸爸"],
        "fill_opts": ["有", "是", "叫"],
        "fill_answer": "有",
        "fill_prompt": "خالی جگہ بھریں: 我____一个姐姐。",
    }
    s_family_q = {
        "hanzi": "你家有几口人？",
        "pinyin": "Nǐ jiā yǒu jǐ kǒu rén?",
        "urdu": "آپ کے گھر میں کتنے لوگ ہیں؟",
        "parts": ["你家", "有", "几口", "人"],
        "extra": ["老师"],
        "fill_opts": ["几口", "什么", "很好"],
        "fill_answer": "几口",
        "fill_prompt": "خالی جگہ بھریں: 你家有____人？",
    }
    s_tea = {
        "hanzi": "我想喝茶。",
        "pinyin": "Wǒ xiǎng hē chá.",
        "urdu": "میں چائے پینا چاہتا ہوں۔",
        "parts": ["我", "想", "喝", "茶"],
        "extra": ["饭"],
        "fill_opts": ["喝茶", "再见", "老师"],
        "fill_answer": "喝茶",
        "fill_prompt": "خالی جگہ بھریں: 我想____。",
    }
    s_rice = {
        "hanzi": "你吃米饭吗？",
        "pinyin": "Nǐ chī mǐfàn ma?",
        "urdu": "کیا آپ چاول کھاتے ہیں؟",
        "parts": ["你", "吃", "米饭", "吗"],
        "extra": ["茶"],
        "fill_opts": ["吃", "喝", "叫"],
        "fill_answer": "吃",
        "fill_prompt": "خالی جگہ بھریں: 你____米饭吗？",
    }
    s_tasty = {
        "hanzi": "这个很好吃。",
        "pinyin": "Zhège hěn hǎochī.",
        "urdu": "یہ بہت مزیدار ہے۔",
        "parts": ["这个", "很", "好吃"],
        "extra": ["喝茶"],
        "fill_opts": ["好吃", "老师", "再见"],
        "fill_answer": "好吃",
        "fill_prompt": "خالی جگہ بھریں: 这个很____。",
    }
    s_not_hungry = {
        "hanzi": "我不饿。谢谢你。",
        "pinyin": "Wǒ bù è. Xièxie nǐ.",
        "urdu": "مجھے بھوک نہیں ہے۔ آپ کا شکریہ۔",
        "parts": ["我", "不", "饿"],
        "extra": ["好吃", "茶"],
        "fill_opts": ["饿", "好", "叫"],
        "fill_answer": "饿",
        "fill_prompt": "خالی جگہ بھریں: 我不____。",
    }

    unit1 = add_unit(
        token, course["id"], 1, "سلام کے جملے", "问候句子",
        "怎么样 asks how someone is. 很好 answers it. 吗 is not needed here.",
        "HSK 1", ["Daily", "Culture"], 20, "Easy",
        ["Greet someone with a full sentence", "Ask how they are", "Say goodbye politely"],
    )
    add_learn_then_quiz(
        token,
        unit1["id"],
        "Easy",
        8,
        [s_how, s_fine, s_bye, s_sorry],
        "سلام کے جملے سیکھیں",
        "سلام کی مشق",
        "سلام کا جائزہ",
        tones=[tone("你", "nǐ", "آپ", 3)],
    )

    unit2 = add_unit(
        token, course["id"], 2, "نام اور تعارف", "介绍句子",
        "叫 is used for names. 是 links two nouns. 吗 turns a statement into a yes/no question.",
        "HSK 1", ["Daily", "Education"], 20, "Easy",
        ["Ask someone's name", "Say your name", "Say you are a student"],
    )
    add_learn_then_quiz(
        token,
        unit2["id"],
        "Easy",
        8,
        [s_name_q, s_name_a, s_meet, s_student],
        "تعارف کے جملے سیکھیں",
        "تعارف کی مشق",
        "تعارف کا جائزہ",
        tones=[tone("我", "wǒ", "میں", 3)],
    )

    unit3 = add_unit(
        token, course["id"], 3, "خاندان کے جملے", "家人句子",
        "这是我的… introduces family. 有 means to have. 几口人 asks family size.",
        "HSK 1", ["Daily", "Culture"], 20, "Easy",
        ["Introduce family members", "Say you have a sibling", "Ask family size"],
    )
    add_learn_then_quiz(
        token,
        unit3["id"],
        "Easy",
        8,
        [s_dad, s_mom, s_sister, s_family_q],
        "خاندان کے جملے سیکھیں",
        "خاندان کی مشق",
        "خاندان کا جائزہ",
    )

    unit4 = add_unit(
        token, course["id"], 4, "کھانے کے جملے", "饮食句子",
        "想 + verb expresses want. 吗 makes 你吃米饭 a question. 很好吃 describes food.",
        "HSK 1", ["Food", "Daily"], 20, "Easy",
        ["Say you want tea", "Ask if someone eats rice", "Praise food"],
    )
    add_learn_then_quiz(
        token,
        unit4["id"],
        "Easy",
        7,
        [s_tea, s_rice, s_tasty, s_not_hungry],
        "کھانے کے جملے سیکھیں",
        "کھانے کی مشق",
        "کھانے کا جائزہ",
    )

    print(f"Created {EASY_NAME}: 4 units, 12 lessons of full sentences.")
    return course["id"]


def seed_medium(token):
    course = add_course(
        token,
        MEDIUM_NAME,
        "HSK 1-2",
        "Medium Chinese full sentences for Urdu speakers: time, shopping, directions, likes, and repair phrases.",
        "Intermediate",
    )

    s_time_q = {
        "hanzi": "现在几点？",
        "pinyin": "Xiànzài jǐ diǎn?",
        "urdu": "اب کیا وقت ہے؟",
        "parts": ["现在", "几点"],
        "extra": ["今天", "很好"],
        "fill_opts": ["几点", "什么", "几口"],
        "fill_answer": "几点",
        "fill_prompt": "خالی جگہ بھریں: 现在____？",
    }
    s_time_a = {
        "hanzi": "现在八点。我八点起床。",
        "pinyin": "Xiànzài bā diǎn. Wǒ bā diǎn qǐchuáng.",
        "urdu": "اب آٹھ بجے ہیں۔ میں آٹھ بجے اٹھتا ہوں۔",
        "parts": ["现在", "八点"],
        "extra": ["几点", "今天"],
        "fill_opts": ["八点", "老师", "喝茶"],
        "fill_answer": "八点",
        "fill_prompt": "خالی جگہ بھریں: 现在____。",
    }
    s_monday = {
        "hanzi": "今天是星期一。",
        "pinyin": "Jīntiān shì xīngqīyī.",
        "urdu": "آج پیر ہے۔",
        "parts": ["今天", "是", "星期一"],
        "extra": ["八点"],
        "fill_opts": ["星期一", "八点", "学生"],
        "fill_answer": "星期一",
        "fill_prompt": "خالی جگہ بھریں: 今天是____。",
    }
    s_see_tom = {
        "hanzi": "我们明天见面。",
        "pinyin": "Wǒmen míngtiān jiànmiàn.",
        "urdu": "ہم کل ملتے ہیں۔",
        "parts": ["我们", "明天", "见面"],
        "extra": ["今天", "八点"],
        "fill_opts": ["明天", "现在", "几点"],
        "fill_answer": "明天",
        "fill_prompt": "خالی جگہ بھریں: 我们____见面。",
    }
    s_price = {
        "hanzi": "这个多少钱？",
        "pinyin": "Zhège duōshao qián?",
        "urdu": "یہ کتنے کا ہے؟",
        "parts": ["这个", "多少", "钱"],
        "extra": ["很好"],
        "fill_opts": ["多少钱", "怎么样", "几点"],
        "fill_answer": "多少钱",
        "fill_prompt": "خالی جگہ بھریں: 这个____？",
    }
    s_expensive = {
        "hanzi": "太贵了。可以便宜一点吗？",
        "pinyin": "Tài guì le. Kěyǐ piányi yìdiǎn ma?",
        "urdu": "بہت مہنگا ہے۔ کیا تھوڑا سستا ہو سکتا ہے؟",
        "parts": ["太贵了"],
        "extra": ["很好", "谢谢"],
        "fill_opts": ["太贵了", "很好吃", "没关系"],
        "fill_answer": "太贵了",
        "fill_prompt": "مہنگا والا جملہ چنیں",
    }
    s_want = {
        "hanzi": "我要这个。",
        "pinyin": "Wǒ yào zhège.",
        "urdu": "مجھے یہ چاہیے۔",
        "parts": ["我", "要", "这个"],
        "extra": ["那个", "钱"],
        "fill_opts": ["要", "是", "有"],
        "fill_answer": "要",
        "fill_prompt": "خالی جگہ بھریں: 我____这个。",
    }
    s_cheap = {
        "hanzi": "这个很便宜。",
        "pinyin": "Zhège hěn piányi.",
        "urdu": "یہ بہت سستا ہے۔",
        "parts": ["这个", "很", "便宜"],
        "extra": ["贵", "钱"],
        "fill_opts": ["便宜", "贵", "好吃"],
        "fill_answer": "便宜",
        "fill_prompt": "خالی جگہ بھریں: 这个很____。",
    }
    s_where = {
        "hanzi": "请问，厕所在哪儿？",
        "pinyin": "Qǐngwèn, cèsuǒ zài nǎr?",
        "urdu": "معاف کیجیے، بیت الخلا کہاں ہے؟",
        "parts": ["请问", "厕所", "在哪儿"],
        "extra": ["银行"],
        "fill_opts": ["在哪儿", "多少钱", "怎么样"],
        "fill_answer": "在哪儿",
        "fill_prompt": "خالی جگہ بھریں: 厕所____？",
    }
    s_straight = {
        "hanzi": "往前走。银行在左边。",
        "pinyin": "Wǎng qián zǒu. Yínháng zài zuǒbian.",
        "urdu": "سیدھا جائیں۔ بینک بائیں طرف ہے۔",
        "parts": ["往前走"],
        "extra": ["谢谢", "再见"],
        "fill_opts": ["往前走", "太贵了", "我很好"],
        "fill_answer": "往前走",
        "fill_prompt": "سیدھا جانے والا جملہ چنیں",
    }
    s_right = {
        "hanzi": "学校在右边。",
        "pinyin": "Xuéxiào zài yòubian.",
        "urdu": "اسکول دائیں طرف ہے۔",
        "parts": ["学校", "在", "右边"],
        "extra": ["左边", "厕所"],
        "fill_opts": ["右边", "左边", "八点"],
        "fill_answer": "右边",
        "fill_prompt": "خالی جگہ بھریں: 学校在____。",
    }
    s_here = {
        "hanzi": "我就在这儿。",
        "pinyin": "Wǒ jiù zài zhèr.",
        "urdu": "میں یہیں ہوں۔",
        "parts": ["我", "就", "在这儿"],
        "extra": ["那儿", "学校"],
        "fill_opts": ["在这儿", "在哪儿", "多少钱"],
        "fill_answer": "在这儿",
        "fill_prompt": "خالی جگہ بھریں: 我就____。",
    }
    s_like = {
        "hanzi": "我喜欢中国菜。",
        "pinyin": "Wǒ xǐhuan Zhōngguó cài.",
        "urdu": "مجھے چینی کھانا پسند ہے۔",
        "parts": ["我", "喜欢", "中国菜"],
        "extra": ["咖啡"],
        "fill_opts": ["喜欢", "要", "是"],
        "fill_answer": "喜欢",
        "fill_prompt": "خالی جگہ بھریں: 我____中国菜。",
    }
    s_like_q = {
        "hanzi": "你喜欢喝茶吗？",
        "pinyin": "Nǐ xǐhuan hē chá ma?",
        "urdu": "کیا آپ کو چائے پسند ہے؟",
        "parts": ["你", "喜欢", "喝茶", "吗"],
        "extra": ["咖啡"],
        "fill_opts": ["喝茶", "吃饭", "再见"],
        "fill_answer": "喝茶",
        "fill_prompt": "خالی جگہ بھریں: 你喜欢____吗？",
    }
    s_dislike = {
        "hanzi": "我不喜欢咖啡。",
        "pinyin": "Wǒ bù xǐhuan kāfēi.",
        "urdu": "مجھے کافی پسند نہیں۔",
        "parts": ["我", "不", "喜欢", "咖啡"],
        "extra": ["茶", "菜"],
        "fill_opts": ["不喜欢", "很好", "再见"],
        "fill_answer": "不喜欢",
        "fill_prompt": "خالی جگہ بھریں: 我____咖啡。",
    }
    s_repair = {
        "hanzi": "我听不懂。请再说一遍。",
        "pinyin": "Wǒ tīng bù dǒng. Qǐng zài shuō yí biàn.",
        "urdu": "میں سمجھ نہیں پا رہا۔ براہ کرم دوبارہ کہیں۔",
        "parts": ["我", "听不懂"],
        "extra": ["喜欢", "谢谢"],
        "fill_opts": ["听不懂", "喜欢", "起床"],
        "fill_answer": "听不懂",
        "fill_prompt": "خالی جگہ بھریں: 我____。",
    }

    unit1 = add_unit(
        token, course["id"], 1, "وقت کے جملے", "时间句子",
        "几点 asks the time. 是 links today with the weekday. 明天见面 makes a plan.",
        "HSK 1", ["Daily", "Education"], 25, "Medium",
        ["Ask the time", "Say the day", "Make a tomorrow plan"],
    )
    add_learn_then_quiz(
        token,
        unit1["id"],
        "Medium",
        8,
        [s_time_q, s_time_a, s_monday, s_see_tom],
        "وقت کے جملے سیکھیں",
        "وقت کی مشق",
        "وقت کا جائزہ",
        tones=[tone("点", "diǎn", "بجے", 3)],
    )

    unit2 = add_unit(
        token, course["id"], 2, "خریداری کے جملے", "购物句子",
        "多少钱 asks price. 太…了 intensifies. 可以…吗 politely asks for a discount.",
        "HSK 1", ["Shopping", "Daily"], 25, "Medium",
        ["Ask a price", "Say it is expensive", "Buy an item"],
    )
    add_learn_then_quiz(
        token,
        unit2["id"],
        "Medium",
        8,
        [s_price, s_expensive, s_want, s_cheap],
        "خریداری کے جملے سیکھیں",
        "خریداری کی مشق",
        "خریداری کا جائزہ",
    )

    unit3 = add_unit(
        token, course["id"], 3, "جگہ اور راستہ", "问路句子",
        "在哪儿 asks location. 往前走 gives a direction. 左边 / 右边 mark left and right.",
        "HSK 1", ["Travel", "Daily"], 25, "Medium",
        ["Ask where something is", "Give a direction", "Say left or right"],
    )
    add_learn_then_quiz(
        token,
        unit3["id"],
        "Medium",
        8,
        [s_where, s_straight, s_right, s_here],
        "راستے کے جملے سیکھیں",
        "راستے کی مشق",
        "راستے کا جائزہ",
    )

    unit4 = add_unit(
        token, course["id"], 4, "پسند اور سمجھنا", "喜好句子",
        "喜欢 expresses likes. 不喜欢 is the negative. 听不懂 and 请再说一遍 repair a conversation.",
        "HSK 1", ["Food", "Daily"], 25, "Medium",
        ["Say what you like", "Say what you dislike", "Ask someone to repeat"],
    )
    add_learn_then_quiz(
        token,
        unit4["id"],
        "Medium",
        8,
        [s_like, s_like_q, s_dislike, s_repair],
        "پسند کے جملے سیکھیں",
        "پسند کی مشق",
        "گفتگو کا جائزہ",
        tones=[tone("喜", "xǐ", "پسند", 3)],
    )

    print(f"Created {MEDIUM_NAME}: 4 units, 12 lessons of full sentences.")
    return course["id"]


def existing_hanzi(token):
    payload = req("GET", "/vocabulary?page=0&size=500", token)
    data = payload.get("data") or {}
    content = data.get("content") if isinstance(data, dict) else data
    return {item.get("hanzi") for item in (content or []) if item.get("hanzi")}


def replace_course(token, name):
    names = existing_course_names(token)
    course = names.get(name)
    if not course:
        return
    req("DELETE", f"/courses/{course['id']}", token)
    print(f"Removed old {name}")


def seed_hsk_greetings(token):
    s_nihao = {
        "hanzi": "你好",
        "pinyin": "nǐ hǎo",
        "urdu": "ہیلو",
        "parts": ["你", "好"],
        "extra": ["吗", "谢"],
        "fill_opts": ["你好", "谢谢", "再见"],
        "fill_answer": "你好",
        "fill_prompt": "ہیلو والا لفظ چنیں",
    }
    s_thanks = {
        "hanzi": "谢谢",
        "pinyin": "xièxie",
        "urdu": "شکریہ",
        "parts": ["谢", "谢"],
        "extra": ["你", "好"],
        "fill_opts": ["谢谢", "你好", "再见"],
        "fill_answer": "谢谢",
        "fill_prompt": "شکریہ والا لفظ چنیں",
    }
    s_bye = {
        "hanzi": "再见",
        "pinyin": "zàijiàn",
        "urdu": "الوداع",
        "parts": ["再", "见"],
        "extra": ["你", "好"],
        "fill_opts": ["再见", "你好", "谢谢"],
        "fill_answer": "再见",
        "fill_prompt": "الوداع والا لفظ چنیں",
    }
    s_sorry = {
        "hanzi": "对不起",
        "pinyin": "duìbuqǐ",
        "urdu": "معاف کیجیے",
        "parts": ["对", "不起"],
        "extra": ["谢谢", "你好"],
        "fill_opts": ["对不起", "你好", "谢谢"],
        "fill_answer": "对不起",
        "fill_prompt": "معافی والا لفظ چنیں",
    }
    s_name_q = {
        "hanzi": "你叫什么名字？",
        "pinyin": "Nǐ jiào shénme míngzi?",
        "urdu": "آپ کا نام کیا ہے؟",
        "parts": ["你", "叫", "什么", "名字"],
        "extra": ["好"],
        "fill_opts": ["名字", "谢谢", "再见"],
        "fill_answer": "名字",
        "fill_prompt": "خالی جگہ: 你叫什么____？",
    }
    s_name_a = {
        "hanzi": "我叫",
        "pinyin": "wǒ jiào",
        "urdu": "میرا نام ہے",
        "parts": ["我", "叫"],
        "extra": ["你", "好"],
        "fill_opts": ["我", "你", "他"],
        "fill_answer": "我",
        "fill_prompt": "خالی جگہ: ____叫",
    }

    course = add_course(
        token,
        "HSK 1 Greetings",
        "HSK 1",
        "Beginner Chinese greetings and introductions for Urdu speakers.",
        "Beginner",
    )
    unit1 = add_unit(
        token, course["id"], 1, "سلام اور تعارف", "问候",
        "你好 is a greeting. 吗 turns a sentence into a yes/no question.",
        "HSK 1", ["Daily", "Culture"], 20, "Easy",
        ["Say hello", "Say thank you", "Say goodbye"],
    )
    add_learn_then_quiz(
        token,
        unit1["id"],
        "Easy",
        8,
        [s_nihao, s_thanks, s_bye, s_sorry],
        "سلام کے الفاظ سیکھیں",
        "سلام کی مشق",
        "سلام کا جائزہ",
        tones=[tone("你", "nǐ", "آپ", 3)],
    )
    unit2 = add_unit(
        token, course["id"], 2, "نام پوچھنا", "名字",
        "你叫什么名字？ asks someone's name. 我叫… answers it.",
        "HSK 1", ["Daily", "Education"], 20, "Easy",
        ["Ask a name", "Say your name"],
    )
    add_learn_then_quiz(
        token,
        unit2["id"],
        "Easy",
        6,
        [s_name_q, s_name_a],
        "نام کے جملے سیکھیں",
        "نام کی مشق",
        "نام کا جائزہ",
        tones=[tone("我", "wǒ", "میں", 3)],
    )
    print("Created HSK 1 Greetings: teach words first, then quiz.")
    return course["id"]


def seed_vocabulary(token):
    words = [
        vocab_item("你好", "nǐ hǎo", 3, "ہیلو", "hello", "phrase", 1, ["Daily"],
                   ("你好！你怎么样？", "Nǐ hǎo! Nǐ zěnmeyàng?", "ہیلو! آپ کیسے ہیں؟")),
        vocab_item("怎么样", "zěnmeyàng", 3, "کیسے ہیں", "kaise hain", "phrase", 1, ["Daily"],
                   ("你怎么样？", "Nǐ zěnmeyàng?", "آپ کیسے ہیں؟")),
        vocab_item("我很好", "wǒ hěn hǎo", 3, "میں بہت اچھا ہوں", "main bohat acha hon", "phrase", 1, ["Daily"],
                   ("我很好，谢谢！", "Wǒ hěn hǎo, xièxie!", "میں بہت اچھا ہوں، شکریہ!")),
        vocab_item("明天见", "míngtiān jiàn", 4, "کل ملتے ہیں", "kal milte hain", "phrase", 1, ["Daily"],
                   ("再见！明天见！", "Zàijiàn! Míngtiān jiàn!", "الوداع! کل ملتے ہیں!")),
        vocab_item("没关系", "méi guānxi", 2, "کوئی بات نہیں", "koi baat nahi", "phrase", 1, ["Daily"],
                   ("对不起。没关系。", "Duìbuqǐ. Méi guānxi.", "معاف کیجیے۔ کوئی بات نہیں۔")),
        vocab_item("叫", "jiào", 4, "کہلاتا ہوں", "kehlata hon", "verb", 1, ["Daily"],
                   ("我叫阿里。", "Wǒ jiào Ālǐ.", "میرا نام علی ہے۔")),
        vocab_item("名字", "míngzi", 2, "نام", "naam", "noun", 1, ["Daily"],
                   ("你叫什么名字？", "Nǐ jiào shénme míngzi?", "آپ کا نام کیا ہے؟")),
        vocab_item("认识", "rènshi", 4, "جاننا / ملنا", "milna", "verb", 1, ["Daily"],
                   ("很高兴认识你。", "Hěn gāoxìng rènshi nǐ.", "آپ سے مل کر خوشی ہوئی۔")),
        vocab_item("学生", "xuésheng", 2, "طالب علم", "talib e ilm", "noun", 1, ["Education"],
                   ("我是学生。", "Wǒ shì xuésheng.", "میں طالب علم ہوں۔")),
        vocab_item("老师", "lǎoshī", 1, "استاد", "ustad", "noun", 1, ["Education"],
                   ("你是老师吗？", "Nǐ shì lǎoshī ma?", "کیا آپ استاد ہیں؟")),
        vocab_item("爸爸", "bàba", 4, "والد", "walid", "noun", 1, ["Daily"],
                   ("这是我的爸爸。", "Zhè shì wǒ de bàba.", "یہ میرے والد ہیں۔")),
        vocab_item("妈妈", "māma", 1, "والدہ", "walida", "noun", 1, ["Daily"],
                   ("这是我的妈妈。", "Zhè shì wǒ de māma.", "یہ میری والدہ ہیں۔")),
        vocab_item("姐姐", "jiějie", 3, "بڑی بہن", "badi behan", "noun", 1, ["Daily"],
                   ("我有一个姐姐。", "Wǒ yǒu yí ge jiějie.", "میری ایک بڑی بہن ہے۔")),
        vocab_item("喝茶", "hē chá", 1, "چائے پینا", "chai peena", "phrase", 1, ["Food"],
                   ("我想喝茶。", "Wǒ xiǎng hē chá.", "میں چائے پینا چاہتا ہوں۔")),
        vocab_item("米饭", "mǐfàn", 3, "چاول", "chawal", "noun", 1, ["Food"],
                   ("你吃米饭吗？", "Nǐ chī mǐfàn ma?", "کیا آپ چاول کھاتے ہیں؟")),
        vocab_item("好吃", "hǎochī", 3, "مزیدار", "mazedar", "adjective", 1, ["Food"],
                   ("这个很好吃。", "Zhège hěn hǎochī.", "یہ بہت مزیدار ہے۔")),
        vocab_item("现在", "xiànzài", 4, "اب", "ab", "adverb", 1, ["Daily"],
                   ("现在几点？", "Xiànzài jǐ diǎn?", "اب کیا وقت ہے؟")),
        vocab_item("八点", "bā diǎn", 3, "آٹھ بجے", "aath baje", "phrase", 1, ["Daily"],
                   ("现在八点。", "Xiànzài bā diǎn.", "اب آٹھ بجے ہیں۔")),
        vocab_item("星期一", "xīngqīyī", 1, "پیر", "peer", "noun", 1, ["Daily"],
                   ("今天是星期一。", "Jīntiān shì xīngqīyī.", "آج پیر ہے۔")),
        vocab_item("多少钱", "duōshao qián", 1, "کتنے کا", "kitne ka", "phrase", 1, ["Shopping"],
                   ("这个多少钱？", "Zhège duōshao qián?", "یہ کتنے کا ہے؟")),
        vocab_item("太贵了", "tài guì le", 4, "بہت مہنگا ہے", "bohat mehnga hai", "phrase", 1, ["Shopping"],
                   ("太贵了。可以便宜一点吗？", "Tài guì le. Kěyǐ piányi yìdiǎn ma?", "بہت مہنگا ہے۔ کیا تھوڑا سستا ہو سکتا ہے؟")),
        vocab_item("便宜", "piányi", 2, "سستا", "sasta", "adjective", 1, ["Shopping"],
                   ("这个很便宜。", "Zhège hěn piányi.", "یہ بہت سستا ہے۔")),
        vocab_item("在哪儿", "zài nǎr", 3, "کہاں ہے", "kahan hai", "phrase", 1, ["Travel"],
                   ("请问，厕所在哪儿？", "Qǐngwèn, cèsuǒ zài nǎr?", "معاف کیجیے، بیت الخلا کہاں ہے؟")),
        vocab_item("往前走", "wǎng qián zǒu", 3, "سیدھا جائیں", "seedha jayen", "phrase", 1, ["Travel"],
                   ("往前走。银行在左边。", "Wǎng qián zǒu. Yínháng zài zuǒbian.", "سیدھا جائیں۔ بینک بائیں طرف ہے۔")),
        vocab_item("右边", "yòubian", 4, "دائیں طرف", "dayen taraf", "noun", 1, ["Travel"],
                   ("学校在右边。", "Xuéxiào zài yòubian.", "اسکول دائیں طرف ہے۔")),
        vocab_item("喜欢", "xǐhuan", 3, "پسند کرنا", "pasand karna", "verb", 1, ["Food"],
                   ("我喜欢中国菜。", "Wǒ xǐhuan Zhōngguó cài.", "مجھے چینی کھانا پسند ہے۔")),
        vocab_item("咖啡", "kāfēi", 1, "کافی", "coffee", "noun", 1, ["Food"],
                   ("我不喜欢咖啡。", "Wǒ bù xǐhuan kāfēi.", "مجھے کافی پسند نہیں۔")),
        vocab_item("听不懂", "tīng bù dǒng", 1, "سمجھ نہیں پا رہا", "samajh nahi pa raha", "phrase", 1, ["Daily"],
                   ("我听不懂。请再说一遍。", "Wǒ tīng bù dǒng. Qǐng zài shuō yí biàn.", "میں سمجھ نہیں پا رہا۔ براہ کرم دوبارہ کہیں۔")),
        vocab_item("请再说一遍", "qǐng zài shuō yí biàn", 4, "براہ کرم دوبارہ کہیں", "dobara kahen", "phrase", 1, ["Daily"],
                   ("请再说一遍。", "Qǐng zài shuō yí biàn.", "براہ کرم دوبارہ کہیں۔")),
        vocab_item("起床", "qǐchuáng", 3, "اٹھنا", "uthna", "verb", 1, ["Daily"],
                   ("我八点起床。", "Wǒ bā diǎn qǐchuáng.", "میں آٹھ بجے اٹھتا ہوں۔")),
    ]
    existing = existing_hanzi(token)
    words = [word for word in words if word["hanzi"] not in existing]
    if not words:
        print("Vocabulary already present. Skipping.")
        return
    created = req("POST", "/vocabulary/bulk", token, words)
    count = len(created.get("data") or [])
    print(f"Uploaded {count} vocabulary items with example sentences.")


def main():
    token = req(
        "POST",
        "/auth/login",
        body={"email": "admin@nihao-urdu.com", "password": "admin123"},
    )["data"]["token"]

    for name in ("HSK 1 Greetings", EASY_NAME, MEDIUM_NAME):
        replace_course(token, name)

    seed_hsk_greetings(token)
    seed_easy(token)
    seed_medium(token)
    seed_vocabulary(token)
    print("Done. Lessons now teach all words first, then the quiz.")


if __name__ == "__main__":
    main()
