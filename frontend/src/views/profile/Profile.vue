<template>
  <v-container>

    <v-navigation-drawer>

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
          v-for="[icon, text] in links"
          :key="icon"
          link
        >
          <v-list-item-icon>
            <v-icon>{{ icon }}</v-icon>
          </v-list-item-icon>

          <v-list-item-title>{{ text }}</v-list-item-title>
        </v-list-item>
      </v-list>
    </v-navigation-drawer>

    <v-main>
      <router-view></router-view>
    </v-main>
  </v-container>
</template>

<script>
export default {
  data() {
    return {
      links: [
        ['mdi-inbox-arrow-down', '个人资料'],
        ['mdi-send', '申请信息'],
        ['mdi-delete', '个人消息'],
      ],
    }
  }
}
</script>

<style>

</style>