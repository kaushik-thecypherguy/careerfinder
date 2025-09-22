-- q_item ---------------------------------------
INSERT INTO q_item (qkey, section_key, order_index, qtype, required, active)
SELECT 'q.sector_primary','profile',1,'SINGLE',true,true WHERE NOT EXISTS (SELECT 1 FROM q_item WHERE qkey='q.sector_primary');
INSERT INTO q_item (qkey, section_key, order_index, qtype, required, active)
SELECT 'q.availability_weekend','profile',2,'SINGLE',true,true WHERE NOT EXISTS (SELECT 1 FROM q_item WHERE qkey='q.availability_weekend');
INSERT INTO q_item (qkey, section_key, order_index, qtype, required, active)
SELECT 'q.comfort_english','profile',3,'SINGLE',true,true WHERE NOT EXISTS (SELECT 1 FROM q_item WHERE qkey='q.comfort_english');
INSERT INTO q_item (qkey, section_key, order_index, qtype, required, active)
SELECT 'q.comfort_hindi','profile',4,'SINGLE',true,true WHERE NOT EXISTS (SELECT 1 FROM q_item WHERE qkey='q.comfort_hindi');
INSERT INTO q_item (qkey, section_key, order_index, qtype, required, active)
SELECT 'q.comfort_marathi','profile',5,'SINGLE',true,true WHERE NOT EXISTS (SELECT 1 FROM q_item WHERE qkey='q.comfort_marathi');
INSERT INTO q_item (qkey, section_key, order_index, qtype, required, active)
SELECT 'q.has_smartphone','profile',6,'SINGLE',true,true WHERE NOT EXISTS (SELECT 1 FROM q_item WHERE qkey='q.has_smartphone');
INSERT INTO q_item (qkey, section_key, order_index, qtype, required, active)
SELECT 'q.internet_quality','profile',7,'SINGLE',true,true WHERE NOT EXISTS (SELECT 1 FROM q_item WHERE qkey='q.internet_quality');
INSERT INTO q_item (qkey, section_key, order_index, qtype, required, active)
SELECT 'q.family_support','profile',8,'SINGLE',true,true WHERE NOT EXISTS (SELECT 1 FROM q_item WHERE qkey='q.family_support');
INSERT INTO q_item (qkey, section_key, order_index, qtype, required, active)
SELECT 'q.physical_work_ok','profile',9,'SINGLE',true,true WHERE NOT EXISTS (SELECT 1 FROM q_item WHERE qkey='q.physical_work_ok');
INSERT INTO q_item (qkey, section_key, order_index, qtype, required, active)
SELECT 'q.night_shift_ok','profile',10,'SINGLE',true,true WHERE NOT EXISTS (SELECT 1 FROM q_item WHERE qkey='q.night_shift_ok');

INSERT INTO q_item (qkey, section_key, order_index, qtype, required, active)
SELECT 'q.training_mode','prefs',1,'SINGLE',true,true WHERE NOT EXISTS (SELECT 1 FROM q_item WHERE qkey='q.training_mode');
INSERT INTO q_item (qkey, section_key, order_index, qtype, required, active)
SELECT 'q.education_level','prefs',2,'SINGLE',true,true WHERE NOT EXISTS (SELECT 1 FROM q_item WHERE qkey='q.education_level');
INSERT INTO q_item (qkey, section_key, order_index, qtype, required, active)
SELECT 'q.age_group','prefs',3,'SINGLE',true,true WHERE NOT EXISTS (SELECT 1 FROM q_item WHERE qkey='q.age_group');
INSERT INTO q_item (qkey, section_key, order_index, qtype, required, active)
SELECT 'q.employment_status','prefs',4,'SINGLE',true,true WHERE NOT EXISTS (SELECT 1 FROM q_item WHERE qkey='q.employment_status');
INSERT INTO q_item (qkey, section_key, order_index, qtype, required, active)
SELECT 'q.goal_type','prefs',5,'SINGLE',true,true WHERE NOT EXISTS (SELECT 1 FROM q_item WHERE qkey='q.goal_type');
INSERT INTO q_item (qkey, section_key, order_index, qtype, required, active)
SELECT 'q.pref_indoor_outdoor','prefs',6,'SINGLE',true,true WHERE NOT EXISTS (SELECT 1 FROM q_item WHERE qkey='q.pref_indoor_outdoor');
INSERT INTO q_item (qkey, section_key, order_index, qtype, required, active)
SELECT 'q.travel_ok','prefs',7,'SINGLE',true,true WHERE NOT EXISTS (SELECT 1 FROM q_item WHERE qkey='q.travel_ok');
INSERT INTO q_item (qkey, section_key, order_index, qtype, required, active)
SELECT 'q.lifting_ok','prefs',8,'SINGLE',true,true WHERE NOT EXISTS (SELECT 1 FROM q_item WHERE qkey='q.lifting_ok');
INSERT INTO q_item (qkey, section_key, order_index, qtype, required, active)
SELECT 'q.reading_writing_ok','prefs',9,'SINGLE',true,true WHERE NOT EXISTS (SELECT 1 FROM q_item WHERE qkey='q.reading_writing_ok');
INSERT INTO q_item (qkey, section_key, order_index, qtype, required, active)
SELECT 'q.computer_access','prefs',10,'SINGLE',true,true WHERE NOT EXISTS (SELECT 1 FROM q_item WHERE qkey='q.computer_access');

-- EN labels + options ---------------------------------------------------
WITH q AS (SELECT id, qkey FROM q_item)
INSERT INTO q_item_locale (q_item_id, locale, question_text, options_json)
SELECT q.id, 'en',
       CASE q.qkey
           WHEN 'q.sector_primary'       THEN 'Primary sector of interest'
           WHEN 'q.availability_weekend' THEN 'Available on weekends?'
           WHEN 'q.comfort_english'      THEN 'English comfort'
           WHEN 'q.comfort_hindi'        THEN 'Hindi comfort'
           WHEN 'q.comfort_marathi'      THEN 'Marathi comfort'
           WHEN 'q.has_smartphone'       THEN 'Do you have a smartphone?'
           WHEN 'q.internet_quality'     THEN 'Internet quality'
           WHEN 'q.family_support'       THEN 'Family supports your training?'
           WHEN 'q.physical_work_ok'     THEN 'Comfortable with physical work?'
           WHEN 'q.night_shift_ok'       THEN 'Okay with night shift?'
           WHEN 'q.training_mode'        THEN 'Preferred training mode'
           WHEN 'q.education_level'      THEN 'Highest education'
           WHEN 'q.age_group'            THEN 'Age group'
           WHEN 'q.employment_status'    THEN 'Employment status'
           WHEN 'q.goal_type'            THEN 'Your training goal'
           WHEN 'q.pref_indoor_outdoor'  THEN 'Prefer indoor or outdoor work?'
           WHEN 'q.travel_ok'            THEN 'Travel willingness'
           WHEN 'q.lifting_ok'           THEN 'Lifting comfort'
           WHEN 'q.reading_writing_ok'   THEN 'Reading/Writing in chosen language OK?'
           WHEN 'q.computer_access'      THEN 'Computer access'
       END,
       CASE q.qkey
           WHEN 'q.sector_primary'       THEN '[{"value":"it","label":"IT / Computer"},{"value":"manufacturing","label":"Manufacturing"},{"value":"electrical","label":"Electrical"},{"value":"healthcare","label":"Healthcare"},{"value":"construction","label":"Construction"},{"value":"solar","label":"Renewable / Solar"}]'
           WHEN 'q.availability_weekend' THEN '[{"value":"yes","label":"Yes"},{"value":"no","label":"No"}]'
           WHEN 'q.comfort_english'      THEN '[{"value":"good","label":"Comfortable"},{"value":"basic","label":"Basic"},{"value":"none","label":"None"}]'
           WHEN 'q.comfort_hindi'        THEN '[{"value":"good","label":"Comfortable"},{"value":"basic","label":"Basic"},{"value":"none","label":"None"}]'
           WHEN 'q.comfort_marathi'      THEN '[{"value":"good","label":"Comfortable"},{"value":"basic","label":"Basic"},{"value":"none","label":"None"}]'
           WHEN 'q.has_smartphone'       THEN '[{"value":"yes","label":"Yes"},{"value":"no","label":"No"}]'
           WHEN 'q.internet_quality'     THEN '[{"value":"good","label":"Good"},{"value":"ok","label":"OK"},{"value":"poor","label":"Poor"}]'
           WHEN 'q.family_support'       THEN '[{"value":"yes","label":"Yes"},{"value":"maybe","label":"Maybe"},{"value":"no","label":"No"}]'
           WHEN 'q.physical_work_ok'     THEN '[{"value":"yes","label":"Yes"},{"value":"no","label":"No"}]'
           WHEN 'q.night_shift_ok'       THEN '[{"value":"yes","label":"Yes"},{"value":"no","label":"No"}]'
           WHEN 'q.training_mode'        THEN '[{"value":"onsite","label":"On-site"},{"value":"online","label":"Online"},{"value":"hybrid","label":"Hybrid"}]'
           WHEN 'q.education_level'      THEN '[{"value":"below10","label":"Below 10th"},{"value":"x","label":"10th Pass"},{"value":"xii","label":"12th Pass"},{"value":"diploma_iti","label":"Diploma / ITI"},{"value":"ug","label":"Undergraduate"},{"value":"pg","label":"Postgraduate"}]'
           WHEN 'q.age_group'            THEN '[{"value":"under18","label":"Under 18"},{"value":"18_21","label":"18–21"},{"value":"22_25","label":"22–25"},{"value":"26_30","label":"26–30"},{"value":"30p","label":"30+"}]'
           WHEN 'q.employment_status'    THEN '[{"value":"student","label":"Student"},{"value":"unemployed","label":"Unemployed"},{"value":"full","label":"Employed full-time"},{"value":"part","label":"Employed part-time"},{"value":"self","label":"Self-employed"}]'
           WHEN 'q.goal_type'            THEN '[{"value":"job","label":"Get a job"},{"value":"promotion","label":"Promotion"},{"value":"skill","label":"New skill"},{"value":"govt_exam","label":"Government exam"},{"value":"entrepreneur","label":"Entrepreneurship"},{"value":"other","label":"Other"}]'
           WHEN 'q.pref_indoor_outdoor'  THEN '[{"value":"indoor","label":"Indoor"},{"value":"outdoor","label":"Outdoor"},{"value":"mixed","label":"Mixed"}]'
           WHEN 'q.travel_ok'            THEN '[{"value":"none","label":"No travel"},{"value":"city","label":"Within city"},{"value":"outstation","label":"Outside city"}]'
           WHEN 'q.lifting_ok'           THEN '[{"value":"none","label":"No lifting"},{"value":"10kg","label":"Up to 10 kg"},{"value":"20kg","label":"Up to 20 kg"}]'
           WHEN 'q.reading_writing_ok'   THEN '[{"value":"yes","label":"Yes"},{"value":"no","label":"No"}]'
           WHEN 'q.computer_access'      THEN '[{"value":"home","label":"Home/Personal"},{"value":"cafe","label":"Cyber cafe"},{"value":"phone_only","label":"Phone only"},{"value":"none","label":"No access"}]'
       END
FROM q
WHERE NOT EXISTS (SELECT 1 FROM q_item_locale l WHERE l.q_item_id = q.id AND l.locale = 'en');

-- HI / MR placeholders ---------------------------------------------------
WITH q AS (SELECT id FROM q_item)
INSERT INTO q_item_locale (q_item_id, locale, question_text, options_json)
SELECT q.id, 'hi', '—', '[]'
FROM q WHERE NOT EXISTS (SELECT 1 FROM q_item_locale l WHERE l.q_item_id = q.id AND l.locale = 'hi');

WITH q AS (SELECT id FROM q_item)
INSERT INTO q_item_locale (q_item_id, locale, question_text, options_json)
SELECT q.id, 'mr', '—', '[]'
FROM q WHERE NOT EXISTS (SELECT 1 FROM q_item_locale l WHERE l.q_item_id = q.id AND l.locale = 'mr');