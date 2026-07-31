package com.study.kids.config;

import com.study.kids.common.JsonLines;
import com.study.kids.dao.ChineseCharacterMapper;
import com.study.kids.dao.EnglishWordMapper;
import com.study.kids.dao.PoemMapper;
import com.study.kids.entity.ChineseCharacter;
import com.study.kids.entity.EnglishWord;
import com.study.kids.entity.Poem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 启动时补齐题库：已有数据不覆盖，仅插入缺失条目。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final ChineseCharacterMapper characterMapper;
    private final PoemMapper poemMapper;
    private final EnglishWordMapper englishWordMapper;

    @Override
    public void run(ApplicationArguments args) {
        int c = seedCharacters();
        int p = seedPoems();
        int e = seedEnglish();
        if (c + p + e > 0) {
            log.info("题库补齐完成：汉字 +{}，古诗 +{}，英语 +{}", c, p, e);
        }
    }

    private int seedCharacters() {
        Set<String> exists = characterMapper.findAll().stream()
                .map(ChineseCharacter::getCharText)
                .collect(Collectors.toCollection(HashSet::new));
        Object[][] rows = {
                {"一", "yī", "横", "一个,一起,一天", "我有一个苹果。", 1},
                {"二", "èr", "横,横", "二月,两个,第二", "树上有两只鸟。", 2},
                {"三", "sān", "横,横,横", "三个,三月,三年", "三只小熊去野餐。", 3},
                {"四", "sì", "竖,横折,撇,点", "四个,四季,四周", "一年有四个季节。", 4},
                {"五", "wǔ", "横,竖,横折,横,竖弯钩", "五个,五月,五行", "我有五根手指。", 5},
                {"六", "liù", "点,横,撇,点", "六个,六月,六一", "六一儿童节真开心。", 6},
                {"七", "qī", "横,竖弯钩", "七个,七月,七夕", "天上有七颗小星星。", 7},
                {"八", "bā", "撇,捺", "八个,八月,八方", "八只小鸭排排走。", 8},
                {"九", "jiǔ", "撇,横折弯钩", "九个,九月,九天", "小猫有九条命吗？", 9},
                {"十", "shí", "横,竖", "十个,十月,十分", "我有十个好朋友。", 10},
                {"人", "rén", "撇,捺", "大人,好人,人们", "人要好好学习。", 11},
                {"大", "dà", "横,撇,捺", "大象,太阳,大家", "大象好大呀！", 12},
                {"小", "xiǎo", "竖钩,撇,点", "小猫,小鸟,小孩", "小猫爱喝牛奶。", 13},
                {"天", "tiān", "横,横,撇,捺", "天空,白天,今天", "今天天气真好。", 14},
                {"日", "rì", "竖,横折,横,横", "日出,日子,生日", "太阳每天从东边升起。", 15},
                {"月", "yuè", "撇,横折钩,横,横", "月亮,月光,一月", "月亮弯弯像小船。", 16},
                {"水", "shuǐ", "竖钩,横撇,撇,捺", "水果,喝水,河水", "小朋友要多喝水。", 17},
                {"火", "huǒ", "点,撇,撇,捺", "火车,火花,大火", "火车开得真快。", 18},
                {"山", "shān", "竖,竖折,竖", "大山,山水,高山", "山上有好多树。", 19},
                {"木", "mù", "横,竖,撇,捺", "木头,树木,果木", "大树是木头做成的吗？", 20},
                {"口", "kǒu", "竖,横折,横", "大口,人口,出口", "张开大口说啊。", 21},
                {"手", "shǒu", "撇,横,横,竖钩", "小手,洗手,拍手", "洗手要洗干净。", 22},
                {"足", "zú", "竖,横,横,竖,横,捺", "足球,充足,满足", "我喜欢踢足球。", 23},
                {"花", "huā", "横,竖,竖,撇,撇,竖弯钩,撇,竖弯钩", "花朵,鲜花,开花", "春天开满鲜花。", 24},
                {"鸟", "niǎo", "撇,横折钩,点,竖折折钩,横", "小鸟,飞鸟,鸟窝", "小鸟在树上唱歌。", 25},
                {"鱼", "yú", "撇,横折,横,竖,横折,横,横", "小鱼,钓鱼,金鱼", "小鱼在水里游。", 26},
                {"猫", "māo", "撇,弯钩,撇,点,横,竖,竖,横,竖,横折,横,横", "小猫,猫咪,花猫", "小猫喵喵叫。", 27},
                {"狗", "gǒu", "撇,弯钩,撇,点,撇,横折钩,竖,横", "小狗,狗狗,黄狗", "小狗汪汪叫。", 28},
                {"牛", "niú", "撇,横,横,竖", "牛奶,黄牛,牛仔", "小牛爱吃草。", 29},
                {"羊", "yáng", "点,撇,横,横,横,竖", "小羊,山羊,羊毛", "小羊咩咩叫。", 30},
                {"马", "mǎ", "横折,竖折折钩,横", "小马,木马,马上", "小马跑得真快。", 31},
                {"车", "chē", "横,撇折,横,竖", "汽车,火车,开车", "汽车嘀嘀叫。", 32},
                {"门", "mén", "点,竖,横折钩", "大门,开门,门口", "请把门关好。", 33},
                {"开", "kāi", "横,横,撇,竖", "开门,开心,打开", "我今天很开心。", 34},
                {"心", "xīn", "点,卧钩,点,点", "开心,小心,爱心", "妈妈有一颗爱心。", 35},
                {"见", "jiàn", "竖,横折,撇,竖弯钩", "看见,见面,再见", "明天再见！", 36},
                {"听", "tīng", "竖,横折,横,撇,撇,横,竖,竖", "听见,听话,听说", "小朋友要认真听。", 37},
                {"看", "kàn", "撇,横,横,撇,竖,横折,横,横", "看见,好看,看书", "我喜欢看书。", 38},
                {"走", "zǒu", "横,竖,横,撇,捺", "走路,走开,走走", "我们一起去散步。", 39},
                {"来", "lái", "横,点,撇,竖,撇,捺", "过来,来了,回来", "爸爸回来了。", 40},
                {"去", "qù", "横,竖,横,撇折,点", "出去,去年,过去", "我们出去玩吧。", 41},
                {"上", "shàng", "竖,横,横", "上面,上学,早上", "早上要上学。", 42},
                {"下", "xià", "横,竖,点", "下面,下雨,一下", "外面下雨了。", 43},
                {"中", "zhōng", "竖,横折,横,竖", "中国,中间,中午", "我爱中国。", 44},
                {"学", "xué", "点,点,撇,点,横撇,横钩,竖钩,横", "学习,学生,学校", "我爱学习。", 45},
                {"友", "yǒu", "横,撇,横撇,捺", "朋友,友好,友情", "我们是好朋友。", 46},
                {"爸", "bà", "撇,点,撇,捺,竖,横折,横,竖弯钩", "爸爸,爸妈,父爱", "我爱爸爸。", 47},
                {"妈", "mā", "撇点,撇,横,撇折,点,横折,竖折折钩,横", "妈妈,爸妈,妈咪", "我爱妈妈。", 48},
                {"红", "hóng", "撇折,撇折,提,横,竖,横", "红色,红花,红红", "红花开得真漂亮。", 49},
                {"绿", "lǜ", "撇折,撇折,提,横撇,捺,横,竖钩,撇,点", "绿色,绿叶,绿绿", "树叶是绿色的。", 50},
                {"白", "bái", "撇,竖,横折,横,横", "白云,白天,白菜", "天上有白云。", 51},
                {"云", "yún", "横,横,撇折,点", "白云,云朵,乌云", "云朵像棉花糖。", 52},
                {"风", "fēng", "撇,横折弯钩,撇,点", "大风,刮风,风扇", "今天刮风了。", 53},
                {"雨", "yǔ", "横,竖,横折钩,竖,点,点,点,点", "下雨,雨水,雨伞", "下雨要打伞。", 54},
                {"电", "diàn", "竖,横折,横,竖,竖弯钩", "电话,电脑,闪电", "我会打电话。", 55},
                {"光", "guāng", "竖,点,撇,横,撇,竖弯钩", "阳光,光明,灯光", "阳光暖洋洋。", 56},
                {"早", "zǎo", "竖,横折,横,横,横,竖", "早上,早餐,早安", "早上好！", 57},
                {"晚", "wǎn", "竖,横折,横,横,撇,竖撇,横折钩,撇,竖弯钩", "晚上,晚饭,晚安", "晚安，做个好梦。", 58},
                {"春", "chūn", "横,横,横,撇,捺,竖,横折,横,横", "春天,春节,春风", "春天来了。", 59},
                {"夏", "xià", "横,撇,竖,横折,横,横,横撇,捺", "夏天,立夏,夏日", "夏天可以游泳。", 60},
        };
        int added = 0;
        for (Object[] r : rows) {
            String ch = (String) r[0];
            if (exists.contains(ch)) {
                continue;
            }
            ChineseCharacter c = new ChineseCharacter();
            c.setCharText(ch);
            c.setPinyin((String) r[1]);
            c.setStrokeOrder((String) r[2]);
            c.setWords((String) r[3]);
            c.setSentence((String) r[4]);
            c.setSortOrder((Integer) r[5]);
            characterMapper.insert(c);
            exists.add(ch);
            added++;
        }
        return added;
    }

    private int seedPoems() {
        Set<String> exists = poemMapper.findAll().stream()
                .map(Poem::getTitle)
                .collect(Collectors.toCollection(HashSet::new));
        Object[][] rows = {
                {"静夜思", "李白", "唐", List.of("床前明月光", "疑是地上霜", "举头望明月", "低头思故乡"), 1},
                {"咏鹅", "骆宾王", "唐", List.of("鹅鹅鹅", "曲项向天歌", "白毛浮绿水", "红掌拨清波"), 2},
                {"春晓", "孟浩然", "唐", List.of("春眠不觉晓", "处处闻啼鸟", "夜来风雨声", "花落知多少"), 3},
                {"登鹳雀楼", "王之涣", "唐", List.of("白日依山尽", "黄河入海流", "欲穷千里目", "更上一层楼"), 4},
                {"悯农", "李绅", "唐", List.of("锄禾日当午", "汗滴禾下土", "谁知盘中餐", "粒粒皆辛苦"), 5},
                {"赠汪伦", "李白", "唐", List.of("李白乘舟将欲行", "忽闻岸上踏歌声", "桃花潭水深千尺", "不及汪伦送我情"), 6},
                {"望庐山瀑布", "李白", "唐", List.of("日照香炉生紫烟", "遥看瀑布挂前川", "飞流直下三千尺", "疑是银河落九天"), 7},
                {"早发白帝城", "李白", "唐", List.of("朝辞白帝彩云间", "千里江陵一日还", "两岸猿声啼不住", "轻舟已过万重山"), 8},
                {"寻隐者不遇", "贾岛", "唐", List.of("松下问童子", "言师采药去", "只在此山中", "云深不知处"), 9},
                {"江雪", "柳宗元", "唐", List.of("千山鸟飞绝", "万径人踪灭", "孤舟蓑笠翁", "独钓寒江雪"), 10},
                {"乐游原", "李商隐", "唐", List.of("向晚意不适", "驱车登古原", "夕阳无限好", "只是近黄昏"), 11},
                {"风", "李峤", "唐", List.of("解落三秋叶", "能开二月花", "过江千尺浪", "入竹万竿斜"), 12},
                {"画", "王维", "唐", List.of("远看山有色", "近听水无声", "春去花还在", "人来鸟不惊"), 13},
                {"赋得古原草送别", "白居易", "唐", List.of("离离原上草", "一岁一枯荣", "野火烧不尽", "春风吹又生"), 14},
                {"池上", "白居易", "唐", List.of("小娃撑小艇", "偷采白莲回", "不解藏踪迹", "浮萍一道开"), 15},
                {"小池", "杨万里", "宋", List.of("泉眼无声惜细流", "树阴照水爱晴柔", "小荷才露尖尖角", "早有蜻蜓立上头"), 16},
                {"所见", "袁枚", "清", List.of("牧童骑黄牛", "歌声振林樾", "意欲捕鸣蝉", "忽然闭口立"), 17},
                {"村居", "高鼎", "清", List.of("草长莺飞二月天", "拂堤杨柳醉春烟", "儿童散学归来早", "忙趁东风放纸鸢"), 18},
                {"咏柳", "贺知章", "唐", List.of("碧玉妆成一树高", "万条垂下绿丝绦", "不知细叶谁裁出", "二月春风似剪刀"), 19},
                {"回乡偶书", "贺知章", "唐", List.of("少小离家老大回", "乡音无改鬓毛衰", "儿童相见不相识", "笑问客从何处来"), 20},
        };
        int added = 0;
        for (Object[] r : rows) {
            String title = (String) r[0];
            if (exists.contains(title)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            List<String> lines = (List<String>) r[3];
            Poem p = new Poem();
            p.setTitle(title);
            p.setAuthor((String) r[1]);
            p.setDynasty((String) r[2]);
            p.setLines(lines);
            p.setLinesJson(JsonLines.stringify(lines));
            p.setSortOrder((Integer) r[4]);
            poemMapper.insert(p);
            exists.add(title);
            added++;
        }
        return added;
    }

    private int seedEnglish() {
        Set<String> exists = englishWordMapper.findAll().stream()
                .map(w -> w.getCategory() + ":" + w.getWord().toLowerCase())
                .collect(Collectors.toCollection(HashSet::new));
        int added = 0;

        String[] letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("");
        String[] phonetics = {"/eɪ/", "/biː/", "/siː/", "/diː/", "/iː/", "/ef/", "/dʒiː/", "/eɪtʃ/", "/aɪ/", "/dʒeɪ/",
                "/keɪ/", "/el/", "/em/", "/en/", "/əʊ/", "/piː/", "/kjuː/", "/ɑː/", "/es/", "/tiː/",
                "/juː/", "/viː/", "/ˈdʌbəljuː/", "/eks/", "/waɪ/", "/zed/"};
        String[] examples = {"Apple", "Ball", "Cat", "Dog", "Egg", "Fish", "Girl", "Hat", "Ice", "Juice",
                "Kite", "Lion", "Moon", "Nose", "Orange", "Pig", "Queen", "Rabbit", "Sun", "Tree",
                "Umbrella", "Violin", "Water", "Box", "Yo-yo", "Zebra"};
        String[] emojis = {"🍎", "⚽", "🐱", "🐶", "🥚", "🐟", "👧", "🎩", "🧊", "🧃",
                "🪁", "🦁", "🌙", "👃", "🍊", "🐷", "👸", "🐰", "☀️", "🌳",
                "☂️", "🎻", "💧", "📦", "🪀", "🦓"};
        for (int i = 0; i < letters.length; i++) {
            String key = "letter:" + letters[i].toLowerCase();
            if (exists.contains(key)) {
                continue;
            }
            EnglishWord w = new EnglishWord();
            w.setWord(letters[i]);
            w.setPhonetic(phonetics[i]);
            w.setMeaning("字母 " + letters[i]);
            w.setCategory("letter");
            w.setExample(examples[i] + " starts with " + letters[i]);
            w.setEmoji(emojis[i]);
            w.setSortOrder(i + 1);
            englishWordMapper.insert(w);
            exists.add(key);
            added++;
        }

        Object[][] words = {
                {"apple", "/ˈæpl/", "苹果", "I like apple.", "🍎", 101},
                {"banana", "/bəˈnɑːnə/", "香蕉", "A yellow banana.", "🍌", 102},
                {"cat", "/kæt/", "猫", "The cat is cute.", "🐱", 103},
                {"dog", "/dɒɡ/", "狗", "My dog runs fast.", "🐶", 104},
                {"egg", "/eɡ/", "鸡蛋", "An egg for breakfast.", "🥚", 105},
                {"fish", "/fɪʃ/", "鱼", "Fish swim in water.", "🐟", 106},
                {"sun", "/sʌn/", "太阳", "The sun is bright.", "☀️", 107},
                {"moon", "/muːn/", "月亮", "I see the moon.", "🌙", 108},
                {"star", "/stɑː/", "星星", "Twinkle little star.", "⭐", 109},
                {"book", "/bʊk/", "书", "I read a book.", "📚", 110},
                {"ball", "/bɔːl/", "球", "Kick the ball!", "⚽", 111},
                {"milk", "/mɪlk/", "牛奶", "Drink some milk.", "🥛", 112},
                {"water", "/ˈwɔːtə/", "水", "I drink water.", "💧", 113},
                {"bread", "/bred/", "面包", "I eat bread.", "🍞", 114},
                {"rice", "/raɪs/", "米饭", "Rice is yummy.", "🍚", 115},
                {"cake", "/keɪk/", "蛋糕", "Happy birthday cake!", "🎂", 116},
                {"bird", "/bɜːd/", "鸟", "A bird can fly.", "🐦", 117},
                {"duck", "/dʌk/", "鸭子", "The duck says quack.", "🦆", 118},
                {"frog", "/frɒɡ/", "青蛙", "A green frog.", "🐸", 119},
                {"bear", "/beə/", "熊", "A big bear.", "🐻", 120},
                {"pig", "/pɪɡ/", "猪", "A pink pig.", "🐷", 121},
                {"cow", "/kaʊ/", "牛", "The cow says moo.", "🐮", 122},
                {"horse", "/hɔːs/", "马", "I ride a horse.", "🐴", 123},
                {"rabbit", "/ˈræbɪt/", "兔子", "A cute rabbit.", "🐰", 124},
                {"mouse", "/maʊs/", "老鼠", "A little mouse.", "🐭", 125},
                {"car", "/kɑː/", "汽车", "A red car.", "🚗", 126},
                {"bus", "/bʌs/", "公交车", "I take the bus.", "🚌", 127},
                {"train", "/treɪn/", "火车", "The train is long.", "🚂", 128},
                {"plane", "/pleɪn/", "飞机", "A plane in the sky.", "✈️", 129},
                {"bike", "/baɪk/", "自行车", "I ride a bike.", "🚲", 130},
                {"red", "/red/", "红色", "An apple is red.", "🔴", 131},
                {"blue", "/bluː/", "蓝色", "The sky is blue.", "🔵", 132},
                {"green", "/ɡriːn/", "绿色", "The grass is green.", "🟢", 133},
                {"yellow", "/ˈjeləʊ/", "黄色", "The sun is yellow.", "🟡", 134},
                {"one", "/wʌn/", "一", "I have one toy.", "1️⃣", 135},
                {"two", "/tuː/", "二", "I see two birds.", "2️⃣", 136},
                {"three", "/θriː/", "三", "Three little pigs.", "3️⃣", 137},
                {"happy", "/ˈhæpi/", "开心的", "I am happy.", "😄", 138},
                {"hello", "/həˈləʊ/", "你好", "Hello, friend!", "👋", 139},
                {"bye", "/baɪ/", "再见", "Bye bye!", "👋", 140},
                {"thank you", "/θæŋk juː/", "谢谢", "Thank you, Mom!", "🙏", 141},
                {"family", "/ˈfæməli/", "家庭", "I love my family.", "👨‍👩‍👧‍👦", 142},
                {"friend", "/frend/", "朋友", "You are my friend.", "🤝", 143},
                {"school", "/skuːl/", "学校", "I go to school.", "🏫", 144},
                {"home", "/həʊm/", "家", "Home is warm.", "🏠", 145},
                {"flower", "/ˈflaʊə/", "花", "A pretty flower.", "🌸", 146},
                {"tree", "/triː/", "树", "A tall tree.", "🌳", 147},
                {"rain", "/reɪn/", "雨", "It is raining.", "🌧️", 148},
                {"snow", "/snəʊ/", "雪", "I like snow.", "❄️", 149},
                {"love", "/lʌv/", "爱", "I love you.", "❤️", 150},
        };
        for (Object[] r : words) {
            String word = (String) r[0];
            String key = "word:" + word.toLowerCase();
            if (exists.contains(key)) {
                continue;
            }
            EnglishWord w = new EnglishWord();
            w.setWord(word);
            w.setPhonetic((String) r[1]);
            w.setMeaning((String) r[2]);
            w.setCategory("word");
            w.setExample((String) r[3]);
            w.setEmoji((String) r[4]);
            w.setSortOrder((Integer) r[5]);
            englishWordMapper.insert(w);
            exists.add(key);
            added++;
        }
        return added;
    }
}
