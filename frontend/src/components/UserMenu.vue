<template>
  <div class="text-center">
    <v-menu
      open-on-hover
      offset-y
    >
      <template v-slot:activator="{ on, attrs }">
        <v-btn
          icon
          v-bind="attrs"
          v-on="on"
        >
          <v-icon>
            mdi-account-circle
          </v-icon>
        </v-btn>
      </template>

      <v-list dense>
        <v-list-item
          v-for="(item, index) in items"
          :key="index"
          @click="href(item.url)"
        >
          <v-list-item-content>{{ item.title }}</v-list-item-content>
        </v-list-item>
      </v-list>
    </v-menu>
  </div>
</template>

<script>
export default {
  data() {
    return {
      items: [
        {
          title: "个人中心",
          url: "/user/profile"
        },
        {
          title: "认证学者",
          url: "scholarIdentity"
        }
      ]
    }
  },
  methods: {
    href(url) {
      this.$router.push({ path:url })
    }
  }
}
</script>

<style>

</style>