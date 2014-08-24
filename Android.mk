LOCAL_PATH:= $(call my-dir)

ifneq ($(TARGET_RECOVERY_FSTAB),)
  recovery_fstab := $(strip $(wildcard $(TARGET_RECOVERY_FSTAB)))
else
  recovery_fstab := $(strip $(wildcard $(TARGET_DEVICE_DIR)/recovery.fstab))
endif

ALTERNATE_IS_INTERNAL := false
ifneq ($(recovery_fstab),)
  recovery_fstab := $(ANDROID_BUILD_TOP)/$(recovery_fstab)
  ifneq ($(shell grep "/emmc" $(recovery_fstab)),)
  ALTERNATE_IS_INTERNAL := true
  endif
endif

include $(CLEAR_VARS)

LOCAL_JAVA_LIBRARIES := bouncycastle conscrypt telephony-common telephony-msim
LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4 android-support-v13 jsr305 libGoogleAnalyticsV2 libMoKeePushService libDashClockAPI volley alipay_msp payecoplugin UPPayAssistEx UPPayPluginEx WanpuPay PayPal

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := \
        $(call all-java-files-under, src) \
        src/com/android/settings/EventLogTags.logtags

LOCAL_SRC_FILES += \
        src/com/android/display/IPPService.aidl

LOCAL_PACKAGE_NAME := Settings
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_AAPT_INCLUDE_ALL_RESOURCES := true
LOCAL_AAPT_FLAGS += --extra-packages com.koushikdutta.superuser:com.koushikdutta.widgets:com.mokee.helper --auto-add-overlay

LOCAL_SRC_FILES += $(call all-java-files-under,../../../external/koush/Superuser/Superuser/src) $(call all-java-files-under,../../../external/koush/Widgets/Widgets/src) $(call all-java-files-under,../../../external/mokee/MoKeeHelper/MoKeeHelper/src)

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res $(LOCAL_PATH)/../../../external/koush/Widgets/Widgets/res $(LOCAL_PATH)/../../../external/koush/Superuser/Superuser/res $(LOCAL_PATH)/../../../external/mokee/MoKeeHelper/MoKeeHelper/res-pay $(LOCAL_PATH)/../../../external/mokee/MoKeeHelper/MoKeeHelper/res

ifeq ($(ALTERNATE_IS_INTERNAL), true)
  LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/../../../external/mokee/MoKeeHelper/MoKeeHelper/res-compat $(LOCAL_RESOURCE_DIR)
endif

LOCAL_ASSET_DIR := $(LOCAL_PATH)/assets

LOCAL_JAVA_LIBRARIES += org.mokee.hardware

include $(BUILD_PACKAGE)

# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
