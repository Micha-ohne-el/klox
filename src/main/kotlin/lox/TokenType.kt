package lox

enum class TokenType {
    // One-character tokens:
    LeftParen, RightParen,
    LeftBrace, RightBrace,
    Dot, Comma,
    Plus, Minus,
    Semicolon,
    Slash,
    Asterisk,

    // One- or two-character tokens:
    Bang, BangEqual,
    Equal, EqualEqual,
    Less, LessEqual,
    Greater, GreaterEqual,

    // Literals:
    Identifier,
    String,
    Number,

    // Keywords:
    Nil,
    True, False,
    And, Or,
    If, Else,
    For, While,
    Return,
    Var, Fun, Class,
    This, Super,
    Print,

    // Specials:
    EndOfFile
}
