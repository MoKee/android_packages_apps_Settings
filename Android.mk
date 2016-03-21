LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_JAVA_LIBRARIES := bouncycastle conscrypt telephony-common ims-common org.apache.http.legacy
LOCAL_STATIC_JAVA_LIBRARIES := \
    android-support-design \
    android-support-v4 \
    android-support-v7-appcompat \
    android-support-v13 \
    mokee-support-widget \
    jsr305 \
    org.mokee.platform.internal \
    libGoogleAnalyticsV3 \
    libGoogleAdMobAdsSdk \
    libMoKeePushService \
    volley

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := \
        $(call all-java-files-under, src) \
        src/com/android/settings/EventLogTags.logtags

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res \
        frameworks/support/design/res \
        frameworks/support/mk/widget/res \
        frameworks/support/v7/appcompat/res

LOCAL_SRC_FILES += \
        src/com/android/display/IPPService.aidl

LOCAL_PACKAGE_NAME := Settings
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

LOCAL_PROGUARD_FLAGS := -ignorewarnings -include build/core/proguard_basic_keeps.flags

LOCAL_PROGUARD_ENABLED := nosystem

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_AAPT_INCLUDE_ALL_RESOURCES := true
LOCAL_AAPT_FLAGS := \
        --auto-add-overlay \
        --extra-packages android.support.design \
        --extra-packages android.support.v7.appcompat \
        --extra-packages com.mokee.helper \
        --extra-packages mokee.support.widget
LOCAL_SRC_FILES += $(call all-java-files-under,../../../external/mokee/MoKeeHelper/MoKeeHelper/src)
LOCAL_RESOURCE_DIR := $(LOCAL_RESOURCE_DIR) $(LOCAL_PATH)/../../../external/mokee/MoKeeHelper/MoKeeHelper/res

ifneq ($(INCREMENTAL_BUILDS),)
    LOCAL_PROGUARD_ENABLED := disabled
    LOCAL_JACK_ENABLED := incremental
endif
LOCAL_JACK_ENABLED := disabled

include frameworks/opt/setupwizard/navigationbar/common.mk
include frameworks/opt/setupwizard/library/common.mk
include frameworks/base/packages/SettingsLib/common.mk

include $(BUILD_PACKAGE)

# Use the following include to make our test apk.
ifeq (,$(ONE_SHOT_MAKEFILE))
include $(call all-makefiles-under,$(LOCAL_PATH))
endif
