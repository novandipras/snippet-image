package local.snippetimage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

final class SnapshotRenderer {
    record Span(String text, Color color, int style) {}

    static BufferedImage render(List<Span> spans, Font font, Color background, Color foreground,
                                int firstLine, int tabSize, boolean numbers, boolean transparent, int scale) {
        BufferedImage measure = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D metrics = measure.createGraphics();
        int[] bounds;
        int lineHeight;
        int ascent;
        int gutter;
        try {
            configure(metrics);
            metrics.setFont(font);
            lineHeight = metrics.getFontMetrics().getHeight();
            ascent = metrics.getFontMetrics().getAscent();
            int lines = 1;
            for (Span span : spans) lines += (int) span.text().chars().filter(c -> c == '\n').count();
            gutter = numbers ? metrics.getFontMetrics().stringWidth(Integer.toString(firstLine + lines - 1)) + 20 : 0;
            bounds = paintCode(metrics, spans, font, foreground, firstLine, tabSize, gutter, lineHeight, ascent, false);
        } finally {
            metrics.dispose();
        }
        long width = (long) bounds[0] + gutter + 64;
        long height = (long) bounds[1] * lineHeight + 64;
        if (width * scale > 8192 || height * scale > 8192 || width * height * scale * scale > 16_000_000) {
            throw new IllegalArgumentException("Image is too large. Select less code or use 1x scale.");
        }
        BufferedImage image = new BufferedImage((int) width * scale, (int) height * scale, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            configure(graphics);
            graphics.scale(scale, scale);
            if (!transparent) {
                graphics.setColor(background.darker());
                graphics.fillRect(0, 0, (int) width, (int) height);
            }
            graphics.setColor(background);
            graphics.fillRoundRect(16, 16, (int) width - 32, (int) height - 32, 16, 16);
            graphics.translate(32, 32);
            paintCode(graphics, spans, font, foreground, firstLine, tabSize, gutter, lineHeight, ascent, true);
        } finally {
            graphics.dispose();
        }
        return image;
    }

    private static int[] paintCode(Graphics2D graphics, List<Span> spans, Font font, Color foreground,
                                   int firstLine, int tabSize, int gutter, int lineHeight, int ascent, boolean draw) {
        int x = 0;
        int line = 0;
        int maxWidth = 0;
        graphics.setFont(font);
        int tabWidth = Math.max(1, tabSize) * Math.max(1, graphics.getFontMetrics().charWidth(' '));
        if (draw && gutter > 0) drawNumber(graphics, font, foreground, firstLine, gutter, ascent);
        for (Span span : spans) {
            graphics.setFont(font.deriveFont(span.style()));
            graphics.setColor(span.color() == null ? foreground : span.color());
            String text = span.text();
            int start = 0;
            for (int i = 0; i <= text.length(); i++) {
                if (i < text.length() && text.charAt(i) != '\n' && text.charAt(i) != '\t') continue;
                String part = text.substring(start, i);
                if (draw) graphics.drawString(part, gutter + x, line * lineHeight + ascent);
                x += graphics.getFontMetrics().stringWidth(part);
                maxWidth = Math.max(maxWidth, x);
                if (i < text.length()) {
                    if (text.charAt(i) == '\t') {
                        x = (x / tabWidth + 1) * tabWidth;
                        maxWidth = Math.max(maxWidth, x);
                    } else {
                        x = 0;
                        line++;
                        if (draw && gutter > 0) {
                            drawNumber(graphics, font, foreground, firstLine + line, gutter, line * lineHeight + ascent);
                            graphics.setFont(font.deriveFont(span.style()));
                            graphics.setColor(span.color() == null ? foreground : span.color());
                        }
                    }
                }
                start = i + 1;
            }
        }
        return new int[]{Math.max(1, maxWidth), line + 1};
    }

    private static void drawNumber(Graphics2D graphics, Font font, Color color, int line, int gutter, int y) {
        graphics.setFont(font);
        graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));
        String number = Integer.toString(line);
        graphics.drawString(number, gutter - 20 - graphics.getFontMetrics().stringWidth(number), y);
    }

    private static void configure(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }
}
