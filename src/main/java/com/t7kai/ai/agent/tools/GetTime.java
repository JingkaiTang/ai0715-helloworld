package com.t7kai.ai.agent.tools;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.time.LocalDateTime;

@JsonClassDescription("""
        获取当前时间。
        """)
public class GetTime implements Tool {

    @JsonPropertyDescription("参数可忽略")
    public String a;

    @Override
    public String execute() {
        return LocalDateTime.now().toString();
    }

}
