LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)


OpenCV_INSTALL_MODULES := on
OpenCV_CAMERA_MODULES := off

OPENCV_LIB_TYPE :=STATIC

ifeq ("$(wildcard $(OPENCV_MK_PATH))","")
#include ..\..\..\..\native\jni\OpenCV.mk
include C:\Users\lijialin\AppData\Local\Android\AndroidStudioProjects\DreameraJni\native\jni\OpenCV.mk

else
include $(OPENCV_MK_PATH)
endif

LOCAL_MODULE := OpenCV

LOCAL_SRC_FILES := neu_dreamerajni_utils_Canny.cpp
LOCAL_SRC_FILES += neu_dreamerajni_utils_Smooth.cpp

#LOCAL_LDLIBS +=  -lm -llog
LOCAL_LDLIBS += -llog

include $(BUILD_SHARED_LIBRARY)