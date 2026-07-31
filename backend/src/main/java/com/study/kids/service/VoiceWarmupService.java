package com.study.kids.service;

import com.study.kids.common.ChineseNumbers;
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
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 启动后后台预缓存：数字 1–100，以及题库中的汉字 / 英语词 / 诗句。
 * 在题库种子数据之后执行。
 */
@Slf4j
@Service
@Order(100)
@RequiredArgsConstructor
public class VoiceWarmupService implements ApplicationRunner {

    private final VoiceCacheService voiceCacheService;
    private final ChineseCharacterMapper characterMapper;
    private final EnglishWordMapper englishWordMapper;
    private final PoemMapper poemMapper;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean done = new AtomicBoolean(false);
    private final AtomicInteger cachedCount = new AtomicInteger(0);
    private final AtomicInteger failedCount = new AtomicInteger(0);

    @Override
    public void run(ApplicationArguments args) {
        Thread t = new Thread(this::warmupSafe, "voice-warmup");
        t.setDaemon(true);
        t.start();
    }

    private void warmupSafe() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        try {
            log.info("开始预缓存发音…");
            warmupNumbers();
            warmupCharacters();
            warmupEnglish();
            warmupPoems();
            done.set(true);
            log.info("发音预缓存完成：成功 {}，失败 {}", cachedCount.get(), failedCount.get());
        } catch (Exception e) {
            log.warn("发音预缓存异常: {}", e.getMessage());
        } finally {
            running.set(false);
        }
    }

    private void warmupNumbers() {
        for (int n = 1; n <= 100; n++) {
            cacheOne(ChineseNumbers.toChinese(n), "zh", 2);
            sleepQuiet(60);
        }
        log.info("数字 1–100 发音预缓存阶段完成");
    }

    private void warmupCharacters() {
        List<ChineseCharacter> list = characterMapper.findAll();
        for (ChineseCharacter c : list) {
            if (c.getCharText() != null && !c.getCharText().isBlank()) {
                cacheOne(c.getCharText(), "zh", 2);
                sleepQuiet(50);
            }
        }
    }

    private void warmupEnglish() {
        List<EnglishWord> list = englishWordMapper.findAll();
        for (EnglishWord w : list) {
            if (w.getWord() != null && !w.getWord().isBlank()) {
                cacheOne(w.getWord(), "en", 2);
                sleepQuiet(50);
            }
        }
    }

    private void warmupPoems() {
        List<Poem> poems = poemMapper.findAll();
        for (Poem poem : poems) {
            if (poem.getTitle() != null && !poem.getTitle().isBlank()) {
                cacheOne(poem.getTitle(), "zh", 2);
                sleepQuiet(50);
            }
            if (poem.getLinesJson() != null) {
                for (String line : JsonLines.parse(poem.getLinesJson())) {
                    if (line != null && !line.isBlank()) {
                        cacheOne(line, "zh", 2);
                        sleepQuiet(50);
                    }
                }
            }
        }
    }

    private void cacheOne(String text, String lang, int type) {
        try {
            if (voiceCacheService.isCached(text, lang, type)) {
                cachedCount.incrementAndGet();
                return;
            }
            voiceCacheService.resolveAudio(text, lang, type);
            cachedCount.incrementAndGet();
        } catch (Exception e) {
            failedCount.incrementAndGet();
            log.debug("缓存失败 [{}]: {}", text, e.getMessage());
        }
    }

    private void sleepQuiet(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public Map<String, Object> status() {
        Map<String, Object> map = new HashMap<>();
        map.put("running", running.get());
        map.put("done", done.get());
        map.put("cached", cachedCount.get());
        map.put("failed", failedCount.get());
        return map;
    }
}
