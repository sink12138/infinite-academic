package com.buaa.academic.account.model;

import com.buaa.academic.document.system.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MessagePage {
    List<Message> messages;
    Integer pageCount;
}
