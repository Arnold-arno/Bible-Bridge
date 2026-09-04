package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class FAQItem(
    val category: String,
    val question: String,
    val answer: String,
    val verses: List<KeyVerse>
)

data class KeyVerse(
    val book: String,
    val chapter: Int,
    val verse: Int,
    val reference: String,
    val text: String,
    val contextNotes: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqScreen(
    viewModel: BibleViewModel,
    onNavigateToRead: (String, Int, Int) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val faqItems = remember {
        listOf(
            FAQItem(
                category = "Core Beliefs",
                question = "Who is God and what is His nature?",
                answer = "In Christianity, God is the loving Creator of the universe. He is holy, eternal, all-powerful, all-knowing, and present everywhere. Christians believe in one God who exists in three distinct persons: the Father, the Son (Jesus Christ), and the Holy Spirit (the Trinity). His core nature is love, truth, and mercy.",
                verses = listOf(
                    KeyVerse(
                        book = "1 John",
                        chapter = 4,
                        verse = 8,
                        reference = "1 John 4:8",
                        text = "He that loveth not knoweth not God; for God is love.",
                        contextNotes = "This verse underscores that God's very essence is defined by unconditional love."
                    ),
                    KeyVerse(
                        book = "Isaiah",
                        chapter = 40,
                        verse = 28,
                        reference = "Isaiah 40:28",
                        text = "Hast thou not known? hast thou not heard, that the everlasting God, the LORD, the Creator of the ends of the earth, fainteth not, neither is weary? there is no searching of his understanding.",
                        contextNotes = "Affirms God's infinite power, wisdom, and eternal, unchanging nature."
                    )
                )
            ),
            FAQItem(
                category = "Core Beliefs",
                question = "What is Salvation and how is it received?",
                answer = "Salvation is the redemption of humanity from sin and separation from God, leading to eternal life and restoration of fellowship with Him. It is a free gift of God's grace, not earned by good works, and is received through personal faith in Jesus Christ, His death on the cross, and His resurrection.",
                verses = listOf(
                    KeyVerse(
                        book = "Ephesians",
                        chapter = 2,
                        verse = 8,
                        reference = "Ephesians 2:8-9",
                        text = "For by grace are ye saved through faith; and that not of yourselves: it is the gift of God: Not of works, lest any man should boast.",
                        contextNotes = "The foundational teaching that salvation is an unmerited gift of grace through faith."
                    ),
                    KeyVerse(
                        book = "Romans",
                        chapter = 10,
                        verse = 9,
                        reference = "Romans 10:9",
                        text = "That if thou shalt confess with thy mouth the Lord Jesus, and shalt believe in thine heart that God hath raised him from the dead, thou shalt be saved.",
                        contextNotes = "A clear, actionable path to receiving salvation through heart belief and verbal confession."
                    )
                )
            ),
            FAQItem(
                category = "Life's Challenges",
                question = "How do I overcome anxiety and find peace?",
                answer = "Anxiety is a common human experience, but the Bible encourages believers to bring their worries, fears, and needs to God in prayer. True peace does not come from perfect circumstances, but from trusting in God's sovereignty and constant presence.",
                verses = listOf(
                    KeyVerse(
                        book = "Philippians",
                        chapter = 4,
                        verse = 6,
                        reference = "Philippians 4:6-7",
                        text = "Be careful for nothing; but in every thing by prayer and supplication with thanksgiving let your requests be made known unto God. And the peace of God, which passeth all understanding, shall keep your hearts and minds through Christ Jesus.",
                        contextNotes = "Instructs us to replace worry with prayerful thanksgiving, promising supernatural peace."
                    ),
                    KeyVerse(
                        book = "1 Peter",
                        chapter = 5,
                        verse = 7,
                        reference = "1 Peter 5:7",
                        text = "Casting all your care upon him; for he careth for you.",
                        contextNotes = "A warm invitation to release our burdens because God deeply loves and watches over us."
                    )
                )
            ),
            FAQItem(
                category = "Life's Challenges",
                question = "What does the Bible say about handling trials and suffering?",
                answer = "Suffering and trials are part of life in a broken world. However, God promises to use these challenges to build our endurance, character, and faith. He is always close to those who are hurting and will ultimately wipe away every tear.",
                verses = listOf(
                    KeyVerse(
                        book = "Romans",
                        chapter = 8,
                        verse = 28,
                        reference = "Romans 8:28",
                        text = "And we know that all things work together for good to them that love God, to them who are the called according to his purpose.",
                        contextNotes = "A powerful assurance that God works in all circumstances for the ultimate good of His children."
                    ),
                    KeyVerse(
                        book = "James",
                        chapter = 1,
                        verse = 2,
                        reference = "James 1:2-3",
                        text = "My brethren, count it all joy when ye fall into divers temptations; Knowing this, that the trying of your faith worketh patience.",
                        contextNotes = "Encourages a positive perspective on trials, knowing they develop strong faith and perseverance."
                    )
                )
            ),
            FAQItem(
                category = "Prayer & Faith",
                question = "How should I pray and is there a pattern?",
                answer = "Prayer is simply talking and listening to God as a loving Father. It is not about using fancy words or showing off. Jesus provided a model prayer (the Lord's Prayer) which guides us to praise God, seek His will, ask for daily needs, request forgiveness, and pray for protection.",
                verses = listOf(
                    KeyVerse(
                        book = "Matthew",
                        chapter = 6,
                        verse = 9,
                        reference = "Matthew 6:9-13",
                        text = "Our Father which art in heaven, Hallowed be thy name. Thy kingdom come. Thy will be done in earth, as it is in heaven...",
                        contextNotes = "The Lord's Prayer, providing a model of adoration, submission, request, confession, and praise."
                    ),
                    KeyVerse(
                        book = "Philippians",
                        chapter = 4,
                        verse = 6,
                        reference = "Philippians 4:6",
                        text = "In every thing by prayer and supplication with thanksgiving let your requests be made known unto God.",
                        contextNotes = "Emphasizes prayer as our primary, ongoing response to every life situation."
                    )
                )
            ),
            FAQItem(
                category = "Prayer & Faith",
                question = "Why is forgiveness important and how do I practice it?",
                answer = "Forgiveness is central to Christian faith because God has fully forgiven us through Christ. We are called to forgive others not because they deserve it, but because holding onto bitterness damages our souls and hinders our relationship with God. Forgiveness is a conscious choice to release resentment.",
                verses = listOf(
                    KeyVerse(
                        book = "Colossians",
                        chapter = 3,
                        verse = 13,
                        reference = "Colossians 3:13",
                        text = "Forbearing one another, and forgiving one another, if any man have a quarrel against any: even as Christ forgave you, so also do ye.",
                        contextNotes = "Instructs us to extend the same limitless forgiveness to others that Christ extended to us."
                    ),
                    KeyVerse(
                        book = "Matthew",
                        chapter = 6,
                        verse = 14,
                        reference = "Matthew 6:14",
                        text = "For if ye forgive men their trespasses, your heavenly Father will also forgive you.",
                        contextNotes = "Highlights the direct connection between our willingness to forgive and receiving forgiveness."
                    )
                )
            ),
            FAQItem(
                category = "Scripture",
                question = "How do I grow my faith daily?",
                answer = "Faith grows through consistent spiritual habits: reading and meditating on God's Word, prayer, worshiping with a local community of believers, and actively obeying God in your daily choices. Faith is like a muscle that strengthens when put into action.",
                verses = listOf(
                    KeyVerse(
                        book = "Romans",
                        chapter = 10,
                        verse = 17,
                        reference = "Romans 10:17",
                        text = "So then faith cometh by hearing, and hearing by the word of God.",
                        contextNotes = "Shows that regularly filling our minds with scripture directly feeds and builds our faith."
                    ),
                    KeyVerse(
                        book = "Hebrews",
                        chapter = 11,
                        verse = 1,
                        reference = "Hebrews 11:1",
                        text = "Now faith is the substance of things hoped for, the evidence of things not seen.",
                        contextNotes = "The classic biblical definition of faith as active, confident trust in God's promises."
                    )
                )
            ),
            FAQItem(
                category = "Scripture",
                question = "What is the Bible and how was it written?",
                answer = "The Bible is the inspired Word of God, consisting of 66 books written by over 40 human authors over approximately 1,500 years. It is divided into the Old Testament (39 books covering creation, God's covenant with Israel, and prophecies) and the New Testament (27 books covering the life of Jesus, early church, and foundational teachings). It is considered fully authoritative and guide for faith and life.",
                verses = listOf(
                    KeyVerse(
                        book = "2 Timothy",
                        chapter = 3,
                        verse = 16,
                        reference = "2 Timothy 3:16",
                        text = "All scripture is given by inspiration of God, and is profitable for doctrine, for reproof, for correction, for instruction in righteousness.",
                        contextNotes = "Affirms that all scripture is divinely inspired and highly useful for personal growth."
                    ),
                    KeyVerse(
                        book = "Psalms",
                        chapter = 119,
                        verse = 105,
                        reference = "Psalms 119:105",
                        text = "Thy word is a lamp unto my feet, and a light unto my path.",
                        contextNotes = "Beautiful imagery portraying God's Word as a guide through life's darkness."
                    )
                )
            ),
            FAQItem(
                category = "Core Beliefs",
                question = "Who is Jesus Christ?",
                answer = "Jesus Christ is the Savior and central figure of Christianity. Christians believe He is the Son of God, the promised Messiah of the Old Testament, and God incarnate in human form, who came to redeem humanity and offer eternal life.",
                verses = listOf(
                    KeyVerse(
                        book = "John",
                        chapter = 1,
                        verse = 1,
                        reference = "John 1:1",
                        text = "In the beginning was the Word, and the Word was with God, and the Word was God.",
                        contextNotes = "Establishes Jesus' eternal divinity as the co-creator and living Word."
                    ),
                    KeyVerse(
                        book = "John",
                        chapter = 14,
                        verse = 6,
                        reference = "John 14:6",
                        text = "Jesus saith unto him, I am the way, the truth, and the life: no man cometh unto the Father, but by me.",
                        contextNotes = "Declares Jesus as the exclusive and secure path to knowing God the Father."
                    )
                )
            ),
            FAQItem(
                category = "Life's Challenges",
                question = "How do I handle anger biblically?",
                answer = "The Bible teaches us that anger itself is an emotion that must be handled with care, warning us to control our temper, avoid letting it turn into sin, seek reconciliation quickly, and entrust justice and vengeance to God.",
                verses = listOf(
                    KeyVerse(
                        book = "Romans",
                        chapter = 12,
                        verse = 19,
                        reference = "Romans 12:19",
                        text = "Dearly beloved, avenge not yourselves, but rather give place unto wrath: for it is written, Vengeance is mine; I will repay, saith the Lord.",
                        contextNotes = "Encourages us to release our anger and trust God's perfect justice."
                    )
                )
            ),
            FAQItem(
                category = "Bible Fun Facts",
                question = "What is the longest chapter in the Bible?",
                answer = "Psalms 119 is the longest chapter in the Bible, containing 176 verses. It is designed as an alphabetic acrostic poem divided into 22 sections, with each section beginning with a letter of the Hebrew alphabet, focusing entirely on the value and guidance of God's Word.",
                verses = listOf(
                    KeyVerse(
                        book = "Psalms",
                        chapter = 119,
                        verse = 105,
                        reference = "Psalms 119:105",
                        text = "Thy word is a lamp unto my feet, and a light unto my path.",
                        contextNotes = "This beautiful verse highlights how scripture serves as our daily source of direction and wisdom."
                    )
                )
            ),
            FAQItem(
                category = "Bible Fun Facts",
                question = "What is the shortest chapter in the Bible?",
                answer = "Psalms 117 is both the shortest chapter in the Bible and the exact physical center of the entire Bible (containing just 2 verses). It is a direct and universal call for all nations to praise and worship the Lord.",
                verses = listOf(
                    KeyVerse(
                        book = "Psalms",
                        chapter = 117,
                        verse = 1,
                        reference = "Psalms 117:1",
                        text = "O praise the LORD, all ye nations: praise him, all ye people.",
                        contextNotes = "A glorious invitation to all people groups to worship Yahweh."
                    )
                )
            ),
            FAQItem(
                category = "Bible Fun Facts",
                question = "What is the shortest verse in the Bible?",
                answer = "John 11:35 is the shortest verse in the Bible, consisting of just two words in English: 'Jesus wept.' This simple yet profound statement reveals the deep empathy, compassion, and shared humanity of Jesus Christ as He grieved with Mary and Martha.",
                verses = listOf(
                    KeyVerse(
                        book = "John",
                        chapter = 11,
                        verse = 35,
                        reference = "John 11:35",
                        text = "Jesus wept.",
                        contextNotes = "Shows the heart of Savior who shares in our suffering and tears."
                    )
                )
            ),
            FAQItem(
                category = "Bible Fun Facts",
                question = "What is the middle verse of the entire Bible?",
                answer = "Psalms 118:8 is traditionally considered the exact middle verse of the King James Bible (with 594 chapters preceding and succeeding it). It presents a powerful and timeless spiritual instruction: 'It is better to trust in the LORD than to put confidence in man.'",
                verses = listOf(
                    KeyVerse(
                        book = "Psalms",
                        chapter = 118,
                        verse = 8,
                        reference = "Psalms 118:8",
                        text = "It is better to trust in the LORD than to put confidence in man.",
                        contextNotes = "Reminds us to anchor our security and faith in God's reliability rather than human promises."
                    )
                )
            ),
            FAQItem(
                category = "Bible Fun Facts",
                question = "What is the most repeated word in the Bible?",
                answer = "The most repeated word in the Bible is the word 'the'. However, among nouns and names, the covenant name of God 'LORD' (Yahweh) is the most frequent, appearing over 7,000 times in the Old Testament, emphasizing God's personal and relational nature with His people.",
                verses = listOf(
                    KeyVerse(
                        book = "Psalms",
                        chapter = 23,
                        verse = 1,
                        reference = "Psalms 23:1",
                        text = "The LORD is my shepherd; I shall not want.",
                        contextNotes = "Uses God's personal covenant name (LORD) to assure us of His sovereign protection."
                    )
                )
            ),
            FAQItem(
                category = "Bible Fun Facts",
                question = "What is the most popular and common verse in the Bible?",
                answer = "John 3:16 is widely recognized as the most popular, famous, and translated verse in the entire Bible. It beautifully and concisely summarizes the core message of the Christian gospel: God's ultimate love for the world, the sacrificial gift of His only Son, and the promise of eternal life through simple faith.",
                verses = listOf(
                    KeyVerse(
                        book = "John",
                        chapter = 3,
                        verse = 16,
                        reference = "John 3:16",
                        text = "For God so loved the world, that he gave his only begotten Son, that whosoever believeth in him should not perish, but have everlasting life.",
                        contextNotes = "The beautiful golden verse of the scriptures."
                    )
                )
            )
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var expandedQuestion by remember { mutableStateOf<String?>(null) }

    val categories = remember { listOf("All", "Core Beliefs", "Life's Challenges", "Prayer & Faith", "Scripture", "Bible Fun Facts") }

    val filteredFaqItems = remember(searchQuery, selectedCategory, faqItems) {
        faqItems.filter { item ->
            val matchesCategory = selectedCategory == "All" || item.category == selectedCategory
            val matchesSearch = searchQuery.trim().isEmpty() ||
                    item.question.contains(searchQuery, ignoreCase = true) ||
                    item.answer.contains(searchQuery, ignoreCase = true) ||
                    item.verses.any { it.reference.contains(searchQuery, ignoreCase = true) || it.text.contains(searchQuery, ignoreCase = true) }
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search questions, answers or verses...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("faq_search_field"),
            shape = RoundedCornerShape(12.dp)
        )

        // Category Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            categories.forEach { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    label = { Text(category, fontSize = 12.sp) },
                    modifier = Modifier.testTag("faq_chip_$category"),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        if (filteredFaqItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No questions found matching your search.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredFaqItems) { item ->
                    val isExpanded = expandedQuestion == item.question
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedQuestion = if (isExpanded) null else item.question
                            }
                            .testTag("faq_item_${item.question.hashCode()}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isExpanded) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        border = border(
                            isExpanded = isExpanded,
                            primaryColor = MaterialTheme.colorScheme.primary,
                            outlineColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "?",
                                            fontWeight = FontWeight.Bold,
                                            color = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 16.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = item.question,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                    
                                    // Category Tag
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.secondaryContainer)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = item.category.uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = item.answer,
                                        style = MaterialTheme.typography.bodyMedium,
                                        lineHeight = 22.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "Key Bible Passages",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    item.verses.forEach { keyVerse ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                            )
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = keyVerse.reference,
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        IconButton(
                                                            onClick = {
                                                                clipboardManager.setText(AnnotatedString("${keyVerse.reference}: ${keyVerse.text}"))
                                                                Toast.makeText(context, "Copied verse reference and text!", Toast.LENGTH_SHORT).show()
                                                            },
                                                            modifier = Modifier.size(32.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.ContentCopy,
                                                                contentDescription = "Copy Verse",
                                                                modifier = Modifier.size(16.dp),
                                                                tint = MaterialTheme.colorScheme.primary
                                                            )
                                                        }
                                                        IconButton(
                                                            onClick = {
                                                                onNavigateToRead(keyVerse.book, keyVerse.chapter, keyVerse.verse)
                                                                Toast.makeText(context, "Navigating to ${keyVerse.reference}...", Toast.LENGTH_SHORT).show()
                                                            },
                                                            modifier = Modifier.size(32.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.MenuBook,
                                                                contentDescription = "Read in Context",
                                                                modifier = Modifier.size(16.dp),
                                                                tint = MaterialTheme.colorScheme.primary
                                                            )
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "\"${keyVerse.text}\"",
                                                    fontStyle = FontStyle.Italic,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = keyVerse.contextNotes,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun border(
    isExpanded: Boolean,
    primaryColor: Color,
    outlineColor: Color
): androidx.compose.foundation.BorderStroke {
    return androidx.compose.foundation.BorderStroke(
        width = if (isExpanded) 1.5.dp else 1.dp,
        color = if (isExpanded) primaryColor else outlineColor.copy(alpha = 0.2f)
    )
}
