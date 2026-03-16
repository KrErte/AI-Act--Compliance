-- ================================================================
-- AI Act Risk Classification Questions
-- Encodes the logic of Articles 5, 6, Annex III, and Article 50
-- ================================================================

-- === CATEGORY: PROHIBITED PRACTICES (Article 5) ===

INSERT INTO classification_question (id, question_key, question_text, question_type, category, help_text, sort_order)
VALUES
(uuid_generate_v4(), 'PROHIBITED_SOCIAL_SCORING',
 'Does the AI system evaluate or classify natural persons based on their social behavior or personal characteristics, leading to detrimental or unfavorable treatment (social scoring)?',
 'YES_NO', 'PROHIBITED', 'Article 5(1)(c): Social scoring by public authorities or on their behalf is prohibited.', 1),

(uuid_generate_v4(), 'PROHIBITED_SUBLIMINAL',
 'Does the AI system deploy subliminal techniques beyond a person''s consciousness, or purposefully manipulative or deceptive techniques, to materially distort behavior causing significant harm?',
 'YES_NO', 'PROHIBITED', 'Article 5(1)(a): Techniques that manipulate beyond consciousness are prohibited.', 2),

(uuid_generate_v4(), 'PROHIBITED_VULNERABILITY',
 'Does the AI system exploit vulnerabilities of a specific group of persons due to their age, disability, or social/economic situation to materially distort behavior causing significant harm?',
 'YES_NO', 'PROHIBITED', 'Article 5(1)(b): Exploiting vulnerabilities of specific groups is prohibited.', 3),

(uuid_generate_v4(), 'PROHIBITED_FACIAL_SCRAPING',
 'Does the AI system create or expand facial recognition databases through untargeted scraping of facial images from the internet or CCTV footage?',
 'YES_NO', 'PROHIBITED', 'Article 5(1)(e): Untargeted scraping of facial images is prohibited.', 4),

(uuid_generate_v4(), 'PROHIBITED_EMOTION_WORKPLACE',
 'Does the AI system infer emotions of natural persons in the areas of workplace or education, except for medical or safety reasons?',
 'YES_NO', 'PROHIBITED', 'Article 5(1)(f): Emotion recognition in workplace/education is prohibited except for medical/safety purposes.', 5),

(uuid_generate_v4(), 'PROHIBITED_BIOMETRIC_CATEGORIZATION',
 'Does the AI system categorize natural persons based on biometric data to deduce or infer their race, political opinions, trade union membership, religious beliefs, sex life, or sexual orientation?',
 'YES_NO', 'PROHIBITED', 'Article 5(1)(g): Biometric categorization for sensitive attributes is prohibited.', 6),

(uuid_generate_v4(), 'PROHIBITED_PREDICTIVE_POLICING',
 'Does the AI system make risk assessments of natural persons to predict the risk of criminal offending based solely on profiling or personality traits?',
 'YES_NO', 'PROHIBITED', 'Article 5(1)(d): Individual predictive policing based solely on profiling is prohibited.', 7),

-- === CATEGORY: HIGH-RISK ANNEX III AREAS ===

(uuid_generate_v4(), 'HIGH_BIOMETRICS',
 'Is the AI system used for biometric identification, categorization, or emotion recognition (other than those prohibited)?',
 'YES_NO', 'HIGH_RISK_ANNEX_III', 'Annex III, Area 1: Biometrics — remote biometric identification, categorization, emotion recognition.', 10),

(uuid_generate_v4(), 'HIGH_CRITICAL_INFRASTRUCTURE',
 'Is the AI system used as a safety component or for management/operation of critical digital infrastructure, road traffic, or supply of water, gas, heating, or electricity?',
 'YES_NO', 'HIGH_RISK_ANNEX_III', 'Annex III, Area 2: Critical Infrastructure — AI as safety component or infrastructure management.', 11),

(uuid_generate_v4(), 'HIGH_EDUCATION',
 'Is the AI system used to determine access to, admission to, or assignment in educational/vocational institutions, or to evaluate learning outcomes or detect prohibited behavior during tests?',
 'YES_NO', 'HIGH_RISK_ANNEX_III', 'Annex III, Area 3: Education and Vocational Training.', 12),

(uuid_generate_v4(), 'HIGH_EMPLOYMENT',
 'Is the AI system used for recruitment, selection, HR decisions (promotion, termination, task allocation, performance monitoring) in employment?',
 'YES_NO', 'HIGH_RISK_ANNEX_III', 'Annex III, Area 4: Employment, Workers Management, Access to Self-Employment.', 13),

(uuid_generate_v4(), 'HIGH_ESSENTIAL_SERVICES',
 'Is the AI system used to evaluate eligibility for essential public/private services and benefits (e.g., healthcare, credit scoring, insurance, emergency dispatch)?',
 'YES_NO', 'HIGH_RISK_ANNEX_III', 'Annex III, Area 5: Access to Essential Private and Public Services.', 14),

(uuid_generate_v4(), 'HIGH_LAW_ENFORCEMENT',
 'Is the AI system used by or on behalf of law enforcement for risk assessment, polygraph/deception detection, evidence evaluation, crime prediction (geographic), or profiling during investigations?',
 'YES_NO', 'HIGH_RISK_ANNEX_III', 'Annex III, Area 6: Law Enforcement.', 15),

(uuid_generate_v4(), 'HIGH_MIGRATION',
 'Is the AI system used for migration, asylum, or border control (risk assessment, document authenticity, examining applications)?',
 'YES_NO', 'HIGH_RISK_ANNEX_III', 'Annex III, Area 7: Migration, Asylum and Border Control Management.', 16),

(uuid_generate_v4(), 'HIGH_JUSTICE',
 'Is the AI system used to assist judicial authorities in researching/interpreting facts and law, or applying law to facts (except purely administrative tasks)?',
 'YES_NO', 'HIGH_RISK_ANNEX_III', 'Annex III, Area 8: Administration of Justice and Democratic Processes.', 17),

-- === CATEGORY: HIGH-RISK SAFETY COMPONENT (Article 6(1)) ===

(uuid_generate_v4(), 'HIGH_SAFETY_COMPONENT',
 'Is the AI system intended to be used as a safety component of a product, or is it itself a product, covered by EU harmonization legislation listed in Annex I?',
 'YES_NO', 'HIGH_RISK_SAFETY', 'Article 6(1): AI systems that are safety components of products covered by EU harmonization legislation (e.g., machinery, toys, medical devices, vehicles, aviation).', 20),

(uuid_generate_v4(), 'HIGH_SAFETY_THIRD_PARTY',
 'Is the product in which the AI is embedded, or the AI system itself, required to undergo third-party conformity assessment under the applicable EU harmonization legislation?',
 'YES_NO', 'HIGH_RISK_SAFETY', 'Article 6(1)(b): Third-party conformity assessment requirement under Annex I legislation.', 21),

-- === CATEGORY: TRANSPARENCY / LIMITED RISK (Article 50) ===

(uuid_generate_v4(), 'LIMITED_CHATBOT',
 'Does the AI system interact directly with natural persons (e.g., chatbot, virtual assistant)?',
 'YES_NO', 'LIMITED_RISK', 'Article 50(1): Providers must ensure persons are informed they are interacting with an AI system.', 30),

(uuid_generate_v4(), 'LIMITED_SYNTHETIC_CONTENT',
 'Does the AI system generate or manipulate synthetic audio, image, video, or text content (including deepfakes)?',
 'YES_NO', 'LIMITED_RISK', 'Article 50(2)(4): Synthetic/deepfake content must be labeled as AI-generated.', 31),

(uuid_generate_v4(), 'LIMITED_EMOTION_RECOGNITION',
 'Does the AI system perform emotion recognition or biometric categorization (in contexts where it is not prohibited)?',
 'YES_NO', 'LIMITED_RISK', 'Article 50(3): Users must be informed about emotion recognition / biometric categorization.', 32),

-- === CATEGORY: GENERAL PURPOSE / CONTEXT ===

(uuid_generate_v4(), 'CONTEXT_GPAI_MODEL',
 'Is this a general-purpose AI model (GPAI) or a system built on top of a GPAI model (e.g., GPT-4, Claude, Gemini)?',
 'YES_NO', 'CONTEXT', 'GPAI models have additional obligations under Article 51-56 of the AI Act.', 40),

(uuid_generate_v4(), 'CONTEXT_GPAI_SYSTEMIC_RISK',
 'If this uses a GPAI model, does the model have systemic risk (training compute > 10^25 FLOPs, or designated by the AI Office)?',
 'YES_NO', 'CONTEXT', 'GPAI models with systemic risk face stricter requirements under Article 51(2).', 41),

(uuid_generate_v4(), 'CONTEXT_PROVIDER_OR_DEPLOYER',
 'Is your organization the provider (develops/places on market) or deployer (uses under own authority) of this AI system?',
 'SINGLE_CHOICE', 'CONTEXT', 'Providers and deployers have different obligations under the AI Act.',  42),

(uuid_generate_v4(), 'CONTEXT_EU_MARKET',
 'Is this AI system placed on the market, put into service, or used within the European Union?',
 'YES_NO', 'CONTEXT', 'The AI Act applies to AI systems placed on the EU market or whose output is used in the EU.', 43),

-- === CATEGORY: ANNEX III EXEMPTION CHECK ===

(uuid_generate_v4(), 'EXEMPTION_NARROW_PROCEDURAL',
 'Does the AI system perform a narrow procedural task (e.g., converting unstructured data to structured, classifying documents by categories with no assessment of persons)?',
 'YES_NO', 'EXEMPTION', 'Article 6(3): Some Annex III systems may be exempt if they only perform narrow procedural tasks.', 50),

(uuid_generate_v4(), 'EXEMPTION_IMPROVE_HUMAN_ACTIVITY',
 'Is the AI system intended only to improve the result of a previously completed human activity?',
 'YES_NO', 'EXEMPTION', 'Article 6(3): Systems that only improve prior human work may be exempt.', 51),

(uuid_generate_v4(), 'EXEMPTION_PREPARATORY_TASK',
 'Does the AI system only perform a preparatory task to an assessment relevant for the intended use of the high-risk system listed in Annex III?',
 'YES_NO', 'EXEMPTION', 'Article 6(3): Preparatory tasks that do not constitute the assessment itself may be exempt.', 52),

(uuid_generate_v4(), 'EXEMPTION_DETECT_PATTERNS',
 'Does the AI system only detect decision patterns or deviations from prior decision patterns, without replacing or influencing the previously completed human assessment?',
 'YES_NO', 'EXEMPTION', 'Article 6(3): Pattern detection without replacing human assessment may be exempt.', 53);

-- Add options for the single-choice question
UPDATE classification_question
SET options = '["PROVIDER", "DEPLOYER", "BOTH"]'::jsonb
WHERE question_key = 'CONTEXT_PROVIDER_OR_DEPLOYER';
