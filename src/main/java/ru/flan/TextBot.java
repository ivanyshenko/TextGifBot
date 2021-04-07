package ru.flan;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class TextBot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return "rodion_checker_bot";
    }

    @Override
    public String getBotToken() {
        return "1745967559:AAHEviu0Y5nT6_Hf6YoaKdRqo4EBFwrAblw";
    }

    @Override
    public void onUpdateReceived(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            // Set variables
            try {
                System.setOut(new PrintStream(System.out, true, "UTF-8"));

                String messageText = update.getMessage().getText();
                String chatId = update.getMessage().getChatId().toString();
                String filename = UUID.randomUUID().toString() + ".gif";
                GifCreator.makeGif(messageText, filename);
                execute(new SendAnimation(chatId, new InputFile(new File(filename))));
//                String user = update.getMessage().getAuthorSignature();
//                System.out.println(user + ": " + messageText);
//
//                BotApiMethod message = getMessage(messageText, chatId);
//                execute(message); // Sending our message object to user
//                System.out.println(((SendMessage)message).getText());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private BotApiMethod getMessage(String msg, String chatId) throws Exception {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);

        String s = new String("да".getBytes(), StandardCharsets.UTF_8);
        if (URLEncoder.encode(msg, "UTF-16").toLowerCase().contains(s)) {
            message.setText(URLEncoder.encode("Хуй на!", "UTF-16"));
        } else if (msg.toLowerCase().contains("no")) {
            message.setText("pidora otver");
        } else message.setText("Rodion, are u ready to rock-n-roll?");

        return message;
    }


}
