LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_SRC_FILES :=  serial_com.cpp	

LOCAL_LDLIBS += -llog

LOCAL_MODULE:= libdafeng_serial_assistance

include $(BUILD_SHARED_LIBRARY)                