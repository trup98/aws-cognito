package com.learning.cognito.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GetSortBy {

    ID("id"),
    USER_NAME("user_name"),
    EMAIL("email"),
    USER_GROUP_NAME("userGroupName");

    private final String value;


}
