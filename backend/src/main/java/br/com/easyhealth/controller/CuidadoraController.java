package br.com.easyhealth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/cuidadoras")
public class CuidadoraController {

    @GetMapping("/link-busca")
    public ResponseEntity obterLinkLinkedIn(
            @RequestParam(required = false) String cidade) {
        
        String termo = (cidade != null && !cidade.isBlank())
                ? "cuidadora de idosos " + cidade
                : "cuidadora de idosos";

        String termoCodificado = URLEncoder.encode(termo, StandardCharsets.UTF_8);
        String url = "https://www.linkedin.com/search/results/people/?keywords=" + termoCodificado;

        return ResponseEntity.ok(Map.of("url", url));
    }
}

