package com.buaa.academic.account.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MessageStatistic {
    Integer count;
    Integer unreadCount;
}
