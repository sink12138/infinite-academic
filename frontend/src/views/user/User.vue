<template>
  <v-app id="inspire">
    <v-navigation-drawer app>

      <v-sheet
        color="grey lighten-4"
        class="pa-4"
      >
        <v-avatar
          size="64"
        >
          <v-icon size="64">mdi-account-circle</v-icon>
        </v-avatar>

        <div>NAME</div>
        <div>123456@buaa.edu.cn</div>
      </v-sheet>

      <v-divider></v-divider>

      <v-list>
        <v-list-item
          v-for="[icon, text, url] in links"
          link
          :key="icon"
          :to="url"
        >
          <v-list-item-icon>
            <v-icon>{{ icon }}</v-icon>
          </v-list-item-icon>

          <v-list-item-title>{{ text }}</v-list-item-title>
        </v-list-item>
      </v-list>
    </v-navigation-drawer>

    <v-main app>
      <router-view></router-view>
    </v-main>

  </v-app>
</template>

<script>
export default {
  data() {
    return {
      links: [
        ['mdi-inbox-arrow-down', '个人资料', '/user/profile'],
        ['mdi-send', '申请信息', '/user/apply'],
        ['mdi-delete', '个人消息', '/user/message'],
        ['mdi-message-text','专利转移','/user/patenttransfer'],
        ['mdi-account','学者认证','/user/scholarIdentity']
      ],
    }
  }
}
</script>

<style>

</style>
