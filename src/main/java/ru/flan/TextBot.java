package ru.flan;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.*;

public class TextBot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return "rodion_checker_bot";
    }

    @Override
    public String getBotToken() {
        return "1745967559:AAHEviu0Y5nT6_Hf6YoaKdRqo4EBFwrAblw";
    }

    private static final int MAX_LENGTH = 1000;

    @Override
    public void onUpdateReceived(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            // Set variables
            try {
                System.setOut(new PrintStream(System.out, true, "UTF-8"));

                GifCreator creator = new GifCreator(2, 500);

                String message = update.getMessage().getText();

                String chatId = update.getMessage().getChatId().toString();

                String textToGif = message.length() > MAX_LENGTH ? message.substring(0, MAX_LENGTH-1) : message;

                execute(new SendAnimation(chatId, creator.makeGif(textToGif, GifCreator.MODE.WORD_BY_WORD)));

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}
