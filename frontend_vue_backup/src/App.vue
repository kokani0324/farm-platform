<script setup>
import {
  NConfigProvider,
  NMessageProvider,
  NDialogProvider,
  NLoadingBarProvider,
  zhTW,
  dateZhTW,
} from 'naive-ui'
import { themeOverrides } from '@/theme'
</script>

<template>
  <n-config-provider
    :theme-overrides="themeOverrides"
    :locale="zhTW"
    :date-locale="dateZhTW"
  >
    <n-loading-bar-provider>
      <n-dialog-provider>
        <n-message-provider>
          <RouterView />
        </n-message-provider>
      </n-dialog-provider>
    </n-loading-bar-provider>
  </n-config-provider>
</template>
