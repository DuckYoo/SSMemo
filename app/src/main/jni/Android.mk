LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := NDKTest
LOCAL_SRC_FILES := main.cpp convertForShow.cpp LetterSeperate.cpp
LOCAL_LDLIBS := -llog -ljnigraphics
LOCAL_LDFLAGS += -ljnigraphics

include $(BUILD_SHARED_LIBRARY)
APP_OPTIM := debug
LOCAL_CFLAGS := -g