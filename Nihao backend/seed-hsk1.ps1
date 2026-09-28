$ErrorActionPreference = "Stop"
$base = "http://localhost:8089/api"

function U {
    param([int[]]$Codes)
    -join ($Codes | ForEach-Object { [char]$_ })
}

# Chinese
$NIHAO     = (U 0x4F60,0x597D)
$XIESIE    = (U 0x8C22,0x8C22)
$ZAIJIAN   = (U 0x518D,0x89C1)
$DUI        = (U 0x5BF9,0x4E0D,0x8D77)
$NI         = (U 0x4F60)
$HAO        = (U 0x597D)
$ZAI        = (U 0x518D)
$JIAN       = (U 0x89C1)
$XIE        = (U 0x8C22)

# Urdu
$HELLO     = (U 0x06C1,0x06CC,0x0644,0x0648)
$THANKS    = (U 0x0634,0x06A9,0x0631,0x06CC,0x06C1)
$BYE       = (U 0x0627,0x0644,0x0648,0x062F,0x0627,0x0639)
$SORRY     = (U 0x0645,0x0639,0x0627,0x0641,0x0020,0x06A9,0x06CC,0x062C,0x06CC,0x06D2)
$LEARN     = (U 0x0627,0x0633,0x0020,0x0644,0x0641,0x0638,0x0020,0x06A9,0x0648,0x0020,0x0633,0x06CC,0x06A9,0x06BE,0x06CC,0x06BA)
$PICK      = (U 0x0635,0x062D,0x06CC,0x062D,0x0020,0x0644,0x0641,0x0638,0x0020,0x0686,0x0646,0x06CC,0x06BA)
$TONEUR    = (U 0x0644,0x06C1,0x062C,0x06C1,0x0020,0x0686,0x0646,0x06CC,0x06BA)
$FILLUR    = (U 0x062E,0x0627,0x0644,0x06CC,0x0020,0x062C,0x06AF,0x06C1,0x0020,0x0628,0x06BE,0x0631,0x06CC,0x06BA)
$BUILDUR   = (U 0x0627,0x0644,0x0641,0x0627,0x0638,0x0020,0x062F,0x0631,0x0633,0x062A,0x0020,0x062A,0x0631,0x062A,0x06CC,0x0628,0x0020,0x0645,0x06CC,0x06BA,0x0020,0x0686,0x0646,0x06CC,0x06BA)
$UNIT_UR   = (U 0x062A,0x0639,0x0627,0x0631,0x0641,0x0020,0x0627,0x0648,0x0631,0x0020,0x0633,0x0644,0x0627,0x0645)
$L1        = $HELLO
$L2        = $THANKS
$L3        = $BYE
$L4        = (U 0x062C,0x0645,0x0644,0x06D2,0x0020,0x0628,0x0646,0x0627,0x0626,0x06CC,0x06BA)
$L5        = (U 0x0686,0x06CC,0x06A9,0x0020,0x067E,0x0648,0x0627,0x0626,0x0646,0x0679)
$COURSE_D  = (U 0x06CC,0x06C1,0x0020,0x06A9,0x0648,0x0631,0x0633,0x0020,0x0628,0x0646,0x06CC,0x0627,0x062F,0x06CC,0x0020,0x0686,0x06CC,0x0646,0x06CC,0x0020,0x0633,0x0644,0x0627,0x0645,0x0020,0x0633,0x06A9,0x06BE,0x0627,0x062A,0x0627,0x0020,0x06C1,0x06D2,0x06D4)

function Invoke-Api {
    param($Method, $Path, $Body = $null, $Token = $null)
    $headers = @{ Accept = "application/json" }
    if ($Token) { $headers.Authorization = "Bearer $Token" }
    $params = @{
        Method      = $Method
        Uri         = "$base$Path"
        Headers     = $headers
        ContentType = "application/json; charset=utf-8"
    }
    if ($null -ne $Body) {
        $json = $Body | ConvertTo-Json -Depth 12 -Compress
        $params.Body = [System.Text.Encoding]::UTF8.GetBytes($json)
    }
    Invoke-RestMethod @params
}

Write-Host "Logging in as admin..."
$login = Invoke-Api POST "/auth/login" @{ email = "admin@nihao-urdu.com"; password = "admin123" }
$token = $login.data.token
if (-not $token) { throw "Login failed" }

Write-Host "Removing old content..."
$courses = (Invoke-Api GET "/courses?page=0&size=200" -Token $token).data.content
foreach ($c in @($courses)) {
    $units = @((Invoke-Api GET "/units/course/$($c.id)" -Token $token).data)
    foreach ($u in $units) {
        if (-not $u.id) { continue }
        $lessons = @((Invoke-Api GET "/lessons/unit/$($u.id)" -Token $token).data)
        foreach ($l in $lessons) {
            if (-not $l.id) { continue }
            $exs = @((Invoke-Api GET "/exercises/lesson/$($l.id)" -Token $token).data)
            foreach ($e in $exs) {
                if ($e.id) { Invoke-Api DELETE "/exercises/$($e.id)" -Token $token | Out-Null }
            }
            Invoke-Api DELETE "/lessons/$($l.id)" -Token $token | Out-Null
        }
        Invoke-Api DELETE "/units/$($u.id)" -Token $token | Out-Null
    }
    Invoke-Api DELETE "/courses/$($c.id)" -Token $token | Out-Null
}
$vocab = @((Invoke-Api GET "/vocabulary?page=0&size=500" -Token $token).data.content)
foreach ($v in $vocab) {
    if ($v.id) { Invoke-Api DELETE "/vocabulary/$($v.id)" -Token $token | Out-Null }
}

function New-Ex($lessonId, $order, $type, $data) {
    Invoke-Api POST "/exercises/lesson/$lessonId" -Token $token -Body @{
        exerciseType  = $type
        exerciseOrder = $order
        exerciseData  = $data
    } | Out-Null
}

function Teach($word, $pinyin, $urdu) {
    @{
        promptUr = $LEARN
        promptEn = "Learn this word"
        word     = $word
        items    = @($word)
        answer   = $word
        pinyin   = $pinyin
        urdu     = $urdu
        gloss    = @(@{ ur = $urdu; en = $word })
    }
}

function Choice($options, $answer, $pinyin, $urdu, $en) {
    @{
        promptUr = $PICK
        promptEn = $en
        options  = $options
        items    = $options
        answer   = $answer
        word     = $answer
        pinyin   = $pinyin
        urdu     = $urdu
        gloss    = @(@{ ur = $urdu; en = $answer })
    }
}

function ToneData($word, $pinyin, $urdu, $tone) {
    @{
        promptUr = $TONEUR
        promptEn = "Pick the tone"
        word     = $word
        items    = @($word)
        answer   = "$word$tone"
        pinyin   = $pinyin
        urdu     = $urdu
        gloss    = @(@{ ur = $urdu; en = $word })
    }
}

function BuildData($items, $answerParts, $urdu, $en) {
    @{
        promptUr = $BUILDUR
        promptEn = $en
        items    = $items
        options  = $items
        answer   = ($answerParts -join " ")
        urdu     = $urdu
        gloss    = @(@{ ur = $urdu; en = ($answerParts -join "") })
    }
}

Write-Host "Creating vocabulary..."
@(
    @{ hanzi = $NIHAO; pinyin = "ni hao"; tone = 3; urduTranslation = $HELLO; romanUrdu = "hello"; partOfSpeech = "phrase"; hskLevel = 1 }
    @{ hanzi = $XIESIE; pinyin = "xie xie"; tone = 4; urduTranslation = $THANKS; romanUrdu = "shukriya"; partOfSpeech = "verb"; hskLevel = 1 }
    @{ hanzi = $ZAIJIAN; pinyin = "zai jian"; tone = 4; urduTranslation = $BYE; romanUrdu = "alwida"; partOfSpeech = "phrase"; hskLevel = 1 }
    @{ hanzi = $DUI; pinyin = "dui bu qi"; tone = 4; urduTranslation = $SORRY; romanUrdu = "maaf"; partOfSpeech = "phrase"; hskLevel = 1 }
) | ForEach-Object { Invoke-Api POST "/vocabulary" -Token $token -Body $_ | Out-Null }

Write-Host "Creating course..."
$course = (Invoke-Api POST "/courses" -Token $token -Body @{
    name        = "Basic Chinese (HSK 1)"
    hskLevel    = "HSK 1"
    description = $COURSE_D
    difficulty  = "Beginner"
    status      = "PUBLISHED"
}).data

Write-Host "Creating unit..."
$unit = (Invoke-Api POST "/units/course/$($course.id)" -Token $token -Body @{
    unitNumber         = 1
    urduTitle          = $UNIT_UR
    hanziTitle         = $NIHAO
    grammarPoint       = "Greetings"
    hskLevel           = "HSK 1"
    estimatedTime      = 20
    difficulty         = "Easy"
    topics             = @("Greetings")
    learningObjectives = @("Say hello", "Say thank you", "Say goodbye")
    status             = "PUBLISHED"
}).data

function New-Lesson($num, $type, $title, $instruction) {
    (Invoke-Api POST "/lessons/unit/$($unit.id)" -Token $token -Body @{
        lessonNumber    = $num
        lessonType      = $type
        urduTitle       = $title
        instructionText = $instruction
        difficulty      = "Easy"
        crowns          = 3
        status          = "PUBLISHED"
        wordsCount      = 4
    }).data
}

$all4 = @($NIHAO, $XIESIE, $ZAIJIAN, $DUI)

Write-Host "Lesson 1 Hello..."
$l1 = New-Lesson 1 "NORMAL" $L1 "Learn nihao"
New-Ex $l1.id 1 "TEACH_FRAME" (Teach $NIHAO "ni hao" $HELLO)
New-Ex $l1.id 2 "PICTURE_MATCH" (Choice $all4 $NIHAO "ni hao" $HELLO "Choose Hello")
New-Ex $l1.id 3 "LISTENING_CHOICE" (Choice @($XIESIE, $NIHAO, $ZAIJIAN, $DUI) $NIHAO "ni hao" $HELLO "Listen and choose Hello")
New-Ex $l1.id 4 "TONE_DRILL" (ToneData $NIHAO "ni hao" $HELLO "3")
New-Ex $l1.id 5 "FILL_IN_THE_BLANK" (Choice @($NIHAO, $XIESIE, $ZAIJIAN) $NIHAO "ni hao" $HELLO "Fill the blank")

Write-Host "Lesson 2 Thanks..."
$l2 = New-Lesson 2 "NORMAL" $L2 "Learn xiexie"
New-Ex $l2.id 1 "TEACH_FRAME" (Teach $XIESIE "xie xie" $THANKS)
New-Ex $l2.id 2 "PICTURE_MATCH" (Choice $all4 $XIESIE "xie xie" $THANKS "Choose Thank you")
New-Ex $l2.id 3 "LISTENING_CHOICE" (Choice @($ZAIJIAN, $DUI, $XIESIE, $NIHAO) $XIESIE "xie xie" $THANKS "Listen and choose Thank you")
New-Ex $l2.id 4 "TONE_DRILL" (ToneData $XIESIE "xie xie" $THANKS "4")
New-Ex $l2.id 5 "FILL_IN_THE_BLANK" (Choice @($NIHAO, $XIESIE, $ZAIJIAN) $XIESIE "xie xie" $THANKS "Fill the blank")

Write-Host "Lesson 3 Goodbye..."
$l3 = New-Lesson 3 "NORMAL" $L3 "Learn zaijian"
New-Ex $l3.id 1 "TEACH_FRAME" (Teach $ZAIJIAN "zai jian" $BYE)
New-Ex $l3.id 2 "PICTURE_MATCH" (Choice $all4 $ZAIJIAN "zai jian" $BYE "Choose Goodbye")
New-Ex $l3.id 3 "LISTENING_CHOICE" (Choice @($NIHAO, $ZAIJIAN, $XIESIE, $DUI) $ZAIJIAN "zai jian" $BYE "Listen and choose Goodbye")
New-Ex $l3.id 4 "TONE_DRILL" (ToneData $ZAIJIAN "zai jian" $BYE "4")
New-Ex $l3.id 5 "TAP_TO_BUILD" (BuildData @($ZAI, $JIAN, $NI, $HAO) @($ZAI, $JIAN) $BYE "Tap goodbye in order")

Write-Host "Lesson 4 Build..."
$l4 = New-Lesson 4 "NORMAL" $L4 "Build greeting sentences"
New-Ex $l4.id 1 "TEACH_FRAME" (Teach $DUI "dui bu qi" $SORRY)
New-Ex $l4.id 2 "TAP_TO_BUILD" (BuildData @($NI, $HAO) @($NI, $HAO) $HELLO "Build Hello")
New-Ex $l4.id 3 "TAP_TO_BUILD" (BuildData @($XIE, $XIE, $ZAI, $JIAN) @($XIE, $XIE) $THANKS "Build Thank you")
New-Ex $l4.id 4 "PICTURE_MATCH" (Choice $all4 $DUI "dui bu qi" $SORRY "Choose Sorry")
New-Ex $l4.id 5 "FILL_IN_THE_BLANK" (Choice @($NIHAO, $DUI, $ZAIJIAN) $DUI "dui bu qi" $SORRY "Fill the blank")

Write-Host "Lesson 5 Checkpoint..."
$l5 = New-Lesson 5 "CHECKPOINT" $L5 "Review greetings"
New-Ex $l5.id 1 "PICTURE_MATCH" (Choice $all4 $NIHAO "ni hao" $HELLO "Choose Hello")
New-Ex $l5.id 2 "LISTENING_CHOICE" (Choice @($XIESIE, $ZAIJIAN, $NIHAO, $DUI) $XIESIE "xie xie" $THANKS "Choose Thank you")
New-Ex $l5.id 3 "TAP_TO_BUILD" (BuildData @($ZAI, $JIAN, $NI) @($ZAI, $JIAN) $BYE "Build Goodbye")
New-Ex $l5.id 4 "FILL_IN_THE_BLANK" (Choice @($NIHAO, $XIESIE, $DUI) $DUI "dui bu qi" $SORRY "Fill the blank")
New-Ex $l5.id 5 "TONE_DRILL" (ToneData $NIHAO "ni hao" $HELLO "3")

Write-Host "Created course: $($course.name)"
Write-Host "Unit + 5 lessons, 5 exercises each. Refresh admin and reopen the app."
