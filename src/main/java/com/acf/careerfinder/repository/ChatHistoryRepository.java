package com.acf.careerfinder.repository;

import com.acf.careerfinder.model.ChatHistory;
import com.acf.careerfinder.model.UserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory,Integer> {
    List<ChatHistory> findByEmailOrderByDateAsc(String email);

    List<ChatHistory> findByChatIdOrderByDateAsc(int chatId);

    int deleteByChatId(int chatId);

    @Query("""
       select distinct c.chatId
       from ChatHistory c
       where c.email = :email
       order by c.chatId
       """)
    List<Integer> findAllChatIdsByEmail(@Param("email") String email);



}
