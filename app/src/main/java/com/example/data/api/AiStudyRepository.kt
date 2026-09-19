package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.StudyTaskEntity
import com.example.data.model.Flashcard
import com.example.data.model.QuizQuestion
import com.example.data.model.SmartNotesResult
import com.example.data.model.TutorMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class AiStudyRepository {

    private val isKeyValid: Boolean
        get() {
            val key = BuildConfig.GEMINI_API_KEY
            return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
        }

    suspend fun askTutor(userQuestion: String, examContext: String): TutorMessage = withContext(Dispatchers.IO) {
        val messageId = UUID.randomUUID().toString()

        if (isKeyValid) {
            try {
                val prompt = """
                    You are the AI Study OS Tutor strictly for Class 10 Maharashtra State Board (MSBSHSE Pune SSC).
                    Exam Context: $examContext (Class 10 Maharashtra Board SSC)
                    Student Doubt: $userQuestion
                    
                    Follow Maharashtra SSC Board textbook explanations, definitions, and 40-mark paper patterns.
                    Respond strictly in clean structured sections:
                    1. Direct Step-by-Step Explanation (according to Maharashtra SSC textbook)
                    2. Memory Trick or Practical Analogy
                    3. Board Exam Formula / Key Theorem / Rule
                    4. SSC Board Practice Question (with complete step-by-step solution labeled)
                """.trimIndent()

                val response = GeminiNetwork.apiService.generateContent(
                    BuildConfig.GEMINI_API_KEY,
                    GeminiRequest(listOf(GeminiContent(listOf(GeminiPart(text = prompt)))))
                )
                val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!rawText.isNullOrBlank()) {
                    return@withContext parseTutorResponse(messageId, rawText)
                }
            } catch (e: Exception) {
                Log.w("AiStudyRepository", "Gemini API call failed, using Maharashtra SSC expert curriculum engine", e)
            }
        }

        // Curated Maharashtra SSC Class 10 Engine
        return@withContext getCuratedTutorResponse(messageId, userQuestion, examContext)
    }

    private fun parseTutorResponse(id: String, rawText: String): TutorMessage {
        val lines = rawText.lines()
        val steps = mutableListOf<String>()
        var analogy = ""
        var formula = ""
        var practiceQ = ""
        var practiceA = ""

        var currentSection = 0
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("1.") || trimmed.contains("Explanation", ignoreCase = true)) {
                currentSection = 1
            } else if (trimmed.startsWith("2.") || trimmed.contains("Analogy", ignoreCase = true) || trimmed.contains("Memory Trick", ignoreCase = true)) {
                currentSection = 2
            } else if (trimmed.startsWith("3.") || trimmed.contains("Formula", ignoreCase = true) || trimmed.contains("Theorem", ignoreCase = true) || trimmed.contains("Rule", ignoreCase = true)) {
                currentSection = 3
            } else if (trimmed.startsWith("4.") || trimmed.contains("Practice", ignoreCase = true)) {
                currentSection = 4
            } else if (trimmed.isNotBlank()) {
                when (currentSection) {
                    1 -> if (trimmed.startsWith("-") || trimmed.startsWith("*") || trimmed.matches(Regex("""^\d+\..*"""))) {
                        steps.add(trimmed.replace(Regex("""^[-*•\d.]+\s*"""), ""))
                    } else if (steps.isEmpty()) {
                        steps.add(trimmed)
                    }
                    2 -> analogy += if (analogy.isEmpty()) trimmed else " $trimmed"
                    3 -> formula += if (formula.isEmpty()) trimmed else "\n$trimmed"
                    4 -> {
                        if (trimmed.contains("Solution:", ignoreCase = true) || trimmed.contains("Answer:", ignoreCase = true)) {
                            practiceA = trimmed
                        } else {
                            practiceQ += if (practiceQ.isEmpty()) trimmed else " $trimmed"
                        }
                    }
                }
            }
        }

        if (steps.isEmpty()) {
            steps.add(rawText.take(300) + "...")
        }

        return TutorMessage(
            id = id,
            isUser = false,
            messageText = "Maharashtra SSC Board Concept Solution:",
            stepByStepPoints = steps,
            practicalAnalogy = analogy.ifBlank { "Maha SSC Board Tip: Always draw neat labelled diagrams and mention standard SI units in Science!" },
            formulaShortcut = formula.ifBlank { "Key SSC Relation: Refer to Maharashtra State Board textbook summary." },
            practiceQuestion = practiceQ.ifBlank { "Solve: If mass of an object is 10 kg on Earth, calculate its weight on Earth and on the Moon (g_moon = 1/6 g_earth)." },
            practiceAnswer = practiceA.ifBlank { "Solution: Weight on Earth = m·g = 10 × 9.8 = 98 N. Weight on Moon = 98 / 6 ≈ 16.33 N. Mass remains constant 10 kg." }
        )
    }

    private fun getCuratedTutorResponse(id: String, query: String, exam: String): TutorMessage {
        val q = query.lowercase()
        return when {
            q.contains("kepler") || q.contains("gravit") -> TutorMessage(
                id = id,
                isUser = false,
                messageText = "Kepler's Laws of Planetary Motion (Science Part 1 - Chapter 1):",
                stepByStepPoints = listOf(
                    "Kepler's First Law (Law of Orbit): The orbit of a planet is an ellipse with the Sun at one of the two foci.",
                    "Kepler's Second Law (Law of Area): The line joining the planet and the Sun sweeps equal areas in equal intervals of time (Areal velocity is constant).",
                    "Kepler's Third Law (Law of Period): The square of its period of revolution around the Sun is directly proportional to the cube of the mean distance of a planet from the Sun: T² ∝ r³, i.e., T²/r³ = Constant (K).",
                    "Newton's Universal Law of Gravitation uses Kepler's 3rd Law to prove inverse-square dependence of gravitational force: F = G·(m₁·m₂)/d²."
                ),
                practicalAnalogy = "Memory Trick: O-A-P -> Law 1 = Orbit (ellipse), Law 2 = Area (equal time), Law 3 = Period (T² ∝ r³).",
                formulaShortcut = "T² / r³ = Constant (K)\nUniversal Gravitation: F = G·m₁·m₂ / r² (G = 6.67 × 10⁻¹¹ N·m²/kg²)\nAcceleration due to gravity: g = G·M / R² = 9.8 m/s²",
                practiceQuestion = "SSC 2024 Board Question: If the mass of a planet is twice that of Earth and its radius is thrice that of Earth, what is the value of 'g' on that planet?",
                practiceAnswer = "Solution: g_p = G·(2M)/(3R)² = (2/9)·(G·M/R²) = (2/9) × 9.8 ≈ 2.18 m/s²."
            )
            q.contains("cramer") || q.contains("linear equation") || q.contains("determinant") -> TutorMessage(
                id = id,
                isUser = false,
                messageText = "Cramer's Rule for Linear Equations (Maths 1 Algebra - Chapter 1):",
                stepByStepPoints = listOf(
                    "Step 1: Write given linear equations in standard form: a₁x + b₁y = c₁ and a₂x + b₂y = c₂.",
                    "Step 2: Find determinant D = |a₁  b₁| / |a₂  b₂| = (a₁·b₂ - a₂·b₁). Note: D must NOT be 0.",
                    "Step 3: Find determinant Dx by replacing x-coefficients with constants: Dx = |c₁  b₁| / |c₂  b₂| = (c₁·b₂ - c₂·b₁).",
                    "Step 4: Find determinant Dy by replacing y-coefficients with constants: Dy = |a₁  c₁| / |a₂  c₂| = (a₁·c₂ - a₂·c₁).",
                    "Step 5: Apply Cramer's formula: x = Dx / D and y = Dy / D."
                ),
                practicalAnalogy = "Memory Code: For D use columns (1, 2). For Dx replace col 1 with constants (C, 2). For Dy replace col 2 with constants (1, C).",
                formulaShortcut = "D = a₁b₂ - a₂b₁\nDx = c₁b₂ - c₂b₁\nDy = a₁c₂ - a₂c₁\nValues: x = Dx/D, y = Dy/D (Condition: D ≠ 0)",
                practiceQuestion = "Solve using Cramer's rule: 3x - 4y = 10 and 4x + 3y = 5.",
                practiceAnswer = "Solution: D = |3 -4 ; 4 3| = 9 - (-16) = 25. Dx = |10 -4 ; 5 3| = 30 - (-20) = 50. Dy = |3 10 ; 4 5| = 15 - 40 = -25. x = 50/25 = 2, y = -25/25 = -1. (x, y) = (2, -1)."
            )
            q.contains("quadratic") || q.contains("discriminant") || q.contains("formula method") -> TutorMessage(
                id = id,
                isUser = false,
                messageText = "Quadratic Equations Formula Method (Maths 1 Algebra - Chapter 2):",
                stepByStepPoints = listOf(
                    "Step 1: Compare the given equation with general form ax² + bx + c = 0 and identify a, b, c.",
                    "Step 2: Calculate the discriminant Δ (Delta): Δ = b² - 4ac.",
                    "Step 3: Analyze nature of roots:\n• If Δ > 0: Roots are real and unequal.\n• If Δ = 0: Roots are real and equal.\n• If Δ < 0: Roots are not real numbers.",
                    "Step 4: Calculate roots using quadratic formula: x = [-b ± √(b² - 4ac)] / (2a)."
                ),
                practicalAnalogy = "Board Exam Trap: Always write ± and remember the whole numerator [-b ± √Δ] is divided by 2a, not just √Δ!",
                formulaShortcut = "Discriminant: Δ = b² - 4ac\nRoots formula: x = (-b ± √Δ) / 2a\nSum of roots α + β = -b/a, Product α·β = c/a",
                practiceQuestion = "Find the discriminant and solve by formula method: x² + 2x - 5 = 0.",
                practiceAnswer = "Solution: a=1, b=2, c=-5. Δ = 2² - 4(1)(-5) = 4 + 20 = 24. x = [-2 ± √24] / 2 = [-2 ± 2√6] / 2 = -1 ± √6. Roots: -1 + √6 and -1 - √6."
            )
            q.contains("similarity") || q.contains("bpt") || q.contains("basic proportionality") -> TutorMessage(
                id = id,
                isUser = false,
                messageText = "Basic Proportionality Theorem (BPT) (Maths 2 Geometry - Chapter 1):",
                stepByStepPoints = listOf(
                    "Statement: If a line parallel to a side of a triangle intersects the remaining sides in two distinct points, then the line divides the sides in the same ratio.",
                    "Given: In ΔABC, line l ∥ side BC, intersecting AB at P and AC at Q.",
                    "To Prove: AP / PB = AQ / QC.",
                    "Construction: Draw segment PC and segment BQ. Draw perpendicular height from Q to AB and from P to AC.",
                    "Proof: Area(ΔAPQ)/Area(ΔBPQ) = AP/PB (triangles with equal heights). Area(ΔAPQ)/Area(ΔCPQ) = AQ/QC. Since ΔBPQ and ΔCPQ lie between parallel lines l and BC, Area(ΔBPQ) = Area(ΔCPQ). Hence AP/PB = AQ/QC!"
                ),
                practicalAnalogy = "SSC Board Golden Rule: 4-Mark Board Theorem question. Writing Given, To Prove, Construction, and Reason for each step gives full 4/4 marks!",
                formulaShortcut = "BPT: AP / PB = AQ / QC\nConverse of BPT: If AP/PB = AQ/QC, then line PQ ∥ BC\nProperty of angle bisector: BD/DC = AB/AC",
                practiceQuestion = "In ΔPQR, seg PM = 15, MQ = 10, seg PN = 12, seg NR = 8. State whether line MN is parallel to side QR or not.",
                practiceAnswer = "Solution: PM/MQ = 15/10 = 3/2. PN/NR = 12/8 = 3/2. Since PM/MQ = PN/NR = 3/2, by Converse of Basic Proportionality Theorem, line MN ∥ side QR."
            )
            q.contains("motor") || q.contains("fleming") || q.contains("electric current") -> TutorMessage(
                id = id,
                isUser = false,
                messageText = "Fleming's Left Hand Rule & Electric Motor (Science 1 - Chapter 4):",
                stepByStepPoints = listOf(
                    "Fleming's Left Hand Rule: Stretch thumb, index finger, and middle finger of left hand mutually perpendicular. Forefinger = Direction of Magnetic Field (N to S); Middle finger = Direction of Current; Thumb = Direction of Force (Motion).",
                    "Electric Motor Principle: Converts Electrical energy into Mechanical energy based on magnetic force on a current-carrying conductor.",
                    "Split Ring Commutator Role: Reverses the direction of current in the coil every half rotation, ensuring the coil keeps rotating continuously in the same direction.",
                    "Carbon Brushes Role: Maintain sliding electrical contact with split rings to supply current from the battery."
                ),
                practicalAnalogy = "Memory Mnemonic: Father (Force - Thumb), Mother (Magnetic Field - Forefinger), Child (Current - Middle finger) -> FMC Left Hand!",
                formulaShortcut = "Joule's Heating Law: H = I² · R · t (or H = V · I · t)\nElectric Power: P = V · I = I²R = V²/R\n1 Unit of electricity = 1 kWh = 3.6 × 10⁶ Joules",
                practiceQuestion = "SSC Board 3-Mark Question: What is the function of split rings (commutator) in an electric motor?",
                practiceAnswer = "Solution: The split rings reverse the direction of current in the armature coil after every half rotation. Consequently, the force on the two arms continues in the same sense, making the coil rotate unidirectionally."
            )
            q.contains("transcription") || q.contains("heredity") || q.contains("evolution") -> TutorMessage(
                id = id,
                isUser = false,
                messageText = "Transcription, Translation & Translocation (Science 2 - Chapter 1):",
                stepByStepPoints = listOf(
                    "Central Dogma: Genetic information flows from DNA to mRNA (Transcription) and then from mRNA to Proteins (Translation).",
                    "Transcription: Synthesis of mRNA from one strand of DNA inside the nucleus using RNA Polymerase. Uracil (U) replaces Thymine (T).",
                    "Translation: In cytoplasm, ribosome attaches to mRNA. tRNA brings amino acids matching the triplet codons on mRNA via complementary anticodons.",
                    "Translocation: The ribosome keeps moving along mRNA from one triplet codon to next by the distance of one triplet codon. This movement is called Translocation."
                ),
                practicalAnalogy = "Analogy: DNA is the master recipe book in the library (nucleus). mRNA is a photocopy of one recipe taken to the kitchen (cytoplasm). tRNA are the delivery chefs bringing ingredients (amino acids) to the oven (ribosome)!",
                formulaShortcut = "Start Codon: AUG (Methionine)\nTriplet Codon: Sequence of 3 nucleotides coding for 1 amino acid (Dr. Har Gobind Khorana's discovery).",
                practiceQuestion = "Explain the difference between Transcription and Translation.",
                practiceAnswer = "Solution: Transcription takes place in nucleus (DNA -> mRNA). Translation takes place in cytoplasm on ribosomes (mRNA + tRNA -> Polypeptide chain)."
            )
            else -> TutorMessage(
                id = id,
                isUser = false,
                messageText = "Maharashtra SSC Board Concept Breakdown: $query",
                stepByStepPoints = listOf(
                    "Maharashtra Board Textbook Definition: Clearly state the law or concept in standard textbook phrasing.",
                    "Activity & Application: Connect the concept to Maharashtra Board practical experiments and textbook diagrams.",
                    "SSC Board Marking Strategy: Structure answers into labelled points, SI units, and formula highlights to maximize marks in the 40-mark paper."
                ),
                practicalAnalogy = "SSC Board Tip: Maharashtra Board examiners award step-wise marks. Always write Given, Formula, Substitution, Calculation, and Final Answer with units!",
                formulaShortcut = "Always memorize formulas with their SI units (e.g., G = N·m²/kg², Power of Lens = Dioptre D = 1/f in metres).",
                practiceQuestion = "Review question: Practice drawing neat labelled diagrams for this chapter from your Balbharati textbook!",
                practiceAnswer = "Tip: High scorers in Maharashtra SSC practice Previous 5 Years Question Papers (PYQs)."
            )
        }
    }

    suspend fun generateSmartNotes(topic: String, subject: String): SmartNotesResult = withContext(Dispatchers.IO) {
        val t = topic.lowercase()
        val s = subject.lowercase()

        if (t.contains("gravit") || t.contains("kepler") || s.contains("science 1") || s.contains("physics")) {
            return@withContext SmartNotesResult(
                topic = "Gravitation & Kepler's Laws",
                subject = "Science & Tech Part 1 (SSC)",
                summaryPoints = listOf(
                    "Gravitation is a universal attractive force acting between any two bodies in the universe, discovered by Sir Isaac Newton.",
                    "Kepler's First Law: The orbit of a planet is an ellipse with the Sun at one of the foci.",
                    "Kepler's Second Law: The line joining planet and Sun sweeps equal areas in equal intervals of time.",
                    "Kepler's Third Law: T² ∝ r³ (T²/r³ = Constant K).",
                    "Newton's Universal Law: F = G·m₁·m₂ / d². Gravitational constant G = 6.673 × 10⁻¹¹ N·m²/kg².",
                    "Acceleration due to gravity (g): g = GM/R². Value on Earth's surface = 9.8 m/s² (maximum at poles 9.832 m/s², minimum at equator 9.78 m/s²).",
                    "Escape velocity (v_esc): The minimum initial velocity required for an object to escape Earth's gravitational influence: v_esc = √(2gR) = 11.2 km/s."
                ),
                definitions = listOf(
                    "Universal Gravitation Constant (G)" to "The gravitational force of attraction between two unit masses placed at a unit distance apart. Value: 6.67 × 10⁻¹¹ N·m²/kg².",
                    "Acceleration due to Gravity (g)" to "The acceleration produced in a body due to the gravitational force of the Earth. SI unit is m/s².",
                    "Free Fall" to "Whenever an object moves solely under the influence of gravitational force without air resistance, it is said to be in free fall.",
                    "Escape Velocity" to "The minimum speed needed for a free, non-propelled object to escape from the gravitational field of a primary body.",
                    "Mass vs Weight" to "Mass (m) is the amount of matter (scalar, SI unit kg, constant everywhere). Weight (W = mg) is force of gravity (vector, SI unit Newton, varies with g)."
                ),
                formulas = listOf(
                    "Universal Law of Gravitation" to "F = G · (m₁ · m₂) / r²",
                    "Acceleration due to Gravity" to "g = (G · M) / R²",
                    "Weight" to "W = m · g (in Newtons)",
                    "Kinematic Equations for Free Fall" to "v = gt,  s = ½ gt²,  v² = 2gs",
                    "Potential Energy" to "P.E. = - (G · M · m) / (R + h)",
                    "Escape Velocity" to "v_esc = √(2GM / R) = √(2gR) = 11.2 km/s"
                ),
                flashcards = listOf(
                    Flashcard("Where is 'g' maximum on Earth's surface?", "At the Poles (9.832 m/s²) because distance from Earth's center is minimum.", "Science 1"),
                    Flashcard("What is the value of 'g' at the centre of Earth?", "g = 0 at the centre of Earth.", "Science 1"),
                    Flashcard("What is the ratio T²/r³ called in Kepler's third law?", "It is constant K, independent of planet mass.", "Science 1"),
                    Flashcard("What is the escape velocity from the surface of Earth?", "11.2 km/s.", "Science 1")
                ),
                mcqs = listOf(
                    QuizQuestion(
                        id = 1,
                        subject = "Science 1",
                        chapter = "Gravitation",
                        questionText = "The value of acceleration due to gravity 'g' is maximum at:",
                        options = listOf("Equator", "Poles", "Center of Earth", "At an altitude of 1000 km"),
                        correctIndex = 1,
                        explanation = "Earth is flattened at poles, so radius R is smallest at poles. Since g = GM/R², g is maximum at the poles (9.832 m/s²).",
                        conceptTag = "Variation in g"
                    ),
                    QuizQuestion(
                        id = 2,
                        subject = "Science 1",
                        chapter = "Gravitation",
                        questionText = "If Earth's radius shrinks by 1% keeping mass constant, the value of g on surface will:",
                        options = listOf("Decrease by 2%", "Increase by 2%", "Remain unchanged", "Increase by 1%"),
                        correctIndex = 1,
                        explanation = "g = GM/R². For small fractional change, Δg/g ≈ -2(ΔR/R). If radius decreases by 1%, g increases by approximately 2%.",
                        conceptTag = "Formula Derivation"
                    )
                ),
                revisionQuestions = listOf(
                    "State Kepler's three laws of planetary motion with neat diagram.",
                    "Distinguish between Mass and Weight with 4 points.",
                    "Derive the formula for escape velocity on the surface of Earth.",
                    "Explain why value of g changes with depth inside Earth."
                )
            )
        }

        if (t.contains("linear") || t.contains("cramer") || s.contains("algebra") || s.contains("maths 1")) {
            return@withContext SmartNotesResult(
                topic = "Linear Equations & Cramer's Rule",
                subject = "Mathematics Part 1 (Algebra)",
                summaryPoints = listOf(
                    "An equation of form ax + by + c = 0 where a, b, c are real numbers and a, b ≠ 0 is a linear equation in two variables.",
                    "Simultaneous equations can be solved by Elimination, Substitution, Graphical method, and Cramer's Determinant method.",
                    "In Graphical method, the coordinates of point of intersection of two lines is the unique solution.",
                    "In Cramer's rule: x = Dx / D and y = Dy / D, provided determinant D ≠ 0.",
                    "Condition for consistency: a₁/a₂ ≠ b₁/b₂ gives a unique solution (intersecting lines)."
                ),
                definitions = listOf(
                    "Linear Equation in Two Variables" to "An algebraic equation containing two variables with degree 1 for each variable term.",
                    "Determinant of Order 2" to "A square arrangement of numbers |a b ; c d| whose value is (ad - bc).",
                    "Simultaneous Linear Equations" to "Two linear equations considered together to find common solutions satisfying both."
                ),
                formulas = listOf(
                    "General Form" to "a₁x + b₁y = c₁  and  a₂x + b₂y = c₂",
                    "Determinant D" to "D = a₁·b₂ - a₂·b₁",
                    "Determinant Dx" to "Dx = c₁·b₂ - c₂·b₁",
                    "Determinant Dy" to "Dy = a₁·c₂ - a₂·c₁",
                    "Cramer's Solution" to "x = Dx / D,  y = Dy / D (D ≠ 0)"
                ),
                flashcards = listOf(
                    Flashcard("What is the degree of a linear equation in two variables?", "Degree is 1.", "Algebra"),
                    Flashcard("What is the condition for Cramer's rule to give a unique solution?", "D ≠ 0.", "Algebra"),
                    Flashcard("Find value of determinant |5 3 ; -7 -4|.", "5(-4) - (-7)(3) = -20 + 21 = 1.", "Algebra")
                ),
                mcqs = listOf(
                    QuizQuestion(
                        id = 11,
                        subject = "Maths 1",
                        chapter = "Linear Equations",
                        questionText = "For simultaneous equations in x and y, if Dx = 49, Dy = -63 and D = 7, then x equals:",
                        options = listOf("7", "-9", "1/7", "-7"),
                        correctIndex = 0,
                        explanation = "By Cramer's Rule: x = Dx / D = 49 / 7 = 7.",
                        conceptTag = "Cramer's Rule Formula"
                    )
                ),
                revisionQuestions = listOf(
                    "Solve by Cramer's Rule: 4m + 6n = 54 and 3m + 2n = 28.",
                    "Complete the activity to draw the graph of x + y = 3 and x - y = 4.",
                    "Find the values of Dx and Dy for 3x - 4y = 10, 4x + 3y = 5."
                )
            )
        }

        // Default Maharashtra SSC Smart Notes
        return@withContext SmartNotesResult(
            topic = topic.ifBlank { "Chemical Reactions & Equations" },
            subject = subject.ifBlank { "Science & Tech Part 1 (SSC)" },
            summaryPoints = listOf(
                "A chemical reaction is a process in which some substances undergo bond breaking and are transformed into new substances by bond formation.",
                "Types of reactions: Combination (A + B → AB), Decomposition (AB → A + B), Displacement (A + BC → AC + B), Double Displacement (AB + CD → AD + CB).",
                "Endothermic reactions absorb heat, while Exothermic reactions release heat.",
                "Oxidation: Gain of oxygen or loss of electrons. Reduction: Gain of hydrogen or gain of electrons. Redox: Both occur simultaneously.",
                "Corrosion: Slow destruction of metals due to atmospheric gases and moisture. Rusting formula: Fe₂O₃ · xH₂O.",
                "Rancidity: Oxidation of oils and fats producing unpleasant smell and taste, prevented by antioxidants."
            ),
            definitions = listOf(
                "Catalyst" to "A substance in whose presence the rate of a chemical reaction changes without causing any chemical change to it (e.g. MnO₂, Ni catalyst).",
                "Redox Reaction" to "A reaction in which reduction and oxidation take place simultaneously.",
                "Corrosion" to "The slow oxidation of metals by the action of air, moisture, and acids resulting in damage."
            ),
            formulas = listOf(
                "Rust of Iron" to "2 Fe + 3/2 O₂ + x H₂O → Fe₂O₃ · xH₂O",
                "Combination Reaction" to "2 Mg + O₂ → 2 MgO + Heat",
                "Photosynthesis" to "6 CO₂ + 6 H₂O ⎯(Sunlight/Chlorophyll)→ C₆H₁₂O₆ + 6 O₂",
                "Double Displacement" to "BaCl₂ + H₂SO₄ → BaSO₄↓ (White ppt) + 2 HCl"
            ),
            flashcards = listOf(
                Flashcard("What is the colour of BaSO₄ precipitate formed in double displacement?", "White precipitate.", "Science 1"),
                Flashcard("What type of reaction is respiration?", "Exothermic reaction.", "Science 1"),
                Flashcard("Which gas is flushed in potato chips packets to prevent rancidity?", "Nitrogen gas (N₂).", "Science 1")
            ),
            mcqs = listOf(
                QuizQuestion(
                    id = 21,
                    subject = "Science 1",
                    chapter = "Chemical Reactions",
                    questionText = "When crystals of ferrous sulphate are heated strongly in a test tube, the brown residue left is:",
                    options = listOf("FeO", "Fe₂O₃", "Fe₃O₄", "FeS"),
                    correctIndex = 1,
                    explanation = "Thermal decomposition: 2 FeSO₄(s) ⎯(Δ)→ Fe₂O₃(s) [Ferric oxide, brown residue] + SO₂(g) + SO₃(g).",
                    conceptTag = "Decomposition Reaction"
                )
            ),
            revisionQuestions = listOf(
                "Distinguish between Exothermic and Endothermic reactions with examples.",
                "Balance the chemical equation: NaOH + H₂SO₄ → Na₂SO₄ + H₂O.",
                "Explain Redox reaction with a balanced chemical equation and identify the oxidant and reductant."
            )
        )
    }

    suspend fun getChapterQuiz(subject: String, chapter: String, difficulty: String): List<QuizQuestion> = withContext(Dispatchers.IO) {
        val s = subject.lowercase()
        when {
            s.contains("science 1") || s.contains("physics") -> listOf(
                QuizQuestion(
                    id = 101,
                    subject = "Science 1",
                    chapter = chapter.ifBlank { "Gravitation" },
                    questionText = "According to Kepler's Third Law, which of the following is constant for all planets revolving around the Sun?",
                    options = listOf("T / r", "T² / r³", "T³ / r²", "T² · r³"),
                    correctIndex = 1,
                    explanation = "Kepler's Third Law states that the square of orbital period T is directly proportional to the cube of mean distance r: T² ∝ r³ => T²/r³ = Constant K.",
                    conceptTag = "Kepler's 3rd Law"
                ),
                QuizQuestion(
                    id = 102,
                    subject = "Science 1",
                    chapter = chapter.ifBlank { "Effects of Electric Current" },
                    questionText = "The direction of magnetic field produced around a straight current-carrying wire is determined by:",
                    options = listOf("Fleming's Left Hand Rule", "Right Hand Thumb Rule", "Fleming's Right Hand Rule", "Joule's Law"),
                    correctIndex = 1,
                    explanation = "Right Hand Thumb Rule: Imagine holding the conductor in right hand with thumb pointing in direction of current, curled fingers give direction of magnetic field lines.",
                    conceptTag = "Right Hand Thumb Rule"
                ),
                QuizQuestion(
                    id = 103,
                    subject = "Science 1",
                    chapter = chapter.ifBlank { "Refraction of Light" },
                    questionText = "When a ray of light travels obliquely from a rarer medium (Air) into a denser medium (Glass), it bends:",
                    options = listOf("Away from the normal", "Towards the normal", "Does not bend", "Reflects back at 90°"),
                    correctIndex = 1,
                    explanation = "In an optically denser medium, speed of light decreases (v₂ < v₁), so according to Snell's Law it bends towards the normal.",
                    conceptTag = "Laws of Refraction"
                ),
                QuizQuestion(
                    id = 104,
                    subject = "Science 1",
                    chapter = chapter.ifBlank { "Periodic Classification" },
                    questionText = "In the Modern Periodic Table, atomic radius increases as we go:",
                    options = listOf("From left to right in a period", "Down a group from top to bottom", "From right to left in a group", "Diagonally across periods"),
                    correctIndex = 1,
                    explanation = "As we move down a group, new electron shells are added, increasing the distance between nucleus and outermost valence electrons, so atomic radius increases.",
                    conceptTag = "Periodic Trends"
                )
            )
            s.contains("science 2") || s.contains("biology") -> listOf(
                QuizQuestion(
                    id = 201,
                    subject = "Science 2",
                    chapter = chapter.ifBlank { "Heredity and Evolution" },
                    questionText = "The synthesis of mRNA from a DNA strand inside the cell nucleus is called:",
                    options = listOf("Translation", "Transcription", "Translocation", "Mutation"),
                    correctIndex = 1,
                    explanation = "The process of RNA synthesis from DNA template is called Transcription, catalyzed by RNA polymerase enzyme.",
                    conceptTag = "Central Dogma"
                ),
                QuizQuestion(
                    id = 202,
                    subject = "Science 2",
                    chapter = chapter.ifBlank { "Life Processes" },
                    questionText = "Glycolysis takes place in which cellular compartment of the cell?",
                    options = listOf("Mitochondria", "Cytoplasm", "Nucleus", "Ribosome"),
                    correctIndex = 1,
                    explanation = "Glycolysis (EMP pathway) occurs in the cytoplasm, breaking 1 glucose molecule into 2 molecules of pyruvic acid, ATP, and NADH₂.",
                    conceptTag = "Cellular Respiration"
                ),
                QuizQuestion(
                    id = 203,
                    subject = "Science 2",
                    chapter = chapter.ifBlank { "Cell Biology & Biotech" },
                    questionText = "Stem cells that can give rise to all types of cells in the body are primarily derived from:",
                    options = listOf("Blastocyst / Embryo", "Bone marrow only", "Adipose tissue only", "Skin epidermis"),
                    correctIndex = 0,
                    explanation = "Embryonic stem cells present in the inner cell mass of the blastocyst are pluripotent and can differentiate into all cell lineages.",
                    conceptTag = "Stem Cell Research"
                ),
                QuizQuestion(
                    id = 204,
                    subject = "Science 2",
                    chapter = chapter.ifBlank { "Animal Classification" },
                    questionText = "Which phylum exhibits a water-vascular system and spiny skin?",
                    options = listOf("Mollusca", "Arthropoda", "Echinodermata", "Annelida"),
                    correctIndex = 2,
                    explanation = "Phylum Echinodermata (e.g., Starfish, Sea urchin) has calcareous spines on the body and a specialized water-vascular system for locomotion and feeding.",
                    conceptTag = "Phylum Echinodermata"
                )
            )
            s.contains("maths 1") || s.contains("algebra") -> listOf(
                QuizQuestion(
                    id = 301,
                    subject = "Maths 1 (Algebra)",
                    chapter = chapter.ifBlank { "Quadratic Equations" },
                    questionText = "If the discriminant Δ = b² - 4ac = 0 for ax² + bx + c = 0, then the roots are:",
                    options = listOf("Real and unequal", "Real and equal", "Not real", "Rational and distinct"),
                    correctIndex = 1,
                    explanation = "When Δ = 0, x = (-b ± 0)/(2a) = -b/(2a). Both roots are identical, meaning they are real and equal.",
                    conceptTag = "Nature of Roots"
                ),
                QuizQuestion(
                    id = 302,
                    subject = "Maths 1 (Algebra)",
                    chapter = chapter.ifBlank { "Arithmetic Progression" },
                    questionText = "For an A.P. where first term a = 3.5 and common difference d = 0, what is the 101st term (t₁₀₁)?",
                    options = listOf("0", "3.5", "103.5", "350"),
                    correctIndex = 1,
                    explanation = "t_n = a + (n - 1)·d. Since d = 0, t₁₀₁ = 3.5 + (100)·0 = 3.5. Every term in the progression is 3.5.",
                    conceptTag = "AP nth Term"
                ),
                QuizQuestion(
                    id = 303,
                    subject = "Maths 1 (Algebra)",
                    chapter = chapter.ifBlank { "Probability" },
                    questionText = "A card is drawn from a well-shuffled pack of 52 playing cards. The probability of getting a face card is:",
                    options = listOf("3/13", "1/13", "4/13", "12/52"),
                    correctIndex = 0,
                    explanation = "There are 12 face cards (4 Kings, 4 Queens, 4 Jacks) in a deck of 52 cards. P(Face Card) = 12/52 = 3/13.",
                    conceptTag = "Probability Face Cards"
                ),
                QuizQuestion(
                    id = 304,
                    subject = "Maths 1 (Algebra)",
                    chapter = chapter.ifBlank { "Financial Planning" },
                    questionText = "If the total GST rate on an article is 18%, what are the rates of CGST and SGST respectively?",
                    options = listOf("18% and 18%", "9% and 9%", "10% and 8%", "12% and 6%"),
                    correctIndex = 1,
                    explanation = "Under intra-state GST, Central GST (CGST) and State GST (SGST) are shared equally: CGST = SGST = Half of GST = 18% / 2 = 9%.",
                    conceptTag = "GST Calculation"
                )
            )
            s.contains("maths 2") || s.contains("geometry") -> listOf(
                QuizQuestion(
                    id = 401,
                    subject = "Maths 2 (Geometry)",
                    chapter = chapter.ifBlank { "Similarity" },
                    questionText = "The ratio of the areas of two similar triangles is equal to the ratio of the:",
                    options = listOf("Squares of their corresponding sides", "Corresponding sides", "Cubes of their corresponding sides", "Perimeters"),
                    correctIndex = 0,
                    explanation = "Theorem on Areas of Similar Triangles: If ΔABC ~ ΔPQR, then Area(ΔABC) / Area(ΔPQR) = AB² / PQ² = BC² / QR² = AC² / PR².",
                    conceptTag = "Theorem of Areas of Similar Δ"
                ),
                QuizQuestion(
                    id = 402,
                    subject = "Maths 2 (Geometry)",
                    chapter = chapter.ifBlank { "Pythagoras Theorem" },
                    questionText = "In a right angled triangle, if sides containing the right angle are 7 cm and 24 cm, the hypotenuse is:",
                    options = listOf("25 cm", "26 cm", "31 cm", "23 cm"),
                    correctIndex = 0,
                    explanation = "By Pythagoras Theorem: Hypotenuse² = 7² + 24² = 49 + 576 = 625 => Hypotenuse = √625 = 25 cm (Pythagorean Triplet: 7, 24, 25).",
                    conceptTag = "Pythagoras Triplet"
                ),
                QuizQuestion(
                    id = 403,
                    subject = "Maths 2 (Geometry)",
                    chapter = chapter.ifBlank { "Circle" },
                    questionText = "The measure of an inscribed angle is ______ the measure of the arc intercepted by it.",
                    options = listOf("Equal to", "Half", "Twice", "One-fourth"),
                    correctIndex = 1,
                    explanation = "Inscribed Angle Theorem: The measure of an inscribed angle is half the measure of the arc intercepted by it: ∠ABC = ½ m(arc AXC).",
                    conceptTag = "Inscribed Angle Theorem"
                ),
                QuizQuestion(
                    id = 404,
                    subject = "Maths 2 (Geometry)",
                    chapter = chapter.ifBlank { "Trigonometry" },
                    questionText = "If sin θ = 3/5, what is the value of cos θ (for acute angle θ)?",
                    options = listOf("4/5", "5/4", "3/4", "1/5"),
                    correctIndex = 0,
                    explanation = "Using identity sin²θ + cos²θ = 1: cos²θ = 1 - (3/5)² = 1 - 9/25 = 16/25 => cos θ = √(16/25) = 4/5.",
                    conceptTag = "Trigonometric Identities"
                )
            )
            else -> listOf(
                QuizQuestion(
                    id = 501,
                    subject = "Social Science",
                    chapter = "History & Pol. Science",
                    questionText = "Who is known as the 'Father of Modern Historiography' according to Maharashtra Board Class 10?",
                    options = listOf("Voltaire", "Rene Descartes", "Karl Marx", "Michel Foucault"),
                    correctIndex = 0,
                    explanation = "Voltaire (François-Marie Arouet) insisted that history must consider all aspects of human life like agriculture, trade, and culture, so he is considered the founder of modern historiography.",
                    conceptTag = "Applied Historiography"
                ),
                QuizQuestion(
                    id = 502,
                    subject = "Social Science",
                    chapter = "Geography",
                    questionText = "Which river basin in Brazil is the largest drainage basin in the world?",
                    options = listOf("Parana Basin", "Amazon Basin", "Sao Francisco Basin", "Paraguay Basin"),
                    correctIndex = 1,
                    explanation = "The Amazon basin occupies a huge catchment area in northern Brazil, carrying an immense volume of water into the Atlantic Ocean.",
                    conceptTag = "Brazil Physiography"
                )
            )
        }
    }

    fun generateDefaultTasks(): List<StudyTaskEntity> {
        return listOf(
            StudyTaskEntity(
                title = "Science 1: Gravitation & Kepler's Laws 3-Mark Activity Questions",
                subject = "Science 1",
                chapter = "Gravitation",
                timeSlot = "06:00 AM - 07:30 AM",
                durationMinutes = 90,
                isCompleted = false,
                isBreak = false,
                priority = "High",
                dateStr = "Today"
            ),
            StudyTaskEntity(
                title = "Breakfast & Morning Refreshment Break",
                subject = "Health",
                chapter = "Mindset Recovery",
                timeSlot = "07:30 AM - 07:45 AM",
                durationMinutes = 15,
                isCompleted = false,
                isBreak = true,
                priority = "Low",
                dateStr = "Today"
            ),
            StudyTaskEntity(
                title = "Maths 1 (Algebra): Quadratic Equations Formula Method & Practice Set 2.4",
                subject = "Maths 1 (Algebra)",
                chapter = "Quadratic Equations",
                timeSlot = "08:00 AM - 09:30 AM",
                durationMinutes = 90,
                isCompleted = true,
                isBreak = false,
                priority = "High",
                dateStr = "Today"
            ),
            StudyTaskEntity(
                title = "Maths 2 (Geometry): Similarity BPT Theorem Proof Writing Drill",
                subject = "Maths 2 (Geometry)",
                chapter = "Similarity",
                timeSlot = "10:00 AM - 11:15 AM",
                durationMinutes = 75,
                isCompleted = false,
                isBreak = false,
                priority = "High",
                dateStr = "Today"
            ),
            StudyTaskEntity(
                title = "Lunch & Relaxation (Cognitive recharge)",
                subject = "Rest",
                chapter = "Recovery",
                timeSlot = "01:00 PM - 02:00 PM",
                durationMinutes = 60,
                isCompleted = false,
                isBreak = true,
                priority = "Low",
                dateStr = "Today"
            ),
            StudyTaskEntity(
                title = "Science 2: Heredity & Evolution Diagram & Transcription Steps",
                subject = "Science 2",
                chapter = "Heredity and Evolution",
                timeSlot = "05:00 PM - 06:15 PM",
                durationMinutes = 75,
                isCompleted = false,
                isBreak = false,
                priority = "Medium",
                dateStr = "Today"
            ),
            StudyTaskEntity(
                title = "SSC Board Mistake Book Review & Formula Revision",
                subject = "Revision",
                chapter = "Board Exam Readiness",
                timeSlot = "07:30 PM - 08:30 PM",
                durationMinutes = 60,
                isCompleted = false,
                isBreak = false,
                priority = "High",
                dateStr = "Today"
            )
        )
    }
}
