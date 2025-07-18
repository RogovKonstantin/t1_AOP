package com.bishop.synthetic_human.controller;

import com.bishop.synthetic_human.service.CommandService;
import com.bishop.synthetic_human.dto.CommandRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/commands")
@Validated
public class CommandController {
    private final CommandService commandService;

    public CommandController(CommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping
    public ResponseEntity<Void> submit(@Valid @RequestBody CommandRequest req) {
        commandService.submit(req);
        return ResponseEntity.accepted().build();
    }
}

