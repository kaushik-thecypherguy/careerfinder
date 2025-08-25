package com.acf.careerfinder.service;

import com.acf.careerfinder.model.ChatHistory;
import com.acf.careerfinder.model.UserData;
import com.acf.careerfinder.repository.ChatHistoryRepository;
import com.acf.careerfinder.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.Optional;

@Service
public class ChatHistoryService {

    @Autowired
    ChatHistoryRepository repository;

    public ChatHistory save(ChatHistory chatHistory) {
        return repository.save(chatHistory);   // one-liner wrapper
    }

    /* ✱ NEW ✱ ---------------------------------------------------------------- */
    public List<ChatHistory> getHistory(String email) {
        return repository.findByEmailOrderByDateAsc(email);
    }

    public List<ChatHistory> getHistoryByChatId(int chatId) {
        return repository.findByChatIdOrderByDateAsc(chatId);
    }

    @Transactional          // omit if you kept it on the repo
    public int deleteChatByChatId(int chatId) {
        return repository.deleteByChatId(chatId);
    }

    public List<Integer> getChatIdsForEmail(String email) {
        return repository.findAllChatIdsByEmail(email);
    }


}
