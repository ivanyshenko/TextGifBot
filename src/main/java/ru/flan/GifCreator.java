package ru.flan;

import org.telegram.telegrambots.meta.api.objects.InputFile;

import javax.imageio.stream.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GifCreator {

    /**
     * number of rows on gif
     *  0 < rows < 3
     */
    private int rows;

    /**
     * time between frames in millisec
     */
    private int frameDelay;

    /**
     * Background color
     */
    private Color backgroundColor;

    /**
     * Text color
     */
    private Color textColor;

    private static final Color OPAQUE = new Color(0,0,0, 0);
    private static final int BORDER = 3;
    private static final int GIF_NAME_LENGTH = 20;
    private static final int MIN_WIDTH = 100;

    public GifCreator(int rows, int frameDelay) {
        this.rows = rows;
        this.frameDelay = frameDelay;
        this.backgroundColor = Color.WHITE;
        this.textColor = Color.BLACK;
    }

    public GifCreator(int rows, int frameDelay, Color backgroundColor, Color textColor) {
        this.rows = rows;
        this.frameDelay = frameDelay;
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
    }

    public enum MODE {
        WORD_BY_WORD,
        TEST;
    }

    public InputFile makeGif(String text, MODE mode) {
        List<BufferedImage> gifFrames = makeImageFromText(text, mode);

        if (!gifFrames.isEmpty()) {
            ByteArrayOutputStream gifBytes = new ByteArrayOutputStream();


            try {
                ImageOutputStream output = new MemoryCacheImageOutputStream(gifBytes);
                GifSequenceWriter writer = new GifSequenceWriter(output, gifFrames.get(0).getType(), frameDelay, true);
                gifFrames.forEach(f -> {
                            try {
                                writer.writeToSequence(f);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                writer.close();
                output.flush();
                output.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            ByteArrayInputStream inputStream = new ByteArrayInputStream(gifBytes.toByteArray());

            return new InputFile(inputStream, trimFileName(text));
        }
        return null;
    }

    /**
     * gif name, that will be printed in telegram
     */
    private String trimFileName(String text) {
        boolean longer = text.length() - 1 > GIF_NAME_LENGTH;
        if (longer) {
            return text.substring(0, GIF_NAME_LENGTH) + ".gif";
        }
        return text + ".gif";
    }


    private List<BufferedImage> makeImageFromText(String text, MODE mode) {
        switch (mode) {
            case TEST:
                return testMode();

            case WORD_BY_WORD:
                return wordByWordMode(text);
        }

        return null;
    }

    private List<BufferedImage> testMode() {
        BufferedImage testImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D testG2d = testImg.createGraphics();

        Font font = new Font("Arial", Font.PLAIN, 28);
        testG2d.setFont(font);
        final FontMetrics fm = testG2d.getFontMetrics();

        int height = fm.getHeight();
        int width = fm.stringWidth("test");
        testG2d.dispose();

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        setMagicProperties(g2d);
        g2d.setFont(font);

        g2d.setFont(font);
        FontMetrics lastFm = g2d.getFontMetrics();

        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(textColor);
        g2d.drawString("test", 0, lastFm.getAscent());

        g2d.dispose();

        return Collections.singletonList(img);
    }

    private List<BufferedImage> wordByWordMode(String text) {
        BufferedImage testImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D testG2d = testImg.createGraphics();

        Font font = new Font("Arial", Font.PLAIN, 28);
        testG2d.setFont(font);
        final FontMetrics fm = testG2d.getFontMetrics();

        int rowHeight = fm.getHeight();
        int height = rowHeight * rows + BORDER * (rows - 1);

        List<String> words = Stream.of(text.split("\\s"))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
        int width = words.stream()
                .map(fm::stringWidth)
                .max(Integer::compareTo)
                .orElse(100) + 20;
        testG2d.dispose();

        List<BufferedImage> images = new ArrayList<>();
        boolean nextPage = true;
        int row = 0;
        int rowWidth;
        int wordsToRow = 0;
        int offset = 0;

        for (int w = 0; w < words.size(); w++) {
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = img.createGraphics();
            setMagicProperties(g2d);
            g2d.setFont(font);

            String currentWord = words.get(w);

            if (nextPage) {
                nextPage = false;
                row = 0;
                g2d.setColor(backgroundColor);
                g2d.fillRect(0, 0, width, height);
            }

            g2d.setColor(textColor);

            //new row
            if (wordsToRow == 0) {
                wordsToRow++;
                StringBuilder preparedRow = new StringBuilder(currentWord);
                rowWidth = fm.stringWidth(currentWord);
                for (int seq = w+1; seq < words.size(); seq++) {
                    preparedRow.append(" ").append(words.get(seq));
                    int longerRowWidth = fm.stringWidth(preparedRow.toString());
                    if (longerRowWidth < width) {
                        wordsToRow++;
                        rowWidth = longerRowWidth;
                    }
                }
                //centrilize row
                offset = (width - rowWidth) >>> 1;
            }

            if (wordsToRow != 1) currentWord = currentWord + " ";

            g2d.drawString(currentWord, offset, fm.getAscent() + row * (fm.getAscent() + BORDER));
            g2d.dispose();

            offset += fm.stringWidth(currentWord);
            wordsToRow--;
            if (wordsToRow == 0) {
                row++;
                offset = 0;
                if (row == rows) {
                    nextPage = true;
                }
            }

            images.add(img);
        }

        return images;
    }

    private void setMagicProperties(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

}
