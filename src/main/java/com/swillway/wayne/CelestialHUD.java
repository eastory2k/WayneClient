package com.swillway.wayne;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import java.text.SimpleDateFormat;
import java.util.*;

public class CelestialHUD {
    
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    
    // Цвета Celestial
    private static final int BG_MAIN = 0xCC0D0D0D;
    private static final int BG_PANEL = 0xDD141414;
    private static final int BORDER = 0xFF2A2A2A;
    private static final int ACCENT = 0xFF6BB5FF;      // Голубой акцент Celestial
    private static final int ACCENT_DARK = 0xFF4A90D9;
    private static final int TEXT_WHITE = 0xFFFFFFFF;
    private static final int TEXT_GRAY = 0xFFAAAAAA;
    private static final int TEXT_DARK = 0xFF666666;
    private static final int GREEN = 0xFF55FF55;
    private static final int RED = 0xFFFF5555;
    private static final int YELLOW = 0xFFFFFF55;
    private static final int ORANGE = 0xFFFFAA00;
    private static final int FRIEND = 0xFF55AAFF;
    
    private float smoothFPS = 0;
    private long lastFrameTime = System.currentTimeMillis();
    private final List<Float> fpsHistory = new ArrayList<>();
    
    public void render(DrawContext ctx, float tickDelta) {
        if (mc.player == null || mc.world == null) return;
        if (mc.options.hudHidden) return;
        
        TextRenderer font = mc.textRenderer;
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        
        // Обновление FPS
        updateFPS();
        
        // Рендер всех элементов
        renderModuleList(ctx, font, 5, 5);
        renderInfoPanel(ctx, font, sw - 155, 5);
        renderPlayerList(ctx, font, sw - 155, 90);
        renderStatusBar(ctx, font, sw, sh);
        renderWatermark(ctx, font, sw, sh);
    }
    
    // ═══════════════════════════════════════════
    // ЛЕВАЯ ПАНЕЛЬ — СПИСОК МОДУЛЕЙ
    // ═══════════════════════════════════════════
    
    private void renderModuleList(DrawContext ctx, TextRenderer font, int x, int y) {
        List<Module> modules = WayneClient.INSTANCE.modules.all();
        int panelW = 115;
        int panelH = 55 + modules.size() * 13;
        
        // Тень
        fill(ctx, x + 2, y + 2, panelW, panelH, 0x60000000);
        
        // Фон с градиентом
        fillGradient(ctx, x, y, panelW, panelH, BG_PANEL, BG_MAIN);
        
        // Акцентная линия сверху
        fill(ctx, x, y, panelW, 2, ACCENT);
        
        // Обводка
        border(ctx, x, y, panelW, panelH, BORDER);
        
        int ty = y + 10;
        
        // Заголовок
        drawCenteredString(ctx, font, "§lWAYNE DLC", x + panelW / 2, ty, ACCENT);
        ty += 12;
        drawCenteredString(ctx, font, "v1.0.0", x + panelW / 2, ty, TEXT_DARK);
        ty += 10;
        
        // Разделитель
        fill(ctx, x + 10, ty, panelW - 20, 1, BORDER);
        ty += 6;
        
        // Модули
        for (Module m : modules) {
            boolean on = m.on;
            int color = on ? GREEN : RED;
            String dot = on ? " ●" : " ○";
            String keyText = m.key > 0 ? " [" + getKeyName(m.key) + "]" : "";
            
            // Точка-индикатор
            drawString(ctx, font, dot, x + 10, ty, color);
            
            // Название
            drawString(ctx, font, m.name, x + 22, ty, on ? TEXT_WHITE : TEXT_GRAY);
            
            // Клавиша
            if (!keyText.isEmpty()) {
                drawString(ctx, font, keyText, x + panelW - font.getWidth(keyText) - 10, ty, TEXT_DARK);
            }
            
            ty += 13;
        }
        
        // Нижний разделитель
        ty += 2;
        fill(ctx, x + 10, ty, panelW - 20, 1, BORDER);
        ty += 5;
        
        // Счётчик модулей
        int enabled = WayneClient.INSTANCE.modules.enabled();
        String counter = enabled + "/" + modules.size() + " modules";
        drawCenteredString(ctx, font, counter, x + panelW / 2, ty, TEXT_DARK);
    }
    
    // ═══════════════════════════════════════════
    // ПРАВАЯ ВЕРХНЯЯ — ИНФО ПАНЕЛЬ
    // ═══════════════════════════════════════════
    
    private void renderInfoPanel(DrawContext ctx, TextRenderer font, int x, int y) {
        int panelW = 150;
        int panelH = 80;
        
        // Фон и обводка
        fillGradient(ctx, x, y, panelW, panelH, BG_PANEL, BG_MAIN);
        border(ctx, x, y, panelW, panelH, BORDER);
        
        int ty = y + 7;
        int leftX = x + 8;
        int rightX = x + panelW - 8;
        
        // FPS с цветовой индикацией
        int fps = (int) smoothFPS;
        int fpsColor = fps >= 60 ? GREEN : (fps >= 30 ? YELLOW : RED);
        drawString(ctx, font, "FPS", leftX, ty, TEXT_GRAY);
        drawStringRight(ctx, font, String.valueOf(fps), rightX, ty, fpsColor);
        ty += 14;
        
        // Пинг
        int ping = getPing();
        int pingColor = ping < 50 ? GREEN : (ping < 100 ? YELLOW : RED);
        drawString(ctx, font, "Ping", leftX, ty, TEXT_GRAY);
        drawStringRight(ctx, font, ping + "ms", rightX, ty, pingColor);
        ty += 14;
        
        // Сервер
        String server = getServerIP();
        drawString(ctx, font, "Server", leftX, ty, TEXT_GRAY);
        drawStringRight(ctx, font, server.length() > 18 ? server.substring(0, 15) + "..." : server, rightX, ty, TEXT_WHITE);
        ty += 14;
        
        // Время
        drawString(ctx, font, "Time", leftX, ty, TEXT_GRAY);
        drawStringRight(ctx, font, timeFormat.format(new Date()), rightX, ty, ACCENT);
        ty += 12;
        
        // Дата
        drawCenteredString(ctx, font, dateFormat.format(new Date()), x + panelW / 2, ty, TEXT_DARK);
    }
    
    // ═══════════════════════════════════════════
    // ПРАВАЯ — СПИСОК ИГРОКОВ
    // ═══════════════════════════════════════════
    
    private void renderPlayerList(DrawContext ctx, TextRenderer font, int x, int y) {
        List<PlayerEntity> players = new ArrayList<>();
        for (var e : mc.world.getEntities()) {
            if (e instanceof PlayerEntity p && p != mc.player) players.add(p);
        }
        
        if (players.isEmpty()) return;
        
        players.sort((a, b) -> Double.compare(mc.player.distanceTo(a), mc.player.distanceTo(b)));
        
        int panelW = 150;
        int maxPlayers = Math.min(players.size(), 8);
        int panelH = 30 + maxPlayers * 20;
        
        // Фон и обводка
        fillGradient(ctx, x, y, panelW, panelH, BG_PANEL, BG_MAIN);
        border(ctx, x, y, panelW, panelH, BORDER);
        
        int ty = y + 7;
        
        // Заголовок
        drawCenteredString(ctx, font, "§lPLAYERS", x + panelW / 2, ty, ACCENT);
        ty += 12;
        
        // Разделитель
        fill(ctx, x + 8, ty, panelW - 16, 1, BORDER);
        ty += 5;
        
        for (int i = 0; i < maxPlayers; i++) {
            PlayerEntity pl = players.get(i);
            float hp = (pl.getHealth() + pl.getAbsorptionAmount()) / pl.getMaxHealth();
            
            int nameColor;
            if (isFriend(pl)) nameColor = FRIEND;
            else if (hp > 0.6f) nameColor = GREEN;
            else if (hp > 0.3f) nameColor = YELLOW;
            else nameColor = RED;
            
            // Имя
            String name = pl.getName().getString();
            if (name.length() > 14) name = name.substring(0, 11) + "...";
            drawString(ctx, font, name, x + 8, ty, nameColor);
            
            // HP справа
            String hpText = (int)(hp * 100) + "%";
            drawStringRight(ctx, font, hpText, x + panelW - 8, ty, nameColor);
            
            // Полоса здоровья
            int barY = ty + 10;
            int barW = panelW - 20;
            int barH = 3;
            
            // Фон полосы
            fill(ctx, x + 10, barY, barW, barH, 0xFF1A1A1A);
            
            // Градиент на полосе
            int fillW = (int)(barW * hp);
            if (fillW > 0) {
                fillGradient(ctx, x + 10, barY, fillW, barH, 
                    hp > 0.5f ? GREEN : (hp > 0.25f ? YELLOW : RED),
                    hp > 0.5f ? 0xFF33AA33 : (hp > 0.25f ? ORANGE : 0xFFCC0000));
            }
            
            ty += 18;
        }
    }
    
    // ═══════════════════════════════════════════
    // НИЖНИЙ СТАТУС-БАР
    // ═══════════════════════════════════════════
    
    private void renderStatusBar(DrawContext ctx, TextRenderer font, int sw, int sh) {
        int barH = 16;
        int y = sh - barH;
        
        // Фон
        fill(ctx, 0, y, sw, barH, 0xEE0A0A0A);
        
        // Акцентная линия сверху
        fillGradient(ctx, 0, y, sw, 2, ACCENT, ACCENT_DARK);
        
        int ty = y + 4;
        
        // Левая часть
        String left = "Wayne DLC  |  " + (int)smoothFPS + " FPS  |  " + getPing() + "ms";
        drawString(ctx, font, left, 8, ty, TEXT_WHITE);
        
        // Центр
        String center = getServerIP();
        drawCenteredString(ctx, font, center, sw / 2, ty, TEXT_GRAY);
        
        // Правая часть
        String right = timeFormat.format(new Date());
        drawStringRight(ctx, font, right, sw - 8, ty, ACCENT);
    }
    
    // ═══════════════════════════════════════════
    // ВОДЯНОЙ ЗНАК
    // ═══════════════════════════════════════════
    
    private void renderWatermark(DrawContext ctx, TextRenderer font, int sw, int sh) {
        String text = "WAYNE";
        drawStringRight(ctx, font, text, sw - 5, sh - 20, 0x15FFFFFF);
    }
    
    // ═══════════════════════════════════════════
    // УТИЛИТЫ ОТРИСОВКИ
    // ═══════════════════════════════════════════
    
    private void fill(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + h, color);
    }
    
    private void fillGradient(DrawContext ctx, int x, int y, int w, int h, int top, int bottom) {
        ctx.fillGradient(x, y, x + w, y + h, top, bottom);
    }
    
    private void border(DrawContext ctx, int x, int y, int w, int h, int color) {
        fill(ctx, x, y, w, 1, color);
        fill(ctx, x, y + h - 1, w, 1, color);
        fill(ctx, x, y, 1, h, color);
        fill(ctx, x + w - 1, y, 1, h, color);
    }
    
    private void drawString(DrawContext ctx, TextRenderer font, String text, int x, int y, int color) {
        ctx.drawTextWithShadow(font, text, x, y, color);
    }
    
    private void drawStringRight(DrawContext ctx, TextRenderer font, String text, int x, int y, int color) {
        ctx.drawTextWithShadow(font, text, x - font.getWidth(text), y, color);
    }
    
    private void drawCenteredString(DrawContext ctx, TextRenderer font, String text, int x, int y, int color) {
        ctx.drawTextWithShadow(font, text, x - font.getWidth(text) / 2, y, color);
    }
    
    // ═══════════════════════════════════════════
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ═══════════════════════════════════════════
    
    private void updateFPS() {
        long now = System.currentTimeMillis();
        long diff = now - lastFrameTime;
        lastFrameTime = now;
        
        if (diff > 0) {
            float fps = 1000f / diff;
            fpsHistory.add(fps);
            if (fpsHistory.size() > 20) fpsHistory.remove(0);
            
            float sum = 0;
            for (float f : fpsHistory) sum += f;
            smoothFPS = sum / fpsHistory.size();
        }
    }
    
    private int getPing() {
        if (mc.getNetworkHandler() != null && mc.player != null) {
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (entry != null) return entry.getLatency();
        }
        return 0;
    }
    
    private String getServerIP() {
        if (mc.getCurrentServerEntry() != null) {
            return mc.getCurrentServerEntry().address;
        }
        return "Singleplayer";
    }
    
    private String getKeyName(int keyCode) {
        if (keyCode >= 290 && keyCode <= 301) return "F" + (keyCode - 289);
        return "?";
    }
    
    private boolean isFriend(PlayerEntity player) {
        return player.getName().getString().startsWith("[F]");
    }
}
