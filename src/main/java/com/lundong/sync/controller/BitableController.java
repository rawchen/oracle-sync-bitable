package com.lundong.sync.controller;

import com.lundong.sync.service.BitableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author shuangquan.chen
 * @date 2023-12-11 11:13
 */
@Slf4j
@RestController
@RequestMapping
public class BitableController {

    @Autowired
    BitableService bitableService;

    @RequestMapping(value = "/sync")
    public void sync() throws Exception {
        bitableService.sync();
    }
}
