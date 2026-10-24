#!/usr/bin/env -S PYTHONPATH=../../../tools/extract-utils python3
#
# SPDX-FileCopyrightText: 2024 The LineageOS Project
# SPDX-License-Identifier: Apache-2.0
#

from extract_utils.fixups_blob import (
    blob_fixup,
    blob_fixups_user_type,
)
from extract_utils.fixups_lib import (
    lib_fixups,
    lib_fixups_user_type,
)
from extract_utils.main import (
    ExtractUtils,
    ExtractUtilsModule,
)

# High-speed (slow motion) encoder profiles for the back camera; the stock
# media_profiles only carries them for the other camera ids.
CAMERA0_HIGHSPEED_PROFILES = '''    <EncoderProfile quality="highspeed1080p" fileFormat="mp4" duration="30">
      <Video codec="h264"
             bitRate="42000000"
             width="1920"
             height="1080"
             frameRate="120" />

      <!-- audio setting is ignored -->
      <Audio codec="aac"
             bitRate="96000"
             sampleRate="48000"
             channels="1" />
    </EncoderProfile>

    <EncoderProfile quality="highspeedhigh" fileFormat="mp4" duration="30">
      <Video codec="h264"
             bitRate="24000000"
             width="1280"
             height="720"
             frameRate="240" />

      <!-- audio setting is ignored -->
      <Audio codec="aac"
             bitRate="96000"
             sampleRate="48000"
             channels="1" />
    </EncoderProfile>

    <EncoderProfile quality="highspeed720p" fileFormat="mp4" duration="30">
      <Video codec="h264"
             bitRate="24000000"
             width="1280"
             height="720"
             frameRate="240" />

      <!-- audio setting is ignored -->
      <Audio codec="aac"
             bitRate="96000"
             sampleRate="48000"
             channels="1" />
    </EncoderProfile>

    <EncoderProfile quality="highspeed480p" fileFormat="mp4" duration="30">
      <Video codec="h264"
             bitRate="12000000"
             width="640"
             height="480"
             frameRate="240" />

      <Audio codec="aac"
             bitRate="156000"
             sampleRate="48000"
             channels="2" />
    </EncoderProfile>

'''
CAMERA0_END = '    </CamcorderProfiles>\n    <!-- Front Camera -->'


blob_fixups: blob_fixups_user_type = {
    'system/framework/WfdCommon.jar': blob_fixup()
        .apktool_patch('blob-patches/WfdCommon.patch'),
    'system_ext/lib64/libwfdservice.so': blob_fixup()
        .replace_needed('android.media.audio.common.types-V4-cpp.so', 'android.media.audio.common.types-V5-cpp.so'),
    'vendor/etc/media_profiles_V1_0.xml': blob_fixup()
        .regex_replace(CAMERA0_END, CAMERA0_HIGHSPEED_PROFILES + CAMERA0_END)
        .regex_replace('maxHFRMode="120"  />', 'maxHFRMode="240"  />'),
    'vendor/etc/msm_irqbalance.conf': blob_fixup()
        .regex_replace('IGNORED_IRQ=19,21,39', 'IGNORED_IRQ=19,21,38'),
    'vendor/etc/init/android.hardware.drm@1.3-service.widevine.rc': blob_fixup()
        .regex_replace(r'writepid.*', 'task_profiles ProcessCapacityHigh HighPerformance'),
    'vendor/etc/init/android.hardware.neuralnetworks@1.3-service-qti.rc': blob_fixup()
        .regex_replace(r'writepid.*', 'task_profiles NNApiHALPerformance'),
    'vendor/etc/init/init.mi_thermald.rc': blob_fixup()
        .regex_replace('.*seclabel u:r:mi_thermald:s0\n', ''),
    'vendor/etc/init/vendor.qti.media.c2@1.0-service.rc': blob_fixup()
        .regex_replace(r'writepid.*', 'task_profiles ProcessCapacityHigh HighPerformance'),
    'vendor/etc/seccomp_policy/atfwd@2.0.policy': blob_fixup()
        .add_line_if_missing('gettid: 1'),
    'vendor/lib64/mediadrm/libwvdrmengine.so': blob_fixup()
        .add_needed('libcrypto_shim.so'),
    'vendor/lib64/libdpps.so': blob_fixup()
        .replace_needed('libtinyxml2.so', 'libtinyxml2-v34.so'),
    'vendor/lib64/libwvhidl.so': blob_fixup()
        .add_needed('libcrypto_shim.so'),
}  # fmt: skip


def lib_fixup_vendor_suffix(lib: str, partition: str, *args, **kwargs):
    return f'{lib}_{partition}' if partition == 'vendor' else None


lib_fixups: lib_fixups_user_type = {
    **lib_fixups,
    (
        'com.qualcomm.qti.dpm.api@1.0',
        'libmmosal',
        'vendor.qti.hardware.fm@1.0',
        'vendor.qti.hardware.wifidisplaysession@1.0',
        'vendor.qti.imsrtpservice@3.0',
    ): lib_fixup_vendor_suffix,
}

namespace_imports = [
    'device/xiaomi/sm8150-common',
    'hardware/qcom-caf/sm8150',
    'hardware/qcom-caf/wlan',
    'hardware/xiaomi',
    'vendor/qcom/opensource/commonsys-intf/display',
    'vendor/qcom/opensource/commonsys/display',
    'vendor/qcom/opensource/dataservices',
    'vendor/qcom/opensource/display',
]

module = ExtractUtilsModule(
    'sm8150-common',
    'xiaomi',
    blob_fixups=blob_fixups,
    lib_fixups=lib_fixups,
    namespace_imports=namespace_imports,
)

module.add_proprietary_file('proprietary-files-fm.txt').add_copy_files_guard(
    'TARGET_HAS_FM', 'true'
)

module.add_proprietary_file('proprietary-files-phone.txt').add_copy_files_guard(
    'TARGET_IS_TABLET', 'true', invert=True
)

if __name__ == '__main__':
    utils = ExtractUtils.device(module)
    utils.run()
