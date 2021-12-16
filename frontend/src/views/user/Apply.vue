<template>
  <v-card height="100%">
    <v-tabs
      v-model="window"
      dark
      grow
      background-color="indigo darken-1"
    >
      <v-tab>
        <v-icon>mdi-timer-sand-full</v-icon>
        审核中申请
      </v-tab>
      <v-tab>
        <v-icon>mdi-cloud-check</v-icon>
        已通过申请
      </v-tab>
      <v-tab>
        <v-icon>mdi-cloud-alert</v-icon>
        未通过申请
      </v-tab>
      <v-tab>
        <v-icon>mdi-menu</v-icon>
        所有申请
      </v-tab>
    </v-tabs>

    <v-window
      v-model="window"
    >

      <!-- 审核中 -->
      <v-window-item>
        
      </v-window-item>
      
      <!-- 已通过申请 -->
      <v-window-item>
        
      </v-window-item>
      
      <!-- 未通过申请 -->
      <v-window-item>
        
      </v-window-item>
      
      <!-- 所有申请 -->
      <v-window-item>
        <v-card>
          <v-list
            class="text-left"
          >
            <v-list-group
              v-for="item in types"
              :key="item.type"
              v-show="item.items.length != 0"
              prepend-icon="mdi-arrow-down-drop-circle"
              sub-group
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
                class="pl-8 item-color"
              >
              
                <v-chip
                  v-if="child.status == '审核通过'"
                  dark
                >
                  <v-icon color="green lighten-3">
                    mdi-checkbox-marked
                  </v-icon>
                  <span>已通过</span>
                </v-chip>
                <v-chip
                  v-if="child.status == '审核不通过'"
                  dark
                >
                  <v-icon color="red lighten-3">
                    mdi-close-box
                  </v-icon>
                  <span>未通过</span>
                </v-chip>
                <v-chip
                  v-if="child.status == '审核中'"
                  dark
                >
                  <v-icon color="blue lighten-4">
                    mdi-timer-sand-full
                  </v-icon>
                  <span>审核中</span>
                </v-chip>

                <v-list-item-content class="ml-2">
                  {{child.type}} : {{child.title}}
                </v-list-item-content>

                <v-spacer></v-spacer>

                <v-icon>
                  mdi-clock
                </v-icon>
                <v-list-item-content>
                  {{child.time}}
                </v-list-item-content>
                
                <v-spacer></v-spacer>

                <message-dialog :message="child"></message-dialog>
                
              </v-list-item>
              <v-divider></v-divider>
            </v-list-group>
          </v-list>
        </v-card>
      </v-window-item>
    </v-window>

  </v-card>
</template>

<script>
import MessageDialog from "../../components/MessageDialog.vue"
export default {
  components: {
    MessageDialog,
  },
  data() {
    return {
      types: [
        {
          type: "学者认证",
          icon: "mdi-school-outline",
          items: [
            { 
              type: "修改",
              title: "认证申请",
              status: "审核不通过",
              "time": "2021-08-17 19:26",
            },
          ]
        },
        {
          type: "门户管理",
          icon: "mdi-home-outline",
          items: [
            { 
              type: "修改",
              title: "门户修改",
              status: "审核中",
              "time": "2021-08-17 19:26",
            }
          ]
        },
        {
          type: "论文管理",
          icon: "mdi-book-open-page-variant-outline",
          items: [
            { 
              type: "修改",
              title: "title",
              status: "审核不通过",
              "time": "2021-08-17 19:26",
             },
            { 
              type: "修改",
              title: "title",
              status: "审核通过",
              "time": "2021-08-17 19:26",
            },
            { 
              type: "修改",
              title: "title",
              status: "审核中",
              "time": "2021-08-17 19:26",
            },
          ]
        },
        {
          type: "专利转让",
          icon: "mdi-swap-horizontal-circle-outline",
          items: [
            { 
              type: "修改",
              title: "转让",
              status: "审核中",
              "time": "2021-08-17 19:26",
            }
          ]
        },
      ],
      window: 0,
      
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

<style scoped>
.item-color {
  background-color: #FAFAFA;
}
</style>