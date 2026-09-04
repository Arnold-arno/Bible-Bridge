package com.example.data

object BibleData {
    fun getSeedVerses(): List<BibleVerse> {
        val list = mutableListOf<BibleVerse>()

        // ------------------ KING JAMES VERSION (KJV) ------------------
        
        // Genesis 1:1-5
        list.add(BibleVerse(translation = "KJV", bookName = "Genesis", chapter = 1, verseNumber = 1, 
            text = "In the beginning God created the heaven and the earth."))
        list.add(BibleVerse(translation = "KJV", bookName = "Genesis", chapter = 1, verseNumber = 2, 
            text = "And the earth was without form, and void; and darkness was upon the face of the deep. And the Spirit of God moved upon the face of the waters."))
        list.add(BibleVerse(translation = "KJV", bookName = "Genesis", chapter = 1, verseNumber = 3, 
            text = "And God said, Let there be light: and there was light."))
        list.add(BibleVerse(translation = "KJV", bookName = "Genesis", chapter = 1, verseNumber = 4, 
            text = "And God saw the light, that it was good: and God divided the light from the darkness."))
        list.add(BibleVerse(translation = "KJV", bookName = "Genesis", chapter = 1, verseNumber = 5, 
            text = "And God called the light Day, and the darkness he called Night. And the evening and the morning were the first day."))

        // Genesis 1:26-27
        list.add(BibleVerse(translation = "KJV", bookName = "Genesis", chapter = 1, verseNumber = 26, 
            text = "And God said, Let us make man in our image, after our likeness: and let them have dominion over the fish of the sea, and over the fowl of the air, and over the cattle, and over all the earth, and over every creeping thing that creepeth upon the earth."))
        list.add(BibleVerse(translation = "KJV", bookName = "Genesis", chapter = 1, verseNumber = 27, 
            text = "So God created man in his own image, in the image of God created he him; male and female created he them."))

        // Psalm 23:1-6
        list.add(BibleVerse(translation = "KJV", bookName = "Psalms", chapter = 23, verseNumber = 1, 
            text = "The LORD is my shepherd; I shall not want."))
        list.add(BibleVerse(translation = "KJV", bookName = "Psalms", chapter = 23, verseNumber = 2, 
            text = "He maketh me to lie down in green pastures: he leadeth me beside the still waters."))
        list.add(BibleVerse(translation = "KJV", bookName = "Psalms", chapter = 23, verseNumber = 3, 
            text = "He restoreth my soul: he leadeth me in the paths of righteousness for his name's sake."))
        list.add(BibleVerse(translation = "KJV", bookName = "Psalms", chapter = 23, verseNumber = 4, 
            text = "Yea, though I walk through the valley of the shadow of death, I will fear no evil: for thou art with me; thy rod and thy staff they comfort me."))
        list.add(BibleVerse(translation = "KJV", bookName = "Psalms", chapter = 23, verseNumber = 5, 
            text = "Thou preparest a table before me in the presence of mine enemies: thou anointest my head with oil; my cup runneth over."))
        list.add(BibleVerse(translation = "KJV", bookName = "Psalms", chapter = 23, verseNumber = 6, 
            text = "Surely goodness and mercy shall follow me all the days of my life: and I will dwell in the house of the LORD for ever."))

        // Proverbs 3:5-6
        list.add(BibleVerse(translation = "KJV", bookName = "Proverbs", chapter = 3, verseNumber = 5, 
            text = "Trust in the LORD with all thine heart; and lean not unto thine own understanding."))
        list.add(BibleVerse(translation = "KJV", bookName = "Proverbs", chapter = 3, verseNumber = 6, 
            text = "In all thy ways acknowledge him, and he shall direct thy paths."))

        // Isaiah 9:6
        list.add(BibleVerse(translation = "KJV", bookName = "Isaiah", chapter = 9, verseNumber = 6, 
            text = "For unto us a child is born, unto us a son is given: and the government shall be upon his shoulder: and his name shall be called Wonderful, Counsellor, The mighty God, The everlasting Father, The Prince of Peace."))

        // Matthew 5:3-9 (The Beatitudes)
        list.add(BibleVerse(translation = "KJV", bookName = "Matthew", chapter = 5, verseNumber = 3, 
            text = "Blessed are the poor in spirit: for theirs is the kingdom of heaven."))
        list.add(BibleVerse(translation = "KJV", bookName = "Matthew", chapter = 5, verseNumber = 4, 
            text = "Blessed are they that mourn: for they shall be comforted."))
        list.add(BibleVerse(translation = "KJV", bookName = "Matthew", chapter = 5, verseNumber = 5, 
            text = "Blessed are the meek: for they shall inherit the earth."))
        list.add(BibleVerse(translation = "KJV", bookName = "Matthew", chapter = 5, verseNumber = 6, 
            text = "Blessed are they which do hunger and thirst after righteousness: for they shall be filled."))
        list.add(BibleVerse(translation = "KJV", bookName = "Matthew", chapter = 5, verseNumber = 7, 
            text = "Blessed are the merciful: for they shall obtain mercy."))
        list.add(BibleVerse(translation = "KJV", bookName = "Matthew", chapter = 5, verseNumber = 8, 
            text = "Blessed are the pure in heart: for they shall see God."))
        list.add(BibleVerse(translation = "KJV", bookName = "Matthew", chapter = 5, verseNumber = 9, 
            text = "Blessed are the peacemakers: for they shall be called the children of God."))

        // John 1:1-5
        list.add(BibleVerse(translation = "KJV", bookName = "John", chapter = 1, verseNumber = 1, 
            text = "In the beginning was the Word, and the Word was with God, and the Word was God."))
        list.add(BibleVerse(translation = "KJV", bookName = "John", chapter = 1, verseNumber = 2, 
            text = "The same was in the beginning with God."))
        list.add(BibleVerse(translation = "KJV", bookName = "John", chapter = 1, verseNumber = 3, 
            text = "All things were made by him; and without him was not any thing made that was made."))
        list.add(BibleVerse(translation = "KJV", bookName = "John", chapter = 1, verseNumber = 4, 
            text = "In him was life; and the life was the light of men."))
        list.add(BibleVerse(translation = "KJV", bookName = "John", chapter = 1, verseNumber = 5, 
            text = "And the light shineth in darkness; and the darkness comprehended it not."))

        // John 3:16
        list.add(BibleVerse(translation = "KJV", bookName = "John", chapter = 3, verseNumber = 16, 
            text = "For God so loved the world, that he gave his only begotten Son, that whosoever believeth in him should not perish, but have everlasting life."))

        // Romans 8:28, 38-39
        list.add(BibleVerse(translation = "KJV", bookName = "Romans", chapter = 8, verseNumber = 28, 
            text = "And we know that all things work together for good to them that love God, to them who are the called according to his purpose."))
        list.add(BibleVerse(translation = "KJV", bookName = "Romans", chapter = 8, verseNumber = 38, 
            text = "For I am persuaded, that neither death, nor life, nor angels, nor principalities, nor powers, nor things present, nor things to come,"))
        list.add(BibleVerse(translation = "KJV", bookName = "Romans", chapter = 8, verseNumber = 39, 
            text = "Nor height, nor depth, nor any other creature, shall be able to separate us from the love of God, which is in Christ Jesus our Lord."))


        // ------------------ GOOD NEWS VERSION (GNT) ------------------
        
        // Genesis 1:1-5
        list.add(BibleVerse(translation = "GNT", bookName = "Genesis", chapter = 1, verseNumber = 1, 
            text = "In the beginning, when God created the universe, the earth was formless and desolate."))
        list.add(BibleVerse(translation = "GNT", bookName = "Genesis", chapter = 1, verseNumber = 2, 
            text = "The raging ocean that covered everything was engulfed in total darkness, and the Spirit of God was moving over the water."))
        list.add(BibleVerse(translation = "GNT", bookName = "Genesis", chapter = 1, verseNumber = 3, 
            text = "Then God commanded, 'Let there be light'—and light appeared."))
        list.add(BibleVerse(translation = "GNT", bookName = "Genesis", chapter = 1, verseNumber = 4, 
            text = "God was pleased with what he saw. Then he separated the light from the darkness."))
        list.add(BibleVerse(translation = "GNT", bookName = "Genesis", chapter = 1, verseNumber = 5, 
            text = "God named the light 'Day' and the darkness 'Night.' Evening passed and morning came—that was the first day."))

        // Genesis 1:26-27
        list.add(BibleVerse(translation = "GNT", bookName = "Genesis", chapter = 1, verseNumber = 26, 
            text = "Then God said, 'And now we will make human beings; they will be like us and resemble us. They will have power over the fish, the birds, and all animals, domestic and wild.'"))
        list.add(BibleVerse(translation = "GNT", bookName = "Genesis", chapter = 1, verseNumber = 27, 
            text = "So God created human beings, making them to be like himself. He created them male and female."))

        // Psalm 23:1-6
        list.add(BibleVerse(translation = "GNT", bookName = "Psalms", chapter = 23, verseNumber = 1, 
            text = "The LORD is my shepherd; I have everything I need."))
        list.add(BibleVerse(translation = "GNT", bookName = "Psalms", chapter = 23, verseNumber = 2, 
            text = "He lets me rest in fields of green grass and leads me to quiet pools of fresh water."))
        list.add(BibleVerse(translation = "GNT", bookName = "Psalms", chapter = 23, verseNumber = 3, 
            text = "He gives me new strength. He guides me in the right paths, as he has promised."))
        list.add(BibleVerse(translation = "GNT", bookName = "Psalms", chapter = 23, verseNumber = 4, 
            text = "Even if I go through the deepest darkness, I will not be afraid, LORD, for you are with me. Your shepherd's rod and staff protect me."))
        list.add(BibleVerse(translation = "GNT", bookName = "Psalms", chapter = 23, verseNumber = 5, 
            text = "You prepare a banquet for me, where all my enemies can see me; you welcome me as an honored guest and fill my cup to the brim."))
        list.add(BibleVerse(translation = "GNT", bookName = "Psalms", chapter = 23, verseNumber = 6, 
            text = "I know that your goodness and love will be with me all my life, and your house will be my home as long as I live."))

        // Proverbs 3:5-6
        list.add(BibleVerse(translation = "GNT", bookName = "Proverbs", chapter = 3, verseNumber = 5, 
            text = "Trust in the LORD with all your heart. Never rely on what you think you know."))
        list.add(BibleVerse(translation = "GNT", bookName = "Proverbs", chapter = 3, verseNumber = 6, 
            text = "Remember the LORD in everything you do, and he will show you the right path."))

        // Isaiah 9:6
        list.add(BibleVerse(translation = "GNT", bookName = "Isaiah", chapter = 9, verseNumber = 6, 
            text = "A child is born to us! A son is given to us! And he will be our ruler. He will be called 'Wonderful Counselor,' 'Mighty God,' 'Eternal Father,' 'Prince of Peace.'"))

        // Matthew 5:3-9
        list.add(BibleVerse(translation = "GNT", bookName = "Matthew", chapter = 5, verseNumber = 3, 
            text = "Happy are those who know they are spiritually poor; the Kingdom of heaven belongs to them!"))
        list.add(BibleVerse(translation = "GNT", bookName = "Matthew", chapter = 5, verseNumber = 4, 
            text = "Happy are those who mourn; God will comfort them!"))
        list.add(BibleVerse(translation = "GNT", bookName = "Matthew", chapter = 5, verseNumber = 5, 
            text = "Happy are those who are humble; they will receive what God has promised!"))
        list.add(BibleVerse(translation = "GNT", bookName = "Matthew", chapter = 5, verseNumber = 6, 
            text = "Happy are those whose greatest desire is to do what God requires; God will satisfy them fully!"))
        list.add(BibleVerse(translation = "GNT", bookName = "Matthew", chapter = 5, verseNumber = 7, 
            text = "Happy are those who are merciful to others; God will be merciful to them!"))
        list.add(BibleVerse(translation = "GNT", bookName = "Matthew", chapter = 5, verseNumber = 8, 
            text = "Happy are the pure in heart; they will see God!"))
        list.add(BibleVerse(translation = "GNT", bookName = "Matthew", chapter = 5, verseNumber = 9, 
            text = "Happy are those who work for peace; God will call them his children!"))

        // John 1:1-5, 3:16
        list.add(BibleVerse(translation = "GNT", bookName = "John", chapter = 1, verseNumber = 1, 
            text = "In the beginning the Word already existed; the Word was with God, and the Word was God."))
        list.add(BibleVerse(translation = "GNT", bookName = "John", chapter = 1, verseNumber = 2, 
            text = "From the very beginning the Word was with God."))
        list.add(BibleVerse(translation = "GNT", bookName = "John", chapter = 1, verseNumber = 3, 
            text = "Through him God made all things; not one thing in all creation was made without him."))
        list.add(BibleVerse(translation = "GNT", bookName = "John", chapter = 1, verseNumber = 4, 
            text = "The Word was the source of life, and this life brought light to humanity."))
        list.add(BibleVerse(translation = "GNT", bookName = "John", chapter = 1, verseNumber = 5, 
            text = "The light shines in the darkness, and the darkness has never put it out."))
        list.add(BibleVerse(translation = "GNT", bookName = "John", chapter = 3, verseNumber = 16, 
            text = "For God loved the world so much that he gave his only Son, so that everyone who believes in him may not die but have eternal life."))

        // Romans 8:28, 38-39
        list.add(BibleVerse(translation = "GNT", bookName = "Romans", chapter = 8, verseNumber = 28, 
            text = "We know that in all things God works for good with those who love him, those whom he has called according to his purpose."))
        list.add(BibleVerse(translation = "GNT", bookName = "Romans", chapter = 8, verseNumber = 38, 
            text = "For I am certain that nothing can separate us from his love: neither death nor life, neither angels nor other heavenly rulers or powers, neither the present nor the future,"))
        list.add(BibleVerse(translation = "GNT", bookName = "Romans", chapter = 8, verseNumber = 39, 
            text = "neither the world above nor the world below—there is nothing in all creation that will ever be able to separate us from the love of God which is ours through Christ Jesus our Lord."))

        // ------------------ WORLD ENGLISH BIBLE (WEB) ------------------
        
        // Genesis 1:1-5
        list.add(BibleVerse(translation = "WEB", bookName = "Genesis", chapter = 1, verseNumber = 1, 
            text = "In the beginning, God created the heavens and the earth."))
        list.add(BibleVerse(translation = "WEB", bookName = "Genesis", chapter = 1, verseNumber = 2, 
            text = "The earth was formless and empty. Darkness was on the surface of the deep and God's Spirit was hovering over the surface of the waters."))
        list.add(BibleVerse(translation = "WEB", bookName = "Genesis", chapter = 1, verseNumber = 3, 
            text = "God said, \"Let there be light,\" and there was light."))
        list.add(BibleVerse(translation = "WEB", bookName = "Genesis", chapter = 1, verseNumber = 4, 
            text = "God saw the light, and saw that it was good. God divided the light from the darkness."))
        list.add(BibleVerse(translation = "WEB", bookName = "Genesis", chapter = 1, verseNumber = 5, 
            text = "God called the light \"day,\" and the darkness he called \"night.\" There was evening and there was morning, one day."))

        // Genesis 1:26-27
        list.add(BibleVerse(translation = "WEB", bookName = "Genesis", chapter = 1, verseNumber = 26, 
            text = "God said, \"Let us make man in our image, after our likeness: and let them have dominion over the fish of the sea, and over the birds of the sky, and over the livestock, and over all the earth, and over every creeping thing that creeps on the earth.\""))
        list.add(BibleVerse(translation = "WEB", bookName = "Genesis", chapter = 1, verseNumber = 27, 
            text = "God created man in his own image. In God's image he created him; male and female he created them."))

        // Psalm 23:1-6
        list.add(BibleVerse(translation = "WEB", bookName = "Psalms", chapter = 23, verseNumber = 1, 
            text = "Yahweh is my shepherd: I shall lack nothing."))
        list.add(BibleVerse(translation = "WEB", bookName = "Psalms", chapter = 23, verseNumber = 2, 
            text = "He makes me lie down in green pastures. He leads me beside still waters."))
        list.add(BibleVerse(translation = "WEB", bookName = "Psalms", chapter = 23, verseNumber = 3, 
            text = "He restores my soul. He guides me in the paths of righteousness for his name's sake."))
        list.add(BibleVerse(translation = "WEB", bookName = "Psalms", chapter = 23, verseNumber = 4, 
            text = "Even though I walk through the valley of the shadow of death, I will fear no evil, for you are with me. Your rod and your staff, they comfort me."))
        list.add(BibleVerse(translation = "WEB", bookName = "Psalms", chapter = 23, verseNumber = 5, 
            text = "You prepare a table before me in the presence of my enemies. You anoint my head with oil. My cup runs over."))
        list.add(BibleVerse(translation = "WEB", bookName = "Psalms", chapter = 23, verseNumber = 6, 
            text = "Surely goodness and loving kindness shall follow me all the days of my life, and I will dwell in Yahweh's house forever."))

        // Proverbs 3:5-6
        list.add(BibleVerse(translation = "WEB", bookName = "Proverbs", chapter = 3, verseNumber = 5, 
            text = "Trust in Yahweh with all your heart, and do not lean on your own understanding."))
        list.add(BibleVerse(translation = "WEB", bookName = "Proverbs", chapter = 3, verseNumber = 6, 
            text = "In all your ways acknowledge him, and he will make your paths straight."))

        // Isaiah 9:6
        list.add(BibleVerse(translation = "WEB", bookName = "Isaiah", chapter = 9, verseNumber = 6, 
            text = "For to us a child is born, to us a son is given; and the government will be on his shoulders. His name will be called Wonderful Counselor, Mighty God, Everlasting Father, Prince of Peace."))

        // Matthew 5:3-9
        list.add(BibleVerse(translation = "WEB", bookName = "Matthew", chapter = 5, verseNumber = 3, 
            text = "Blessed are the poor in spirit, for theirs is the Kingdom of Heaven."))
        list.add(BibleVerse(translation = "WEB", bookName = "Matthew", chapter = 5, verseNumber = 4, 
            text = "Blessed are those who mourn, for they shall be comforted."))
        list.add(BibleVerse(translation = "WEB", bookName = "Matthew", chapter = 5, verseNumber = 5, 
            text = "Blessed are the gentle, for they shall inherit the earth."))
        list.add(BibleVerse(translation = "WEB", bookName = "Matthew", chapter = 5, verseNumber = 6, 
            text = "Blessed are those who hunger and thirst after righteousness, for they shall be filled."))
        list.add(BibleVerse(translation = "WEB", bookName = "Matthew", chapter = 5, verseNumber = 7, 
            text = "Blessed are the merciful, for they shall obtain mercy."))
        list.add(BibleVerse(translation = "WEB", bookName = "Matthew", chapter = 5, verseNumber = 8, 
            text = "Blessed are the pure in heart, for they shall see God."))
        list.add(BibleVerse(translation = "WEB", bookName = "Matthew", chapter = 5, verseNumber = 9, 
            text = "Blessed are the peacemakers, for they shall be called children of God."))

        // John 1:1-5
        list.add(BibleVerse(translation = "WEB", bookName = "John", chapter = 1, verseNumber = 1, 
            text = "In the beginning was the Word, and the Word was with God, and the Word was God."))
        list.add(BibleVerse(translation = "WEB", bookName = "John", chapter = 1, verseNumber = 2, 
            text = "The same was in the beginning with God."))
        list.add(BibleVerse(translation = "WEB", bookName = "John", chapter = 1, verseNumber = 3, 
            text = "All things were made through him. Without him, was not anything made that has been made."))
        list.add(BibleVerse(translation = "WEB", bookName = "John", chapter = 1, verseNumber = 4, 
            text = "In him was life, and the life was the light of men."))
        list.add(BibleVerse(translation = "WEB", bookName = "John", chapter = 1, verseNumber = 5, 
            text = "The light shines in the darkness, and the darkness hasn't overcome it."))

        // John 3:16
        list.add(BibleVerse(translation = "WEB", bookName = "John", chapter = 3, verseNumber = 16, 
            text = "For God so loved the world, that he gave his one and only Son, that whoever believes in him should not perish, but have eternal life."))

        // Romans 8:28, 38-39
        list.add(BibleVerse(translation = "WEB", bookName = "Romans", chapter = 8, verseNumber = 28, 
            text = "We know that all things work together for good for those who love God, to those who are called according to his purpose."))
        list.add(BibleVerse(translation = "WEB", bookName = "Romans", chapter = 8, verseNumber = 38, 
            text = "For I am persuaded, that neither death, nor life, nor angels, nor principalities, nor things present, nor things to come, nor powers,"))
        list.add(BibleVerse(translation = "WEB", bookName = "Romans", chapter = 8, verseNumber = 39, 
            text = "Nor height, nor depth, nor any other created thing, will be able to separate us from the love of God, which is in Christ Jesus our Lord."))


        // ------------------ AMERICAN STANDARD VERSION (ASV) ------------------
        
        // Genesis 1:1-5
        list.add(BibleVerse(translation = "ASV", bookName = "Genesis", chapter = 1, verseNumber = 1, 
            text = "In the beginning God created the heavens and the earth."))
        list.add(BibleVerse(translation = "ASV", bookName = "Genesis", chapter = 1, verseNumber = 2, 
            text = "And the earth was waste and void; and darkness was upon the face of the deep: and the Spirit of God moved upon the face of the waters."))
        list.add(BibleVerse(translation = "ASV", bookName = "Genesis", chapter = 1, verseNumber = 3, 
            text = "And God said, Let there be light: and there was light."))
        list.add(BibleVerse(translation = "ASV", bookName = "Genesis", chapter = 1, verseNumber = 4, 
            text = "And God saw the light, that it was good: and God divided the light from the darkness."))
        list.add(BibleVerse(translation = "ASV", bookName = "Genesis", chapter = 1, verseNumber = 5, 
            text = "And God called the light Day, and the darkness he called Night. And there was evening and there was morning, one day."))

        // Genesis 1:26-27
        list.add(BibleVerse(translation = "ASV", bookName = "Genesis", chapter = 1, verseNumber = 26, 
            text = "And God said, Let us make man in our image, after our likeness: and let them have dominion over the fish of the sea, and over the birds of the heavens, and over the cattle, and over all the earth, and over every creeping thing that creepeth upon the earth."))
        list.add(BibleVerse(translation = "ASV", bookName = "Genesis", chapter = 1, verseNumber = 27, 
            text = "And God created man in his own image, in the image of God created he him; male and female created he them."))

        // Psalm 23:1-6
        list.add(BibleVerse(translation = "ASV", bookName = "Psalms", chapter = 23, verseNumber = 1, 
            text = "Jehovah is my shepherd; I shall not want."))
        list.add(BibleVerse(translation = "ASV", bookName = "Psalms", chapter = 23, verseNumber = 2, 
            text = "He maketh me to lie down in green pastures; He leadeth me beside still waters."))
        list.add(BibleVerse(translation = "ASV", bookName = "Psalms", chapter = 23, verseNumber = 3, 
            text = "He restoreth my soul: He guideth me in the paths of righteousness for his name's sake."))
        list.add(BibleVerse(translation = "ASV", bookName = "Psalms", chapter = 23, verseNumber = 4, 
            text = "Yea, though I walk through the valley of the shadow of death, I will fear no evil; for thou art with me; Thy rod and thy staff, they comfort me."))
        list.add(BibleVerse(translation = "ASV", bookName = "Psalms", chapter = 23, verseNumber = 5, 
            text = "Thou preparest a table before me in the presence of mine enemies: Thou hast anointed my head with oil; My cup runneth over."))
        list.add(BibleVerse(translation = "ASV", bookName = "Psalms", chapter = 23, verseNumber = 6, 
            text = "Surely goodness and lovingkindness shall follow me all the days of my life; And I shall dwell in the house of Jehovah for ever."))

        // Proverbs 3:5-6
        list.add(BibleVerse(translation = "ASV", bookName = "Proverbs", chapter = 3, verseNumber = 5, 
            text = "Trust in Jehovah with all thy heart, And lean not upon thine own understanding."))
        list.add(BibleVerse(translation = "ASV", bookName = "Proverbs", chapter = 3, verseNumber = 6, 
            text = "In all thy ways acknowledge him, And he will direct thy paths."))

        // Isaiah 9:6
        list.add(BibleVerse(translation = "ASV", bookName = "Isaiah", chapter = 9, verseNumber = 6, 
            text = "For unto us a child is born, unto us a son is given; and the government shall be upon his shoulder: and his name shall be called Wonderful Counsellor, Mighty God, Everlasting Father, Prince of Peace."))

        // Matthew 5:3-9
        list.add(BibleVerse(translation = "ASV", bookName = "Matthew", chapter = 5, verseNumber = 3, 
            text = "Blessed are the poor in spirit: for theirs is the kingdom of heaven."))
        list.add(BibleVerse(translation = "ASV", bookName = "Matthew", chapter = 5, verseNumber = 4, 
            text = "Blessed are they that mourn: for they shall be comforted."))
        list.add(BibleVerse(translation = "ASV", bookName = "Matthew", chapter = 5, verseNumber = 5, 
            text = "Blessed are the meek: for they shall inherit the earth."))
        list.add(BibleVerse(translation = "ASV", bookName = "Matthew", chapter = 5, verseNumber = 6, 
            text = "Blessed are they that hunger and thirst after righteousness: for they shall be filled."))
        list.add(BibleVerse(translation = "ASV", bookName = "Matthew", chapter = 5, verseNumber = 7, 
            text = "Blessed are the merciful: for they shall obtain mercy."))
        list.add(BibleVerse(translation = "ASV", bookName = "Matthew", chapter = 5, verseNumber = 8, 
            text = "Blessed are the pure in heart: for they shall see God."))
        list.add(BibleVerse(translation = "ASV", bookName = "Matthew", chapter = 5, verseNumber = 9, 
            text = "Blessed are the peacemakers: for they shall be called sons of God."))

        // John 1:1-5
        list.add(BibleVerse(translation = "ASV", bookName = "John", chapter = 1, verseNumber = 1, 
            text = "In the beginning was the Word, and the Word was with God, and the Word was God."))
        list.add(BibleVerse(translation = "ASV", bookName = "John", chapter = 1, verseNumber = 2, 
            text = "The same was in the beginning with God."))
        list.add(BibleVerse(translation = "ASV", bookName = "John", chapter = 1, verseNumber = 3, 
            text = "All things were made through him; and without him was not anything made that hath been made."))
        list.add(BibleVerse(translation = "ASV", bookName = "John", chapter = 1, verseNumber = 4, 
            text = "In him was life; and the life was the light of men."))
        list.add(BibleVerse(translation = "ASV", bookName = "John", chapter = 1, verseNumber = 5, 
            text = "And the light shineth in the darkness; and the darkness apprehended it not."))

        // John 3:16
        list.add(BibleVerse(translation = "ASV", bookName = "John", chapter = 3, verseNumber = 16, 
            text = "For God so loved the world, that he gave his only begotten Son, that whosoever believeth on him should not perish, but have eternal life."))

        // Romans 8:28, 38-39
        list.add(BibleVerse(translation = "ASV", bookName = "Romans", chapter = 8, verseNumber = 28, 
            text = "And we know that to them that love God all things work together for good, even to them that are called according to his purpose."))
        list.add(BibleVerse(translation = "ASV", bookName = "Romans", chapter = 8, verseNumber = 38, 
            text = "For I am persuaded, that neither death, nor life, nor angels, nor principalities, nor things present, nor things to come, nor powers,"))
        list.add(BibleVerse(translation = "ASV", bookName = "Romans", chapter = 8, verseNumber = 39, 
            text = "Nor height, nor depth, nor any other creature, shall be able to separate us from the love of God, which is in Christ Jesus our Lord."))

        // ------------------ GOOD NEWS BIBLE (GNB) ------------------
        // Genesis 1:1-5, 26-27
        list.add(BibleVerse(translation = "GNB", bookName = "Genesis", chapter = 1, verseNumber = 1, 
            text = "In the beginning, when God created the universe,"))
        list.add(BibleVerse(translation = "GNB", bookName = "Genesis", chapter = 1, verseNumber = 2, 
            text = "the earth was formless and desolate. The raging ocean that covered everything was engulfed in total darkness, and the Spirit of God was moving over the water."))
        list.add(BibleVerse(translation = "GNB", bookName = "Genesis", chapter = 1, verseNumber = 3, 
            text = "Then God commanded, 'Let there be light'—and light appeared."))
        list.add(BibleVerse(translation = "GNB", bookName = "Genesis", chapter = 1, verseNumber = 4, 
            text = "God was pleased with what he saw. Then he separated the light from the darkness,"))
        list.add(BibleVerse(translation = "GNB", bookName = "Genesis", chapter = 1, verseNumber = 5, 
            text = "and he named the light 'Day' and the darkness 'Night.' Evening passed and morning came—that was the first day."))
        list.add(BibleVerse(translation = "GNB", bookName = "Genesis", chapter = 1, verseNumber = 26, 
            text = "Then God said, 'And now we will make human beings; they will be like us and resemble us. They will have power over the fish, the birds, and all animals, domestic and wild, large and small.'"))
        list.add(BibleVerse(translation = "GNB", bookName = "Genesis", chapter = 1, verseNumber = 27, 
            text = "So God created human beings, making them to be like himself. He created them male and female,"))

        // Psalms 23:1-6
        list.add(BibleVerse(translation = "GNB", bookName = "Psalms", chapter = 23, verseNumber = 1, 
            text = "The Lord is my shepherd; I have everything I need."))
        list.add(BibleVerse(translation = "GNB", bookName = "Psalms", chapter = 23, verseNumber = 2, 
            text = "He lets me rest in fields of green grass and leads me to quiet pools of fresh water."))
        list.add(BibleVerse(translation = "GNB", bookName = "Psalms", chapter = 23, verseNumber = 3, 
            text = "He gives me new strength. He guides me in the right paths, as he has promised."))
        list.add(BibleVerse(translation = "GNB", bookName = "Psalms", chapter = 23, verseNumber = 4, 
            text = "Even if I go through the deepest darkness, I will not be afraid, Lord, for you are with me. Your shepherd's rod and staff protect me."))
        list.add(BibleVerse(translation = "GNB", bookName = "Psalms", chapter = 23, verseNumber = 5, 
            text = "You prepare a banquet for me, where all my enemies can see me; you welcome me as an honored guest and fill my cup to the brim."))
        list.add(BibleVerse(translation = "GNB", bookName = "Psalms", chapter = 23, verseNumber = 6, 
            text = "I know that your goodness and love will be with me all my life; and your house will be my home as long as I live."))

        // Proverbs 3:5-6
        list.add(BibleVerse(translation = "GNB", bookName = "Proverbs", chapter = 3, verseNumber = 5, 
            text = "Trust in the Lord with all your heart. Never rely on what you think you know."))
        list.add(BibleVerse(translation = "GNB", bookName = "Proverbs", chapter = 3, verseNumber = 6, 
            text = "Remember the Lord in everything you do, and he will show you the right way."))

        // Isaiah 9:6
        list.add(BibleVerse(translation = "GNB", bookName = "Isaiah", chapter = 9, verseNumber = 6, 
            text = "A child is born to us! A son is given to us! And he will be our ruler. He will be called, 'Wonderful Counselor,' 'Mighty God,' 'Eternal Father,' 'Prince of Peace.'"))

        // Matthew 5:3-9
        list.add(BibleVerse(translation = "GNB", bookName = "Matthew", chapter = 5, verseNumber = 3, 
            text = "Happy are those who know they are spiritually poor; the Kingdom of heaven belongs to them!"))
        list.add(BibleVerse(translation = "GNB", bookName = "Matthew", chapter = 5, verseNumber = 4, 
            text = "Happy are those who mourn; God will comfort them!"))
        list.add(BibleVerse(translation = "GNB", bookName = "Matthew", chapter = 5, verseNumber = 5, 
            text = "Happy are those who are humble; they will receive what God has promised!"))
        list.add(BibleVerse(translation = "GNB", bookName = "Matthew", chapter = 5, verseNumber = 6, 
            text = "Happy are those whose greatest desire is to do what God requires; God will satisfy them fully!"))
        list.add(BibleVerse(translation = "GNB", bookName = "Matthew", chapter = 5, verseNumber = 7, 
            text = "Happy are those who show mercy to others; God will show mercy to them!"))
        list.add(BibleVerse(translation = "GNB", bookName = "Matthew", chapter = 5, verseNumber = 8, 
            text = "Happy are the pure in heart; they will see God!"))
        list.add(BibleVerse(translation = "GNB", bookName = "Matthew", chapter = 5, verseNumber = 9, 
            text = "Happy are those who work for peace; God will call them his children!"))

        // John 1:1-5, 3:16
        list.add(BibleVerse(translation = "GNB", bookName = "John", chapter = 1, verseNumber = 1, 
            text = "In the beginning the Word already existed; he was with God, and he was God."))
        list.add(BibleVerse(translation = "GNB", bookName = "John", chapter = 1, verseNumber = 2, 
            text = "From the very beginning the Word was with God."))
        list.add(BibleVerse(translation = "GNB", bookName = "John", chapter = 1, verseNumber = 3, 
            text = "Through him God made all things; not one thing in all creation was made without him."))
        list.add(BibleVerse(translation = "GNB", bookName = "John", chapter = 1, verseNumber = 4, 
            text = "The Word was the source of life, and this life brought light to people."))
        list.add(BibleVerse(translation = "GNB", bookName = "John", chapter = 1, verseNumber = 5, 
            text = "The light shines in the darkness, and the darkness has never put it out."))
        list.add(BibleVerse(translation = "GNB", bookName = "John", chapter = 3, verseNumber = 16, 
            text = "For God loved the world so much that he gave his only Son, so that everyone who believes in him may not die but have eternal life."))

        // Romans 8:28, 38-39
        list.add(BibleVerse(translation = "GNB", bookName = "Romans", chapter = 8, verseNumber = 28, 
            text = "We know that in all things God works for good with those who love him, those whom he has called according to his purpose."))
        list.add(BibleVerse(translation = "GNB", bookName = "Romans", chapter = 8, verseNumber = 38, 
            text = "For I am certain that nothing can separate us from his love: neither death nor life, neither angels nor other heavenly rulers or powers, neither the present nor the future,"))
        list.add(BibleVerse(translation = "GNB", bookName = "Romans", chapter = 8, verseNumber = 39, 
            text = "neither the world above nor the world below—there is nothing in all creation that will ever be able to separate us from the love of God which is ours through Christ Jesus our Lord."))

        // ------------------ INTERNATIONAL CHILDREN'S BIBLE (ICB) ------------------
        // Genesis 1:1-5, 26-27
        list.add(BibleVerse(translation = "ICB", bookName = "Genesis", chapter = 1, verseNumber = 1, 
            text = "In the beginning God created the sky and the earth."))
        list.add(BibleVerse(translation = "ICB", bookName = "Genesis", chapter = 1, verseNumber = 2, 
            text = "The earth was empty and had no form. Darkness covered the ocean, and the Spirit of God was moving over the water."))
        list.add(BibleVerse(translation = "ICB", bookName = "Genesis", chapter = 1, verseNumber = 3, 
            text = "Then God said, 'Let there be light!' And there was light."))
        list.add(BibleVerse(translation = "ICB", bookName = "Genesis", chapter = 1, verseNumber = 4, 
            text = "God saw that the light was good. So he divided the light from the darkness."))
        list.add(BibleVerse(translation = "ICB", bookName = "Genesis", chapter = 1, verseNumber = 5, 
            text = "God named the light 'day' and the darkness 'night.' There was evening, and then there was morning. This was the first day."))
        list.add(BibleVerse(translation = "ICB", bookName = "Genesis", chapter = 1, verseNumber = 26, 
            text = "Then God said, 'Let us make human beings in our image and likeness. And let them rule over the fish in the sea and the birds in the sky. Let them rule over the tame animals, and over all the earth, and over all the small crawling animals on the earth.'"))
        list.add(BibleVerse(translation = "ICB", bookName = "Genesis", chapter = 1, verseNumber = 27, 
            text = "So God created human beings in his own image. He created them in the image of God. He created them male and female."))

        // Psalms 23:1-6
        list.add(BibleVerse(translation = "ICB", bookName = "Psalms", chapter = 23, verseNumber = 1, 
            text = "The Lord is my shepherd. I have everything I need."))
        list.add(BibleVerse(translation = "ICB", bookName = "Psalms", chapter = 23, verseNumber = 2, 
            text = "He lets me rest in fields of green grass. He leads me to quiet pools of fresh water."))
        list.add(BibleVerse(translation = "ICB", bookName = "Psalms", chapter = 23, verseNumber = 3, 
            text = "He gives me new strength. He guides me in the right paths for the honor of his name."))
        list.add(BibleVerse(translation = "ICB", bookName = "Psalms", chapter = 23, verseNumber = 4, 
            text = "Even if I walk through a very dark valley, I will not be afraid. Lord, you are with me. Your shepherd's rod and staff comfort me."))
        list.add(BibleVerse(translation = "ICB", bookName = "Psalms", chapter = 23, verseNumber = 5, 
            text = "You prepare a meal for me in front of my enemies. You pour oil of blessing on my head. My cup runs over."))
        list.add(BibleVerse(translation = "ICB", bookName = "Psalms", chapter = 23, verseNumber = 6, 
            text = "I know that your goodness and love will be with me all my life. And I will live in the house of the Lord forever."))

        // Proverbs 3:5-6
        list.add(BibleVerse(translation = "ICB", bookName = "Proverbs", chapter = 3, verseNumber = 5, 
            text = "Trust the Lord with all your heart. Don't depend on your own understanding."))
        list.add(BibleVerse(translation = "ICB", bookName = "Proverbs", chapter = 3, verseNumber = 6, 
            text = "Remember the Lord in everything you do. And he will give you success."))

        // Isaiah 9:6
        list.add(BibleVerse(translation = "ICB", bookName = "Isaiah", chapter = 9, verseNumber = 6, 
            text = "A child will be born to us. God will give a son to us. He will be responsible for leading the people. His name will be Wonderful Counselor, Powerful God, Father Who Lives Forever, Prince of Peace."))

        // Matthew 5:3-9
        list.add(BibleVerse(translation = "ICB", bookName = "Matthew", chapter = 5, verseNumber = 3, 
            text = "Those who know they have great spiritual needs are happy. The kingdom of heaven belongs to them."))
        list.add(BibleVerse(translation = "ICB", bookName = "Matthew", chapter = 5, verseNumber = 4, 
            text = "Those who are sad now are happy. God will comfort them."))
        list.add(BibleVerse(translation = "ICB", bookName = "Matthew", chapter = 5, verseNumber = 5, 
            text = "Those who are humble are happy. The earth will belong to them."))
        list.add(BibleVerse(translation = "ICB", bookName = "Matthew", chapter = 5, verseNumber = 6, 
            text = "Those who want to do right more than anything else are happy. God will fully satisfy them."))
        list.add(BibleVerse(translation = "ICB", bookName = "Matthew", chapter = 5, verseNumber = 7, 
            text = "Those who give mercy to others are happy. Mercy will be given to them."))
        list.add(BibleVerse(translation = "ICB", bookName = "Matthew", chapter = 5, verseNumber = 8, 
            text = "Those who are pure in their thinking are happy. They will be with God."))
        list.add(BibleVerse(translation = "ICB", bookName = "Matthew", chapter = 5, verseNumber = 9, 
            text = "Those who work to bring peace are happy. God will call them his sons."))

        // John 1:1-5, 3:16
        list.add(BibleVerse(translation = "ICB", bookName = "John", chapter = 1, verseNumber = 1, 
            text = "In the beginning there was the Word. The Word was with God, and the Word was God."))
        list.add(BibleVerse(translation = "ICB", bookName = "John", chapter = 1, verseNumber = 2, 
            text = "He was with God in the beginning."))
        list.add(BibleVerse(translation = "ICB", bookName = "John", chapter = 1, verseNumber = 3, 
            text = "All things were made through him. Nothing was made without him."))
        list.add(BibleVerse(translation = "ICB", bookName = "John", chapter = 1, verseNumber = 4, 
            text = "In him there was life. That life was light for the people of the world."))
        list.add(BibleVerse(translation = "ICB", bookName = "John", chapter = 1, verseNumber = 5, 
            text = "The Light shines in the darkness. And the darkness has not overcome the Light."))
        list.add(BibleVerse(translation = "ICB", bookName = "John", chapter = 3, verseNumber = 16, 
            text = "For God loved the world so much that he gave his only Son. God gave his Son so that whoever believes in him may not be lost, but have eternal life."))

        // Romans 8:28, 38-39
        list.add(BibleVerse(translation = "ICB", bookName = "Romans", chapter = 8, verseNumber = 28, 
            text = "We know that in everything God works for the good of those who love him. They are the people God called, because that was his plan."))
        list.add(BibleVerse(translation = "ICB", bookName = "Romans", chapter = 8, verseNumber = 38, 
            text = "Yes, I am sure that nothing can separate us from the love God has for us. Not death, not life, not angels, not ruling spirits, not things now, not things in the future, not those powers."))
        list.add(BibleVerse(translation = "ICB", bookName = "Romans", chapter = 8, verseNumber = 39, 
            text = "Not anything above us, not anything below us. There is nothing in all creation that can separate us from the love God has for us in Christ Jesus our Lord."))

        return list
    }

    fun getSeedBooks(): List<BibleBookEntity> {
        val rawBooks = listOf(
            // Old Testament
            Tuple7("Genesis", 1, "Old Testament", "Pentateuch", 50, "Gen"),
            Tuple7("Exodus", 2, "Old Testament", "Pentateuch", 40, "Exo"),
            Tuple7("Leviticus", 3, "Old Testament", "Pentateuch", 27, "Lev"),
            Tuple7("Numbers", 4, "Old Testament", "Pentateuch", 36, "Num"),
            Tuple7("Deuteronomy", 5, "Old Testament", "Pentateuch", 34, "Deu"),
            Tuple7("Joshua", 6, "Old Testament", "History", 24, "Jos"),
            Tuple7("Judges", 7, "Old Testament", "History", 21, "Jdg"),
            Tuple7("Ruth", 8, "Old Testament", "History", 4, "Rth"),
            Tuple7("1 Samuel", 9, "Old Testament", "History", 31, "1Sa"),
            Tuple7("2 Samuel", 10, "Old Testament", "History", 24, "2Sa"),
            Tuple7("1 Kings", 11, "Old Testament", "History", 22, "1Ki"),
            Tuple7("2 Kings", 12, "Old Testament", "History", 25, "2Ki"),
            Tuple7("1 Chronicles", 13, "Old Testament", "History", 29, "1Ch"),
            Tuple7("2 Chronicles", 14, "Old Testament", "History", 36, "2Ch"),
            Tuple7("Ezra", 15, "Old Testament", "History", 10, "Ezr"),
            Tuple7("Nehemiah", 16, "Old Testament", "History", 13, "Neh"),
            Tuple7("Esther", 17, "Old Testament", "History", 10, "Est"),
            Tuple7("Job", 18, "Old Testament", "Poetry", 42, "Job"),
            Tuple7("Psalms", 19, "Old Testament", "Poetry", 150, "Psa"),
            Tuple7("Proverbs", 20, "Old Testament", "Poetry", 31, "Pro"),
            Tuple7("Ecclesiastes", 21, "Old Testament", "Poetry", 12, "Ecc"),
            Tuple7("Song of Solomon", 22, "Old Testament", "Poetry", 8, "Sng"),
            Tuple7("Isaiah", 23, "Old Testament", "Prophecy", 66, "Isa"),
            Tuple7("Jeremiah", 24, "Old Testament", "Prophecy", 52, "Jer"),
            Tuple7("Lamentations", 25, "Old Testament", "Prophecy", 5, "Lam"),
            Tuple7("Ezekiel", 26, "Old Testament", "Prophecy", 48, "Ezk"),
            Tuple7("Daniel", 27, "Old Testament", "Prophecy", 12, "Dan"),
            Tuple7("Hosea", 28, "Old Testament", "Prophecy", 14, "Hos"),
            Tuple7("Joel", 29, "Old Testament", "Prophecy", 3, "Jol"),
            Tuple7("Amos", 30, "Old Testament", "Prophecy", 9, "Amo"),
            Tuple7("Obadiah", 31, "Old Testament", "Prophecy", 1, "Oba"),
            Tuple7("Jonah", 32, "Old Testament", "Prophecy", 4, "Jon"),
            Tuple7("Micah", 33, "Old Testament", "Prophecy", 7, "Mic"),
            Tuple7("Nahum", 34, "Old Testament", "Prophecy", 3, "Nam"),
            Tuple7("Habakkuk", 35, "Old Testament", "Prophecy", 3, "Hab"),
            Tuple7("Zephaniah", 36, "Old Testament", "Prophecy", 3, "Zep"),
            Tuple7("Haggai", 37, "Old Testament", "Prophecy", 2, "Hag"),
            Tuple7("Zechariah", 38, "Old Testament", "Prophecy", 14, "Zec"),
            Tuple7("Malachi", 39, "Old Testament", "Prophecy", 4, "Mal"),

            // New Testament
            Tuple7("Matthew", 40, "New Testament", "Gospels", 28, "Mat"),
            Tuple7("Mark", 41, "New Testament", "Gospels", 16, "Mrk"),
            Tuple7("Luke", 42, "New Testament", "Gospels", 24, "Luk"),
            Tuple7("John", 43, "New Testament", "Gospels", 21, "Jhn"),
            Tuple7("Acts", 44, "New Testament", "History", 28, "Act"),
            Tuple7("Romans", 45, "New Testament", "Epistles", 16, "Rom"),
            Tuple7("1 Corinthians", 46, "New Testament", "Epistles", 16, "1Co"),
            Tuple7("2 Corinthians", 47, "New Testament", "Epistles", 13, "2Co"),
            Tuple7("Galatians", 48, "New Testament", "Epistles", 6, "Gal"),
            Tuple7("Ephesians", 49, "New Testament", "Epistles", 6, "Eph"),
            Tuple7("Philippians", 50, "New Testament", "Epistles", 4, "Php"),
            Tuple7("Colossians", 51, "New Testament", "Epistles", 4, "Col"),
            Tuple7("1 Thessalonians", 52, "New Testament", "Epistles", 5, "1Th"),
            Tuple7("2 Thessalonians", 53, "New Testament", "Epistles", 3, "2Th"),
            Tuple7("1 Timothy", 54, "New Testament", "Epistles", 6, "1Ti"),
            Tuple7("2 Timothy", 55, "New Testament", "Epistles", 4, "2Ti"),
            Tuple7("Titus", 56, "New Testament", "Epistles", 3, "Tit"),
            Tuple7("Philemon", 57, "New Testament", "Epistles", 1, "Phm"),
            Tuple7("Hebrews", 58, "New Testament", "Epistles", 13, "Heb"),
            Tuple7("James", 59, "New Testament", "Epistles", 5, "Jas"),
            Tuple7("1 Peter", 60, "New Testament", "Epistles", 5, "1Pe"),
            Tuple7("2 Peter", 61, "New Testament", "Epistles", 3, "2Pe"),
            Tuple7("1 John", 62, "New Testament", "Epistles", 5, "1Jo"),
            Tuple7("2 John", 63, "New Testament", "Epistles", 1, "2Jo"),
            Tuple7("3 John", 64, "New Testament", "Epistles", 1, "3Jo"),
            Tuple7("Jude", 65, "New Testament", "Epistles", 1, "Jud"),
            Tuple7("Revelation", 66, "New Testament", "Revelation", 22, "Rev")
        )

        return rawBooks.map { t ->
            BibleBookEntity(
                bookName = t.name,
                bookOrder = t.order,
                testament = t.testament,
                category = t.category,
                totalChapters = t.chapters,
                abbreviation = t.abbr,
                translation = "ALL"
            )
        }
    }

    fun getSeedChapters(): List<BibleChapterEntity> {
        val chapters = mutableListOf<BibleChapterEntity>()
        for (book in getSeedBooks()) {
            for (ch in 1..book.totalChapters) {
                chapters.add(
                    BibleChapterEntity(
                        bookName = book.bookName,
                        chapterNumber = ch,
                        totalVerses = 30, // Default baseline verse count per chapter
                        translation = "ALL"
                    )
                )
            }
        }
        return chapters
    }
}

private data class Tuple7(
    val name: String,
    val order: Int,
    val testament: String,
    val category: String,
    val chapters: Int,
    val abbr: String
)
