package com.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author :infinite-war
 * @date : 2022/11/10 15:01
 * @desc :
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Content {
    private String img;
    private String price;
    private String title;
}