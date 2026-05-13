package com.minicompiler.service;

import com.minicompiler.compiler.lexer.Lexer;
import com.minicompiler.compiler.lexer.Token;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Wraps the {@link Lexer} for use within the Spring service layer,
 * keeping compiler instantiation out of the controller.
 */
@Service
public class TokenizerService {

    /**
     * Tokenizes the given source code and returns the result.
     *
     * @param  sourceCode the raw source code to tokenize
     * @return a {@link TokenizationResult} with the token list and count
     */
    public TokenizationResult tokenize(String sourceCode) {
        var tokens = new Lexer(sourceCode).tokenize();
        return new TokenizationResult(tokens, tokens.size());
    }

    // =========================================================================
    // Result record
    // =========================================================================

    /**
     * Immutable value object carrying the tokenization output.
     *
     * @param tokens list of tokens produced by the lexer
     * @param count  total number of tokens
     */
    public record TokenizationResult(List<Token> tokens, int count) {}
}