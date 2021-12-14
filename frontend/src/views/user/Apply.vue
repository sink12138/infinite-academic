<template>
  <v-card>
    <v-list class="text-left">
      <v-list-group
        v-for="item in types"
        :key="item.type"
        v-model="item.active"
        v-show="item.items.length != 0"
        no-action
      >
        <template v-slot:activator>
          <v-icon>{{item.icon}}</v-icon>
          <v-list-item-content>
            <v-list-item-title v-text="item.type"></v-list-item-title>
          </v-list-item-content>
        </template>

        <v-list-item
          v-for="child in item.items"
          :key="child.title"
        >
          <v-list-item-content>
            <v-list-item-title v-text="child.title"></v-list-item-title>
          </v-list-item-content>
        </v-list-item>
      </v-list-group>
    </v-list>
  </v-card>
</template>

<script>
export default {
  data() {
    return {
      types: [
        {
          type: "学者认证",
          active: false,
          icon: "mdi-account",
          items: [
            { title: "test" },
            { title: "test" },
            { title: "test" },
          ]
        },
        {
          type: "门户认领",
          active: false,
          icon: "mdi-account",
          items: [
            { title: "test" }
          ]
        },
        {
          type: "修改门户",
          active: false,
          icon: "mdi-account",
          items: [
            { title: "test" }
          ]
        },
        {
          type: "添加论文",
          active: false,
          icon: "mdi-account",
          items: [
            { title: "test" }
          ]
        },
        {
          type: "修改论文",
          active: false,
          icon: "mdi-account",
          items: [
            { title: "test" }
          ]
        },
        {
          type: "移除论文",
          active: false,
          icon: "mdi-account",
          items: [
            { title: "test" }
          ]
        },
        {
          type: "专利转让",
          active: false,
          icon: "mdi-account",
          items: [
            
          ]
        },
      ]
    }
  },
  methods: {
    getAllApplications() {
      this.$axios({
        method: "get",
        url: "/api/account/application/list",
        params: {
          page: 0,
          size: 0,
        }
      }).then(res => {
        console.log(res.data)
      }).catch(error => {
        console.log(error)
      })
    }
  }
}
</script>

<style>

</style>