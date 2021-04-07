package ru.flan;

import org.telegram.telegrambots.meta.api.objects.InputFile;

import javax.imageio.ImageIO;
import javax.imageio.stream.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GifCreator {
    public static void makeGif(String text,  String filename) throws IOException {
        List<BufferedImage> gifFrames =makeImageFromWord(text);

        if (!gifFrames.isEmpty()) {
            try {
                ImageOutputStream output =
                        new FileImageOutputStream(new File(filename));
                GifSequenceWriter writer = new GifSequenceWriter(output, gifFrames.get(0).getType(), 200, true);
                gifFrames.forEach(f -> {
                            try {
                                writer.writeToSequence(f);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

//    public static void main(String[] args) throws IOException {
//        String word = new Scanner(System.in).nextLine();
//        makeGif(word);
//    }

    public static List<BufferedImage> makeImageFromWord(String text) {
        BufferedImage testImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D testG2d = testImg.createGraphics();


        //шрифт
        Font font = new Font("Arial", Font.PLAIN, 36);
        testG2d.setFont(font);
        final FontMetrics fm = testG2d.getFontMetrics();

        int height = fm.getHeight();
        List<String> words = Stream.of(text.split(" "))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());

        int width = words.stream()
                .map(fm::stringWidth)
                .max(Integer::compareTo)
                .orElse(16);
        testG2d.dispose();

        return words.stream().map(
                word -> {
                    BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2d = img.createGraphics();
                    setUrMama(g2d);

                    g2d.setFont(font);
                    FontMetrics lastFm = g2d.getFontMetrics();

                    g2d.setColor(Color.WHITE);
                    g2d.fillRect(0, 0, width, height);
                    g2d.setColor(Color.BLACK);
                    g2d.drawString(word, 0, lastFm.getAscent());

                    g2d.dispose();
                    return img;
                }
        ).collect(Collectors.toList());
    }

    private static void setUrMama(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }


}
