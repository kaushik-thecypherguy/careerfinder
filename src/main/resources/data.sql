-- =============== HINDI (hi) labels & options ===================
WITH q AS (SELECT id, qkey FROM public.q_item)
INSERT INTO public.q_item_locale (q_item_id, locale, question_text, options_json)
SELECT
  q.id,
  'hi',
  CASE q.qkey
    WHEN 'q.sector_primary'       THEN 'रुचि का प्राथमिक क्षेत्र'
    WHEN 'q.availability_weekend' THEN 'क्या आप सप्ताहांत में उपलब्ध हैं?'
    WHEN 'q.comfort_english'      THEN 'अंग्रेज़ी में सहजता'
    WHEN 'q.comfort_hindi'        THEN 'हिंदी में सहजता'
    WHEN 'q.comfort_marathi'      THEN 'मराठी में सहजता'
    WHEN 'q.has_smartphone'       THEN 'क्या आपके पास स्मार्टफोन है?'
    WHEN 'q.internet_quality'     THEN 'इंटरनेट की गुणवत्ता'
    WHEN 'q.family_support'       THEN 'क्या परिवार प्रशिक्षण में आपका साथ देता है?'
    WHEN 'q.physical_work_ok'     THEN 'क्या आप शारीरिक काम में सहज हैं?'
    WHEN 'q.night_shift_ok'       THEN 'क्या आप नाइट शिफ्ट में काम कर सकते हैं?'
    WHEN 'q.training_mode'        THEN 'पसंदीदा प्रशिक्षण मोड'
    WHEN 'q.education_level'      THEN 'सबसे उच्च शिक्षा'
    WHEN 'q.age_group'            THEN 'आयु वर्ग'
    WHEN 'q.employment_status'    THEN 'रोज़गार स्थिति'
    WHEN 'q.goal_type'            THEN 'आपका प्रशिक्षण लक्ष्य'
    WHEN 'q.pref_indoor_outdoor'  THEN 'आप इनडोर या आउटडोर काम पसंद करते हैं?'
    WHEN 'q.travel_ok'            THEN 'यात्रा करने की इच्छा'
    WHEN 'q.lifting_ok'           THEN 'वजन उठाने में सहजता'
    WHEN 'q.reading_writing_ok'   THEN 'चुनी हुई भाषा में पढ़ना/लिखना ठीक है?'
    WHEN 'q.computer_access'      THEN 'कंप्यूटर की उपलब्धता'
  END,
  CASE q.qkey
    WHEN 'q.sector_primary' THEN
      '[{"value":"it","label":"आईटी / कंप्यूटर"},{"value":"manufacturing","label":"मैन्युफैक्चरिंग"},{"value":"electrical","label":"इलेक्ट्रिकल"},{"value":"healthcare","label":"हेल्थकेयर"},{"value":"construction","label":"कंस्ट्रक्शन"},{"value":"solar","label":"नवीकरणीय / सोलर"}]'
    WHEN 'q.availability_weekend' THEN
      '[{"value":"yes","label":"हाँ"},{"value":"no","label":"नहीं"}]'
    WHEN 'q.comfort_english' THEN
      '[{"value":"good","label":"सहज"},{"value":"basic","label":"बुनियादी"},{"value":"none","label":"कोई नहीं"}]'
    WHEN 'q.comfort_hindi' THEN
      '[{"value":"good","label":"सहज"},{"value":"basic","label":"बुनियादी"},{"value":"none","label":"कोई नहीं"}]'
    WHEN 'q.comfort_marathi' THEN
      '[{"value":"good","label":"सहज"},{"value":"basic","label":"बुनियादी"},{"value":"none","label":"कोई नहीं"}]'
    WHEN 'q.has_smartphone' THEN
      '[{"value":"yes","label":"हाँ"},{"value":"no","label":"नहीं"}]'
    WHEN 'q.internet_quality' THEN
      '[{"value":"good","label":"अच्छा"},{"value":"ok","label":"ठीक-ठाक"},{"value":"poor","label":"खराब"}]'
    WHEN 'q.family_support' THEN
      '[{"value":"yes","label":"हाँ"},{"value":"maybe","label":"शायद"},{"value":"no","label":"नहीं"}]'
    WHEN 'q.physical_work_ok' THEN
      '[{"value":"yes","label":"हाँ"},{"value":"no","label":"नहीं"}]'
    WHEN 'q.night_shift_ok' THEN
      '[{"value":"yes","label":"हाँ"},{"value":"no","label":"नहीं"}]'
    WHEN 'q.training_mode' THEN
      '[{"value":"onsite","label":"ऑन-साइट"},{"value":"online","label":"ऑनलाइन"},{"value":"hybrid","label":"हाइब्रिड"}]'
    WHEN 'q.education_level' THEN
      '[{"value":"below10","label":"10वीं से कम"},{"value":"x","label":"10वीं उत्तीर्ण"},{"value":"xii","label":"12वीं उत्तीर्ण"},{"value":"diploma_iti","label":"डिप्लोमा / आईटीआई"},{"value":"ug","label":"स्नातक"},{"value":"pg","label":"स्नातकोत्तर"}]'
    WHEN 'q.age_group' THEN
      '[{"value":"under18","label":"18 से कम"},{"value":"18_21","label":"18–21"},{"value":"22_25","label":"22–25"},{"value":"26_30","label":"26–30"},{"value":"30p","label":"30+"}]'
    WHEN 'q.employment_status' THEN
      '[{"value":"student","label":"विद्यार्थी"},{"value":"unemployed","label":"बेरोज़गार"},{"value":"full","label":"पूर्णकालिक नौकरी"},{"value":"part","label":"आंशिककालिक नौकरी"},{"value":"self","label":"स्वरोज़गार"}]'
    WHEN 'q.goal_type' THEN
      '[{"value":"job","label":"नौकरी पाना"},{"value":"promotion","label":"पदोन्नति"},{"value":"skill","label":"नया कौशल"},{"value":"govt_exam","label":"सरकारी परीक्षा"},{"value":"entrepreneur","label":"उद्यमिता"},{"value":"other","label":"अन्य"}]'
    WHEN 'q.pref_indoor_outdoor' THEN
      '[{"value":"indoor","label":"इनडोर"},{"value":"outdoor","label":"आउटडोर"},{"value":"mixed","label":"मिश्र"}]'
    WHEN 'q.travel_ok' THEN
      '[{"value":"none","label":"यात्रा नहीं"},{"value":"city","label":"शहर के भीतर"},{"value":"outstation","label":"शहर के बाहर"}]'
    WHEN 'q.lifting_ok' THEN
      '[{"value":"none","label":"वजन उठाना नहीं"},{"value":"10kg","label":"10 किग्रा तक"},{"value":"20kg","label":"20 किग्रा तक"}]'
    WHEN 'q.reading_writing_ok' THEN
      '[{"value":"yes","label":"हाँ"},{"value":"no","label":"नहीं"}]'
    WHEN 'q.computer_access' THEN
      '[{"value":"home","label":"घर/व्यक्तिगत"},{"value":"cafe","label":"साइबर कैफ़े"},{"value":"phone_only","label":"सिर्फ़ फ़ोन"},{"value":"none","label":"कोई पहुँच नहीं"}]'
  END
FROM q
ON CONFLICT (q_item_id, locale)
DO UPDATE SET
  question_text = EXCLUDED.question_text,
  options_json  = EXCLUDED.options_json;

-- =============== MARATHI (mr) labels & options ===================
WITH q AS (SELECT id, qkey FROM public.q_item)
INSERT INTO public.q_item_locale (q_item_id, locale, question_text, options_json)
SELECT
  q.id,
  'mr',
  CASE q.qkey
    WHEN 'q.sector_primary'       THEN 'प्राथमिक आवडीचे क्षेत्र'
    WHEN 'q.availability_weekend' THEN 'सप्ताहांताला उपलब्ध आहात का?'
    WHEN 'q.comfort_english'      THEN 'इंग्रजीमध्ये सहजता'
    WHEN 'q.comfort_hindi'        THEN 'हिंदीमध्ये सहजता'
    WHEN 'q.comfort_marathi'      THEN 'मराठीत सहजता'
    WHEN 'q.has_smartphone'       THEN 'तुमच्याकडे स्मार्टफोन आहे का?'
    WHEN 'q.internet_quality'     THEN 'इंटरनेट गुणवत्ता'
    WHEN 'q.family_support'       THEN 'कुटुंब तुमच्या प्रशिक्षणाला साथ देतो का?'
    WHEN 'q.physical_work_ok'     THEN 'आपण शारीरिक कामात सहज आहात का?'
    WHEN 'q.night_shift_ok'       THEN 'रात्रीच्या शिफ्टमध्ये काम करू शकता का?'
    WHEN 'q.training_mode'        THEN 'प्राधान्य प्रशिक्षण पद्धत'
    WHEN 'q.education_level'      THEN 'सर्वोच्च शिक्षण'
    WHEN 'q.age_group'            THEN 'वय गट'
    WHEN 'q.employment_status'    THEN 'रोजगार स्थिती'
    WHEN 'q.goal_type'            THEN 'तुमचे प्रशिक्षण उद्दिष्ट'
    WHEN 'q.pref_indoor_outdoor'  THEN 'घरातील की बाहेरील काम आवडते?'
    WHEN 'q.travel_ok'            THEN 'प्रवास करण्याची तयारी'
    WHEN 'q.lifting_ok'           THEN 'वजन उचलण्यात सहजता'
    WHEN 'q.reading_writing_ok'   THEN 'निवडलेल्या भाषेत वाचन/लेखन ठीक आहे का?'
    WHEN 'q.computer_access'      THEN 'कॉम्प्युटर प्रवेश'
  END,
  CASE q.qkey
    WHEN 'q.sector_primary' THEN
      '[{"value":"it","label":"आयटी / कॉम्प्युटर"},{"value":"manufacturing","label":"मॅन्युफॅक्चरिंग"},{"value":"electrical","label":"इलेक्ट्रिकल"},{"value":"healthcare","label":"हेल्थकेअर"},{"value":"construction","label":"कन्स्ट्रक्शन"},{"value":"solar","label":"नवीन ऊर्जास्रोत / सौर"}]'
    WHEN 'q.availability_weekend' THEN
      '[{"value":"yes","label":"होय"},{"value":"no","label":"नाही"}]'
    WHEN 'q.comfort_english' THEN
      '[{"value":"good","label":"सहज"},{"value":"basic","label":"मूलभूत"},{"value":"none","label":"काहीही नाही"}]'
    WHEN 'q.comfort_hindi' THEN
      '[{"value":"good","label":"सहज"},{"value":"basic","label":"मूलभूत"},{"value":"none","label":"काहीही नाही"}]'
    WHEN 'q.comfort_marathi' THEN
      '[{"value":"good","label":"सहज"},{"value":"basic","label":"मूलभूत"},{"value":"none","label":"काहीही नाही"}]'
    WHEN 'q.has_smartphone' THEN
      '[{"value":"yes","label":"होय"},{"value":"no","label":"नाही"}]'
    WHEN 'q.internet_quality' THEN
      '[{"value":"good","label":"चांगले"},{"value":"ok","label":"ठीक"},{"value":"poor","label":"कमकुवत"}]'
    WHEN 'q.family_support' THEN
      '[{"value":"yes","label":"होय"},{"value":"maybe","label":"कदाचित"},{"value":"no","label":"नाही"}]'
    WHEN 'q.physical_work_ok' THEN
      '[{"value":"yes","label":"होय"},{"value":"no","label":"नाही"}]'
    WHEN 'q.night_shift_ok' THEN
      '[{"value":"yes","label":"होय"},{"value":"no","label":"नाही"}]'
    WHEN 'q.training_mode' THEN
      '[{"value":"onsite","label":"ऑन-साइट"},{"value":"online","label":"ऑनलाइन"},{"value":"hybrid","label":"हायब्रिड"}]'
    WHEN 'q.education_level' THEN
      '[{"value":"below10","label":"१०वीपेक्षा कमी"},{"value":"x","label":"१०वी उत्तीर्ण"},{"value":"xii","label":"१२वी उत्तीर्ण"},{"value":"diploma_iti","label":"डिप्लोमा / आयटीआय"},{"value":"ug","label":"पदवी"},{"value":"pg","label":"पदव्युत्तर"}]'
    WHEN 'q.age_group' THEN
      '[{"value":"under18","label":"१८ पेक्षा कमी"},{"value":"18_21","label":"१८–२१"},{"value":"22_25","label":"२२–२५"},{"value":"26_30","label":"२६–३०"},{"value":"30p","label":"३०+"}]'
    WHEN 'q.employment_status' THEN
      '[{"value":"student","label":"विद्यार्थी"},{"value":"unemployed","label":"बेरोजगार"},{"value":"full","label":"पूर्णवेळ नोकरी"},{"value":"part","label":"अर्धवेळ नोकरी"},{"value":"self","label":"स्वरोजगार"}]'
    WHEN 'q.goal_type' THEN
      '[{"value":"job","label":"नोकरी मिळवणे"},{"value":"promotion","label":"पदोन्नती"},{"value":"skill","label":"नवीन कौशल्य"},{"value":"govt_exam","label":"शासकीय परीक्षा"},{"value":"entrepreneur","label":"उद्योजकता"},{"value":"other","label":"इतर"}]'
    WHEN 'q.pref_indoor_outdoor' THEN
      '[{"value":"indoor","label":"घरातील"},{"value":"outdoor","label":"बाहेरील"},{"value":"mixed","label":"मिश्र"}]'
    WHEN 'q.travel_ok' THEN
      '[{"value":"none","label":"प्रवास नाही"},{"value":"city","label":"शहरात"},{"value":"outstation","label":"शहराबाहेर"}]'
    WHEN 'q.lifting_ok' THEN
      '[{"value":"none","label":"उचल नाही"},{"value":"10kg","label":"१० कि.ग्रा. पर्यंत"},{"value":"20kg","label":"२० कि.ग्रा. पर्यंत"}]'
    WHEN 'q.reading_writing_ok' THEN
      '[{"value":"yes","label":"होय"},{"value":"no","label":"नाही"}]'
    WHEN 'q.computer_access' THEN
      '[{"value":"home","label":"घर/वैयक्तिक"},{"value":"cafe","label":"सायबर कॅफे"},{"value":"phone_only","label":"फक्त फोन"},{"value":"none","label":"प्रवेश नाही"}]'
  END
FROM q
ON CONFLICT (q_item_id, locale)
DO UPDATE SET
  question_text = EXCLUDED.question_text,
  options_json  = EXCLUDED.options_json;