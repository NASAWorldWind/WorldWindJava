/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

/**
 * Version: $Id: AWTEventSupport.cpp 1171 2013-02-11 21:45:02Z dcollins $
 */

#include "stdafx.h"
#include "AWTEventSupport.h"
#include "WebViewWindow.h"
#include <Winuser.h>

// Constants to identify bits in the LPARAM structure used to
// send WM_KEYUP and WM_KEYDOWN messages
#define EXTENDED_KEY            (1 << 24)
#define PREVIOUS_KEY_STATE_BIT  (1 << 30)
#define TRANSITION_STATE_BIT    (1 << 31)

static jclass AWTInputEvent;

/////////////////////////////////
// Mouse methods and fields
/////////////////////////////////

static jclass AWTMouseEvent;

static jmethodID AWTInputEvent_getID;
static jmethodID AWTInputEvent_isShiftDown;
static jmethodID AWTInputEvent_isControlDown;
static jmethodID AWTInputEvent_isAltDown;
static jmethodID AWTInputEvent_getModifiersEx;

static jmethodID AWTMouseEvent_getClickCount;
static jmethodID AWTMouseEvent_getButton;
static jmethodID AWTMouseEvent_getX;
static jmethodID AWTMouseEvent_getY;

static jfieldID AWT_NOBUTTON;
static jfieldID AWT_BUTTON1;
static jfieldID AWT_BUTTON2;
static jfieldID AWT_BUTTON3;
static jfieldID AWT_BUTTON1_DOWN_MASK;
static jfieldID AWT_BUTTON2_DOWN_MASK;
static jfieldID AWT_BUTTON3_DOWN_MASK;

static jfieldID AWT_MOUSE_CLICKED;
static jfieldID AWT_MOUSE_PRESSED;
static jfieldID AWT_MOUSE_RELEASED;
static jfieldID AWT_MOUSE_MOVED;
static jfieldID AWT_MOUSE_ENTERED;
static jfieldID AWT_MOUSE_EXITED;
static jfieldID AWT_MOUSE_DRAGGED;
static jfieldID AWT_MOUSE_WHEEL;

//////////////////////////////////
// Mouse wheel methods and fields
//////////////////////////////////

static jclass AWTMouseWheelEvent;
static jmethodID AWTMouseWheelEvent_getWheelRotation;

/////////////////////////////////
// Key methods and fields
/////////////////////////////////

static jclass AWTKeyEvent;
static jmethodID AWTKeyEvent_getKeyCode;
static jmethodID AWTKeyEvent_getKeyLocation;

static jfieldID AWT_KEY_TYPED;
static jfieldID AWT_KEY_PRESSED;
static jfieldID AWT_KEY_RELEASED;
static jfieldID AWT_KEY_LOCATION_UNKNOWN;
static jfieldID AWT_KEY_LOCATION_STANDARD;
static jfieldID AWT_KEY_LOCATION_LEFT;
static jfieldID AWT_KEY_LOCATION_RIGHT;
static jfieldID AWT_KEY_LOCATION_NUMPAD;
static jfieldID AWT_CHAR_UNDEFINED;
static jfieldID AWT_VK_ENTER;
static jfieldID AWT_VK_BACK_SPACE;
static jfieldID AWT_VK_TAB;
static jfieldID AWT_VK_CANCEL;
static jfieldID AWT_VK_CLEAR;
static jfieldID AWT_VK_SHIFT;
static jfieldID AWT_VK_CONTROL;
static jfieldID AWT_VK_ALT;
static jfieldID AWT_VK_PAUSE;
static jfieldID AWT_VK_CAPS_LOCK;
static jfieldID AWT_VK_ESCAPE;
static jfieldID AWT_VK_SPACE;
static jfieldID AWT_VK_PAGE_UP;
static jfieldID AWT_VK_PAGE_DOWN;
static jfieldID AWT_VK_END;
static jfieldID AWT_VK_HOME;
static jfieldID AWT_VK_LEFT;
static jfieldID AWT_VK_UP;
static jfieldID AWT_VK_RIGHT;
static jfieldID AWT_VK_DOWN;
static jfieldID AWT_VK_COMMA;
static jfieldID AWT_VK_MINUS;
static jfieldID AWT_VK_PERIOD;
static jfieldID AWT_VK_SLASH;
static jfieldID AWT_VK_0;
static jfieldID AWT_VK_1;
static jfieldID AWT_VK_2;
static jfieldID AWT_VK_3;
static jfieldID AWT_VK_4;
static jfieldID AWT_VK_5;
static jfieldID AWT_VK_6;
static jfieldID AWT_VK_7;
static jfieldID AWT_VK_8;
static jfieldID AWT_VK_9;
static jfieldID AWT_VK_SEMICOLON;
static jfieldID AWT_VK_EQUALS;
static jfieldID AWT_VK_A;
static jfieldID AWT_VK_B;
static jfieldID AWT_VK_C;
static jfieldID AWT_VK_D;
static jfieldID AWT_VK_E;
static jfieldID AWT_VK_F;
static jfieldID AWT_VK_G;
static jfieldID AWT_VK_H;
static jfieldID AWT_VK_I;
static jfieldID AWT_VK_J;
static jfieldID AWT_VK_K;
static jfieldID AWT_VK_L;
static jfieldID AWT_VK_M;
static jfieldID AWT_VK_N;
static jfieldID AWT_VK_O;
static jfieldID AWT_VK_P;
static jfieldID AWT_VK_Q;
static jfieldID AWT_VK_R;
static jfieldID AWT_VK_S;
static jfieldID AWT_VK_T;
static jfieldID AWT_VK_U;
static jfieldID AWT_VK_V;
static jfieldID AWT_VK_W;
static jfieldID AWT_VK_X;
static jfieldID AWT_VK_Y;
static jfieldID AWT_VK_Z;
static jfieldID AWT_VK_OPEN_BRACKET;
static jfieldID AWT_VK_BACK_SLASH;
static jfieldID AWT_VK_CLOSE_BRACKET;
static jfieldID AWT_VK_NUMPAD0;
static jfieldID AWT_VK_NUMPAD1;
static jfieldID AWT_VK_NUMPAD2;
static jfieldID AWT_VK_NUMPAD3;
static jfieldID AWT_VK_NUMPAD4;
static jfieldID AWT_VK_NUMPAD5;
static jfieldID AWT_VK_NUMPAD6;
static jfieldID AWT_VK_NUMPAD7;
static jfieldID AWT_VK_NUMPAD8;
static jfieldID AWT_VK_NUMPAD9;
static jfieldID AWT_VK_MULTIPLY;
static jfieldID AWT_VK_ADD;
static jfieldID AWT_VK_SEPARATER;
static jfieldID AWT_VK_SEPARATOR;
static jfieldID AWT_VK_SUBTRACT;
static jfieldID AWT_VK_DECIMAL;
static jfieldID AWT_VK_DIVIDE;
static jfieldID AWT_VK_DELETE;
static jfieldID AWT_VK_NUM_LOCK;
static jfieldID AWT_VK_SCROLL_LOCK;
static jfieldID AWT_VK_F1;
static jfieldID AWT_VK_F2;
static jfieldID AWT_VK_F3;
static jfieldID AWT_VK_F4;
static jfieldID AWT_VK_F5;
static jfieldID AWT_VK_F6;
static jfieldID AWT_VK_F7;
static jfieldID AWT_VK_F8;
static jfieldID AWT_VK_F9;
static jfieldID AWT_VK_F10;
static jfieldID AWT_VK_F11;
static jfieldID AWT_VK_F12;
static jfieldID AWT_VK_F13;
static jfieldID AWT_VK_F14;
static jfieldID AWT_VK_F15;
static jfieldID AWT_VK_F16;
static jfieldID AWT_VK_F17;
static jfieldID AWT_VK_F18;
static jfieldID AWT_VK_F19;
static jfieldID AWT_VK_F20;
static jfieldID AWT_VK_F21;
static jfieldID AWT_VK_F22;
static jfieldID AWT_VK_F23;
static jfieldID AWT_VK_F24;
static jfieldID AWT_VK_PRINTSCREEN;
static jfieldID AWT_VK_INSERT;
static jfieldID AWT_VK_HELP;
static jfieldID AWT_VK_META;
static jfieldID AWT_VK_BACK_QUOTE;
static jfieldID AWT_VK_QUOTE;
static jfieldID AWT_VK_KP_UP;
static jfieldID AWT_VK_KP_DOWN;
static jfieldID AWT_VK_KP_LEFT;
static jfieldID AWT_VK_KP_RIGHT;
static jfieldID AWT_VK_DEAD_GRAVE;
static jfieldID AWT_VK_DEAD_ACUTE;
static jfieldID AWT_VK_DEAD_CIRCUMFLEX;
static jfieldID AWT_VK_DEAD_TILDE;
static jfieldID AWT_VK_DEAD_MACRON;
static jfieldID AWT_VK_DEAD_BREVE;
static jfieldID AWT_VK_DEAD_ABOVEDOT;
static jfieldID AWT_VK_DEAD_DIAERESIS;
static jfieldID AWT_VK_DEAD_ABOVERING;
static jfieldID AWT_VK_DEAD_DOUBLEACUTE;
static jfieldID AWT_VK_DEAD_CARON;
static jfieldID AWT_VK_DEAD_CEDILLA;
static jfieldID AWT_VK_DEAD_OGONEK;
static jfieldID AWT_VK_DEAD_IOTA;
static jfieldID AWT_VK_DEAD_VOICED_SOUND;
static jfieldID AWT_VK_DEAD_SEMIVOICED_SOUND;
static jfieldID AWT_VK_AMPERSAND;
static jfieldID AWT_VK_ASTERISK;
static jfieldID AWT_VK_QUOTEDBL;
static jfieldID AWT_VK_LESS;
static jfieldID AWT_VK_GREATER;
static jfieldID AWT_VK_BRACELEFT;
static jfieldID AWT_VK_BRACERIGHT;
static jfieldID AWT_VK_AT;
static jfieldID AWT_VK_COLON;
static jfieldID AWT_VK_CIRCUMFLEX;
static jfieldID AWT_VK_DOLLAR;
static jfieldID AWT_VK_EURO_SIGN;
static jfieldID AWT_VK_EXCLAMATION_MARK;
static jfieldID AWT_VK_INVERTED_EXCLAMATION_MARK;
static jfieldID AWT_VK_LEFT_PARENTHESIS;
static jfieldID AWT_VK_NUMBER_SIGN;
static jfieldID AWT_VK_PLUS;
static jfieldID AWT_VK_RIGHT_PARENTHESIS;
static jfieldID AWT_VK_UNDERSCORE;
static jfieldID AWT_VK_WINDOWS;
static jfieldID AWT_VK_CONTEXT_MENU;
static jfieldID AWT_VK_FINAL;
static jfieldID AWT_VK_CONVERT;
static jfieldID AWT_VK_NONCONVERT;
static jfieldID AWT_VK_ACCEPT;
static jfieldID AWT_VK_MODECHANGE;
static jfieldID AWT_VK_KANA;
static jfieldID AWT_VK_KANJI;
static jfieldID AWT_VK_ALPHANUMERIC;
static jfieldID AWT_VK_KATAKANA;
static jfieldID AWT_VK_HIRAGANA;
static jfieldID AWT_VK_FULL_WIDTH;
static jfieldID AWT_VK_HALF_WIDTH;
static jfieldID AWT_VK_ROMAN_CHARACTERS;
static jfieldID AWT_VK_ALL_CANDIDATES;
static jfieldID AWT_VK_PREVIOUS_CANDIDATE;
static jfieldID AWT_VK_CODE_INPUT;
static jfieldID AWT_VK_JAPANESE_KATAKANA;
static jfieldID AWT_VK_JAPANESE_HIRAGANA;
static jfieldID AWT_VK_JAPANESE_ROMAN;
static jfieldID AWT_VK_KANA_LOCK;
static jfieldID AWT_VK_INPUT_METHOD_ON_OFF;
static jfieldID AWT_VK_CUT;
static jfieldID AWT_VK_COPY;
static jfieldID AWT_VK_PASTE;
static jfieldID AWT_VK_UNDO;
static jfieldID AWT_VK_AGAIN;
static jfieldID AWT_VK_FIND;
static jfieldID AWT_VK_PROPS;
static jfieldID AWT_VK_STOP;
static jfieldID AWT_VK_COMPOSE;
static jfieldID AWT_VK_ALT_GRAPH;
static jfieldID AWT_VK_BEGIN;
static jfieldID AWT_VK_UNDEFINED;

void AWTEvent_Initialize(JNIEnv *env)
{
    AWTInputEvent = (jclass)env->NewGlobalRef(env->FindClass("java/awt/event/InputEvent"));
    AWTMouseWheelEvent = (jclass)env->NewGlobalRef(env->FindClass("java/awt/event/MouseWheelEvent"));

    AWTInputEvent_getID = env->GetMethodID(AWTInputEvent, "getID", "()I");
    AWTInputEvent_isShiftDown = env->GetMethodID(AWTInputEvent, "isShiftDown", "()Z");
    AWTInputEvent_isAltDown = env->GetMethodID(AWTInputEvent, "isAltDown", "()Z");
    AWTInputEvent_isControlDown = env->GetMethodID(AWTInputEvent, "isControlDown", "()Z");
    AWTInputEvent_getModifiersEx = env->GetMethodID(AWTInputEvent, "getModifiersEx", "()I");
    assert(AWTInputEvent_getModifiersEx);

    AWTMouseEvent = (jclass)env->NewGlobalRef(env->FindClass("java/awt/event/MouseEvent"));
    AWTMouseEvent_getClickCount = env->GetMethodID(AWTMouseEvent, "getClickCount", "()I");
    AWTMouseEvent_getButton = env->GetMethodID(AWTMouseEvent, "getButton", "()I");
    AWTMouseEvent_getX = env->GetMethodID(AWTMouseEvent, "getX", "()I");
    AWTMouseEvent_getY = env->GetMethodID(AWTMouseEvent, "getY", "()I");

    AWTMouseWheelEvent_getWheelRotation = env->GetMethodID(AWTMouseWheelEvent, "getWheelRotation", "()I");

    AWT_NOBUTTON = env->GetStaticFieldID(AWTMouseEvent, "NOBUTTON", "I");

    AWT_BUTTON1 = env->GetStaticFieldID(AWTMouseEvent, "BUTTON1", "I");
    AWT_BUTTON2 = env->GetStaticFieldID(AWTMouseEvent, "BUTTON2", "I");
    AWT_BUTTON3 = env->GetStaticFieldID(AWTMouseEvent, "BUTTON3", "I");

    AWT_BUTTON1_DOWN_MASK = env->GetStaticFieldID(AWTInputEvent, "BUTTON1_DOWN_MASK", "I");
    AWT_BUTTON2_DOWN_MASK = env->GetStaticFieldID(AWTInputEvent, "BUTTON2_DOWN_MASK", "I");
    AWT_BUTTON3_DOWN_MASK = env->GetStaticFieldID(AWTInputEvent, "BUTTON3_DOWN_MASK", "I");

    AWT_MOUSE_CLICKED = env->GetStaticFieldID(AWTMouseEvent, "MOUSE_CLICKED", "I");
    AWT_MOUSE_PRESSED = env->GetStaticFieldID(AWTMouseEvent, "MOUSE_PRESSED", "I");
    AWT_MOUSE_RELEASED = env->GetStaticFieldID(AWTMouseEvent, "MOUSE_RELEASED", "I");
    AWT_MOUSE_MOVED = env->GetStaticFieldID(AWTMouseEvent, "MOUSE_MOVED", "I");
    AWT_MOUSE_ENTERED = env->GetStaticFieldID(AWTMouseEvent, "MOUSE_ENTERED", "I");
    AWT_MOUSE_EXITED = env->GetStaticFieldID(AWTMouseEvent, "MOUSE_EXITED", "I");
    AWT_MOUSE_DRAGGED = env->GetStaticFieldID(AWTMouseEvent, "MOUSE_DRAGGED", "I");
    AWT_MOUSE_WHEEL = env->GetStaticFieldID(AWTMouseEvent, "MOUSE_WHEEL", "I");

    AWTKeyEvent = (jclass)env->NewGlobalRef(env->FindClass("java/awt/event/KeyEvent"));
    AWTKeyEvent_getKeyCode = env->GetMethodID(AWTKeyEvent, "getKeyCode", "()I");
    AWTKeyEvent_getKeyLocation = env->GetMethodID(AWTKeyEvent, "getKeyLocation", "()I");

    AWT_KEY_TYPED = env->GetStaticFieldID(AWTKeyEvent, "KEY_TYPED", "I");
    AWT_KEY_PRESSED = env->GetStaticFieldID(AWTKeyEvent, "KEY_PRESSED", "I");
    AWT_KEY_RELEASED = env->GetStaticFieldID(AWTKeyEvent, "KEY_RELEASED", "I");
    AWT_KEY_LOCATION_UNKNOWN = env->GetStaticFieldID(AWTKeyEvent, "KEY_LOCATION_UNKNOWN", "I");
    AWT_KEY_LOCATION_STANDARD = env->GetStaticFieldID(AWTKeyEvent, "KEY_LOCATION_STANDARD", "I");
    AWT_KEY_LOCATION_LEFT = env->GetStaticFieldID(AWTKeyEvent, "KEY_LOCATION_LEFT", "I");
    AWT_KEY_LOCATION_RIGHT = env->GetStaticFieldID(AWTKeyEvent, "KEY_LOCATION_RIGHT", "I");
    AWT_KEY_LOCATION_NUMPAD = env->GetStaticFieldID(AWTKeyEvent, "KEY_LOCATION_NUMPAD", "I");
    AWT_CHAR_UNDEFINED = env->GetStaticFieldID(AWTKeyEvent, "CHAR_UNDEFINED", "C");
    AWT_VK_ENTER = env->GetStaticFieldID(AWTKeyEvent, "VK_ENTER", "I");
    AWT_VK_BACK_SPACE = env->GetStaticFieldID(AWTKeyEvent, "VK_BACK_SPACE", "I");
    AWT_VK_TAB = env->GetStaticFieldID(AWTKeyEvent, "VK_TAB", "I");
    AWT_VK_CANCEL = env->GetStaticFieldID(AWTKeyEvent, "VK_CANCEL", "I");
    AWT_VK_CLEAR = env->GetStaticFieldID(AWTKeyEvent, "VK_CLEAR", "I");
    AWT_VK_SHIFT = env->GetStaticFieldID(AWTKeyEvent, "VK_SHIFT", "I");
    AWT_VK_CONTROL = env->GetStaticFieldID(AWTKeyEvent, "VK_CONTROL", "I");
    AWT_VK_ALT = env->GetStaticFieldID(AWTKeyEvent, "VK_ALT", "I");
    AWT_VK_PAUSE = env->GetStaticFieldID(AWTKeyEvent, "VK_PAUSE", "I");
    AWT_VK_CAPS_LOCK = env->GetStaticFieldID(AWTKeyEvent, "VK_CAPS_LOCK", "I");
    AWT_VK_ESCAPE = env->GetStaticFieldID(AWTKeyEvent, "VK_ESCAPE", "I");
    AWT_VK_SPACE = env->GetStaticFieldID(AWTKeyEvent, "VK_SPACE", "I");
    AWT_VK_PAGE_UP = env->GetStaticFieldID(AWTKeyEvent, "VK_PAGE_UP", "I");
    AWT_VK_PAGE_DOWN = env->GetStaticFieldID(AWTKeyEvent, "VK_PAGE_DOWN", "I");
    AWT_VK_END = env->GetStaticFieldID(AWTKeyEvent, "VK_END", "I");
    AWT_VK_HOME = env->GetStaticFieldID(AWTKeyEvent, "VK_HOME", "I");
    AWT_VK_LEFT = env->GetStaticFieldID(AWTKeyEvent, "VK_LEFT", "I");
    AWT_VK_UP = env->GetStaticFieldID(AWTKeyEvent, "VK_UP", "I");
    AWT_VK_RIGHT = env->GetStaticFieldID(AWTKeyEvent, "VK_RIGHT", "I");
    AWT_VK_DOWN = env->GetStaticFieldID(AWTKeyEvent, "VK_DOWN", "I");
    AWT_VK_COMMA = env->GetStaticFieldID(AWTKeyEvent, "VK_COMMA", "I");
    AWT_VK_MINUS = env->GetStaticFieldID(AWTKeyEvent, "VK_MINUS", "I");
    AWT_VK_PERIOD = env->GetStaticFieldID(AWTKeyEvent, "VK_PERIOD", "I");
    AWT_VK_SLASH = env->GetStaticFieldID(AWTKeyEvent, "VK_SLASH", "I");
    AWT_VK_0 = env->GetStaticFieldID(AWTKeyEvent, "VK_0", "I");
    AWT_VK_1 = env->GetStaticFieldID(AWTKeyEvent, "VK_1", "I");
    AWT_VK_2 = env->GetStaticFieldID(AWTKeyEvent, "VK_2", "I");
    AWT_VK_3 = env->GetStaticFieldID(AWTKeyEvent, "VK_3", "I");
    AWT_VK_4 = env->GetStaticFieldID(AWTKeyEvent, "VK_4", "I");
    AWT_VK_5 = env->GetStaticFieldID(AWTKeyEvent, "VK_5", "I");
    AWT_VK_6 = env->GetStaticFieldID(AWTKeyEvent, "VK_6", "I");
    AWT_VK_7 = env->GetStaticFieldID(AWTKeyEvent, "VK_7", "I");
    AWT_VK_8 = env->GetStaticFieldID(AWTKeyEvent, "VK_8", "I");
    AWT_VK_9 = env->GetStaticFieldID(AWTKeyEvent, "VK_9", "I");
    AWT_VK_SEMICOLON = env->GetStaticFieldID(AWTKeyEvent, "VK_SEMICOLON", "I");
    AWT_VK_EQUALS = env->GetStaticFieldID(AWTKeyEvent, "VK_EQUALS", "I");
    AWT_VK_A = env->GetStaticFieldID(AWTKeyEvent, "VK_A", "I");
    AWT_VK_B = env->GetStaticFieldID(AWTKeyEvent, "VK_B", "I");
    AWT_VK_C = env->GetStaticFieldID(AWTKeyEvent, "VK_C", "I");
    AWT_VK_D = env->GetStaticFieldID(AWTKeyEvent, "VK_D", "I");
    AWT_VK_E = env->GetStaticFieldID(AWTKeyEvent, "VK_E", "I");
    AWT_VK_F = env->GetStaticFieldID(AWTKeyEvent, "VK_F", "I");
    AWT_VK_G = env->GetStaticFieldID(AWTKeyEvent, "VK_G", "I");
    AWT_VK_H = env->GetStaticFieldID(AWTKeyEvent, "VK_H", "I");
    AWT_VK_I = env->GetStaticFieldID(AWTKeyEvent, "VK_I", "I");
    AWT_VK_J = env->GetStaticFieldID(AWTKeyEvent, "VK_J", "I");
    AWT_VK_K = env->GetStaticFieldID(AWTKeyEvent, "VK_K", "I");
    AWT_VK_L = env->GetStaticFieldID(AWTKeyEvent, "VK_L", "I");
    AWT_VK_M = env->GetStaticFieldID(AWTKeyEvent, "VK_M", "I");
    AWT_VK_N = env->GetStaticFieldID(AWTKeyEvent, "VK_N", "I");
    AWT_VK_O = env->GetStaticFieldID(AWTKeyEvent, "VK_O", "I");
    AWT_VK_P = env->GetStaticFieldID(AWTKeyEvent, "VK_P", "I");
    AWT_VK_Q = env->GetStaticFieldID(AWTKeyEvent, "VK_Q", "I");
    AWT_VK_R = env->GetStaticFieldID(AWTKeyEvent, "VK_R", "I");
    AWT_VK_S = env->GetStaticFieldID(AWTKeyEvent, "VK_S", "I");
    AWT_VK_T = env->GetStaticFieldID(AWTKeyEvent, "VK_T", "I");
    AWT_VK_U = env->GetStaticFieldID(AWTKeyEvent, "VK_U", "I");
    AWT_VK_V = env->GetStaticFieldID(AWTKeyEvent, "VK_V", "I");
    AWT_VK_W = env->GetStaticFieldID(AWTKeyEvent, "VK_W", "I");
    AWT_VK_X = env->GetStaticFieldID(AWTKeyEvent, "VK_X", "I");
    AWT_VK_Y = env->GetStaticFieldID(AWTKeyEvent, "VK_Y", "I");
    AWT_VK_Z = env->GetStaticFieldID(AWTKeyEvent, "VK_Z", "I");
    AWT_VK_OPEN_BRACKET = env->GetStaticFieldID(AWTKeyEvent, "VK_OPEN_BRACKET", "I");
    AWT_VK_BACK_SLASH = env->GetStaticFieldID(AWTKeyEvent, "VK_BACK_SLASH", "I");
    AWT_VK_CLOSE_BRACKET = env->GetStaticFieldID(AWTKeyEvent, "VK_CLOSE_BRACKET", "I");
    AWT_VK_NUMPAD0 = env->GetStaticFieldID(AWTKeyEvent, "VK_NUMPAD0", "I");
    AWT_VK_NUMPAD1 = env->GetStaticFieldID(AWTKeyEvent, "VK_NUMPAD1", "I");
    AWT_VK_NUMPAD2 = env->GetStaticFieldID(AWTKeyEvent, "VK_NUMPAD2", "I");
    AWT_VK_NUMPAD3 = env->GetStaticFieldID(AWTKeyEvent, "VK_NUMPAD3", "I");
    AWT_VK_NUMPAD4 = env->GetStaticFieldID(AWTKeyEvent, "VK_NUMPAD4", "I");
    AWT_VK_NUMPAD5 = env->GetStaticFieldID(AWTKeyEvent, "VK_NUMPAD5", "I");
    AWT_VK_NUMPAD6 = env->GetStaticFieldID(AWTKeyEvent, "VK_NUMPAD6", "I");
    AWT_VK_NUMPAD7 = env->GetStaticFieldID(AWTKeyEvent, "VK_NUMPAD7", "I");
    AWT_VK_NUMPAD8 = env->GetStaticFieldID(AWTKeyEvent, "VK_NUMPAD8", "I");
    AWT_VK_NUMPAD9 = env->GetStaticFieldID(AWTKeyEvent, "VK_NUMPAD9", "I");
    AWT_VK_MULTIPLY = env->GetStaticFieldID(AWTKeyEvent, "VK_MULTIPLY", "I");
    AWT_VK_ADD = env->GetStaticFieldID(AWTKeyEvent, "VK_ADD", "I");
    AWT_VK_SEPARATER = env->GetStaticFieldID(AWTKeyEvent, "VK_SEPARATER", "I");
    AWT_VK_SEPARATOR = env->GetStaticFieldID(AWTKeyEvent, "VK_SEPARATOR", "I");
    AWT_VK_SUBTRACT = env->GetStaticFieldID(AWTKeyEvent, "VK_SUBTRACT", "I");
    AWT_VK_DECIMAL = env->GetStaticFieldID(AWTKeyEvent, "VK_DECIMAL", "I");
    AWT_VK_DIVIDE = env->GetStaticFieldID(AWTKeyEvent, "VK_DIVIDE", "I");
    AWT_VK_DELETE = env->GetStaticFieldID(AWTKeyEvent, "VK_DELETE", "I");
    AWT_VK_NUM_LOCK = env->GetStaticFieldID(AWTKeyEvent, "VK_NUM_LOCK", "I");
    AWT_VK_SCROLL_LOCK = env->GetStaticFieldID(AWTKeyEvent, "VK_SCROLL_LOCK", "I");
    AWT_VK_F1 = env->GetStaticFieldID(AWTKeyEvent, "VK_F1", "I");
    AWT_VK_F2 = env->GetStaticFieldID(AWTKeyEvent, "VK_F2", "I");
    AWT_VK_F3 = env->GetStaticFieldID(AWTKeyEvent, "VK_F3", "I");
    AWT_VK_F4 = env->GetStaticFieldID(AWTKeyEvent, "VK_F4", "I");
    AWT_VK_F5 = env->GetStaticFieldID(AWTKeyEvent, "VK_F5", "I");
    AWT_VK_F6 = env->GetStaticFieldID(AWTKeyEvent, "VK_F6", "I");
    AWT_VK_F7 = env->GetStaticFieldID(AWTKeyEvent, "VK_F7", "I");
    AWT_VK_F8 = env->GetStaticFieldID(AWTKeyEvent, "VK_F8", "I");
    AWT_VK_F9 = env->GetStaticFieldID(AWTKeyEvent, "VK_F9", "I");
    AWT_VK_F10 = env->GetStaticFieldID(AWTKeyEvent, "VK_F10", "I");
    AWT_VK_F11 = env->GetStaticFieldID(AWTKeyEvent, "VK_F11", "I");
    AWT_VK_F12 = env->GetStaticFieldID(AWTKeyEvent, "VK_F12", "I");
    AWT_VK_F13 = env->GetStaticFieldID(AWTKeyEvent, "VK_F13", "I");
    AWT_VK_F14 = env->GetStaticFieldID(AWTKeyEvent, "VK_F14", "I");
    AWT_VK_F15 = env->GetStaticFieldID(AWTKeyEvent, "VK_F15", "I");
    AWT_VK_F16 = env->GetStaticFieldID(AWTKeyEvent, "VK_F16", "I");
    AWT_VK_F17 = env->GetStaticFieldID(AWTKeyEvent, "VK_F17", "I");
    AWT_VK_F18 = env->GetStaticFieldID(AWTKeyEvent, "VK_F18", "I");
    AWT_VK_F19 = env->GetStaticFieldID(AWTKeyEvent, "VK_F19", "I");
    AWT_VK_F20 = env->GetStaticFieldID(AWTKeyEvent, "VK_F20", "I");
    AWT_VK_F21 = env->GetStaticFieldID(AWTKeyEvent, "VK_F21", "I");
    AWT_VK_F22 = env->GetStaticFieldID(AWTKeyEvent, "VK_F22", "I");
    AWT_VK_F23 = env->GetStaticFieldID(AWTKeyEvent, "VK_F23", "I");
    AWT_VK_F24 = env->GetStaticFieldID(AWTKeyEvent, "VK_F24", "I");
    AWT_VK_PRINTSCREEN = env->GetStaticFieldID(AWTKeyEvent, "VK_PRINTSCREEN", "I");
    AWT_VK_INSERT = env->GetStaticFieldID(AWTKeyEvent, "VK_INSERT", "I");
    AWT_VK_HELP = env->GetStaticFieldID(AWTKeyEvent, "VK_HELP", "I");
    AWT_VK_META = env->GetStaticFieldID(AWTKeyEvent, "VK_META", "I");
    AWT_VK_BACK_QUOTE = env->GetStaticFieldID(AWTKeyEvent, "VK_BACK_QUOTE", "I");
    AWT_VK_QUOTE = env->GetStaticFieldID(AWTKeyEvent, "VK_QUOTE", "I");
    AWT_VK_KP_UP = env->GetStaticFieldID(AWTKeyEvent, "VK_KP_UP", "I");
    AWT_VK_KP_DOWN = env->GetStaticFieldID(AWTKeyEvent, "VK_KP_DOWN", "I");
    AWT_VK_KP_LEFT = env->GetStaticFieldID(AWTKeyEvent, "VK_KP_LEFT", "I");
    AWT_VK_KP_RIGHT = env->GetStaticFieldID(AWTKeyEvent, "VK_KP_RIGHT", "I");
    AWT_VK_DEAD_GRAVE = env->GetStaticFieldID(AWTKeyEvent, "VK_DEAD_GRAVE", "I");
    AWT_VK_DEAD_ACUTE = env->GetStaticFieldID(AWTKeyEvent, "VK_DEAD_ACUTE", "I");
    AWT_VK_DEAD_CIRCUMFLEX = env->GetStaticFieldID(AWTKeyEvent, "VK_DEAD_CIRCUMFLEX", "I");
    AWT_VK_DEAD_TILDE = env->GetStaticFieldID(AWTKeyEvent, "VK_DEAD_TILDE", "I");
    AWT_VK_DEAD_MACRON = env->GetStaticFieldID(AWTKeyEvent, "VK_DEAD_MACRON", "I");
    AWT_VK_DEAD_BREVE = env->GetStaticFieldID(AWTKeyEvent, "VK_DEAD_BREVE", "I");
    AWT_VK_DEAD_ABOVEDOT = env->GetStaticFieldID(AWTKeyEvent, "VK_DEAD_ABOVEDOT", "I");
    AWT_VK_DEAD_DIAERESIS = env->GetStaticFieldID(AWTKeyEvent, "VK_DEAD_DIAERESIS", "I");
    AWT_VK_DEAD_ABOVERING = env->GetStaticFieldID(AWTKeyEvent, "VK_DEAD_ABOVERING", "I");
    AWT_VK_DEAD_DOUBLEACUTE = env->GetStaticFieldID(AWTKeyEvent, "VK_DEAD_DOUBLEACUTE", "I");
    AWT_VK_DEAD_CARON = env->GetStaticFieldID(AWTKeyEvent, "VK_DEAD_CARON", "I");
    AWT_VK_DEAD_CEDILLA = env->GetStaticFieldID(AWTKeyEvent, "VK_DEAD_CEDILLA", "I");
    AWT_VK_DEAD_OGONEK = env->GetStaticFieldID(AWTKeyEvent, "VK_DEAD_OGONEK", "I");
    AWT_VK_DEAD_IOTA = env->GetStaticFieldID(AWTKeyEvent, "VK_DEAD_IOTA", "I");
    AWT_VK_DEAD_VOICED_SOUND = env->GetStaticFieldID(AWTKeyEvent, "VK_DEAD_VOICED_SOUND", "I");
    AWT_VK_DEAD_SEMIVOICED_SOUND = env->GetStaticFieldID(AWTKeyEvent, "VK_DEAD_SEMIVOICED_SOUND", "I");
    AWT_VK_AMPERSAND = env->GetStaticFieldID(AWTKeyEvent, "VK_AMPERSAND", "I");
    AWT_VK_ASTERISK = env->GetStaticFieldID(AWTKeyEvent, "VK_ASTERISK", "I");
    AWT_VK_QUOTEDBL = env->GetStaticFieldID(AWTKeyEvent, "VK_QUOTEDBL", "I");
    AWT_VK_LESS = env->GetStaticFieldID(AWTKeyEvent, "VK_LESS", "I");
    AWT_VK_GREATER = env->GetStaticFieldID(AWTKeyEvent, "VK_GREATER", "I");
    AWT_VK_BRACELEFT = env->GetStaticFieldID(AWTKeyEvent, "VK_BRACELEFT", "I");
    AWT_VK_BRACERIGHT = env->GetStaticFieldID(AWTKeyEvent, "VK_BRACERIGHT", "I");
    AWT_VK_AT = env->GetStaticFieldID(AWTKeyEvent, "VK_AT", "I");
    AWT_VK_COLON = env->GetStaticFieldID(AWTKeyEvent, "VK_COLON", "I");
    AWT_VK_CIRCUMFLEX = env->GetStaticFieldID(AWTKeyEvent, "VK_CIRCUMFLEX", "I");
    AWT_VK_DOLLAR = env->GetStaticFieldID(AWTKeyEvent, "VK_DOLLAR", "I");
    AWT_VK_EURO_SIGN = env->GetStaticFieldID(AWTKeyEvent, "VK_EURO_SIGN", "I");
    AWT_VK_EXCLAMATION_MARK = env->GetStaticFieldID(AWTKeyEvent, "VK_EXCLAMATION_MARK", "I");
    AWT_VK_INVERTED_EXCLAMATION_MARK = env->GetStaticFieldID(AWTKeyEvent, "VK_INVERTED_EXCLAMATION_MARK", "I");
    AWT_VK_LEFT_PARENTHESIS = env->GetStaticFieldID(AWTKeyEvent, "VK_LEFT_PARENTHESIS", "I");
    AWT_VK_NUMBER_SIGN = env->GetStaticFieldID(AWTKeyEvent, "VK_NUMBER_SIGN", "I");
    AWT_VK_PLUS = env->GetStaticFieldID(AWTKeyEvent, "VK_PLUS", "I");
    AWT_VK_RIGHT_PARENTHESIS = env->GetStaticFieldID(AWTKeyEvent, "VK_RIGHT_PARENTHESIS", "I");
    AWT_VK_UNDERSCORE = env->GetStaticFieldID(AWTKeyEvent, "VK_UNDERSCORE", "I");
    AWT_VK_WINDOWS = env->GetStaticFieldID(AWTKeyEvent, "VK_WINDOWS", "I");
    AWT_VK_CONTEXT_MENU = env->GetStaticFieldID(AWTKeyEvent, "VK_CONTEXT_MENU", "I");
    AWT_VK_FINAL = env->GetStaticFieldID(AWTKeyEvent, "VK_FINAL", "I");
    AWT_VK_CONVERT = env->GetStaticFieldID(AWTKeyEvent, "VK_CONVERT", "I");
    AWT_VK_NONCONVERT = env->GetStaticFieldID(AWTKeyEvent, "VK_NONCONVERT", "I");
    AWT_VK_ACCEPT = env->GetStaticFieldID(AWTKeyEvent, "VK_ACCEPT", "I");
    AWT_VK_MODECHANGE = env->GetStaticFieldID(AWTKeyEvent, "VK_MODECHANGE", "I");
    AWT_VK_KANA = env->GetStaticFieldID(AWTKeyEvent, "VK_KANA", "I");
    AWT_VK_KANJI = env->GetStaticFieldID(AWTKeyEvent, "VK_KANJI", "I");
    AWT_VK_ALPHANUMERIC = env->GetStaticFieldID(AWTKeyEvent, "VK_ALPHANUMERIC", "I");
    AWT_VK_KATAKANA = env->GetStaticFieldID(AWTKeyEvent, "VK_KATAKANA", "I");
    AWT_VK_HIRAGANA = env->GetStaticFieldID(AWTKeyEvent, "VK_HIRAGANA", "I");
    AWT_VK_FULL_WIDTH = env->GetStaticFieldID(AWTKeyEvent, "VK_FULL_WIDTH", "I");
    AWT_VK_HALF_WIDTH = env->GetStaticFieldID(AWTKeyEvent, "VK_HALF_WIDTH", "I");
    AWT_VK_ROMAN_CHARACTERS = env->GetStaticFieldID(AWTKeyEvent, "VK_ROMAN_CHARACTERS", "I");
    AWT_VK_ALL_CANDIDATES = env->GetStaticFieldID(AWTKeyEvent, "VK_ALL_CANDIDATES", "I");
    AWT_VK_PREVIOUS_CANDIDATE = env->GetStaticFieldID(AWTKeyEvent, "VK_PREVIOUS_CANDIDATE", "I");
    AWT_VK_CODE_INPUT = env->GetStaticFieldID(AWTKeyEvent, "VK_CODE_INPUT", "I");
    AWT_VK_JAPANESE_KATAKANA = env->GetStaticFieldID(AWTKeyEvent, "VK_JAPANESE_KATAKANA", "I");
    AWT_VK_JAPANESE_HIRAGANA = env->GetStaticFieldID(AWTKeyEvent, "VK_JAPANESE_HIRAGANA", "I");
    AWT_VK_JAPANESE_ROMAN = env->GetStaticFieldID(AWTKeyEvent, "VK_JAPANESE_ROMAN", "I");
    AWT_VK_KANA_LOCK = env->GetStaticFieldID(AWTKeyEvent, "VK_KANA_LOCK", "I");
    AWT_VK_INPUT_METHOD_ON_OFF = env->GetStaticFieldID(AWTKeyEvent, "VK_INPUT_METHOD_ON_OFF", "I");
    AWT_VK_CUT = env->GetStaticFieldID(AWTKeyEvent, "VK_CUT", "I");
    AWT_VK_COPY = env->GetStaticFieldID(AWTKeyEvent, "VK_COPY", "I");
    AWT_VK_PASTE = env->GetStaticFieldID(AWTKeyEvent, "VK_PASTE", "I");
    AWT_VK_UNDO = env->GetStaticFieldID(AWTKeyEvent, "VK_UNDO", "I");
    AWT_VK_AGAIN = env->GetStaticFieldID(AWTKeyEvent, "VK_AGAIN", "I");
    AWT_VK_FIND = env->GetStaticFieldID(AWTKeyEvent, "VK_FIND", "I");
    AWT_VK_PROPS = env->GetStaticFieldID(AWTKeyEvent, "VK_PROPS", "I");
    AWT_VK_STOP = env->GetStaticFieldID(AWTKeyEvent, "VK_STOP", "I");
    AWT_VK_COMPOSE = env->GetStaticFieldID(AWTKeyEvent, "VK_COMPOSE", "I");
    AWT_VK_ALT_GRAPH = env->GetStaticFieldID(AWTKeyEvent, "VK_ALT_GRAPH", "I");
    AWT_VK_BEGIN = env->GetStaticFieldID(AWTKeyEvent, "VK_BEGIN", "I");
    AWT_VK_UNDEFINED = env->GetStaticFieldID(AWTKeyEvent, "VK_UNDEFINED", "I");
}

/**
 * Translate a AWT key code to a Windows key code.
 *
 * @param env             JNI environment for accessing Java objects
 * @param event           java.awt.event.KeyEvent
 * @param winKeyCode      receives the Windows key code
 * @param winExtendedInfo receives extended info about the key event
 */
void WindowsKeyCodeFromAWTKeyCode(JNIEnv *env, jobject event, WPARAM &winKeyCode, LPARAM &winExtendedInfo)
{
    jint keyCode = env->CallIntMethod(event, AWTKeyEvent_getKeyCode);
    jint location = env->CallIntMethod(event, AWTKeyEvent_getKeyLocation);

    // Windows uses a bit in the LPARAM to indicate that the key pressed is in a non-standard
    // location, for example the right alt and control keys. Windows only distinguishes
    // between "standard" and "extended", extended being the version on the right side of the
    // keyboard. If the AWT event says that the key location is to the right, we'll set
    // the extended bit. Otherwise, leave it at zero.
    if (location == env->GetStaticIntField(AWTKeyEvent, AWT_KEY_LOCATION_RIGHT))
        winExtendedInfo |= EXTENDED_KEY;

    // Virtual keycodes. Note that Windows uses the ASCII codes as virtual
    // keycodes for numbers and letters.
    if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_A))
        winKeyCode = 'A';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_B))
      winKeyCode = 'B';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_C))
      winKeyCode = 'C';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_D))
      winKeyCode = 'D';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_E))
      winKeyCode = 'E';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_F))
      winKeyCode = 'F';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_G))
      winKeyCode = 'G';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_H))
      winKeyCode = 'H';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_I))
      winKeyCode = 'I';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_J))
      winKeyCode = 'J';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_K))
      winKeyCode = 'K';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_L))
      winKeyCode = 'L';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_M))
      winKeyCode = 'M';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_N))
      winKeyCode = 'N';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_O))
      winKeyCode = 'O';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_P))
      winKeyCode = 'P';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_Q))
      winKeyCode = 'Q';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_R))
      winKeyCode = 'R';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_S))
      winKeyCode = 'S';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_T))
      winKeyCode = 'T';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_U))
      winKeyCode = 'U';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_V))
      winKeyCode = 'V';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_W))
      winKeyCode = 'W';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_X))
      winKeyCode = 'X';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_Y))
      winKeyCode = 'Y';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_Z))
      winKeyCode = 'Z';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_0))
      winKeyCode = '0';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_1))
      winKeyCode = '1';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_2))
      winKeyCode = '2';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_3))
      winKeyCode = '3';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_4))
      winKeyCode = '4';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_5))
      winKeyCode = '5';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_6))
      winKeyCode = '6';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_7))
      winKeyCode = '7';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_8))
      winKeyCode = '8';
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_9))
      winKeyCode = '9';

    // Modifier keys. Left and right versions are handled above by setting
    // a bit in the extended data.
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_SHIFT))
        winKeyCode = VK_SHIFT;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_CONTROL))
        winKeyCode = VK_CONTROL;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_ALT))
        winKeyCode = VK_MENU;

   // Special keys.
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_ESCAPE))
        winKeyCode = VK_ESCAPE;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_TAB))
        winKeyCode = VK_TAB;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_CAPS_LOCK))
        winKeyCode = VK_CAPITAL;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_MINUS))
        winKeyCode = VK_OEM_MINUS;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_BACK_SPACE))
        winKeyCode = VK_BACK;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_NUM_LOCK))
        winKeyCode = VK_NUMLOCK;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_SCROLL_LOCK))
        winKeyCode = VK_SCROLL;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_ENTER))
        winKeyCode = VK_RETURN;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_WINDOWS))
    {
        if (location == env->GetStaticIntField(AWTKeyEvent, AWT_KEY_LOCATION_RIGHT))
            winKeyCode = VK_RWIN;
        else
            winKeyCode = VK_LWIN;
    }
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_CONTEXT_MENU))
        winKeyCode = VK_APPS;

    // Punctuation
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_OPEN_BRACKET))
        winKeyCode = VK_OEM_4;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_CLOSE_BRACKET))
        winKeyCode = VK_OEM_6;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_BACK_SLASH))
        winKeyCode = VK_OEM_5;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_SEMICOLON))
        winKeyCode = VK_OEM_1;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_QUOTE))
        winKeyCode = VK_OEM_7;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_COMMA))
        winKeyCode = VK_OEM_COMMA;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_PERIOD))
        winKeyCode = VK_OEM_PERIOD;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_SLASH))
        winKeyCode = VK_OEM_2;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_SPACE))
        winKeyCode = VK_SPACE;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_BACK_QUOTE))
        winKeyCode = VK_OEM_3;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_EQUALS))
        winKeyCode = VK_OEM_PLUS;

    // Non-numpad arrow keys.
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_UP))
        winKeyCode = VK_UP;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_DOWN))
        winKeyCode = VK_DOWN;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_LEFT))
        winKeyCode = VK_LEFT;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_RIGHT))
        winKeyCode = VK_RIGHT;

    // Other keys.
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_HOME))
        winKeyCode = VK_HOME;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_PAGE_UP))
        winKeyCode = VK_PRIOR;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_DELETE))
        winKeyCode = VK_DELETE;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_END))
        winKeyCode = VK_END;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_PAGE_DOWN))
        winKeyCode = VK_NEXT;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_HELP))
        winKeyCode = VK_HELP;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_PRINTSCREEN))
        winKeyCode = VK_SNAPSHOT;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_INSERT))
        winKeyCode = VK_INSERT;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_PAUSE))
        winKeyCode = VK_PAUSE;

    // Numpad keys.
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_CLEAR))
        winKeyCode = VK_CLEAR;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_DIVIDE))
        winKeyCode = VK_DIVIDE;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_MULTIPLY))
        winKeyCode = VK_MULTIPLY;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_SUBTRACT))
        winKeyCode = VK_SUBTRACT;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_ADD))
        winKeyCode = VK_ADD;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_DECIMAL))
        winKeyCode = VK_DECIMAL;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_NUMPAD0))
        winKeyCode = VK_NUMPAD0;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_NUMPAD1))
        winKeyCode = VK_NUMPAD1;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_NUMPAD2))
        winKeyCode = VK_NUMPAD2; 
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_NUMPAD3))
        winKeyCode = VK_NUMPAD3;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_NUMPAD4))
        winKeyCode = VK_NUMPAD4;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_NUMPAD5))
        winKeyCode = VK_NUMPAD5;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_NUMPAD6))
        winKeyCode = VK_NUMPAD6;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_NUMPAD7))
        winKeyCode = VK_NUMPAD7;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_NUMPAD8))
        winKeyCode = VK_NUMPAD8;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_NUMPAD9))
        winKeyCode = VK_NUMPAD9;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_SEPARATER)
             || keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_SEPARATOR))
        winKeyCode = VK_SEPARATOR;

    // Function keys.
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_F1))
        winKeyCode = VK_F1;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_F2))
        winKeyCode = VK_F2;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_F3))
        winKeyCode = VK_F3;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_F4))
        winKeyCode = VK_F4;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_F5))
        winKeyCode = VK_F5;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_F6))
        winKeyCode = VK_F6;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_F7))
        winKeyCode = VK_F7;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_F8))
        winKeyCode = VK_F8;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_F9))
        winKeyCode = VK_F9;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_F10))
        winKeyCode = VK_F10;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_F11))
        winKeyCode = VK_F11;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_F12))
        winKeyCode = VK_F12;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_F13))
        winKeyCode = VK_F13;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_F14))
        winKeyCode = VK_F14;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_F15))
        winKeyCode = VK_F15;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_F16))
        winKeyCode = VK_F16;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_F17))
        winKeyCode = VK_F17;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_F18))
        winKeyCode = VK_F18;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_F19))
        winKeyCode = VK_F19;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_F20))
        winKeyCode = VK_F20;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_F21))
        winKeyCode = VK_F21;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_F22))
        winKeyCode = VK_F22;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_F23))
        winKeyCode = VK_F23;
    else if (keyCode == env->GetStaticIntField(AWTKeyEvent, AWT_VK_F24))
        winKeyCode = VK_F24;

#ifdef DEBUG
    if (winKeyCode == 0 && keyCode != 0)
    {
        ATLTRACE("Unhandled key code: %d", keyCode);
        assert(FALSE && "Unhandled key code");
    }
#endif
}

/**
 * Translate an AWT KeyEvent into a Windows event.
 *
 * @param env JNI environment
 * @param event java.awt.event.KeyEvent
 * @param targetWindow HWND of window to send event to
 */
void PostKeyMsgFromAWTKeyEvent(JNIEnv *env, jobject event, HWND targetWindow)
{
    // Windows uses the WPARAM and LPARAM arguments to identify the key pressed.
    // WPARAM holds the key code, and LPARAM holds extended data.
    //
    // Format of the LPARAM extended key info field (from http://msdn.microsoft.com/en-us/library/ms646280%28VS.85%29.aspx)
    //
    // Bits	    Meaning
    // 0-15	    The repeat count for the current message. The value is the number of times the keystroke is autorepeated as a result
    //          of the user holding down the key. If the keystroke is held long enough, multiple messages are sent. However, the repeat count is not cumulative.
    // 16-23	The scan code. The value depends on the OEM.
    // 24	    Indicates whether the key is an extended key, such as the right-hand ALT and CTRL keys that appear on an enhanced 101- or 102-key keyboard.
    //          The value is 1 if it is an extended key; otherwise, it is 0.
    // 25-28	Reserved; do not use.
    // 29	    The context code. The value is always 0 for a WM_KEYDOWN message.
    // 30	    The previous key state. The value is 1 if the key is down before the message is sent, or it is zero if the key is up.
    // 31	    The transition state. The value is always 0 for a WM_KEYDOWN message.

    WPARAM wParam = 0;
    LPARAM lParam = 1; // Initialize the field with the repeat count as 1, and all other bits 0

    WindowsKeyCodeFromAWTKeyCode(env, event, wParam, lParam);

    UINT message = 0;

    jint eventID = env->CallIntMethod(event, AWTInputEvent_getID);
    if (eventID == env->GetStaticIntField(AWTKeyEvent, AWT_KEY_PRESSED))
    {
        message = WM_KEYDOWN;
    }
    else if (eventID == env->GetStaticIntField(AWTKeyEvent, AWT_KEY_RELEASED))
    {
        message = WM_KEYUP;

        // Set the previous key state and transitions state bits to 1. These bits are always 1
        // for WM_KEYUP
        lParam |= PREVIOUS_KEY_STATE_BIT;
        lParam |= TRANSITION_STATE_BIT;
    }

    if (message != 0)
    {
        PostMessage(targetWindow, message, wParam, lParam);
    }
}

/**
 * Convert an AWT mouse button mask into a Windows mouse button mask.
 *
 * @param env JNI environment
 * @param event java.awt.event.MouseEvent to translate
 * @param button AWT mouse mask
 *
 * @return Windows mouse mask
 */
DWORD WindowsMouseMaskFromAWTMask(JNIEnv *env, jobject event, jint button)
{
    DWORD mask = 0;

    if (button & env->GetStaticIntField(AWTMouseEvent, AWT_BUTTON1_DOWN_MASK))
        mask |= MK_LBUTTON;
    if (button & env->GetStaticIntField(AWTMouseEvent, AWT_BUTTON3_DOWN_MASK))
        mask |= MK_RBUTTON;
    if (button & env->GetStaticIntField(AWTMouseEvent, AWT_BUTTON2_DOWN_MASK))
        mask |= MK_MBUTTON;
    if (env->CallBooleanMethod(event, AWTInputEvent_isShiftDown))
        mask |= MK_SHIFT;
    if (env->CallBooleanMethod(event, AWTInputEvent_isControlDown))
        mask |= MK_CONTROL;

    return mask;
}

/**
 * Convert the coordinates in an AWT MouseEvent to point represented as an LPARAM.
 *
 * @param env JNI environment
 * @param event java.awt.event.MouseEvent to read coordinates from
 *
 * @return the coordinates from event, as an LPARAM
 */ 
LPARAM LParamPointFromAWTEvent(JNIEnv *env, jobject event)
{
    jint x = env->CallIntMethod(event, AWTMouseEvent_getX);
    jint y = env->CallIntMethod(event, AWTMouseEvent_getY);

    return MAKELPARAM(x, y);
}

/**
 * Translate a mouse event into a Windows event.
 *
 * @param env          JNI environment
 * @param event        java.awt.event.MouseEvent to translate
 * @param targetWindow HWND of Window to send event to
 */
void PostMouseMsgFromAWTMouseEvent(JNIEnv *env, jobject event, HWND targetWindow)
{
    jint button = env->CallIntMethod(event, AWTInputEvent_getModifiersEx);
    DWORD windowsButton = WindowsMouseMaskFromAWTMask(env, event, button);

    WPARAM wParam = windowsButton;
    LPARAM lParam = LParamPointFromAWTEvent(env, event);

    int clientX = GET_X_LPARAM(lParam);
    int clientY = GET_Y_LPARAM(lParam);

    UINT message = 0;

    jint eventID = env->CallIntMethod(event, AWTInputEvent_getID);
    if (eventID == env->GetStaticIntField(AWTMouseEvent, AWT_MOUSE_PRESSED))
    {
        if (wParam == MK_LBUTTON)
            message = WM_LBUTTONDOWN;
        else if (wParam == MK_RBUTTON)
            message = WM_RBUTTONDOWN;
        else if (wParam == MK_MBUTTON)
            message = WM_MBUTTONDOWN;
        else
            message = WM_LBUTTONDOWN; // Default to left button
    }
    else if (eventID == env->GetStaticIntField(AWTMouseEvent, AWT_MOUSE_RELEASED))
    {
        if (wParam == MK_LBUTTON)
            message = WM_LBUTTONUP;
        else if (wParam == MK_RBUTTON)
            message = WM_RBUTTONUP;
        else if (wParam == MK_MBUTTON)
            message = WM_MBUTTONUP;
        else
            message = WM_LBUTTONUP; // Default to left button
    }
    else if ((eventID == env->GetStaticIntField(AWTMouseEvent, AWT_MOUSE_MOVED))
             || (eventID == env->GetStaticIntField(AWTMouseEvent, AWT_MOUSE_DRAGGED)))
    {
        message = WM_MOUSEMOVE;
    }
    else if (eventID == env->GetStaticIntField(AWTMouseEvent, AWT_MOUSE_CLICKED))
    {
        // Windows has a special message for double clicks, but uses the normal mouse up and mouse
        // down messages for single clicks, so the only AWT mouse clicked event that we need to
        // translate is a double click.
        jint clickCount = env->CallIntMethod(event, AWTMouseEvent_getClickCount);

        if (clickCount % 2 == 0)
        {
            if (wParam == MK_LBUTTON)
                message = WM_LBUTTONDBLCLK;
            else if (wParam == MK_RBUTTON)
                message = WM_RBUTTONDBLCLK;
            else if (wParam == MK_MBUTTON)
                message = WM_MBUTTONDBLCLK;
            else
                message = WM_LBUTTONDBLCLK; // Default to left button
        }
    }

    PostMessage(targetWindow, message, wParam, lParam);
}

/**
 * Translate a mouse wheel event into a Windows event.
 *
 * @param env          JNI environment
 * @param event        java.awt.event.MouseWheelEvent to translate
 * @param targetWindow HWND of Window to send event to
 */
void PostMouseWheelMsgFromAWTMouseWheelEvent(JNIEnv *env, jobject event, HWND targetWindow)
{
    jint units = env->CallIntMethod(event, AWTMouseWheelEvent_getWheelRotation);

    jint button = env->CallIntMethod(event, AWTInputEvent_getModifiersEx);
    DWORD windowsButton = WindowsMouseMaskFromAWTMask(env, event, button);

    // The high word of WPARAM holds the amount that the wheel was rotated as a multiple of WHEEL_DELTA.
    // The low order word indicates which buttons are down.
    // Windows uses positive values for rotation away from the user, opposite to AWT, so we need to negate
    // the rotation units.
    WPARAM wParam = MAKEWPARAM(windowsButton, -units * WHEEL_DELTA);

    // LPARAM holds the mouse point
    LPARAM lParam = LParamPointFromAWTEvent(env, event);

    int clientX = GET_X_LPARAM(lParam);
    int clientY = GET_Y_LPARAM(lParam);

    // Get the position of the window on screen. WM_MOUSEWHEEL sends coordinates relative to the corner of the
    // screen, not the corner of the window.
    RECT windowRect;
    GetWindowRect(targetWindow, &windowRect);

    // Convert window coordinates to screen coordinates.
    lParam = MAKELPARAM(clientX + windowRect.left, clientY + windowRect.top);

    // Send WM_SIM_MOUSEWHEEL instead of WM_MOUSEWHEEL because WM_MOUSEWHEEL will be sent back to the
    // WebViewWindow from the browser component if the browser can't scroll. There would be no way
    // for the window to tell the simulated event that we send from the event sent back from the browser.
    PostMessage(targetWindow, WM_SIM_MOUSEWHEEL, wParam, lParam);
}

void PostMsgFromAWTEvent(JNIEnv *env, jobject event, HWND targetWindow)
{
    if (env->IsInstanceOf(event, AWTKeyEvent))
        PostKeyMsgFromAWTKeyEvent(env, event, targetWindow);

    // Must check mouse wheel event before mouse event because mouse wheel event is a subclass of MouseEvent.
    else if (env->IsInstanceOf(event, AWTMouseWheelEvent))
        PostMouseWheelMsgFromAWTMouseWheelEvent(env, event,targetWindow);
        
    else if (env->IsInstanceOf(event, AWTMouseEvent))
        PostMouseMsgFromAWTMouseEvent(env, event, targetWindow);
}
