# CONSTANTS ========================================================================================
LOCAL_PATH  := $(call my-dir)


# ADJUSTABLE VARIABLES =============================================================================
external := $(NDK_APP_PROJECT_PATH)/external
prefix   := data/data/net.sourceforge.lifeograph/app_opt
LOCAL    := $(external)/$(prefix)


# libgcrypt
include $(CLEAR_VARS)
LOCAL_MODULE    := libgcrypt
LOCAL_SRC_FILES := $(LOCAL)/lib/libgcrypt.so
include $(PREBUILT_SHARED_LIBRARY)


# libgpg-error
include $(CLEAR_VARS)
LOCAL_MODULE    := libgpg-error
LOCAL_SRC_FILES := $(LOCAL)/lib/libgpg-error.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL)/include
include $(PREBUILT_SHARED_LIBRARY)


# JNI
include $(CLEAR_VARS)
LOCAL_MODULE    := lifeocrypt
LOCAL_SRC_FILES := lifeocrypt.c helpers.c
LOCAL_SHARED_LIBRARIES := libgpg-error libgcrypt
include $(BUILD_SHARED_LIBRARY)
