/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
#import "JNIUtil.h"
#import <JavaNativeFoundation/JavaNativeFoundation.h> // Helper framework for Cocoa and JNI development.

/*
 * Version $Id: JNIUtil.m 1171 2013-02-11 21:45:02Z dcollins $
 */

/* Dimension class, member, and method info global variables. The first parameter specifies the global variable name. */
static JNF_CLASS_CACHE(Dimension_class, "java/awt/Dimension");
static JNF_CTOR_CACHE(Dimension_init, Dimension_class, "(II)V");
static JNF_MEMBER_CACHE(Dimension_getWidth, Dimension_class, "getWidth", "()D");
static JNF_MEMBER_CACHE(Dimension_getHeight, Dimension_class, "getHeight", "()D");

/* AWT generic input event methods and constants. The first parameter specifies the global variable name. */
static JNF_CLASS_CACHE(InputEvent, "java/awt/event/InputEvent");
static JNF_MEMBER_CACHE(InputEvent_getID, InputEvent, "getID", "()I");
static JNF_MEMBER_CACHE(InputEvent_isShiftDown, InputEvent, "isShiftDown", "()Z");

/* AWT key event methods and constants. The first parameter specifies the global variable name. */
static JNF_CLASS_CACHE(KeyEvent, "java/awt/event/KeyEvent");
static JNF_MEMBER_CACHE(KeyEvent_getKeyCode, KeyEvent, "getKeyCode", "()I");
static JNF_MEMBER_CACHE(KeyEvent_getKeyLocation, KeyEvent, "getKeyLocation", "()I");
static JNF_STATIC_MEMBER_CACHE(AWT_KEY_TYPED, KeyEvent, "KEY_TYPED", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_KEY_PRESSED, KeyEvent, "KEY_PRESSED", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_KEY_RELEASED, KeyEvent, "KEY_RELEASED", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_KEY_LOCATION_UNKNOWN, KeyEvent, "KEY_LOCATION_UNKNOWN", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_KEY_LOCATION_STANDARD, KeyEvent, "KEY_LOCATION_STANDARD", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_KEY_LOCATION_LEFT, KeyEvent, "KEY_LOCATION_LEFT", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_KEY_LOCATION_RIGHT, KeyEvent, "KEY_LOCATION_RIGHT", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_KEY_LOCATION_NUMPAD, KeyEvent, "KEY_LOCATION_NUMPAD", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_CHAR_UNDEFINED, KeyEvent, "CHAR_UNDEFINED", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_ENTER, KeyEvent, "VK_ENTER", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_BACK_SPACE, KeyEvent, "VK_BACK_SPACE", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_TAB, KeyEvent, "VK_TAB", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_CANCEL, KeyEvent, "VK_CANCEL", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_CLEAR, KeyEvent, "VK_CLEAR", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_SHIFT, KeyEvent, "VK_SHIFT", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_CONTROL, KeyEvent, "VK_CONTROL", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_ALT, KeyEvent, "VK_ALT", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_PAUSE, KeyEvent, "VK_PAUSE", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_CAPS_LOCK, KeyEvent, "VK_CAPS_LOCK", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_ESCAPE, KeyEvent, "VK_ESCAPE", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_SPACE, KeyEvent, "VK_SPACE", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_PAGE_UP, KeyEvent, "VK_PAGE_UP", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_PAGE_DOWN, KeyEvent, "VK_PAGE_DOWN", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_END, KeyEvent, "VK_END", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_HOME, KeyEvent, "VK_HOME", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_LEFT, KeyEvent, "VK_LEFT", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_UP, KeyEvent, "VK_UP", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_RIGHT, KeyEvent, "VK_RIGHT", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_DOWN, KeyEvent, "VK_DOWN", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_COMMA, KeyEvent, "VK_COMMA", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_MINUS, KeyEvent, "VK_MINUS", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_PERIOD, KeyEvent, "VK_PERIOD", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_SLASH, KeyEvent, "VK_SLASH", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_0, KeyEvent, "VK_0", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_1, KeyEvent, "VK_1", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_2, KeyEvent, "VK_2", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_3, KeyEvent, "VK_3", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_4, KeyEvent, "VK_4", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_5, KeyEvent, "VK_5", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_6, KeyEvent, "VK_6", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_7, KeyEvent, "VK_7", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_8, KeyEvent, "VK_8", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_9, KeyEvent, "VK_9", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_SEMICOLON, KeyEvent, "VK_SEMICOLON", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_EQUALS, KeyEvent, "VK_EQUALS", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_A, KeyEvent, "VK_A", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_B, KeyEvent, "VK_B", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_C, KeyEvent, "VK_C", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_D, KeyEvent, "VK_D", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_E, KeyEvent, "VK_E", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_F, KeyEvent, "VK_F", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_G, KeyEvent, "VK_G", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_H, KeyEvent, "VK_H", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_I, KeyEvent, "VK_I", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_J, KeyEvent, "VK_J", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_K, KeyEvent, "VK_K", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_L, KeyEvent, "VK_L", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_M, KeyEvent, "VK_M", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_N, KeyEvent, "VK_N", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_O, KeyEvent, "VK_O", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_P, KeyEvent, "VK_P", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_Q, KeyEvent, "VK_Q", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_R, KeyEvent, "VK_R", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_S, KeyEvent, "VK_S", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_T, KeyEvent, "VK_T", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_U, KeyEvent, "VK_U", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_V, KeyEvent, "VK_V", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_W, KeyEvent, "VK_W", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_X, KeyEvent, "VK_X", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_Y, KeyEvent, "VK_Y", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_Z, KeyEvent, "VK_Z", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_OPEN_BRACKET, KeyEvent, "VK_OPEN_BRACKET", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_BACK_SLASH, KeyEvent, "VK_BACK_SLASH", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_CLOSE_BRACKET, KeyEvent, "VK_CLOSE_BRACKET", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_NUMPAD0, KeyEvent, "VK_NUMPAD0", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_NUMPAD1, KeyEvent, "VK_NUMPAD1", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_NUMPAD2, KeyEvent, "VK_NUMPAD2", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_NUMPAD3, KeyEvent, "VK_NUMPAD3", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_NUMPAD4, KeyEvent, "VK_NUMPAD4", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_NUMPAD5, KeyEvent, "VK_NUMPAD5", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_NUMPAD6, KeyEvent, "VK_NUMPAD6", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_NUMPAD7, KeyEvent, "VK_NUMPAD7", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_NUMPAD8, KeyEvent, "VK_NUMPAD8", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_NUMPAD9, KeyEvent, "VK_NUMPAD9", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_MULTIPLY, KeyEvent, "VK_MULTIPLY", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_ADD, KeyEvent, "VK_ADD", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_SEPARATER, KeyEvent, "VK_SEPARATER", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_SEPARATOR, KeyEvent, "VK_SEPARATOR", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_SUBTRACT, KeyEvent, "VK_SUBTRACT", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_DECIMAL, KeyEvent, "VK_DECIMAL", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_DIVIDE, KeyEvent, "VK_DIVIDE", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_DELETE, KeyEvent, "VK_DELETE", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_NUM_LOCK, KeyEvent, "VK_NUM_LOCK", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_SCROLL_LOCK, KeyEvent, "VK_SCROLL_LOCK", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_F1, KeyEvent, "VK_F1", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_F2, KeyEvent, "VK_F2", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_F3, KeyEvent, "VK_F3", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_F4, KeyEvent, "VK_F4", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_F5, KeyEvent, "VK_F5", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_F6, KeyEvent, "VK_F6", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_F7, KeyEvent, "VK_F7", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_F8, KeyEvent, "VK_F8", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_F9, KeyEvent, "VK_F9", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_F10, KeyEvent, "VK_F10", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_F11, KeyEvent, "VK_F11", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_F12, KeyEvent, "VK_F12", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_F13, KeyEvent, "VK_F13", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_F14, KeyEvent, "VK_F14", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_F15, KeyEvent, "VK_F15", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_F16, KeyEvent, "VK_F16", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_F17, KeyEvent, "VK_F17", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_F18, KeyEvent, "VK_F18", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_F19, KeyEvent, "VK_F19", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_F20, KeyEvent, "VK_F20", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_F21, KeyEvent, "VK_F21", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_F22, KeyEvent, "VK_F22", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_F23, KeyEvent, "VK_F23", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_F24, KeyEvent, "VK_F24", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_PRINTSCREEN, KeyEvent, "VK_PRINTSCREEN", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_INSERT, KeyEvent, "VK_INSERT", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_HELP, KeyEvent, "VK_HELP", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_META, KeyEvent, "VK_META", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_BACK_QUOTE, KeyEvent, "VK_BACK_QUOTE", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_QUOTE, KeyEvent, "VK_QUOTE", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_KP_UP, KeyEvent, "VK_KP_UP", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_KP_DOWN, KeyEvent, "VK_KP_DOWN", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_KP_LEFT, KeyEvent, "VK_KP_LEFT", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_KP_RIGHT, KeyEvent, "VK_KP_RIGHT", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_DEAD_GRAVE, KeyEvent, "VK_DEAD_GRAVE", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_DEAD_ACUTE, KeyEvent, "VK_DEAD_ACUTE", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_DEAD_CIRCUMFLEX, KeyEvent, "VK_DEAD_CIRCUMFLEX", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_DEAD_TILDE, KeyEvent, "VK_DEAD_TILDE", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_DEAD_MACRON, KeyEvent, "VK_DEAD_MACRON", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_DEAD_BREVE, KeyEvent, "VK_DEAD_BREVE", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_DEAD_ABOVEDOT, KeyEvent, "VK_DEAD_ABOVEDOT", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_DEAD_DIAERESIS, KeyEvent, "VK_DEAD_DIAERESIS", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_DEAD_ABOVERING, KeyEvent, "VK_DEAD_ABOVERING", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_DEAD_DOUBLEACUTE, KeyEvent, "VK_DEAD_DOUBLEACUTE", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_DEAD_CARON, KeyEvent, "VK_DEAD_CARON", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_DEAD_CEDILLA, KeyEvent, "VK_DEAD_CEDILLA", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_DEAD_OGONEK, KeyEvent, "VK_DEAD_OGONEK", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_DEAD_IOTA, KeyEvent, "VK_DEAD_IOTA", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_DEAD_VOICED_SOUND, KeyEvent, "VK_DEAD_VOICED_SOUND", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_DEAD_SEMIVOICED_SOUND, KeyEvent, "VK_DEAD_SEMIVOICED_SOUND", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_AMPERSAND, KeyEvent, "VK_AMPERSAND", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_ASTERISK, KeyEvent, "VK_ASTERISK", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_QUOTEDBL, KeyEvent, "VK_QUOTEDBL", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_LESS, KeyEvent, "VK_LESS", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_GREATER, KeyEvent, "VK_GREATER", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_BRACELEFT, KeyEvent, "VK_BRACELEFT", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_BRACERIGHT, KeyEvent, "VK_BRACERIGHT", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_AT, KeyEvent, "VK_AT", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_COLON, KeyEvent, "VK_COLON", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_CIRCUMFLEX, KeyEvent, "VK_CIRCUMFLEX", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_DOLLAR, KeyEvent, "VK_DOLLAR", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_EURO_SIGN, KeyEvent, "VK_EURO_SIGN", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_EXCLAMATION_MARK, KeyEvent, "VK_EXCLAMATION_MARK", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_INVERTED_EXCLAMATION_MARK, KeyEvent, "VK_INVERTED_EXCLAMATION_MARK", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_LEFT_PARENTHESIS, KeyEvent, "VK_LEFT_PARENTHESIS", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_NUMBER_SIGN, KeyEvent, "VK_NUMBER_SIGN", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_PLUS, KeyEvent, "VK_PLUS", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_RIGHT_PARENTHESIS, KeyEvent, "VK_RIGHT_PARENTHESIS", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_UNDERSCORE, KeyEvent, "VK_UNDERSCORE", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_WINDOWS, KeyEvent, "VK_WINDOWS", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_CONTEXT_MENU, KeyEvent, "VK_CONTEXT_MENU", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_FINAL, KeyEvent, "VK_FINAL", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_CONVERT, KeyEvent, "VK_CONVERT", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_NONCONVERT, KeyEvent, "VK_NONCONVERT", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_ACCEPT, KeyEvent, "VK_ACCEPT", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_MODECHANGE, KeyEvent, "VK_MODECHANGE", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_KANA, KeyEvent, "VK_KANA", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_KANJI, KeyEvent, "VK_KANJI", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_ALPHANUMERIC, KeyEvent, "VK_ALPHANUMERIC", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_KATAKANA, KeyEvent, "VK_KATAKANA", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_HIRAGANA, KeyEvent, "VK_HIRAGANA", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_FULL_WIDTH, KeyEvent, "VK_FULL_WIDTH", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_HALF_WIDTH, KeyEvent, "VK_HALF_WIDTH", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_ROMAN_CHARACTERS, KeyEvent, "VK_ROMAN_CHARACTERS", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_ALL_CANDIDATES, KeyEvent, "VK_ALL_CANDIDATES", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_PREVIOUS_CANDIDATE, KeyEvent, "VK_PREVIOUS_CANDIDATE", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_CODE_INPUT, KeyEvent, "VK_CODE_INPUT", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_JAPANESE_KATAKANA, KeyEvent, "VK_JAPANESE_KATAKANA", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_JAPANESE_HIRAGANA, KeyEvent, "VK_JAPANESE_HIRAGANA", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_JAPANESE_ROMAN, KeyEvent, "VK_JAPANESE_ROMAN", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_KANA_LOCK, KeyEvent, "VK_KANA_LOCK", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_INPUT_METHOD_ON_OFF, KeyEvent, "VK_INPUT_METHOD_ON_OFF", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_CUT, KeyEvent, "VK_CUT", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_COPY, KeyEvent, "VK_COPY", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_PASTE, KeyEvent, "VK_PASTE", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_UNDO, KeyEvent, "VK_UNDO", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_AGAIN, KeyEvent, "VK_AGAIN", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_FIND, KeyEvent, "VK_FIND", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_PROPS, KeyEvent, "VK_PROPS", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_STOP, KeyEvent, "VK_STOP", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_COMPOSE, KeyEvent, "VK_COMPOSE", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_ALT_GRAPH, KeyEvent, "VK_ALT_GRAPH", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_BEGIN, KeyEvent, "VK_BEGIN", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_VK_UNDEFINED, KeyEvent, "VK_UNDEFINED", "I");

/* Logger class methods and constants. The first parameter specifies the global variable name. */
static JNF_CLASS_CACHE(Logger_class, "java/util/logging/Logger");
static JNF_MEMBER_CACHE(Logger_severe, Logger_class, "severe", "(Ljava/lang/String;)V");

/* Logger utility class methods and constants. */
static jclass Logging_class = NULL;
static jmethodID Logging_logger = NULL;
static jmethodID Logging_getMessage = NULL;
static jmethodID Logging_getMessageWithArg = NULL;

/* AWT mouse event methods and constants. The first parameter specifies the global variable name. */
static JNF_CLASS_CACHE(MouseEvent, "java/awt/event/MouseEvent");
static JNF_MEMBER_CACHE(MouseEvent_getX, MouseEvent, "getX", "()I");
static JNF_MEMBER_CACHE(MouseEvent_getY, MouseEvent, "getY", "()I");
static JNF_MEMBER_CACHE(MouseEvent_getButton, MouseEvent, "getButton", "()I");
static JNF_STATIC_MEMBER_CACHE(AWT_MOUSE_CLICKED, MouseEvent, "MOUSE_CLICKED", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_MOUSE_PRESSED, MouseEvent, "MOUSE_PRESSED", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_MOUSE_RELEASED, MouseEvent, "MOUSE_RELEASED", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_MOUSE_MOVED, MouseEvent, "MOUSE_MOVED", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_MOUSE_ENTERED, MouseEvent, "MOUSE_ENTERED", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_MOUSE_EXITED, MouseEvent, "MOUSE_EXITED", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_MOUSE_DRAGGED, MouseEvent, "MOUSE_DRAGGED", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_MOUSE_WHEEL, MouseEvent, "MOUSE_WHEEL", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_NOBUTTON, MouseEvent, "NOBUTTON", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_BUTTON1, MouseEvent, "BUTTON1", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_BUTTON2, MouseEvent, "BUTTON2", "I");
static JNF_STATIC_MEMBER_CACHE(AWT_BUTTON3, MouseEvent, "BUTTON3", "I");

/* AWT mouse wheel event methods and constants. The first parameter specifies the global variable name. */
static JNF_CLASS_CACHE(MouseWheelEvent, "java/awt/event/MouseWheelEvent");
static JNF_MEMBER_CACHE(MouseWheelEvent_getWheelRotation, MouseWheelEvent, "getWheelRotation", "()I");

/* Object class, member, and method info global variables. The first parameter specifies the global variable name. */
static JNF_CLASS_CACHE(Object_class, "java/lang/Object");
static JNF_MEMBER_CACHE(Object_toString, Object_class, "toString", "()Ljava/lang/String;");

/* Rectangle class, member, and method info global variables. The first parameter specifies the global variable name. */
static JNF_CLASS_CACHE(Rectangle_class, "java/awt/Rectangle");
static JNF_CTOR_CACHE(Rectangle_init, Rectangle_class, "(IIII)V");
static JNF_MEMBER_CACHE(Rectangle_getX, Rectangle_class, "getX", "()D");
static JNF_MEMBER_CACHE(Rectangle_getY, Rectangle_class, "getY", "()D");
static JNF_MEMBER_CACHE(Rectangle_getWidth, Rectangle_class, "getWidth", "()D");
static JNF_MEMBER_CACHE(Rectangle_getHeight, Rectangle_class, "getHeight", "()D");

/* URL class, member, and method info global variables. The first parameter specifies the global variable name. */
static JNF_CLASS_CACHE(URL_class, "java/net/URL");
static JNF_CTOR_CACHE(URL_init, URL_class, "(Ljava/lang/String;)V");
static JNF_MEMBER_CACHE(URL_toExternalForm, URL_class, "toExternalForm", "()Ljava/lang/String;");

/* The constant for an undefined Carbon key. CGKeyCode is an unsigned short, therefore we use the largest unsigned short
   to represent an undefined key. */
static const CGKeyCode kVK_Undefined = 0xFFFF;

/* The constant for no Carbon mouse button. CGMouseButton is unsigned and uses 0 as the symbol for kCGMouseButton1,
   therefore we use the largest unsigned int to represent no button.*/
static const CGMouseButton kCGMouseButtonNone = 0xFFFFFFFF;

/* The constant for an undefined Carbon scroll wheel unit. CGScrollEventUnit is unsigned and uses 0 as the symbol for
   kCGScrollEventUnitPixel, therefore we use the largest unsigned int to represent an undefined unit.*/
static const CGScrollEventUnit kCGScrollEventUnitUndefined = 0xFFFFFFFF;

NSString *NSStringFromJavaObject(JNIEnv *env, jobject object)
{
    // Get the Java Object's String representation by calling Object.toString. JNFCallObjectMethod returns a JNI local
    // reference. We free these reference by calling DeleteLocalRef after converting this Java String to an NSString.
    jstring jstr = JNFCallObjectMethod(env, object, Object_toString);
    // Convert the Java Object's String representation to a Cocoa NSString. The NSString returned by JNFJavaToNSString
    // is autoreleased. We do not retain it here because this function does not it.
    NSString *nsstr = JNFJavaToNSString(env, jstr);

    (*env)->DeleteLocalRef(env, jstr);

    return nsstr;
}

NSRect NSRectFromJavaRectangle(JNIEnv *env, jobject rect)
{
    jdouble x = JNFCallDoubleMethod(env, rect, Rectangle_getX);
    jdouble y = JNFCallDoubleMethod(env, rect, Rectangle_getY);
    jdouble width = JNFCallDoubleMethod(env, rect, Rectangle_getWidth);
    jdouble height = JNFCallDoubleMethod(env, rect, Rectangle_getHeight);
    return NSMakeRect((CGFloat) x, (CGFloat) y, (CGFloat) width, (CGFloat) height);
}

jobject JavaRectangleFromNSRect(JNIEnv *env, NSRect rect)
{
    return JNFNewObject(env, Rectangle_init, (jint) rect.origin.x, (jint) rect.origin.y, (jint) rect.size.width,
        (jint) rect.size.height);
}

NSSize NSSizeFromJavaDimension(JNIEnv *env, jobject size)
{
    jdouble width = JNFCallDoubleMethod(env, size, Dimension_getWidth);
    jdouble height = JNFCallDoubleMethod(env, size, Dimension_getHeight);
    return NSMakeSize((CGFloat)width, (CGFloat)height);
}

jobject JavaDimensionFromNSSize(JNIEnv *env, NSSize size)
{
    return JNFNewObject(env, Dimension_init, (jint) size.width, (jint) size.height);
}

NSURL *NSURLFromJavaURL(JNIEnv *env, jobject url)
{
    // Convert the Java URL to a Cocoa NSURL. The NSString returned by JNFJavaToNSString and the NSURL returned by
    // URLWithString are autoreleased. We do not retain them here because this function does not them.
    jobject urlString = JNFCallObjectMethod(env, url, URL_toExternalForm);
    NSString *nsURLString = JNFJavaToNSString(env, urlString);
    return [NSURL URLWithString:nsURLString];
}

jobject JavaURLFromNSURL(JNIEnv *env, NSURL *url)
{
    // Convert the Cocoa URL NSString to a Java String. The call to JNFNSToJavaString returns a JNI local reference.
	// We don't free this reference by calling DeleteLocalRef because it's when it's passed back to Java.
	jobject jurlString = JNFNSToJavaString(env, [url absoluteString]);
	return JNFNewObject(env, URL_init, jurlString);
}

NSEvent *NSEventFromJavaEvent(JNIEnv *env, jobject event, NSWindow *sourceWindow)
{
    if (JNFIsInstanceOf(env, event, &KeyEvent))
        return NSEventFromJavaKeyEvent(env, event, sourceWindow);

    // Must check mouse wheel event before mouse event because mouse wheel event is a subclass of MouseEvent.
    else if (JNFIsInstanceOf(env, event, &MouseWheelEvent))
        return NSEventFromJavaMouseWheelEvent(env, event, sourceWindow);

    else if (JNFIsInstanceOf(env, event, &MouseEvent))
        return NSEventFromJavaMouseEvent(env, event, sourceWindow);

    return nil;
}

NSEvent *NSEventFromJavaKeyEvent(JNIEnv *env, jobject event, NSWindow *sourceWindow)
{
    CGEventType type = CGEventTypeFromJavaEvent(env, event);
    CGKeyCode code = CGKeyCodeFromJavaEvent(env, event);

    // Return nil if the AWT event does not map to a Carbon event type.
    if (type == kCGEventNull)
        return nil;

    // Return nil if the AWT key event does not map to a Carbon virtual key.
    if (code == kVK_Undefined)
        return nil;

    // Create a CGEvent from the AWT key event. Return nil if the CGEvent cannot be created.
    CGEventRef cgEvent = CGEventCreateKeyboardEvent(NULL, (CGKeyCode)code, (type == kCGEventKeyDown));
    if (cgEvent == NULL)
        return nil;

    // Wrap the CGEvent in an NSEvent. Return nil if the CGEvent doesn't map to an NSEvent. The returned NSEvent is
    // autoreleased, and takes ownership of the CGEvent.
    NSEvent *nsEvent = [NSEvent eventWithCGEvent:cgEvent];
    if (nsEvent == nil)
        return nil;

    // The NSEvent created by eventWithCGEvent has an erroneous modifier flag in its mask indicating that the event is
    // a gesture event. We remove that flag from the NSEvent returned to the caller.
    NSUInteger flags = [nsEvent modifierFlags] & ~NSEventMaskGesture;

    // Copy the NSEvent properties into a new NSEvent, overriding the window number. Overriding the NSEvent's window
    // number as we do in NSEventFromJavaMouseEvent causes the window to ignore this event, so we create a new event with
    // the appropriate values. We override the window number because the NSEvent returned by eventWithCGEvent does not
    // specify a window number. Offscreen windows ignore events that don't contain their window number, therefore we
    // must specify the window number to ensure that the event is delivered and accepted by the specified window. The
    // returned event is autoreleased. For NSFlagsChanged events, we omit the following properties: characters,
    // charactersIgnoringModifiers, and isARepeat.
    return [NSEvent keyEventWithType:[nsEvent type]
        location:[nsEvent locationInWindow]
        modifierFlags:flags
        timestamp:[nsEvent timestamp]
        windowNumber:[sourceWindow windowNumber] // Override the NSEvent's window number with our window number.
        context:nil // Don't need to specify a graphics context.
        characters:([nsEvent type] != NSFlagsChanged ? [nsEvent characters] : @"")
        charactersIgnoringModifiers:([nsEvent type] != NSFlagsChanged ? [nsEvent charactersIgnoringModifiers] : @"")
        isARepeat:([nsEvent type] != NSFlagsChanged ? [nsEvent isARepeat] : NO)
        keyCode:[nsEvent keyCode]];
}

NSEvent *NSEventFromJavaMouseEvent(JNIEnv *env, jobject event, NSWindow *sourceWindow)
{
    CGEventType type = CGEventTypeFromJavaEvent(env, event);
    CGMouseButton button = CGMouseButtonFromJavaEvent(env, event);
    CGPoint point = CGMousePointFromJavaEvent(env, event);

    // Return nil if the Java mouse event does not map to a Carbon event type.
    if (type == kCGEventNull)
        return nil;

    // Create a CGEvent from the AWT mouse event. Return nil if the CGEvent cannot be created.
    CGEventRef cgEvent = CGEventCreateMouseEvent(NULL, type, point, button);
    if (cgEvent == NULL)
        return nil;

    // Wrap the CGEvent in an NSEvent. Return nil if the CGEvent doesn't map to an NSEvent. The returned NSEvent is
    // autoreleased, and takes ownership of the CGEvent.
    NSEvent *nsEvent = [NSEvent eventWithCGEvent:cgEvent];
    if (nsEvent == nil)
        return nil;

    // Override the NSEvent's window number with our window number. The NSEvent returned by eventWithCGEvent does
    // not specify a window number. Offscreen windows ignore events that don't contain their window number,
    // therefore we must specify the window number to ensure that the event is delivered and accepted by the
    // specified window. The CGEvent does not detect the window number.
    NSNumber *windowNum = [NSNumber numberWithInt:[sourceWindow windowNumber]];
    [nsEvent setValue:windowNum forKey:@"windowNumber"];

    // Override the NSEvent's location with the AWT event's location. The NSEvent returned by eventWithCGEvent uses
    // a different coordinate system than standard NSEvents.
    NSValue *location = [NSValue valueWithPoint:NSMakePoint(point.x, point.y)];
    [nsEvent setValue:location forKey:@"location"];

    return nsEvent;
}

NSEvent *NSEventFromJavaMouseWheelEvent(JNIEnv *env, jobject event, NSWindow *sourceWindow)
{
    int32_t amount[2] = {0, 0};
    CGScrollAmountFromJavaEvent(env, event, sourceWindow, amount);
    CGPoint point = CGMousePointFromJavaEvent(env, event);

    // Create a CGEvent from the AWT mouse wheel event. We always use pixels as the unit of scrolling. Return nil if the
    // CGEvent cannot be created.
    CGEventRef cgEvent = CGEventCreateScrollWheelEvent(NULL, kCGScrollEventUnitPixel, 2, amount[0], amount[1]);
    if (cgEvent == NULL)
        return nil;

    // Wrap the CGEvent in an NSEvent. Return nil if the CGEvent doesn't map to an NSEvent. The returned NSEvent is
    // autoreleased, and takes ownership of the CGEvent.
    NSEvent *nsEvent = [NSEvent eventWithCGEvent:cgEvent];
    if (nsEvent == nil)
        return nil;

    // Override the NSEvent's window number with our window number. The NSEvent returned by eventWithCGEvent does
    // not specify a window number. Offscreen windows ignore events that don't contain their window number,
    // therefore we must specify the window number to ensure that the event is delivered and accepted by the
    // specified window. The CGEvent does not detect the window number.
    NSNumber *windowNum = [NSNumber numberWithInt:[sourceWindow windowNumber]];
    [nsEvent setValue:windowNum forKey:@"windowNumber"];

    // Override the NSEvent's location with the AWT event's location. The NSEvent returned by eventWithCGEvent uses
    // a different coordinate system than standard NSEvents.
    NSValue *location = [NSValue valueWithPoint:NSMakePoint(point.x, point.y)];
    [nsEvent setValue:location forKey:@"location"];

    return nsEvent;
}

CGEventType CGEventTypeFromJavaEvent(JNIEnv *env, jobject event)
{
    jint id = JNFCallIntMethod(env, event, InputEvent_getID);

    if (id == JNFGetStaticIntField(env, AWT_KEY_TYPED))
    {
        // Intentionally left blank. Carbon does not have a key typed event. We use key down and key up instead.
    }
    else if (id == JNFGetStaticIntField(env, AWT_KEY_PRESSED))
    {
        return kCGEventKeyDown;
    }
    else if (id == JNFGetStaticIntField(env, AWT_KEY_RELEASED))
    {
        return kCGEventKeyUp;
    }
    else if (id == JNFGetStaticIntField(env, AWT_MOUSE_CLICKED))
    {
        // Intentionally left blank. Carbon does not have a mouse clicked event. We use mouse pressed and mouse
        // released instead.
    }
    else if (id == JNFGetStaticIntField(env, AWT_MOUSE_PRESSED))
    {
        jint button = JNFCallIntMethod(env, event, MouseEvent_getButton);

        if (button == JNFGetStaticIntField(env, AWT_NOBUTTON))
            return kCGEventNull;
        else if (button == JNFGetStaticIntField(env, AWT_BUTTON1))
            return kCGEventLeftMouseDown;
        else if (button == JNFGetStaticIntField(env, AWT_BUTTON2))
            return kCGEventOtherMouseDown;
        else if (button == JNFGetStaticIntField(env, AWT_BUTTON3))
            return kCGEventRightMouseDown;
    }
    else if (id == JNFGetStaticIntField(env, AWT_MOUSE_RELEASED))
    {
        jint button = JNFCallIntMethod(env, event, MouseEvent_getButton);

        if (button == JNFGetStaticIntField(env, AWT_NOBUTTON))
            return kCGEventNull;
        else if (button == JNFGetStaticIntField(env, AWT_BUTTON1))
            return kCGEventLeftMouseUp;
        else if (button == JNFGetStaticIntField(env, AWT_BUTTON2))
            return kCGEventOtherMouseUp;
        else if (button == JNFGetStaticIntField(env, AWT_BUTTON3))
            return kCGEventRightMouseUp;
    }
    else if (id == JNFGetStaticIntField(env, AWT_MOUSE_MOVED))
    {
        return kCGEventMouseMoved;
    }
    else if (id == JNFGetStaticIntField(env, AWT_MOUSE_ENTERED))
    {
        // Intentionally left blank. Carbon does not have a mouse entered event.
    }
    else if (id == JNFGetStaticIntField(env, AWT_MOUSE_EXITED))
    {
        // Intentionally left blank. Carbon does not have a mouse exited event.
    }
    else if (id == JNFGetStaticIntField(env, AWT_MOUSE_DRAGGED))
    {
        jint button = JNFCallIntMethod(env, event, MouseEvent_getButton);

        if (button == JNFGetStaticIntField(env, AWT_NOBUTTON))
            return kCGEventNull;
        else if (button == JNFGetStaticIntField(env, AWT_BUTTON1))
            return kCGEventLeftMouseDragged;
        else if (button == JNFGetStaticIntField(env, AWT_BUTTON2))
            return kCGEventOtherMouseDragged;
        else if (button == JNFGetStaticIntField(env, AWT_BUTTON3))
            return kCGEventRightMouseDragged;
    }
    else if (id == JNFGetStaticIntField(env, AWT_MOUSE_WHEEL))
    {
        kCGEventScrollWheel;
    }

    return kCGEventNull;
}

CGKeyCode CGKeyCodeFromJavaEvent(JNIEnv *env, jobject event)
{
    jint keyCode = JNFCallIntMethod(env, event, KeyEvent_getKeyCode);
    jint location = JNFCallIntMethod(env, event, KeyEvent_getKeyLocation);

    /*
     *  Summary:
     *    Virtual keycodes
     *
     *  Discussion:
     *    These constants are the virtual keycodes defined originally in
     *    Inside Mac Volume V, pg. V-191. They identify physical keys on a
     *    keyboard. Those constants with "ANSI" in the name are labeled
     *    according to the key position on an ANSI-standard US keyboard.
     *    For example, kVK_ANSI_A indicates the virtual keycode for the key
     *    with the letter 'A' in the US keyboard layout. Other keyboard
     *    layouts may have the 'A' key label on a different physical key;
     *    in this case, pressing 'A' will generate a different virtual
     *    keycode.
     */
    if (keyCode == JNFGetStaticIntField(env, AWT_VK_A))
		return kVK_ANSI_A;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_B))
		return kVK_ANSI_B;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_C))
		return kVK_ANSI_C;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_D))
		return kVK_ANSI_D;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_E))
		return kVK_ANSI_E;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_F))
		return kVK_ANSI_F;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_G))
		return kVK_ANSI_G;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_H))
		return kVK_ANSI_H;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_I))
		return kVK_ANSI_I;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_J))
		return kVK_ANSI_J;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_K))
		return kVK_ANSI_K;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_L))
		return kVK_ANSI_L;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_M))
		return kVK_ANSI_M;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_N))
		return kVK_ANSI_N;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_O))
		return kVK_ANSI_O;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_P))
		return kVK_ANSI_P;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_Q))
		return kVK_ANSI_Q;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_R))
		return kVK_ANSI_R;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_S))
		return kVK_ANSI_S;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_T))
		return kVK_ANSI_T;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_U))
		return kVK_ANSI_U;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_V))
		return kVK_ANSI_V;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_W))
		return kVK_ANSI_W;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_X))
		return kVK_ANSI_X;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_Y))
		return kVK_ANSI_Y;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_Z))
		return kVK_ANSI_Z;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_0))
		return kVK_ANSI_0;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_1))
		return kVK_ANSI_1;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_2))
		return kVK_ANSI_2;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_3))
		return kVK_ANSI_3;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_4))
		return kVK_ANSI_4;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_5))
		return kVK_ANSI_5;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_6))
		return kVK_ANSI_6;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_7))
		return kVK_ANSI_7;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_8))
		return kVK_ANSI_8;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_9))
		return kVK_ANSI_9;
    //else if (keyCode == JNFGetStaticIntField(env, AWT_VK_))
	//	return kVK_ANSI_Grave;

    // Modifier keys.
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_SHIFT) && location != JNFGetStaticIntField(env, AWT_KEY_LOCATION_RIGHT))
		return kVK_Shift;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_CONTROL) && location != JNFGetStaticIntField(env, AWT_KEY_LOCATION_RIGHT))
		return kVK_Control;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_ALT) && location != JNFGetStaticIntField(env, AWT_KEY_LOCATION_RIGHT))
		return kVK_Option;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_META))
		return kVK_Command;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_SHIFT) && location == JNFGetStaticIntField(env, AWT_KEY_LOCATION_RIGHT))
		return kVK_RightShift;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_CONTROL) && location == JNFGetStaticIntField(env, AWT_KEY_LOCATION_RIGHT))
		return kVK_RightControl;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_ALT) && location == JNFGetStaticIntField(env, AWT_KEY_LOCATION_RIGHT))
		return kVK_RightOption;

    // Special keys.
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_ESCAPE))
		return kVK_Escape;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_TAB))
		return kVK_Tab;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_CAPS_LOCK))
		return kVK_CapsLock;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_MINUS))
		return kVK_ANSI_Minus;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_EQUALS) && location != JNFGetStaticIntField(env, AWT_KEY_LOCATION_NUMPAD))
		return kVK_ANSI_Equal;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_BACK_SPACE))
		return kVK_Delete;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_OPEN_BRACKET))
		return kVK_ANSI_LeftBracket;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_CLOSE_BRACKET))
		return kVK_ANSI_RightBracket;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_BACK_SLASH))
		return kVK_ANSI_Backslash;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_SEMICOLON))
		return kVK_ANSI_Semicolon;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_QUOTE))
		return kVK_ANSI_Quote;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_ENTER) && location != JNFGetStaticIntField(env, AWT_KEY_LOCATION_NUMPAD))
		return kVK_Return;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_COMMA))
		return kVK_ANSI_Comma;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_PERIOD))
		return kVK_ANSI_Period;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_SLASH))
		return kVK_ANSI_Slash;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_SPACE))
		return kVK_Space;

    // Non-numpad arrow keys.
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_UP))
		return kVK_UpArrow;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_DOWN))
		return kVK_DownArrow;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_LEFT))
		return kVK_LeftArrow;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_RIGHT))
		return kVK_RightArrow;

    // Other keys.
    //else if (keyCode == JNFGetStaticIntField(env, AWT_VK_))
	//	return kVK_Function;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_HOME))
		return kVK_Home;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_PAGE_UP))
		return kVK_PageUp;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_DELETE))
		return kVK_ForwardDelete;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_END))
		return kVK_End;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_PAGE_DOWN))
		return kVK_PageDown;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_HELP))
		return kVK_Help;

    // Numpad keys.
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_CLEAR))
		return kVK_ANSI_KeypadClear;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_EQUALS) && location == JNFGetStaticIntField(env, AWT_KEY_LOCATION_NUMPAD))
		return kVK_ANSI_KeypadEquals;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_DIVIDE))
		return kVK_ANSI_KeypadDivide;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_MULTIPLY))
		return kVK_ANSI_KeypadMultiply;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_SUBTRACT))
		return kVK_ANSI_KeypadMinus;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_ADD))
		return kVK_ANSI_KeypadPlus;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_ENTER) && location == JNFGetStaticIntField(env, AWT_KEY_LOCATION_NUMPAD))
		return kVK_ANSI_KeypadEnter;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_DECIMAL))
		return kVK_ANSI_KeypadDecimal;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_NUMPAD0))
		return kVK_ANSI_Keypad0;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_NUMPAD1))
		return kVK_ANSI_Keypad1;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_NUMPAD2))
		return kVK_ANSI_Keypad2;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_NUMPAD3))
		return kVK_ANSI_Keypad3;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_NUMPAD4))
		return kVK_ANSI_Keypad4;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_NUMPAD5))
		return kVK_ANSI_Keypad5;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_NUMPAD6))
		return kVK_ANSI_Keypad6;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_NUMPAD7))
		return kVK_ANSI_Keypad7;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_NUMPAD8))
		return kVK_ANSI_Keypad8;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_NUMPAD9))
		return kVK_ANSI_Keypad9;

    // Function keys.
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_F1))
		return kVK_F1;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_F2))
		return kVK_F2;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_F3))
		return kVK_F3;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_F4))
		return kVK_F4;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_F5))
		return kVK_F5;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_F6))
		return kVK_F6;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_F7))
		return kVK_F7;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_F8))
		return kVK_F8;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_F9))
		return kVK_F9;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_F10))
		return kVK_F10;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_F11))
		return kVK_F11;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_F12))
		return kVK_F12;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_F13))
		return kVK_F13;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_F14))
		return kVK_F14;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_F15))
		return kVK_F15;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_F16))
		return kVK_F16;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_F17))
		return kVK_F17;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_F18))
		return kVK_F18;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_F19))
		return kVK_F19;
    else if (keyCode == JNFGetStaticIntField(env, AWT_VK_F20))
		return kVK_F20;
    //else if (keyCode == JNFGetStaticIntField(env, AWT_VK_))
	//	return kVK_VolumeUp;
    //else if (keyCode == JNFGetStaticIntField(env, AWT_VK_))
	//	return kVK_VolumeDown;
    //else if (keyCode == JNFGetStaticIntField(env, AWT_VK_))
	//	return kVK_Mute;

    // ISO keyboards only.
    //else if (keyCode == JNFGetStaticIntField(env, AWT_VK_))
	//	return kVK_ISO_Section;

    // JIS keyboards only.
    //else if (keyCode == JNFGetStaticIntField(env, AWT_VK_))
	//	return kVK_JIS_Yen;
    //else if (keyCode == JNFGetStaticIntField(env, AWT_VK_))
	//	return kVK_JIS_Underscore;
    //else if (keyCode == JNFGetStaticIntField(env, AWT_VK_))
	//	return kVK_JIS_KeypadComma;
    //else if (keyCode == JNFGetStaticIntField(env, AWT_VK_))
	//	return kVK_JIS_Eisu;
    //else if (keyCode == JNFGetStaticIntField(env, AWT_VK_))
	//	return kVK_JIS_Kana;

    return kVK_Undefined;
}

CGMouseButton CGMouseButtonFromJavaEvent(JNIEnv *env, jobject event)
{
    jint button = JNFCallIntMethod(env, event, MouseEvent_getButton);

    if (button == JNFGetStaticIntField(env, AWT_NOBUTTON))
        return kCGMouseButtonNone;
    else if (button == JNFGetStaticIntField(env, AWT_BUTTON1))
        return kCGMouseButtonLeft;
    else if (button == JNFGetStaticIntField(env, AWT_BUTTON3))
        return kCGMouseButtonCenter;
    else if (button == JNFGetStaticIntField(env, AWT_BUTTON2))
        return kCGMouseButtonRight;

    return kCGMouseButtonNone;
}

CGPoint CGMousePointFromJavaEvent(JNIEnv *env, jobject event)
{
    jint x = JNFCallIntMethod(env, event, MouseEvent_getX);
    jint y = JNFCallIntMethod(env, event, MouseEvent_getY);
    return CGPointMake(x, y);
}

void CGScrollAmountFromJavaEvent(JNIEnv *env, jobject event, NSWindow *sourceWindow, int32_t *values)
{
    // Attempt to find the NSScrollView that contains the hit view. We use the enclosing NSScrollView to determine the
    // the amount to scroll based on the raw mouse input.
    NSScrollView *scrollView = nil;
    CGPoint point = CGMousePointFromJavaEvent(env, event);
    NSView *hitView = [[sourceWindow contentView] hitTest:NSMakePoint(point.x, point.y)];
    if (hitView != nil)
        scrollView = [hitView enclosingScrollView];

    // Get the units to scroll from the raw wheel rotation amount. AWT and Cocoa/Carbon wheel units are reversed, so we
    // negate the units when computing the amount to scroll. On Mac OS X, horizontal scroll events are delivered as
    // Shift+ScrollWheel events, since there is no horizontal scrolling API in Java. See Radar #4631846 in
    // http://developer.apple.com/library/mac/#releasenotes/Java/JavaLeopardRN/ResolvedIssues/ResolvedIssues.html.
    jint units = JNFCallIntMethod(env, event, MouseWheelEvent_getWheelRotation);
    jboolean shiftDown = JNFCallBooleanMethod(env, event, InputEvent_isShiftDown);

    if (shiftDown == JNI_TRUE)
        values[1] = (scrollView != nil ? [scrollView horizontalPageScroll] : 1) * (-units);
    else
        values[0] = (scrollView != nil ? [scrollView verticalPageScroll] : 1) * (-units);
}

void InitLoggerJNI(JNIEnv *env)
{
    if (Logging_class != NULL)
        return;

    // Initialize the logging class and methodID pointers. We do this here rather than after obtaining an environment in
    // the *ObtainEnv functions because the World Wind Java classes cannot be found from the AppKit thread. By finding
    // them here and retaining a reference to them, we make them accessible by calls to *ObtainEnv from the AppKit
    // thread. Note that this stores the class and method references in static variables, and therefore assumes that
    // there is one class definition for gov.nasa.worldwind.util.Logging in the JNI environment.
    Logging_class = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "gov/nasa/worldwind/util/Logging"));
    Logging_logger = (*env)->GetStaticMethodID(env, Logging_class, "logger", "()Ljava/util/logging/Logger;");
    Logging_getMessage = (*env)->GetStaticMethodID(env, Logging_class, "getMessage", "(Ljava/lang/String;)Ljava/lang/String;");
    Logging_getMessageWithArg = (*env)->GetStaticMethodID(env, Logging_class, "getMessage", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
}

void LogSevere(JNIEnv *env, NSString *message)
{
    // Convert the Cocoa message NSString to a Java String. JNFNSToJavaString returns a JNI local reference. We free
    // this reference by calling DeleteLocalRef after the call to Logger.severe executes.
    jobject jmessage = JNFNSToJavaString(env, message);

    // Get the default Logger from the Logging utility class, and call Logger.severe with the specified message.
    jobject jlogger = (*env)->CallStaticObjectMethod(env, Logging_class, Logging_logger);
    JNFCallVoidMethod(env, jlogger, Logger_severe, jmessage);

    (*env)->DeleteLocalRef(env, jmessage);
}

void LogSevereObtainEnv(NSString *message)
{
    // Keep the AppKit thread attached to the JVM as long as it's alive. This causes subsequent calls to JNFObtainEnv to
    // return quickly, and reduces the overhead of frequently using PropertyChangeListener to execute Java code on the
    // AppKit thread.
    JNFThreadContext tc = JNFThreadDetachOnThreadDeath;
    JNIEnv *env = NULL;
    @try
    {
        env = JNFObtainEnv(&tc);
        LogSevere(env, message);
    }
    @finally
    {
        if (env != NULL)
            JNFReleaseEnv(env, &tc);
    }
}

NSString *GetLogMessage(JNIEnv *env, NSString *property)
{
    // Convert the Cocoa property NSString to a Java String. JNFNSToJavaString returns a JNI local reference. We free
    // this reference by calling DeleteLocalRef after the call to Logging.getMessage executes.
    jobject jproperty = JNFNSToJavaString(env, property);

    // Get the Java logging string and convert it to a Cocoa string. The NSString returned by JNFJavaToNSString is
    // autoreleased. We do not retain it because this function does not own it. We let the autorelease pool release this
    // string after this function executes.
    jobject jmessage = (*env)->CallStaticObjectMethod(env, Logging_class, Logging_getMessage, jproperty);
    NSString *message = JNFJavaToNSString(env, jmessage);

    (*env)->DeleteLocalRef(env, jproperty);

    return message;
}


NSString *GetLogMessageObtainEnv(NSString *property)
{
    NSString *message = nil;

    // Keep the AppKit thread attached to the JVM as long as it's alive. This causes subsequent calls to JNFObtainEnv to
    // return quickly, and reduces the overhead of frequently using PropertyChangeListener to execute Java code on the
    // AppKit thread.
    JNFThreadContext tc = JNFThreadDetachOnThreadDeath;
    JNIEnv *env = NULL;
    @try
    {
        env = JNFObtainEnv(&tc);
        message = GetLogMessage(env, property);
    }
    @finally
    {
        if (env != NULL)
            JNFReleaseEnv(env, &tc);
    }

    return message;
}

NSString *GetLogMessageWithException(JNIEnv *env, NSString *property, NSException *exception)
{
    // Convert the Cocoa property NSString to a Java String. JNFNSToJavaString returns a JNI local reference. We free
    // this reference by calling DeleteLocalRef after the call to Logging.getMessage executes.
    jobject jproperty = JNFNSToJavaString(env, property);

    // Get the Java logging string and convert it to a Cocoa string. The NSString returned by JNFJavaToNSString is
    // autoreleased. We do not retain it because this function does not own it. We let the autorelease pool release this
    // string after this function executes.
    jobject jmessage = (*env)->CallStaticObjectMethod(env, Logging_class, Logging_getMessage, jproperty);
    NSString *message = JNFJavaToNSString(env, jmessage);

    if (exception != nil)
    {
        // If the exception is non-nil, append it's description to the logging messaage. The NSMutableString returned by
        // stringWithCapacity is autoreleased. We do not retain it because this function does not own it. We let the
        // autorelease pool release this string after this function executes.
        NSMutableString *sb = [NSMutableString stringWithCapacity:8];
        [sb appendString:message];

        // The NSString returned by GetDescriptionForException is autoreleased. We do not retain it because this function
        // does not own it. We let the autorelease pool release this string after this function executes.
        NSString *desc = GetDescriptionForException(exception);
        [sb appendString:@"\n"];
        [sb appendString:desc];

        message = sb; // The object referenced by "message" is autoreleased after this function exits.
    }

    (*env)->DeleteLocalRef(env, jproperty);

    return message;
}


NSString *GetLogMessageWithExceptionObtainEnv(NSString *property, NSException *exception)
{
    NSString *message = nil;

    // Keep the AppKit thread attached to the JVM as long as it's alive. This causes subsequent calls to JNFObtainEnv to
    // return quickly, and reduces the overhead of frequently using PropertyChangeListener to execute Java code on the
    // AppKit thread.
    JNFThreadContext tc = JNFThreadDetachOnThreadDeath;
    JNIEnv *env = NULL;
    @try
    {
        env = JNFObtainEnv(&tc);
        message = GetLogMessageWithException(env, property, exception);
    }
    @finally
    {
        if (env != NULL)
            JNFReleaseEnv(env, &tc);
    }

    return message;
}

NSString *GetLogMessageWithArgException(JNIEnv *env, NSString *property, NSString *arg, NSException *exception)
{
    // Convert the Cocoa property and arg NSStrings to Java Strings. JNFNSToJavaString returns a JNI local reference. We
    // free these reference by calling DeleteLocalRef after the call to Logging.getMessage executes.
    jobject jproperty = JNFNSToJavaString(env, property);
    jobject jarg = JNFNSToJavaString(env, arg);

    // Get the Java logging string and convert it to a Cocoa string. The NSString returned by JNFJavaToNSString is
    // autoreleased. We do not retain it because this function does not own it. We let the autorelease pool release this
    // string after this function executes.
    jobject jmessage = (*env)->CallStaticObjectMethod(env, Logging_class, Logging_getMessageWithArg, jproperty, jarg);
    NSString *message = JNFJavaToNSString(env, jmessage);

    if (exception != nil)
    {
        // If the exception is non-nil, append it's description to the logging messaage. The NSMutableString returned by
        // StringWithCapacity is autoreleased. We do not retain it because this function does not own it. We let the
        // autorelease pool release this string after this function executes.
        NSMutableString *sb = [NSMutableString stringWithCapacity:8];
        [sb appendString:message];

        // The NSString returned by GetDescriptionForException is autoreleased. We do not retain it because this function
        // does not own it. We let the autorelease pool release this string after this function executes.
        NSString *desc = GetDescriptionForException(exception);
        [sb appendString:@"\n"];
        [sb appendString:desc];
        
        message = sb; // The object referenced by "message" is autoreleased after this function exits.
    }

    (*env)->DeleteLocalRef(env, jproperty);
    (*env)->DeleteLocalRef(env, jarg);

    return message;
}


NSString *GetLogMessageWithArgExceptionObtainEnv(NSString *property, NSString *arg, NSException *exception)
{
    NSString *message = nil;

    // Keep the AppKit thread attached to the JVM as long as it's alive. This causes subsequent calls to JNFObtainEnv to
    // return quickly, and reduces the overhead of frequently using PropertyChangeListener to execute Java code on the
    // AppKit thread.
    JNFThreadContext tc = JNFThreadDetachOnThreadDeath;
    JNIEnv *env = NULL;
    @try
    {
        env = JNFObtainEnv(&tc);
        message = GetLogMessageWithArgException(env, property, arg, exception);
    }
    @finally
    {
        if (env != NULL)
            JNFReleaseEnv(env, &tc);
    }

    return message;
}

NSString *GetDescriptionForException(NSException *exception)
{
    // The NSMutableString returned by stringWithCapacity is autoreleased. We do not retain it because this function
    // does not own it. We let the autorelease pool release this string after this function executes.
    NSMutableString *sb = [NSMutableString stringWithCapacity:8];

    if ([exception name] != nil)
    {
        [sb appendString:[exception name]];
    }

    if ([exception reason] != nil)
    {
        if ([sb length] > 0)
            [sb appendString:@": "];
        [sb appendString:[exception reason]];
    }

    // The message [NSException callStackSymbols] is available only in Mac OS X 10.6 and later. If we're running on an
    // earlier version we omit the stack trace from the exception's description.
    SEL selector = @selector(callStackSymbols);
    if ([exception respondsToSelector:selector])
    {
        NSArray *stackSymbols = [exception performSelector:selector];
        if (stackSymbols != nil)
        {
            for (NSString *symbol in stackSymbols)
            {
                if (symbol != nil)
                {
                    [sb appendString:@"\n\t"];
                    [sb appendString:symbol];
                }
            }
        }
    }

    return sb;
}
