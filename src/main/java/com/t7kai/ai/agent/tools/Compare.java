package com.t7kai.ai.agent.tools;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@JsonClassDescription("""
        比较两个浮点数的大小。
        :param a: 第一个浮点数。
        :param b: 第二个浮点数。
        :return: 比较结果的字符串表示。
        """)
public class Compare implements Tool {
    @JsonPropertyDescription("第一个浮点数。")
    public double a;

    @JsonPropertyDescription("第二个浮点数。")
    public double b;

    @Override
    public String execute() {
        if (a > b) {
            return String.format("%.2f is greater than %.2f", a, b);
        } else if (a < b) {
            return String.format("%.2f is less than %.2f", a, b);
        } else {
            return String.format("%.2f is equal to %.2f", a, b);
        }
    }
}
