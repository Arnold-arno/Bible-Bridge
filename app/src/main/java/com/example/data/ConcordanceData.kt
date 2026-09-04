package com.example.data

data class ConcordanceEntry(
    val word: String,
    val partOfSpeech: String,
    val meaning: String,
    val occurrences: List<String>,
    val seeAlso: List<String>,
    val greekWord: String = "",
    val transliteration: String = "",
    val strongsNumber: String = "",
    val usedCount: Int = 0,
    val firstMention: String = "",
    val lastMention: String = ""
)

object ConcordanceData {
    val entries = listOf(
        ConcordanceEntry(
            word = "Love",
            partOfSpeech = "noun / verb",
            meaning = "God's self-giving affection and active benevolence; unconditional commitment to the well-being of others.",
            occurrences = listOf(
                "Genesis 22:2",
                "Leviticus 19:18",
                "Deuteronomy 6:5",
                "Psalm 136:1",
                "Matthew 22:37",
                "John 3:16",
                "Romans 5:8",
                "1 Corinthians 13:4",
                "1 John 4:8"
            ),
            seeAlso = listOf("Grace", "Mercy", "Compassion", "Faith", "Kindness"),
            greekWord = "ἀγάπη / ἀγαπάω",
            transliteration = "agape / agapao",
            strongsNumber = "G26 / G25",
            usedCount = 320,
            firstMention = "Genesis 22:2",
            lastMention = "Revelation 2:4"
        ),
        ConcordanceEntry(
            word = "Faith",
            partOfSpeech = "noun",
            meaning = "Trust, assurance, and loyalty directed toward God; acting on the conviction of things unseen.",
            occurrences = listOf(
                "Matthew 8:10",
                "Mark 5:34",
                "John 3:16",
                "Romans 10:17",
                "Hebrews 11:1",
                "James 2:17"
            ),
            seeAlso = listOf("Belief", "Trust", "Hope", "Obedience", "Grace"),
            greekWord = "πίστις",
            transliteration = "pistis",
            strongsNumber = "G4102",
            usedCount = 243,
            firstMention = "Habakkuk 2:4",
            lastMention = "Revelation 14:12"
        ),
        ConcordanceEntry(
            word = "Grace",
            partOfSpeech = "noun",
            meaning = "Unmerited divine favor, help, and empowerment bestowed on mankind by God.",
            occurrences = listOf(
                "Genesis 6:8",
                "John 1:17",
                "Romans 3:24",
                "Ephesians 2:8",
                "2 Corinthians 12:9",
                "Hebrews 4:16"
            ),
            seeAlso = listOf("Mercy", "Gift", "Love", "Salvation", "Peace"),
            greekWord = "χάρις",
            transliteration = "charis",
            strongsNumber = "G5485",
            usedCount = 155,
            firstMention = "Genesis 6:8",
            lastMention = "Revelation 22:21"
        ),
        ConcordanceEntry(
            word = "Peace",
            partOfSpeech = "noun",
            meaning = "Completeness, wholeness, safety, and reconciliation with God and neighbor; flourishing tranquility.",
            occurrences = listOf(
                "Numbers 6:26",
                "Psalm 29:11",
                "Isaiah 9:6",
                "John 14:27",
                "Romans 5:1",
                "Philippians 4:7"
            ),
            seeAlso = listOf("Grace", "Hope", "Righteousness", "Rest", "Covenant"),
            greekWord = "εἰρήνη / שָׁלוֹם",
            transliteration = "eirene / shalom",
            strongsNumber = "G1515 / H7965",
            usedCount = 429,
            firstMention = "Genesis 15:15",
            lastMention = "Revelation 1:4"
        ),
        ConcordanceEntry(
            word = "Hope",
            partOfSpeech = "noun / verb",
            meaning = "Confident expectation of good based on God's promises and character, rather than mere wishful thinking.",
            occurrences = listOf(
                "Psalm 42:11",
                "Jeremiah 29:11",
                "Romans 5:5",
                "Romans 8:24",
                "Hebrews 6:19",
                "1 Peter 1:3"
            ),
            seeAlso = listOf("Faith", "Trust", "Patience", "Peace", "Glory"),
            greekWord = "ἐλπίς",
            transliteration = "elpis",
            strongsNumber = "G1680",
            usedCount = 53,
            firstMention = "Ruth 1:12",
            lastMention = "1 John 3:3"
        ),
        ConcordanceEntry(
            word = "Mercy",
            partOfSpeech = "noun",
            meaning = "Compassion or forbearance shown to an offender or someone under one's power; loving-kindness in action.",
            occurrences = listOf(
                "Exodus 34:6",
                "Psalm 23:6",
                "Psalm 136:1",
                "Lamentations 3:22",
                "Matthew 5:7",
                "Luke 10:37",
                "Ephesians 2:4"
            ),
            seeAlso = listOf("Grace", "Love", "Compassion", "Forgiveness", "Pity"),
            greekWord = "ἔλεος / חֶסֶד",
            transliteration = "eleos / chesed",
            strongsNumber = "G1656 / H2617",
            usedCount = 340,
            firstMention = "Genesis 19:19",
            lastMention = "Jude 1:21"
        ),
        ConcordanceEntry(
            word = "Covenant",
            partOfSpeech = "noun",
            meaning = "A solemn, binding agreement establishing a relationship between God and His people, marked by promises and responsibilities.",
            occurrences = listOf(
                "Genesis 9:9",
                "Genesis 15:18",
                "Exodus 19:5",
                "Jeremiah 31:31",
                "Luke 22:20",
                "Hebrews 8:6"
            ),
            seeAlso = listOf("Promise", "Law", "Covenant-faithfulness", "Truth"),
            greekWord = "διαθήκη / בְּרִית",
            transliteration = "diatheke / berith",
            strongsNumber = "G1242 / H1285",
            usedCount = 292,
            firstMention = "Genesis 6:18",
            lastMention = "Revelation 11:19"
        ),
        ConcordanceEntry(
            word = "Wisdom",
            partOfSpeech = "noun",
            meaning = "Practical insight, understanding, and skill for godly living, aligned with God's design for creation.",
            occurrences = listOf(
                "Job 28:28",
                "Psalm 111:10",
                "Proverbs 1:7",
                "Proverbs 3:13",
                "1 Corinthians 1:30",
                "James 1:5",
                "James 3:17"
            ),
            seeAlso = listOf("Knowledge", "Understanding", "Prudence", "Truth", "Fear of the Lord"),
            greekWord = "σοφία / חָכְמָה",
            transliteration = "sophia / chokmah",
            strongsNumber = "G4678 / H2451",
            usedCount = 224,
            firstMention = "Exodus 28:3",
            lastMention = "Revelation 7:12"
        ),
        ConcordanceEntry(
            word = "Glory",
            partOfSpeech = "noun",
            meaning = "The majestic weight, radiance, and manifest presence of God's character and honor.",
            occurrences = listOf(
                "Exodus 33:18",
                "Psalm 19:1",
                "Isaiah 6:3",
                "Luke 2:14",
                "John 1:14",
                "Romans 3:23",
                "Revelation 21:23"
            ),
            seeAlso = listOf("Majesty", "Honor", "Light", "Presence", "Praise"),
            greekWord = "δόξα / כָּבוֹד",
            transliteration = "doxa / kavod",
            strongsNumber = "G1391 / H3519",
            usedCount = 371,
            firstMention = "Genesis 31:1",
            lastMention = "Revelation 21:23"
        ),
        ConcordanceEntry(
            word = "Righteousness",
            partOfSpeech = "noun",
            meaning = "Moral integrity, justice, and being in right relationship with God and other people; conformity to God's standard.",
            occurrences = listOf(
                "Genesis 15:6",
                "Psalm 106:3",
                "Isaiah 61:10",
                "Matthew 5:6",
                "Romans 1:17",
                "Romans 3:22",
                "2 Corinthians 5:21"
            ),
            seeAlso = listOf("Justice", "Holiness", "Truth", "Faithfulness", "Law"),
            greekWord = "δικαιοσύνη / צְדָקָה",
            transliteration = "dikaiosyne / tzedakah",
            strongsNumber = "G1343 / H6666",
            usedCount = 306,
            firstMention = "Genesis 15:6",
            lastMention = "Revelation 19:11"
        )
    )
}
